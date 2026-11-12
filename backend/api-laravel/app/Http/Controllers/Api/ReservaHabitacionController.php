<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Habitacion;
use App\Models\ReservaHabitacion;
use App\Services\Disponibilidad;
use App\Services\ReservaPresenter;
use App\Support\PagoSimulado;
use App\Support\Precio;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

/**
 * HU-V06 (cotizar), HU-V07 (solicitar), HU-V11 (mis reservas), HU-V13 (cancelar).
 */
class ReservaHabitacionController extends Controller
{
    private function reglasSeleccion(): array
    {
        return [
            'fecha_inicio'           => ['required', 'date_format:Y-m-d', 'after_or_equal:today'],
            'fecha_fin'              => ['required', 'date_format:Y-m-d', 'after:fecha_inicio'],
            'items'                  => ['required', 'array', 'min:1', 'max:10'],
            'items.*.habitacion_id'  => ['required', 'integer', 'distinct', 'exists:habitaciones,id'],
            'items.*.cantidad'       => ['required', 'integer', 'min:1', 'max:10'],
        ];
    }

    private function mensajes(): array
    {
        return [
            'fecha_inicio.after_or_equal' => 'La fecha de entrada no puede ser anterior a hoy.',
            'fecha_fin.after'             => 'La fecha de salida debe ser posterior a la de entrada.',
            'items.required'              => 'Selecciona al menos una habitación.',
            'items.*.habitacion_id.distinct' => 'Cada tipo de habitación debe aparecer una sola vez.',
        ] + PagoSimulado::mensajes();
    }

    /** Compatibilidad: {habitacion_id, cantidad} → items[] */
    private function normalizar(Request $request): void
    {
        if (!$request->has('items') && $request->has('habitacion_id')) {
            $request->merge(['items' => [[
                'habitacion_id' => $request->input('habitacion_id'),
                'cantidad'      => $request->input('cantidad', 1),
            ]]]);
        }
    }

    /**
     * Calcula líneas y totales. Lanza 422 si un tipo no tiene unidades suficientes
     * o si las habitaciones no pertenecen al mismo hotel activo.
     */
    private function cotizarItems(array $v, ?callable $lock = null): array
    {
        $ids = collect($v['items'])->pluck('habitacion_id')->map(fn ($x) => (int) $x)->all();
        $query = Habitacion::with('servicio')->whereIn('id', $ids)->orderBy('id');
        $habitaciones = ($lock ? $lock($query) : $query)->get()->keyBy('id');

        $servicios = $habitaciones->pluck('servicio_id')->unique();
        if ($servicios->count() !== 1) {
            throw ValidationException::withMessages(['items' => 'Todas las habitaciones deben ser del mismo hotel.']);
        }
        $servicio = $habitaciones->first()->servicio;
        if (!$servicio || !$servicio->activo) {
            throw ValidationException::withMessages(['items' => 'El hotel no está disponible.']);
        }

        $noches = Carbon::parse($v['fecha_inicio'])->diffInDays(Carbon::parse($v['fecha_fin']));
        if ($noches > 30) {
            throw ValidationException::withMessages(['fecha_fin' => 'La estadía no puede superar 30 noches.']);
        }

        $ocup = Disponibilidad::ocupacion($ids, $v['fecha_inicio'], $v['fecha_fin']);
        $lineas = [];
        $sinStock = [];
        $subtotalCent = 0;
        foreach ($v['items'] as $item) {
            $h = $habitaciones[(int) $item['habitacion_id']];
            $libres = max(0, (int) $h->cantidad - ($ocup[$h->id] ?? 0));
            $cant = (int) $item['cantidad'];
            if ($cant > $libres) {
                $sinStock[] = ['habitacion_id' => $h->id, 'nombre' => $h->nombre, 'solicitadas' => $cant, 'disponibles' => $libres];
            }
            $lineaCent = Precio::lineaCentimos($h->precio_por_noche, $cant, (int) $noches);
            $subtotalCent += $lineaCent;
            $lineas[] = [
                'habitacion'       => $h,
                'habitacion_id'    => $h->id,
                'nombre'           => $h->nombre,
                'cantidad'         => $cant,
                'precio_por_noche' => (float) $h->precio_por_noche,
                'unidades_disponibles' => $libres,
                'subtotal_cent'    => $lineaCent,
            ] + Precio::desdeSubtotalCentimos($lineaCent);
        }

        return [
            'servicio'  => $servicio,
            'noches'    => (int) $noches,
            'lineas'    => $lineas,
            'sin_stock' => $sinStock,
            'totales'   => Precio::desdeSubtotalCentimos($subtotalCent),
        ];
    }

