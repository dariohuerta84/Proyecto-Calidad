<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreHabitacionRequest;
use App\Http\Requests\UpdateHabitacionRequest;
use App\Models\Habitacion;
use App\Models\Hotel;
use Illuminate\Http\JsonResponse;

class HabitacionController extends Controller
{
    // POST /api/hoteles/{servicio_id}/habitaciones
    public function store(StoreHabitacionRequest $request, int $servicio_id): JsonResponse
    {
        $hotel = Hotel::find($servicio_id);
        if (!$hotel) return response()->json(['message' => 'Hotel no encontrado'], 404);

        $h = Habitacion::create(array_merge(
            $request->validated(),
            ['servicio_id' => $servicio_id]
        ));

        return response()->json([
            'message' => 'Habitación creada correctamente.',
            'data'    => $h,
        ], 201);
    }

    // PUT /api/habitaciones/{habitacion}
    public function update(UpdateHabitacionRequest $request, Habitacion $habitacion): JsonResponse
    {
        $habitacion->update($request->validated());
        $habitacion->refresh();

        return response()->json([
            'message' => 'Habitación actualizada correctamente.',
            'data'    => $habitacion,
        ], 200);
    }

    // DELETE /api/habitaciones/{habitacion}
    public function destroy(UpdateHabitacionRequest $request, Habitacion $habitacion): JsonResponse
    {
        // reutilizamos authorize() del UpdateHabitacionRequest para validar propiedad
        $habitacion->delete();
        return response()->json(null, 204);
    }
}
