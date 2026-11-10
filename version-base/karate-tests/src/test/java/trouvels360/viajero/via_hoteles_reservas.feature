@viajero @hoteles
Feature: HU-VIA-003, HU-VIA-004, HU-VIA-005 y HU-VIA-008: Búsqueda, Disponibilidad, Reserva y Cancelación de Hoteles

  Background:
    * url baseUrl
    * header Accept = 'application/json'
    * def session = call read('classpath:trouvels360/common/auth-session.feature') { email: '#(defaultViajeroEmail)', password: '#(defaultViajeroPass)' }
    * def authCookie = session.jwtCookie
    * def xsrf = session.xsrfVal

  @HU-VIA-003 @CP-VIA-03
  Scenario: CP03 - Buscar hoteles disponibles por destino y rango de fechas (GET)
    Given path 'hoteles'
    And param check_in = '2026-12-01'
    And param check_out = '2026-12-05'
    And param ciudad = 'Cusco'
    And param habitaciones = 1
    When method get
    Then status 200
    And match response.data == '#[]'

  @HU-VIA-004 @HU-VIA-005 @HU-VIA-008 @CP-VIA-04
  Scenario: CP04 - Flujo Completo de Reserva de Hotel: Disponibilidad (GET), Solicitar Reserva (POST), Consultar Mis Reservas (GET) y Cancelar Reserva (POST)
    # 1. Obtener lista de hoteles para seleccionar una habitación existente
    Given path 'hoteles'
    When method get
    Then status 200
    * def primerHotel = response.data[0]
    * def habitacionId = primerHotel.habitaciones[0].id
    * def servicioId = primerHotel.servicio_id

    # 2. Consultar disponibilidad de ese hotel específico
    Given path 'hoteles', servicioId, 'disponibilidad'
    And param check_in = '2026-12-10'
    And param check_out = '2026-12-12'
    And param habitaciones = 1
    When method get
    Then status 200
    And match response.data.hotel.servicio_id == servicioId

    # 3. Solicitar Reserva de Habitación POST /api/reservas-habitaciones (HU-VIA-004)
    Given path 'reservas-habitaciones'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "habitacion_id": '#(habitacionId)',
      "fecha_inicio": "2026-12-10",
      "fecha_fin": "2026-12-12",
      "cantidad": 1
    }
    """
    When method post
    Then status 201
    And match response.message == 'Reserva creada correctamente.'
    And match response.data.estado == 'pendiente'
    * def reservaId = response.data.id
    * def codigoReserva = response.data.codigo_reserva

    # 4. Consultar Mis Reservas GET /api/mis-reservas (HU-VIA-005)
    Given path 'mis-reservas'
    And cookie access_token = authCookie
    When method get
    Then status 200
    And match response == '#[]'
    * def encontrada = karate.jsonPath(response, "$[?(@.codigo=='" + codigoReserva + "')]")
    And match encontrada[0].id == reservaId

    # 5. Cancelar Reserva POST /api/reservas-habitaciones/{id}/cancelar (HU-VIA-008)
    Given path 'reservas-habitaciones', reservaId, 'cancelar'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method post
    Then status 200
    And match response.message == 'Reserva cancelada.'
    And match response.data.estado == 'cancelada'
