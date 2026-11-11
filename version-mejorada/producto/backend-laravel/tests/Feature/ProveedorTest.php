<?php
// TRV-501, TRV-502, TRV-503, TRV-504, TRV-505, TRV-508
use App\Models\Habitacion;
use App\Models\Servicio;
use App\Models\TourSalida;

it('TRV-508-04 no se puede eliminar la publicación de otro proveedor (403)', function () {
    $ajeno = Servicio::where('nombre', 'Hotel QA Concurrencia')->value('id');
    $this->como('qa.proveedor2@trouvels360.test')->api('DELETE', "servicios/$ajeno")->assertForbidden();
    $this->como('qa.viajero1@trouvels360.test')->api('DELETE', "servicios/$ajeno")->assertForbidden();
    expect(Servicio::find($ajeno))->not->toBeNull();
});

it('TRV-508-04 con reservas activas responde 409', function () {
    $id = Servicio::where('nombre', 'Hotel QA Concurrencia')->value('id');
    $this->como('edwinproveedor@gmail.com')->api('DELETE', "servicios/$id")->assertStatus(409);
});

it('TRV-508-02/03 elimina sin reservas activas y el historial del viajero sigue visible', function () {
    $sid = Servicio::where('nombre', 'Hotel QA Proveedor Dos')->value('id');
    $hab = Habitacion::where('servicio_id', $sid)->first();
    // reserva pasada (historial)
    \App\Models\ReservaHabitacion::create([
        'codigo_reserva' => 'HISTQA0001', 'usuario_id' => \App\Models\Usuario::where('email', 'qa.viajero25@trouvels360.test')->value('id'),
        'habitacion_id' => $hab->id, 'fecha_inicio' => now()->subDays(9), 'fecha_fin' => now()->subDays(8), 'cantidad' => 1,
        'estado' => 'confirmada', 'precio_por_noche' => 90, 'subtotal' => 90, 'impuesto' => 16.2, 'total' => 106.2,
    ]);
    $this->como('qa.proveedor2@trouvels360.test')->api('DELETE', "servicios/$sid")->assertOk();
    $this->api('DELETE', "servicios/$sid")->assertNotFound();
    $this->anonimo()->api('GET', "hoteles/$sid")->assertNotFound();
    $this->como('qa.viajero25@trouvels360.test')->api('GET', 'mis-reservas')->assertOk()
        ->assertJsonPath('data.0.servicio.nombre', 'Hotel QA Proveedor Dos')->assertJsonPath('data.0.servicio.eliminado', true);
});

it('TRV-501 panel paginado con indicadores por estado', function () {
    $this->como('edwinproveedor@gmail.com');
    $r = $this->api('GET', 'proveedor/servicios?per_page=15')->assertOk();
    expect($r->json('meta.total'))->toBeGreaterThan(15);
    $this->api('GET', 'proveedor/servicios?per_page=15&page=2')->assertOk()->assertJsonPath('meta.current_page', 2);
    $r->assertJsonStructure(['data' => [['reservas_totales' => ['pendientes', 'confirmadas', 'rechazadas', 'canceladas'], 'resenas' => ['promedio', 'cantidad']]]]);
    $this->api('GET', 'proveedor/indicadores')->assertOk()
        ->assertJsonStructure(['data' => ['solicitudes_pendientes', 'reservas_hotel' => ['pendiente', 'confirmada', 'rechazada', 'cancelada'], 'ingresos_confirmados_mes', 'resenas']]);
    // Conteos por tipo correctos (tipos de habitación del hotel QA)
    $qa = collect($this->api('GET', 'proveedor/servicios?search=QA%20Concurrencia')->json('data'))->firstWhere('nombre', 'Hotel QA Concurrencia');
    expect($qa['meta_tipo']['habitaciones_count'])->toBe(3);
});

it('TRV-502 publica un hotel con habitaciones; 6 imágenes 422; sin habitaciones 422', function () {
    $this->como('qa.proveedor2@trouvels360.test');
    $base = ['nombre' => 'Hotel Nuevo', 'direccion' => 'Calle 1', 'ciudad' => 'Arequipa', 'pais' => 'Perú', 'estrellas' => 4,
        'imagen_url' => 'https://img.test/h.jpg',
        'habitaciones' => [['nombre' => 'Doble', 'capacidad_adultos' => 2, 'capacidad_ninos' => 1, 'cantidad' => 4, 'precio_por_noche' => 180]]];
    $id = $this->api('POST', 'hoteles', $base)->assertCreated()->json('data.servicio_id');
    $this->anonimo()->api('GET', "hoteles/$id")->assertOk()->assertJsonPath('hotel.nombre', 'Hotel Nuevo')->assertJsonCount(1, 'habitaciones');
    $this->como('qa.proveedor2@trouvels360.test');
    $this->api('POST', 'hoteles', $base + ['imagenes' => array_fill(0, 6, ['url' => 'https://img.test/x.jpg'])])->assertStatus(422);
    $this->api('POST', 'hoteles', array_merge($base, ['habitaciones' => []]))->assertStatus(422);
    $this->como('edwinproveedor@gmail.com')->api('PUT', "hoteles/$id", ['nombre' => 'Hackeado'])->assertForbidden();
});

