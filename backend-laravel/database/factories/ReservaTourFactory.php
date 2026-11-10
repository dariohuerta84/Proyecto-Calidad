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
            'estado'          => $this->faker->randomElement(['pendiente','confirmada']),
            // Se recalcula luego del create para usar el precio del Tour
            'precio_unitario' => 0,
            'total'           => 0,
        ];
    }

    public function pendiente(): self
    {
        return $this->state(fn () => ['estado' => 'pendiente']);
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
            $precio = $reserva->salida?->tour?->precio_persona ?? 0;

            $reserva->precio_unitario = $precio;
            $reserva->total = $precio * $reserva->personas;
            $reserva->save();

            // Si la reserva está pendiente o confirmada, reservar cupo
            if (in_array($reserva->estado, ['pendiente','confirmada'], true)) {
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
