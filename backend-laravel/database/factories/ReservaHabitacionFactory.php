<?php

namespace Database\Factories;

use App\Models\ReservaHabitacion;
use App\Models\Usuario;
use App\Models\Habitacion;
use Illuminate\Database\Eloquent\Factories\Factory;
use Carbon\Carbon;

class ReservaHabitacionFactory extends Factory
{
    protected $model = ReservaHabitacion::class;

    public function definition(): array
    {
        $fechaInicio = Carbon::instance($this->faker->dateTimeBetween('+1 days', '+1 month'));
        $noches      = $this->faker->numberBetween(1, 7);
        $fechaFin    = (clone $fechaInicio)->copy()->addDays($noches);

        $precio   = $this->faker->randomFloat(2, 50, 500);
        $cantidad = $this->faker->numberBetween(1, 3);

        return [
            //'codigo_reserva'   => strtoupper($this->faker->bothify('RES-####')),
            'codigo_reserva' => 'RES-' . str_pad($this->faker->unique()->numberBetween(1, 9999), 4, '0', STR_PAD_LEFT),
            // Si tienes un factory state 'viajero' en Usuario, úsalo; si no, deja ->state(['rol'=>'viajero'])
            'usuario_id'       => Usuario::factory()->state(['rol' => 'viajero']),
            'habitacion_id'    => Habitacion::factory(),
            'fecha_inicio'     => $fechaInicio->toDateString(),
            'fecha_fin'        => $fechaFin->toDateString(),
            'cantidad'         => $cantidad,
            'estado'           => $this->faker->randomElement(['pendiente','confirmada','cancelada']),
            'precio_por_noche' => $precio,
            'total'            => round($precio * $cantidad * $noches, 2), // noches reales
        ];
    }

    public function pasado(): self
    {
        return $this->state(function (array $attributes) {
            $fechaInicio = Carbon::instance($this->faker->dateTimeBetween('-1 month', '-1 day'));
            $noches = $this->faker->numberBetween(1, 7);
            $fechaFin = (clone $fechaInicio)->copy()->addDays($noches);

            return [
                'fecha_inicio' => $fechaInicio->toDateString(),
                'fecha_fin' => $fechaFin->toDateString(),
            ];
        });
    }
}
