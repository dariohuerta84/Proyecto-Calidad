# Matriz de trazabilidad — Trouvels360

- 30 historias · 130 criterios (130 con caso) · 160 casos · automatizados con ubicación 157/157 + 3 manuales
- Último resultado: Pasó: 160

| Herramienta | Pasó | Falló | No ejecutado |
|---|---|---|---|
| Karate | 86 | 0 | 0 |
| Selenium | 33 | 0 | 0 |
| Appium | 32 | 0 | 0 |
| JMeter | 6 | 0 | 0 |
| Manual | 3 | 0 | 0 |

## HU-V01 — Crear mi cuenta de viajero

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-001 Registro viajero válido devuelve 201 y esquema de usuario sin password | Karate | CA-V01-01 Registro exitoso | `qa/karate/src\test\java\trouvels360\auth.feature:9` | Pasó |
| TC-WEB-001 Registro de viajero end-to-end y aterrizaje en catálogo | Selenium | CA-V01-01 Registro exitoso | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:27` | Pasó |
| TC-MOB-001 Registro de viajero desde la app y sesión iniciada | Appium | CA-V01-01 Registro exitoso | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:23` | Pasó |
| TC-API-002 Data-driven (CSV) de campos inválidos devuelve 422 con error por campo | Karate | CA-V01-02 Datos incompletos o inválidos<br>CA-V01-03 Contraseñas distintas | `qa/karate/src\test\java\trouvels360\auth.feature:18` | Pasó |
| TC-WEB-002 Mensajes de validación en el formulario | Selenium | CA-V01-02 Datos incompletos o inválidos<br>CA-V01-03 Contraseñas distintas | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:46` | Pasó |
| TC-MOB-002 Botón Registrarme deshabilitado con contraseñas distintas | Appium | CA-V01-03 Contraseñas distintas | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:41` | Pasó |
| TC-API-003 Correo duplicado en mayúsculas devuelve 422 | Karate | CA-V01-04 Correo ya registrado | `qa/karate/src\test\java\trouvels360\auth.feature:33` | Pasó |
| TC-API-004 Rol 'admin' devuelve 422 | Karate | CA-V01-05 Rol no permitido | `qa/karate/src\test\java\trouvels360\auth.feature:43` | Pasó |

## HU-V02 — Iniciar sesión y volver a lo que estaba haciendo

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-008 Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | Karate | CA-V02-01 Ingreso correcto | `qa/karate/src\test\java\trouvels360\auth.feature:89` | Pasó |
| TC-WEB-004 Login viajero y proveedor muestran el menú de su rol | Selenium | CA-V02-01 Ingreso correcto | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:89` | Pasó |
| TC-MOB-004 Login, cierre de sesión y bloqueo de pestañas privadas | Appium | CA-V02-01 Ingreso correcto<br>CA-V02-03 Datos incorrectos<br>CA-V02-05 Cerrar sesión de verdad | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:70` | Pasó |
| TC-WEB-005 Login desde 'Reservar' vuelve al detalle con la selección | Selenium | CA-V02-02 Vuelvo a mi reserva | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:114` | Pasó |
| TC-MOB-005 Reservar sin sesión → login → regreso al detalle | Appium | CA-V02-02 Vuelvo a mi reserva | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:95` | Pasó |
| TC-API-010 Credenciales inválidas 401 y faltantes 422 | Karate | CA-V02-03 Datos incorrectos | `qa/karate/src\test\java\trouvels360\auth.feature:114` | Pasó |
| TC-WEB-033 Sesión vencida: 401, login y regreso a la página | Selenium | CA-V02-04 Sesión vencida | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\CoberturaAdicionalTest.java:80` | Pasó |
| TC-API-011 Token reutilizado tras logout devuelve 401 | Karate | CA-V02-04 Sesión vencida<br>CA-V02-05 Cerrar sesión de verdad | `qa/karate/src\test\java\trouvels360\auth.feature:129` | Pasó |
| TC-API-009 POST sin X-XSRF-TOKEN devuelve 403 | Karate | CA-V02-06 Protección contra peticiones falsificadas | `qa/karate/src\test\java\trouvels360\auth.feature:107` | Pasó |
| TC-PERF-001 Login con 50 usuarios concurrentes desde CSV: p95 < 800 ms, 0% errores | JMeter | CA-V02-07 Login con muchos usuarios a la vez | `qa/jmeter/trouvels360.jmx` | Pasó |

## HU-V03 — Actualizar mis datos y mi contraseña

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-013 PATCH /usuarios/me actualiza y GET /auth/me devuelve lo mismo | Karate | CA-V03-01 Guardar mis datos | `qa/karate/src\test\java\trouvels360\cuenta.feature:10` | Pasó |
| TC-WEB-006 Editar perfil y ver el nombre actualizado en el menú | Selenium | CA-V03-01 Guardar mis datos | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:136` | Pasó |
| TC-MOB-006 Editar perfil en la app y verificarlo en la pestaña Perfil | Appium | CA-V03-01 Guardar mis datos | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:114` | Pasó |
| TC-API-014 Avatar y teléfono inválidos devuelven 422 | Karate | CA-V03-02 Datos inválidos | `qa/karate/src\test\java\trouvels360\cuenta.feature:24` | Pasó |
| TC-API-015 Enviar rol devuelve 422 | Karate | CA-V03-03 No puedo cambiar mi rol | `qa/karate/src\test\java\trouvels360\cuenta.feature:39` | Pasó |
| TC-API-016 Cambio de contraseña: login con nueva 200 y con anterior 401 | Karate | CA-V03-04 Cambiar la contraseña<br>CA-V03-05 Contraseña actual incorrecta | `qa/karate/src\test\java\trouvels360\cuenta.feature:51` | Pasó |
| TC-MOB-007 Cambio de contraseña con confirmación distinta deshabilita Guardar | Appium | CA-V03-04 Cambiar la contraseña | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:130` | Pasó |
| TC-MAN-001 Error de servidor al guardar el perfil: mensaje con reintentar y sin éxito falso | Manual | CA-V03-06 Error del servidor | `manual` | Pasó |