    /** POST /api/reservas-habitaciones/cotizar — público, no reserva nada. */
    public function cotizar(Request $request): JsonResponse
    {
        $this->normalizar($request);
        $v = $request->validate($this->reglasSeleccion(), $this->mensajes());
        $c = $this->cotizarItems($v);

        if ($c['sin_stock']) {
            $f = $c['sin_stock'][0];
            return response()->json([
                'message' => "Solo hay {$f['disponibles']} unidad(es) disponible(s) de '{$f['nombre']}'.",
                'errors'  => ['items' => ["Máximo {$f['disponibles']} para '{$f['nombre']}'."]],
                'sin_stock' => $c['sin_stock'],
            ], 422);
        }

        return response()->json([
            'data' => [
                'hotel'        => ['id' => $c['servicio']->id, 'nombre' => $c['servicio']->nombre],
                'fecha_inicio' => $v['fecha_inicio'],
                'fecha_fin'    => $v['fecha_fin'],
                'noches'       => $c['noches'],
                'items'        => collect($c['lineas'])->map(fn ($l) => collect($l)->except(['habitacion', 'subtotal_cent']))->values(),
                'subtotal'     => $c['totales']['subtotal'],
                'impuesto'     => $c['totales']['impuesto'],
                'igv_porcentaje' => Precio::IGV,
                'total'        => $c['totales']['total'],
            ],
        ]);
    }

    /** POST /api/reservas-habitaciones  (Idempotency-Key) — crea la solicitud PENDIENTE. */
    public function store(Request $request): JsonResponse
    {
        $user = $request->user();
        if (!$user->esViajero()) {
            return response()->json(['message' => 'Solo las cuentas de viajero pueden reservar.'], 403);
        }

        $this->normalizar($request);
        $v = $request->validate($this->reglasSeleccion() + PagoSimulado::reglas(), $this->mensajes());

        $resultado = DB::transaction(function () use ($v, $user) {
            // Bloqueo de fila sobre los tipos de habitación: serializa reservas simultáneas
            $c = $this->cotizarItems($v, fn ($q) => $q->lockForUpdate());

            if ($c['sin_stock']) {
                return ['error' => $c['sin_stock']];
            }

            $solicitud = 'SOL-' . strtoupper(Str::random(8));
            $ultimos4 = PagoSimulado::ultimos4($v['pago']);
            $reservas = [];
            foreach ($c['lineas'] as $l) {
                $reservas[] = ReservaHabitacion::create([
                    'codigo_reserva'   => $this->codigoUnico(),
                    'solicitud_codigo' => $solicitud,
                    'usuario_id'       => $user->id,
                    'habitacion_id'    => $l['habitacion_id'],
                    'fecha_inicio'     => $v['fecha_inicio'],
                    'fecha_fin'        => $v['fecha_fin'],
                    'cantidad'         => $l['cantidad'],
                    'estado'           => 'pendiente',
                    'precio_por_noche' => $l['precio_por_noche'],
                    'subtotal'         => $l['subtotal'],
                    'impuesto'         => $l['impuesto'],
                    'total'            => $l['total'],
                    'tarjeta_ultimos4' => $ultimos4,
                ]);
            }

            return ['solicitud' => $solicitud, 'reservas' => $reservas, 'c' => $c];
        });

        if (isset($resultado['error'])) {
            $f = $resultado['error'][0];
            return response()->json([
                'message'   => "Ya no hay unidades suficientes de '{$f['nombre']}' (disponibles: {$f['disponibles']}). No se registró ninguna habitación.",
                'sin_stock' => $resultado['error'],
            ], 409);
        }

        $t = $resultado['c']['totales'];
        $primera = $resultado['reservas'][0];

        return response()->json([
            'message' => 'Solicitud registrada. El proveedor la revisará y te avisaremos cuando la confirme.',
            'data'    => [
                'solicitud_codigo' => $resultado['solicitud'],
                'estado'           => 'pendiente',
                'hotel'            => ['id' => $resultado['c']['servicio']->id, 'nombre' => $resultado['c']['servicio']->nombre],
                'fecha_inicio'     => $v['fecha_inicio'],
                'fecha_fin'        => $v['fecha_fin'],
                'noches'           => $resultado['c']['noches'],
                'subtotal'         => $t['subtotal'],
                'impuesto'         => $t['impuesto'],
                'total'            => $t['total'],
                'tarjeta_ultimos4' => $primera->tarjeta_ultimos4,
                // compatibilidad: primer código como codigo_reserva
                'codigo_reserva'   => $primera->codigo_reserva,
                'reservas'         => collect($resultado['reservas'])->map(fn ($r) => ReservaPresenter::hotel($r->fresh()))->values(),
            ],
        ], 201);
    }

