<?php
// HU-V03, HU-V04

it('CA-V03-01 actualiza nombre y avatar', function () {
    $this->como('qa.viajero3@trouvels360.test');
    $this->api('PATCH', 'usuarios/me', ['nombre' => 'Rosa', 'avatar_url' => 'https://img.test/a.png'])
        ->assertOk()->assertJsonPath('data.nombre', 'Rosa');
    $this->api('GET', 'auth/me')->assertJsonPath('data.avatar_url', 'https://img.test/a.png');
});

it('CA-V03-02/03 rechaza avatar inválido y cambio de rol', function () {
    $this->como('qa.viajero3@trouvels360.test');
    $this->api('PATCH', 'usuarios/me', ['avatar_url' => 'no-es-url'])->assertStatus(422);
    $this->api('PATCH', 'usuarios/me', ['rol' => 'proveedor'])->assertStatus(422)->assertJsonValidationErrors('rol');
});

it('CA-V03-04/05 cambia la contraseña solo con la actual correcta', function () {
    $this->como('qa.viajero4@trouvels360.test');
    $this->api('PUT', 'usuarios/me/password', ['password_actual' => 'mala', 'password' => 'Nueva123', 'password_confirmation' => 'Nueva123'])
        ->assertStatus(422);
    $this->api('PUT', 'usuarios/me/password', ['password_actual' => 'Viajero123', 'password' => 'Nueva123', 'password_confirmation' => 'Nueva123'])
        ->assertOk();
    $this->anonimo();
    $this->api('POST', 'auth/login', ['email' => 'qa.viajero4@trouvels360.test', 'password' => 'Nueva123'])->assertOk();
    $this->api('POST', 'auth/login', ['email' => 'qa.viajero4@trouvels360.test', 'password' => 'Viajero123'])->assertStatus(401);
});

it('CA-V04-03 no elimina la cuenta con reservas activas', function () {
    $this->como('edwinviajero@gmail.com');
    $this->api('DELETE', 'usuarios/me')->assertStatus(409);
});

it('CA-V04-02 elimina una cuenta sin reservas y ya no puede ingresar', function () {
    $this->como('qa.viajero5@trouvels360.test');
    $this->api('DELETE', 'usuarios/me')->assertOk();
    $this->anonimo()->api('POST', 'auth/login', ['email' => 'qa.viajero5@trouvels360.test', 'password' => 'Viajero123'])->assertStatus(401);
});
