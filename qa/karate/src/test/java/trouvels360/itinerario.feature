@itinerario
Feature: Itinerario sugerido por el microservicio Python (TRV-404)

  Background:
    * url baseUrl
    * def F = callonce read('classpath:trouvels360/helpers/fixtures.feature')
    * def itinerarioSchema = read('classpath:trouvels360/schemas/itinerario.json')
    * eval como(S.viajero2)

  @TRV-404 @TC-API-054
  Scenario: Itinerario válido: un día por noche; los días con tour traen fecha y salida con cupo
    # Datos propios: hotel y tour de Aventura en la misma ciudad, con una salida dentro de la estadía
    * def dia = diaLibre()
    * def h = call read('classpath:trouvels360/helpers/hotel-propio.feature') { sesion: '#(S.proveedor)' }
    * def t = call read('classpath:trouvels360/helpers/tour-propio.feature') { sesion: '#(S.proveedor)', cupo: 8, ciudad: '#(h.ciudad)', fechaSalida: '#(fecha(dia + 1))', categoria: 'Aventura' }
    * eval como(S.viajero2)
    Given path 'itineraries'
    And request { destino: '#(h.ciudad)', hotel_id: '#(h.hotelId)', fecha_checkin: '#(fecha(dia))', fecha_checkout: '#(fecha(dia + 3))', interes: 'Aventura' }
    When method post
    Then status 200
    And assert responseTime < 5000
    And match response == itinerarioSchema
    And match response.data.resumen.total_dias == response.data.itinerario.length
    And match each response.data.itinerario contains { dia: '#number', fecha_sugerida: '##regex \\d{4}-\\d{2}-\\d{2}', actividades: '#array' }
    * def acts = karate.jsonPath(response, '$.data.itinerario[*].actividades[*]')
    And match acts[*].tour_id contains t.tourId
    And match response.data.resumen.total_dias == 3
    And match each acts contains { tour_id: '#number', nombre: '#string', salida: { salida_id: '#number', hora: '#string', cupos_disponibles: '#? _ > 0' } }

  @TRV-404 @TC-API-055
  Scenario Outline: Entrada inválida (<caso>) devuelve 422
    Given path 'itineraries'
    And request { destino: 'Cusco', hotel_id: '#(F.hotelQaId)', fecha_checkin: '#(fecha(<desde>))', fecha_checkout: '#(fecha(<hasta>))', interes: '<interes>' }
    When method post
    Then status 422
    And match response.errors.<campo> == '#[_ > 0]'

    Examples:
      | caso                     | interes   | desde | hasta | campo          |
      | interés fuera de catálogo | Compras   | 10    | 12    | interes        |
      | interés vacío            |           | 10    | 12    | interes        |
      | salida antes de entrada  | Cultura   | 12    | 10    | fecha_checkout |
      | más de 14 días           | Cultura   | 10    | 30    | fecha_checkout |
      | entrada en el pasado     | Cultura   | -2    | 3     | fecha_checkin  |
