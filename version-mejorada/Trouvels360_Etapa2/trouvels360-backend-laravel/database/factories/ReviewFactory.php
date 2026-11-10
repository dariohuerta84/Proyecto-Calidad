<?php

namespace Database\Factories;

use App\Models\Review;
use App\Models\Servicio;
use App\Models\Usuario;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends \Illuminate\Database\Eloquent\Factories\Factory<\App\Models\Reseña>
 */
class ReviewFactory extends Factory
{
    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    protected $model = Review::class;
    
    public function definition(): array
    {
        return [
            'servicio_id' => Servicio::factory(),  // se sobreescribe en seeder
            'usuario_id' => Usuario::inRandomOrder()->first()->id ?? Usuario::factory(),
            'comentario' => $this->faker->realText(100),
            'calificacion' => $this->faker->numberBetween(1, 5),
        ];
    }
}
