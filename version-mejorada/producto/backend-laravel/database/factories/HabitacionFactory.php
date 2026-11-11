<?php

namespace Database\Factories;

use App\Models\Habitacion;
use App\Models\Hotel;
use Illuminate\Database\Eloquent\Factories\Factory;

class HabitacionFactory extends Factory
{
    protected $model = Habitacion::class;

    public function definition(): array
    {
        return [
            // Se asegura que exista el hotel (y por ende el servicio tipo hotel)
            'servicio_id'       => Hotel::factory(), 
            'nombre'            => $this->faker->word() . ' Room',
            'capacidad_adultos' => $this->faker->numberBetween(1, 4),
            'capacidad_ninos'   => $this->faker->numberBetween(0, 3),
            'cantidad'          => $this->faker->numberBetween(1, 20),
            'precio_por_noche'  => $this->faker->randomFloat(2, 50, 500),
            'descripcion'       => $this->faker->sentence(8),
        ];
    }
}
