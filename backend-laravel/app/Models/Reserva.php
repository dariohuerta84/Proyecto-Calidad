<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

/**
 * @use HasFactory<\Database\Factories\ReservaFactory>
 */
class Reserva extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'reservas';

    protected $fillable = [
        'codigo_reserva',
        'usuario_id',
        'servicio_id',
        'fecha_inicio',
        'fecha_fin',
        'huespedes',
        'estado',
    ];
    protected $dates = ['deleted_at'];

    // � Accessors
    public function getPrecioTotalAttribute(): float
    {
        $fechaInicio = new \DateTime($this->fecha_inicio);
        $fechaFin = new \DateTime($this->fecha_fin);
        $dias = $fechaInicio->diff($fechaFin)->days;

        if ($this->servicio->tipo === 'hotel' && $this->servicio->relationLoaded('hotel') && $this->servicio->hotel) {
            return $dias * $this->servicio->hotel->precio_por_noche;
        }

        if ($this->servicio->tipo === 'tour' && $this->servicio->relationLoaded('tour') && $this->servicio->tour) {
            return $this->huespedes * $this->servicio->tour->precio_por_persona;
        }

        return $this->servicio->precio; // Precio base como fallback
    }

    public function getDiasReservaAttribute(): int
    {
        $fechaInicio = new \DateTime($this->fecha_inicio);
        $fechaFin = new \DateTime($this->fecha_fin);
        
        return $fechaInicio->diff($fechaFin)->days;
    }

    // �🔗 Relaciones
    public function usuario()
    {
        return $this->belongsTo(Usuario::class, 'usuario_id');
    }

    public function servicio()
    {
        return $this->belongsTo(Servicio::class, 'servicio_id');
    }
}
