@proveedor @auth
Feature: HU-PRO-001 y HU-PRO-002: Autenticación y Gestión de Perfil de Proveedor

  Background:
    * url baseUrl
    * header Accept = 'application/json'

  @HU-PRO-001 @CP-PRO-01 @smoke
  Scenario: CP01 - Registro exitoso de nuevo proveedor con validación de rol
    * def csrfCall = call read('classpath:trouvels360/common/csrf-token.feature')
    * def randomSuffix = Math.floor(Math.random() * 89999999 + 10000000)
    * def uniqueRuc = '20' + Math.floor(Math.random() * 899999999 + 100000000)
    * def uniqueEmail = 'qa_prov_' + randomSuffix + '@calidadtest.pe'

    Given path 'auth/register'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = csrfCall.xsrfCookie
    And cookie XSRF-TOKEN = csrfCall.xsrfCookie
    And request
    """
    {
      "rol": "proveedor",
      "empresa_nombre": "Agencia Tours Andinos SAC",
      "ruc": '#(uniqueRuc)',
      "telefono": "+51 987654321",
      "email": '#(uniqueEmail)',
      "password": "Password123!"
    }
    """
    When method post
    Then status 201
    And match response.message == 'Registro exitoso.'
    And match response.data.user.rol == 'proveedor'
    And match response.data.user.empresa_nombre == 'Agencia Tours Andinos SAC'

  @HU-PRO-001 @CP-PRO-02 @negative
  Scenario Outline: CP02 - Validar rechazo de login con credenciales inválidas (<descripcion>)
    * def csrfCall = call read('classpath:trouvels360/common/csrf-token.feature')

    Given path 'auth/login'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = csrfCall.xsrfCookie
    And cookie XSRF-TOKEN = csrfCall.xsrfCookie
    And request { email: '<email>', password: '<password>' }
    When method post
    Then status <statusEsperado>
    And match response.message contains '<mensajeEsperado>'

    Examples:
      | descripcion           | email                   | password      | statusEsperado | mensajeEsperado        |
      | Clave errónea         | edwinproveedor@gmail.com| claveInvalida | 401            | Credenciales inválidas |
      | Correo inexistente    | noexiste999@test.pe     | proveedor     | 401            | Credenciales inválidas |
      | Formato email inválido| email_invalido_formato  | proveedor     | 422            | Datos inválidos        |

  @HU-PRO-002 @CP-PRO-03 @regression
  Scenario: CP03 - Flujo completo de Perfil de Proveedor (GET /me, PUT /usuarios/me, POST /logout)
    # 1. Login Proveedor
    * def session = call read('classpath:trouvels360/common/auth-session.feature') { email: '#(defaultProveedorEmail)', password: '#(defaultProveedorPass)' }

    # 2. Consultar /me (GET)
    Given path 'auth/me'
    And cookie access_token = session.jwtCookie
    When method get
    Then status 200
    And match response.data.rol == 'proveedor'
    And match response.data.email == defaultProveedorEmail

    # 3. Actualizar Perfil con PUT
    Given path 'usuarios/me'
    And header Content-Type = 'application/json'
    And header X-XSRF-TOKEN = session.xsrfVal
    And cookie XSRF-TOKEN = session.xsrfVal
    And cookie access_token = session.jwtCookie
    And request
    """
    {
      "empresa_nombre": "Edwin Proveedor Tours SAC Modificado",
      "telefono": "+51 999888777"
    }
    """
    When method put
    Then status 200
    And match response.message == 'Usuario actualizado correctamente.'
    And match response.data.empresa_nombre == 'Edwin Proveedor Tours SAC Modificado'

    # 4. Logout (POST)
    Given path 'auth/logout'
    And header X-XSRF-TOKEN = session.xsrfVal
    And cookie XSRF-TOKEN = session.xsrfVal
    And cookie access_token = session.jwtCookie
    When method post
    Then status 200
    And match response.message == 'Logout OK'
