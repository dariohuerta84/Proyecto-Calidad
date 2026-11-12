<?php

namespace Database\Factories;

use App\Models\Usuario;
use App\Models\TourSalida;
use App\Models\ReservaTour;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<ReservaTour>
 */
class ReservaTourFactory extends Factory
{
    protected $model = ReservaTour::class;

    public function definition(): array
    {
        return [
            'codigo_reserva'  => strtoupper($this->faker->bothify('RT-########')),
            'usuario_id'      => Usuario::factory()->state(['rol' => 'viajero']),
            'salida_id'       => TourSalida::factory(), // se creará una salida con su tour/servicio
            'personas'        => $this->faker->numberBetween(1, 6),
            // Los tours se confirman automáticamente (HU-V09): no existen reservas de tour pendientes.
            'estado'          => $this->faker->randomElement(['confirmada', 'confirmada', 'confirmada', 'cancelada']),
            // Se recalcula luego del create para usar el precio del Tour
            'precio_unitario' => 0,
            'total'           => 0,
        ];
    }

    public function confirmada(): self
    {
        return $this->state(fn () => ['estado' => 'confirmada']);
    }

    public function cancelada(): self
    {
        return $this->state(fn () => ['estado' => 'cancelada']);
    }

    public function pasado(): self
    {
        return $this->state(function (array $attributes) {
            return [
                'salida_id' => TourSalida::factory()->pasado(),
            ];
        });
    }

    public function configure(): self
    {
        return $this->afterCreating(function (ReservaTour $reserva) {
            // Tomamos el precio desde el Tour asociado a la salida
            $precio = (float) ($reserva->salida?->tour?->precio ?? 0);
            $t = \App\Support\Precio::desdeSubtotalCentimos(\App\Support\Precio::lineaCentimos($precio, (int) $reserva->personas));
            $extra = $reserva->estado === 'cancelada' ? ['cancelado_en' => now()] : [];
            $reserva->forceFill(['precio_unitario' => $precio, 'tarjeta_ultimos4' => '4242'] + $t + $extra)->saveQuietly();

            // Solo las confirmadas ocupan cupo
            if ($reserva->estado === 'confirmada') {
                $salida = $reserva->salida;
                if ($salida) {
                    $salida->cupo_reservado = min(
                        $salida->cupo_total,
                        $salida->cupo_reservado + $reserva->personas
                    );
                    $salida->save();
                }
            }
        });
    }
}
