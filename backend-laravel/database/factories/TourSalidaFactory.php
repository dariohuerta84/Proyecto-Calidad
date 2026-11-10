<?php

namespace Database\Factories;

use App\Models\Tour;
use App\Models\TourSalida;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<TourSalida>
 */
class TourSalidaFactory extends Factory
{
    protected $model = TourSalida::class;

    public function definition(): array
    {
        /**
         * Si desde fuera ya te pasan 'servicio_id' (p. ej. create(['servicio_id' => X])),
         * Eloquent fusiona ese estado con el array retornado aquí.
         * Para evitar crear tours “fantasma”, buscamos un Tour existente y,
         * solo si no existe ninguno, creamos uno.
         */
        $tour = Tour::inRandomOrder()->first() ?? Tour::factory()->create();
        $servicioId = $tour->servicio_id;

        $cupoTotal = $tour->capacidad_por_salida ?? $this->faker->numberBetween(8, 40);

        return [
            'servicio_id'    => $servicioId,
            'fecha'          => $this->faker->dateTimeBetween('+1 days', '+2 months')->format('Y-m-d'),
            'hora'           => $this->faker->time('H:i:00'),
            'cupo_total'     => $cupoTotal,
            'cupo_reservado' => 0,
            'estado'         => 'programada', // programada | cerrada | cancelada
        ];
    }

    public function cerrada(): self
    {
        return $this->state(fn () => ['estado' => 'cerrada']);
    }

    public function cancelada(): self
    {
        return $this->state(fn () => ['estado' => 'cancelada']);
    }

    /**
     * Conveniencia para forzar el servicio objetivo sin crear Tour nuevo.
     */
    public function forServicio(int $servicioId): self
    {
        return $this->state(fn () => ['servicio_id' => $servicioId]);
    }

    public function pasado(): self
    {
        return $this->state(function (array $attributes) {
            return [
                'fecha' => $this->faker->dateTimeBetween('-2 months', '-1 day')->format('Y-m-d'),
            ];
        });
    }
}
