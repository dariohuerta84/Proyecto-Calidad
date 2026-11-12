# Matriz de trazabilidad — Trouvels360 (TRV-605)

- Historias: **24** · Criterios de aceptación: **119** (cubiertos por al menos un caso: **119**)
- Casos de prueba: **160** — Karate: 86, Selenium: 33, Appium: 32, JMeter: 6, Manual: 3
- Casos automatizados con su ubicación en el código: **157 / 157** (+ 3 manuales con pasos)
- Casos agregados en la etapa 5 (criterios sin caso en la etapa 1): 14
- Último resultado: Pasó: 121, No ejecutado: 39

| Herramienta | Pasó | Falló | No ejecutado |
|---|---|---|---|
| Karate | 86 | 0 | 0 |
| Selenium | 33 | 0 | 0 |
| Appium | 0 | 0 | 32 |
| JMeter | 2 | 0 | 4 |
| Manual | 0 | 0 | 3 |

## TRV-101 — Registro de cuenta de viajero

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-001 Registro viajero válido devuelve 201 y esquema de usuario sin password | Karate | CA-TRV-101-01 Registro exitoso | `karate/src/test/java/trouvels360/auth.feature:9` | Pasó |
| TC-API-002 Data-driven (CSV) de campos inválidos devuelve 422 con error por campo | Karate | CA-TRV-101-02 Campos inválidos<br> CA-TRV-101-03 Contraseñas distintas | `karate/src/test/java/trouvels360/auth.feature:18` | Pasó |
| TC-API-003 Correo duplicado en mayúsculas devuelve 422 | Karate | CA-TRV-101-04 Correo duplicado<br> CA-TRV-102-04  | `karate/src/test/java/trouvels360/auth.feature:33` | Pasó |
| TC-API-004 Rol 'admin' devuelve 422 | Karate | CA-TRV-101-05 Rol no manipulable | `karate/src/test/java/trouvels360/auth.feature:43` | Pasó |
| TC-WEB-001 Registro de viajero end-to-end y aterrizaje en catálogo | Selenium | CA-TRV-101-01 Registro exitoso | `selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:27` | Pasó |
| TC-WEB-002 Mensajes de validación en el formulario | Selenium | CA-TRV-101-02 Campos inválidos<br> CA-TRV-101-03 Contraseñas distintas<br> CA-TRV-102-04  | `selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:46` | Pasó |
| TC-MOB-001 Registro de viajero desde la app y sesión iniciada | Appium | CA-TRV-101-01 Registro exitoso | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:23` | No ejecutado |
| TC-MOB-002 Botón Registrarme deshabilitado con contraseñas distintas | Appium | CA-TRV-101-03 Contraseñas distintas<br> CA-TRV-102-04  | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:41` | No ejecutado |

