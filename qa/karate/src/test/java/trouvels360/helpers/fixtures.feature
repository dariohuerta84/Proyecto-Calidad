@ignore
Feature: Helper - localizar los datos semilla del QaSeeder (Hotel QA Concurrencia y Tour QA Cupos)

  Scenario:
    * url baseUrl
    * configure cookies = null
    * configure headers = { Accept: 'application/json' }
    Given path 'hoteles'
    And params { ciudad: 'Cusco', per_page: 100 }
    When method get
    Then status 200
    * def hotelQaId = karate.filter(response.data, function(h){ return h.nombre == 'Hotel QA Concurrencia' })[0].id
    Given path 'hoteles', hotelQaId
    When method get
    Then status 200
    * def porNombre = function(n){ return karate.filter(response.habitaciones, function(h){ return h.nombre == n })[0] }
    * def unica = porNombre('Habitación Única')
    * def doble = porNombre('Doble QA')
    * def suite = porNombre('Suite QA')
    Given path 'tours'
    And params { destino: 'Cusco', per_page: 100 }
    When method get
    Then status 200
    * def tourQaId = karate.filter(response.data, function(t){ return t.nombre == 'Tour QA Cupos' })[0].id
    # Todas las salidas (vista del proveedor dueño): abierta, cerrada y pasada
    * eval como(S.proveedor)
    Given path 'tours', tourQaId, 'salidas'
    When method get
    Then status 200
    * def hoy = fecha(0)
    * def salidaCerrada = karate.filter(response, function(s){ return s.estado == 'cerrada' && s.fecha > hoy })[0]
    * def salidaPasada = karate.filter(response, function(s){ return s.fecha < hoy })[0]
    * def salidaAbierta = karate.filter(response, function(s){ return s.estado == 'programada' && s.fecha > hoy && s.cupo_disponible > 3 })[0]
