<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\SoftDeletes;

class ReservaTour extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'reservas_tour';

    protected $fillable = [
        'codigo_reserva',
        'usuario_id',
        'salida_id',
        'personas',
        'estado',
        'precio_unitario',
        'subtotal',
        'impuesto',
        'total',
        'tarjeta_ultimos4',
        'cancelado_en',
    ];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'personas'        => 'integer',
        'precio_unitario' => 'decimal:2',
        'subtotal'        => 'decimal:2',
        'impuesto'        => 'decimal:2',
        'total'           => 'decimal:2',
        'cancelado_en'    => 'datetime',
    ];

    /** Viajero que reserva */
    public function usuario()
    {
        return $this->belongsTo(Usuario::class, 'usuario_id');
    }

    /** Salida reservada */
    public function salida()
    {
        return $this->belongsTo(TourSalida::class, 'salida_id')->withTrashed();
    }

    /** Servicio (a través de la salida) */
    public function servicio()
    {
        // Relación de conveniencia: reservas_tour -> tour_salidas -> servicios
        return $this->hasOneThrough(
            Servicio::class,
            TourSalida::class,
            'id',            // Foreign key on TourSalida...
            'id',            // Foreign key on Servicio...
            'salida_id',     // Local key on ReservaTour...
            'servicio_id'    // Local key on TourSalida...
        );
    }
}