## HU-V04 — Eliminar mi cuenta

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-WEB-007 Diálogo de confirmación: cancelar conserva la cuenta | Selenium | CA-V04-01 Me arrepiento | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:150` | Pasó |
| TC-API-018 DELETE sin reservas devuelve 200 y login posterior 401 | Karate | CA-V04-02 Eliminación permitida | `qa/karate/src\test\java\trouvels360\cuenta.feature:78` | Pasó |
| TC-MOB-008 Eliminar cuenta desde Perfil y volver a la pantalla de login | Appium | CA-V04-02 Eliminación permitida | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:151` | Pasó |
| TC-API-017 DELETE con reserva activa devuelve 409 | Karate | CA-V04-03 Bloqueo por reservas activas | `qa/karate/src\test\java\trouvels360\cuenta.feature:65` | Pasó |
| TC-API-019 Reservas de la cuenta eliminada siguen visibles para el proveedor | Karate | CA-V04-04 El historial se conserva | `qa/karate/src\test\java\trouvels360\cuenta.feature:89` | Pasó |

## HU-V05 — Buscar hoteles disponibles

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-020 Búsqueda válida: todos los resultados cumplen ciudad, capacidad y unidades (match each) | Karate | CA-V05-01 Búsqueda válida | `qa/karate/src\test\java\trouvels360\hoteles.feature:13` | Pasó |
| TC-WEB-008 Búsqueda en Cusco y validación de tarjetas de resultado | Selenium | CA-V05-01 Búsqueda válida | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\HotelesTest.java:30` | Pasó |
| TC-MOB-009 Búsqueda por ciudad y fechas en la app | Appium | CA-V05-01 Búsqueda válida | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\HotelesMovilTest.java:29` | Pasó |
| TC-API-022 Hotel con unidades ocupadas por reservas pendientes no aparece | Karate | CA-V05-02 Las solicitudes pendientes ocupan | `qa/karate/src\test\java\trouvels360\hoteles.feature:41` | Pasó |
| TC-API-021 Data-driven de fechas y cantidades inválidas devuelve 422 | Karate | CA-V05-03 Fechas inválidas<br>CA-V05-04 Cantidades fuera de rango | `qa/karate/src\test\java\trouvels360\hoteles.feature:29` | Pasó |
| TC-WEB-009 Validación de fechas pasadas en el buscador | Selenium | CA-V05-03 Fechas inválidas | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\HotelesTest.java:50` | Pasó |
| TC-MOB-010 Estado vacío vs error con Reintentar | Appium | CA-V05-05 Sin resultados no es lo mismo que un error | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\HotelesMovilTest.java:42` | Pasó |
| TC-WEB-031 Volver desde el detalle conserva la búsqueda (hoteles y tours) | Selenium | CA-V05-06 Conservo mi búsqueda | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\CoberturaAdicionalTest.java:28` | Pasó |
| TC-PERF-002 Carga de catálogo: 100 usuarios, ramp-up 60 s, ciudades desde CSV, p95 < 1 s | JMeter | CA-V05-07 Catálogo con mucho tráfico | `qa/jmeter/trouvels360.jmx` | Pasó |

## HU-V06 — Ver un hotel y cotizar mi estadía

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-023 Esquema JSON del detalle de hotel (habitaciones, calificación, reseñas) | Karate | CA-V06-01 Detalle completo | `qa/karate/src\test\java\trouvels360\hoteles.feature:61` | Pasó |
| TC-WEB-010 Selección de 2 tipos y verificación del resumen con IGV | Selenium | CA-V06-02 Elegir varios tipos<br>CA-V06-03 Cálculo con IGV | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\HotelesTest.java:66` | Pasó |
| TC-MOB-011 Selección de habitaciones y total con IGV en la app | Appium | CA-V06-02 Elegir varios tipos<br>CA-V06-03 Cálculo con IGV | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\HotelesMovilTest.java:71` | Pasó |
| TC-API-024 Cotización data-driven (CSV) con tarifas, noches y unidades → subtotal, igv, total exactos | Karate | CA-V06-03 Cálculo con IGV | `qa/karate/src\test\java\trouvels360\hoteles.feature:76` | Pasó |
| TC-API-025 Cotizar más unidades que las libres devuelve 422 | Karate | CA-V06-04 No puedo pedir más de lo disponible | `qa/karate/src\test\java\trouvels360\hoteles.feature:94` | Pasó |
| TC-MOB-012 Botón + deshabilitado al llegar a las unidades disponibles | Appium | CA-V06-04 No puedo pedir más de lo disponible | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\HotelesMovilTest.java:88` | Pasó |
| TC-API-079 Cambiar la fecha de salida recalcula el total y no crea reserva | Karate | CA-V06-05 Cotizar no reserva | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:13` | Pasó |

## HU-V07 — Reservar un hotel con pago simulado

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-026 Reserva válida 201: estado pendiente, código único, subtotal/igv/total = cotización | Karate | CA-V07-01 Solicitud registrada | `qa/karate/src\test\java\trouvels360\reservas-hotel.feature:11` | Pasó |
| TC-WEB-011 Flujo completo búsqueda → selección → pago → comprobante PENDIENTE | Selenium | CA-V07-01 Solicitud registrada | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\HotelesTest.java:83` | Pasó |
| TC-MOB-013 Flujo completo de reserva de hotel en la app | Appium | CA-V07-01 Solicitud registrada | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\HotelesMovilTest.java:102` | Pasó |
| TC-API-027 Data-driven de tarjetas inválidas (Luhn, vencida, CVC) → 422 sin reserva creada | Karate | CA-V07-02 Pago inválido | `qa/karate/src\test\java\trouvels360\reservas-hotel.feature:28` | Pasó |
| TC-WEB-012 Validaciones del formulario de pago | Selenium | CA-V07-02 Pago inválido | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\HotelesTest.java:114` | Pasó |
| TC-MOB-014 Formulario de pago rechaza tarjeta vencida | Appium | CA-V07-02 Pago inválido | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\HotelesMovilTest.java:126` | Pasó |
| TC-API-028 Disponibilidad tras reservar descuenta las unidades | Karate | CA-V07-03 Mis habitaciones quedan apartadas | `qa/karate/src\test\java\trouvels360\reservas-hotel.feature:52` | Pasó |
| TC-API-029 Varios tipos con uno sin stock → 409 y ninguno registrado | Karate | CA-V07-04 Se acabaron mientras pagaba | `qa/karate/src\test\java\trouvels360\reservas-hotel.feature:70` | Pasó |
| TC-PERF-003 20 hilos simultáneos (Synchronizing Timer) por la última unidad: exactamente 1×201 y 19×409 | JMeter | CA-V07-05 Nunca se vende de más | `qa/jmeter/trouvels360.jmx` | Pasó |
| TC-API-030 Mismo Idempotency-Key devuelve la misma solicitud | Karate | CA-V07-06 Sin duplicados al reintentar | `qa/karate/src\test\java\trouvels360\reservas-hotel.feature:87` | Pasó |

## HU-V08 — Buscar tours

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-036 Filtros combinados: todos los resultados cumplen destino y categoría (match each) | Karate | CA-V08-01 Búsqueda válida | `qa/karate/src\test\java\trouvels360\tours.feature:10` | Pasó |
| TC-WEB-015 Filtrar por categoría y destino | Selenium | CA-V08-01 Búsqueda válida | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ToursTest.java:28` | Pasó |
| TC-MOB-017 Chips de categoría y búsqueda por destino | Appium | CA-V08-01 Búsqueda válida | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ToursMovilTest.java:27` | Pasó |
| TC-API-037 Data-driven de filtros inválidos → 422 | Karate | CA-V08-02 Filtros inválidos | `qa/karate/src\test\java\trouvels360\tours.feature:29` | Pasó |
| TC-WEB-032 Sin resultados vs. error con Reintentar (tours y bandeja del proveedor) | Selenium | CA-V08-03 Sin resultados no es lo mismo que un error | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\CoberturaAdicionalTest.java:59` | Pasó |
| TC-WEB-031 Volver desde el detalle conserva la búsqueda (hoteles y tours) | Selenium | CA-V08-04 Conservo mi búsqueda | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\CoberturaAdicionalTest.java:28` | Pasó |
| TC-PERF-004 Carga de búsqueda de tours: 100 usuarios, categorías desde CSV, p95 < 1 s | JMeter | CA-V08-05 Búsqueda con mucho tráfico | `qa/jmeter/trouvels360.jmx` | Pasó |

## HU-V09 — Ver un tour y cotizar

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-038 Esquema del detalle; ninguna salida devuelta es pasada ni no programada | Karate | CA-V09-01 Detalle | `qa/karate/src\test\java\trouvels360\tours.feature:41` | Pasó |
| TC-API-039 Cotización de tour data-driven (precio × personas + 18%) | Karate | CA-V09-02 Cálculo con IGV | `qa/karate/src\test\java\trouvels360\tours.feature:62` | Pasó |
| TC-WEB-016 Elegir salida y personas, verificar total con IGV | Selenium | CA-V09-02 Cálculo con IGV | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ToursTest.java:41` | Pasó |
| TC-MOB-018 Elegir salida y personas en la app; + se deshabilita al tope de cupos | Appium | CA-V09-02 Cálculo con IGV<br>CA-V09-03 Selección no válida | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ToursMovilTest.java:50` | Pasó |
| TC-API-040 Cotizar más personas que el cupo → 422 | Karate | CA-V09-03 Selección no válida | `qa/karate/src\test\java\trouvels360\tours.feature:76` | Pasó |
| TC-API-080 Tour sin salidas disponibles: detalle sin salidas y cotizar 409 | Karate | CA-V09-04 Tour sin salidas | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:35` | Pasó |

