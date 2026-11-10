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
        'estado',            // 'pendiente' | 'confirmada' | 'cancelada'
        'precio_por_noche',
        'total',
    ];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'fecha_inicio'     => 'date',
        'fecha_fin'        => 'date',
        'cantidad'         => 'int',
        'precio_por_noche' => 'decimal:2',
        'total'            => 'decimal:2',
    ];

    // 🔗 Relaciones
    public function usuario()
    {
        return $this->belongsTo(Usuario::class, 'usuario_id', 'id');
    }

    public function habitacion()
    {
        return $this->belongsTo(Habitacion::class, 'habitacion_id', 'id');
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
        return $q->whereIn('estado', ['pendiente','confirmada']);
    }

    public function scopeRango($q, string $desde, string $hasta)
    {
        // traslape: (inicio < hasta) AND (fin > desde)
        return $q->where('fecha_inicio', '<', $hasta)
                 ->where('fecha_fin', '>', $desde);
    }
}
