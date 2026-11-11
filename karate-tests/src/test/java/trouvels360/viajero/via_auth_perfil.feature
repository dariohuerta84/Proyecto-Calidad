@viajero @auth
Feature: HU-VIA-001 y HU-VIA-002: Autenticación y Gestión de Perfil del Viajero

  Background:
    * url baseUrl
    * header Accept = 'application/json'

  @HU-VIA-001 @CP-VIA-01 @smoke
  Scenario: CP01 - Registro exitoso de nuevo viajero
    * def csrfCall = call read('classpath:trouvels360/common/csrf-token.feature')
    * def rnd = Math.floor(Math.random() * 8999999 + 1000000)
    * def uniqueEmail = 'viajero_qa_' + rnd + '@testviajes.pe'

    Given path 'auth/register'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = csrfCall.xsrfCookie
    And cookie XSRF-TOKEN = csrfCall.xsrfCookie
    And request
    """
    {
      "rol": "viajero",
      "nombre": "Carlos",
      "apellido": "Mendoza",
      "email": '#(uniqueEmail)',
      "password": "Password123!"
    }
    """
    When method post
    Then status 201
    And match response.message == 'Registro exitoso.'
    And match response.data.user.rol == 'viajero'
    And match response.data.user.nombre == 'Carlos'

  @HU-VIA-001 @HU-VIA-002 @CP-VIA-02 @regression
  Scenario: CP02 - Ciclo de Perfil de Viajero: Login (POST), Consultar (GET), Actualizar (PUT) y Baja de Cuenta (DELETE)
    # 1. Registrar usuario temporal para ciclo de vida de cuenta
    * def csrfCall = call read('classpath:trouvels360/common/csrf-token.feature')
    * def rnd = Math.floor(Math.random() * 8999999 + 1000000)
    * def emailTemp = 'viajero_temp_' + rnd + '@testviajes.pe'

    Given path 'auth/register'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = csrfCall.xsrfCookie
    And cookie XSRF-TOKEN = csrfCall.xsrfCookie
    And request { "rol": "viajero", "nombre": "Temp", "apellido": "User", "email": '#(emailTemp)', "password": "Password123!" }
    When method post
    Then status 201
    * def authCookie = responseCookies['access_token'] ? responseCookies['access_token'].value : null
    * def xsrf = csrfCall.xsrfCookie

    # 2. Consultar Perfil GET /api/auth/me
    Given path 'auth/me'
    And cookie access_token = authCookie
    When method get
    Then status 200
    And match response.data.email == emailTemp
    And match response.data.rol == 'viajero'

    # 3. Actualizar Perfil con PUT /api/usuarios/me
    Given path 'usuarios/me'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    And request { "nombre": "Carlos Alberto", "apellido": "Mendoza Torres" }
    When method put
    Then status 200
    And match response.message == 'Usuario actualizado correctamente.'
    And match response.data.nombre == 'Carlos Alberto'

    # 4. Eliminar cuenta con DELETE /api/usuarios/me
    Given path 'usuarios/me'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And cookie access_token = authCookie
    When method delete
    Then status 200
    And match response.message == 'Cuenta eliminada exitosamente.'
