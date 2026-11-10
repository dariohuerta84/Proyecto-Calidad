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
            'codigo_reserva'   => strtoupper($this->faker->unique()->bothify('??########')),
            'solicitud_codigo' => 'SOL-' . strtoupper($this->faker->bothify('########')),
            // Si tienes un factory state 'viajero' en Usuario, úsalo; si no, deja ->state(['rol'=>'viajero'])
            'usuario_id'       => Usuario::factory()->state(['rol' => 'viajero']),
            'habitacion_id'    => Habitacion::factory(),
            'fecha_inicio'     => $fechaInicio->toDateString(),
            'fecha_fin'        => $fechaFin->toDateString(),
            'cantidad'         => $cantidad,
            'estado'           => $this->faker->randomElement(['pendiente','confirmada','confirmada','rechazada','cancelada']),
            'precio_por_noche' => $precio,
            'subtotal'         => round($precio * $cantidad * $noches, 2),
            'impuesto'         => 0,   // se recalcula en configure()
            'total'            => 0,
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

    /** Recalcula importes desde la tarifa real de la habitación (subtotal + 18% IGV) y completa la decisión. */
    public function configure(): self
    {
        return $this->afterCreating(function (\App\Models\ReservaHabitacion $r) {
            $hab = $r->habitacion;
            $precio = $hab ? (float) $hab->precio_por_noche : (float) $r->precio_por_noche;
            $noches = max(1, $r->fecha_inicio->diffInDays($r->fecha_fin));
            $t = \App\Support\Precio::desdeSubtotalCentimos(\App\Support\Precio::lineaCentimos($precio, (int) $r->cantidad, (int) $noches));
            $extra = [];
            if ($r->estado === 'rechazada') {
                $extra = ['motivo_rechazo' => 'Sin disponibilidad por mantenimiento en esas fechas.', 'decidido_en' => now()];
            } elseif ($r->estado === 'confirmada') {
                $extra = ['decidido_en' => now()];
            } elseif ($r->estado === 'cancelada') {
                $extra = ['cancelado_en' => now()];
            }
            $r->forceFill(['precio_por_noche' => $precio, 'tarjeta_ultimos4' => '4242'] + $t + $extra)->saveQuietly();
        });
    }
}
