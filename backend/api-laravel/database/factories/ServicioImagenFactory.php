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
        'https://image-tc.galaxy.tf/wijpeg-8spaz5qwcxxga1tyvo5hwqgh3/superior-1_standard.jpg?crop=0%2C0%2C1280%2C960',
        'https://media.istockphoto.com/id/1418784247/es/foto/lobby-del-hotel-de-lujo.jpg?s=612x612&w=0&k=20&c=DUaM_aN8lXyCsH7sGFAXXxFsS1D9qGVCcv_2kNuuu1E=',
        'https://www.swissotel.com/assets/0/92/2119/2178/2217/2219/6442451722/2916b340-6b67-43c8-a285-70205acccfd5.jpg',
        'https://yucatantoday.com/hubfs/Imported_Blog_Media/Restaurante-Cuna-by-Wayam-Mundo-Imperial-CUNA_Lavalle-Peniche-Arquitectos-20-sin-logo-1.jpeg',
        'https://www.thebathcollection.com/wp-content/uploads/2025/10/ORANGINE-4-1024x684.jpg',
        'https://images.mirai.com/INFOROOMS/100129840/Bd6e4oDlvFOE8cxbG3js/Bd6e4oDlvFOE8cxbG3js_large.jpg',
    ];

    protected $tourImgs = [
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQHAgolf5pLEapP1aL7GEcqs6UYl5sTBD1PFTMRDlIWKXycfkfJGhKX3SHw&s=10',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRw1tUer7b67eR2rIQy33_GMxtneOeSE19iz4-m5ukr2j83i1KCSig0cWNj&s=10',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGeGUMw3MQFgsEivjq0ihYTPQfLhFkEUBTmp_OWipZ_Uk6oIb25bAGkuw&s=10',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQhwBYh-wny_orE4DfsS4FM4Pbzg-WpL1wtWmk6U1lyzmLn0imU1g2ILXE&s=10',
        'https://images.myguide-cdn.com/md/common/large/5db96c42e3c31-630329.jpg',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRZpaicx_EJWl67Jqqoe6-GELX7vi9ctVX8wqlAkop8LqGMtYnRfEl_66Aw&s=10',
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