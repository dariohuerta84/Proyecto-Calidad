<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

/**
 * Modelo para la tabla `reservas_habitaciones`
 */
class ReservaHabitacion extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'reservas_habitaciones';

    protected $fillable = [
        'codigo_reserva',
        'usuario_id',
        'habitacion_id',
        'fecha_inicio',
        'fecha_fin',
        'cantidad',
        'estado',            // 'pendiente' | 'confirmada' | 'rechazada' | 'cancelada'
        'precio_por_noche',
        'subtotal',
        'impuesto',
        'total',
        'solicitud_codigo',
        'tarjeta_ultimos4',
        'motivo_rechazo',
        'decidido_por',
        'decidido_en',
        'cancelado_en',
    ];

    public const ESTADOS = ['pendiente', 'confirmada', 'rechazada', 'cancelada'];
    /** Estados que ocupan unidades de habitación */
    public const OCUPAN = ['pendiente', 'confirmada'];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'fecha_inicio'     => 'date',
        'fecha_fin'        => 'date',
        'cantidad'         => 'int',
        'precio_por_noche' => 'decimal:2',
        'subtotal'         => 'decimal:2',
        'impuesto'         => 'decimal:2',
        'total'            => 'decimal:2',
        'decidido_en'      => 'datetime',
        'cancelado_en'     => 'datetime',
    ];

    // 🔗 Relaciones
    public function usuario()
    {
        return $this->belongsTo(Usuario::class, 'usuario_id', 'id');
    }

    public function habitacion()
    {
        // withTrashed: el historial sigue visible aunque el proveedor elimine la habitación o el hotel
        return $this->belongsTo(Habitacion::class, 'habitacion_id', 'id')->withTrashed();
    }

    public function decisor()
    {
        return $this->belongsTo(Usuario::class, 'decidido_por')->withTrashed();
    }

    /** Servicio (hotel) aunque haya sido eliminado lógicamente. */
    public function servicioHistorico(): ?Servicio
    {
        $sid = $this->habitacion?->servicio_id;
        return $sid ? Servicio::withTrashed()->find($sid) : null;
    }

    /** ¿La estadía todavía no empezó? (cancelable) */
    public function noIniciada(): bool
    {
        return $this->fecha_inicio && $this->fecha_inicio->isAfter(now()->startOfDay());
    }

    // (Opcional) Atajo: servicio al que pertenece la habitación
    public function servicio()
    {
        return $this->hasOneThrough(
            Servicio::class,
            Habitacion::class,
            'id',            // PK en Habitacion
            'id',            // PK en Servicio
            'habitacion_id', // FK en ReservaHabitacion -> Habitacion
            'servicio_id'    // FK en Habitacion -> Servicio
        );
    }

    // 🔎 Scopes
    public function scopeVigentes($q)
    {
        return $q->whereIn('estado', self::OCUPAN);
    }

    public function scopeRango($q, string $desde, string $hasta)
    {
        // traslape: (inicio < hasta) AND (fin > desde)
        return $q->where('fecha_inicio', '<', $hasta)
                 ->where('fecha_fin', '>', $desde);
    }
}