## HU-V10 — Reservar un tour

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-041 Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | Karate | CA-V10-01 Reserva confirmada | `qa/karate/src\test\java\trouvels360\reservas-tour.feature:11` | Pasó |
| TC-WEB-017 Reserva de tour end-to-end con comprobante CONFIRMADA | Selenium | CA-V10-01 Reserva confirmada | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ToursTest.java:56` | Pasó |
| TC-MOB-019 Reserva de tour end-to-end en la app | Appium | CA-V10-01 Reserva confirmada | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ToursMovilTest.java:65` | Pasó |
| TC-API-081 Pago de tour inválido (CSV) no crea reserva ni descuenta cupos | Karate | CA-V10-02 Pago inválido o desisto | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:53` | Pasó |
| TC-API-042 Salida cerrada / pasada / sin cupo → 409 o 422 y cupos sin cambio | Karate | CA-V10-03 Salida no disponible | `qa/karate/src\test\java\trouvels360\reservas-tour.feature:27` | Pasó |
| TC-PERF-005 30 hilos simultáneos sobre salida de 10 cupos: suma de personas confirmadas <= 10 | JMeter | CA-V10-04 Nunca se venden cupos de más | `qa/jmeter/trouvels360.jmx` | Pasó |
| TC-API-043 Idempotencia: dos envíos, una sola reserva y un solo descuento | Karate | CA-V10-05 Sin duplicados al reintentar | `qa/karate/src\test\java\trouvels360\reservas-tour.feature:47` | Pasó |

## HU-V11 — Ver mis reservas

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-044 Esquema paginado (data, meta.total, meta.last_page) y solo reservas propias | Karate | CA-V11-01 Solo las mías, con todos sus datos<br>CA-V11-02 Lo mismo en la web y en la app | `qa/karate/src\test\java\trouvels360\mis-reservas.feature:20` | Pasó |
| TC-WEB-018 Mis reservas: pestañas hotel/tour, filtro por estado y paginación | Selenium | CA-V11-01 Solo las mías, con todos sus datos<br>CA-V11-02 Lo mismo en la web y en la app | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\MisReservasTest.java:26` | Pasó |
| TC-MOB-020 Mis reservas en la app coincide con la web (mismo código y total) | Appium | CA-V11-02 Lo mismo en la web y en la app | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ReservasMovilTest.java:25` | Pasó |
| TC-MOB-021 Scroll infinito / paginación de reservas | Appium | CA-V11-02 Lo mismo en la web y en la app | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ReservasMovilTest.java:45` | Pasó |
| TC-API-046 Reserva ajena → 404 | Karate | CA-V11-03 Vacío, error y reservas ajenas | `qa/karate/src\test\java\trouvels360\mis-reservas.feature:73` | Pasó |

