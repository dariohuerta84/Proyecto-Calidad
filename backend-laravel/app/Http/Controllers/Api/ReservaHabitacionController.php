<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreReservaHabitacionRequest;
use App\Models\Habitacion;
use App\Models\ReservaHabitacion;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class ReservaHabitacionController extends Controller
{
    // POST /api/reservas-habitaciones  (viajero)
    public function store(StoreReservaHabitacionRequest $request): JsonResponse
    {
        $user = $request->user();
        $data = $request->validated();

        $habitacion = Habitacion::findOrFail($data['habitacion_id']);

        $in  = Carbon::parse($data['fecha_inicio'])->toDateString();
        $out = Carbon::parse($data['fecha_fin'])->toDateString();
        $qty = (int) $data['cantidad'];

        // calcular ocupadas
        $ocupadas = $habitacion->reservas()
            ->whereIn('estado', ['pendiente', 'confirmada'])
            ->whereDate('fecha_inicio', '<',  $out)
            ->whereDate('fecha_fin',    '>',  $in)
            ->sum('cantidad');

        $disponibles = max(0, $habitacion->cantidad - $ocupadas);

        if ($qty > $disponibles) {
            return response()->json([
                'message' => 'No hay suficientes unidades disponibles para ese rango de fechas.',
                'detalles' => [
                    'solicitadas' => $qty,
                    'disponibles' => $disponibles,
                ]
            ], 422);
        }

        $noches = Carbon::parse($in)->diffInDays(Carbon::parse($out));
        $precio = (float) $habitacion->precio_por_noche;
        $total  = $precio * $qty * max(1, $noches);

        $reserva = ReservaHabitacion::create([
            'codigo_reserva'   => strtoupper(Str::random(10)),
            'usuario_id'       => $user->id,
            'habitacion_id'    => $habitacion->id,
            'fecha_inicio'     => $in,
            'fecha_fin'        => $out,
            'cantidad'         => $qty,
            'estado'           => 'pendiente',
            'precio_por_noche' => $precio,
            'total'            => $total,
        ]);

        return response()->json([
            'message' => 'Reserva creada correctamente.',
            'data'    => $reserva,
        ], 201);
    }

    // POST /api/reservas-habitaciones/{reserva}/cancelar
    public function cancelar(Request $request, ReservaHabitacion $reserva): JsonResponse
    {
        $user = $request->user();
        if ($reserva->usuario_id !== $user->id) {
            return response()->json(['message' => 'No puedes cancelar reservas de otro usuario.'], 403);
        }

        if ($reserva->estado === 'cancelada') {
            return response()->json(['message' => 'La reserva ya está cancelada.'], 422);
        }

        $reserva->update(['estado' => 'cancelada']);

        return response()->json([
            'message' => 'Reserva cancelada.',
            'data'    => $reserva,
        ], 200);
    }

    // GET /api/mis-reservas
    public function misReservas(Request $request): JsonResponse
    {
        $user = $request->user();

        $reservas = ReservaHabitacion::with(['habitacion.hotel.servicio'])
            ->where('usuario_id', $user->id)
            ->orderByDesc('id')
            ->get()
            ->map(function ($r) {
                return [
                    'id'            => $r->id,
                    'codigo'        => $r->codigo_reserva,
                    'estado'        => $r->estado,
                    'fecha_inicio'  => $r->fecha_inicio,
                    'fecha_fin'     => $r->fecha_fin,
                    'cantidad'      => $r->cantidad,
                    'precio_noche'  => (float) $r->precio_por_noche,
                    'total'         => (float) $r->total,
                    'hotel'         => [
                        'servicio_id' => $r->habitacion->hotel->servicio_id,
                        'nombre'      => $r->habitacion->hotel->servicio->nombre ?? null,
                        'ciudad'      => $r->habitacion->hotel->servicio->ciudad ?? null,
                    ],
                    'habitacion'    => [
                        'id'       => $r->habitacion->id,
                        'nombre'   => $r->habitacion->nombre,
                    ],
                ];
            });

        return response()->json($reservas, 200);
    }
}
