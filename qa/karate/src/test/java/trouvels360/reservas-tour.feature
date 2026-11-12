@reservas-tour
Feature: Reserva de tour con pago simulado y confirmación automática (HU-V10)

  Background:
    * url baseUrl
    * def reservar = read('classpath:trouvels360/helpers/reservar-tour.feature')
    * def leerSalida = read('classpath:trouvels360/helpers/salida.feature')
    * def reservaSchema = read('classpath:trouvels360/schemas/reserva-tour.json')

  @HU-V10 @HU-V12 @TC-API-041
  Scenario: Reserva 201 CONFIRMADA, total = cotización y el cupo reservado aumenta en las personas
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 10, precio: 120 }
    * eval anonimo()
    Given path 'tours', 'salidas', t.salidaId, 'cotizar'
    And request { personas: 3 }
    When method post
    Then status 200
    * def cot = response.data
    * def r = call reservar { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 3 }
    * match r.status == 201
    * match r.body.data == reservaSchema
    * match r.body.data contains { estado: 'confirmada', personas: 3, tarjeta_ultimos4: '4242', subtotal: '#(cot.subtotal)', impuesto: '#(cot.impuesto)', total: '#(cot.total)' }
    * def s = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(t.tourId)', salidaId: '#(t.salidaId)' }
    * match s.salida.cupo_reservado == 3

  @HU-V10 @TC-API-042
  Scenario: Salida cerrada, pasada o sin cupo suficiente se rechaza y los cupos no cambian
    * def F = callonce read('classpath:trouvels360/helpers/fixtures.feature')
    * def cerrada = call reservar { sesion: '#(S.viajero2)', salidaId: '#(F.salidaCerrada.id)', personas: 1 }
    * match cerrada.status == 409
    * def pasada = call reservar { sesion: '#(S.viajero2)', salidaId: '#(F.salidaPasada.id)', personas: 1 }
    * match pasada.status == 409
    * def sc = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(F.tourQaId)', salidaId: '#(F.salidaCerrada.id)' }
    * match sc.salida.cupo_reservado == F.salidaCerrada.cupo_reservado
    # Sin cupo suficiente
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 2 }
    * def sinCupo = call reservar { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 3 }
    * match sinCupo.status == 409
    * match sinCupo.body.cupos_disponibles == 2
    * def s = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(t.tourId)', salidaId: '#(t.salidaId)' }
    * match s.salida.cupo_reservado == 0
    # Un proveedor no puede reservar
    * def prov = call reservar { sesion: '#(S.proveedor2)', salidaId: '#(t.salidaId)', personas: 1 }
    * match prov.status == 403

  @HU-V10 @TC-API-043
  Scenario: Idempotencia: dos envíos con la misma clave → una reserva y un solo descuento de cupo
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 10 }
    * def clave = uuid()
    * def r1 = call reservar { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 2, clave: '#(clave)' }
    * def r2 = call reservar { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 2, clave: '#(clave)' }
    * match r1.status == 201
    * match r2.body.data.codigo == r1.body.data.codigo
    * def s = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(t.tourId)', salidaId: '#(t.salidaId)' }
    * match s.salida.cupo_reservado == 2
