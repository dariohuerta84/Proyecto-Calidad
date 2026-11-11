<?php

namespace Database\Factories;

use App\Models\Tour;
use App\Models\Servicio;
use App\Models\TourItem;
use Illuminate\Database\Eloquent\Factories\Factory;

class TourFactory extends Factory
{
    protected $model = Tour::class;

    public function definition(): array
    {
        return [
            'categoria'         => $this->faker->randomElement(['Gastronomía','Aventura','Cultura','Relajación']),
            'duracion'          => $this->faker->numberBetween(120, 480), // 2h–8h
            'precio'            => $this->faker->randomFloat(2, 20, 150),
        ];
    }
    /**
     * Hook para crear items automáticamente después de crear el tour
     */
    public function createWithServicioAndItems(int $itemsMin = 2, int $itemsMax = 4): Tour
    {
        // Crear el servicio primero
        $servicio = Servicio::factory()->tour()->create();

        // 2. Crear Tour con servicio_id como PK y FK
        $tour = Tour::create(array_merge(
            $this->definition(),
            ['servicio_id' => $servicio->id] // PK y FK
        ));

        TourItem::factory()
            ->count(rand($itemsMin, $itemsMax))
            ->forServicio($servicio->id)
            ->create();

        return $tour;
    }
}