## HU-V12 — Saber si el hotel aceptó o rechazó mi solicitud

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-045 Estado tras aceptar y tras rechazar (con motivo) | Karate | CA-V12-01 Pendiente o confirmada<br>CA-V12-02 Rechazada con motivo | `qa/karate/src\test\java\trouvels360\mis-reservas.feature:49` | Pasó |
| TC-API-031 Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | Karate | CA-V12-01 Pendiente o confirmada | `qa/karate/src\test\java\trouvels360\aprobacion.feature:14` | Pasó |
| TC-WEB-013 Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | Selenium | CA-V12-01 Pendiente o confirmada | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AprobacionTest.java:26` | Pasó |
| TC-WEB-019 Reserva rechazada muestra motivo | Selenium | CA-V12-02 Rechazada con motivo | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\MisReservasTest.java:59` | Pasó |
| TC-WEB-014 Proveedor rechaza con motivo y el viajero ve RECHAZADA | Selenium | CA-V12-02 Rechazada con motivo | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AprobacionTest.java:55` | Pasó |
| TC-API-041 Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | Karate | CA-V12-03 Los tours no esperan aprobación | `qa/karate/src\test\java\trouvels360\reservas-tour.feature:11` | Pasó |
| TC-API-082 Salida cancelada por el proveedor: reserva confirmada con aviso | Karate | CA-V12-04 Salida cancelada por el proveedor | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:69` | Pasó |

