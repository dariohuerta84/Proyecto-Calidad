<?php

namespace Database\Factories;

use Illuminate\Database\Eloquent\Factories\Factory;
use App\Models\Reserva;
use App\Models\Usuario;
use App\Models\Servicio;
use Illuminate\Support\Str;

/**
 * @extends \Illuminate\Database\Eloquent\Factories\Factory<\App\Models\Reserva>
 */
class ReservaFactory extends Factory
{
    protected $model = Reserva::class;

    public function definition(): array
    {
        $fechaInicio = $this->faker->dateTimeBetween('+1 days', '+1 month');
        $fechaFin    = (clone $fechaInicio)->modify('+'.rand(1,5).' days');

        return [
            'codigo_reserva' => strtoupper(Str::random(8)),
            'usuario_id'     => Usuario::factory()->state(['rol' => 'viajero']),
            'servicio_id'    => Servicio::factory(),
            'fecha_inicio'   => $fechaInicio->format('Y-m-d'),
            'fecha_fin'      => $fechaFin->format('Y-m-d'),
            'huespedes'      => $this->faker->numberBetween(1, 5),
            'estado'         => $this->faker->randomElement(['pendiente','confirmada','cancelada']),
        ];
    }
}
