@ignore
Feature: Helper - iniciar sesión (CSRF + login) y devolver cookie JWT y token XSRF

  # Entrada: email, password. Salida: token, xsrf, user, loginResponse, loginStatus
  Scenario:
    * url baseUrl
    * configure cookies = null
    * configure headers = { Accept: "application/json" }
    * call read('this:csrf.feature')
    Given path 'auth', 'login'
    And header X-XSRF-TOKEN = xsrf
    And cookie XSRF-TOKEN = xsrf
    And request { email: '#(email)', password: '#(password)' }
    When method post
    * def loginStatus = responseStatus
    * def loginResponse = response
    * def token = loginStatus == 200 ? responseCookies.access_token.value : null
    * def user = loginStatus == 200 ? response.data.user : null