## TRV-102 — Registro de cuenta de proveedor

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-005 Registro proveedor normaliza '987654321' a '+51 987654321' | Karate | CA-TRV-102-01 Registro exitoso | `karate/src/test/java/trouvels360/auth.feature:53` | Pasó |
| TC-API-006 Data-driven de RUC inválidos y duplicado (422) | Karate | CA-TRV-102-02 RUC inválido o duplicado | `karate/src/test/java/trouvels360/auth.feature:62` | Pasó |
| TC-API-007 Teléfonos inválidos devuelven 422 con mensaje | Karate | CA-TRV-102-03 Teléfono inválido | `karate/src/test/java/trouvels360/auth.feature:76` | Pasó |
| TC-WEB-003 Registro de proveedor y acceso al panel del proveedor | Selenium | CA-TRV-102-01 Registro exitoso | `selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:73` | Pasó |
| TC-MOB-003 Registro de proveedor desde la app y acceso al panel | Appium | CA-TRV-102-01 Registro exitoso | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:55` | No ejecutado |

## TRV-103 — Inicio y cierre de sesión con retorno al destino

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-008 Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | Karate | CA-TRV-103-01 Login correcto | `karate/src/test/java/trouvels360/auth.feature:89` | Pasó |
| TC-API-009 POST sin X-XSRF-TOKEN devuelve 403 | Karate | CA-TRV-103-01 Login correcto | `karate/src/test/java/trouvels360/auth.feature:107` | Pasó |
| TC-API-010 Credenciales inválidas 401 y faltantes 422 | Karate | CA-TRV-103-03 Credenciales incorrectas | `karate/src/test/java/trouvels360/auth.feature:114` | Pasó |
| TC-API-011 Token reutilizado tras logout devuelve 401 | Karate | CA-TRV-103-05 Cierre de sesión<br> CA-TRV-103-04 Sesión vencida | `karate/src/test/java/trouvels360/auth.feature:129` | Pasó |
| TC-API-012 Viajero en ruta de proveedor devuelve 403 | Karate | CA-TRV-103-06 Separación de roles | `karate/src/test/java/trouvels360/auth.feature:145` | Pasó |
| TC-PERF-001 Login con 50 usuarios concurrentes desde CSV: p95 < 800 ms, 0% errores | JMeter | CA-TRV-103-01 Login correcto | `jmeter/trouvels360.jmx` | No ejecutado |
| TC-WEB-004 Login viajero y proveedor muestran el menú de su rol | Selenium | CA-TRV-103-01 Login correcto<br> CA-TRV-103-06 Separación de roles | `selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:89` | Pasó |
| TC-WEB-005 Login desde 'Reservar' vuelve al detalle con la selección | Selenium | CA-TRV-103-02 Retorno a la reserva | `selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:114` | Pasó |
| TC-MOB-004 Login, cierre de sesión y bloqueo de pestañas privadas | Appium | CA-TRV-103-01 Login correcto<br> CA-TRV-103-05 Cierre de sesión | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:70` | No ejecutado |
| TC-MOB-005 Reservar sin sesión → login → regreso al detalle | Appium | CA-TRV-103-02 Retorno a la reserva | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:92` | No ejecutado |
| TC-WEB-033 Sesión vencida: 401, login y regreso a la página | Selenium | CA-TRV-103-04 Sesión vencida | `selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:80` | Pasó |

## TRV-104 — Gestionar perfil y contraseña

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-013 PATCH /usuarios/me actualiza y GET /auth/me devuelve lo mismo | Karate | CA-TRV-104-01 Editar datos | `karate/src/test/java/trouvels360/cuenta.feature:10` | Pasó |
| TC-API-014 Avatar y teléfono inválidos devuelven 422 | Karate | CA-TRV-104-02 Datos inválidos | `karate/src/test/java/trouvels360/cuenta.feature:24` | Pasó |
| TC-API-015 Enviar rol devuelve 422 | Karate | CA-TRV-104-03 Intento de cambiar rol | `karate/src/test/java/trouvels360/cuenta.feature:39` | Pasó |
| TC-API-016 Cambio de contraseña: login con nueva 200 y con anterior 401 | Karate | CA-TRV-104-04 Cambiar contraseña<br> CA-TRV-104-05 Contraseña actual incorrecta | `karate/src/test/java/trouvels360/cuenta.feature:51` | Pasó |
| TC-WEB-006 Editar perfil y ver el nombre actualizado en el menú | Selenium | CA-TRV-104-01 Editar datos | `selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:136` | Pasó |
| TC-MOB-006 Editar perfil en la app y verificarlo en la pestaña Perfil | Appium | CA-TRV-104-01 Editar datos | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:111` | No ejecutado |
| TC-MOB-007 Cambio de contraseña con confirmación distinta deshabilita Guardar | Appium | CA-TRV-104-04 Cambiar contraseña | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:127` | No ejecutado |
| TC-MAN-001 Error de servidor al guardar el perfil: mensaje con reintentar y sin éxito falso | Manual | CA-TRV-104-06 Error de servidor | `manual` | No ejecutado |

## TRV-105 — Eliminar mi cuenta conservando el historial

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-017 DELETE con reserva activa devuelve 409 | Karate | CA-TRV-105-03 Bloqueo por reservas activas | `karate/src/test/java/trouvels360/cuenta.feature:65` | Pasó |
| TC-API-018 DELETE sin reservas devuelve 200 y login posterior 401 | Karate | CA-TRV-105-02 Eliminación permitida | `karate/src/test/java/trouvels360/cuenta.feature:78` | Pasó |
| TC-API-019 Reservas de la cuenta eliminada siguen visibles para el proveedor | Karate | CA-TRV-105-04 Historial conservado | `karate/src/test/java/trouvels360/cuenta.feature:89` | Pasó |
| TC-WEB-007 Diálogo de confirmación: cancelar conserva la cuenta | Selenium | CA-TRV-105-01 Desistir | `selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:150` | Pasó |
| TC-MOB-008 Eliminar cuenta desde Perfil y volver a la pantalla de login | Appium | CA-TRV-105-02 Eliminación permitida | `appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:146` | No ejecutado |

## TRV-201 — Buscar hoteles por destino, fechas y huéspedes

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-020 Búsqueda válida: todos los resultados cumplen ciudad, capacidad y unidades (match each) | Karate | CA-TRV-201-01 Búsqueda válida | `karate/src/test/java/trouvels360/hoteles.feature:13` | Pasó |
| TC-API-021 Data-driven de fechas y cantidades inválidas devuelve 422 | Karate | CA-TRV-201-02 Fechas inválidas<br> CA-TRV-201-03 Cantidades inválidas | `karate/src/test/java/trouvels360/hoteles.feature:29` | Pasó |
| TC-API-022 Hotel con unidades ocupadas por reservas pendientes no aparece | Karate | CA-TRV-201-01 Búsqueda válida | `karate/src/test/java/trouvels360/hoteles.feature:41` | Pasó |
| TC-PERF-002 Carga de catálogo: 100 usuarios, ramp-up 60 s, ciudades desde CSV, p95 < 1 s | JMeter | CA-TRV-201-01 Búsqueda válida | `jmeter/trouvels360.jmx` | No ejecutado |
| TC-WEB-008 Búsqueda en Cusco y validación de tarjetas de resultado | Selenium | CA-TRV-201-01 Búsqueda válida | `selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:30` | Pasó |
| TC-WEB-009 Validación de fechas pasadas en el buscador | Selenium | CA-TRV-201-02 Fechas inválidas | `selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:50` | Pasó |
| TC-MOB-009 Búsqueda por ciudad y fechas en la app | Appium | CA-TRV-201-01 Búsqueda válida | `appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:26` | No ejecutado |
| TC-MOB-010 Estado vacío vs error con Reintentar | Appium | CA-TRV-201-04 Sin resultados vs error | `appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:39` | No ejecutado |
| TC-WEB-031 Volver desde el detalle conserva la búsqueda (hoteles y tours) | Selenium | CA-TRV-201-05 Conservar búsqueda<br> CA-TRV-301-04  | `selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:28` | Pasó |

## TRV-202 — Ver detalle del hotel y cotizar habitaciones con IGV

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-023 Esquema JSON del detalle de hotel (habitaciones, calificación, reseñas) | Karate | CA-TRV-202-01 Detalle completo | `karate/src/test/java/trouvels360/hoteles.feature:61` | Pasó |
| TC-API-024 Cotización data-driven (CSV) con tarifas, noches y unidades → subtotal, igv, total exactos | Karate | CA-TRV-202-03 Cálculo con IGV | `karate/src/test/java/trouvels360/hoteles.feature:76` | Pasó |
| TC-API-025 Cotizar más unidades que las libres devuelve 422 | Karate | CA-TRV-202-04 Cantidad excedida | `karate/src/test/java/trouvels360/hoteles.feature:94` | Pasó |
| TC-WEB-010 Selección de 2 tipos y verificación del resumen con IGV | Selenium | CA-TRV-202-02 Selección múltiple<br> CA-TRV-202-03 Cálculo con IGV | `selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:66` | Pasó |
| TC-MOB-011 Selección de habitaciones y total con IGV en la app | Appium | CA-TRV-202-02 Selección múltiple<br> CA-TRV-202-03 Cálculo con IGV | `appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:61` | No ejecutado |
| TC-MOB-012 Botón + deshabilitado al llegar a las unidades disponibles | Appium | CA-TRV-202-04 Cantidad excedida | `appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:78` | No ejecutado |
| TC-API-079 Cambiar la fecha de salida recalcula el total y no crea reserva | Karate | CA-TRV-202-05 Recalcular sin reservar | `karate/src/test/java/trouvels360/cobertura-adicional.feature:13` | Pasó |

## TRV-203 — Solicitar reserva de hotel con pago simulado

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-026 Reserva válida 201: estado pendiente, código único, subtotal/igv/total = cotización | Karate | CA-TRV-203-01 Solicitud registrada | `karate/src/test/java/trouvels360/reservas-hotel.feature:11` | Pasó |
| TC-API-027 Data-driven de tarjetas inválidas (Luhn, vencida, CVC) → 422 sin reserva creada | Karate | CA-TRV-203-02 Pago inválido | `karate/src/test/java/trouvels360/reservas-hotel.feature:28` | Pasó |
| TC-API-028 Disponibilidad tras reservar descuenta las unidades | Karate | CA-TRV-203-03 Unidades ocupadas | `karate/src/test/java/trouvels360/reservas-hotel.feature:52` | Pasó |
| TC-API-029 Varios tipos con uno sin stock → 409 y ninguno registrado | Karate | CA-TRV-203-04 Disponibilidad insuficiente | `karate/src/test/java/trouvels360/reservas-hotel.feature:70` | Pasó |
| TC-API-030 Mismo Idempotency-Key devuelve la misma solicitud | Karate | CA-TRV-203-06 Envío repetido | `karate/src/test/java/trouvels360/reservas-hotel.feature:87` | Pasó |
| TC-PERF-003 20 hilos simultáneos (Synchronizing Timer) por la última unidad: exactamente 1×201 y 19×409 | JMeter | CA-TRV-203-05 Concurrencia | `jmeter/trouvels360.jmx` | Pasó |
| TC-WEB-011 Flujo completo búsqueda → selección → pago → comprobante PENDIENTE | Selenium | CA-TRV-203-01 Solicitud registrada | `selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:83` | Pasó |
| TC-WEB-012 Validaciones del formulario de pago | Selenium | CA-TRV-203-02 Pago inválido | `selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:114` | Pasó |
| TC-MOB-013 Flujo completo de reserva de hotel en la app | Appium | CA-TRV-203-01 Solicitud registrada | `appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:92` | No ejecutado |
| TC-MOB-014 Formulario de pago rechaza tarjeta vencida | Appium | CA-TRV-203-02 Pago inválido | `appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:116` | No ejecutado |

## TRV-204 — Aprobar o rechazar solicitudes de hotel

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-031 Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | Karate | CA-TRV-204-01 Aceptar | `karate/src/test/java/trouvels360/aprobacion.feature:14` | Pasó |
| TC-API-032 Rechazo libera unidades: disponibilidad antes y después | Karate | CA-TRV-204-02 Rechazar | `karate/src/test/java/trouvels360/aprobacion.feature:31` | Pasó |
| TC-API-033 Aceptar dos veces → 409; aceptar una cancelada → 409 | Karate | CA-TRV-204-04 Decisión repetida o estado incompatible | `karate/src/test/java/trouvels360/aprobacion.feature:54` | Pasó |
| TC-API-034 Proveedor B sobre reserva de proveedor A → 403 | Karate | CA-TRV-204-05 Reserva ajena | `karate/src/test/java/trouvels360/aprobacion.feature:77` | Pasó |
| TC-API-035 Rechazo sin motivo → 422 | Karate | CA-TRV-204-02 Rechazar | `karate/src/test/java/trouvels360/aprobacion.feature:90` | Pasó |
| TC-WEB-013 Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | Selenium | CA-TRV-204-01 Aceptar<br> CA-TRV-204-03 Sin decisión | `selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java:26` | Pasó |
| TC-WEB-014 Proveedor rechaza con motivo y el viajero ve RECHAZADA | Selenium | CA-TRV-204-02 Rechazar | `selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java:55` | Pasó |
| TC-MOB-015 Proveedor acepta una solicitud desde la app | Appium | CA-TRV-204-01 Aceptar | `appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java:25` | No ejecutado |
| TC-MOB-016 Proveedor rechaza con motivo desde la app | Appium | CA-TRV-204-02 Rechazar | `appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java:55` | No ejecutado |

## TRV-301 — Buscar tours por destino, categoría y fechas

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-036 Filtros combinados: todos los resultados cumplen destino y categoría (match each) | Karate | CA-TRV-301-01 Búsqueda válida | `karate/src/test/java/trouvels360/tours.feature:10` | Pasó |
| TC-API-037 Data-driven de filtros inválidos → 422 | Karate | CA-TRV-301-02 Filtros inválidos | `karate/src/test/java/trouvels360/tours.feature:29` | Pasó |
| TC-PERF-004 Carga de búsqueda de tours: 100 usuarios, categorías desde CSV, p95 < 1 s | JMeter | CA-TRV-301-01 Búsqueda válida | `jmeter/trouvels360.jmx` | No ejecutado |
| TC-WEB-015 Filtrar por categoría y destino | Selenium | CA-TRV-301-01 Búsqueda válida | `selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:28` | Pasó |
| TC-MOB-017 Chips de categoría y búsqueda por destino | Appium | CA-TRV-301-01 Búsqueda válida | `appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:26` | No ejecutado |
| TC-WEB-032 Sin resultados vs. error con Reintentar (tours y bandeja del proveedor) | Selenium | CA-TRV-301-03 Sin resultados o error<br> CA-TRV-507-03  | `selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:59` | Pasó |

## TRV-302 — Ver detalle del tour y elegir salida y participantes

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-038 Esquema del detalle; ninguna salida devuelta es pasada ni no programada | Karate | CA-TRV-302-01 Detalle | `karate/src/test/java/trouvels360/tours.feature:41` | Pasó |
| TC-API-039 Cotización de tour data-driven (precio × personas + 18%) | Karate | CA-TRV-302-02 Cálculo | `karate/src/test/java/trouvels360/tours.feature:62` | Pasó |
| TC-API-040 Cotizar más personas que el cupo → 422 | Karate | CA-TRV-302-03 Selección inválida | `karate/src/test/java/trouvels360/tours.feature:76` | Pasó |
| TC-WEB-016 Elegir salida y personas, verificar total con IGV | Selenium | CA-TRV-302-02 Cálculo | `selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:41` | Pasó |
| TC-MOB-018 Elegir salida y personas en la app; + se deshabilita al tope de cupos | Appium | CA-TRV-302-02 Cálculo<br> CA-TRV-302-03 Selección inválida | `appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:48` | No ejecutado |
| TC-API-080 Tour sin salidas disponibles: detalle sin salidas y cotizar 409 | Karate | CA-TRV-302-04 Sin salidas | `karate/src/test/java/trouvels360/cobertura-adicional.feature:35` | Pasó |

## TRV-303 — Reservar tour con confirmación automática

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-041 Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | Karate | CA-TRV-303-01 Reserva confirmada<br> CA-TRV-204-06  | `karate/src/test/java/trouvels360/reservas-tour.feature:11` | Pasó |
| TC-API-042 Salida cerrada / pasada / sin cupo → 409 o 422 y cupos sin cambio | Karate | CA-TRV-303-03 Salida no válida | `karate/src/test/java/trouvels360/reservas-tour.feature:27` | Pasó |
| TC-API-043 Idempotencia: dos envíos, una sola reserva y un solo descuento | Karate | CA-TRV-303-05 Envío repetido | `karate/src/test/java/trouvels360/reservas-tour.feature:47` | Pasó |
| TC-PERF-005 30 hilos simultáneos sobre salida de 10 cupos: suma de personas confirmadas <= 10 | JMeter | CA-TRV-303-04 Concurrencia | `jmeter/trouvels360.jmx` | Pasó |
| TC-WEB-017 Reserva de tour end-to-end con comprobante CONFIRMADA | Selenium | CA-TRV-303-01 Reserva confirmada | `selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:56` | Pasó |
| TC-MOB-019 Reserva de tour end-to-end en la app | Appium | CA-TRV-303-01 Reserva confirmada | `appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:62` | No ejecutado |
| TC-API-081 Pago de tour inválido (CSV) no crea reserva ni descuenta cupos | Karate | CA-TRV-303-02 Pago inválido o desistir | `karate/src/test/java/trouvels360/cobertura-adicional.feature:53` | Pasó |

## TRV-401 — Consultar mis reservas y sus estados

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-044 Esquema paginado (data, meta.total, meta.last_page) y solo reservas propias | Karate | CA-TRV-401-01 Listado propio<br> CA-TRV-401-05 Consistencia entre canales | `karate/src/test/java/trouvels360/mis-reservas.feature:20` | Pasó |
| TC-API-045 Estado tras aceptar y tras rechazar (con motivo) | Karate | CA-TRV-401-02 Pendiente y aceptada<br> CA-TRV-401-03 Rechazada | `karate/src/test/java/trouvels360/mis-reservas.feature:49` | Pasó |
| TC-API-046 Reserva ajena → 404 | Karate | CA-TRV-401-06 Vacío, error y ajeno | `karate/src/test/java/trouvels360/mis-reservas.feature:73` | Pasó |
| TC-WEB-018 Mis reservas: pestañas hotel/tour, filtro por estado y paginación | Selenium | CA-TRV-401-01 Listado propio<br> CA-TRV-401-05 Consistencia entre canales | `selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:26` | Pasó |
| TC-WEB-019 Reserva rechazada muestra motivo | Selenium | CA-TRV-401-03 Rechazada | `selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:59` | Pasó |
| TC-MOB-020 Mis reservas en la app coincide con la web (mismo código y total) | Appium | CA-TRV-401-05 Consistencia entre canales | `appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:24` | No ejecutado |
| TC-MOB-021 Scroll infinito / paginación de reservas | Appium | CA-TRV-401-05 Consistencia entre canales | `appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:44` | No ejecutado |
| TC-API-082 Salida cancelada por el proveedor: reserva confirmada con aviso | Karate | CA-TRV-401-04 Tour con salida cancelada | `karate/src/test/java/trouvels360/cobertura-adicional.feature:69` | Pasó |

## TRV-402 — Cancelar mis reservas

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-047 Cancelar hotel: estado cancelada y unidades liberadas | Karate | CA-TRV-402-02 Cancelación exitosa | `karate/src/test/java/trouvels360/mis-reservas.feature:85` | Pasó |
| TC-API-048 Cancelar tour devuelve exactamente las personas al cupo | Karate | CA-TRV-402-03 Liberar oferta una vez | `karate/src/test/java/trouvels360/mis-reservas.feature:98` | Pasó |
| TC-API-049 Cancelar dos veces → 409; cancelar rechazada → 409; ajena → 404 | Karate | CA-TRV-402-04 No cancelable<br> CA-TRV-402-05 Respuesta incierta | `karate/src/test/java/trouvels360/mis-reservas.feature:113` | Pasó |
| TC-WEB-020 Cancelar desde Mis reservas con diálogo de confirmación | Selenium | CA-TRV-402-01 Confirmar o desistir<br> CA-TRV-402-02 Cancelación exitosa | `selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:76` | Pasó |
| TC-MOB-022 Cancelar desde la app y ver estado CANCELADA | Appium | CA-TRV-402-02 Cancelación exitosa | `appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:66` | No ejecutado |
| TC-MAN-002 Corte de conexión al cancelar: al refrescar se ve el estado real | Manual | CA-TRV-402-05 Respuesta incierta | `manual` | No ejecutado |

## TRV-403 — Publicar, editar y eliminar reseñas

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-050 Resumen: promedio y total coinciden con el conjunto completo | Karate | CA-TRV-403-01 Consultar<br> CA-TRV-501-03  | `karate/src/test/java/trouvels360/resenas.feature:27` | Pasó |
| TC-API-051 Publicar con reserva finalizada 201; sin reserva 403 con mensaje | Karate | CA-TRV-403-02 Publicar<br> CA-TRV-403-03 No permitido | `karate/src/test/java/trouvels360/resenas.feature:62` | Pasó |
| TC-API-052 Data-driven de longitudes de comentario y calificaciones fuera de rango → 422 | Karate | CA-TRV-403-03 No permitido | `karate/src/test/java/trouvels360/resenas.feature:85` | Pasó |
| TC-API-053 Editar/eliminar reseña ajena → 403; segunda reseña → 409 | Karate | CA-TRV-403-04 Editar y eliminar<br> CA-TRV-403-05 Sin duplicados | `karate/src/test/java/trouvels360/resenas.feature:105` | Pasó |
| TC-WEB-021 Publicar, editar y eliminar reseña en el detalle | Selenium | CA-TRV-403-02 Publicar<br> CA-TRV-403-04 Editar y eliminar | `selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java:30` | Pasó |
| TC-MOB-023 Publicar reseña desde la app | Appium | CA-TRV-403-02 Publicar | `appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java:21` | No ejecutado |

## TRV-404 — Generar un itinerario sugerido para mi estadía

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-054 Itinerario válido: esquema de días y actividades | Karate | CA-TRV-404-01 Propuesta | `karate/src/test/java/trouvels360/itinerario.feature:11` | Pasó |
| TC-API-055 Interés inválido → 422 | Karate | CA-TRV-404-04 Validación | `karate/src/test/java/trouvels360/itinerario.feature:31` | Pasó |
| TC-WEB-022 Generar itinerario desde el detalle del hotel | Selenium | CA-TRV-404-01 Propuesta<br> CA-TRV-404-02 Sugerencia, no reserva | `selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java:60` | Pasó |
| TC-MOB-024 Generar itinerario en la app y continuar con la reserva | Appium | CA-TRV-404-01 Propuesta<br> CA-TRV-404-03 Continuar | `appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java:46` | No ejecutado |
| TC-MAN-003 Microservicio de itinerarios caído: aviso con Reintentar y se puede seguir con el hotel | Manual | CA-TRV-404-05 Servicio caído | `manual` | No ejecutado |

## TRV-501 — Panel de publicaciones e indicadores

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-056 Paginación: meta.total, last_page y página 2 con los registros 16+ | Karate | CA-TRV-501-01 Listado propio | `karate/src/test/java/trouvels360/proveedor.feature:19` | Pasó |
| TC-API-057 Indicadores por estado tras crear reservas en 4 estados | Karate | CA-TRV-501-02 Indicadores por estado<br> CA-TRV-501-04 Actualización | `karate/src/test/java/trouvels360/proveedor.feature:54` | Pasó |
| TC-API-058 Viajero en /proveedor/servicios → 403 | Karate | CA-TRV-501-05 Vacío y no autorizado | `karate/src/test/java/trouvels360/proveedor.feature:94` | Pasó |
| TC-PERF-006 Panel del proveedor con 30 usuarios concurrentes, p95 < 1,5 s | JMeter | CA-TRV-501-01 Listado propio | `jmeter/trouvels360.jmx` | No ejecutado |
| TC-WEB-023 Panel: paginación y tarjetas de indicadores | Selenium | CA-TRV-501-01 Listado propio<br> CA-TRV-501-02 Indicadores por estado | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:29` | Pasó |
| TC-MOB-025 Panel del proveedor en la app con indicadores | Appium | CA-TRV-501-01 Listado propio<br> CA-TRV-501-02 Indicadores por estado | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:31` | No ejecutado |

## TRV-502 — Publicar y editar hoteles

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-059 Crear hotel con habitaciones y verificar en el catálogo | Karate | CA-TRV-502-01 Publicar | `karate/src/test/java/trouvels360/proveedor.feature:112` | Pasó |
| TC-API-060 6 imágenes → 422 | Karate | CA-TRV-502-02 Galería | `karate/src/test/java/trouvels360/proveedor.feature:137` | Pasó |
| TC-API-061 Editar hotel ajeno → 403 | Karate | CA-TRV-502-04 Inválido o ajeno | `karate/src/test/java/trouvels360/proveedor.feature:147` | Pasó |
| TC-WEB-024 Crear hotel desde el formulario del proveedor | Selenium | CA-TRV-502-01 Publicar | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:56` | Pasó |
| TC-MOB-026 Crear hotel desde la app del proveedor | Appium | CA-TRV-502-01 Publicar | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:50` | No ejecutado |
| TC-API-083 Editar descripción e imágenes conserva id y reservas | Karate | CA-TRV-502-03 Editar | `karate/src/test/java/trouvels360/cobertura-adicional.feature:86` | Pasó |
| TC-API-084 Tipo de habitación inválido: no queda hotel a medias | Karate | CA-TRV-502-05 Transacción | `karate/src/test/java/trouvels360/cobertura-adicional.feature:109` | Pasó |

## TRV-503 — Gestionar tipos de habitación

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-062 Reducir unidades bajo ocupación → 409 con mínimo | Karate | CA-TRV-503-02 Reducir bajo ocupación | `karate/src/test/java/trouvels360/proveedor.feature:166` | Pasó |
| TC-API-063 Eliminar con reservas → 409; sin reservas → 204 | Karate | CA-TRV-503-03 Eliminar con reservas | `karate/src/test/java/trouvels360/proveedor.feature:185` | Pasó |
| TC-API-064 Cambio de tarifa no altera total de reservas previas | Karate | CA-TRV-503-04 Historial | `karate/src/test/java/trouvels360/proveedor.feature:209` | Pasó |
| TC-WEB-025 Agregar y editar tipo de habitación | Selenium | CA-TRV-503-01 Agregar o editar | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:75` | Pasó |
| TC-MOB-027 Agregar tipo de habitación en la app | Appium | CA-TRV-503-01 Agregar o editar | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:70` | No ejecutado |
| TC-API-085 Tarifa 0 → 422 y tipo ajeno → 403 sin cambios | Karate | CA-TRV-503-05 Inválido o ajeno | `karate/src/test/java/trouvels360/cobertura-adicional.feature:129` | Pasó |

## TRV-504 — Publicar y editar tours

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-065 Crear tour y verificar en catálogo | Karate | CA-TRV-504-01 Publicar | `karate/src/test/java/trouvels360/proveedor.feature:233` | Pasó |
| TC-API-066 Editar precio: ids de salidas y totales de reservas sin cambio | Karate | CA-TRV-504-03 Editar sin afectar reservas | `karate/src/test/java/trouvels360/proveedor.feature:258` | Pasó |
| TC-API-067 Data-driven de límites (imágenes, items, categoría) → 422 | Karate | CA-TRV-504-02 Límites<br> CA-TRV-504-04 Inválido o ajeno | `karate/src/test/java/trouvels360/proveedor.feature:282` | Pasó |
| TC-WEB-026 Crear tour con salida desde el formulario | Selenium | CA-TRV-504-01 Publicar | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:96` | Pasó |
| TC-MOB-028 Crear tour desde la app | Appium | CA-TRV-504-01 Publicar | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:92` | No ejecutado |

## TRV-505 — Gestionar actividades del tour

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-068 Dos actividades sin orden reciben 1 y 2 | Karate | CA-TRV-505-02 Agregar o editar | `karate/src/test/java/trouvels360/proveedor.feature:303` | Pasó |
| TC-API-069 Actividad en tour ajeno → 403 | Karate | CA-TRV-505-04 Inválido o ajeno | `karate/src/test/java/trouvels360/proveedor.feature:328` | Pasó |
| TC-WEB-027 Agregar, reordenar y eliminar actividades | Selenium | CA-TRV-505-01 Consultar<br> CA-TRV-505-02 Agregar o editar<br> CA-TRV-505-03 Eliminar | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:118` | Pasó |
| TC-MOB-029 Agregar actividad desde la app | Appium | CA-TRV-505-02 Agregar o editar | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:114` | No ejecutado |

## TRV-506 — Gestionar salidas y cupos

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-070 Salida duplicada → 409; fecha pasada → 422 | Karate | CA-TRV-506-02 Duplicado o fecha pasada | `karate/src/test/java/trouvels360/proveedor.feature:342` | Pasó |
| TC-API-071 cupo_total menor a reservado → 422; cupo_reservado enviado se ignora | Karate | CA-TRV-506-03 Editar con reservas | `karate/src/test/java/trouvels360/proveedor.feature:360` | Pasó |
| TC-API-072 Salida cerrada rechaza reservas (call a reserva de viajero) | Karate | CA-TRV-506-04 Cerrar o cancelar | `karate/src/test/java/trouvels360/proveedor.feature:377` | Pasó |
| TC-WEB-028 Crear, cerrar y eliminar salida desde el panel | Selenium | CA-TRV-506-01 Crear<br> CA-TRV-506-04 Cerrar o cancelar<br> CA-TRV-506-05 Eliminar<br> CA-TRV-506-06 Consistencia | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:140` | Pasó |
| TC-MOB-030 Crear y cerrar salida desde la app | Appium | CA-TRV-506-01 Crear<br> CA-TRV-506-04 Cerrar o cancelar<br> CA-TRV-506-06 Consistencia | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:129` | No ejecutado |

## TRV-507 — Consultar reservas recibidas

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-073 Solo reservas de servicios propios (match each servicio.proveedor_id) | Karate | CA-TRV-507-01 Listado y detalle<br> CA-TRV-204-06  | `karate/src/test/java/trouvels360/proveedor.feature:397` | Pasó |
| TC-API-074 Filtro estado=pendiente | Karate | CA-TRV-507-02 Filtros | `karate/src/test/java/trouvels360/proveedor.feature:422` | Pasó |
| TC-API-075 Detalle ajeno → 404 | Karate | CA-TRV-507-04 Ajeno | `karate/src/test/java/trouvels360/proveedor.feature:450` | Pasó |
| TC-WEB-029 Bandeja de reservas con filtros y paginación | Selenium | CA-TRV-507-01 Listado y detalle<br> CA-TRV-507-02 Filtros | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:162` | Pasó |
| TC-MOB-031 Bandeja de reservas en la app | Appium | CA-TRV-507-01 Listado y detalle | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:148` | No ejecutado |

## TRV-508 — Eliminar publicaciones propias

| Caso | Herramienta | Criterios | Automatización | Estado |
|---|---|---|---|---|
| TC-API-076 Eliminar servicio ajeno → 403 (seguridad) | Karate | CA-TRV-508-04 Ajeno o con reservas activas | `karate/src/test/java/trouvels360/proveedor.feature:466` | Pasó |
| TC-API-077 Con reserva activa → 409 | Karate | CA-TRV-508-04 Ajeno o con reservas activas | `karate/src/test/java/trouvels360/proveedor.feature:489` | Pasó |
| TC-API-078 Tras eliminar, GET /mis-reservas del viajero sigue 200 con el nombre del servicio | Karate | CA-TRV-508-03 Historial | `karate/src/test/java/trouvels360/proveedor.feature:504` | Pasó |
| TC-WEB-030 Eliminar publicación con diálogo de confirmación | Selenium | CA-TRV-508-01 Confirmar o desistir<br> CA-TRV-508-02 Eliminación permitida | `selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:198` | Pasó |
| TC-MOB-032 Eliminar publicación desde la app | Appium | CA-TRV-508-02 Eliminación permitida | `appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:170` | No ejecutado |
| TC-API-086 Repetir la eliminación devuelve 404 sin efectos | Karate | CA-TRV-508-05 Repetición | `karate/src/test/java/trouvels360/cobertura-adicional.feature:149` | Pasó |
