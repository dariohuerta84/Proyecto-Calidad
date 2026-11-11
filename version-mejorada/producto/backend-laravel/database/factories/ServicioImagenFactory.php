<?php

namespace Database\Factories;

use App\Models\ServicioImagen;
use App\Models\Servicio;
use Illuminate\Database\Eloquent\Factories\Factory;

class ServicioImagenFactory extends Factory
{
    protected $model = ServicioImagen::class;

    // Bancos de imágenes definidos como propiedades para reutilizarlos
    protected $hotelImgs = [
        'https://hotevia.info/wp-content/uploads/2021/08/acolpacha-tambo-boutique-hotel-arequipa.jpeg',
        'https://www.fiesta-tours-peru.com/img/hotels/arequipa/city/big/qp-hotels-arequipa.jpg',
        'https://www.es.kayak.com/rimg/himg/46/62/64/expedia_group-677469-f1935a-282581.jpg?width=836&height=607&crop=true',
        'https://content.r9cdn.net/rimg/dimg/97/d2/53102ef1-city-10174-17279266c9c.jpg?width=1200&height=630&crop=true',
        'https://www.lostambos.com.pe/colonial/wp-content/uploads/2022/12/Fachada-2.jpeg',
        'https://www.marvelousperu.com/wp-content/uploads/2025/09/Hoteles-mas-exclusivos-de-Lima-con-rooftop-y-vista-al-mar.webp',
        'https://www.pacuchaglamping.com/wp-content/uploads/2023/08/cropped-A68732A5-1832-4CFA-BD1E-28EEF73E49AE-scaled-1-e1694223137525.jpeg',
        'https://www.pacuchaglamping.com/wp-content/uploads/2023/09/libertador-puno-hoteles-con-las-mejores-vistas-de-peru.jpeg',
        'https://media-cdn.tripadvisor.com/media/photo-s/30/db/19/ce/courtyard.jpg',
        'https://media-cdn.tripadvisor.com/media/photo-s/2f/f8/93/1a/breezy-king-guest-room.jpg',
        'https://travitour.pe/cdn/shop/files/HOTELPLAYAPISCINARESTAURANTEVISTAALMARVICHAYITOMANCORATALARAPIURAPERUTRAVITOUR_5.jpg?v=1736639283&width=1445',
    ];

    protected $tourImgs = [
        'https://www.tours-machupicchu-peru.com/wp-content/uploads/2021/10/City-Tour-Cusco-Peru.jpg',
        'https://www.amazingperu.com/es/imagenes/tours/tour_de_aventura_de_lujo_peru_tour.jpg',
        'https://rycexcursionesrd.com/wp-content/uploads/2024/05/tour-oasis-huacachina-x-travel-peru.jpg',
        'https://www.qeswachakaperutours.com/wp-content/uploads/galeria-machupicchu-full-day-4.jpg',
        'https://www.limatourperu.com/wp-content/uploads/city-tour-centro-historico-lima-tour-peru.jpg',
        'https://www.machupicchuandestours.com/wp-content/uploads/2018/09/ballestas-islands-tours-paracas-peru.webp',
        'https://www.machupicchu.com.pe/wp-content/uploads/2023/08/tours-a-machu-picchu-desde-cusco-1024x634.jpg',
        'https://andinoperu.b-cdn.net/wp-content/uploads/2023/06/portada-andino-peru-tours-caminata-montana-de-colores-full-day.webp',
        'https://andeangreattreks.com/wp-content/uploads/what-to-expect-of-the-tour-in-peru-.jpg',
        'https://www.machupicchu.com.pe/wp-content/uploads/2023/07/machupicchu-panoramico.jpg',
    ];

    public function definition(): array
    {
        // Por defecto, mezcla todas
        $todas = array_merge($this->hotelImgs, $this->tourImgs);

        return [
            // Definimos Servicio::factory() por si se usa standalone, 
            // pero el Seeder sobrescribirá esto automáticamente.
            'servicio_id' => Servicio::factory(), 
            'url' => $this->faker->randomElement($todas),
            'alt' => $this->faker->sentence(2),
        ];
    }

    /** * Estado para forzar fotos de Hotel 
     * Este es el método que te faltaba y causaba el error.
     */
    public function hotel(): self
    {
        return $this->state(function (array $attributes) {
            return [
                'url' => $this->faker->randomElement($this->hotelImgs),
            ];
        });
    }

    /** * Estado para forzar fotos de Tour
     * Este es el método que te faltaba y causaba el error.
     */
    public function tour(): self
    {
        return $this->state(function (array $attributes) {
            return [
                'url' => $this->faker->randomElement($this->tourImgs),
            ];
        });
    }
}