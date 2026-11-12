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
        'https://x.cdrst.com/foto/hotel-sf/1396c2d5/granderesp/foto-hotel-1396b82b.jpg',
        'https://www.tangol.com/Fotos/Hospedaje/selina-miraflores-lima_10419_202105061415280.JPG',
        'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/03/5b/67/7f/hilton-lima-miraflores.jpg?w=900&h=-1&s=1',
        'https://cf.bstatic.com/xdata/images/hotel/max1024x768/794330398.jpg?k=7b49f06ce75edcf9b541b1d5bef21a2d8c1c28f74f98906e7d12f7a305482f08&o=',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT8d3B_SWorkunmvZM55wYWWOLfDaHfjnzev_EWtBeOOqbkxZhWwfeuOBM&s=10',
        'https://media-cdn.tripadvisor.com/media/photo-s/27/59/0e/18/casa-andina-premium-cusco.jpg',
    ];

    private static array $tourImgs = [
        'https://www.machupicchu-inca-boletos.com/wp-content/uploads/2024/11/7584-machu-picchu-full-day-80.jpg',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQSQKtGPz58-FV_busuvzEz5UcQqEt_h8aKTjcBOL-n6v7RKGdgqO2ooumx&s=10',
        'https://www.kontikiperu.com/wp-content/uploads/2019/04/city-tour-lima7.jpg',
        'https://wp.tour7colores.com/wp-content/uploads/2025/05/turistas-montana-colores-vinicunca-sm.jpg',
        'https://www.qeswachakaperutours.com/wp-content/uploads/galeria-tour-laguna-humantay-3.jpg',
        'https://www.condorxtreme.com/wp-content/uploads/2023/07/Tour-Colca-Full-Day-Arequipa-Peru.jpg',
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
