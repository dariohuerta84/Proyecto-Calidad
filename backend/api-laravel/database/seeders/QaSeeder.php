<?php

namespace Database\Seeders;

use App\Models\Habitacion;
use App\Models\Hotel;
use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\Servicio;
use App\Models\Tour;
use App\Models\TourActividad;
use App\Models\TourSalida;
use App\Models\Usuario;
use App\Support\Precio;
use Illuminate\Database\Seeder;
use Illuminate\Support\Str;

/**
 * Datos deterministas para las suites automatizadas (Karate, JMeter, Selenium, Appium).
 * Ver docs/DATOS_DE_PRUEBA.md. Todos los correos terminan en @trouvels360.test.
 */
class QaSeeder extends Seeder
{
    public const PASS_VIAJERO = 'Viajero123';
    public const PASS_PROVEEDOR = 'Proveedor123';

    public function run(): void
    {
        $proveedor = Usuario::where('email', 'edwinproveedor@gmail.com')->firstOrFail();

        // ---- Proveedor secundario (pruebas de autorización 403)
        $proveedor2 = Usuario::updateOrCreate(['email' => 'qa.proveedor2@trouvels360.test'], [
            'rol' => 'proveedor', 'empresa_nombre' => 'QA Turismo SAC', 'telefono' => '+51 912345678',
            'ruc' => '20999999991', 'password' => self::PASS_PROVEEDOR,
        ]);

        // ---- 30 viajeros (CSV de JMeter y pruebas independientes)
        $viajeros = collect(range(1, 30))->map(fn ($n) => Usuario::updateOrCreate(
            ['email' => "qa.viajero{$n}@trouvels360.test"],
            ['rol' => 'viajero', 'nombre' => 'QA', 'apellido' => "Viajero {$n}", 'password' => self::PASS_VIAJERO]
        ));

        // ---- Hotel QA (Cusco) del proveedor principal
        $hotelQa = $this->hotel($proveedor, 'Hotel QA Concurrencia', 'Cusco', -13.5163, -71.9785, 4, [
            ['nombre' => 'Habitación Única', 'capacidad_adultos' => 2, 'capacidad_ninos' => 1, 'cantidad' => 1, 'precio_por_noche' => 100.00],
            ['nombre' => 'Doble QA', 'capacidad_adultos' => 2, 'capacidad_ninos' => 1, 'cantidad' => 5, 'precio_por_noche' => 150.00],
            ['nombre' => 'Suite QA', 'capacidad_adultos' => 4, 'capacidad_ninos' => 2, 'cantidad' => 2, 'precio_por_noche' => 320.00],
        ]);

        // ---- Hotel del proveedor 2 (Lima)
        $this->hotel($proveedor2, 'Hotel QA Proveedor Dos', 'Lima', -12.1211, -77.0297, 3, [
            ['nombre' => 'Estándar', 'capacidad_adultos' => 2, 'capacidad_ninos' => 0, 'cantidad' => 3, 'precio_por_noche' => 90.00],
        ]);

        // ---- Tour QA (Cusco, Aventura, S/ 120.00)
        $tourQa = Servicio::updateOrCreate(['nombre' => 'Tour QA Cupos', 'proveedor_id' => $proveedor->id], [
            'tipo' => 'tour', 'ciudad' => 'Cusco', 'pais' => 'Perú', 'activo' => true,
            'descripcion' => 'Tour de prueba con cupos controlados para pruebas de concurrencia.',
            'imagen_url' => 'https://wp.tour7colores.com/wp-content/uploads/2025/05/turistas-montana-colores-vinicunca-sm.jpg',
            'latitud' => -13.5226, 'longitud' => -71.9673,
        ]);
        Tour::updateOrCreate(['servicio_id' => $tourQa->id], ['categoria' => 'Aventura', 'duracion' => 240, 'precio' => 120.00]);
        if (!TourActividad::where('servicio_id', $tourQa->id)->exists()) {
            foreach (['Recojo en hotel', 'Caminata guiada', 'Almuerzo campestre'] as $i => $t) {
                TourActividad::create(['servicio_id' => $tourQa->id, 'titulo' => $t, 'orden' => $i + 1, 'duracion_min' => 60 + 20 * $i]);
            }
        }
        $salidaAbierta = TourSalida::firstOrCreate(
            ['servicio_id' => $tourQa->id, 'fecha' => now()->addDays(10)->toDateString(), 'hora' => '09:00:00'],
            ['cupo_total' => 10, 'cupo_reservado' => 0, 'estado' => 'programada']
        );
        TourSalida::firstOrCreate(
            ['servicio_id' => $tourQa->id, 'fecha' => now()->addDays(12)->toDateString(), 'hora' => '09:00:00'],
            ['cupo_total' => 10, 'cupo_reservado' => 0, 'estado' => 'cerrada']
        );
        TourSalida::firstOrCreate(
            ['servicio_id' => $tourQa->id, 'fecha' => now()->addDays(14)->toDateString(), 'hora' => '15:00:00'],
            ['cupo_total' => 30, 'cupo_reservado' => 0, 'estado' => 'programada']
        );
        $salidaPasada = TourSalida::firstOrCreate(
            ['servicio_id' => $tourQa->id, 'fecha' => now()->subDays(5)->toDateString(), 'hora' => '09:00:00'],
            ['cupo_total' => 10, 'cupo_reservado' => 0, 'estado' => 'programada']
        );

        // ---- Historial que habilita reseñas (qa.viajero1): estadía y tour ya finalizados
        $v1 = $viajeros[0];
        $doble = Habitacion::where('servicio_id', $hotelQa->servicio_id)->where('nombre', 'Doble QA')->first();
        $this->reservaHotel($v1, $doble, now()->subDays(10), now()->subDays(7), 1, 'confirmada');
        if (!ReservaTour::where('usuario_id', $v1->id)->where('salida_id', $salidaPasada->id)->exists()) {
            $t = Precio::desdeSubtotalCentimos(Precio::lineaCentimos(120, 2));
            ReservaTour::create([
                'codigo_reserva' => 'TQA' . strtoupper(Str::random(7)), 'usuario_id' => $v1->id, 'salida_id' => $salidaPasada->id,
                'personas' => 2, 'estado' => 'confirmada', 'precio_unitario' => 120, 'tarjeta_ultimos4' => '4242',
            ] + $t);
            $salidaPasada->increment('cupo_reservado', 2);
        }

        // ---- Solicitudes PENDIENTES para la demo del flujo de aprobación (viajero principal)
        $edwin = Usuario::where('email', 'edwinviajero@gmail.com')->first();
        if ($edwin) {
            $this->reservaHotel($edwin, $doble, now()->addDays(20), now()->addDays(22), 1, 'pendiente');
            $this->reservaHotel($edwin, $doble, now()->addDays(25), now()->addDays(27), 2, 'pendiente');
        }
    }