## HU-V13 — Cancelar una reserva

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-WEB-020 Cancelar desde Mis reservas con diálogo de confirmación | Selenium | CA-V13-01 Me arrepiento<br>CA-V13-02 Cancelación exitosa | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\MisReservasTest.java:76` | Pasó |
| TC-API-047 Cancelar hotel: estado cancelada y unidades liberadas | Karate | CA-V13-02 Cancelación exitosa | `qa/karate/src\test\java\trouvels360\mis-reservas.feature:85` | Pasó |
| TC-MOB-022 Cancelar desde la app y ver estado CANCELADA | Appium | CA-V13-02 Cancelación exitosa | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ReservasMovilTest.java:69` | Pasó |
| TC-API-048 Cancelar tour devuelve exactamente las personas al cupo | Karate | CA-V13-03 El tour devuelve los cupos exactos | `qa/karate/src\test\java\trouvels360\mis-reservas.feature:98` | Pasó |
| TC-API-049 Cancelar dos veces → 409; cancelar rechazada → 409; ajena → 404 | Karate | CA-V13-04 Reserva no cancelable<br>CA-V13-05 Se cortó la conexión | `qa/karate/src\test\java\trouvels360\mis-reservas.feature:113` | Pasó |
| TC-MAN-002 Corte de conexión al cancelar: al refrescar se ve el estado real | Manual | CA-V13-05 Se cortó la conexión | `manual` | Pasó |

## HU-V14 — Leer y escribir reseñas

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-050 Resumen: promedio y total coinciden con el conjunto completo | Karate | CA-V14-01 Leer opiniones | `qa/karate/src\test\java\trouvels360\resenas.feature:27` | Pasó |
| TC-API-051 Publicar con reserva finalizada 201; sin reserva 403 con mensaje | Karate | CA-V14-02 Publicar mi reseña<br>CA-V14-03 Reseña no permitida | `qa/karate/src\test\java\trouvels360\resenas.feature:62` | Pasó |
| TC-WEB-021 Publicar, editar y eliminar reseña en el detalle | Selenium | CA-V14-02 Publicar mi reseña<br>CA-V14-04 Editar o borrar solo la mía | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ResenasItinerarioTest.java:30` | Pasó |
| TC-MOB-023 Publicar reseña desde la app | Appium | CA-V14-02 Publicar mi reseña | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ResenasItinerarioMovilTest.java:21` | Pasó |
| TC-API-052 Data-driven de longitudes de comentario y calificaciones fuera de rango → 422 | Karate | CA-V14-03 Reseña no permitida | `qa/karate/src\test\java\trouvels360\resenas.feature:85` | Pasó |
| TC-API-053 Editar/eliminar reseña ajena → 403; segunda reseña → 409 | Karate | CA-V14-04 Editar o borrar solo la mía<br>CA-V14-05 Una reseña por servicio | `qa/karate/src\test\java\trouvels360\resenas.feature:105` | Pasó |

## HU-V15 — Recibir un itinerario sugerido

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-054 Itinerario válido: esquema de días y actividades | Karate | CA-V15-01 Propuesta generada | `qa/karate/src\test\java\trouvels360\itinerario.feature:11` | Pasó |
| TC-WEB-022 Generar itinerario desde el detalle del hotel | Selenium | CA-V15-01 Propuesta generada<br>CA-V15-02 Es una sugerencia, no una reserva | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ResenasItinerarioTest.java:60` | Pasó |
| TC-MOB-024 Generar itinerario en la app y continuar con la reserva | Appium | CA-V15-01 Propuesta generada<br>CA-V15-03 Sigo con mi reserva | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ResenasItinerarioMovilTest.java:46` | Pasó |
| TC-API-055 Interés inválido → 422 | Karate | CA-V15-04 Datos inválidos | `qa/karate/src\test\java\trouvels360\itinerario.feature:31` | Pasó |
| TC-MAN-003 Microservicio de itinerarios caído: aviso con Reintentar y se puede seguir con el hotel | Manual | CA-V15-05 Servicio de itinerarios caído | `manual` | Pasó |

