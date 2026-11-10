<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\SoftDeletes;

class Tour extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'tours';
    protected $primaryKey = 'servicio_id';
    public $incrementing = false;
    protected $keyType = 'int';

    protected $fillable = [
        'servicio_id',
        'categoria',
        'duracion',
        'precio',
    ];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'duracion'           => 'integer',
        'precio'             => 'decimal:2',
    ];

    // Para que {tour} en rutas use servicio_id
    public function getRouteKeyName(): string
    {
        return 'servicio_id';
    }

    /** Servicio padre */
    public function servicio()
    {
        return $this->belongsTo(Servicio::class, 'servicio_id');
    }

    /** Salidas programadas (fecha/hora + cupo) */
    public function salidas()
    {
        return $this->hasMany(TourSalida::class, 'servicio_id', 'servicio_id');
    }

    /** Items del tour  **/
    public function items()
    {
        return $this->hasMany(TourItem::class, 'servicio_id', 'servicio_id');
    }

    /** Actividades/itinerario del tour (ordenadas) */
    public function actividades()
    {
        return $this->hasMany(TourActividad::class, 'servicio_id', 'servicio_id')
                    ->orderBy('orden');
    }
}
