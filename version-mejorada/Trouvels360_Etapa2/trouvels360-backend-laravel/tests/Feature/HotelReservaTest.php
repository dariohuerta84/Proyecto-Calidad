<?php
// TRV-201, TRV-202, TRV-203
use App\Models\Habitacion;
use App\Models\ReservaHabitacion;
use App\Models\Servicio;

function habQa(string $nombre): Habitacion {
    $sid = Servicio::where('nombre', 'Hotel QA Concurrencia')->value('id');
    return Habitacion::where('servicio_id', $sid)->where('nombre', $nombre)->firstOrFail();
}
function fechas(int $desde = 40, int $noches = 2): array {
    return [now()->addDays($desde)->toDateString(), now()->addDays($desde + $noches)->toDateString()];
}

it('TRV-201-01 la búsqueda devuelve solo hoteles de la ciudad con capacidad y unidades', function () {
    [$in, $out] = fechas();
    $r = $this->api('GET', "hoteles?ciudad=cusco&check_in=$in&check_out=$out&adultos=2&habitaciones=1")->assertOk();
    expect($r->json('data'))->not->toBeEmpty();
    foreach ($r->json('data') as $h) {
        expect(strtolower($h['ciudad']))->toBe('cusco');
        foreach ($h['habitaciones'] as $hab) {
            expect($hab['capacidad_adultos'])->toBeGreaterThanOrEqual(2);
            expect($hab['unidades_disponibles'])->toBeGreaterThanOrEqual(1);
        }
    }
    $r->assertJsonStructure(['data', 'meta' => ['current_page', 'last_page', 'per_page', 'total']]);
});

it('TRV-201-02/03 rechaza fechas pasadas y cantidades fuera de rango', function () {
    $this->api('GET', 'hoteles?check_in=' . now()->subDay()->toDateString() . '&check_out=' . now()->addDay()->toDateString())
        ->assertStatus(422)->assertJsonValidationErrors('check_in');
    [$in, $out] = fechas();
    $this->api('GET', "hoteles?check_in=$out&check_out=$in")->assertStatus(422);
    $this->api('GET', "hoteles?check_in=$in&check_out=$out&adultos=0&habitaciones=11")->assertStatus(422)
        ->assertJsonValidationErrors(['adultos', 'habitaciones']);
});

it('TRV-202-03 cotiza con IGV: 150 × 2 noches × 2 = 600 + 108 = 708', function () {
    [$in, $out] = fechas(40, 2);
    $this->api('POST', 'reservas-habitaciones/cotizar', [
        'fecha_inicio' => $in, 'fecha_fin' => $out, 'items' => [['habitacion_id' => habQa('Doble QA')->id, 'cantidad' => 2]],
    ])->assertOk()
        ->assertJsonPath('data.subtotal', 600)->assertJsonPath('data.impuesto', 108)->assertJsonPath('data.total', 708)
        ->assertJsonPath('data.noches', 2);
});

it('TRV-202-04 cotizar más unidades que las libres responde 422', function () {
    [$in, $out] = fechas();
    $this->api('POST', 'reservas-habitaciones/cotizar', [
        'fecha_inicio' => $in, 'fecha_fin' => $out, 'items' => [['habitacion_id' => habQa('Habitación Única')->id, 'cantidad' => 2]],
    ])->assertStatus(422);
});

it('TRV-203-01 registra la solicitud PENDIENTE con los importes cotizados', function () {
    [$in, $out] = fechas(45, 3);
    $this->como('qa.viajero6@trouvels360.test');
    $r = $this->api('POST', 'reservas-habitaciones', [
        'fecha_inicio' => $in, 'fecha_fin' => $out,
        'items' => [['habitacion_id' => habQa('Doble QA')->id, 'cantidad' => 1], ['habitacion_id' => habQa('Suite QA')->id, 'cantidad' => 1]],
        'pago' => $this->pagoValido(),
    ], $this->idem())->assertCreated()
        ->assertJsonPath('data.estado', 'pendiente')
        ->assertJsonPath('data.subtotal', 1410)   // (150 + 320) × 3
        ->assertJsonPath('data.impuesto', 253.8)
        ->assertJsonPath('data.total', 1663.8)
        ->assertJsonPath('data.tarjeta_ultimos4', '4242');
    expect($r->json('data.reservas'))->toHaveCount(2);
    expect($r->json('data.solicitud_codigo'))->toStartWith('SOL-');
});

