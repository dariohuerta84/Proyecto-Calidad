<?php
// TRV-301, TRV-302, TRV-303, TRV-402, TRV-506
use App\Models\ReservaTour;
use App\Models\Servicio;
use App\Models\TourSalida;

function tourQa(): Servicio { return Servicio::where('nombre', 'Tour QA Cupos')->firstOrFail(); }
function salidaQa(string $estado = 'programada', int $cupo = 10): TourSalida {
    return TourSalida::where('servicio_id', tourQa()->id)->where('estado', $estado)->where('cupo_total', $cupo)
        ->whereDate('fecha', '>', now()->toDateString())->orderBy('fecha')->firstOrFail();
}

it('TRV-301-01 filtra por destino y categoría sin distinguir tildes', function () {
    $r = $this->api('GET', 'tours?destino=CUSCO&categoria=Aventura')->assertOk();
    expect($r->json('data'))->not->toBeEmpty();
    foreach ($r->json('data') as $t) {
        expect($t['ciudad'])->toBe('Cusco');
        expect($t['tour']['categoria'])->toBe('Aventura');
    }
});

it('TRV-301-02 rechaza categoría inexistente y fechas pasadas', function () {
    $this->api('GET', 'tours?categoria=Buceo')->assertStatus(422)->assertJsonValidationErrors('categoria');
    $this->api('GET', 'tours?checkIn=' . now()->subDays(3)->toDateString())->assertStatus(422);
});

it('TRV-302-01 el detalle solo muestra salidas futuras programadas', function () {
    $r = $this->api('GET', 'tours/' . tourQa()->id)->assertOk();
    foreach ($r->json('servicio.salidas') as $s) {
        expect($s['estado'])->toBe('programada');
        expect($s['fecha'] >= now()->toDateString())->toBeTrue();
    }
});

it('TRV-302-02 cotiza 120 × 3 = 360 + 64.80 = 424.80', function () {
    $this->api('POST', 'tours/salidas/' . salidaQa()->id . '/cotizar', ['personas' => 3])->assertOk()
        ->assertJsonPath('data.subtotal', 360)->assertJsonPath('data.impuesto', 64.8)->assertJsonPath('data.total', 424.8);
    $this->api('POST', 'tours/salidas/' . salidaQa()->id . '/cotizar', ['personas' => 11])->assertStatus(422);
});

it('TRV-303-01 reserva confirmada automáticamente y descuenta cupos', function () {
    $s = salidaQa();
    $this->como('qa.viajero18@trouvels360.test');
    $this->api('POST', "tours/salidas/{$s->id}/reservas", ['personas' => 3, 'pago' => $this->pagoValido()], $this->idem())
        ->assertCreated()->assertJsonPath('estado', 'confirmada')->assertJsonPath('total', 424.8)
        ->assertJsonPath('data.servicio.nombre', 'Tour QA Cupos');
    expect($s->fresh()->cupo_reservado)->toBe($s->cupo_reservado + 3);
});

it('TRV-303-03 salida cerrada, pasada o sin cupo no se reserva y los cupos no cambian', function () {
    $this->como('qa.viajero19@trouvels360.test');
    $cerrada = salidaQa('cerrada');
    $this->api('POST', "tours/salidas/{$cerrada->id}/reservas", ['personas' => 1, 'pago' => $this->pagoValido()], $this->idem())->assertStatus(409);
    $pasada = TourSalida::where('servicio_id', tourQa()->id)->whereDate('fecha', '<', now()->toDateString())->first();
    $this->api('POST', "tours/salidas/{$pasada->id}/reservas", ['personas' => 1, 'pago' => $this->pagoValido()], $this->idem())->assertStatus(409);
    $s = salidaQa();
    $this->api('POST', "tours/salidas/{$s->id}/reservas", ['personas' => 11, 'pago' => $this->pagoValido()], $this->idem())->assertStatus(409);
    expect($s->fresh()->cupo_reservado)->toBe($s->cupo_reservado);
});

it('TRV-303-05 idempotencia: dos envíos, una reserva y un solo descuento', function () {
    $s = salidaQa();
    $this->como('qa.viajero20@trouvels360.test');
    $k = $this->idem();
    $body = ['personas' => 2, 'pago' => $this->pagoValido()];
    $a = $this->api('POST', "tours/salidas/{$s->id}/reservas", $body, $k)->assertCreated();
    $b = $this->api('POST', "tours/salidas/{$s->id}/reservas", $body, $k)->assertOk();
    expect($b->json('codigo'))->toBe($a->json('codigo'));
    expect($s->fresh()->cupo_reservado)->toBe($s->cupo_reservado + 2);
});

it('TRV-402-03 cancelar un tour devuelve exactamente sus personas', function () {
    $s = salidaQa();
    $this->como('qa.viajero21@trouvels360.test');
    $id = $this->api('POST', "tours/salidas/{$s->id}/reservas", ['personas' => 3, 'pago' => $this->pagoValido()], $this->idem())->json('reserva_id');
    $antes = $s->fresh()->cupo_reservado;
    $this->api('POST', "tours/reservas/$id/cancelar")->assertOk();
    expect($s->fresh()->cupo_reservado)->toBe($antes - 3);
    $this->api('POST', "tours/reservas/$id/cancelar")->assertStatus(409);
    expect($s->fresh()->cupo_reservado)->toBe($antes - 3);
});

it('TRV-401 mis reservas de tour incluye el nombre del tour y paginación', function () {
    $this->como('qa.viajero1@trouvels360.test');
    $this->api('GET', 'tours/mis-reservas')->assertOk()->assertJsonPath('data.0.servicio.nombre', 'Tour QA Cupos')
        ->assertJsonStructure(['meta' => ['total']]);
});

it('TRV-506 salidas: duplicada 409, fecha pasada 422, cupo menor a lo reservado 422, cerrar bloquea reservas', function () {
    $t = tourQa();
    $s = salidaQa();
    $this->como('edwinproveedor@gmail.com');
    $this->api('POST', "tours/{$t->id}/salidas", ['fecha' => $s->fechaIso(), 'hora' => '09:00', 'cupo_total' => 5])->assertStatus(409);
    $this->api('POST', "tours/{$t->id}/salidas", ['fecha' => now()->subDay()->toDateString(), 'hora' => '09:00', 'cupo_total' => 5])->assertStatus(422);
    $nueva = $this->api('POST', "tours/{$t->id}/salidas", ['fecha' => now()->addDays(30)->toDateString(), 'hora' => '07:30', 'cupo_total' => 8])
        ->assertCreated()->assertJsonPath('cupo_reservado', 0)->assertJsonPath('estado', 'programada')->json('id');

    $this->como('qa.viajero22@trouvels360.test')->api('POST', "tours/salidas/$nueva/reservas", ['personas' => 6, 'pago' => $this->pagoValido()], $this->idem())->assertCreated();
    $this->como('edwinproveedor@gmail.com');
    $this->api('PUT', "tours/{$t->id}/salidas/$nueva", ['cupo_total' => 5, 'cupo_reservado' => 0])->assertStatus(422);
    $this->api('PUT', "tours/{$t->id}/salidas/$nueva", ['estado' => 'cerrada'])->assertOk()->assertJsonPath('cupo_reservado', 6);
    $this->api('DELETE', "tours/{$t->id}/salidas/$nueva")->assertStatus(409);
    $this->como('qa.viajero23@trouvels360.test')->api('POST', "tours/salidas/$nueva/reservas", ['personas' => 1, 'pago' => $this->pagoValido()], $this->idem())->assertStatus(409);
});