## HU-P01 — Crear mi cuenta de proveedor

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-005 Registro proveedor normaliza '987654321' a '+51 987654321' | Karate | CA-P01-01 Registro exitoso | `qa/karate/src\test\java\trouvels360\auth.feature:53` | Pasó |
| TC-WEB-003 Registro de proveedor y acceso al panel del proveedor | Selenium | CA-P01-01 Registro exitoso | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:73` | Pasó |
| TC-MOB-003 Registro de proveedor desde la app y acceso al panel | Appium | CA-P01-01 Registro exitoso | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:55` | Pasó |
| TC-API-006 Data-driven de RUC inválidos y duplicado (422) | Karate | CA-P01-02 RUC inválido o repetido | `qa/karate/src\test\java\trouvels360\auth.feature:62` | Pasó |
| TC-API-007 Teléfonos inválidos devuelven 422 con mensaje | Karate | CA-P01-03 Celular inválido | `qa/karate/src\test\java\trouvels360\auth.feature:76` | Pasó |
| TC-API-003 Correo duplicado en mayúsculas devuelve 422 | Karate | CA-P01-04 Correo repetido o contraseñas distintas | `qa/karate/src\test\java\trouvels360\auth.feature:33` | Pasó |
| TC-WEB-002 Mensajes de validación en el formulario | Selenium | CA-P01-04 Correo repetido o contraseñas distintas | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:46` | Pasó |
| TC-MOB-002 Botón Registrarme deshabilitado con contraseñas distintas | Appium | CA-P01-04 Correo repetido o contraseñas distintas | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\CuentaMovilTest.java:41` | Pasó |

## HU-P02 — Entrar a mi panel y mantener los datos de mi empresa

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-008 Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | Karate | CA-P02-01 Llego a mi panel | `qa/karate/src\test\java\trouvels360\auth.feature:89` | Pasó |
| TC-WEB-004 Login viajero y proveedor muestran el menú de su rol | Selenium | CA-P02-01 Llego a mi panel<br>CA-P02-02 El panel es solo para proveedores | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AutenticacionTest.java:89` | Pasó |
| TC-API-012 Viajero en ruta de proveedor devuelve 403 | Karate | CA-P02-02 El panel es solo para proveedores | `qa/karate/src\test\java\trouvels360\auth.feature:145` | Pasó |
| TC-API-014 Avatar y teléfono inválidos devuelven 422 | Karate | CA-P02-03 Celular de la empresa inválido | `qa/karate/src\test\java\trouvels360\cuenta.feature:24` | Pasó |

## HU-P03 — Ver mis publicaciones

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-056 Paginación: meta.total, last_page y página 2 con los registros 16+ | Karate | CA-P03-01 Solo las mías, por páginas | `qa/karate/src\test\java\trouvels360\proveedor.feature:19` | Pasó |
| TC-WEB-023 Panel: paginación y tarjetas de indicadores | Selenium | CA-P03-01 Solo las mías, por páginas | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:29` | Pasó |
| TC-MOB-025 Panel del proveedor en la app con indicadores | Appium | CA-P03-01 Solo las mías, por páginas | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:32` | Pasó |
| TC-API-058 Viajero en /proveedor/servicios → 403 | Karate | CA-P03-02 Panel vacío o acceso no autorizado | `qa/karate/src\test\java\trouvels360\proveedor.feature:94` | Pasó |
| TC-PERF-006 Panel del proveedor con 30 usuarios concurrentes, p95 < 1,5 s | JMeter | CA-P03-03 Panel con muchos proveedores a la vez | `qa/jmeter/trouvels360.jmx` | Pasó |

## HU-P04 — Ver los indicadores de mi negocio

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-057 Indicadores por estado tras crear reservas en 4 estados | Karate | CA-P04-01 Cada estado por separado<br>CA-P04-03 Los números se actualizan | `qa/karate/src\test\java\trouvels360\proveedor.feature:54` | Pasó |
| TC-WEB-023 Panel: paginación y tarjetas de indicadores | Selenium | CA-P04-01 Cada estado por separado | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:29` | Pasó |
| TC-MOB-025 Panel del proveedor en la app con indicadores | Appium | CA-P04-01 Cada estado por separado | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:32` | Pasó |
| TC-API-050 Resumen: promedio y total coinciden con el conjunto completo | Karate | CA-P04-02 Reseñas sobre el total | `qa/karate/src\test\java\trouvels360\resenas.feature:27` | Pasó |

## HU-P05 — Publicar un hotel

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-059 Crear hotel con habitaciones y verificar en el catálogo | Karate | CA-P05-01 Hotel publicado | `qa/karate/src\test\java\trouvels360\proveedor.feature:112` | Pasó |
| TC-WEB-024 Crear hotel desde el formulario del proveedor | Selenium | CA-P05-01 Hotel publicado | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:56` | Pasó |
| TC-MOB-026 Crear hotel desde la app del proveedor | Appium | CA-P05-01 Hotel publicado | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:51` | Pasó |
| TC-API-060 6 imágenes → 422 | Karate | CA-P05-02 Máximo 5 imágenes | `qa/karate/src\test\java\trouvels360\proveedor.feature:137` | Pasó |
| TC-API-084 Tipo de habitación inválido: no queda hotel a medias | Karate | CA-P05-03 Todo o nada | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:109` | Pasó |

