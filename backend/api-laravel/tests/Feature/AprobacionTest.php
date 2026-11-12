<?php
// HU-P13/HU-P14, HU-V11, HU-V13, HU-P12
use App\Models\Habitacion;
use App\Models\Servicio;

function solicitar(object $t, string $email, string $hab = 'Habitación Única', int $desde = 70): array {
    $sid = Servicio::where('nombre', 'Hotel QA Concurrencia')->value('id');
    $h = Habitacion::where('servicio_id', $sid)->where('nombre', $hab)->first();
    $in = now()->addDays($desde)->toDateString(); $out = now()->addDays($desde + 2)->toDateString();
    $t->como($email);
    $r = $t->api('POST', 'reservas-habitaciones', ['fecha_inicio' => $in, 'fecha_fin' => $out,
        'items' => [['habitacion_id' => $h->id, 'cantidad' => 1]], 'pago' => $t->pagoValido()], $t->idem())->assertCreated();
    return ['id' => $r->json('data.reservas.0.id'), 'sid' => $sid, 'hab' => $h->id, 'in' => $in, 'out' => $out];
}

it('CA-V12-01 el proveedor acepta y el viajero ve CONFIRMADA', function () {
    $s = solicitar($this, 'qa.viajero11@trouvels360.test');
    $this->como('edwinproveedor@gmail.com')->api('POST', "proveedor/reservas-habitaciones/{$s['id']}/aceptar")
        ->assertOk()->assertJsonPath('data.estado', 'confirmada');
    $this->como('qa.viajero11@trouvels360.test')->api('GET', "mis-reservas/{$s['id']}")->assertJsonPath('data.estado', 'confirmada');
});

it('CA-V12-02 rechazar exige motivo, libera la unidad y el viajero ve el motivo', function () {
    $s = solicitar($this, 'qa.viajero12@trouvels360.test', 'Habitación Única', 75);
    $this->como('edwinproveedor@gmail.com');
    $this->api('POST', "proveedor/reservas-habitaciones/{$s['id']}/rechazar", [])->assertStatus(422);
    $this->api('POST', "proveedor/reservas-habitaciones/{$s['id']}/rechazar", ['motivo' => 'Mantenimiento programado en esas fechas'])
        ->assertOk()->assertJsonPath('data.estado', 'rechazada');
    $disp = collect($this->anonimo()->api('GET', "hoteles/{$s['sid']}/disponibilidad?check_in={$s['in']}&check_out={$s['out']}")->json('data.habitaciones'))->pluck('id');
    expect($disp)->toContain($s['hab']);
    $this->como('qa.viajero12@trouvels360.test')->api('GET', "mis-reservas/{$s['id']}")
        ->assertJsonPath('data.estado', 'rechazada')->assertJsonPath('data.motivo_rechazo', 'Mantenimiento programado en esas fechas');
});

it('CA-P13-03 decidir dos veces o sobre una cancelada responde 409', function () {
    $s = solicitar($this, 'qa.viajero13@trouvels360.test', 'Doble QA');
    $this->como('edwinproveedor@gmail.com');
    $this->api('POST', "proveedor/reservas-habitaciones/{$s['id']}/aceptar")->assertOk();
    $this->api('POST', "proveedor/reservas-habitaciones/{$s['id']}/aceptar")->assertStatus(409)->assertJsonPath('estado', 'confirmada');

    $s2 = solicitar($this, 'qa.viajero13@trouvels360.test', 'Doble QA', 80);
    $this->api('POST', "reservas-habitaciones/{$s2['id']}/cancelar")->assertOk();
    $this->como('edwinproveedor@gmail.com')->api('POST', "proveedor/reservas-habitaciones/{$s2['id']}/aceptar")->assertStatus(409);
});

it('CA-P13-04 un proveedor ajeno no puede decidir (403)', function () {
    $s = solicitar($this, 'qa.viajero14@trouvels360.test', 'Doble QA');
    $this->como('qa.proveedor2@trouvels360.test')->api('POST', "proveedor/reservas-habitaciones/{$s['id']}/aceptar")->assertForbidden();
});

it('HU-V11 mis reservas es paginado, propio y filtra por estado', function () {
    $this->como('edwinviajero@gmail.com');
    $r = $this->api('GET', 'mis-reservas?estado=pendiente')->assertOk()->assertJsonStructure(['data', 'meta' => ['total', 'last_page']]);
    foreach ($r->json('data') as $x) {
        expect($x['estado'])->toBe('pendiente');
    }
    $ajena = \App\Models\ReservaHabitacion::where('usuario_id', '!=', \App\Models\Usuario::where('email', 'edwinviajero@gmail.com')->value('id'))->value('id');
    $this->api('GET', "mis-reservas/$ajena")->assertNotFound();
});

it('HU-V13 cancelar: confirmada libera, repetida 409, rechazada 409, ajena 404', function () {
    $s = solicitar($this, 'qa.viajero15@trouvels360.test', 'Doble QA', 90);
    $this->api('POST', "reservas-habitaciones/{$s['id']}/cancelar")->assertOk()->assertJsonPath('data.estado', 'cancelada');
    $this->api('POST', "reservas-habitaciones/{$s['id']}/cancelar")->assertStatus(409);

    $s2 = solicitar($this, 'qa.viajero15@trouvels360.test', 'Doble QA', 95);
    $this->como('edwinproveedor@gmail.com')->api('POST', "proveedor/reservas-habitaciones/{$s2['id']}/rechazar", ['motivo' => 'No podemos atender esas fechas']);
    $this->como('qa.viajero15@trouvels360.test')->api('POST', "reservas-habitaciones/{$s2['id']}/cancelar")->assertStatus(409);
    $this->como('qa.viajero16@trouvels360.test')->api('POST', "reservas-habitaciones/{$s2['id']}/cancelar")->assertNotFound();
});

it('HU-P12 la bandeja del proveedor solo trae reservas propias con cliente', function () {
    solicitar($this, 'qa.viajero17@trouvels360.test', 'Doble QA', 100);
    $r = $this->como('qa.proveedor2@trouvels360.test')->api('GET', 'proveedor/reservas?estado=pendiente')->assertOk();
    expect(collect($r->json('data'))->pluck('servicio.nombre')->unique()->all())->not->toContain('Hotel QA Concurrencia');
    $r = $this->como('edwinproveedor@gmail.com')->api('GET', 'proveedor/reservas?estado=pendiente&tipo=hotel')->assertOk();
    expect($r->json('data.0.cliente.email'))->not->toBeNull();
});