it('TRV-203-02 pago inválido no crea reserva', function (array $pago, string $campo) {
    [$in, $out] = fechas();
    $this->como('qa.viajero6@trouvels360.test');
    $antes = ReservaHabitacion::count();
    $this->api('POST', 'reservas-habitaciones', [
        'fecha_inicio' => $in, 'fecha_fin' => $out, 'items' => [['habitacion_id' => habQa('Doble QA')->id, 'cantidad' => 1]],
        'pago' => $pago + $this->pagoValido(),
    ], $this->idem())->assertStatus(422)->assertJsonValidationErrors($campo);
    expect(ReservaHabitacion::count())->toBe($antes);
})->with([
    'luhn inválido' => [['numero' => '4242424242424241'], 'pago.numero'],
    '15 dígitos'    => [['numero' => '424242424242424'], 'pago.numero'],
    'vencida'       => [['vencimiento' => '01/20'], 'pago.vencimiento'],
    'cvc corto'     => [['cvc' => '12'], 'pago.cvc'],
    'titular'       => [['titular' => ''], 'pago.titular'],
]);

it('TRV-203-03 las unidades de una solicitud pendiente dejan de estar disponibles', function () {
    [$in, $out] = fechas(50, 2);
    $hab = habQa('Habitación Única');
    $sid = $hab->servicio_id;
    $this->api('GET', "hoteles/$sid/disponibilidad?check_in=$in&check_out=$out")->assertJsonFragment(['id' => $hab->id]);
    $this->como('qa.viajero7@trouvels360.test');
    $this->api('POST', 'reservas-habitaciones', [
        'fecha_inicio' => $in, 'fecha_fin' => $out, 'items' => [['habitacion_id' => $hab->id, 'cantidad' => 1]], 'pago' => $this->pagoValido(),
    ], $this->idem())->assertCreated();
    $disp = collect($this->api('GET', "hoteles/$sid/disponibilidad?check_in=$in&check_out=$out")->json('data.habitaciones'))->pluck('id');
    expect($disp)->not->toContain($hab->id);
});

it('TRV-203-04 con un tipo sin unidades no se registra ningún tipo (409)', function () {
    [$in, $out] = fechas(55, 2);
    $unica = habQa('Habitación Única');
    $this->como('qa.viajero8@trouvels360.test');
    $this->api('POST', 'reservas-habitaciones', [
        'fecha_inicio' => $in, 'fecha_fin' => $out, 'items' => [['habitacion_id' => $unica->id, 'cantidad' => 1]], 'pago' => $this->pagoValido(),
    ], $this->idem())->assertCreated();
    $antes = ReservaHabitacion::count();
    $this->como('qa.viajero9@trouvels360.test');
    $this->api('POST', 'reservas-habitaciones', [
        'fecha_inicio' => $in, 'fecha_fin' => $out,
        'items' => [['habitacion_id' => habQa('Doble QA')->id, 'cantidad' => 1], ['habitacion_id' => $unica->id, 'cantidad' => 1]],
        'pago' => $this->pagoValido(),
    ], $this->idem())->assertStatus(409)->assertJsonPath('sin_stock.0.habitacion_id', $unica->id);
    expect(ReservaHabitacion::count())->toBe($antes);
});

it('TRV-203-06 el mismo Idempotency-Key devuelve la misma solicitud sin duplicar', function () {
    [$in, $out] = fechas(60, 1);
    $this->como('qa.viajero10@trouvels360.test');
    $body = ['fecha_inicio' => $in, 'fecha_fin' => $out, 'items' => [['habitacion_id' => habQa('Doble QA')->id, 'cantidad' => 1]], 'pago' => $this->pagoValido()];
    $key = $this->idem();
    $a = $this->api('POST', 'reservas-habitaciones', $body, $key)->assertCreated();
    $b = $this->api('POST', 'reservas-habitaciones', $body, $key)->assertOk()->assertHeader('Idempotent-Replayed', 'true');
    expect($b->json('data.solicitud_codigo'))->toBe($a->json('data.solicitud_codigo'));
    expect(ReservaHabitacion::where('solicitud_codigo', $a->json('data.solicitud_codigo'))->count())->toBe(1);
    $this->api('POST', 'reservas-habitaciones', $body)->assertStatus(400); // sin clave
});

it('TRV-203 un proveedor no puede reservar', function () {
    [$in, $out] = fechas();
    $this->como('edwinproveedor@gmail.com');
    $this->api('POST', 'reservas-habitaciones', [
        'fecha_inicio' => $in, 'fecha_fin' => $out, 'items' => [['habitacion_id' => habQa('Doble QA')->id, 'cantidad' => 1]], 'pago' => $this->pagoValido(),
    ], $this->idem())->assertForbidden();
});
