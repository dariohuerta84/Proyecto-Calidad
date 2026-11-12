@ignore
Feature: Helper - crear una solicitud de reserva de hotel como un viajero

  # Entrada: sesion, fecha_inicio, fecha_fin, items, (opcional) clave. Salida: status, body, clave
  Scenario:
    * url baseUrl
    * eval como(sesion)
    * def clave = karate.get('clave') || uuid()
    Given path 'reservas-habitaciones'
    And header Idempotency-Key = clave
    And request { fecha_inicio: '#(fecha_inicio)', fecha_fin: '#(fecha_fin)', items: '#(items)', pago: '#(pago)' }
    When method post
    * def status = responseStatus
    * def body = response
