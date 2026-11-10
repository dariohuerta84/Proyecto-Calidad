<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\SoftDeletes;

class TourActividad extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'tour_actividades';

    protected $fillable = [
        'servicio_id',
        'titulo',
        'descripcion',
        'orden',
        'duracion_min',
        'direccion',
        'imagen_url',
    ];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'orden'        => 'integer',
        'duracion_min' => 'integer',
    ];

    /** Servicio (de tipo 'tour') */
    public function servicio()
    {
        return $this->belongsTo(Servicio::class, 'servicio_id');
    }
}
