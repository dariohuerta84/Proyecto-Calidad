<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

/**
 * Modelo para la tabla `hoteles`
 * PK = FK (servicio_id)
 */
class Hotel extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'hoteles';
    protected $primaryKey = 'servicio_id';
    public $incrementing = false;
    protected $keyType = 'int';

    protected $fillable = [
        'servicio_id',
        'direccion',
        'estrellas',   // 1..5 (nullable)
    ];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'estrellas' => 'int',
    ];

    // 🔗 Relaciones
    public function servicio()
    {
        return $this->belongsTo(Servicio::class, 'servicio_id', 'id');
    }

    public function habitaciones()
    {
        // Nota: en Habitacion, servicio_id apunta a hoteles.servicio_id
        return $this->hasMany(Habitacion::class, 'servicio_id', 'servicio_id');
    }

    // (Opcional) Accesor para mostrar nombre del hotel desde servicio
    public function getNombreAttribute()
    {
        return optional($this->servicio)->nombre;
    }
}
