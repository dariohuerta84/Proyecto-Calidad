@proveedor @tours
Feature: HU-PRO-006, HU-PRO-007, HU-PRO-008 y HU-PRO-009: Gestión de Tours, Actividades, Salidas y Reporte de Reservas

  Background:
    * url baseUrl
    * header Accept = 'application/json'
    * def session = call read('classpath:trouvels360/common/auth-session.feature') { email: '#(defaultProveedorEmail)', password: '#(defaultProveedorPass)' }
    * def authCookie = session.jwtCookie
    * def xsrf = session.xsrfVal

  @HU-PRO-006 @HU-PRO-007 @HU-PRO-008 @HU-PRO-009 @CP-PRO-06
  Scenario: CP06 - Ciclo CRUD Completo de Tour: Crear Tour (POST), Actividad (POST, GET, PUT, DELETE), Salida (POST, GET, PUT, DELETE), Reporte (GET) y Eliminar Tour (DELETE)
    # 1. POST /api/tours -> Crear Tour con salida inicial
    * def rnd = Math.floor(Math.random() * 90000 + 10000)
    * def tourNombre = 'Tour QA Valle Sagrado ' + rnd
    Given path 'tours'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "nombre": '#(tourNombre)',
      "descripcion": "Excursión guiada por los mejores sitios arqueológicos",
      "ciudad": "Cusco",
      "pais": "Perú",
      "categoria": "Cultura",
      "duracion": 8,
      "precio": 120.00,
      "activo": true,
      "salidas": [
        {
          "fecha": "2026-12-15",
          "hora": "08:30",
          "cupo_total": 20,
          "estado": "programada"
        }
      ]
    }
    """
    When method post
    Then status 201
    And match response.message == 'Tour creado correctamente'
    * def tourId = response.data.id

    # 2. GET /api/tours/{tourId} -> Consultar Tour
    Given path 'tours', tourId
    When method get
    Then status 200
    And match response.servicio.nombre == tourNombre
    And match response.servicio.tour.categoria == 'Cultura'

    # 3. PUT /api/tours/{tourId} -> Actualizar precio y descripción
    Given path 'tours', tourId
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "nombre": '#(tourNombre + " Actualizado")',
      "precio": 135.00,
      "duracion": 9
    }
    """
    When method put
    Then status 200
    And match response.message == 'Tour actualizado correctamente'

    # 4. POST /api/tours/{tourId}/actividades -> Agregar actividad al programa
    Given path 'tours', tourId, 'actividades'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "titulo": "Visita a las Ruinas de Pisac",
      "descripcion": "Recorrido guiado por el complejo arqueológico",
      "orden": 1,
      "duracion_min": 120,
      "direccion": "Pisac, Cusco"
    }
    """
    When method post
    Then status 201
    * def actividadId = response.id

    # 5. GET /api/tours/{tourId}/actividades -> Listar actividades
    Given path 'tours', tourId, 'actividades'
    When method get
    Then status 200
    And match response == '#[]'
    And match response[0].titulo == 'Visita a las Ruinas de Pisac'

    # 6. PUT /api/tours/{tourId}/actividades/{actividadId} -> Actualizar actividad
    Given path 'tours', tourId, 'actividades', actividadId
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request { "titulo": "Visita a Pisac y Mercado Artesanal", "duracion_min": 150 }
    When method put
    Then status 200
    And match response.titulo == 'Visita a Pisac y Mercado Artesanal'

    # 7. DELETE /api/tours/{tourId}/actividades/{actividadId} -> Eliminar actividad
    Given path 'tours', tourId, 'actividades', actividadId
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method delete
    Then status 200
    And match response.message == 'Actividad eliminada'

    # 8. POST /api/tours/{tourId}/salidas -> Crear nueva salida programada
    Given path 'tours', tourId, 'salidas'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "fecha": "2026-12-20",
      "hora": "09:00",
      "cupo_total": 15,
      "estado": "programada"
    }
    """
    When method post
    Then status 201
    * def salidaId = response.id

    # 9. GET /api/tours/{tourId}/salidas -> Listar salidas con cupos
    Given path 'tours', tourId, 'salidas'
    When method get
    Then status 200
    And match response == '#[]'

    # 10. PUT /api/tours/{tourId}/salidas/{salidaId} -> Actualizar cupo total
    Given path 'tours', tourId, 'salidas', salidaId
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request { "cupo_total": 18, "estado": "programada" }
    When method put
    Then status 200
    And match response.cupo_total == 18

    # 11. DELETE /api/tours/{tourId}/salidas/{salidaId} -> Eliminar salida sin reservas
    Given path 'tours', tourId, 'salidas', salidaId
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method delete
    Then status 200
    And match response.message == 'Salida eliminada'

    # 12. GET /api/proveedor/servicios/{tourId}/reservas -> Consultar métricas de reservas
    Given path 'proveedor/servicios', tourId, 'reservas'
    And cookie access_token = authCookie
    When method get
    Then status 200
    And match response.servicio_id == tourId
    And match response.totales.confirmadas == '#number'

    # 13. DELETE /api/tours/{tourId} -> Eliminar Tour (HU-PRO-010)
    Given path 'tours', tourId
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method delete
    Then status 200
    And match response.message == 'Tour eliminado'
