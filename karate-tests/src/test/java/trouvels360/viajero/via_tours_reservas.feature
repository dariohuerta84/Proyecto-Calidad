@viajero @tours
Feature: HU-VIA-005, HU-VIA-006, HU-VIA-007 y HU-VIA-008: Búsqueda, Reserva, Consulta y Cancelación de Tours

  Background:
    * url baseUrl
    * header Accept = 'application/json'
    * def session = call read('classpath:trouvels360/common/auth-session.feature') { email: '#(defaultViajeroEmail)', password: '#(defaultViajeroPass)' }
    * def authCookie = session.jwtCookie
    * def xsrf = session.xsrfVal

  @HU-VIA-006 @CP-VIA-05
  Scenario: CP05 - Búsqueda de tours por destino y categoría (GET)
    Given path 'tours'
    And param destino = 'Cusco'
    When method get
    Then status 200
    And match response.data == '#[]'

  @HU-VIA-005 @HU-VIA-007 @HU-VIA-008 @CP-VIA-06
  Scenario: CP06 - Flujo Completo de Tour: Consultar Salidas (GET), Reservar Tour (POST), Mis Reservas (GET) y Cancelar Tour (POST)
    # 1. Obtener un tour existente para conocer una salida válida
    Given path 'tours'
    When method get
    Then status 200
    * def tourObj = response.data[0]
    * def tourId = tourObj.id

    # 2. Consultar salidas del tour GET /api/tours/{tourId}/salidas
    Given path 'tours', tourId, 'salidas'
    When method get
    Then status 200
    * def salidaId = response[0].id

    # 3. Reservar Tour POST /api/tours/salidas/{salidaId}/reservas (HU-VIA-007)
    Given path 'tours', 'salidas', salidaId, 'reservas'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request { "personas": 1 }
    When method post
    Then status 201
    And match response.estado == 'confirmada'
    * def reservaTourId = response.reserva_id
    * def codigoReservaTour = response.codigo

    # 4. Consultar Mis Reservas de Tour GET /api/tours/mis-reservas (HU-VIA-005)
    Given path 'tours', 'mis-reservas'
    And cookie access_token = authCookie
    When method get
    Then status 200
    And match response.data == '#[]'

    # 5. Cancelar Reserva de Tour POST /api/tours/reservas/{reservaTourId}/cancelar (HU-VIA-008)
    Given path 'tours', 'reservas', reservaTourId, 'cancelar'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method post
    Then status 200
    And match response.message == 'Reserva cancelada'
