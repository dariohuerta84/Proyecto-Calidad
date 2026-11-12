@ignore
Feature: Helper - leer una salida concreta de un tour (vista del proveedor dueño)

  # Entrada: sesion (proveedor dueño), tourId, salidaId. Salida: salida
  Scenario:
    * url baseUrl
    * eval como(sesion)
    Given path 'tours', tourId, 'salidas'
    When method get
    Then status 200
    * def salida = karate.filter(response, function(s){ return s.id == salidaId })[0]
