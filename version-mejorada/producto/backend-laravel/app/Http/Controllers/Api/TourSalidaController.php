<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ReservaTour;
use App\Models\Servicio;
use App\Models\TourSalida;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;

/** TRV-506 — Salidas y cupos. cupo_reservado solo lo cambia el sistema. */
class TourSalidaController extends Controller
{
    private function formato(TourSalida $s): array
    {
        return [
            'id'              => $s->id,
            'servicio_id'     => $s->servicio_id,
            'fecha'           => $s->fechaIso(),
            'hora'            => $s->horaCorta(),
            'cupo_total'      => (int) $s->cupo_total,
            'cupo_reservado'  => (int) $s->cupo_reservado,
            'cupo_disponible' => $s->disponibilidad,
            'estado'          => $s->estado,
            'reservas_confirmadas' => ReservaTour::where('salida_id', $s->id)->where('estado', 'confirmada')->count(),
        ];
    }

    /** GET /api/tours/{tour}/salidas?desde=&hasta=&estado=  (público) */
    public function index(Request $req, $tour): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $v = $req->validate([
            'estado' => ['nullable', 'in:programada,cerrada,cancelada'],
            'desde'  => ['nullable', 'date_format:Y-m-d'],
            'hasta'  => ['nullable', 'date_format:Y-m-d'],
        ]);

        $salidas = TourSalida::where('servicio_id', $serv->id)
            ->when($v['estado'] ?? null, fn ($q, $e) => $q->where('estado', $e))
            ->when($v['desde'] ?? null, fn ($q, $d) => $q->whereDate('fecha', '>=', $d))
            ->when($v['hasta'] ?? null, fn ($q, $h) => $q->whereDate('fecha', '<=', $h))
            ->orderBy('fecha')->orderBy('hora')->get();

        return response()->json($salidas->map(fn ($s) => $this->formato($s))->values());
    }

    /** POST /api/tours/{tour}/salidas */
    public function store(Request $req, $tour): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $this->assertOwnership($serv);

        $data = $req->validate([
            'fecha'      => ['required', 'date_format:Y-m-d', 'after_or_equal:today'],
            'hora'       => ['required', 'date_format:H:i'],
            'cupo_total' => ['required', 'integer', 'min:1', 'max:1000'],
            'estado'     => ['sometimes', 'in:programada,cerrada,cancelada'],
        ], ['fecha.after_or_equal' => 'La fecha de la salida no puede ser pasada.']);

        $duplicada = TourSalida::withTrashed()->where('servicio_id', $serv->id)
            ->whereDate('fecha', $data['fecha'])->where('hora', 'like', $data['hora'] . '%')->first();
        if ($duplicada && !$duplicada->trashed()) {
            return response()->json(['message' => 'Ya existe una salida para esa fecha y hora.'], 409);
        }

        $salida = DB::transaction(function () use ($serv, $data, $duplicada) {
            $duplicada?->forceDelete(); // libera el índice único de una salida eliminada antes
            return $serv->salidas()->create([
                'fecha'          => $data['fecha'],
                'hora'           => $data['hora'],
                'cupo_total'     => $data['cupo_total'],
                'cupo_reservado' => 0,
                'estado'         => $data['estado'] ?? 'programada',
            ]);
        });

        return response()->json($this->formato($salida->fresh()), 201);
    }

    /** PUT/PATCH /api/tours/{tour}/salidas/{salida} — cupo_reservado se ignora si se envía. */
    public function update(Request $req, $tour, $salida): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $this->assertOwnership($serv);
        $sal = TourSalida::where('id', $salida)->where('servicio_id', $serv->id)->firstOrFail();

        $data = $req->validate([
            'fecha'      => ['sometimes', 'date_format:Y-m-d', 'after_or_equal:today'],
            'hora'       => ['sometimes', 'date_format:H:i'],
            'cupo_total' => ['sometimes', 'integer', 'min:1', 'max:1000'],
            'estado'     => ['sometimes', 'in:programada,cerrada,cancelada'],
        ]);

        if (isset($data['cupo_total']) && $data['cupo_total'] < $sal->cupo_reservado) {
            return response()->json([
                'message' => "El cupo total no puede ser menor a lo ya reservado ({$sal->cupo_reservado}).",
                'errors'  => ['cupo_total' => ["Mínimo {$sal->cupo_reservado}."]],
            ], 422);
        }
        if ((isset($data['fecha']) || isset($data['hora'])) && $sal->cupo_reservado > 0) {
            return response()->json(['message' => 'No se puede cambiar la fecha u hora de una salida con reservas. Ciérrala y crea otra.'], 409);
        }
        if (isset($data['fecha']) || isset($data['hora'])) {
            $f = $data['fecha'] ?? $sal->fechaIso();
            $h = $data['hora'] ?? $sal->horaCorta();
            if (TourSalida::where('servicio_id', $serv->id)->where('id', '!=', $sal->id)->whereDate('fecha', $f)->where('hora', 'like', $h . '%')->exists()) {
                return response()->json(['message' => 'Ya existe una salida para esa fecha y hora.'], 409);
            }
        }

        $sal->fill($data)->save();

        return response()->json($this->formato($sal->fresh()));
    }

    /** DELETE /api/tours/{tour}/salidas/{salida} — bloqueado con reservas confirmadas. */
    public function destroy($tour, $salida): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $this->assertOwnership($serv);
        $sal = TourSalida::where('id', $salida)->where('servicio_id', $serv->id)->firstOrFail();

        if (ReservaTour::where('salida_id', $sal->id)->where('estado', 'confirmada')->exists()) {
            return response()->json(['message' => 'No se puede eliminar una salida con reservas confirmadas. Puedes cerrarla o cancelarla.'], 409);
        }

        $sal->delete();

        return response()->json(['message' => 'Salida eliminada']);
    }

    private function assertOwnership(Servicio $serv): void
    {
        $user = Auth::user();
        if (!$user || $user->rol !== 'proveedor' || (int) $user->id !== (int) $serv->proveedor_id) {
            abort(response()->json(['message' => 'No autorizado: no eres el proveedor dueño de este tour.'], 403));
        }
    }
}
