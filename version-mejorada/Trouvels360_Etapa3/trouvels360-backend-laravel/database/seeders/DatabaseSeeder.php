<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Carbon\Carbon;

// Modelos
use App\Models\Usuario;
use App\Models\Servicio;
use App\Models\Hotel;
use App\Models\Habitacion;
use App\Models\ReservaHabitacion;
use App\Models\Tour;
use App\Models\TourSalida;
use App\Models\TourActividad;
use App\Models\TourItem;
use App\Models\ReservaTour;
use App\Models\Review;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // =========================================================================
        // 1. USUARIOS DEL SISTEMA
        // =========================================================================
        
        // Usuario Proveedor Principal
        $proveedor = Usuario::firstOrCreate(
            ['email' => 'edwinproveedor@gmail.com'],
            [
                'nombre'          => null,
                'apellido'        => null,
                'empresa_nombre'  => 'Edwin Proveedor SAC',
                'telefono'        => '+51 999888777',
                'ruc'             => '20123456789',
                'password'        => 'proveedor', 
                'rol'             => 'proveedor',
            ]
        );

        // Usuario Viajero Principal (TÚ)
        $viajeroPrincipal = Usuario::firstOrCreate(
            ['email' => 'edwinviajero@gmail.com'],
            [
                'nombre'          => 'Edwin',
                'apellido'        => 'Viajero',
                'empresa_nombre'  => null,
                'telefono'        => null,
                'ruc'             => null,
                'password'        => 'viajero',
                'rol'             => 'viajero',
            ]
        );

        // SOLO 3 Viajeros extra para simular movimiento en la plataforma
        // Guardamos la referencia en $otrosViajeros para usarla en las reservas de relleno
        $otrosViajeros = Usuario::factory()->viajero()->count(3)->create();

        // =========================================================================
        // 2. HOTELES (Infraestructura base)
        // =========================================================================
        $hotelesConfig = [
            // --- LIMA ---
            [
                'servicio' => ['nombre' => 'JW Marriott Lima', 'ciudad' => 'Lima', 'pais' => 'Perú', 'lat' => -12.1291, 'lon' => -77.0299, 'img' => 'https://blog.viajesmachupicchu.travel/wp-content/uploads/2020/11/image-18.png', 'desc' => 'Lujo frente al mar en Miraflores.'],
                'hotel' => ['estrellas' => 5],
                'habitaciones' => [['n'=>'Deluxe Ocean View', 'p'=>210], ['n'=>'Executive Suite', 'p'=>350], ['n'=>'Family Connecting', 'p'=>420]]
            ],
            [
                'servicio' => ['nombre' => 'Selina Miraflores', 'ciudad' => 'Lima', 'pais' => 'Perú', 'lat' => -12.1220, 'lon' => -77.0310, 'img' => 'https://z.cdrst.com/foto/hotel-sf/1247a0ea/granderesp/foto-hotel-12479640.jpg', 'desc' => 'Ambiente moderno y coworking.'],
                'hotel' => ['estrellas' => 3],
                'habitaciones' => [['n'=>'Standard', 'p'=>80], ['n'=>'Micro', 'p'=>50], ['n'=>'Suite', 'p'=>120]]
            ],
            [
                'servicio' => ['nombre' => 'Hotel Hilton Lima Miraflores', 'ciudad' => 'Lima', 'pais' => 'Perú', 'lat' => -12.125205, 'lon' => -77.030151, 'img' => 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/03/5b/67/7f/hilton-lima-miraflores.jpg?w=900&h=-1&s=1', 'desc' => 'Hotel moderno ubicado en el corazón de Miraflores.'],
                'hotel' => ['estrellas' => 5],
                'habitaciones' => [['n'=>'Bungalow', 'p'=>300], ['n'=>'Doble', 'p'=>190], ['n'=>'Familiar', 'p'=>280]]
            ],
            
            // --- CUSCO ---
            [
                'servicio' => ['nombre' => 'Palacio del Inka', 'ciudad' => 'Cusco', 'pais' => 'Perú', 'lat' => -13.5170, 'lon' => -71.9722, 'img' => 'https://cache.marriott.com/content/dam/marriott-renditions/CUZLC/cuzlc-reception-8082-hor-clsc.jpg?output-quality=70&interpolation=progressive-bilinear&downsize=1300px:*', 'desc' => 'Historia viva frente al Koricancha.'],
                'hotel' => ['estrellas' => 5],
                'habitaciones' => [['n'=>'Colonial', 'p'=>280], ['n'=>'Inca Suite', 'p'=>450]]
            ],
            [
                'servicio' => ['nombre' => 'Tierra Viva Cusco Centro', 'ciudad' => 'Cusco', 'pais' => 'Perú', 'lat' => -13.5150, 'lon' => -71.9750, 'img' => 'https://tierravivahoteles.com/wp-content/uploads/2023/03/0-TVH-Cusco-Centro-Signature.jpeg', 'desc' => 'Comodidad a pasos de la Plaza.'],
                'hotel' => ['estrellas' => 3],
                'habitaciones' => [['n'=>'Doble', 'p'=>90], ['n'=>'Triple', 'p'=>130]]
            ],
            [
                'servicio' => ['nombre' => 'Casa Andina Premium Cusco', 'ciudad' => 'Cusco', 'pais' => 'Perú', 'lat' => -13.521226, 'lon' => -71.968173, 'img' => 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/13/43/8e/ce/casa-andina-premium-cusco.jpg?w=900&h=500&s=1', 'desc' => 'Hotel histórico con arquitectura colonial.'],
                'hotel' => ['estrellas' => 3],
                'habitaciones' => [['n'=>'Bungalow', 'p'=>310], ['n'=>'Doble', 'p'=>199], ['n'=>'Familiar', 'p'=>288]]
            ],

            // --- AREQUIPA ---
            [
                'servicio' => ['nombre' => 'Casa Andina Premium Arequipa', 'ciudad' => 'Arequipa', 'pais' => 'Perú', 'lat' => -16.3988, 'lon' => -71.5369, 'img' => 'https://cdn.adventure-life.com/70/83/2/h76n3yjf/1300x820.webp', 'desc' => 'Casona colonial restaurada.'],
                'hotel' => ['estrellas' => 4],
                'habitaciones' => [['n'=>'Superior', 'p'=>150], ['n'=>'Suite Republicana', 'p'=>250]]
            ],
            [
                'servicio' => ['nombre' => 'Hotel Sonesta Posadas del Inca', 'ciudad' => 'Arequipa', 'pais' => 'Perú', 'lat' => -16.403964, 'lon' => -71.537460, 'img' => 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/1b/91/14/da/sonesta-posadas-del-inca.jpg?w=500&h=-1&s=1', 'desc' => 'Hotel acogedor cerca del Monasterio de Santa Catalina.'],
                'hotel' => ['estrellas' => 3],
                'habitaciones' => [['n'=>'Económica', 'p'=>90], ['n'=>'Doble', 'p'=>140], ['n'=>'Triple', 'p'=>190]]
            ],
            [
                'servicio' => ['nombre' => 'Costa del Sol Arequipa', 'ciudad' => 'Arequipa', 'pais' => 'Perú', 'lat' => -16.3988, 'lon' => -71.5350, 'img' => 'https://www.costadelsolperu.com/wp-content/uploads/2024/07/Oferta-Arequipa-2.jpg', 'desc' => 'Hotel con piscina y vista al Misti.'],
                'hotel' => ['estrellas' => 5],
                'habitaciones' => [['n'=>'Deluxe', 'p'=>220], ['n'=>'Suite', 'p'=>380], ['n'=>'Family', 'p'=>420]]
            ],

            // --- PUNO ---
            [
                'servicio' => ['nombre' => 'GHL Hotel Lago Titicaca', 'ciudad' => 'Puno', 'pais' => 'Perú', 'lat' => -15.8378, 'lon' => -69.9965, 'img' => 'https://cf.bstatic.com/xdata/images/hotel/max1024x768/154138730.jpg?k=a8aa8187fc2981298c1b24bfd5c2651b3e571b379ef74a0f76b8c125d2ac7059&o=', 'desc' => 'Vistas impresionantes al lago.'],
                'hotel' => ['estrellas' => 5],
                'habitaciones' => [['n'=>'Lake View', 'p'=>180], ['n'=>'Sunrise', 'p'=>220]]
            ],
            [
                'servicio' => ['nombre' => 'Hotel Tierra Viva Puno Plaza', 'ciudad' => 'Puno', 'pais' => 'Perú', 'lat' => -15.842231, 'lon' => -70.019951, 'img' => 'https://tierravivahoteles.com/wp-content/uploads/2023/03/TVP-3-Fachada-1.jpg', 'desc' => 'Hotel moderno cerca del Lago Titicaca.'],
                'hotel' => ['estrellas' => 4],
                'habitaciones' => [['n'=>'Bungalow', 'p'=>310], ['n'=>'Doble', 'p'=>199], ['n'=>'Familiar', 'p'=>288]]
            ],

            // --- PIURA ---
            [
                'servicio' => ['nombre' => 'Arennas Máncora', 'ciudad' => 'Piura', 'pais' => 'Perú', 'lat' => -4.1077, 'lon' => -81.0483, 'img' => 'https://www.crehotel.pe/wp-content/uploads/2022/07/Foto-Arennas-Mancora-4.jpg', 'desc' => 'Relax total frente al mar.'],
                'hotel' => ['estrellas' => 5],
                'habitaciones' => [['n'=>'Garden View', 'p'=>250], ['n'=>'Ocean Front', 'p'=>400]]
            ],
            [
                'servicio' => ['nombre' => 'Hotel Casa Andina Piura', 'ciudad' => 'Piura', 'pais' => 'Perú', 'lat' => -5.194490, 'lon' => -80.632820, 'img' => 'https://s3.us-east-1.amazonaws.com/ca-webprod/Hoteles/banner-landing-casa-andina-premium-piura.webp', 'desc' => 'Hotel cómodo en el centro de Piura.'],
                'hotel' => ['estrellas' => 5],
                'habitaciones' => [['n'=>'Bungalow', 'p'=>310], ['n'=>'Doble', 'p'=>199], ['n'=>'Familiar', 'p'=>288]]
            ],
            [
                'servicio' => ['nombre' => 'Wyndham Costa del Sol Piura', 'ciudad' => 'Piura', 'pais' => 'Perú', 'lat' => -5.193789, 'lon' => -80.629950, 'img' => 'https://images.trvl-media.com/lodging/6000000/5110000/5103900/5103882/b4868d6c.jpg?impolicy=resizecrop&rw=575&rh=575&ra=fill', 'desc' => 'Hotel moderno con piscina y restaurante.'],
                'hotel' => ['estrellas' => 2],
                'habitaciones' => [['n'=>'Bungalow', 'p'=>310], ['n'=>'Doble', 'p'=>199], ['n'=>'Familiar', 'p'=>288]]
            ]
        ];

        foreach ($hotelesConfig as $cfg) {
            $hotel = Hotel::factory()->create([
                'estrellas' => $cfg['hotel']['estrellas'],
                'direccion' => $cfg['servicio']['dir'] ?? ('Centro histórico de ' . $cfg['servicio']['ciudad']),
            ]);
            $hotel->servicio->update([
                'proveedor_id' => $proveedor->id,
                'nombre'       => $cfg['servicio']['nombre'],
                'tipo'         => 'hotel',
                'ciudad'       => $cfg['servicio']['ciudad'],
                'pais'         => $cfg['servicio']['pais'],
                'descripcion'  => $cfg['servicio']['desc'],
                'imagen_url'   => $cfg['servicio']['img'],
                'latitud'      => $cfg['servicio']['lat'],
                'longitud'     => $cfg['servicio']['lon'],
                'activo'       => true
            ]);

            \App\Models\ServicioImagen::factory()
                ->count(4) // 4 fotos extra para la galería
                ->hotel()  // FORZAMOS fotos de hoteles
                ->create([
                    'servicio_id' => $hotel->servicio_id
            ]);

            // 3) Habitaciones + reservas de muestra
            $habitacionesCreadas = []; 

            foreach ($cfg['habitaciones'] as $hab) {
                $habitacion = Habitacion::factory()->create([
                    'servicio_id'       => $hotel->servicio_id,
                    'nombre'            => $hab['n'],
                    'precio_por_noche'  => $hab['p'],
                    'cantidad'          => 10,
                    'capacidad_adultos' => 2,
                    'capacidad_ninos'   => 1,
                    'descripcion'       => 'Habitación con todas las comodidades.'
                ]);
                $habitacionesCreadas[] = $habitacion;
                // Reservas aleatorias
                ReservaHabitacion::factory()->count(2)->create([
                    'habitacion_id' => $habitacion->id,
                    'usuario_id'    => $otrosViajeros->random()->id,
                ]);
            }

            $habitacionesParaEdwin = collect($habitacionesCreadas)->take(2);

            foreach ($habitacionesParaEdwin as $habEdwin) {
                ReservaHabitacion::factory()->create([
                    'habitacion_id' => $habEdwin->id,
                    'usuario_id'    => $viajeroPrincipal->id, 
                    'fecha_inicio'  => now()->subDays(15)->toDateString(),
                    'fecha_fin'     => now()->subDays(10)->toDateString(),
                    'estado'        => 'confirmada',
                    'cantidad'      => 1
                ]);
            }
        }

        // ---------------------------------------------------------
        // Generar TOURS (MEGA PACK DE DATOS)
        // ---------------------------------------------------------
        $toursConfig = [
            // =====================================================
            // AREQUIPA (La Ciudad Blanca)
            // =====================================================
            [
                'servicio' => [
                    'nombre'      => 'City Tour Arequipa & Monasterio',
                    'ciudad'      => 'Arequipa',
                    'pais'        => 'Perú',
                    'descripcion' => 'Recorrido por el centro histórico, miradores y Santa Catalina.',
                    'imagen_url'  => 'https://llamitastravel.com/wp-content/uploads/2024/02/arequipa-mosterio-santa-catalina.jpg',
                    'latitud'     => -16.398803,
                    'longitud'    => -71.536883,
                ],
                'tour' => ['categoria' => 'Cultura', 'duracion' => 240, 'precio' => 90.00],
                'items' => [
                    ['nombre' => 'Monasterio Santa Catalina', 'icono' => '⛪'],
                    ['nombre' => 'Mirador de Yanahuara', 'icono' => '🌋'],
                    ['nombre' => 'Mundo Alpaca', 'icono' => '🦙']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Ruta del Sillar',
                    'ciudad'      => 'Arequipa',
                    'pais'        => 'Perú',
                    'descripcion' => 'Descubre las canteras donde nace la ciudad blanca.',
                    'imagen_url'  => 'https://www.arequipa.com/wp-content/uploads/2020/08/Canteras-de-A%C3%B1ashuayco-Arequipa.jpg.jpg',
                    'latitud'     => -16.350000,
                    'longitud'    => -71.600000,
                ],
                'tour' => ['categoria' => 'Cultura', 'duracion' => 240, 'precio' => 50.00],
                'items' => [
                    ['nombre' => 'Canteras de Añashuayco', 'icono' => '⛏️'],
                    ['nombre' => 'Tallado en vivo', 'icono' => '🗿'],
                    ['nombre' => 'Cañón de Culebrillas', 'icono' => '🏜️']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Rafting en el Río Chili',
                    'ciudad'      => 'Arequipa',
                    'pais'        => 'Perú',
                    'descripcion' => 'Adrenalina pura a solo 20 minutos de la plaza.',
                    'imagen_url'  => 'https://media-cdn.tripadvisor.com/media/attractions-splice-spp-674x446/06/73/ed/8f.jpg',
                    'latitud'     => -16.380000,
                    'longitud'    => -71.540000,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 180, 'precio' => 95.00],
                'items' => [
                    ['nombre' => 'Descenso de rápidos II y III', 'icono' => '🚣'],
                    ['nombre' => 'Equipamiento completo', 'icono' => '🦺'],
                    ['nombre' => 'Snack incluido', 'icono' => '🍫']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Tour Cañón del Colca Full Day',
                    'ciudad'      => 'Arequipa',
                    'pais'        => 'Perú',
                    'descripcion' => 'Visita uno de los cañones más profundos del mundo.',
                    'imagen_url'  => 'https://elcomercio.pe/resizer/r9hqOnoLqmKSDvS7Qzh7ORrdWAY=/1200x715/smart/filters:format(jpeg):quality(75)/cloudfront-us-east-1.images.arcpublishing.com/elcomercio/W7NDWTQIO5GIDEHCBM2YLCZAFQ.jpg',
                    'latitud'     => -15.622755,
                    'longitud'    => -71.964438,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 840, 'precio' => 120.00],
                'items' => [
                    ['nombre' => 'Vuelo del Cóndor', 'icono' => '🦅'],
                    ['nombre' => 'Desayuno en Chivay', 'icono' => '☕'],
                    ['nombre' => 'Mirador de los Volcanes', 'icono' => '🗻']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Ruta Gastronómica: Picanterías',
                    'ciudad'      => 'Arequipa',
                    'pais'        => 'Perú',
                    'descripcion' => 'Prueba el Rocoto Relleno y el Adobo en su origen.',
                    'imagen_url'  => 'https://portal.andina.pe/EDPfotografia3/Thumbnail/2021/08/28/000802661M.webp',
                    'latitud'     => -16.410000,
                    'longitud'    => -71.550000,
                ],
                'tour' => ['categoria' => 'Gastronomía', 'duracion' => 180, 'precio' => 110.00],
                'items' => [
                    ['nombre' => 'Visita a Picantería Tradicional', 'icono' => '🍲'],
                    ['nombre' => 'Clase de cocina participativa', 'icono' => '👨‍🍳'],
                    ['nombre' => 'Degustación de Queso Helado', 'icono' => '🍨']
                ]
            ],

            // =====================================================
            // LIMA (La Capital Gastronómica)
            // =====================================================
            [
                'servicio' => [
                    'nombre'      => 'City Tour Lima Colonial y Moderna',
                    'ciudad'      => 'Lima',
                    'pais'        => 'Perú',
                    'descripcion' => 'Recorrido panorámico por la historia de Lima.',
                    'imagen_url'  => 'https://media-cdn.tripadvisor.com/media/attractions-splice-spp-674x446/0a/74/de/e3.jpg',
                    'latitud'     => -12.046374,
                    'longitud'    => -77.042793,
                ],
                'tour' => ['categoria' => 'Cultura', 'duracion' => 210, 'precio' => 70.00],
                'items' => [
                    ['nombre' => 'Catacumbas San Francisco', 'icono' => '💀'],
                    ['nombre' => 'Plaza de Armas', 'icono' => '🏛️'],
                    ['nombre' => 'Parque del Amor', 'icono' => '❤️']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Tour Gastronómico de Mercado y Ceviche',
                    'ciudad'      => 'Lima',
                    'pais'        => 'Perú',
                    'descripcion' => 'Compra los ingredientes y aprende a preparar el mejor ceviche.',
                    'imagen_url'  => 'https://tripgo.com.pe/wp-content/uploads/2024/11/tour-gastronomico-tripgo-ceviche.webp',
                    'latitud'     => -12.126374,
                    'longitud'    => -77.022793,
                ],
                'tour' => ['categoria' => 'Gastronomía', 'duracion' => 240, 'precio' => 160.00],
                'items' => [
                    ['nombre' => 'Mercado de Surquillo', 'icono' => '🛒'],
                    ['nombre' => 'Clase de cocina profesional', 'icono' => '🔪'],
                    ['nombre' => 'Pisco Sour ilimitado', 'icono' => '🍸']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Cena Show y Caballo de Paso',
                    'ciudad'      => 'Lima',
                    'pais'        => 'Perú',
                    'descripcion' => 'Noche mágica con show de danzas típicas y caballos.',
                    'imagen_url'  => 'https://media-cdn.tripadvisor.com/media/attractions-splice-spp-674x446/06/6f/52/fd.jpg',
                    'latitud'     => -12.230000,
                    'longitud'    => -76.920000,
                ],
                'tour' => ['categoria' => 'Gastronomía', 'duracion' => 240, 'precio' => 190.00],
                'items' => [
                    ['nombre' => 'Buffet Criollo', 'icono' => '🍛'],
                    ['nombre' => 'Show de Caballos', 'icono' => '🐎'],
                    ['nombre' => 'Danzas Típicas', 'icono' => '💃']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Parapente Costa Verde',
                    'ciudad'      => 'Lima',
                    'pais'        => 'Perú',
                    'descripcion' => 'Vuela sobre el mar y los edificios de Miraflores.',
                    'imagen_url'  => 'https://360explora.com.pe/wp-content/uploads/2020/06/imagen-destaca-parapente-en-miraflores1-360explora.jpg',
                    'latitud'     => -12.136374,
                    'longitud'    => -77.032793,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 60, 'precio' => 260.00],
                'items' => [
                    ['nombre' => 'Vuelo con instructor', 'icono' => '🪂'],
                    ['nombre' => 'Video HD incluido', 'icono' => '📹'],
                    ['nombre' => 'Traslados', 'icono' => '🚐']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Nado con Lobos Marinos (Palomino)',
                    'ciudad'      => 'Lima',
                    'pais'        => 'Perú',
                    'descripcion' => 'Navega hacia el Callao y nada con la naturaleza.',
                    'imagen_url'  => 'https://www.condorxtreme.com/wp-content/uploads/2024/04/Nadar-con-Lobos-Marinos-en-Las-Islas-Palomino-3.jpg',
                    'latitud'     => -12.066374,
                    'longitud'    => -77.152793,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 240, 'precio' => 140.00],
                'items' => [
                    ['nombre' => 'Paseo en Yate', 'icono' => '🚤'],
                    ['nombre' => 'Traje de Neopreno', 'icono' => '🏊'],
                    ['nombre' => 'Avistamiento de fauna', 'icono' => '🐧']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Circuito Mágico del Agua',
                    'ciudad'      => 'Lima',
                    'pais'        => 'Perú',
                    'descripcion' => 'Espectáculo de luces y fuentes de agua.',
                    'imagen_url'  => 'https://www.libertrekperutravel.com/wp-content/uploads/2023/10/parque-de-la-reserva-circuito-magico-del-agua.jpg',
                    'latitud'     => -12.070000,
                    'longitud'    => -77.035000,
                ],
                'tour' => ['categoria' => 'Relajación', 'duracion' => 120, 'precio' => 45.00],
                'items' => [
                    ['nombre' => 'Show Multimedia', 'icono' => '⛲'],
                    ['nombre' => 'Túnel de las Sorpresas', 'icono' => '✨'],
                    ['nombre' => 'Guía turístico', 'icono' => '🗣️']
                ]
            ],

            // =====================================================
            // CUSCO (El Ombligo del Mundo)
            // =====================================================
            [
                'servicio' => [
                    'nombre'      => 'Machu Picchu Full Day',
                    'ciudad'      => 'Cusco',
                    'pais'        => 'Perú',
                    'descripcion' => 'La maravilla del mundo en un día inolvidable.',
                    'imagen_url'  => 'https://cdn.getyourguide.com/img/location/5c88e7336b94c.jpeg/88.jpg',
                    'latitud'     => -13.163100,
                    'longitud'    => -72.545000,
                ],
                'tour' => ['categoria' => 'Cultura', 'duracion' => 840, 'precio' => 950.00],
                'items' => [
                    ['nombre' => 'Tren Vistadome', 'icono' => '🚆'],
                    ['nombre' => 'Entradas a la Ciudadela', 'icono' => '🎫'],
                    ['nombre' => 'Guía privado', 'icono' => '👨‍🏫']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Montaña de 7 Colores (Vinicunca)',
                    'ciudad'      => 'Cusco',
                    'pais'        => 'Perú',
                    'descripcion' => 'Trekking desafiante hacia la montaña arcoíris.',
                    'imagen_url'  => 'https://www.incatrilogytours.com/wp-content/uploads/2023/12/1-1.jpg',
                    'latitud'     => -13.869444,
                    'longitud'    => -71.302778,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 720, 'precio' => 110.00],
                'items' => [
                    ['nombre' => 'Bastones de trekking', 'icono' => '🦯'],
                    ['nombre' => 'Desayuno y Almuerzo', 'icono' => '🍲'],
                    ['nombre' => 'Oxígeno de emergencia', 'icono' => '💨']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Laguna Humantay',
                    'ciudad'      => 'Cusco',
                    'pais'        => 'Perú',
                    'descripcion' => 'Camina hacia la laguna turquesa al pie del nevado.',
                    'imagen_url'  => 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/17/ba/55/1b/img-20190524-135656-largejpg.jpg?w=900&h=500&s=1',
                    'latitud'     => -13.419000,
                    'longitud'    => -72.570000,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 720, 'precio' => 120.00],
                'items' => [
                    ['nombre' => 'Caminata de altura', 'icono' => '🏔️'],
                    ['nombre' => 'Almuerzo buffet', 'icono' => '🍽️'],
                    ['nombre' => 'Fotos increíbles', 'icono' => '📸']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'ChocoMuseo: Taller de Chocolate',
                    'ciudad'      => 'Cusco',
                    'pais'        => 'Perú',
                    'descripcion' => 'Prepara tu propio chocolate desde el grano.',
                    'imagen_url'  => 'https://chocomuseo.com/wp-content/uploads/2019/09/locales-cusco-chocomuseo.jpg',
                    'latitud'     => -13.517000,
                    'longitud'    => -71.979000,
                ],
                'tour' => ['categoria' => 'Gastronomía', 'duracion' => 120, 'precio' => 75.00],
                'items' => [
                    ['nombre' => 'Tostado de Cacao', 'icono' => '🍫'],
                    ['nombre' => 'Preparación de bombones', 'icono' => '🍬'],
                    ['nombre' => 'Degustación', 'icono' => '😋']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Baños Termales de Cocalmayo',
                    'ciudad'      => 'Cusco',
                    'pais'        => 'Perú',
                    'descripcion' => 'Relájate en aguas cristalinas en la selva cusqueña.',
                    'imagen_url'  => 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0d/ac/0b/10/banos-termales-de-cocalmayo.jpg?w=1200&h=-1&s=1',
                    'latitud'     => -13.123400,
                    'longitud'    => -72.654300,
                ],
                'tour' => ['categoria' => 'Relajación', 'duracion' => 300, 'precio' => 90.00],
                'items' => [
                    ['nombre' => 'Piscinas naturales', 'icono' => '🛁'],
                    ['nombre' => 'Entorno selvático', 'icono' => '🌿'],
                    ['nombre' => 'Traslado privado', 'icono' => '🚐']
                ]
            ],

            // =====================================================
            // PUNO (Capital del Folklore)
            // =====================================================
            [
                'servicio' => [
                    'nombre'      => 'Islas Uros y Taquile Full Day',
                    'ciudad'      => 'Puno',
                    'pais'        => 'Perú',
                    'descripcion' => 'Navega por el Titicaca y conoce culturas vivas.',
                    'imagen_url'  => 'https://www.tayratourscusco.com/wp-content/uploads/2019/12/uros-taquile-amantani-islands-titicaca-puno-hostal-optimized.jpg',
                    'latitud'     => -15.842184,
                    'longitud'    => -70.020071,
                ],
                'tour' => ['categoria' => 'Cultura', 'duracion' => 480, 'precio' => 110.00],
                'items' => [
                    ['nombre' => 'Paseo en balsa de totora', 'icono' => '🌾'],
                    ['nombre' => 'Almuerzo en Taquile', 'icono' => '🥣'],
                    ['nombre' => 'Danzas locales', 'icono' => '🎶']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Kayak en el Lago Titicaca',
                    'ciudad'      => 'Puno',
                    'pais'        => 'Perú',
                    'descripcion' => 'Rema en el lago navegable más alto del mundo.',
                    'imagen_url'  => 'https://incalake.com/galeria/admin/short-slider/PUNO/KAYAK/kayaktopuno.webp',
                    'latitud'     => -15.840000,
                    'longitud'    => -70.020000,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 180, 'precio' => 85.00],
                'items' => [
                    ['nombre' => 'Equipo de Kayak', 'icono' => '🛶'],
                    ['nombre' => 'Guía instructor', 'icono' => '🆘'],
                    ['nombre' => 'Visita a Uros', 'icono' => '🏝️']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Chullpas de Sillustani',
                    'ciudad'      => 'Puno',
                    'pais'        => 'Perú',
                    'descripcion' => 'Necrópolis pre-inca con vistas a la laguna Umayo.',
                    'imagen_url'  => 'https://www.peru.travel/Contenido/Atractivo/Imagen/es/19/1.2/InformacionGeneral/Vista%20arquitectonica.jpg',
                    'latitud'     => -15.720000,
                    'longitud'    => -70.150000,
                ],
                'tour' => ['categoria' => 'Cultura', 'duracion' => 240, 'precio' => 60.00],
                'items' => [
                    ['nombre' => 'Tumbas Reales', 'icono' => '⚱️'],
                    ['nombre' => 'Laguna Umayo', 'icono' => '🌅'],
                    ['nombre' => 'Visita casa rural', 'icono' => '🏠']
                ]
            ],

            // =====================================================
            // PIURA (Eterno Calor)
            // =====================================================
            [
                'servicio' => [
                    'nombre'      => 'Nado con Tortugas en El Ñuro',
                    'ciudad'      => 'Piura',
                    'pais'        => 'Perú',
                    'descripcion' => 'Nada junto a tortugas verdes gigantes en su hábitat.',
                    'imagen_url'  => 'https://blog.viajesmachupicchu.travel/wp-content/uploads/2025/06/playa-el-nuro-talara-tortugas-grupo-1024x576.jpg',
                    'latitud'     => -4.215000,
                    'longitud'    => -81.168000,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 180, 'precio' => 75.00],
                'items' => [
                    ['nombre' => 'Chalecos salvavidas', 'icono' => '🦺'],
                    ['nombre' => 'Fotos acuáticas', 'icono' => '📸'],
                    ['nombre' => 'Visita al muelle', 'icono' => '⚓']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Relax Total en Máncora',
                    'ciudad'      => 'Piura',
                    'pais'        => 'Perú',
                    'descripcion' => 'Día de spa, yoga y atardecer frente al mar.',
                    'imagen_url'  => 'https://www.raptravelperu.com/wp-content/uploads/portada-mancora.webp',
                    'latitud'     => -4.107778,
                    'longitud'    => -81.048333,
                ],
                'tour' => ['categoria' => 'Relajación', 'duracion' => 300, 'precio' => 150.00],
                'items' => [
                    ['nombre' => 'Masaje relajante', 'icono' => '💆'],
                    ['nombre' => 'Clase de Yoga', 'icono' => '🧘'],
                    ['nombre' => 'Cóctel al atardecer', 'icono' => '🍹']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Tour Manglares de Vice',
                    'ciudad'      => 'Piura',
                    'pais'        => 'Perú',
                    'descripcion' => 'Avistamiento de aves y naturaleza en Sechura.',
                    'imagen_url'  => 'https://www.ytuqueplanes.com/imagenes//fotos/banners/meta-Manglares-San-Pedro-de-Vice.webp',
                    'latitud'     => -5.550000,
                    'longitud'    => -80.800000,
                ],
                'tour' => ['categoria' => 'Aventura', 'duracion' => 360, 'precio' => 85.00],
                'items' => [
                    ['nombre' => 'Paseo en bote', 'icono' => '🚣'],
                    ['nombre' => 'Avistamiento de flamencos', 'icono' => '🦩'],
                    ['nombre' => 'Guía naturalista', 'icono' => '🔭']
                ]
            ],
            [
                'servicio' => [
                    'nombre'      => 'Ruta Gastronómica Norteña',
                    'ciudad'      => 'Piura',
                    'pais'        => 'Perú',
                    'descripcion' => 'Ceviche, Seco de Chavelo y Chicha de Jora.',
                    'imagen_url'  => 'https://www.ytuqueplanes.com/imagenes/fotos/novedades/comida-nortena-majado-de-yuca.jpg',
                    'latitud'     => -5.194490,
                    'longitud'    => -80.632820,
                ],
                'tour' => ['categoria' => 'Gastronomía', 'duracion' => 180, 'precio' => 90.00],
                'items' => [
                    ['nombre' => 'Cata de Chicha', 'icono' => '🏺'],
                    ['nombre' => 'Clase de cocina', 'icono' => '👨‍🍳'],
                    ['nombre' => 'Almuerzo típico', 'icono' => '🍛']
                ]
            ]
        ];

        $tours = [];
        foreach ($toursConfig as $cfg) {

            // 1) Crear el Servicio
            $servicio = Servicio::create([
                'proveedor_id' => $proveedor->id,
                'nombre'       => $cfg['servicio']['nombre'],
                'tipo'         => 'tour',
                'ciudad'       => $cfg['servicio']['ciudad'],
                'pais'         => $cfg['servicio']['pais'],
                'descripcion'  => $cfg['servicio']['descripcion'],
                'imagen_url'   => $cfg['servicio']['imagen_url'],
                'latitud'      => $cfg['servicio']['latitud'],
                'longitud'     => $cfg['servicio']['longitud'],
                'activo'       => true
            ]);

            \App\Models\ServicioImagen::factory()
                ->count(4) // 4 fotos de galería
                ->tour()   // FORZAMOS fotos de tours
                ->create([
                    'servicio_id' => $servicio->id
            ]);

            // 2) Crear el Tour
            $tour = Tour::create([
                'servicio_id' => $servicio->id,
                'categoria'   => $cfg['tour']['categoria'],
                'duracion'    => $cfg['tour']['duracion'],
                'precio'      => $cfg['tour']['precio']
            ]);

            $tours[] = $tour;

            // 3) Crear los items del tour (según tu migración REAL)
            foreach ($cfg['items'] as $item) {
                TourItem::create([
                    'servicio_id' => $servicio->id,
                    'nombre'      => $item['nombre'],  // campo correcto
                    'icono'       => $item['icono'] ?? null,
                ]);
            }
        }


        // ---------------------------------------------------------
        // Generar salidas, actividades y reservas para cada tour
        // ---------------------------------------------------------
        foreach ($tours as $tour) {
            $servicioId = $tour->servicio_id;
            $capacidad  = $tour->capacidad_por_salida ?? 20;

            // Evitar duplicados si el tour ya tiene salidas
            if (TourSalida::where('servicio_id', $servicioId)->exists()) {
                continue;
            }

            // Crear 2-3 salidas separadas por semanas
            $numSalidas = rand(2,3);
            $base = Carbon::now()->addDays(3);

            $salidas = [];
            for ($s = 0; $s < $numSalidas; $s++) {
                $fecha = $base->copy()->addDays($s*7); // separación semanal
                $hora  = $fecha->copy()->setTime(rand(8, 15), [0,30][rand(0,1)]);

                $salida = TourSalida::create([
                    'servicio_id'    => $servicioId,
                    'fecha'          => $fecha->format('Y-m-d'),
                    'hora'           => $hora->format('H:i:00'),
                    'cupo_total'     => $capacidad,
                    'cupo_reservado' => 0,
                    'estado'         => 'programada',
                ]);

                $salidas[] = $salida;
            }
            // Crear 4 actividades con orden
            for ($i = 1; $i <= 4; $i++) {
                TourActividad::factory()
                    ->orden($i)
                    ->forServicio($servicioId)
                    ->create();
            }

            // Crear reservas para cada salida
            foreach ($salidas as $salida) {
                // Crear 2-3 reservas normales
                ReservaTour::factory()->count(rand(2,3))->create([
                    'salida_id'  => $salida->id,
                    'usuario_id' => $otrosViajeros->random()->id,
                ]);
            }
        }

        // Crear 3 reservas fijas para el usuario 2 en salidas de tours al azar
        $salidasDisponibles = TourSalida::all()->shuffle()->take(3);
        foreach ($salidasDisponibles as $salida) {
            ReservaTour::factory()->confirmada()->create([
                'salida_id'  => $salida->id,
                'usuario_id' => $viajeroPrincipal->id,
            ]);
        }

        // ------- Crear reservas pasadas -------------
        // Reservas pasadas para habitaciones
        $habitaciones = Habitacion::all();
        if ($habitaciones->isNotEmpty()) {
            foreach ($habitaciones->take(5) as $habitacion) { // Crear 5 reservas pasadas
                ReservaHabitacion::factory()->pasado()->create([
                    'habitacion_id' => $habitacion->id,
                    'usuario_id'    => $otrosViajeros->random()->id,
                ]);
            }
        }

        // Reservas pasadas para tours
        $tours = Tour::all();
        if ($tours->isNotEmpty()) {
            foreach ($tours->take(3) as $tour) { // Crear salidas pasadas para 3 tours
                $salidaPasada = TourSalida::factory()->pasado()->create([
                    'servicio_id' => $tour->servicio_id,
                ]);

                // Crear algunas reservas para esta salida pasada
                ReservaTour::factory()->count(rand(1,3))->create([
                    'salida_id'  => $salidaPasada->id,
                    'usuario_id' => $otrosViajeros->random()->id,
                ]);
            }
        }

        // ------- Reviews (una por usuario y servicio) -------------
        $viajeroIds = Usuario::where('rol', 'viajero')->pluck('id');

        Servicio::all()->each(function ($servicio) use ($viajeroIds) {
            $autores = $viajeroIds->shuffle()->take(rand(0, min(5, $viajeroIds->count())));
            foreach ($autores as $uid) {
                Review::factory()->create([
                    'servicio_id' => $servicio->id,
                    'usuario_id'  => $uid,
                ]);
            }
        });

        // ------- Datos fijos para pruebas automatizadas -------------
        $this->call(QaSeeder::class);
    }
}
