@ignore
Feature: Helper - obtener token CSRF (cookie XSRF-TOKEN)

  Scenario:
    * url baseUrl
    Given path 'auth', 'csrf'
    When method get
    Then status 200
    * def xsrf = responseCookies['XSRF-TOKEN'].value
