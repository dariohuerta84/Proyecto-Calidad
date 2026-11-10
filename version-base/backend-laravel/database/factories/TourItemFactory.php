<?php

namespace Database\Factories;

use App\Models\Tour;
use App\Models\TourItem;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<TourItem>
 */
class TourItemFactory extends Factory
{
    protected $model = TourItem::class;

    public function definition(): array
    {
        // Posibles items y emojis
        $items = [
            ['nombre' => 'Cámara',          'icono' => 'fa-solid fa-camera'],
            ['nombre' => 'Bloqueador solar','icono' => 'fa-solid fa-sun'],
            ['nombre' => 'Gorra',           'icono' => 'fa-solid fa-hat-cowboy'],
            ['nombre' => 'Zapatillas',      'icono' => 'fa-solid fa-shoe-prints'],
            ['nombre' => 'Agua',            'icono' => 'fa-solid fa-bottle-water'],
            ['nombre' => 'Gafas de sol',    'icono' => 'fa-solid fa-glasses'],
        ];

        // Seleccionamos 1 item aleatorio del array
        $item = $this->faker->randomElement($items);

        return [
            //'servicio_id' debe pasarse siempre con forServicio()
            'nombre'      => $item['nombre'],
            'icono'       => $item['icono'],
        ];
    }

    /**
     * Conveniencia para forzar el servicio objetivo sin crear Tour nuevo.
     */
    public function forServicio(int $servicioId): self
    {
        return $this->state(fn () => ['servicio_id' => $servicioId]);
    }
}