## HU-P06 — Editar un hotel

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-083 Editar descripción e imágenes conserva id y reservas | Karate | CA-P06-01 Cambios sin afectar reservas | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:86` | Pasó |
| TC-API-061 Editar hotel ajeno → 403 | Karate | CA-P06-02 Datos inválidos o hotel ajeno | `qa/karate/src\test\java\trouvels360\proveedor.feature:147` | Pasó |

## HU-P07 — Gestionar los tipos de habitación

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-WEB-025 Agregar y editar tipo de habitación | Selenium | CA-P07-01 Agregar o editar | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:75` | Pasó |
| TC-MOB-027 Agregar tipo de habitación en la app | Appium | CA-P07-01 Agregar o editar | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:72` | Pasó |
| TC-API-062 Reducir unidades bajo ocupación → 409 con mínimo | Karate | CA-P07-02 No bajar de lo reservado | `qa/karate/src\test\java\trouvels360\proveedor.feature:166` | Pasó |
| TC-API-063 Eliminar con reservas → 409; sin reservas → 204 | Karate | CA-P07-03 Eliminar solo si no hay reservas | `qa/karate/src\test\java\trouvels360\proveedor.feature:185` | Pasó |
| TC-API-064 Cambio de tarifa no altera total de reservas previas | Karate | CA-P07-04 El precio de reservas pasadas no cambia | `qa/karate/src\test\java\trouvels360\proveedor.feature:209` | Pasó |
| TC-API-085 Tarifa 0 → 422 y tipo ajeno → 403 sin cambios | Karate | CA-P07-05 Datos inválidos o tipo ajeno | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:129` | Pasó |

## HU-P08 — Publicar un tour

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-065 Crear tour y verificar en catálogo | Karate | CA-P08-01 Tour publicado | `qa/karate/src\test\java\trouvels360\proveedor.feature:233` | Pasó |
| TC-WEB-026 Crear tour con salida desde el formulario | Selenium | CA-P08-01 Tour publicado | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:96` | Pasó |
| TC-MOB-028 Crear tour desde la app | Appium | CA-P08-01 Tour publicado | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:94` | Pasó |
| TC-API-067 Data-driven de límites (imágenes, items, categoría) → 422 | Karate | CA-P08-02 Se respetan los límites | `qa/karate/src\test\java\trouvels360\proveedor.feature:282` | Pasó |

## HU-P09 — Editar un tour

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-066 Editar precio: ids de salidas y totales de reservas sin cambio | Karate | CA-P09-01 Cambios sin afectar reservas | `qa/karate/src\test\java\trouvels360\proveedor.feature:258` | Pasó |
| TC-API-067 Data-driven de límites (imágenes, items, categoría) → 422 | Karate | CA-P09-02 Datos inválidos o tour ajeno | `qa/karate/src\test\java\trouvels360\proveedor.feature:282` | Pasó |

## HU-P10 — Armar el programa de actividades del tour

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-WEB-027 Agregar, reordenar y eliminar actividades | Selenium | CA-P10-01 Programa en orden<br>CA-P10-02 Agregar al final<br>CA-P10-03 Quitar una actividad | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:118` | Pasó |
| TC-API-068 Dos actividades sin orden reciben 1 y 2 | Karate | CA-P10-02 Agregar al final | `qa/karate/src\test\java\trouvels360\proveedor.feature:303` | Pasó |
| TC-MOB-029 Agregar actividad desde la app | Appium | CA-P10-02 Agregar al final | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:118` | Pasó |
| TC-API-069 Actividad en tour ajeno → 403 | Karate | CA-P10-04 Datos inválidos o tour ajeno | `qa/karate/src\test\java\trouvels360\proveedor.feature:328` | Pasó |

## HU-P11 — Programar salidas y cupos

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-WEB-028 Crear, cerrar y eliminar salida desde el panel | Selenium | CA-P11-01 Nueva salida<br>CA-P11-04 Cerrar o cancelar<br>CA-P11-05 Eliminar solo si no hay reservas<br>CA-P11-06 Lo mismo en web y app | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:140` | Pasó |
| TC-MOB-030 Crear y cerrar salida desde la app | Appium | CA-P11-01 Nueva salida<br>CA-P11-04 Cerrar o cancelar<br>CA-P11-06 Lo mismo en web y app | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:133` | Pasó |
| TC-API-070 Salida duplicada → 409; fecha pasada → 422 | Karate | CA-P11-02 Salida repetida o pasada | `qa/karate/src\test\java\trouvels360\proveedor.feature:342` | Pasó |
| TC-API-071 cupo_total menor a reservado → 422; cupo_reservado enviado se ignora | Karate | CA-P11-03 No bajar de lo reservado | `qa/karate/src\test\java\trouvels360\proveedor.feature:360` | Pasó |
| TC-API-072 Salida cerrada rechaza reservas (call a reserva de viajero) | Karate | CA-P11-04 Cerrar o cancelar | `qa/karate/src\test\java\trouvels360\proveedor.feature:377` | Pasó |

