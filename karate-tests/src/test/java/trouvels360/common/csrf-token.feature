@ignore
Feature: Helper para obtener Token CSRF y Cookie XSRF-TOKEN de Trouvels360

  Scenario: Obtener CSRF
    Given url baseUrl + '/auth/csrf'
    When method get
    Then status 200
    And match response.csrf_token == '#string'
    * def csrfToken = response.csrf_token
    * def cookiesHeader = responseHeaders['Set-Cookie']
    * def xsrfCookie = responseCookies['XSRF-TOKEN'] ? responseCookies['XSRF-TOKEN'].value : csrfToken
