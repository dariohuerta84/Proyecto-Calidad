@proveedor
Feature: Gestión del proveedor: panel, hoteles, tours, salidas, actividades, reservas recibidas y eliminación (HU-P03/HU-P04 a HU-P15)

  Background:
    * url baseUrl
    * def hotelPropio = read('classpath:trouvels360/helpers/hotel-propio.feature')
    * def tourPropio = read('classpath:trouvels360/helpers/tour-propio.feature')
    * def reservarHotel = read('classpath:trouvels360/helpers/reservar-hotel.feature')
    * def reservarTour = read('classpath:trouvels360/helpers/reservar-tour.feature')
    * def leerSalida = read('classpath:trouvels360/helpers/salida.feature')
    * def servicioSchema = read('classpath:trouvels360/schemas/servicio-proveedor.json')
    * def metaSchema = read('classpath:trouvels360/schemas/meta.json')
    * def tipos = function(n1, c1, n2, c2){ var l = [{ nombre: n1, capacidad_adultos: 2, capacidad_ninos: 0, cantidad: c1, precio_por_noche: 150 }]; if (n2) l.push({ nombre: n2, capacidad_adultos: 2, capacidad_ninos: 0, cantidad: c2, precio_por_noche: 200 }); return l }
    * def solicitud = function(s, habId, dia, unidades){ return karate.call('classpath:trouvels360/helpers/reservar-hotel.feature', { sesion: s, fecha_inicio: fecha(dia), fecha_fin: fecha(dia + 2), items: [{ habitacion_id: habId, cantidad: unidades || 1 }] }) }
    * def tourBase = function(ciudad){ return { nombre: 'Tour ' + ciudad, descripcion: 'Tour creado por Karate', ciudad: ciudad, pais: 'Perú', imagen_url: 'https://example.com/t.jpg', categoria: 'Cultura', duracion: 90, precio: 80, salidas: [{ fecha: fecha(diaLibre()), hora: '09:00', cupo_total: 10 }] } }

  # ---------------------------------------------------------------- HU-P03/HU-P04 Panel
  @HU-P03 @TC-API-056
  Scenario: Paginación de mis publicaciones: meta coherente y la página 2 empieza en el registro 16
    * eval como(S.proveedor)
    Given path 'proveedor', 'servicios'
    When method get
    Then status 200
    * def faltan = 16 - response.meta.total
    * eval for (var i = 0; i < faltan; i++) karate.call('classpath:trouvels360/helpers/tour-propio.feature', { sesion: S.proveedor, cupo: 5 })
    * eval como(S.proveedor)
    Given path 'proveedor', 'servicios'
    And params { per_page: 15, sort: 'nombre' }
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.meta == metaSchema
    * def total = response.meta.total
    And match response.meta.last_page == Math.ceil(total / 15)
    And match response.data == '#[15]'
    And match each response.data == servicioSchema
    * def pag1 = karate.map(response.data, function(s){ return s.id })
    Given path 'proveedor', 'servicios'
    And params { per_page: 15, sort: 'nombre', page: 2 }
    When method get
    Then status 200
    And match response.meta.current_page == 2
    And match response.data == '#[_ > 0]'
    * def pag2 = karate.map(response.data, function(s){ return s.id })
    * def repetidos = karate.filter(pag2, function(x){ return pag1.indexOf(x) >= 0 })
    And match repetidos == []
    # El registro 16 (primero de la página 2) es el mismo que devuelve per_page=1&page=16
    Given path 'proveedor', 'servicios'
    And params { per_page: 1, sort: 'nombre', page: 16 }
    When method get
    Then match response.data[0].id == pag2[0]

  @HU-P04 @TC-API-057
  Scenario: Indicadores por estado tras crear reservas en los 4 estados
    * def p = call read('classpath:trouvels360/helpers/proveedor-nuevo.feature')
    * eval como(p.sesion)
    Given path 'proveedor', 'indicadores'
    When method get
    Then status 200
    And match response.data.reservas_hotel == { pendiente: 0, confirmada: 0, rechazada: 0, cancelada: 0 }
    * def h = call hotelPropio { sesion: '#(p.sesion)', habitaciones: '#(tipos("Doble", 5))' }
    * def habId = h.habitaciones[0].id
    * def dia = diaLibre()
    * def r1 = solicitud(S.viajero2, habId, dia)
    * def r2 = solicitud(S.viajero2, habId, dia)
    * def r3 = solicitud(S.viajero2, habId, dia)
    * def r4 = solicitud(S.viajero2, habId, dia)
    * eval como(p.sesion)
    Given path 'proveedor', 'reservas-habitaciones', r2.body.data.reservas[0].id, 'aceptar'
    When method post
    Then status 200
    Given path 'proveedor', 'reservas-habitaciones', r3.body.data.reservas[0].id, 'rechazar'
    And request { motivo: 'Habitación en mantenimiento esas fechas.' }
    When method post
    Then status 200
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', r4.body.data.reservas[0].id, 'cancelar'
    When method post
    Then status 200
    * eval como(p.sesion)
    Given path 'proveedor', 'indicadores'
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.data contains { publicaciones: 1, hoteles: 1, tours: 0, solicitudes_pendientes: 1 }
    And match response.data.reservas_hotel == { pendiente: 1, confirmada: 1, rechazada: 1, cancelada: 1 }
    And match response.data.ingresos_confirmados_total == r2.body.data.total
    # Los conteos por publicación del listado coinciden
    Given path 'proveedor', 'servicios'
    When method get
    Then match response.data[0].reservas_totales == { pendientes: 1, confirmadas: 1, rechazadas: 1, canceladas: 1 }

  @HU-P03 @TC-API-058
  Scenario: Un viajero no accede a las secciones del proveedor (403) y un anónimo recibe 401
    * eval como(S.viajero2)
    Given path 'proveedor', 'servicios'
    When method get
    Then status 403
    Given path 'proveedor', 'indicadores'
    When method get
    Then status 403
    Given path 'proveedor', 'reservas'
    When method get
    Then status 403
    * eval anonimo()
    Given path 'proveedor', 'servicios'
    When method get
    Then status 401

  # ---------------------------------------------------------------- HU-P05/HU-P06 Hotel
  @HU-P05 @TC-API-059
  Scenario: Publicar un hotel con dos tipos de habitación y encontrarlo en el catálogo
    * def ciudad = 'QA-' + sufijo()
    * eval como(S.proveedor)
    Given path 'hoteles'
    And request { nombre: '#("Hotel " + ciudad)', descripcion: 'Publicado por Karate', ciudad: '#(ciudad)', pais: 'Perú', imagen_url: 'https://example.com/h.jpg', direccion: 'Jr. Prueba 10', estrellas: 4, imagenes: [{ url: 'https://example.com/1.jpg', alt: 'Fachada' }, { url: 'https://example.com/2.jpg' }], habitaciones: '#(tipos("Estándar", 4, "Familiar", 2))' }
    When method post
    Then status 201
    And assert responseTime < maxMs
    * def id = response.data.servicio_id || response.data.id
    * eval anonimo()
    Given path 'hoteles'
    And param ciudad = ciudad
    When method get
    Then status 200
    And match response.meta.total == 1
    And match response.data[0] contains { id: '#(id)', nombre: '#("Hotel " + ciudad)' }
    Given path 'hoteles', id
    When method get
    Then status 200
    And match response.hotel contains { estrellas: 4, direccion: 'Jr. Prueba 10', proveedor_id: '#(S.proveedor.user.id)' }
    And match response.hotel.imagenes == '#[2]'
    * def nombres = karate.map(response.habitaciones, function(x){ return x.nombre })
    And match nombres contains only ['Estándar', 'Familiar']

  @HU-P05 @TC-API-060
  Scenario: Más de 5 imágenes en la galería devuelve 422
    * def imgs = karate.repeat(6, function(i){ return { url: 'https://example.com/' + i + '.jpg' } })
    * eval como(S.proveedor)
    Given path 'hoteles'
    And request { nombre: 'Hotel con muchas fotos', ciudad: 'QA-fotos', pais: 'Perú', imagen_url: 'https://example.com/h.jpg', direccion: 'Jr. Prueba 10', estrellas: 3, imagenes: '#(imgs)', habitaciones: '#(tipos("Doble", 1))' }
    When method post
    Then status 422
    And match response.errors.imagenes == '#[_ > 0]'

  @HU-P06 @TC-API-061
  Scenario: Editar el hotel de otro proveedor devuelve 403 y no cambia nada
    * def h = call hotelPropio { sesion: '#(S.proveedor)' }
    * eval como(S.proveedor2)
    Given path 'hoteles', h.hotelId
    And request { nombre: 'Hotel secuestrado' }
    When method put
    Then status 403
    Given path 'habitaciones', h.habitaciones[0].id
    And request { precio_por_noche: 1 }
    When method put
    Then status 403
    * eval anonimo()
    Given path 'hoteles', h.hotelId
    When method get
    Then match response.hotel.nombre == '#("Hotel " + h.ciudad)'
    And match response.habitaciones[0].precio_por_noche == 100

  # ---------------------------------------------------------------- HU-P07 Habitaciones
  @HU-P07 @TC-API-062
  Scenario: Reducir unidades por debajo de lo ocupado devuelve 409 con el mínimo permitido
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3))' }
    * def habId = h.habitaciones[0].id
    * def r = solicitud(S.viajero2, habId, diaLibre(), 2)
    * match r.status == 201
    * eval como(S.proveedor)
    Given path 'habitaciones', habId
    And request { cantidad: 1 }
    When method put
    Then status 409
    And match response.minimo == 2
    And match response.message contains 'Mínimo permitido: 2'
    Given path 'habitaciones', habId
    And request { cantidad: 2 }
    When method put
    Then status 200
    And match response.data.cantidad == 2

  @HU-P07 @TC-API-063
  Scenario: Eliminar un tipo de habitación con reservas activas → 409; sin reservas → 204
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3, "Suite", 1))' }
    * def doble = karate.filter(h.habitaciones, function(x){ return x.nombre == 'Doble' })[0].id
    * def suite = karate.filter(h.habitaciones, function(x){ return x.nombre == 'Suite' })[0].id
    * def r = solicitud(S.viajero2, doble, diaLibre())
    * eval como(S.proveedor)
    Given path 'habitaciones', doble
    When method delete
    Then status 409
    Given path 'habitaciones', suite
    When method delete
    Then status 204
    # Con un solo tipo restante tampoco se puede dejar el hotel sin habitaciones
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', r.body.data.reservas[0].id, 'cancelar'
    When method post
    Then status 200
    * eval como(S.proveedor)
    Given path 'habitaciones', doble
    When method delete
    Then status 409
    And match response.message contains 'al menos un tipo'

  @HU-P07 @TC-API-064
  Scenario: Cambiar la tarifa no altera el total de reservas ya hechas, solo las nuevas cotizaciones
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3))' }
    * def habId = h.habitaciones[0].id
    * def dia = diaLibre()
    * def r = solicitud(S.viajero2, habId, dia)
    * def totalOriginal = r.body.data.total
    * match totalOriginal == 354
    * eval como(S.proveedor)
    Given path 'habitaciones', habId
    And request { precio_por_noche: 300 }
    When method put
    Then status 200
    * eval como(S.viajero2)
    Given path 'mis-reservas', r.body.data.reservas[0].id
    When method get
    Then match response.data contains { total: '#(totalOriginal)', precio_noche: 150 }
    Given path 'reservas-habitaciones', 'cotizar'
    And request { fecha_inicio: '#(fecha(dia))', fecha_fin: '#(fecha(dia + 2))', items: [{ habitacion_id: '#(habId)', cantidad: 1 }] }
    When method post
    Then status 200
    And match response.data.total == 708

  # ---------------------------------------------------------------- HU-P08/HU-P09 Tour
  @HU-P08 @TC-API-065
  Scenario: Publicar un tour con salidas e ítems y encontrarlo en el catálogo
    * def ciudad = 'QA-' + sufijo()
    * def cuerpo = tourBase(ciudad)
    * set cuerpo.items = [{ nombre: 'Guía bilingüe', icono: '🗣️' }, { nombre: 'Almuerzo' }]
    * set cuerpo.categoria = 'Gastronomía'
    * eval como(S.proveedor)
    Given path 'tours'
    And request cuerpo
    When method post
    Then status 201
    And assert responseTime < maxMs
    * def id = response.data.servicio_id || response.data.id
    * eval anonimo()
    Given path 'tours'
    And params { destino: '#(ciudad)', categoria: 'Gastronomía' }
    When method get
    Then status 200
    And match response.meta.total == 1
    And match response.data[0] contains { id: '#(id)', tour: { servicio_id: '#(id)', categoria: 'Gastronomía', duracion: 90, precio: 80 } }
    Given path 'tours', id
    When method get
    Then match response.servicio.salidas == '#[1]'
    And match response.servicio.tour.items == '#[2]'

  @HU-P09 @TC-API-066
  Scenario: Editar el precio no cambia las salidas ni el total de reservas existentes
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 10, precio: 100 }
    * def rt = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 2 }
    * match rt.body.data.total == 236
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'salidas'
    When method get
    * def antes = karate.map(response, function(s){ return s.id })
    Given path 'tours', t.tourId
    And request { precio: 150 }
    When method put
    Then status 200
    Given path 'tours', t.tourId, 'salidas'
    When method get
    * def despues = karate.map(response, function(s){ return s.id })
    And match despues == antes
    * eval como(S.viajero2)
    Given path 'tours', 'mis-reservas'
    And param per_page = 50
    When method get
    * def mia = karate.filter(response.data, function(r){ return r.id == rt.body.data.id })[0]
    And match mia contains { total: 236, precio_unitario: 100 }

  @HU-P08 @HU-P09 @TC-API-067
  Scenario Outline: Límites del formulario de tour: <caso> → 422 en <campo>
    * def cuerpo = tourBase('QA-' + sufijo())
    * eval cuerpo['<clave>'] = <valor>
    * eval como(S.proveedor)
    Given path 'tours'
    And request cuerpo
    When method post
    Then status 422
    And match response.errors['<campo>'] == '#[_ > 0]'

    Examples:
      | caso                    | clave     | campo     | valor                                                                       |
      | 6 imágenes              | imagenes  | imagenes  | karate.repeat(6, function(i){ return { url: 'https://example.com/' + i } }) |
      | 11 ítems                | items     | items     | karate.repeat(11, function(i){ return { nombre: 'Ítem ' + i } })            |
      | categoría desconocida   | categoria | categoria | 'Deportes'                                                                  |
      | sin salidas             | salidas   | salidas   | []                                                                          |
      | precio negativo         | precio    | precio    | -1                                                                          |
      | duración cero           | duracion  | duracion  | 0                                                                           |

  # ---------------------------------------------------------------- HU-P10 Actividades
  @HU-P10 @TC-API-068
  Scenario: Dos actividades sin orden reciben 1 y 2; insertar con orden 1 desplaza a las demás
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'actividades'
    And request { titulo: 'Plaza de Armas' }
    When method post
    Then status 201
    And match response.orden == 1
    Given path 'tours', t.tourId, 'actividades'
    And request { titulo: 'Qorikancha', duracion_min: 60 }
    When method post
    Then status 201
    And match response.orden == 2
    Given path 'tours', t.tourId, 'actividades'
    And request { titulo: 'Desayuno de bienvenida', orden: 1 }
    When method post
    Then status 201
    Given path 'tours', t.tourId, 'actividades'
    When method get
    Then status 200
    * def lista = response.data || response
    * def titulos = karate.map(lista, function(a){ return a.titulo + '#' + a.orden })
    And match titulos == ['Desayuno de bienvenida#1', 'Plaza de Armas#2', 'Qorikancha#3']

  @HU-P10 @TC-API-069
  Scenario: Agregar una actividad o una salida en un tour ajeno devuelve 403
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * eval como(S.proveedor2)
    Given path 'tours', t.tourId, 'actividades'
    And request { titulo: 'Actividad intrusa' }
    When method post
    Then status 403
    Given path 'tours', t.tourId, 'salidas'
    And request { fecha: '#(fecha(diaLibre()))', hora: '10:00', cupo_total: 5 }
    When method post
    Then status 403

  # ---------------------------------------------------------------- HU-P11 Salidas
  @HU-P11 @TC-API-070
  Scenario: Salida duplicada (misma fecha y hora) → 409; fecha pasada → 422
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'salidas'
    And request { fecha: '#(t.fechaSalida)', hora: '08:00', cupo_total: 5 }
    When method post
    Then status 409
    Given path 'tours', t.tourId, 'salidas'
    And request { fecha: '#(fecha(-1))', hora: '08:00', cupo_total: 5 }
    When method post
    Then status 422
    And match response.errors.fecha == '#[_ > 0]'
    Given path 'tours', t.tourId, 'salidas'
    And request { fecha: '#(t.fechaSalida)', hora: '15:30', cupo_total: 5 }
    When method post
    Then status 201

  @HU-P11 @TC-API-071
  Scenario: cupo_total menor a lo reservado → 422; el cupo_reservado enviado por el cliente se ignora
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 10 }
    * def rt = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 4 }
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'salidas', t.salidaId
    And request { cupo_total: 3 }
    When method put
    Then status 422
    And match response.errors.cupo_total[0] contains '4'
    Given path 'tours', t.tourId, 'salidas', t.salidaId
    And request { cupo_total: 12, cupo_reservado: 0 }
    When method put
    Then status 200
    * def s = call leerSalida { sesion: '#(S.proveedor)', tourId: '#(t.tourId)', salidaId: '#(t.salidaId)' }
    * match s.salida contains { cupo_total: 12, cupo_reservado: 4 }

  @HU-P11 @TC-API-072
  Scenario: Una salida cerrada rechaza nuevas reservas y al reabrirla vuelve a aceptarlas
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 10 }
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'salidas', t.salidaId
    And request { estado: 'cerrada' }
    When method put
    Then status 200
    * def r1 = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 1 }
    * match r1.status == 409
    * match r1.body.estado == 'cerrada'
    * eval como(S.proveedor)
    Given path 'tours', t.tourId, 'salidas', t.salidaId
    And request { estado: 'programada' }
    When method put
    Then status 200
    * def r2 = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 1 }
    * match r2.status == 201

  # ---------------------------------------------------------------- HU-P12 Reservas recibidas
  @HU-P12 @TC-API-073
  Scenario: Reservas recibidas: solo de servicios propios, con datos del cliente
    * def p = call read('classpath:trouvels360/helpers/proveedor-nuevo.feature')
    * def propio = call hotelPropio { sesion: '#(p.sesion)', habitaciones: '#(tipos("Doble", 3))' }
    * def tour = call tourPropio { sesion: '#(p.sesion)', cupo: 5 }
    * def ajeno = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3))' }
    * def a = solicitud(S.viajero2, propio.habitaciones[0].id, diaLibre())
    * def b = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(tour.salidaId)', personas: 2 }
    * def c = solicitud(S.viajero2, ajeno.habitaciones[0].id, diaLibre())
    * eval como(p.sesion)
    Given path 'proveedor', 'reservas'
    When method get
    Then status 200
    And assert responseTime < maxMs
    And match response.meta.total == 2
    * def propios = [ '#(propio.hotelId)', '#(tour.tourId)' ]
    And match each response.data == '#? propios.indexOf(_.servicio.id) >= 0'
    * def correoCliente = S.viajero2.user.email
    And match each response.data == '#? _.cliente.email == correoCliente'
    Given path 'proveedor', 'reservas'
    And param tipo = 'tour'
    When method get
    Then match response.data == '#[1]'
    And match response.data[0] contains { tipo: 'tour', personas: 2 }

  @HU-P12 @TC-API-074
  Scenario: Filtro estado=pendiente devuelve solo pendientes (y primero las pendientes sin filtro)
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3))' }
    * def habId = h.habitaciones[0].id
    * def dia = diaLibre()
    * def a = solicitud(S.viajero2, habId, dia)
    * def b = solicitud(S.viajero2, habId, dia)
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas-habitaciones', a.body.data.reservas[0].id, 'aceptar'
    When method post
    Then status 200
    Given path 'proveedor', 'reservas'
    And params { estado: 'pendiente', servicio_id: '#(h.hotelId)' }
    When method get
    Then status 200
    And match response.data == '#[1]'
    And match each response.data contains { estado: 'pendiente' }
    And match response.data[0].id == b.body.data.reservas[0].id
    Given path 'proveedor', 'reservas'
    And params { estado: 'pendiente', per_page: 50 }
    When method get
    And match each response.data contains { estado: 'pendiente' }
    Given path 'proveedor', 'reservas'
    And params { servicio_id: '#(h.hotelId)' }
    When method get
    Then match response.data[0].estado == 'pendiente'
    And match response.data[1].estado == 'confirmada'

  @HU-P12 @TC-API-075
  Scenario: El detalle de una reserva de otro proveedor devuelve 404
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3))' }
    * def a = solicitud(S.viajero2, h.habitaciones[0].id, diaLibre())
    * def id = a.body.data.reservas[0].id
    * eval como(S.proveedor)
    Given path 'proveedor', 'reservas', 'hotel', id
    When method get
    Then status 200
    And match response.data.cliente == '#object'
    * eval como(S.proveedor2)
    Given path 'proveedor', 'reservas', 'hotel', id
    When method get
    Then status 404

  # ---------------------------------------------------------------- HU-P15 Eliminar
  @HU-P15 @TC-API-076
  Scenario: Eliminar la publicación de otro proveedor devuelve 403 y sigue en el catálogo
    * def h = call hotelPropio { sesion: '#(S.proveedor)' }
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * eval como(S.proveedor2)
    Given path 'hoteles', h.hotelId
    When method delete
    Then status 403
    Given path 'tours', t.tourId
    When method delete
    Then status 403
    Given path 'servicios', h.hotelId
    When method delete
    Then status 403
    * eval como(S.viajero2)
    Given path 'servicios', t.tourId
    When method delete
    Then status 403
    * eval anonimo()
    Given path 'hoteles', h.hotelId
    When method get
    Then status 200

  @HU-P15 @TC-API-077
  Scenario: Con una reserva activa (pendiente o confirmada) no se puede eliminar: 409
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3))' }
    * def a = solicitud(S.viajero2, h.habitaciones[0].id, diaLibre())
    * def t = call tourPropio { sesion: '#(S.proveedor)', cupo: 5 }
    * def b = call reservarTour { sesion: '#(S.viajero2)', salidaId: '#(t.salidaId)', personas: 1 }
    * eval como(S.proveedor)
    Given path 'hoteles', h.hotelId
    When method delete
    Then status 409
    And match response.message contains '1 reserva'
    Given path 'tours', t.tourId
    When method delete
    Then status 409

  @HU-P15 @TC-API-078
  Scenario: Tras eliminar la publicación, el viajero conserva su historial con el nombre del servicio
    * def h = call hotelPropio { sesion: '#(S.proveedor)', habitaciones: '#(tipos("Doble", 3))' }
    * def a = solicitud(S.viajero2, h.habitaciones[0].id, diaLibre())
    * def id = a.body.data.reservas[0].id
    * eval como(S.viajero2)
    Given path 'reservas-habitaciones', id, 'cancelar'
    When method post
    Then status 200
    * eval como(S.proveedor)
    Given path 'hoteles', h.hotelId
    When method delete
    Then status 200
    And match response.message == 'Publicación eliminada.'
    * eval anonimo()
    Given path 'hoteles', h.hotelId
    When method get
    Then status 404
    * eval como(S.viajero2)
    Given path 'mis-reservas', id
    When method get
    Then status 200
    And match response.data.servicio contains { id: '#(h.hotelId)', nombre: '#("Hotel " + h.ciudad)', eliminado: true }
    Given path 'mis-reservas'
    When method get
    Then status 200