## HU-P12 — Ver las reservas que recibo

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-073 Solo reservas de servicios propios (match each servicio.proveedor_id) | Karate | CA-P12-01 Listado completo<br>CA-P12-03 Tours sin aprobación | `qa/karate/src\test\java\trouvels360\proveedor.feature:397` | Pasó |
| TC-WEB-029 Bandeja de reservas con filtros y paginación | Selenium | CA-P12-01 Listado completo<br>CA-P12-02 Filtrar pendientes | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:162` | Pasó |
| TC-MOB-031 Bandeja de reservas en la app | Appium | CA-P12-01 Listado completo | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:152` | Pasó |
| TC-API-074 Filtro estado=pendiente | Karate | CA-P12-02 Filtrar pendientes | `qa/karate/src\test\java\trouvels360\proveedor.feature:422` | Pasó |
| TC-WEB-032 Sin resultados vs. error con Reintentar (tours y bandeja del proveedor) | Selenium | CA-P12-04 Vacío no es lo mismo que un error | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\CoberturaAdicionalTest.java:59` | Pasó |
| TC-API-075 Detalle ajeno → 404 | Karate | CA-P12-05 Reserva de otro proveedor | `qa/karate/src\test\java\trouvels360\proveedor.feature:450` | Pasó |

## HU-P13 — Aceptar una solicitud de hotel

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-031 Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | Karate | CA-P13-01 Aceptar | `qa/karate/src\test\java\trouvels360\aprobacion.feature:14` | Pasó |
| TC-WEB-013 Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | Selenium | CA-P13-01 Aceptar<br>CA-P13-02 Si no decido, sigue pendiente | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AprobacionTest.java:26` | Pasó |
| TC-MOB-015 Proveedor acepta una solicitud desde la app | Appium | CA-P13-01 Aceptar | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\AprobacionMovilTest.java:25` | Pasó |
| TC-API-033 Aceptar dos veces → 409; aceptar una cancelada → 409 | Karate | CA-P13-03 No se decide dos veces | `qa/karate/src\test\java\trouvels360\aprobacion.feature:54` | Pasó |
| TC-API-034 Proveedor B sobre reserva de proveedor A → 403 | Karate | CA-P13-04 Solo mis solicitudes | `qa/karate/src\test\java\trouvels360\aprobacion.feature:77` | Pasó |

## HU-P14 — Rechazar una solicitud de hotel

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-API-032 Rechazo libera unidades: disponibilidad antes y después | Karate | CA-P14-01 Rechazar libera las habitaciones | `qa/karate/src\test\java\trouvels360\aprobacion.feature:31` | Pasó |
| TC-WEB-014 Proveedor rechaza con motivo y el viajero ve RECHAZADA | Selenium | CA-P14-01 Rechazar libera las habitaciones<br>CA-P14-03 El viajero ve el motivo | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\AprobacionTest.java:55` | Pasó |
| TC-MOB-016 Proveedor rechaza con motivo desde la app | Appium | CA-P14-01 Rechazar libera las habitaciones<br>CA-P14-03 El viajero ve el motivo | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\AprobacionMovilTest.java:55` | Pasó |
| TC-API-035 Rechazo sin motivo → 422 | Karate | CA-P14-02 Motivo obligatorio | `qa/karate/src\test\java\trouvels360\aprobacion.feature:90` | Pasó |

## HU-P15 — Eliminar una publicación

| Caso | Herramienta | Criterios | Código | Estado |
|---|---|---|---|---|
| TC-WEB-030 Eliminar publicación con diálogo de confirmación | Selenium | CA-P15-01 Me arrepiento<br>CA-P15-02 Eliminación permitida | `qa/selenium/src\test\java\com\trouvels360\web\pruebas\ProveedorTest.java:198` | Pasó |
| TC-MOB-032 Eliminar publicación desde la app | Appium | CA-P15-02 Eliminación permitida | `qa/appium/src\test\java\com\trouvels360\movil\pruebas\ProveedorMovilTest.java:174` | Pasó |
| TC-API-078 Tras eliminar, GET /mis-reservas del viajero sigue 200 con el nombre del servicio | Karate | CA-P15-03 El historial se conserva | `qa/karate/src\test\java\trouvels360\proveedor.feature:504` | Pasó |
| TC-API-076 Eliminar servicio ajeno → 403 (seguridad) | Karate | CA-P15-04 Publicación ajena o con reservas activas | `qa/karate/src\test\java\trouvels360\proveedor.feature:466` | Pasó |
| TC-API-077 Con reserva activa → 409 | Karate | CA-P15-04 Publicación ajena o con reservas activas | `qa/karate/src\test\java\trouvels360\proveedor.feature:489` | Pasó |
| TC-API-086 Repetir la eliminación devuelve 404 sin efectos | Karate | CA-P15-05 Eliminar dos veces | `qa/karate/src\test\java\trouvels360\cobertura-adicional.feature:149` | Pasó |
