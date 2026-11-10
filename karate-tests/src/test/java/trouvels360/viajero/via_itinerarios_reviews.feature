@viajero @itinerarios @reviews
Feature: HU-VIA-009 y HU-VIA-010: Generación de Itinerarios Inteligentes y Ciclo CRUD de Reseñas

  Background:
    * url baseUrl
    * header Accept = 'application/json'
    * def session = call read('classpath:trouvels360/common/auth-session.feature') { email: '#(defaultViajeroEmail)', password: '#(defaultViajeroPass)' }
    * def authCookie = session.jwtCookie
    * def xsrf = session.xsrfVal

  @HU-VIA-009 @CP-VIA-07
  Scenario: CP07 - Healthcheck del Microservicio FastAPI y Generación de Itinerario Óptimo (GET y POST)
    # 1. Healthcheck directo al microservicio Python (:8001)
    Given url itineraryUrl + '/health'
    When method get
    Then status 200
    And match response.status == 'healthy'
    And match response.service == 'Itinerarios Service'

    # 2. Generar itinerario óptimo (POST)
    Given path 'itineraries'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "destino": "Cusco",
      "hotel_id": 4,
      "fecha_checkin": "2026-12-01",
      "fecha_checkout": "2026-12-04",
      "interes": "Cultura"
    }
    """
    When method post
    Then status 200
    And match response.success == true
    And match response.data == '#object'

  @HU-VIA-010 @CP-VIA-08
  Scenario: CP08 - Ciclo CRUD Completo de Reseñas: Consultar (GET), Crear (POST), Editar (PUT) y Eliminar Reseña (DELETE)
    # 1. Consultar reseñas públicas existentes
    Given path 'reviews'
    When method get
    Then status 200
    And match response.data == '#[]'

    # 2. Crear nueva reseña para servicio con estadía completada (servicio_id = 4 Palacio del Inka de seeders)
    Given path 'reviews'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "servicio_id": 4,
      "comentario": "Excelente experiencia y servicio en Cusco durante toda la estadía QA.",
      "calificacion": 5
    }
    """
    When method post
    Then status 201
    And match response.message == 'Review creada correctamente'
    * def reviewId = response.data.id

    # 3. Editar reseña propia (PUT)
    Given path 'reviews', reviewId
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "comentario": "Excelente experiencia y servicio en Cusco, superó nuestras expectativas.",
      "calificacion": 5
    }
    """
    When method put
    Then status 200
    And match response.message == 'Review actualizada correctamente'

    # 4. Eliminar reseña propia (DELETE)
    Given path 'reviews', reviewId
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method delete
    Then status 204
