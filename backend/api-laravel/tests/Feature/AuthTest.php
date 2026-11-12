<?php
// HU-V01, HU-P01, HU-V02

it('CA-V01-01 registra un viajero con 201 y sesión iniciada', function () {
    $r = $this->api('POST', 'auth/register', [
        'rol' => 'viajero', 'nombre' => 'Lucía', 'apellido' => 'Pérez', 'email' => 'Lucia.Perez@Test.com',
        'password' => 'secreto1', 'password_confirmation' => 'secreto1',
    ]);
    $r->assertCreated()->assertJsonPath('data.user.rol', 'viajero')->assertJsonPath('data.user.email', 'lucia.perez@test.com')
        ->assertJsonMissingPath('data.user.password')->assertCookie('access_token');
});

it('CA-V01-03 rechaza contraseñas distintas', function () {
    $this->api('POST', 'auth/register', [
        'rol' => 'viajero', 'nombre' => 'A', 'apellido' => 'B', 'email' => 'x@test.com',
        'password' => 'secreto1', 'password_confirmation' => 'otra123',
    ])->assertStatus(422)->assertJsonValidationErrors('password');
});

it('CA-V01-04 rechaza correo duplicado sin importar mayúsculas', function () {
    $this->api('POST', 'auth/register', [
        'rol' => 'viajero', 'nombre' => 'A', 'apellido' => 'B', 'email' => 'EDWINVIAJERO@gmail.com',
        'password' => 'secreto1', 'password_confirmation' => 'secreto1',
    ])->assertStatus(422)->assertJsonValidationErrors('email');
});

it('CA-V01-05 rechaza un rol no permitido', function () {
    $this->api('POST', 'auth/register', [
        'rol' => 'admin', 'nombre' => 'A', 'apellido' => 'B', 'email' => 'adm@test.com',
        'password' => 'secreto1', 'password_confirmation' => 'secreto1',
    ])->assertStatus(422)->assertJsonValidationErrors('rol');
});

it('CA-P01-01 normaliza el teléfono del proveedor', function () {
    $this->api('POST', 'auth/register', [
        'rol' => 'proveedor', 'empresa_nombre' => 'Andes SAC', 'ruc' => '20111111111', 'telefono' => '987654321',
        'email' => 'andes@test.com', 'password' => 'secreto1', 'password_confirmation' => 'secreto1',
    ])->assertCreated()->assertJsonPath('data.user.telefono', '+51 987654321');
});

it('CA-P01-02/03 rechaza RUC inválido y teléfono no peruano', function () {
    $this->api('POST', 'auth/register', [
        'rol' => 'proveedor', 'empresa_nombre' => 'X', 'ruc' => '123', 'telefono' => '12345',
        'email' => 'x2@test.com', 'password' => 'secreto1', 'password_confirmation' => 'secreto1',
    ])->assertStatus(422)->assertJsonValidationErrors(['ruc', 'telefono']);
});

it('CA-V02-01 inicia sesión y devuelve el perfil', function () {
    $this->api('POST', 'auth/login', ['email' => 'qa.viajero1@trouvels360.test', 'password' => 'Viajero123'])
        ->assertOk()->assertJsonPath('data.user.rol', 'viajero')->assertCookie('access_token');
});

it('CA-V02-03 credenciales incorrectas responden 401', function () {
    $this->api('POST', 'auth/login', ['email' => 'qa.viajero1@trouvels360.test', 'password' => 'mala'])->assertStatus(401);
    $this->api('POST', 'auth/login', ['email' => 'qa.viajero1@trouvels360.test'])->assertStatus(422);
});

it('HU-V02 una petición que modifica datos sin CSRF responde 403', function () {
    $this->json('POST', '/api/auth/login', ['email' => 'qa.viajero1@trouvels360.test', 'password' => 'Viajero123'])->assertStatus(403);
});

it('CA-V02-05 el token queda invalidado tras cerrar sesión', function () {
    $this->como('qa.viajero2@trouvels360.test');
    $this->api('GET', 'auth/me')->assertOk();
    $this->api('POST', 'auth/logout')->assertOk();
    $this->api('GET', 'auth/me')->assertStatus(401);
});

it('CA-P02-02 un viajero no accede a rutas de proveedor', function () {
    $this->como('qa.viajero1@trouvels360.test');
    $this->api('GET', 'proveedor/servicios')->assertForbidden();
    $this->api('GET', 'proveedor/reservas')->assertForbidden();
});

it('HU-V02 sin sesión las rutas privadas responden 401', function () {
    $this->anonimo()->api('GET', 'mis-reservas')->assertStatus(401);
});
