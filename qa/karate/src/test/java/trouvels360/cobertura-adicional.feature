@cobertura-adicional
Feature: Criterios que no tenían caso en la etapa 1 (agregados en la etapa 5)

  Background:
    * url baseUrl
    * def hotelPropio = read('classpath:trouvels360/helpers/hotel-propio.feature')
    * def tourPropio = read('classpath:trouvels360/helpers/tour-propio.feature')
    * def reservarTour = read('classpath:trouvels360/helpers/reservar-tour.feature')
    * def leerSalida = read('classpath:trouvels360/helpers/salida.feature')
    * def unTipo = [{ nombre: 'Doble', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 3, precio_por_noche: 150 }]

  @HU-V06 @TC-API-079
  Scenario: CA-V06-05 Cambiar la fecha de salida recalcula el total y no crea ninguna reserva
    * def nuevo = call read('classpath:trouvels360/helpers/registrar.feature') { datos: { rol: 'viajero', email: '#(correo("qa.cot"))', password: 'Clave123', nombre: 'Co', apellido: 'Tiza' } }
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(unTipo)' }
    * def dia = diaLibre()
    * def items = [{ habitacion_id: '#(h.habitaciones[0].id)', cantidad: 1 }]
    * eval como(nuevo)
    Given path 'reservas-habitaciones', 'cotizar'
    And request { fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 2))', items: '#(items)' }
    When method post
    Then status 200
    And match response.data.total == 354
    Given path 'reservas-habitaciones', 'cotizar'
    And request { fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 4))', items: '#(items)' }
    When method post
    Then status 200
    And assert responseTime < maxMs
    And match response.data contains { noches: 4, total: 708 }
    Given path 'mis-reservas'
    When method get
    Then match response.meta.total == 0

  @HU-V09 @TC-API-080
  Scenario: CA-V09-04 Un tour sin salidas futuras disponibles no muestra salidas y no se puede cotizar
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'salidas', t.salidaId
    And request { estado: 'cerrada' }
    When method put
    Then status 200
    * eval anonimo()
    Given path 'tours', t.tourId
    When method get
    Then status 200
    And match response.servicio.salidas == []
    Given path 'tours', 'salidas', t.salidaId, 'cotizar'
    And request { personas: 1 }
    When method post
    Then status 409

  @HU-V10 @TC-API-081
  Scenario Outline: CA-V10-02 Pago de tour inválido (<caso>) no crea reserva ni descuenta cupos
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * eval como(S.viajero2)
    Given path 'tours', 'salidas', t.salidaId, 'reservas'
    And header Idempotency-Key = uuid()
    And request { personas: 2, pago: { titular: 'QA', numero: '<numero>', vencimiento: '<vencimiento>', cvc: '<cvc>' } }
    When method post
    Then status 422
    And match response.errors['<campo>'] == '#[_ > 0]'
    * def s = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(t.tourId)', salidaId: '#(t.salidaId)' }
    * match s.salida.cupo_reservado == 0

    Examples:
      | read('classpath:trouvels360/data/tarjetas-invalidas.csv') |

  @HU-V12 @TC-API-082
  Scenario: CA-V12-04 Si el proveedor cancela la salida, la reserva sigue confirmada con el aviso del proveedor
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * def nuevo = call read('classpath:trouvels360/helpers/registrar.feature') { datos: { rol: 'viajero', email: '#(correo("qa.aviso"))', password: 'Clave123', nombre: 'Avi', apellido: 'So' } }
    * def r = call reservarTour { sesion: '#(nuevo)', salidaId: '#(t.salidaId)', personas: 1 }
    * match r.status == 201
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'salidas', t.salidaId
    And request { estado: 'cancelada' }
    When method put
    Then status 200
    * eval como(nuevo)
    Given path 'tours', 'mis-reservas'
    When method get
    Then status 200
    And match response.data[0] contains { estado: 'confirmada', aviso: 'Salida cancelada por el proveedor', cancelado_en: null }

  @HU-P06 @TC-API-083
  Scenario: CA-P06-01 Editar descripción e imágenes conserva el id y no cambia las reservas
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(unTipo)' }
    * def dia = diaLibre()
    * def r = call read('classpath:trouvels360/helpers/reservar-hotel.feature') { sesion: '#(S.viajero2)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 1))', items: [{ habitacion_id: '#(h.habitaciones[0].id)', cantidad: 1 }] }
    * def reserva = r.body.data.reservas[0]
    * eval como(S.proveedor)
    Given path 'hoteles', h.hotelId
    And request { descripcion: 'Descripción renovada por Karate', imagenes: [{ url: 'https://example.com/nueva.jpg', alt: 'Nueva' }] }
    When method put
    Then status 200
    * eval anonimo()
    Given path 'hoteles', h.hotelId
    When method get
    Then status 200
    And match response.hotel contains { servicio_id: '#(h.hotelId)', descripcion: 'Descripción renovada por Karate' }
    And match response.hotel.imagenes[*].url == ['https://example.com/nueva.jpg']
    And match response.habitaciones[0].id == h.habitaciones[0].id
    * eval como(S.viajero2)
    Given path 'mis-reservas', reserva.id
    When method get
    Then match response.data contains { total: '#(reserva.total)', estado: 'pendiente' }

  @HU-P05 @TC-API-084
  Scenario: CA-P05-03 Si un tipo de habitación es inválido no queda un hotel a medias
    * def ciudad = 'QA-' + sufijo()
    * eval como(S.proveedor)
    Given path 'hoteles'
    And request { nombre: '#("Hotel " + ciudad)', ciudad: '#(ciudad)', pais: 'Perú', imagen_url: 'https://example.com/h.jpg', direccion: 'Av. 1', estrellas: 3, habitaciones: [{ nombre: 'Buena', capacidad_adultos: 2, cantidad: 2, precio_por_noche: 100 }, { nombre: 'Mala', capacidad_adultos: 2, cantidad: 2, precio_por_noche: 0 }] }
    When method post
    Then status 422
    And match response.errors['habitaciones.1.precio_por_noche'] == '#[_ > 0]'
    * eval anonimo()
    Given path 'hoteles'
    And param ciudad = ciudad
    When method get
    Then match response.meta.total == 0
    * eval como(S.proveedor)
    Given path 'proveedor', 'servicios'
    And param search = ciudad
    When method get
    Then match response.meta.total == 0

  @HU-P07 @TC-API-085
  Scenario: CA-P07-05 Tarifa 0 devuelve 422 y un tipo de otro proveedor 403, sin cambios
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(unTipo)' }
    * def habId = h.habitaciones[0].id
    * eval como(S.proveedor)
    Given path 'habitaciones', habId
    And request { precio_por_noche: 0 }
    When method put
    Then status 422
    And match response.errors.precio_por_noche == '#[_ > 0]'
    * eval como(S.proveedor2)
    Given path 'habitaciones', habId
    And request { precio_por_noche: 999 }
    When method put
    Then status 403
    * eval anonimo()
    Given path 'hoteles', h.hotelId
    When method get
    Then match response.habitaciones[0].precio_por_noche == 150

  @HU-P15 @TC-API-086
  Scenario: CA-P15-05 Repetir la eliminación devuelve 404 sin efectos adicionales
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(unTipo)' }
    * eval como(S.proveedor)
    Given path 'proveedor', 'indicadores'
    When method get
    * def antes = response.data.publicaciones
    Given path 'hoteles', h.hotelId
    When method delete
    Then status 200
    Given path 'hoteles', h.hotelId
    When method delete
    Then status 404
    Given path 'proveedor', 'indicadores'
    When method get
    Then match response.data.publicaciones == antes - 1
