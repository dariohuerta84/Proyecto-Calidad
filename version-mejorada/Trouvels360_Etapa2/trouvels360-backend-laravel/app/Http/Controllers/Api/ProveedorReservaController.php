<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\Servicio;
use App\Services\ReservaPresenter;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Pagination\LengthAwarePaginator;
use Illuminate\Support\Facades\DB;

/**
 * Portal del proveedor:
 *  - TRV-507 Reservas recibidas (listado y detalle).
 *  - TRV-204 Aceptar / rechazar solicitudes de hotel.
 *  - TRV-501 Indicadores.
 */
class ProveedorReservaController extends Controller
{
    private function soloProveedor(Request $request): ?JsonResponse
    {
        return $request->user()->esProveedor()
            ? null
            : response()->json(['message' => 'Esta sección es solo para proveedores.'], 403);
    }

    /** IDs de servicios propios (incluye eliminados para el historial). */
    private function serviciosPropios(Request $request, ?string $tipo = null)
    {
        return Servicio::withTrashed()->where('proveedor_id', $request->user()->id)
            ->when($tipo, fn ($q) => $q->where('tipo', $tipo))
            ->pluck('id');
    }

    /** GET /api/proveedor/reservas?tipo=hotel|tour&estado=&servicio_id=&page=&per_page= */
    public function index(Request $request): JsonResponse
    {
        if ($r = $this->soloProveedor($request)) {
            return $r;
        }
        $v = $request->validate([
            'tipo'        => ['nullable', 'in:hotel,tour'],
            'estado'      => ['nullable', 'in:pendiente,confirmada,rechazada,cancelada'],
            'servicio_id' => ['nullable', 'integer'],
            'per_page'    => ['nullable', 'integer', 'between:1,100'],
            'page'        => ['nullable', 'integer', 'min:1'],
        ]);

        $propios = $this->serviciosPropios($request);
        if (!empty($v['servicio_id'])) {
            $propios = $propios->intersect([(int) $v['servicio_id']]);
        }

        $items = collect();
        if (($v['tipo'] ?? 'hotel') === 'hotel' || empty($v['tipo'])) {
            ReservaHabitacion::with('habitacion')
                ->whereHas('habitacion', fn ($q) => $q->withTrashed()->whereIn('servicio_id', $propios))
                ->when($v['estado'] ?? null, fn ($q, $e) => $q->where('estado', $e))
                ->get()
                ->each(fn ($r) => $items->push(ReservaPresenter::hotel($r, true)));
        }
        if (($v['tipo'] ?? 'tour') === 'tour' || empty($v['tipo'])) {
            ReservaTour::with('salida')
                ->whereHas('salida', fn ($q) => $q->withTrashed()->whereIn('servicio_id', $propios))
                ->when($v['estado'] ?? null, fn ($q, $e) => $q->where('estado', $e))
                ->get()
                ->each(fn ($r) => $items->push(ReservaPresenter::tour($r, true)));
        }

        // Pendientes primero; luego lo más reciente.
        $items = $items->sort(function ($a, $b) {
            $pa = $a['estado'] === 'pendiente' ? 0 : 1;
            $pb = $b['estado'] === 'pendiente' ? 0 : 1;
            return $pa <=> $pb ?: strcmp((string) $b['creado_en'], (string) $a['creado_en']);
        })->values();

        $perPage = (int) ($v['per_page'] ?? 15);
        $page = (int) ($v['page'] ?? 1);
        $pag = new LengthAwarePaginator($items->forPage($page, $perPage)->values(), $items->count(), $perPage, $page);

        return response()->json(ReservaPresenter::paginado($pag, $pag->items()));
    }

    /** GET /api/proveedor/reservas/hotel/{id} — 404 si no es de un servicio propio. */
    public function showHotel(Request $request, int $id): JsonResponse
    {
        if ($r = $this->soloProveedor($request)) {
            return $r;
        }
        $reserva = $this->reservaHotelPropia($request, $id);
        if (!$reserva) {
            return response()->json(['message' => 'Reserva no encontrada.'], 404);
        }

        return response()->json(['data' => ReservaPresenter::hotel($reserva, true)]);
    }

    private function reservaHotelPropia(Request $request, int $id, bool $lock = false): ?ReservaHabitacion
    {
        $q = ReservaHabitacion::with('habitacion')->where('id', $id);
        if ($lock) {
            $q->lockForUpdate();
        }
        $r = $q->first();
        if (!$r) {
            return null;
        }
        $propietario = Servicio::withTrashed()->where('id', $r->habitacion?->servicio_id)->value('proveedor_id');

        return (int) $propietario === (int) $request->user()->id ? $r : null;
    }

