@aprobacion
Feature: Flujo de aprobación de solicitudes de hotel por el proveedor (HU-P13/HU-P14)

  Background:
    * url baseUrl
    * def reservar = read('classpath:trouvels360/helpers/reservar-hotel.feature')
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)', habitaciones: [{ nombre: 'Doble', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 3, precio_por_noche: 150 }] }
    * def habId = h.habitaciones[0].id
    * def dia = diaLibre()
    * def rango = { check_in: '#(fecha(dia))', check_out: '#(fecha(dia + 2))', adultos: 1, habitaciones: 1 }
    * def solicitar = function(s){ return karate.call('classpath:trouvels360/helpers/reservar-hotel.feature', { sesion: s, fecha_inicio: fecha(dia), fecha_fin: fecha(dia + 2), items: [{ habitacion_id: habId, cantidad: 1 }] }) }

  @HU-V12 @HU-P13 @TC-API-031
  Scenario: Viajero solicita → proveedor acepta → viajero ve CONFIRMADA
    * def res = solicitar(S.viajero2)
    * match res.status == 201
    * def id = res.body.data.reservas[0].id
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', id, 'aceptar'
    When method post
    Then status 200
    And assert responseTime < maxMs
    And match response.data contains { estado: 'confirmada', decidido_en: '#string' }
    * eval como(S.viajero2)
    Given path 'mis-reservas', id
    When method get
    Then status 200
    And match response.data.estado == 'confirmada'

  @HU-P14 @TC-API-032
  Scenario: Rechazar con motivo libera las unidades
    Given path 'hoteles', h.hotelId, 'disponibilidad'
    And params rango
    When method get
    Then match response.data.habitaciones[0].unidades_disponibles == 3
    * def res = solicitar(S.viajero2)
    * def id = res.body.data.reservas[0].id
    Given path 'hoteles', h.hotelId, 'disponibilidad'
    And params rango
    When method get
    Then match response.data.habitaciones[0].unidades_disponibles == 2
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', id, 'rechazar'
    And request { motivo: 'Mantenimiento programado en esas fechas.' }
    When method post
    Then status 200
    And match response.data contains { estado: 'rechazada', motivo_rechazo: 'Mantenimiento programado en esas fechas.' }
    Given path 'hoteles', h.hotelId, 'disponibilidad'
    And params rango
    When method get
    Then match response.data.habitaciones[0].unidades_disponibles == 3

  @HU-P13 @TC-API-033
  Scenario: Aceptar dos veces o aceptar una cancelada devuelve 409
    * def a = solicitar(S.viajero2)
    * def idA = a.body.data.reservas[0].id
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', idA, 'aceptar'
    When method post
    Then status 200
    Given path 'proveedor', 'reservas-habitaciones', idA, 'aceptar'
    When method post
    Then status 409
    And match response.estado == 'confirmada'
    * def b = solicitar(S.viajero2)
    * def idB = b.body.data.reservas[0].id
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', idB, 'cancelar'
    When method post
    Then status 200
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', idB, 'aceptar'
    When method post
    Then status 409

  @HU-P13 @TC-API-034
  Scenario: Un proveedor no puede decidir sobre reservas de otro proveedor (403)
    * def res = solicitar(S.viajero2)
    * def id = res.body.data.reservas[0].id
    * eval como(S.proveedor2)
    Given path 'proveedor', 'reservas-habitaciones', id, 'aceptar'
    When method post
    Then status 403
    Given path 'proveedor', 'reservas-habitaciones', id, 'rechazar'
    And request { motivo: 'Intento de un proveedor ajeno.' }
    When method post
    Then status 403

  @HU-P14 @TC-API-035
  Scenario: Rechazar sin motivo (o con menos de 10 caracteres) devuelve 422
    * def res = solicitar(S.viajero2)
    * def id = res.body.data.reservas[0].id
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', id, 'rechazar'
    And request { }
    When method post
    Then status 422
    And match response.errors.motivo == '#[_ > 0]'
    Given path 'proveedor', 'reservas-habitaciones', id, 'rechazar'
    And request { motivo: 'corto' }
    When method post
    Then status 422
