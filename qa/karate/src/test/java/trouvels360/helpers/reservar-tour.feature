@ignore
Feature: Helper - reservar un tour (pago simulado) como un viajero

  # Entrada: sesion, salidaId, personas, (opcional) clave. Salida: status, body, clave
  Scenario:
    * url baseUrl
    * eval como(sesion)
    * def clave = karate.get('clave') || uuid()
    Given path 'tours', 'salidas', salidaId, 'reservas'
    And header Idempotency-Key = clave
    And request { personas: '#(personas)', pago: '#(pago)' }
    When method post
    * def status = responseStatus
    * def body = response
