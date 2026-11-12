@ignore
Feature: Helper - el proveedor publica un tour de prueba con una salida

  # Entrada: sesion (proveedor), cupo, (opcional) precio, ciudad, fechaSalida, categoria. Salida: tourId, salidaId, fechaSalida, ciudad
  Scenario:
    * url baseUrl
    * eval como(sesion)
    * def ciudad = karate.get('ciudad') || 'QA-' + sufijo()
    * def precio = karate.get('precio') || 100
    * def fechaSalida = karate.get('fechaSalida') || fecha(diaLibre())
    * def categoria = karate.get('categoria') || 'Aventura'
    Given path 'tours'
    And request { nombre: '#("Tour " + ciudad)', descripcion: 'Tour creado por Karate', ciudad: '#(ciudad)', pais: 'Perú', imagen_url: 'https://example.com/t.jpg', categoria: '#(categoria)', duracion: 120, precio: '#(precio)', latitud: -13.52, longitud: -71.97, salidas: [{ fecha: '#(fechaSalida)', hora: '08:00', cupo_total: '#(cupo)' }] }
    When method post
    Then status 201
    * def tourId = response.data.servicio_id || response.data.id
    Given path 'tours', tourId, 'salidas'
    When method get
    Then status 200
    * def salidaId = response[0].id
