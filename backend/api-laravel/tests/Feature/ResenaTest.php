<?php
// HU-V14
use App\Models\Servicio;

function hotelQaId(): int { return Servicio::where('nombre', 'Hotel QA Concurrencia')->value('id'); }

it('CA-V14-03 sin reserva finalizada responde 403 con la causa', function () {
    $this->como('qa.viajero24@trouvels360.test')
        ->api('POST', 'reviews', ['servicio_id' => hotelQaId(), 'calificacion' => 4, 'comentario' => 'Muy buena atención y limpieza.'])
        ->assertForbidden()->assertJsonPath('message', 'Debes haber completado una reserva confirmada de este servicio para opinar.');
});

it('CA-V14-02/05 publica con reserva finalizada, una sola vez, y recalcula el resumen', function () {
    $sid = hotelQaId();
    $antes = $this->api('GET', "reviews/resumen?servicio_id=$sid")->json('data.total');
    $this->como('qa.viajero1@trouvels360.test');
    $this->api('POST', 'reviews', ['servicio_id' => $sid, 'calificacion' => 5, 'comentario' => 'corto'])->assertStatus(422);
    $id = $this->api('POST', 'reviews', ['servicio_id' => $sid, 'calificacion' => 5, 'comentario' => 'Excelente estadía, volvería.'])
        ->assertCreated()->json('data.id');
    $this->api('POST', 'reviews', ['servicio_id' => $sid, 'calificacion' => 4, 'comentario' => 'Otra opinión distinta.'])->assertStatus(409);
    expect($this->api('GET', "reviews/resumen?servicio_id=$sid")->json('data.total'))->toBe($antes + 1);

    $this->api('PUT', "reviews/$id", ['calificacion' => 4])->assertOk()->assertJsonPath('data.calificacion', 4);
    $this->como('qa.viajero2@trouvels360.test')->api('PUT', "reviews/$id", ['calificacion' => 1])->assertForbidden();
    $this->como('qa.viajero2@trouvels360.test')->api('DELETE', "reviews/$id")->assertForbidden();
    $this->como('qa.viajero1@trouvels360.test')->api('DELETE', "reviews/$id")->assertNoContent();
});

it('CA-V14-01 el listado es paginado y filtra por servicio', function () {
    $r = $this->api('GET', 'reviews?servicio_id=' . hotelQaId())->assertOk()->assertJsonStructure(['data', 'meta' => ['total', 'last_page']]);
    foreach ($r->json('data') as $x) {
        expect($x['servicio_id'])->toBe(hotelQaId());
    }
});
