@ignore
Feature: Helper - registrar un usuario nuevo y devolver su sesión

  # Entrada: datos (cuerpo de /auth/register sin password_confirmation). Salida: token, xsrf, user, status, body
  Scenario:
    * url baseUrl
    * configure cookies = null
    * configure headers = { Accept: "application/json" }
    * call read('this:csrf.feature')
    * def cuerpo = karate.merge(datos, { password_confirmation: '#(datos.password)' })
    * set cuerpo.password_confirmation = datos.password
    Given path 'auth', 'register'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And request cuerpo
    When method post
    * def status = responseStatus
    * def body = response
    * def token = status == 201 ? responseCookies.access_token.value : null
    * def user = status == 201 ? response.data.user : null