it('TRV-503 no reduce unidades bajo la ocupación ni elimina con reservas activas', function () {
    $sid = Servicio::where('nombre', 'Hotel QA Concurrencia')->value('id');
    $doble = Habitacion::where('servicio_id', $sid)->where('nombre', 'Doble QA')->first(); // 2 unidades pendientes del seeder en +25
    $this->como('edwinproveedor@gmail.com');
    $this->api('PUT', "habitaciones/{$doble->id}", ['cantidad' => 1])->assertStatus(409)->assertJsonPath('minimo', 2);
    $this->api('PUT', "habitaciones/{$doble->id}", ['cantidad' => 6, 'precio_por_noche' => 170])->assertOk();
    $this->api('DELETE', "habitaciones/{$doble->id}")->assertStatus(409);
    $this->api('PUT', "hoteles/$sid", ['habitaciones' => [['id' => Habitacion::where('servicio_id', $sid)->where('nombre', 'Suite QA')->value('id')]]])->assertStatus(409);
});

it('TRV-504-03 editar el precio de un tour no recrea salidas ni cambia reservas', function () {
    $t = Servicio::where('nombre', 'Tour QA Cupos')->first();
    $ids = TourSalida::where('servicio_id', $t->id)->pluck('id')->sort()->values()->all();
    $this->como('edwinproveedor@gmail.com')->api('PUT', "tours/{$t->id}", ['precio' => 150, 'salidas' => []])->assertOk();
    expect(TourSalida::where('servicio_id', $t->id)->pluck('id')->sort()->values()->all())->toBe($ids);
    $this->como('qa.viajero1@trouvels360.test')->api('GET', 'tours/mis-reservas')->assertJsonPath('data.0.total', 283.2); // 120×2 + 18%
});

it('TRV-504 crea tour con validaciones de límites', function () {
    $this->como('qa.proveedor2@trouvels360.test');
    $base = ['nombre' => 'Kayak', 'descripcion' => 'Kayak en el lago', 'ciudad' => 'Puno', 'pais' => 'Perú', 'categoria' => 'Aventura',
        'duracion' => 120, 'precio' => 80, 'imagen_url' => 'https://img.test/k.jpg',
        'salidas' => [['fecha' => now()->addDays(5)->toDateString(), 'hora' => '08:00', 'cupo_total' => 12]]];
    $this->api('POST', 'tours', $base)->assertCreated()->assertJsonPath('data.salidas.0.cupo_reservado', 0);
    $this->api('POST', 'tours', array_merge($base, ['categoria' => 'Buceo']))->assertStatus(422);
    $this->api('POST', 'tours', $base + ['items' => array_fill(0, 11, ['nombre' => 'x'])])->assertStatus(422);
});

it('TRV-505 actividades sin orden van al final; tour ajeno 403', function () {
    $t = Servicio::where('nombre', 'Tour QA Cupos')->first();
    $this->como('edwinproveedor@gmail.com');
    $a = $this->api('POST', "tours/{$t->id}/actividades", ['titulo' => 'Mirador'])->assertCreated()->json('orden');
    $b = $this->api('POST', "tours/{$t->id}/actividades", ['titulo' => 'Retorno'])->assertCreated()->json('orden');
    expect($b)->toBe($a + 1);
    $this->api('POST', "tours/{$t->id}/actividades", ['titulo' => 'Inicio', 'orden' => 1])->assertCreated()->assertJsonPath('orden', 1);
    $ordenes = collect($this->api('GET', "tours/{$t->id}/actividades")->json())->pluck('orden')->all();
    expect($ordenes)->toBe(range(1, count($ordenes)));
    $this->como('qa.proveedor2@trouvels360.test')->api('POST', "tours/{$t->id}/actividades", ['titulo' => 'X'])->assertForbidden();
});

it('TRV-505 mover una actividad reordena sin huecos ni duplicados', function () {
    $t = Servicio::where('nombre', 'Tour QA Cupos')->first();
    $this->como('edwinproveedor@gmail.com');
    $ids = collect(['A', 'B', 'C'])->map(fn ($x) => $this->api('POST', "tours/{$t->id}/actividades", ['titulo' => "Paso $x"])->json('id'));
    $primera = $this->api('POST', "tours/{$t->id}/actividades", ['titulo' => 'Paso 0', 'orden' => 1])->json('id');
    // bajar la primera una posición
    $this->api('PUT', "tours/{$t->id}/actividades/{$primera}", ['orden' => 2])->assertOk()->assertJsonPath('orden', 2);
    $lista = collect($this->api('GET', "tours/{$t->id}/actividades")->json());
    expect($lista->pluck('orden')->all())->toBe(range(1, $lista->count()));
    expect($lista[1]['id'])->toBe($primera);
    // subir la última al inicio
    $ultima = $lista->last()['id'];
    $this->api('PUT', "tours/{$t->id}/actividades/{$ultima}", ['orden' => 1])->assertOk();
    $lista = collect($this->api('GET', "tours/{$t->id}/actividades")->json());
    expect($lista[0]['id'])->toBe($ultima);
    expect($lista->pluck('orden')->all())->toBe(range(1, $lista->count()));
});
