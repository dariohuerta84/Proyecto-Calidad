@auth
Feature: Autenticación y registro (TRV-101, TRV-102, TRV-103)

  Background:
    * url baseUrl
    * def usuarioSchema = read('classpath:trouvels360/schemas/usuario.json')

  @TRV-101 @TC-API-001
  Scenario: Registro de viajero válido devuelve 201 y usuario sin password
    * def correo1 = correo('qa.reg')
    * def r = call read('classpath:trouvels360/helpers/registrar.feature') { datos: { rol: 'viajero', email: '#(correo1)', password: 'Clave123', nombre: 'Ana', apellido: 'Pérez' } }
    * match r.status == 201
    * match r.body.data.user == usuarioSchema
    * match r.body.data.user contains { rol: 'viajero', email: '#(correo1)' }
    * match r.token == '#string'

  @TRV-101 @TC-API-002
  Scenario Outline: Registro inválido (<caso>) devuelve 422 con error en <campo>
    * call read('classpath:trouvels360/helpers/csrf.feature')
    * def correoCaso = '<email>' == 'ok' ? correo('qa.inv') : '<email>'
    Given path 'auth', 'register'
    And header X-XSRF-TOKEN = xsrf
    And request { rol: 'viajero', email: '#(correoCaso)', password: '<password>', password_confirmation: '<confirmacion>', nombre: '<nombre>', apellido: '<apellido>' }
    When method post
    Then status 422
    And assert responseTime < maxMs
    And match response.errors['<campo>'] == '#[_ > 0]'

    Examples:
      | read('classpath:trouvels360/data/registro-invalido.csv') |

  @TRV-101 @TC-API-003
  Scenario: Correo duplicado escrito en mayúsculas devuelve 422
    * call read('classpath:trouvels360/helpers/csrf.feature')
    Given path 'auth', 'register'
    And header X-XSRF-TOKEN = xsrf
    And request { rol: 'viajero', email: 'EDWINVIAJERO@GMAIL.COM', password: 'Clave123', password_confirmation: 'Clave123', nombre: 'Ana', apellido: 'Pérez' }
    When method post
    Then status 422
    And match response.errors.email == '#[_ > 0]'

  @TRV-101 @TC-API-004
  Scenario: Rol 'admin' devuelve 422
    * call read('classpath:trouvels360/helpers/csrf.feature')
    Given path 'auth', 'register'
    And header X-XSRF-TOKEN = xsrf
    And request { rol: 'admin', email: '#(correo("qa.admin"))', password: 'Clave123', password_confirmation: 'Clave123', nombre: 'Ana', apellido: 'Pérez' }
    When method post
    Then status 422
    And match response.errors.rol == '#[_ > 0]'

  @TRV-102 @TC-API-005
  Scenario: Registro de proveedor normaliza el celular a '+51 9XXXXXXXX'
    * def cel = celular()
    * def r = call read('classpath:trouvels360/helpers/registrar.feature') { datos: { rol: 'proveedor', email: '#(correo("qa.prov"))', password: 'Clave123', empresa_nombre: 'QA Karate SAC', ruc: '#(ruc())', telefono: '#(cel)' } }
    * match r.status == 201
    * match r.body.data.user == usuarioSchema
    * match r.body.data.user.telefono == '+51 ' + cel
    * match r.body.data.user.rol == 'proveedor'

  @TRV-102 @TC-API-006
  Scenario Outline: RUC inválido (<caso>) devuelve 422
    * call read('classpath:trouvels360/helpers/csrf.feature')
    Given path 'auth', 'register'
    And header X-XSRF-TOKEN = xsrf
    And request { rol: 'proveedor', email: '#(correo("qa.ruc"))', password: 'Clave123', password_confirmation: 'Clave123', empresa_nombre: 'QA SAC', ruc: '<ruc>', telefono: '987654321' }
    When method post
    Then status 422
    And assert responseTime < maxMs
    And match response.errors.ruc == '#[_ > 0]'

    Examples:
      | read('classpath:trouvels360/data/rucs-invalidos.csv') |

  @TRV-102 @TC-API-007
  Scenario Outline: Teléfono inválido (<caso>) devuelve 422 con mensaje
    * call read('classpath:trouvels360/helpers/csrf.feature')
    Given path 'auth', 'register'
    And header X-XSRF-TOKEN = xsrf
    And request { rol: 'proveedor', email: '#(correo("qa.tel"))', password: 'Clave123', password_confirmation: 'Clave123', empresa_nombre: 'QA SAC', ruc: '#(ruc())', telefono: '<telefono>' }
    When method post
    Then status 422
    And match response.errors.telefono[0] == '#string'

    Examples:
      | read('classpath:trouvels360/data/telefonos-invalidos.csv') |

  @TRV-103 @TC-API-008
  Scenario: Login devuelve 200, cookie access_token HttpOnly y el usuario
    * def s = call read('classpath:trouvels360/helpers/sesion.feature') { email: 'qa.viajero3@trouvels360.test', password: 'Viajero123' }
    * match s.loginStatus == 200
    * match s.loginResponse.data.user == usuarioSchema
    * match s.token == '#string'
    # La cookie del JWT no es legible desde JavaScript
    * call read('classpath:trouvels360/helpers/csrf.feature')
    Given path 'auth', 'login'
    And header X-XSRF-TOKEN = xsrf
    And request { email: 'qa.viajero3@trouvels360.test', password: 'Viajero123' }
    When method post
    Then status 200
    And assert responseTime < maxMs
    * def setCookies = karate.filter(karate.toJava(responseHeaders['Set-Cookie']) || [], function(c){ return c.indexOf('access_token=') == 0 })
    * match setCookies == '#[1]'
    * match setCookies[0].toLowerCase() contains 'httponly'

  @TRV-103 @TC-API-009
  Scenario: POST sin header X-XSRF-TOKEN devuelve 403
    Given path 'auth', 'login'
    And request { email: 'qa.viajero3@trouvels360.test', password: 'Viajero123' }
    When method post
    Then status 403

  @TRV-103 @TC-API-010
  Scenario: Credenciales inválidas devuelven 401 y campos faltantes 422
    * call read('classpath:trouvels360/helpers/csrf.feature')
    Given path 'auth', 'login'
    And header X-XSRF-TOKEN = xsrf
    And request { email: 'qa.viajero3@trouvels360.test', password: 'incorrecta' }
    When method post
    Then status 401
    Given path 'auth', 'login'
    And header X-XSRF-TOKEN = xsrf
    And request { }
    When method post
    Then status 422
    And match response.errors contains { email: '#array', password: '#array' }

  @TRV-103 @TC-API-011
  Scenario: El token reutilizado después del logout devuelve 401
    * def r = call read('classpath:trouvels360/helpers/registrar.feature') { datos: { rol: 'viajero', email: '#(correo("qa.logout"))', password: 'Clave123', nombre: 'Lo', apellido: 'Gout' } }
    * eval como(r)
    Given path 'auth', 'me'
    When method get
    Then status 200
    Given path 'auth', 'logout'
    When method post
    Then status 200
    # Se reenvía a mano el JWT anterior
    * eval como(r)
    Given path 'auth', 'me'
    When method get
    Then status 401

  @TRV-103 @TC-API-012
  Scenario: Un viajero en una ruta de proveedor recibe 403
    * eval como(S.viajero2)
    Given path 'proveedor', 'reservas'
    When method get
    Then status 403
    And assert responseTime < maxMs