    private function hotel(Usuario $prov, string $nombre, string $ciudad, float $lat, float $lon, int $estrellas, array $habs): Hotel
    {
        $serv = Servicio::updateOrCreate(['nombre' => $nombre, 'proveedor_id' => $prov->id], [
            'tipo' => 'hotel', 'ciudad' => $ciudad, 'pais' => 'Perú', 'activo' => true,
            'descripcion' => 'Hotel de prueba para las suites automatizadas.',
            'imagen_url' => 'https://media-cdn.tripadvisor.com/media/photo-s/27/59/0e/18/casa-andina-premium-cusco.jpg',
            'latitud' => $lat, 'longitud' => $lon,
        ]);
        $hotel = Hotel::updateOrCreate(['servicio_id' => $serv->id], ['direccion' => "Av. QA 123, {$ciudad}", 'estrellas' => $estrellas]);
        foreach ($habs as $h) {
            Habitacion::updateOrCreate(['servicio_id' => $serv->id, 'nombre' => $h['nombre']], $h + ['descripcion' => 'Habitación de prueba.']);
        }

        return $hotel;
    }

    private function reservaHotel(Usuario $u, Habitacion $h, $in, $out, int $cant, string $estado): void
    {
        $in = $in->toDateString();
        $out = $out->toDateString();
        if (ReservaHabitacion::where('usuario_id', $u->id)->where('habitacion_id', $h->id)->whereDate('fecha_inicio', $in)->exists()) {
            return;
        }
        $noches = \Carbon\Carbon::parse($in)->diffInDays(\Carbon\Carbon::parse($out));
        $t = Precio::desdeSubtotalCentimos(Precio::lineaCentimos($h->precio_por_noche, $cant, (int) $noches));
        ReservaHabitacion::create([
            'codigo_reserva' => 'QA' . strtoupper(Str::random(8)), 'solicitud_codigo' => 'SOL-QA' . strtoupper(Str::random(6)),
            'usuario_id' => $u->id, 'habitacion_id' => $h->id, 'fecha_inicio' => $in, 'fecha_fin' => $out,
            'cantidad' => $cant, 'estado' => $estado, 'precio_por_noche' => $h->precio_por_noche, 'tarjeta_ultimos4' => '4242',
            'decidido_en' => $estado === 'confirmada' ? now() : null,
        ] + $t);
    }
}
