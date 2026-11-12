<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreHabitacionRequest;
use App\Http\Requests\UpdateHabitacionRequest;
use App\Models\Habitacion;
use App\Models\Hotel;
use App\Services\Disponibilidad;
use Illuminate\Http\JsonResponse;

/** HU-P07 — Gestión de tipos de habitación (solo el proveedor dueño). */
class HabitacionController extends Controller
{
    // POST /api/hoteles/{servicio_id}/habitaciones
    public function store(StoreHabitacionRequest $request, int $servicio_id): JsonResponse
    {
        if (!Hotel::find($servicio_id)) {
            return response()->json(['message' => 'Hotel no encontrado'], 404);
        }

        $h = Habitacion::create($request->validated() + ['servicio_id' => $servicio_id]);

        return response()->json(['message' => 'Habitación creada correctamente.', 'data' => $h], 201);
    }

    // PUT /api/habitaciones/{habitacion}
    public function update(UpdateHabitacionRequest $request, Habitacion $habitacion): JsonResponse
    {
        $data = $request->validated();

        if (isset($data['cantidad'])) {
            $min = Disponibilidad::ocupacionFutura($habitacion->id);
            if ((int) $data['cantidad'] < $min) {
                return response()->json([
                    'message' => "Hay {$min} unidades comprometidas en reservas futuras. Mínimo permitido: {$min}.",
                    'minimo'  => $min,
                ], 409);
            }
        }
        if (isset($data['nombre']) && Habitacion::where('servicio_id', $habitacion->servicio_id)
                ->where('nombre', $data['nombre'])->where('id', '!=', $habitacion->id)->exists()) {
            return response()->json(['message' => 'Ya existe un tipo de habitación con ese nombre en el hotel.',
                'errors' => ['nombre' => ['Ya existe un tipo de habitación con ese nombre en el hotel.']]], 422);
        }

        // La tarifa nueva no altera reservas existentes: cada reserva guardó su precio_por_noche.
        $habitacion->update($data);

        return response()->json(['message' => 'Habitación actualizada correctamente.', 'data' => $habitacion->refresh()]);
    }

    // DELETE /api/habitaciones/{habitacion}
    public function destroy(UpdateHabitacionRequest $request, Habitacion $habitacion): JsonResponse
    {
        if (Disponibilidad::tieneReservasActivas($habitacion->id)) {
            return response()->json(['message' => 'No se puede eliminar: el tipo de habitación tiene reservas activas.'], 409);
        }
        if (Habitacion::where('servicio_id', $habitacion->servicio_id)->count() <= 1) {
            return response()->json(['message' => 'El hotel debe conservar al menos un tipo de habitación.'], 409);
        }

        $habitacion->delete(); // borrado lógico: el historial de reservas se conserva

        return response()->json(null, 204);
    }
}
