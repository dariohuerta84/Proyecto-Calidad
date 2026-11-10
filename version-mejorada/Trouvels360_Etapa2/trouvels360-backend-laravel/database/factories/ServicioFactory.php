<?php

namespace Database\Factories;

use Illuminate\Database\Eloquent\Factories\Factory;
use App\Models\Servicio;
use App\Models\Usuario;
use App\Models\ServicioImagen;

class ServicioFactory extends Factory
{
    protected $model = Servicio::class;

    // 🔹 Imágenes de portada según tipo
    private static array $hotelImgs = [
        'https://media-cdn.tripadvisor.com/media/photo-s/06/ee/c7/e2/hotel-valdivia.jpg',
        'https://royalparkhotel.com.pe/wp-content/uploads/2016/06/hotel-de-5-estrellas-en-peru.jpg',
        'https://www.cataloniahotels.com/es/blog/wp-content/uploads/2016/11/catalonia-riviera-maya.jpg',
        'https://www.incatrilogytours.com/wp-content/uploads/2024/03/hotel-lujoso-cusco-cartagena2.jpg',
        'https://www.turiweb.pe/wp-content/uploads/2021/10/hoteles-071021.jpg',
        'https://www.viajes.cl/hubfs/Vista%20a%C3%A9rea%20del%20hotel%20Belmond%20Miraflores%20Park%20en%20Lima.jpg',
    ];

    private static array $tourImgs = [
        'https://magiccuscotoursperu.com/wp-content/uploads/2020/02/Excursiones-en-Cusco-y-Machu-Picchu-5-dias-700x500.jpg',
        'https://www.arequipatoursagencia.com/wp-content/uploads/2023/10/puno.jpg',
        'https://www.libertyperutravel.com/wp-content/uploads/2023/10/city-tour-lima.jpg',
        'https://exclusiveperutours.com/wp-content/uploads/2022/03/machupicchu-exclusiveperutours.jpg',
        'https://www.peru-tours.org/images/machupicchu/tour-machupicchu-private.jpg',
        'https://www.unitoursperu.com/wp-content/uploads/2021/07/puno-uros.jpg',
    ];

    public function definition(): array
    {
        // Seleccionar tipo
        $tipo = $this->faker->randomElement(['hotel', 'tour']);

        // Lista de imágenes según tipo
        $imagenes = $tipo === 'hotel' ? self::$hotelImgs : self::$tourImgs;

        return [
            'proveedor_id' => Usuario::factory()->state(['rol' => 'proveedor']),
            'nombre'       => $this->faker->company . ' Service',
            'tipo'         => $tipo,
            'descripcion'  => $this->faker->sentence(10),
            'ciudad'       => $this->faker->city,
            'pais'         => 'Perú', // Mejor forzar Perú si usas coordenadas de Perú
            
            // 📍 NUEVOS CAMPOS DE GEOLOCALIZACIÓN
            // Coordenadas aproximadas de Perú (para que no salgan en África o el mar)
            'latitud'      => $this->faker->latitude(-18, -0.3),  // Entre Tacna y Putumayo
            'longitud'     => $this->faker->longitude(-81, -68), // Entre Talara y Puerto Maldonado
            
            // Portada según tipo
            'imagen_url'   => $this->faker->randomElement($imagenes),
            'activo'       => true,
        ];
    }

    /** Estado: tipo hotel */
    public function hotel(): self
    {
        return $this->state(fn () => [
            'tipo'       => 'hotel',
            'imagen_url' => $this->faker->randomElement(self::$hotelImgs),
        ]);
    }

    /** Estado: tipo tour */
    public function tour(): self
    {
        return $this->state(fn () => [
            'tipo'       => 'tour',
            'imagen_url' => $this->faker->randomElement(self::$tourImgs),
        ]);
    }

    /**
     * Crea hasta N imágenes en servicio_imagenes (1:N) con URLs según tipo.
     * Uso: Servicio::factory()->withImagenes(3)->create()
     */
    public function withImagenes(int $cantidad = 3): self
    {
        $cantidad = max(1, min($cantidad, 5));

        return $this->afterCreating(function (Servicio $servicio) use ($cantidad) {
            // Seleccionar lista de imágenes según tipo del servicio
            $imagenes = $servicio->tipo === 'hotel' ? self::$hotelImgs : self::$tourImgs;
            shuffle($imagenes);
            $seleccion = array_slice($imagenes, 0, $cantidad);

            foreach ($seleccion as $url) {
                ServicioImagen::create([
                    'servicio_id' => $servicio->id,
                    'url'         => $url,
                    'alt'         => $this->faker->words(3, true),
                ]);
            }
        });
    }

    /**
     * Después de crear un servicio, asegura que tenga 5 imágenes en total.
     */
    public function configure(): static
    {
        return $this->afterCreating(function (Servicio $servicio) {
            $actual = $servicio->imagenes()->count();
            $faltan = 5 - $actual;

            if ($faltan > 0) {
                ServicioImagen::factory()
                    ->count($faltan)
                    ->for($servicio)
                    ->create();
            }
        });
    }

}
