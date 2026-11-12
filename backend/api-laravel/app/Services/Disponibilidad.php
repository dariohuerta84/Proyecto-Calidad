<?php

namespace App\Services;

use App\Models\Habitacion;
use App\Models\ReservaHabitacion;
use Carbon\Carbon;
use Carbon\CarbonPeriod;

/**
 * Disponibilidad de habitaciones por noche.
 *
 * Una reserva ocupa sus unidades en cada noche de [fecha_inicio, fecha_fin).
 * Las unidades libres de un tipo en un rango son: cantidad − (máximo ocupado en cualquier noche del rango).
 * Solo ocupan las reservas pendientes o confirmadas (ReservaHabitacion::OCUPAN).
 */
class Disponibilidad
{
    /**
     * @param  int[]  $habitacionIds
     * @return array<int,int> habitacion_id => unidades ocupadas (máximo por noche) en [in, out)
     */
    public static function ocupacion(array $habitacionIds, string $in, string $out, ?int $excluirReservaId = null): array
    {
        $resultado = array_fill_keys($habitacionIds, 0);
        if (!$habitacionIds) {
            return $resultado;
        }

        $reservas = ReservaHabitacion::query()
            ->whereIn('habitacion_id', $habitacionIds)
            ->whereIn('estado', ReservaHabitacion::OCUPAN)
            ->whereDate('fecha_inicio', '<', $out)
            ->whereDate('fecha_fin', '>', $in)
            ->when($excluirReservaId, fn ($q) => $q->where('id', '!=', $excluirReservaId))
            ->get(['habitacion_id', 'fecha_inicio', 'fecha_fin', 'cantidad']);

        $porNoche = [];
        $desde = Carbon::parse($in);
        $hasta = Carbon::parse($out);
        foreach ($reservas as $r) {
            $ini = $r->fecha_inicio->max($desde);
            $fin = $r->fecha_fin->min($hasta);
            foreach (CarbonPeriod::create($ini, $fin->copy()->subDay()) as $noche) {
                $k = $noche->toDateString();
                $porNoche[$r->habitacion_id][$k] = ($porNoche[$r->habitacion_id][$k] ?? 0) + $r->cantidad;
            }
        }
        foreach ($porNoche as $hid => $noches) {
            $resultado[$hid] = max($noches);
        }

        return $resultado;
    }

    /** Máximo de unidades ocupadas en cualquier noche desde hoy (para no reducir por debajo). */
    public static function ocupacionFutura(int $habitacionId): int
    {
        $max = ReservaHabitacion::where('habitacion_id', $habitacionId)
            ->whereIn('estado', ReservaHabitacion::OCUPAN)
            ->whereDate('fecha_fin', '>', now()->toDateString())
            ->max('fecha_fin');
        if (!$max) {
            return 0;
        }

        return self::ocupacion([$habitacionId], now()->toDateString(), Carbon::parse($max)->toDateString())[$habitacionId] ?? 0;
    }

    /** ¿Tiene reservas activas (pendientes/confirmadas que aún no terminan)? */
    public static function tieneReservasActivas(int $habitacionId): bool
    {
        return ReservaHabitacion::where('habitacion_id', $habitacionId)
            ->whereIn('estado', ReservaHabitacion::OCUPAN)
            ->whereDate('fecha_fin', '>=', now()->toDateString())
            ->exists();
    }

    /** Habitaciones con capacidad suficiente: los huéspedes se reparten entre las habitaciones pedidas. */
    public static function cabe(Habitacion $h, ?int $adultos, ?int $ninos, int $habitaciones): bool
    {
        $habitaciones = max(1, $habitaciones);
        if ($adultos !== null && $h->capacidad_adultos * $habitaciones < $adultos) {
            return false;
        }
        if ($ninos !== null && $ninos > 0 && $h->capacidad_ninos * $habitaciones < $ninos) {
            return false;
        }

        return true;
    }
}
