<?php

namespace Database\Factories;

use App\Models\Tour;
use App\Models\TourActividad;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<TourActividad>
 */
class TourActividadFactory extends Factory
{
    protected $model = TourActividad::class;

    public function definition(): array
    {   
        // Posibles actividades
        $activities=[
            "Exploración del casco antiguo",
            "Degustación de comida local",
            "Visita a un mercado tradicional",
            "Recorrido panorámico en bus",
            "Taller de artesanía regional",
            "Paseo en bote por el río",
            "Senderismo con vistas épicas",
            "Descubrimiento de ruinas antiguas",
            "Sesión de fotos en puntos clave",
            "Cata de vinos o bebidas típicas",
            "Observación de fauna silvestre",
            "Noche de leyendas e historias"
        ];
        // Seleccionamos 1 actividad aleatoria del array
        $activity = $this->faker->randomElement($activities);

        return [
            // Relación con un tour (se sobreescribe en el seeder normalmente)
            'servicio_id'  => null, // debe recibir de create() o forServicio()
            'titulo'       => $activity,
            'descripcion'  => $this->faker->optional()->sentence(10),
            'orden'        => 1, // Default (el seeder lo sobreescribe con 1,2,3…)
            'duracion_min' => $this->faker->optional()->numberBetween(30, 180),
            'direccion'    => $this->faker->optional()->address(),
            'imagen_url'   => $this->faker->optional()->imageUrl(800, 600, 'tour', true),
        ];
    }

    /** Permite forzar el orden desde fuera */
    public function orden(int $orden): self
    {
        return $this->state(fn () => ['orden' => $orden]);
    }
    /** Permite forzar el servicio asociado */
    public function forServicio(int $servicioId): self
    {
        return $this->state(fn () => ['servicio_id' => $servicioId]);
    }
}
