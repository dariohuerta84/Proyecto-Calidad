@mis-reservas
Feature: Mis reservas y cancelación por el viajero (TRV-401, TRV-402)

  Background:
    * url baseUrl
    * def reservarHotel = read('classpath:trouvels360/helpers/reservar-hotel.feature')
    * def reservarTour = read('classpath:trouvels360/helpers/reservar-tour.feature')
    * def leerSalida = read('classpath:trouvels360/helpers/salida.feature')
    * def reservaHotelSchema = read('classpath:trouvels360/schemas/reserva-hotel.json')
    * def reservaTourSchema = read('classpath:trouvels360/schemas/reserva-tour.json')
    * def metaSchema = read('classpath:trouvels360/schemas/meta.json')
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)', habitaciones: [{ nombre: 'Doble', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 3, precio_por_noche: 150 }] }
    * def habId = h.habitaciones[0].id
    * def dia = diaLibre()
    * def rango = { check_in: '#(fecha(dia))', check_out: '#(fecha(dia + 2))', adultos: 1, habitaciones: 1 }
    * def solicitar = function(s){ return karate.call('classpath:trouvels360/helpers/reservar-hotel.feature', { sesion: s, fecha_inicio: fecha(dia), fecha_fin: fecha(dia + 2), items: [{ habitacion_id: habId, cantidad: 1 }] }) }
    * def disponibles = function(){ var r = karate.http(baseUrl + '/hoteles/' + h.hotelId + '/disponibilidad').param('check_in', rango.check_in).param('check_out', rango.check_out).param('adultos', '1').param('habitaciones', '1').header('Accept', 'application/json').get(); return r.body.data.habitaciones[0].unidades_disponibles }

  @TRV-401 @TC-API-044
  Scenario: Listado paginado con solo las reservas propias (hotel y tour)
    * def nuevo = call read('classpath:trouvels360/helpers/registrar.feature') { datos: { rol: 'viajero', email: '#(correo("qa.mis"))', password: 'Clave123', nombre: 'QA', apellido: 'Mis' } }
    * def a = solicitar(nuevo)
    * def b = solicitar(nuevo)
    * def c = solicitar(S.viajero2)
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 5 }
    * def rt = call reservarTour { sesion: '#(nuevo)', salidaId: '#(t.salidaId)', personas: 1 }
    * eval como(nuevo)
    Given path 'mis-reservas'
    And param per_page = 1
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.meta == metaSchema
    And match response.meta contains { total: 2, last_page: 2, per_page: 1 }
    And match each response.data == reservaHotelSchema
    Given path 'mis-reservas'
    When method get
    * def propios = karate.map(response.data, function(r){ return r.id }).sort()
    * def esperados = [ '#(a.body.data.reservas[0].id)', '#(b.body.data.reservas[0].id)' ]
    And match propios contains only esperados
    And match propios !contains c.body.data.reservas[0].id
    Given path 'tours', 'mis-reservas'
    When method get
    Then status 200
    And match response.meta.total == 1
    And match each response.data == reservaTourSchema

  @TRV-401 @TC-API-045
  Scenario: El viajero ve el estado tras aceptar y el motivo tras rechazar
    * def a = solicitar(S.viajero2)
    * def b = solicitar(S.viajero2)
    * def idA = a.body.data.reservas[0].id
    * def idB = b.body.data.reservas[0].id
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', idA, 'aceptar'
    When method post
    Then status 200
    Given path 'proveedor', 'reservas-habitaciones', idB, 'rechazar'
    And request { motivo: 'No hay disponibilidad por evento privado.' }
    When method post
    Then status 200
    * eval como(S.viajero2)
    Given path 'mis-reservas', idA
    When method get
    Then status 200
    And match response.data contains { estado: 'confirmada', motivo_rechazo: null }
    Given path 'mis-reservas', idB
    When method get
    Then status 200
    And match response.data contains { estado: 'rechazada', motivo_rechazo: 'No hay disponibilidad por evento privado.', cancelable: false }

  @TRV-401 @TC-API-046
  Scenario: Consultar una reserva ajena devuelve 404 (no revela que existe)
    * def a = solicitar(S.viajero2)
    * eval como(S.viajero)
    Given path 'mis-reservas', a.body.data.reservas[0].id
    When method get
    Then status 404
    * eval anonimo()
    Given path 'mis-reservas', a.body.data.reservas[0].id
    When method get
    Then status 401

  @TRV-402 @TC-API-047
  Scenario: Cancelar una reserva de hotel la deja CANCELADA y libera las unidades
    * def antes = disponibles()
    * def a = solicitar(S.viajero2)
    * match disponibles() == antes - 1
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', a.body.data.reservas[0].id, 'cancelar'
    When method post
    Then status 200
    And assert responseTime < maxMs
    And match response.data contains { estado: 'cancelada', cancelado_en: '#string', cancelable: false }
    * match disponibles() == antes

  @TRV-402 @TC-API-048
  Scenario: Cancelar un tour devuelve exactamente las personas al cupo
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 10 }
    * def r1 = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 2 }
    * def r2 = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 4 }
    * def s = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(t.tourId)', salidaId: '#(t.salidaId)' }
    * match s.salida.cupo_reservado == 6
    * eval como(S.viajero2)
    Given path 'tours', 'reservas', r2.body.data.id, 'cancelar'
    When method post
    Then status 200
    And match response.data.estado == 'cancelada'
    * def s = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(t.tourId)', salidaId: '#(t.salidaId)' }
    * match s.salida.cupo_reservado == 2

  @TRV-402 @TC-API-049
  Scenario: Cancelar dos veces → 409; cancelar una rechazada → 409; cancelar una ajena → 404
    * def a = solicitar(S.viajero2)
    * def idA = a.body.data.reservas[0].id
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', idA, 'cancelar'
    When method post
    Then status 200
    Given path 'reservas-habitaciones', idA, 'cancelar'
    When method post
    Then status 409
    And match response.estado == 'cancelada'
    * def b = solicitar(S.viajero2)
    * def idB = b.body.data.reservas[0].id
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', idB, 'rechazar'
    And request { motivo: 'Cierre temporal del establecimiento.' }
    When method post
    Then status 200
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', idB, 'cancelar'
    When method post
    Then status 409
    * def c = solicitar(S.viajero2)
    * eval como(S.viajero)
    Given path 'reservas-habitaciones', c.body.data.reservas[0].id, 'cancelar'
    When method post
    Then status 404
