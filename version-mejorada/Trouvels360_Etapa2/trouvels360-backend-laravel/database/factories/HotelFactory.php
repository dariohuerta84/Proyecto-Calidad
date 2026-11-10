<?php

namespace Database\Factories;

use App\Models\Hotel;
use App\Models\Servicio;
use Illuminate\Database\Eloquent\Factories\Factory;

class HotelFactory extends Factory
{
    protected $model = Hotel::class;

    public function definition(): array
    {
        return [
            'direccion'   => $this->faker->address(),
            'estrellas'   => $this->faker->numberBetween(1, 5),
        ];
    }

    public function configure()
    {
        return $this->afterMaking(function (Hotel $hotel){
            // Crear el Servicio primero
            $servicio = Servicio::factory()->hotel()->create();

            // Reusar el mismo ID del servicio
            $hotel->servicio_id = $servicio->id;
        });
    }
}
