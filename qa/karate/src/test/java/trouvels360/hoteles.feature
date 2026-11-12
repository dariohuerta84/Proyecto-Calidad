@hoteles
Feature: Búsqueda, detalle y cotización de hoteles (TRV-201, TRV-202)

  Background:
    * url baseUrl
    * def F = callonce read('classpath:trouvels360/helpers/fixtures.feature')
    * def habitacion = read('classpath:trouvels360/schemas/habitacion.json')
    * def hotelItem = read('classpath:trouvels360/schemas/hotel-item.json')
    * def meta = read('classpath:trouvels360/schemas/meta.json')
    * eval anonimo()

  @TRV-201 @TC-API-020
  Scenario: Búsqueda válida: todos los resultados cumplen ciudad, capacidad y unidades libres
    * def dia = diaLibre()
    Given path 'hoteles'
    And params { ciudad: 'cusco', check_in: '#(fecha(dia))', check_out: '#(fecha(dia + 2))', adultos: 2, ninos: 0, habitaciones: 1 }
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.meta == meta
    And match response.data == '#[_ > 0]'
    And match each response.data == hotelItem
    And match each response.data contains { ciudad: 'Cusco' }
    # cada hotel tiene al menos un tipo con unidades libres y capacidad para 2 adultos
    * def apto = function(h){ return karate.filter(h.habitaciones, function(x){ return x.unidades_disponibles >= 1 && x.capacidad_adultos * x.unidades_disponibles >= 2 }).length > 0 }
    * match each response.data == '#? apto(_)'

  @TRV-201 @TC-API-021
  Scenario Outline: Búsqueda inválida (<caso>) devuelve 422
    Given path 'hoteles'
    And params { ciudad: 'Cusco', check_in: '#(fecha(<check_in>))', check_out: '#(fecha(<check_out>))', adultos: <adultos>, habitaciones: <habitaciones> }
    When method get
    Then status 422
    And assert responseTime < maxMs
    And match response.errors == '#object'

    Examples:
      | read('classpath:trouvels360/data/busqueda-invalida.csv') |

  @TRV-201 @TC-API-022
  Scenario: Un hotel con todas sus unidades ocupadas por reservas pendientes no aparece
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)' }
    * def dia = diaLibre()
    * def res = call read('classpath:trouvels360/helpers/reservar-hotel.feature') { sesion: '#(S.viajero2)', fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 2))', items: [{ habitacion_id: '#(h.habitaciones[0].id)', cantidad: 1 }] }
    * match res.status == 201
    * match res.body.data.estado == 'pendiente'
    * eval anonimo()
    Given path 'hoteles'
    And params { ciudad: '#(h.ciudad)', check_in: '#(fecha(dia))', check_out: '#(fecha(dia + 2))', adultos: 1, habitaciones: 1 }
    When method get
    Then status 200
    And match response.meta.total == 0
    # En otras fechas sí aparece
    Given path 'hoteles'
    And params { ciudad: '#(h.ciudad)', check_in: '#(fecha(dia + 5))', check_out: '#(fecha(dia + 6))', adultos: 1, habitaciones: 1 }
    When method get
    Then status 200
    And match response.meta.total == 1

  @TRV-202 @TC-API-023
  Scenario: Esquema del detalle de hotel (habitaciones y calificación) y sus reseñas
    Given path 'hoteles', F.hotelQaId
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.hotel contains { servicio_id: '#(F.hotelQaId)', nombre: 'Hotel QA Concurrencia', ciudad: 'Cusco', direccion: '#string', estrellas: '#number', imagenes: '#array', calificacion: { promedio: '#number', cantidad: '#number' } }
    And match each response.habitaciones == habitacion
    And match response.habitaciones[*].nombre contains only ['Habitación Única', 'Doble QA', 'Suite QA']
    Given path 'reviews'
    And params { servicio_id: '#(F.hotelQaId)' }
    When method get
    Then status 200
    And match response.meta contains { total: '#number', last_page: '#number' }

  @TRV-202 @TC-API-024
  Scenario Outline: Cotización <tipo> × <unidades> × <noches> noches = S/ <total> con IGV
    * def tipos = { unica: '#(F.unica.id)', doble: '#(F.doble.id)', suite: '#(F.suite.id)' }
    * def dia = diaLibre()
    Given path 'reservas-habitaciones', 'cotizar'
    And header X-XSRF-TOKEN = S.viajero2.xsrf
    And cookie XSRF-TOKEN = S.viajero2.xsrf
    And request { fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + <noches>))', items: [{ habitacion_id: '#(tipos.<tipo>)', cantidad: <unidades> }] }
    When method post
    Then status 200
    And assert responseTime < maxMs
    And match response.data == read('classpath:trouvels360/schemas/cotizacion-hotel.json')
    And match response.data contains { noches: <noches>, subtotal: <subtotal>, impuesto: <impuesto>, total: <total> }
    And match response.data.items[0] contains { cantidad: <unidades>, subtotal: <subtotal> }

    Examples:
      | read('classpath:trouvels360/data/cotizaciones.csv') |

  @TRV-202 @TC-API-025
  Scenario: Cotizar más unidades que las libres devuelve 422 con el máximo
    * def dia = diaLibre()
    Given path 'reservas-habitaciones', 'cotizar'
    And header X-XSRF-TOKEN = S.viajero2.xsrf
    And cookie XSRF-TOKEN = S.viajero2.xsrf
    And request { fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 1))', items: [{ habitacion_id: '#(F.unica.id)', cantidad: 2 }] }
    When method post
    Then status 422
    And match response.errors.items[0] contains 'Máximo 1'
