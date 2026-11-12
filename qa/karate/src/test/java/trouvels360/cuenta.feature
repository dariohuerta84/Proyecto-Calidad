@cuenta
Feature: Mi cuenta (HU-V03, HU-V04)

  Background:
    * url baseUrl
    * def registrar = read('classpath:trouvels360/helpers/registrar.feature')
    * def sesion = read('classpath:trouvels360/helpers/sesion.feature')

  @HU-V03 @TC-API-013
  Scenario: PATCH /usuarios/me actualiza y GET /auth/me devuelve lo mismo
    * def r = call registrar { datos: { rol: 'viajero', email: '#(correo("qa.perfil"))', password: 'Clave123', nombre: 'Ana', apellido: 'Pérez' } }
    * eval como(r)
    Given path 'usuarios', 'me'
    And request { nombre: 'Lucía', apellido: 'Ramos', avatar_url: 'https://example.com/a.png' }
    When method patch
    Then status 200
    And assert responseTime < maxMs
    Given path 'auth', 'me'
    When method get
    Then status 200
    And match response.data contains { nombre: 'Lucía', apellido: 'Ramos', avatar_url: 'https://example.com/a.png' }

  @HU-V03 @HU-P02 @TC-API-014
  Scenario: Avatar y teléfono inválidos devuelven 422
    * eval como(S.viajero2)
    Given path 'usuarios', 'me'
    And request { avatar_url: 'no-es-url' }
    When method patch
    Then status 422
    And match response.errors.avatar_url == '#[_ > 0]'
    * eval como(S.proveedor2)
    Given path 'usuarios', 'me'
    And request { telefono: '12345' }
    When method patch
    Then status 422
    And match response.errors.telefono == '#[_ > 0]'

  @HU-V03 @TC-API-015
  Scenario: Enviar el rol devuelve 422 y el rol no cambia
    * eval como(S.viajero2)
    Given path 'usuarios', 'me'
    And request { rol: 'proveedor' }
    When method patch
    Then status 422
    And match response.errors.rol == '#[_ > 0]'
    Given path 'auth', 'me'
    When method get
    Then match response.data.rol == 'viajero'

  @HU-V03 @TC-API-016
  Scenario: Cambio de contraseña: login con la nueva 200 y con la anterior 401
    * def mail = correo('qa.pass')
    * def r = call registrar { datos: { rol: 'viajero', email: '#(mail)', password: 'Clave123', nombre: 'Pa', apellido: 'Ss' } }
    * eval como(r)
    Given path 'usuarios', 'me', 'password'
    And request { password_actual: 'Clave123', password: 'Nueva456', password_confirmation: 'Nueva456' }
    When method put
    Then status 200
    * def nueva = call sesion { email: '#(mail)', password: 'Nueva456' }
    * match nueva.loginStatus == 200
    * def vieja = call sesion { email: '#(mail)', password: 'Clave123' }
    * match vieja.loginStatus == 401

  @HU-V04 @TC-API-017
  Scenario: Eliminar la cuenta con una reserva activa devuelve 409
    * def r = call registrar { datos: { rol: 'viajero', email: '#(correo("qa.del409"))', password: 'Clave123', nombre: 'De', apellido: 'Lete' } }
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)' }
    * def dia = diaLibre()
    * def res = call read('classpath:trouvels360/helpers/reservar-hotel.feature') { sesion: '#(r)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 2))', items: [{ habitacion_id: '#(h.habitaciones[0].id)', cantidad: 1 }] }
    * match res.status == 201
    * eval como(r)
    Given path 'usuarios', 'me'
    When method delete
    Then status 409
    And match response.message == '#string'

  @HU-V04 @TC-API-018
  Scenario: Eliminar la cuenta sin reservas devuelve 200 y el login posterior 401
    * def mail = correo('qa.del')
    * def r = call registrar { datos: { rol: 'viajero', email: '#(mail)', password: 'Clave123', nombre: 'De', apellido: 'Lete' } }
    * eval como(r)
    Given path 'usuarios', 'me'
    When method delete
    Then status 200
    * def s = call sesion { email: '#(mail)', password: 'Clave123' }
    * match s.loginStatus == 401

  @HU-V04 @TC-API-019
  Scenario: Las reservas de una cuenta eliminada siguen visibles para el proveedor
    * def r = call registrar { datos: { rol: 'viajero', email: '#(correo("qa.hist"))', password: 'Clave123', nombre: 'His', apellido: 'Torial' } }
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)' }
    * def dia = diaLibre()
    * def res = call read('classpath:trouvels360/helpers/reservar-hotel.feature') { sesion: '#(r)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 1))', items: [{ habitacion_id: '#(h.habitaciones[0].id)', cantidad: 1 }] }
    * def reservaId = res.body.data.reservas[0].id
    * def codigo = res.body.data.reservas[0].codigo
    # Cancela (ya no está activa) y elimina la cuenta
    * eval como(r)
    Given path 'reservas-habitaciones', reservaId, 'cancelar'
    When method post
    Then status 200
    Given path 'usuarios', 'me'
    When method delete
    Then status 200
    # El proveedor sigue viendo la reserva en su historial
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas'
    And param servicio_id = h.hotelId
    When method get
    Then status 200
    And match response.data[*].codigo contains codigo
