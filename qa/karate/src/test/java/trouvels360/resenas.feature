@resenas @parallel=false
Feature: Reseñas y calificaciones de servicios (TRV-403)
  # Secuencial: qa.viajero1 es el único con estadía y tour finalizados en el QaSeeder.
  # Cada escenario deja sus reseñas como las encontró.

  Background:
    * url baseUrl
    * def F = callonce read('classpath:trouvels360/helpers/fixtures.feature')
    * def reviewSchema = read('classpath:trouvels360/schemas/review.json')
    * def limpiar =
      """
      function(servicioId) {
        como(S.viajero1);
        var r = karate.http(baseUrl + '/reviews/puedo-opinar').param('servicio_id', servicioId + '')
          .header('Accept', 'application/json').header('Cookie', 'access_token=' + S.viajero1.token).get();
        var mia = r.body.data.mi_resena;
        if (mia) {
          karate.http(baseUrl + '/reviews/' + mia.id).header('Accept', 'application/json')
            .header('X-XSRF-TOKEN', S.viajero1.xsrf)
            .header('Cookie', 'access_token=' + S.viajero1.token + '; XSRF-TOKEN=' + S.viajero1.xsrf).delete();
        }
      }
      """
    * def repetir = function(n){ var s = ''; for (var i = 0; i < n; i++) s += 'a'; return s }

  @TRV-403 @TC-API-050
  Scenario: El resumen (promedio y total) coincide con el conjunto completo de reseñas
    * eval anonimo()
    # Se toma el servicio con reseñas más reciente para que el promedio no sea trivial
    Given path 'reviews'
    When method get
    Then status 200
    And match response.data == '#[_ > 0]'
    * def servicioId = response.data[0].servicio_id
    * def tipo = response.data[0].servicio_tipo
    Given path 'reviews'
    And params { servicio_id: '#(servicioId)', per_page: 50 }
    When method get
    Then status 200
    And assert responseTime < maxMs
    * def total = response.meta.total
    * def todas = response.data
    And match each todas == reviewSchema
    * def suma = 0
    * eval for (var i = 0; i < todas.length; i++) suma += todas[i].calificacion
    * def promedio = total == 0 ? 0 : Math.round(suma / total * 10) / 10
    Given path 'reviews', 'resumen'
    And param servicio_id = servicioId
    When method get
    Then status 200
    And match response.data contains { total: '#(total)', promedio: '#(promedio)' }
    * def dist = response.data.distribucion
    * match (dist['1'] + dist['2'] + dist['3'] + dist['4'] + dist['5']) == total
    # El mismo promedio aparece en la ficha del servicio
    Given path tipo == 'tour' ? 'tours' : 'hoteles', servicioId
    When method get
    Then status 200
    * def ficha = response.servicio || response.hotel
    And match ficha.calificacion.promedio == promedio

  @TRV-403 @TC-API-051
  Scenario: Con estadía finalizada publica (201); sin reserva completada recibe 403 con mensaje
    * eval limpiar(F.hotelQaId)
    * eval como(S.viajero1)
    Given path 'reviews'
    And request { servicio_id: '#(F.hotelQaId)', calificacion: 5, comentario: 'Excelente atención y habitación muy limpia.' }
    When method post
    Then status 201
    And assert responseTime < maxMs
    And match response.data == reviewSchema
    And match response.data contains { servicio_id: '#(F.hotelQaId)', calificacion: 5 }
    * def miId = response.data.id
    * eval como(S.viajero2)
    Given path 'reviews'
    And request { servicio_id: '#(F.hotelQaId)', calificacion: 4, comentario: 'Intento de reseña sin haberme hospedado.' }
    When method post
    Then status 403
    And match response.message contains 'reserva confirmada'
    * eval como(S.viajero1)
    Given path 'reviews', miId
    When method delete
    Then status 204

  @TRV-403 @TC-API-052
  Scenario Outline: Validación: <caso> devuelve 422 en <campo>
    * eval limpiar(F.tourQaId)
    * eval como(S.viajero1)
    * def comentario = <comentario>
    Given path 'reviews'
    And request { servicio_id: '#(F.tourQaId)', calificacion: <calificacion>, comentario: '#(comentario)' }
    When method post
    Then status 422
    And match response.errors.<campo> == '#[_ > 0]'

    Examples:
      | caso                      | campo        | calificacion | comentario      |
      | comentario de 9 letras    | comentario   | 4            | repetir(9)      |
      | comentario de 301 letras  | comentario   | 4            | repetir(301)    |
      | comentario vacío          | comentario   | 4            | ''              |
      | calificación 0            | calificacion | 0            | repetir(20)     |
      | calificación 6            | calificacion | 6            | repetir(20)     |
      | calificación decimal      | calificacion | 3.5          | repetir(20)     |

  @TRV-403 @TC-API-053
  Scenario: Editar o eliminar una reseña ajena → 403; una segunda reseña del mismo servicio → 409
    * eval limpiar(F.tourQaId)
    * eval como(S.viajero1)
    Given path 'reviews'
    And request { servicio_id: '#(F.tourQaId)', calificacion: 4, comentario: 'Buen guía, recorrido bien organizado.' }
    When method post
    Then status 201
    * def miId = response.data.id
    Given path 'reviews'
    And request { servicio_id: '#(F.tourQaId)', calificacion: 3, comentario: 'Segunda reseña del mismo tour.' }
    When method post
    Then status 409
    # El autor sí puede editar
    Given path 'reviews', miId
    And request { calificacion: 5, comentario: 'Actualizo: el mejor tour del viaje.' }
    When method put
    Then status 200
    And match response.data contains { calificacion: 5, comentario: 'Actualizo: el mejor tour del viaje.' }
    * eval como(S.viajero2)
    Given path 'reviews', miId
    And request { calificacion: 1, comentario: 'Intento de editar una reseña ajena.' }
    When method put
    Then status 403
    Given path 'reviews', miId
    When method delete
    Then status 403
    * eval como(S.viajero1)
    Given path 'reviews', miId
    When method delete
    Then status 204
