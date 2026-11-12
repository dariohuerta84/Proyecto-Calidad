@tours
Feature: Búsqueda, detalle y cotización de tours (HU-V08, HU-V09)

  Background:
    * url baseUrl
    * eval anonimo()
    * def salidaSchema = read('classpath:trouvels360/schemas/salida.json')

  @HU-V08 @TC-API-036
  Scenario: Filtros combinados destino + categoría: todos los resultados cumplen ambos
    Given path 'tours'
    And params { destino: 'Cusco', categoria: 'Aventura', per_page: 50 }
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.data == '#[_ > 0]'
    And match each response.data contains { tipo: 'tour', ciudad: '#regex (?i).*cusco.*', tour: { categoria: 'Aventura', precio: '#number', duracion: '#number', servicio_id: '#number' } }
    And match response.meta contains { total: '#number', last_page: '#number', current_page: 1 }
    # Con cupos, cada tour devuelto tiene al menos una salida con ese cupo libre
    Given path 'tours'
    And params { destino: 'Cusco', cupos: 2 }
    When method get
    Then status 200
    * def ids = karate.map(response.data, function(t){ return t.id })
    * def tieneCupo = function(id){ var r = karate.http(baseUrl + '/tours/' + id + '?cupos=2').get(); return r.body.servicio.salidas.length > 0 }
    * match each ids == '#? tieneCupo(_)'

  @HU-V08 @TC-API-037
  Scenario Outline: Filtro inválido (<caso>) devuelve 422 con error en <campo>
    Given path 'tours'
    And param <parametro> = '<valor>'
    When method get
    Then status 422
    And assert responseTime < maxMs
    And match response.errors.<campo> == '#[_ > 0]'

    Examples:
      | read('classpath:trouvels360/data/filtros-tour-invalidos.csv') |

  @HU-V09 @TC-API-038
  Scenario: Detalle: esquema correcto y solo salidas futuras programadas con cupo
    * def F = callonce read('classpath:trouvels360/helpers/fixtures.feature')
    Given path 'tours', F.tourQaId
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.servicio contains { id: '#(F.tourQaId)', nombre: 'Tour QA Cupos', tipo: 'tour', tour: '#object', calificacion: '#object', salidas: '#array', reviews: '#array' }
    And match each response.servicio.salidas == salidaSchema
    And match each response.servicio.salidas contains { estado: 'programada' }
    * def hoy = fecha(0)
    And match each response.servicio.salidas == '#? _.fecha >= hoy && _.cupos_disponibles > 0'
    # La salida cerrada y la pasada del QaSeeder no aparecen
    * def ids = karate.map(response.servicio.salidas, function(s){ return s.id })
    And match ids !contains F.salidaCerrada.id
    And match ids !contains F.salidaPasada.id
    # Tour inexistente
    Given path 'tours', 999999
    When method get
    Then status 404

  @HU-V09 @TC-API-039
  Scenario Outline: Cotización de tour: S/ <precio> × <personas> + IGV 18 % = S/ <total>
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 10, precio: <precio> }
    * eval anonimo()
    Given path 'tours', 'salidas', t.salidaId, 'cotizar'
    And request { personas: <personas> }
    When method post
    Then status 200
    And assert responseTime < maxMs
    And match response.data contains { personas: <personas>, precio_unitario: <precio>, igv_porcentaje: 18, subtotal: <subtotal>, impuesto: <impuesto>, total: <total> }

    Examples:
      | read('classpath:trouvels360/data/cotizaciones-tour.csv') |

  @HU-V09 @TC-API-040
  Scenario: Cotizar más personas que el cupo libre devuelve 422 con el máximo
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 3 }
    * eval anonimo()
    Given path 'tours', 'salidas', t.salidaId, 'cotizar'
    And request { personas: 4 }
    When method post
    Then status 422
    And match response.errors.personas[0] contains '3'
    Given path 'tours', 'salidas', t.salidaId, 'cotizar'
    And request { personas: 3 }
    When method post
    Then status 200
