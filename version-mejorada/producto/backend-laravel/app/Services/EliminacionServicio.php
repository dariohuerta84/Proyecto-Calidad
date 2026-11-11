<?php

namespace App\Services;

use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\Servicio;
use App\Models\Usuario;
use Illuminate\Support\Facades\DB;

/**
 * Eliminación lógica de publicaciones (TRV-508).
 * - Solo el proveedor dueño (403).
 * - Bloqueada con reservas activas (409).
 * - Conserva habitaciones, salidas, reseñas y reservas para el historial.
 */
class EliminacionServicio
{
    public static function reservasActivas(Servicio $servicio): int
    {
        $hoy = now()->toDateString();
        if ($servicio->tipo === 'hotel') {
            return ReservaHabitacion::whereHas('habitacion', fn ($q) => $q->where('servicio_id', $servicio->id))
                ->whereIn('estado', ReservaHabitacion::OCUPAN)
                ->whereDate('fecha_fin', '>=', $hoy)
                ->count();
        }

        return ReservaTour::whereHas('salida', fn ($q) => $q->where('servicio_id', $servicio->id)->whereDate('fecha', '>=', $hoy))
            ->where('estado', 'confirmada')
            ->count();
    }

    /** @return array{0:int,1:array} [status, body] */
    public static function eliminar(Servicio $servicio, Usuario $usuario): array
    {
        if (!$usuario->esProveedor() || (int) $servicio->proveedor_id !== (int) $usuario->id) {
            return [403, ['message' => 'No puedes eliminar una publicación que no es tuya.']];
        }

        $activas = self::reservasActivas($servicio);
        if ($activas > 0) {
            return [409, [
                'message' => "No se puede eliminar: la publicación tiene {$activas} reserva(s) activa(s).",
                'reservas_activas' => $activas,
            ]];
        }

        DB::transaction(function () use ($servicio) {
            $servicio->activo = false;
            $servicio->save();
            $servicio->delete(); // soft delete: deja de aparecer en catálogo y panel
        });

        return [200, ['message' => 'Publicación eliminada.', 'id' => $servicio->id]];
    }
}
