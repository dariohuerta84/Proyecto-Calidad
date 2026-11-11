<?php

namespace Database\Factories;

use App\Models\Usuario;
use Illuminate\Database\Eloquent\Factories\Factory;

class UsuarioFactory extends Factory
{
    protected $model = Usuario::class;

    public function definition(): array
    {
        $rol = $this->faker->randomElement(['viajero', 'proveedor']);

        $base = [
            'email'    => $this->faker->unique()->safeEmail(),
            'password' => 'password', // el cast del modelo lo hashea automáticamente
            'rol'      => $rol,
        ];

        if ($rol === 'viajero') {
            return array_merge($base, [
                'nombre'          => $this->faker->firstName(),
                'apellido'        => $this->faker->lastName(),
                'empresa_nombre'  => null,
                'telefono'        => null,
                'ruc'             => null,
            ]);
        }

        // proveedor
        return array_merge($base, [
            'nombre'          => null,
            'apellido'        => null,
            'empresa_nombre'  => $this->faker->company(),
            // Teléfono peruano con prefijo internacional +51
            'telefono'        => '+51 9' . $this->faker->numerify('########'),
            'ruc'             => (string) $this->faker->numerify('2##########'),
        ]);
    }

    /** Estado fijo: viajero */
    public function viajero(): self
    {
        return $this->state(fn() => [
            'rol'             => 'viajero',
            'nombre'          => $this->faker->firstName(),
            'apellido'        => $this->faker->lastName(),
            'empresa_nombre'  => null,
            'telefono'        => null,
            'ruc'             => null,
        ]);
    }

    /** Estado fijo: proveedor */
    public function proveedor(): self
    {
        return $this->state(fn() => [
            'rol'             => 'proveedor',
            'nombre'          => null,
            'apellido'        => null,
            'empresa_nombre'  => $this->faker->company(),
            'telefono'        => '+51 9' . $this->faker->numerify('########'),
            'ruc'             => (string) $this->faker->numerify('2##########'),
        ]);
    }
}