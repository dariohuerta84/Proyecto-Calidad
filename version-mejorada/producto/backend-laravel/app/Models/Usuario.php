<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Tymon\JWTAuth\Contracts\JWTSubject;
use Illuminate\Database\Eloquent\SoftDeletes;

/**
 * Modelo 'usuarios' — Autenticación con JWT (sin Sanctum)
 */
class Usuario extends Authenticatable implements JWTSubject
{
    use Notifiable, HasFactory; // 🔹 Eliminamos HasApiTokens (Sanctum)
    use SoftDeletes;

    protected $table = 'usuarios';

    protected $fillable = [
        'nombre', 'apellido', 'email', 'password', 'rol',
        'empresa_nombre', 'telefono', 'ruc', 'avatar_url',
    ];
    protected $dates = ['deleted_at'];

    protected $hidden = ['password', 'remember_token'];

    /**
     * Casts automáticos de atributos
     */
    protected function casts(): array
    {
        return [
            'password' => 'hashed',          // Hash automático al asignar
            'email_verified_at' => 'datetime',
        ];
    }

    /**
     * Normaliza el email antes de guardar
     */
    public function setEmailAttribute(string $value): void
    {
        $this->attributes['email'] = mb_strtolower(trim($value));
    }

    /**
     * Relación con reseñas (reviews)
     */
    public function esProveedor(): bool
    {
        return $this->rol === 'proveedor';
    }

    public function esViajero(): bool
    {
        return $this->rol === 'viajero';
    }

    public function servicios()
    {
        return $this->hasMany(Servicio::class, 'proveedor_id');
    }

    public function reservasHabitaciones()
    {
        return $this->hasMany(ReservaHabitacion::class, 'usuario_id');
    }

    public function reservasTour()
    {
        return $this->hasMany(ReservaTour::class, 'usuario_id');
    }

    /** Datos públicos de la cuenta (nunca contraseña ni tokens). */
    public function perfil(): array
    {
        return $this->only('id', 'rol', 'nombre', 'apellido', 'empresa_nombre', 'telefono', 'ruc', 'email', 'avatar_url', 'created_at', 'updated_at');
    }

    public function reviews()
    {
        return $this->hasMany(Review::class, 'usuario_id', 'id')
                    ->orderBy('created_at', 'desc');
    }

    // ------------------------------------------------------
    // JWTSubject — Métodos requeridos por tymon/jwt-auth
    // ------------------------------------------------------

    /**
     * Devuelve el identificador (clave primaria del usuario)
     */
    public function getJWTIdentifier()
    {
        return $this->getKey();
    }

    /**
     * Agrega datos personalizados al payload del JWT
     */
    public function getJWTCustomClaims(): array
    {
        return [
            'rol'   => $this->rol ?? null,
            'email' => $this->email ?? null,
        ];
    }
}
