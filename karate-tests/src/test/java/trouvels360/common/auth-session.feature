@ignore
Feature: Helper para autenticar sesión (Proveedor o Viajero) con JWT y CSRF

  Scenario: Autenticación de Usuario
    # 1. Obtener token CSRF
    * def csrfCall = call read('classpath:trouvels360/common/csrf-token.feature')
    * def csrfToken = csrfCall.csrfToken
    * def xsrfVal = csrfCall.xsrfCookie

    # 2. Configurar credenciales recibidas o por defecto
    * def userEmail = __arg && __arg.email ? __arg.email : defaultProveedorEmail
    * def userPass = __arg && __arg.password ? __arg.password : defaultProveedorPass

    Given url baseUrl + '/auth/login'
    And header Content-Type = 'application/json'
    And header Accept = 'application/json'
    And header X-XSRF-TOKEN = xsrfVal
    And cookie XSRF-TOKEN = xsrfVal
    And request { email: '#(userEmail)', password: '#(userPass)' }
    When method post
    Then status 200
    And match response.message == 'Login OK'
    * def jwtCookie = responseCookies['access_token'] ? responseCookies['access_token'].value : null
    * def loggedUser = response.data.user
