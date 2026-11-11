<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\SoftDeletes;

class TourSalida extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'tour_salidas';

    protected $fillable = [
        'servicio_id',
        'fecha',
        'hora',
        'cupo_total',
        'cupo_reservado',
        'estado',
    ];
    protected $dates = ['deleted_at'];

    protected $casts = [
        'fecha'         => 'date',
        'hora'          => 'datetime:H:i',
        'cupo_total'    => 'integer',
        'cupo_reservado'=> 'integer',
    ];

    /** Servicio (padre) */
    public function servicio()
    {
        return $this->belongsTo(Servicio::class, 'servicio_id');
    }

    /** Definición del tour (1:1 con servicios) */
    public function tour()
    {
        return $this->hasOne(Tour::class, 'servicio_id', 'servicio_id');
    }

    /** Reservas asociadas a esta salida */
    public function reservas()
    {
        return $this->hasMany(ReservaTour::class, 'salida_id');
    }

    /** Disponibilidad calculada (atributo virtual) */
    public function getDisponibilidadAttribute(): int
    {
        return max(0, (int)$this->cupo_total - (int)$this->cupo_reservado);
    }

    /** Scope: solo salidas vendibles */
    public function scopeVendibles($q)
    {
        return $q->where('estado', 'programada');
    }

    /** Scope: salidas futuras (desde hoy) */
    public function scopeFuturas($q)
    {
        return $q->whereDate('fecha', '>=', now()->toDateString());
    }

    /** ¿Se puede reservar? Programada, fecha futura (hoy cuenta si la hora no pasó) y con cupo. */
    public function esReservable(int $personas = 1): bool
    {
        return $this->estado === 'programada'
            && !$this->yaPaso()
            && $this->disponibilidad >= $personas;
    }

    /** ¿La salida ya ocurrió? */
    public function yaPaso(): bool
    {
        $hora = $this->hora instanceof \Carbon\Carbon ? $this->hora->format('H:i') : substr((string) $this->hora, 0, 5);
        $inicio = \Carbon\Carbon::parse($this->fecha->toDateString() . ' ' . ($hora ?: '00:00'));
        return $inicio->isPast();
    }

    public function fechaIso(): ?string
    {
        return $this->fecha?->toDateString();
    }

    public function horaCorta(): ?string
    {
        return $this->hora instanceof \Carbon\Carbon ? $this->hora->format('H:i') : (substr((string) $this->hora, 0, 5) ?: null);
    }

}
