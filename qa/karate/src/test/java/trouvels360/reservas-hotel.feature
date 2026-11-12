@reservas-hotel
Feature: Solicitud de reserva de hotel con pago simulado (TRV-203)

  Background:
    * url baseUrl
    * def F = callonce read('classpath:trouvels360/helpers/fixtures.feature')
    * def reservar = read('classpath:trouvels360/helpers/reservar-hotel.feature')
    * def reservaSchema = read('classpath:trouvels360/schemas/reserva-hotel.json')

  @TRV-203 @TC-API-026
  Scenario: Reserva válida 201: PENDIENTE, código único y mismos importes que la cotización
    * def dia = diaLibre()
    * def items = [{ habitacion_id: '#(F.doble.id)', cantidad: 2 }]
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', 'cotizar'
    And request { fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 2))', items: '#(items)' }
    When method post
    Then status 200
    * def cot = response.data
    * def res = call reservar { sesion: '#(S.viajero2)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 2))', items: '#(items)' }
    * match res.status == 201
    * match res.body.data contains { estado: 'pendiente', solicitud_codigo: '#regex SOL-[A-Z0-9]+', tarjeta_ultimos4: '4242' }
    * match res.body.data contains { subtotal: '#(cot.subtotal)', impuesto: '#(cot.impuesto)', total: '#(cot.total)' }
    * match each res.body.data.reservas == reservaSchema
    * match res.body.data.reservas[0].codigo == '#regex [A-Z0-9]{8,12}'

  @TRV-203 @TC-API-027
  Scenario Outline: Tarjeta inválida (<caso>) devuelve 422 y no crea reserva
    # Viajero nuevo por ejemplo: así el conteo no lo alteran otros escenarios en paralelo
    * def nuevo = call read('classpath:trouvels360/helpers/registrar.feature') { datos: { rol: 'viajero', email: '#(correo("qa.tarjeta"))', password: 'Clave123', nombre: 'QA', apellido: 'Tarjeta' } }
    * eval como(nuevo)
    Given path 'mis-reservas'
    When method get
    * def antes = response.meta.total
    * def dia = diaLibre()
    Given path 'reservas-habitaciones'
    And header Idempotency-Key = uuid()
    And request { fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 1))', items: [{ habitacion_id: '#(F.doble.id)', cantidad: 1 }], pago: { titular: 'QA', numero: '<numero>', vencimiento: '<vencimiento>', cvc: '<cvc>' } }
    When method post
    Then status 422
    And assert responseTime < maxMs
    And match response.errors['<campo>'] == '#[_ > 0]'
    Given path 'mis-reservas'
    When method get
    Then match response.meta.total == antes
    And match antes == 0

    Examples:
      | read('classpath:trouvels360/data/tarjetas-invalidas.csv') |

  @TRV-203 @TC-API-028
  Scenario: La disponibilidad descuenta las unidades reservadas
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)', habitaciones: [{ nombre: 'Doble', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 5, precio_por_noche: 150 }] }
    * def dia = diaLibre()
    * def rango = { check_in: '#(fecha(dia))', check_out: '#(fecha(dia + 2))', adultos: 1, habitaciones: 1 }
    Given path 'hoteles', h.hotelId, 'disponibilidad'
    And params rango
    When method get
    Then status 200
    And match response.data.habitaciones[0].unidades_disponibles == 5
    * def res = call reservar { sesion: '#(S.viajero2)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 2))', items: [{ habitacion_id: '#(h.habitaciones[0].id)', cantidad: 2 }] }
    * match res.status == 201
    Given path 'hoteles', h.hotelId, 'disponibilidad'
    And params rango
    When method get
    Then status 200
    And match response.data.habitaciones[0].unidades_disponibles == 3

  @TRV-203 @TC-API-029
  Scenario: Varios tipos con uno sin stock devuelve 409 y no registra ninguno
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)', habitaciones: [{ nombre: 'Única', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 1, precio_por_noche: 100 }, { nombre: 'Doble', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 2, precio_por_noche: 150 }] }
    * def unica = karate.filter(h.habitaciones, function(x){ return x.nombre == 'Única' })[0].id
    * def doble = karate.filter(h.habitaciones, function(x){ return x.nombre == 'Doble' })[0].id
    * def dia = diaLibre()
    * def primera = call reservar { sesion: '#(S.viajero2)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 1))', items: [{ habitacion_id: '#(unica)', cantidad: 1 }] }
    * match primera.status == 201
    * def segunda = call reservar { sesion: '#(S.viajero)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 1))', items: [{ habitacion_id: '#(unica)', cantidad: 1 }, { habitacion_id: '#(doble)', cantidad: 1 }] }
    * match segunda.status == 409
    Given path 'hoteles', h.hotelId, 'disponibilidad'
    And params { check_in: '#(fecha(dia))', check_out: '#(fecha(dia + 1))', adultos: 1, habitaciones: 1 }
    When method get
    Then status 200
    * def dobleDisp = karate.filter(response.data.habitaciones, function(x){ return x.id == doble })[0].unidades_disponibles
    * match dobleDisp == 2

  @TRV-203 @TC-API-030
  Scenario: El mismo Idempotency-Key devuelve la misma solicitud sin duplicarla
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)', habitaciones: [{ nombre: 'Doble', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 5, precio_por_noche: 150 }] }
    * def dia = diaLibre()
    * def clave = uuid()
    * def datos = { sesion: '#(S.viajero2)', clave: '#(clave)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 1))', items: [{ habitacion_id: '#(h.habitaciones[0].id)', cantidad: 1 }] }
    * def r1 = call reservar datos
    * def r2 = call reservar datos
    * match r1.status == 201
    * match r2.body.data.solicitud_codigo == r1.body.data.solicitud_codigo
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas'
    And param servicio_id = h.hotelId
    When method get
    Then status 200
    And match response.meta.total == 1