    /** POST /api/proveedor/reservas-habitaciones/{id}/aceptar  (TRV-204) */
    public function aceptar(Request $request, int $id): JsonResponse
    {
        return $this->decidir($request, $id, 'confirmada');
    }

    /** POST /api/proveedor/reservas-habitaciones/{id}/rechazar {motivo}  (TRV-204) */
    public function rechazar(Request $request, int $id): JsonResponse
    {
        $request->validate(['motivo' => ['required', 'string', 'min:10', 'max:300']], [
            'motivo.required' => 'Indica el motivo del rechazo.',
            'motivo.min'      => 'El motivo debe tener al menos 10 caracteres.',
        ]);

        return $this->decidir($request, $id, 'rechazada', $request->input('motivo'));
    }

    private function decidir(Request $request, int $id, string $nuevo, ?string $motivo = null): JsonResponse
    {
        if ($r = $this->soloProveedor($request)) {
            return $r;
        }

        return DB::transaction(function () use ($request, $id, $nuevo, $motivo) {
            // ¿Existe y es de otro proveedor? → 403 ; ¿no existe? → 404
            $existe = ReservaHabitacion::with('habitacion')->find($id);
            if (!$existe) {
                return response()->json(['message' => 'Reserva no encontrada.'], 404);
            }
            $reserva = $this->reservaHotelPropia($request, $id, true);
            if (!$reserva) {
                return response()->json(['message' => 'Esta reserva no pertenece a tus hoteles.'], 403);
            }
            if ($reserva->estado !== 'pendiente') {
                return response()->json([
                    'message' => "La reserva ya no está pendiente (estado actual: {$reserva->estado}).",
                    'estado'  => $reserva->estado,
                ], 409);
            }

            // Rechazar libera las unidades automáticamente: 'rechazada' no está en OCUPAN.
            $reserva->update([
                'estado'         => $nuevo,
                'motivo_rechazo' => $motivo,
                'decidido_por'   => $request->user()->id,
                'decidido_en'    => now(),
            ]);

            return response()->json([
                'message' => $nuevo === 'confirmada' ? 'Reserva confirmada.' : 'Solicitud rechazada.',
                'data'    => ReservaPresenter::hotel($reserva->fresh(), true),
            ]);
        });
    }

    /** GET /api/proveedor/indicadores  (TRV-501) */
    public function indicadores(Request $request): JsonResponse
    {
        if ($r = $this->soloProveedor($request)) {
            return $r;
        }
        $propios = $this->serviciosPropios($request);
        $inicioMes = now()->startOfMonth();

        $hotel = ReservaHabitacion::whereHas('habitacion', fn ($q) => $q->withTrashed()->whereIn('servicio_id', $propios));
        $tour = ReservaTour::whereHas('salida', fn ($q) => $q->withTrashed()->whereIn('servicio_id', $propios));

        $porEstado = function ($q) {
            $base = ['pendiente' => 0, 'confirmada' => 0, 'rechazada' => 0, 'cancelada' => 0];
            return array_merge($base, (clone $q)->selectRaw('estado, COUNT(*) as n')->groupBy('estado')->pluck('n', 'estado')->map(fn ($n) => (int) $n)->all());
        };

        $ingresosMes = (float) (clone $hotel)->where('estado', 'confirmada')->where('created_at', '>=', $inicioMes)->sum('total')
            + (float) (clone $tour)->where('estado', 'confirmada')->where('created_at', '>=', $inicioMes)->sum('total');
        $ingresosTotales = (float) (clone $hotel)->where('estado', 'confirmada')->sum('total')
            + (float) (clone $tour)->where('estado', 'confirmada')->sum('total');

        $servicios = Servicio::where('proveedor_id', $request->user()->id);
        $resenas = \App\Models\Review::whereIn('servicio_id', $propios);

        return response()->json(['data' => [
            'publicaciones'          => (clone $servicios)->count(),
            'hoteles'                => (clone $servicios)->where('tipo', 'hotel')->count(),
            'tours'                  => (clone $servicios)->where('tipo', 'tour')->count(),
            'solicitudes_pendientes' => (clone $hotel)->where('estado', 'pendiente')->count(),
            'reservas_hotel'         => $porEstado($hotel),
            'reservas_tour'          => $porEstado($tour),
            'ingresos_confirmados_mes' => round($ingresosMes, 2),
            'ingresos_confirmados_total' => round($ingresosTotales, 2),
            'resenas'                => [
                'cantidad' => (clone $resenas)->count(),
                'promedio' => round((float) (clone $resenas)->avg('calificacion'), 1),
            ],
        ]]);
    }
}
