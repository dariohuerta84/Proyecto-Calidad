<?php

namespace App\Services;

use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\Servicio;

/** Formato único de reservas para viajero y proveedor (web y móvil). */
class ReservaPresenter
{
    public static function hotel(ReservaHabitacion $r, bool $conCliente = false): array
    {
        $hab = $r->habitacion;
        $serv = $hab ? Servicio::withTrashed()->find($hab->servicio_id) : null;
        $noches = $r->fecha_inicio && $r->fecha_fin ? $r->fecha_inicio->diffInDays($r->fecha_fin) : 0;

        $data = [
            'id'               => $r->id,
            'tipo'             => 'hotel',
            'codigo'           => $r->codigo_reserva,
            'solicitud_codigo' => $r->solicitud_codigo,
            'estado'           => $r->estado,
            'motivo_rechazo'   => $r->motivo_rechazo,
            'fecha_inicio'     => $r->fecha_inicio?->toDateString(),
            'fecha_fin'        => $r->fecha_fin?->toDateString(),
            'noches'           => (int) $noches,
            'cantidad'         => (int) $r->cantidad,
            'precio_noche'     => (float) $r->precio_por_noche,
            'subtotal'         => (float) $r->subtotal,
            'impuesto'         => (float) $r->impuesto,
            'total'            => (float) $r->total,
            'tarjeta_ultimos4' => $r->tarjeta_ultimos4,
            'decidido_en'      => $r->decidido_en?->toIso8601String(),
            'cancelado_en'     => $r->cancelado_en?->toIso8601String(),
            'creado_en'        => $r->created_at?->toIso8601String(),
            'cancelable'       => in_array($r->estado, ReservaHabitacion::OCUPAN, true) && $r->noIniciada(),
            'servicio'         => [
                'id'          => $serv?->id,
                'nombre'      => $serv?->nombre,
                'ciudad'      => $serv?->ciudad,
                'imagen_url'  => $serv?->imagen_url,
                'eliminado'   => (bool) $serv?->trashed(),
            ],
            // compatibilidad con el formato anterior
            'hotel'      => ['servicio_id' => $serv?->id, 'nombre' => $serv?->nombre, 'ciudad' => $serv?->ciudad],
            'habitacion' => ['id' => $hab?->id, 'nombre' => $hab?->nombre],
        ];
        if ($conCliente) {
            $data['cliente'] = self::cliente($r->usuario()->withTrashed()->first());
        }

        return $data;
    }

    public static function tour(ReservaTour $r, bool $conCliente = false): array
    {
        $sal = $r->salida;
        $serv = $sal ? Servicio::withTrashed()->find($sal->servicio_id) : null;
        $salidaCancelada = $sal && in_array($sal->estado, ['cancelada', 'cerrada'], true);

        $data = [
            'id'               => $r->id,
            'tipo'             => 'tour',
            'codigo'           => $r->codigo_reserva,
            'codigo_reserva'   => $r->codigo_reserva,
            'estado'           => $r->estado,
            'personas'         => (int) $r->personas,
            'precio_unitario'  => (float) $r->precio_unitario,
            'subtotal'         => (float) $r->subtotal,
            'impuesto'         => (float) $r->impuesto,
            'total'            => (float) $r->total,
            'tarjeta_ultimos4' => $r->tarjeta_ultimos4,
            'cancelado_en'     => $r->cancelado_en?->toIso8601String(),
            'creado_en'        => $r->created_at?->toIso8601String(),
            'cancelable'       => $r->estado === 'confirmada' && $sal && !$sal->yaPaso(),
            'aviso'            => $salidaCancelada && $r->estado === 'confirmada'
                ? ($sal->estado === 'cancelada' ? 'Salida cancelada por el proveedor' : 'Salida cerrada por el proveedor')
                : null,
            'servicio' => [
                'id'         => $serv?->id,
                'nombre'     => $serv?->nombre,
                'ciudad'     => $serv?->ciudad,
                'imagen_url' => $serv?->imagen_url,
                'eliminado'  => (bool) $serv?->trashed(),
            ],
            'salida' => [
                'id'     => $sal?->id,
                'fecha'  => $sal?->fechaIso(),
                'hora'   => $sal?->horaCorta(),
                'estado' => $sal?->estado,
            ],
        ];
        if ($conCliente) {
            $data['cliente'] = self::cliente($r->usuario()->withTrashed()->first());
        }

        return $data;
    }

    private static function cliente($u): ?array
    {
        if (!$u) {
            return null;
        }

        return [
            'id'     => $u->id,
            'nombre' => trim(($u->nombre ?? '') . ' ' . ($u->apellido ?? '')) ?: ($u->empresa_nombre ?? $u->email),
            'email'  => $u->email,
        ];
    }

    /** Envoltura estándar de paginación. */
    public static function paginado($paginator, array $data): array
    {
        return [
            'data' => $data,
            'meta' => [
                'current_page' => $paginator->currentPage(),
                'last_page'    => $paginator->lastPage(),
                'per_page'     => $paginator->perPage(),
                'total'        => $paginator->total(),
            ],
        ];
    }
}
