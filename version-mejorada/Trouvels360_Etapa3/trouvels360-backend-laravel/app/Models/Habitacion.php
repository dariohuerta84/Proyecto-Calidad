<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

/**
 * Modelo para la tabla `habitaciones`
 */
class Habitacion extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'habitaciones';

    protected $fillable = [
        'servicio_id',
        'nombre',
        'capacidad_adultos',
        'capacidad_ninos',
        'cantidad',
        'precio_por_noche',
        'descripcion',
    ];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'capacidad_adultos' => 'int',
        'capacidad_ninos'   => 'int',
        'cantidad'          => 'int',
        'precio_por_noche'  => 'decimal:2',
    ];

    // 🔗 Relaciones
    public function hotel()
    {
        return $this->belongsTo(Hotel::class, 'servicio_id', 'servicio_id');
    }

    public function servicio()
    {
        return $this->belongsTo(Servicio::class, 'servicio_id', 'id');
    }

    public function reservas()
    {
        return $this->hasMany(ReservaHabitacion::class, 'habitacion_id', 'id');
    }

    // 🔎 Scopes útiles
    public function scopePorCapacidad($q, int $adultos, int $ninos = 0)
    {
        return $q->where('capacidad_adultos', '>=', $adultos)
                 ->where('capacidad_ninos', '>=', $ninos);
    }

    public function scopeRangoPrecio($q, ?float $min = null, ?float $max = null)
    {
        return $q
            ->when($min !== null, fn($qq) => $qq->where('precio_por_noche', '>=', $min))
            ->when($max !== null, fn($qq) => $qq->where('precio_por_noche', '<=', $max));
    }
}