    /** POST /api/reservas-habitaciones/{id}/cancelar  (HU-V13) */
    public function cancelar(Request $request, int $reserva): JsonResponse
    {
        $user = $request->user();

        return DB::transaction(function () use ($user, $reserva) {
            $r = ReservaHabitacion::where('id', $reserva)->where('usuario_id', $user->id)->lockForUpdate()->first();
            if (!$r) {
                return response()->json(['message' => 'Reserva no encontrada.'], 404);
            }
            if ($r->estado === 'cancelada') {
                return response()->json(['message' => 'La reserva ya está cancelada.', 'estado' => $r->estado], 409);
            }
            if ($r->estado === 'rechazada') {
                return response()->json(['message' => 'La solicitud fue rechazada por el proveedor; no se puede cancelar.', 'estado' => $r->estado], 409);
            }
            if (!$r->noIniciada()) {
                return response()->json(['message' => 'La estadía ya comenzó o terminó; no se puede cancelar.', 'estado' => $r->estado], 409);
            }

            // Al pasar a 'cancelada' deja de ocupar unidades (solo pendiente/confirmada ocupan).
            $r->update(['estado' => 'cancelada', 'cancelado_en' => now()]);

            return response()->json(['message' => 'Reserva cancelada.', 'data' => ReservaPresenter::hotel($r->fresh())]);
        });
    }

    /** GET /api/mis-reservas?estado=&page=&per_page=  (HU-V11) */
    public function misReservas(Request $request): JsonResponse
    {
        $v = $request->validate([
            'estado'   => ['nullable', 'in:' . implode(',', ReservaHabitacion::ESTADOS)],
            'per_page' => ['nullable', 'integer', 'between:1,50'],
        ]);

        $pag = ReservaHabitacion::with('habitacion')
            ->where('usuario_id', $request->user()->id)
            ->when($v['estado'] ?? null, fn ($q, $e) => $q->where('estado', $e))
            ->orderByDesc('created_at')->orderByDesc('id')
            ->paginate((int) ($v['per_page'] ?? 10));

        return response()->json(ReservaPresenter::paginado($pag, collect($pag->items())->map(fn ($r) => ReservaPresenter::hotel($r))->all()));
    }

    /** GET /api/mis-reservas/{id} — 404 si no es propia. */
    public function show(Request $request, int $reserva): JsonResponse
    {
        $r = ReservaHabitacion::with('habitacion')->where('id', $reserva)->where('usuario_id', $request->user()->id)->first();
        if (!$r) {
            return response()->json(['message' => 'Reserva no encontrada.'], 404);
        }

        return response()->json(['data' => ReservaPresenter::hotel($r)]);
    }

    private function codigoUnico(): string
    {
        do {
            $c = strtoupper(Str::random(10));
        } while (ReservaHabitacion::withTrashed()->where('codigo_reserva', $c)->exists());

        return $c;
    }
}
