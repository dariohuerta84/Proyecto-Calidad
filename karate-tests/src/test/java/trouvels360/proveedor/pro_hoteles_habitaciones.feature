@proveedor @hoteles
Feature: HU-PRO-003, HU-PRO-004, HU-PRO-005 y HU-PRO-010: Ciclo de Vida CRUD de Hoteles y Habitaciones del Proveedor

  Background:
    * url baseUrl
    * header Accept = 'application/json'
    * def session = call read('classpath:trouvels360/common/auth-session.feature') { email: '#(defaultProveedorEmail)', password: '#(defaultProveedorPass)' }
    * def authCookie = session.jwtCookie
    * def xsrf = session.xsrfVal

  @HU-PRO-003 @CP-PRO-04
  Scenario: CP04 - Consultar publicaciones propias del proveedor con indicadores de reservas (GET)
    Given path 'proveedor/servicios'
    And param tipo = 'hotel'
    And cookie access_token = authCookie
    When method get
    Then status 200
    And match response.data == '#[]'
    And match response.meta.total == '#number'

  @HU-PRO-004 @HU-PRO-005 @HU-PRO-010 @CP-PRO-05
  Scenario: CP05 - Ciclo CRUD Completo: Crear Hotel (POST), Modificar Hotel (PUT), Gestionar Habitación (POST, PUT, DELETE) y Retirar Servicio (DELETE)
    # 1. POST /api/hoteles -> Crear nuevo hotel
    * def rnd = Math.floor(Math.random() * 90000 + 10000)
    * def hotelNombre = 'Hotel QA Grand Libertador ' + rnd
    Given path 'hoteles'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "nombre": '#(hotelNombre)',
      "descripcion": "Alojamiento de pruebas de calidad automatizadas",
      "ciudad": "Cusco",
      "pais": "Perú",
      "direccion": "Av. El Sol 450",
      "estrellas": 4,
      "activo": true
    }
    """
    When method post
    Then status 201
    And match response.message == 'Servicio y hotel creados correctamente.'
    * def servicioId = response.data.servicio_id

    # 2. GET /api/hoteles/{servicioId} -> Consultar detalle
    Given path 'hoteles', servicioId
    When method get
    Then status 200
    And match response.hotel.nombre == hotelNombre
    And match response.hotel.estrellas == 4

    # 3. PUT /api/hoteles/{servicioId} -> Modificar hotel
    Given path 'hoteles', servicioId
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "nombre": '#(hotelNombre + " - Actualizado")',
      "estrellas": 5,
      "direccion": "Av. El Sol 450 Interior B"
    }
    """
    When method put
    Then status 200
    And match response.message == 'Hotel actualizado correctamente.'
    And match response.data.hotel.estrellas == 5

    # 4. POST /api/hoteles/{servicioId}/habitaciones -> Agregar habitación
    Given path 'hoteles', servicioId, 'habitaciones'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "nombre": "Suite Presidencial QA",
      "capacidad_adultos": 2,
      "capacidad_ninos": 1,
      "cantidad": 5,
      "precio_por_noche": 320.50,
      "descripcion": "Vista panorámica y jacuzzi"
    }
    """
    When method post
    Then status 201
    And match response.message == 'Habitación creada correctamente.'
    * def habitacionId = response.data.id

    # 5. PUT /api/habitaciones/{habitacionId} -> Actualizar precio y unidades
    Given path 'habitaciones', habitacionId
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request
    """
    {
      "nombre": "Suite Presidencial QA Deluxe",
      "capacidad_adultos": 2,
      "cantidad": 4,
      "precio_por_noche": 350.00
    }
    """
    When method put
    Then status 200
    And match response.message == 'Habitación actualizada correctamente.'
    And match response.data.precio_por_noche == 350.00

    # 6. DELETE /api/habitaciones/{habitacionId} -> Eliminar habitación
    Given path 'habitaciones', habitacionId
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method delete
    Then status 204

    # 7. DELETE /api/servicios/{servicioId} -> Retirar servicio en cascada (HU-PRO-010)
    Given path 'servicios', servicioId
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method delete
    Then status 204

    # 8. GET /api/eliminados -> Verificar en papelera
    Given path 'eliminados'
    When method get
    Then status 200
    And match response.count == '#number'
