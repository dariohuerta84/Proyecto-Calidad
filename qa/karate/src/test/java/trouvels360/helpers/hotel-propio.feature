@ignore
Feature: Helper - el proveedor publica un hotel de prueba en una ciudad única

  # Entrada: sesion (proveedor), habitaciones (lista), (opcional) ciudad. Salida: hotelId, ciudad, habitaciones (con ids)
  Scenario:
    * url baseUrl
    * eval como(sesion)
    * def ciudad = karate.get('ciudad') || 'QA-' + sufijo()
    * def hab = karate.get('habitaciones') || [{ nombre: 'Única', capacidad_adultos: 2, capacidad_ninos: 0, cantidad: 1, precio_por_noche: 100 }]
    Given path 'hoteles'
    And request { nombre: '#("Hotel " + ciudad)', descripcion: 'Hotel creado por Karate', ciudad: '#(ciudad)', pais: 'Perú', imagen_url: 'https://example.com/qa.jpg', direccion: 'Av. Karate 1', estrellas: 3, latitud: -13.52, longitud: -71.97, habitaciones: '#(hab)' }
    When method post
    Then status 201
    * def hotelId = response.data.servicio_id || response.data.id
    Given path 'hoteles', hotelId
    When method get
    Then status 200
    * def habitaciones = response.habitaciones
