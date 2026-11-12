<?php

namespace App\Services;

use App\Models\Reserva;

class DisponibilidadService
{
    /**
     * Verificar si un servicio tiene conflictos de disponibilidad en las fechas especificadas
     * 
     * @param int $servicioId
     * @param string $fechaInicio
     * @param string $fechaFin
     * @param int|null $reservaIdExcluir ID de reserva a excluir (útil para actualizaciones)
     * @return bool true si hay conflictos, false si está disponible
     */
    public static function verificarConflictos(
        int $servicioId, 
        string $fechaInicio, 
        string $fechaFin, 
        ?int $reservaIdExcluir = null
    ): bool {
        $query = Reserva::where('servicio_id', $servicioId)
            ->where('estado', '!=', 'cancelada')
            ->where(function ($query) use ($fechaInicio, $fechaFin) {
                // Escenario 1: Nueva reserva inicia durante una existente
                $query->whereBetween('fecha_inicio', [$fechaInicio, $fechaFin])
                      // Escenario 2: Nueva reserva termina durante una existente  
                      ->orWhereBetween('fecha_fin', [$fechaInicio, $fechaFin])
                      // Escenario 3: Nueva reserva envuelve completamente una existente
                      ->orWhere(function ($q) use ($fechaInicio, $fechaFin) {
                          $q->where('fecha_inicio', '<=', $fechaInicio)
                            ->where('fecha_fin', '>=', $fechaFin);
                      });
            });

        // Excluir una reserva específica (útil para actualizaciones)
        if ($reservaIdExcluir) {
            $query->where('id', '!=', $reservaIdExcluir);
        }

        return $query->exists();
    }

    /**
     * Obtener todas las reservas que están en conflicto con las fechas especificadas
     * 
     * @param int $servicioId
     * @param string $fechaInicio
     * @param string $fechaFin
     * @param int|null $reservaIdExcluir
     * @return \Illuminate\Database\Eloquent\Collection
     */
    public static function obtenerConflictos(
        int $servicioId, 
        string $fechaInicio, 
        string $fechaFin, 
        ?int $reservaIdExcluir = null
    ) {
        $query = Reserva::with(['usuario:id,nombre,apellido'])
            ->where('servicio_id', $servicioId)
            ->where('estado', '!=', 'cancelada')
            ->where(function ($query) use ($fechaInicio, $fechaFin) {
                $query->whereBetween('fecha_inicio', [$fechaInicio, $fechaFin])
                      ->orWhereBetween('fecha_fin', [$fechaInicio, $fechaFin])
                      ->orWhere(function ($q) use ($fechaInicio, $fechaFin) {
                          $q->where('fecha_inicio', '<=', $fechaInicio)
                            ->where('fecha_fin', '>=', $fechaFin);
                      });
            });

        if ($reservaIdExcluir) {
            $query->where('id', '!=', $reservaIdExcluir);
        }

        return $query->get();
    }

    /**
     * Verificar disponibilidad y obtener información detallada
     * 
     * @param int $servicioId
     * @param string $fechaInicio
     * @param string $fechaFin
     * @param int|null $reservaIdExcluir
     * @return array
     */
    public static function verificarDisponibilidadDetallada(
        int $servicioId, 
        string $fechaInicio, 
        string $fechaFin, 
        ?int $reservaIdExcluir = null
    ): array {
        $conflictos = static::obtenerConflictos($servicioId, $fechaInicio, $fechaFin, $reservaIdExcluir);
        
        return [
            'disponible' => $conflictos->isEmpty(),
            'conflictos' => $conflictos,
            'total_conflictos' => $conflictos->count(),
        ];
    }
}