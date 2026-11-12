# Catálogo de casos de prueba

[← Documentación](../README.md) · [Estrategia de pruebas](estrategia-de-pruebas.md) · [Matriz de trazabilidad](../3-trazabilidad/matriz-de-trazabilidad.md)

**160 casos**, cada uno vinculado a una o más historias y criterios. La columna «Código» indica dónde está automatizado (relativo a la raíz del repositorio).

| Nivel | Herramienta | Casos | Pasó | Pendiente |
|---|---|---|---|---|
| API | Karate | 86 | 86 | 0 |
| Web | Selenium | 33 | 33 | 0 |
| Móvil | Appium | 32 | 0 | 32 |
| Rendimiento | JMeter | 6 | 2 | 4 |
| Manual | Manual | 3 | 0 | 3 |
| **Total** | | **160** | **121** | **39** |

«Pendiente» significa que el caso existe y compila, pero aún no se ejecutó en un entorno con emulador Android o con JMeter instalado, o que es manual. Las dos carreras de concurrencia (TC-PERF-003 y 005) ya se comprobaron con `qa/jmeter/concurrencia_smoke.py`; falta su corrida en JMeter.

## API · Karate (TC-API)

| Caso | Qué comprueba | Historias | Criterios | Código | Resultado |
|---|---|---|---|---|---|
| TC-API-001 | Registro viajero válido devuelve 201 y esquema de usuario sin password | HU-V01 | CA-V01-01 | `qa/karate/src/test/java/trouvels360/auth.feature:9` | Pasó |
| TC-API-002 | Data-driven (CSV) de campos inválidos devuelve 422 con error por campo | HU-V01 | CA-V01-02, CA-V01-03 | `qa/karate/src/test/java/trouvels360/auth.feature:18` | Pasó |
| TC-API-003 | Correo duplicado en mayúsculas devuelve 422 | HU-V01, HU-P01 | CA-V01-04, CA-P01-04 | `qa/karate/src/test/java/trouvels360/auth.feature:33` | Pasó |
| TC-API-004 | Rol 'admin' devuelve 422 | HU-V01 | CA-V01-05 | `qa/karate/src/test/java/trouvels360/auth.feature:43` | Pasó |
| TC-API-005 | Registro proveedor normaliza '987654321' a '+51 987654321' | HU-P01 | CA-P01-01 | `qa/karate/src/test/java/trouvels360/auth.feature:53` | Pasó |
| TC-API-006 | Data-driven de RUC inválidos y duplicado (422) | HU-P01 | CA-P01-02 | `qa/karate/src/test/java/trouvels360/auth.feature:62` | Pasó |
| TC-API-007 | Teléfonos inválidos devuelven 422 con mensaje | HU-P01 | CA-P01-03 | `qa/karate/src/test/java/trouvels360/auth.feature:76` | Pasó |
| TC-API-008 | Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | HU-V02, HU-P02 | CA-V02-01, CA-P02-01 | `qa/karate/src/test/java/trouvels360/auth.feature:89` | Pasó |
| TC-API-009 | POST sin X-XSRF-TOKEN devuelve 403 | HU-V02 | CA-V02-06 | `qa/karate/src/test/java/trouvels360/auth.feature:107` | Pasó |
| TC-API-010 | Credenciales inválidas 401 y faltantes 422 | HU-V02 | CA-V02-03 | `qa/karate/src/test/java/trouvels360/auth.feature:114` | Pasó |
| TC-API-011 | Token reutilizado tras logout devuelve 401 | HU-V02 | CA-V02-04, CA-V02-05 | `qa/karate/src/test/java/trouvels360/auth.feature:129` | Pasó |
| TC-API-012 | Viajero en ruta de proveedor devuelve 403 | HU-P02 | CA-P02-02 | `qa/karate/src/test/java/trouvels360/auth.feature:145` | Pasó |
| TC-API-013 | PATCH /usuarios/me actualiza y GET /auth/me devuelve lo mismo | HU-V03 | CA-V03-01 | `qa/karate/src/test/java/trouvels360/cuenta.feature:10` | Pasó |
| TC-API-014 | Avatar y teléfono inválidos devuelven 422 | HU-V03, HU-P02 | CA-V03-02, CA-P02-03 | `qa/karate/src/test/java/trouvels360/cuenta.feature:24` | Pasó |
| TC-API-015 | Enviar rol devuelve 422 | HU-V03 | CA-V03-03 | `qa/karate/src/test/java/trouvels360/cuenta.feature:39` | Pasó |
| TC-API-016 | Cambio de contraseña: login con nueva 200 y con anterior 401 | HU-V03 | CA-V03-04, CA-V03-05 | `qa/karate/src/test/java/trouvels360/cuenta.feature:51` | Pasó |
| TC-API-017 | DELETE con reserva activa devuelve 409 | HU-V04 | CA-V04-03 | `qa/karate/src/test/java/trouvels360/cuenta.feature:65` | Pasó |
| TC-API-018 | DELETE sin reservas devuelve 200 y login posterior 401 | HU-V04 | CA-V04-02 | `qa/karate/src/test/java/trouvels360/cuenta.feature:78` | Pasó |
| TC-API-019 | Reservas de la cuenta eliminada siguen visibles para el proveedor | HU-V04 | CA-V04-04 | `qa/karate/src/test/java/trouvels360/cuenta.feature:89` | Pasó |
| TC-API-020 | Búsqueda válida: todos los resultados cumplen ciudad, capacidad y unidades (match each) | HU-V05 | CA-V05-01 | `qa/karate/src/test/java/trouvels360/hoteles.feature:13` | Pasó |
| TC-API-021 | Data-driven de fechas y cantidades inválidas devuelve 422 | HU-V05 | CA-V05-03, CA-V05-04 | `qa/karate/src/test/java/trouvels360/hoteles.feature:29` | Pasó |
| TC-API-022 | Hotel con unidades ocupadas por reservas pendientes no aparece | HU-V05 | CA-V05-02 | `qa/karate/src/test/java/trouvels360/hoteles.feature:41` | Pasó |
| TC-API-023 | Esquema JSON del detalle de hotel (habitaciones, calificación, reseñas) | HU-V06 | CA-V06-01 | `qa/karate/src/test/java/trouvels360/hoteles.feature:61` | Pasó |
| TC-API-024 | Cotización data-driven (CSV) con tarifas, noches y unidades → subtotal, igv, total exactos | HU-V06 | CA-V06-03 | `qa/karate/src/test/java/trouvels360/hoteles.feature:76` | Pasó |
| TC-API-025 | Cotizar más unidades que las libres devuelve 422 | HU-V06 | CA-V06-04 | `qa/karate/src/test/java/trouvels360/hoteles.feature:94` | Pasó |
| TC-API-079 | Cambiar la fecha de salida recalcula el total y no crea reserva | HU-V06 | CA-V06-05 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:13` | Pasó |
| TC-API-026 | Reserva válida 201: estado pendiente, código único, subtotal/igv/total = cotización | HU-V07 | CA-V07-01 | `qa/karate/src/test/java/trouvels360/reservas-hotel.feature:11` | Pasó |
| TC-API-027 | Data-driven de tarjetas inválidas (Luhn, vencida, CVC) → 422 sin reserva creada | HU-V07 | CA-V07-02 | `qa/karate/src/test/java/trouvels360/reservas-hotel.feature:28` | Pasó |
| TC-API-028 | Disponibilidad tras reservar descuenta las unidades | HU-V07 | CA-V07-03 | `qa/karate/src/test/java/trouvels360/reservas-hotel.feature:52` | Pasó |
| TC-API-029 | Varios tipos con uno sin stock → 409 y ninguno registrado | HU-V07 | CA-V07-04 | `qa/karate/src/test/java/trouvels360/reservas-hotel.feature:70` | Pasó |
| TC-API-030 | Mismo Idempotency-Key devuelve la misma solicitud | HU-V07 | CA-V07-06 | `qa/karate/src/test/java/trouvels360/reservas-hotel.feature:87` | Pasó |
| TC-API-031 | Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | HU-V12, HU-P13 | CA-V12-01, CA-P13-01 | `qa/karate/src/test/java/trouvels360/aprobacion.feature:14` | Pasó |
| TC-API-032 | Rechazo libera unidades: disponibilidad antes y después | HU-P14 | CA-P14-01 | `qa/karate/src/test/java/trouvels360/aprobacion.feature:31` | Pasó |
| TC-API-033 | Aceptar dos veces → 409; aceptar una cancelada → 409 | HU-P13 | CA-P13-03 | `qa/karate/src/test/java/trouvels360/aprobacion.feature:54` | Pasó |
| TC-API-034 | Proveedor B sobre reserva de proveedor A → 403 | HU-P13 | CA-P13-04 | `qa/karate/src/test/java/trouvels360/aprobacion.feature:77` | Pasó |
| TC-API-035 | Rechazo sin motivo → 422 | HU-P14 | CA-P14-02 | `qa/karate/src/test/java/trouvels360/aprobacion.feature:90` | Pasó |
| TC-API-036 | Filtros combinados: todos los resultados cumplen destino y categoría (match each) | HU-V08 | CA-V08-01 | `qa/karate/src/test/java/trouvels360/tours.feature:10` | Pasó |
| TC-API-037 | Data-driven de filtros inválidos → 422 | HU-V08 | CA-V08-02 | `qa/karate/src/test/java/trouvels360/tours.feature:29` | Pasó |
| TC-API-038 | Esquema del detalle; ninguna salida devuelta es pasada ni no programada | HU-V09 | CA-V09-01 | `qa/karate/src/test/java/trouvels360/tours.feature:41` | Pasó |
| TC-API-039 | Cotización de tour data-driven (precio × personas + 18%) | HU-V09 | CA-V09-02 | `qa/karate/src/test/java/trouvels360/tours.feature:62` | Pasó |
| TC-API-040 | Cotizar más personas que el cupo → 422 | HU-V09 | CA-V09-03 | `qa/karate/src/test/java/trouvels360/tours.feature:76` | Pasó |
| TC-API-080 | Tour sin salidas disponibles: detalle sin salidas y cotizar 409 | HU-V09 | CA-V09-04 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:35` | Pasó |
| TC-API-041 | Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | HU-V10, HU-V12 | CA-V10-01, CA-V12-03 | `qa/karate/src/test/java/trouvels360/reservas-tour.feature:11` | Pasó |
| TC-API-042 | Salida cerrada / pasada / sin cupo → 409 o 422 y cupos sin cambio | HU-V10 | CA-V10-03 | `qa/karate/src/test/java/trouvels360/reservas-tour.feature:27` | Pasó |
| TC-API-043 | Idempotencia: dos envíos, una sola reserva y un solo descuento | HU-V10 | CA-V10-05 | `qa/karate/src/test/java/trouvels360/reservas-tour.feature:47` | Pasó |
| TC-API-081 | Pago de tour inválido (CSV) no crea reserva ni descuenta cupos | HU-V10 | CA-V10-02 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:53` | Pasó |
| TC-API-044 | Esquema paginado (data, meta.total, meta.last_page) y solo reservas propias | HU-V11 | CA-V11-01, CA-V11-02 | `qa/karate/src/test/java/trouvels360/mis-reservas.feature:20` | Pasó |
| TC-API-045 | Estado tras aceptar y tras rechazar (con motivo) | HU-V12 | CA-V12-01, CA-V12-02 | `qa/karate/src/test/java/trouvels360/mis-reservas.feature:49` | Pasó |
| TC-API-046 | Reserva ajena → 404 | HU-V11 | CA-V11-03 | `qa/karate/src/test/java/trouvels360/mis-reservas.feature:73` | Pasó |
| TC-API-082 | Salida cancelada por el proveedor: reserva confirmada con aviso | HU-V12 | CA-V12-04 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:69` | Pasó |
| TC-API-047 | Cancelar hotel: estado cancelada y unidades liberadas | HU-V13 | CA-V13-02 | `qa/karate/src/test/java/trouvels360/mis-reservas.feature:85` | Pasó |
| TC-API-048 | Cancelar tour devuelve exactamente las personas al cupo | HU-V13 | CA-V13-03 | `qa/karate/src/test/java/trouvels360/mis-reservas.feature:98` | Pasó |
| TC-API-049 | Cancelar dos veces → 409; cancelar rechazada → 409; ajena → 404 | HU-V13 | CA-V13-04, CA-V13-05 | `qa/karate/src/test/java/trouvels360/mis-reservas.feature:113` | Pasó |
| TC-API-050 | Resumen: promedio y total coinciden con el conjunto completo | HU-V14, HU-P04 | CA-V14-01, CA-P04-02 | `qa/karate/src/test/java/trouvels360/resenas.feature:27` | Pasó |
| TC-API-051 | Publicar con reserva finalizada 201; sin reserva 403 con mensaje | HU-V14 | CA-V14-02, CA-V14-03 | `qa/karate/src/test/java/trouvels360/resenas.feature:62` | Pasó |
| TC-API-052 | Data-driven de longitudes de comentario y calificaciones fuera de rango → 422 | HU-V14 | CA-V14-03 | `qa/karate/src/test/java/trouvels360/resenas.feature:85` | Pasó |
| TC-API-053 | Editar/eliminar reseña ajena → 403; segunda reseña → 409 | HU-V14 | CA-V14-04, CA-V14-05 | `qa/karate/src/test/java/trouvels360/resenas.feature:105` | Pasó |
| TC-API-054 | Itinerario válido: esquema de días y actividades | HU-V15 | CA-V15-01 | `qa/karate/src/test/java/trouvels360/itinerario.feature:11` | Pasó |
| TC-API-055 | Interés inválido → 422 | HU-V15 | CA-V15-04 | `qa/karate/src/test/java/trouvels360/itinerario.feature:31` | Pasó |
| TC-API-056 | Paginación: meta.total, last_page y página 2 con los registros 16+ | HU-P03 | CA-P03-01 | `qa/karate/src/test/java/trouvels360/proveedor.feature:19` | Pasó |
| TC-API-057 | Indicadores por estado tras crear reservas en 4 estados | HU-P04 | CA-P04-01, CA-P04-03 | `qa/karate/src/test/java/trouvels360/proveedor.feature:54` | Pasó |
| TC-API-058 | Viajero en /proveedor/servicios → 403 | HU-P03 | CA-P03-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:94` | Pasó |
| TC-API-059 | Crear hotel con habitaciones y verificar en el catálogo | HU-P05 | CA-P05-01 | `qa/karate/src/test/java/trouvels360/proveedor.feature:112` | Pasó |
| TC-API-060 | 6 imágenes → 422 | HU-P05 | CA-P05-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:137` | Pasó |
| TC-API-061 | Editar hotel ajeno → 403 | HU-P06 | CA-P06-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:147` | Pasó |
| TC-API-083 | Editar descripción e imágenes conserva id y reservas | HU-P06 | CA-P06-01 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:86` | Pasó |
| TC-API-084 | Tipo de habitación inválido: no queda hotel a medias | HU-P05 | CA-P05-03 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:109` | Pasó |
| TC-API-062 | Reducir unidades bajo ocupación → 409 con mínimo | HU-P07 | CA-P07-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:166` | Pasó |
| TC-API-063 | Eliminar con reservas → 409; sin reservas → 204 | HU-P07 | CA-P07-03 | `qa/karate/src/test/java/trouvels360/proveedor.feature:185` | Pasó |
| TC-API-064 | Cambio de tarifa no altera total de reservas previas | HU-P07 | CA-P07-04 | `qa/karate/src/test/java/trouvels360/proveedor.feature:209` | Pasó |
| TC-API-085 | Tarifa 0 → 422 y tipo ajeno → 403 sin cambios | HU-P07 | CA-P07-05 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:129` | Pasó |
| TC-API-065 | Crear tour y verificar en catálogo | HU-P08 | CA-P08-01 | `qa/karate/src/test/java/trouvels360/proveedor.feature:233` | Pasó |
| TC-API-066 | Editar precio: ids de salidas y totales de reservas sin cambio | HU-P09 | CA-P09-01 | `qa/karate/src/test/java/trouvels360/proveedor.feature:258` | Pasó |
| TC-API-067 | Data-driven de límites (imágenes, items, categoría) → 422 | HU-P08, HU-P09 | CA-P08-02, CA-P09-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:282` | Pasó |
| TC-API-068 | Dos actividades sin orden reciben 1 y 2 | HU-P10 | CA-P10-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:303` | Pasó |
| TC-API-069 | Actividad en tour ajeno → 403 | HU-P10 | CA-P10-04 | `qa/karate/src/test/java/trouvels360/proveedor.feature:328` | Pasó |
| TC-API-070 | Salida duplicada → 409; fecha pasada → 422 | HU-P11 | CA-P11-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:342` | Pasó |
| TC-API-071 | cupo_total menor a reservado → 422; cupo_reservado enviado se ignora | HU-P11 | CA-P11-03 | `qa/karate/src/test/java/trouvels360/proveedor.feature:360` | Pasó |
| TC-API-072 | Salida cerrada rechaza reservas (call a reserva de viajero) | HU-P11 | CA-P11-04 | `qa/karate/src/test/java/trouvels360/proveedor.feature:377` | Pasó |
| TC-API-073 | Solo reservas de servicios propios (match each servicio.proveedor_id) | HU-P12 | CA-P12-01, CA-P12-03 | `qa/karate/src/test/java/trouvels360/proveedor.feature:397` | Pasó |
| TC-API-074 | Filtro estado=pendiente | HU-P12 | CA-P12-02 | `qa/karate/src/test/java/trouvels360/proveedor.feature:422` | Pasó |
| TC-API-075 | Detalle ajeno → 404 | HU-P12 | CA-P12-05 | `qa/karate/src/test/java/trouvels360/proveedor.feature:450` | Pasó |
| TC-API-076 | Eliminar servicio ajeno → 403 (seguridad) | HU-P15 | CA-P15-04 | `qa/karate/src/test/java/trouvels360/proveedor.feature:466` | Pasó |
| TC-API-077 | Con reserva activa → 409 | HU-P15 | CA-P15-04 | `qa/karate/src/test/java/trouvels360/proveedor.feature:489` | Pasó |
| TC-API-078 | Tras eliminar, GET /mis-reservas del viajero sigue 200 con el nombre del servicio | HU-P15 | CA-P15-03 | `qa/karate/src/test/java/trouvels360/proveedor.feature:504` | Pasó |
| TC-API-086 | Repetir la eliminación devuelve 404 sin efectos | HU-P15 | CA-P15-05 | `qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:149` | Pasó |

## Web · Selenium (TC-WEB)

| Caso | Qué comprueba | Historias | Criterios | Código | Resultado |
|---|---|---|---|---|---|
| TC-WEB-001 | Registro de viajero end-to-end y aterrizaje en catálogo | HU-V01 | CA-V01-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:27` | Pasó |
| TC-WEB-002 | Mensajes de validación en el formulario | HU-V01, HU-P01 | CA-V01-02, CA-V01-03, CA-P01-04 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:46` | Pasó |
| TC-WEB-003 | Registro de proveedor y acceso al panel del proveedor | HU-P01 | CA-P01-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:73` | Pasó |
| TC-WEB-004 | Login viajero y proveedor muestran el menú de su rol | HU-V02, HU-P02 | CA-V02-01, CA-P02-01, CA-P02-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:89` | Pasó |
| TC-WEB-005 | Login desde 'Reservar' vuelve al detalle con la selección | HU-V02 | CA-V02-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:114` | Pasó |
| TC-WEB-033 | Sesión vencida: 401, login y regreso a la página | HU-V02 | CA-V02-04 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:80` | Pasó |
| TC-WEB-006 | Editar perfil y ver el nombre actualizado en el menú | HU-V03 | CA-V03-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:136` | Pasó |
| TC-WEB-007 | Diálogo de confirmación: cancelar conserva la cuenta | HU-V04 | CA-V04-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:150` | Pasó |
| TC-WEB-008 | Búsqueda en Cusco y validación de tarjetas de resultado | HU-V05 | CA-V05-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:30` | Pasó |
| TC-WEB-009 | Validación de fechas pasadas en el buscador | HU-V05 | CA-V05-03 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:50` | Pasó |
| TC-WEB-031 | Volver desde el detalle conserva la búsqueda (hoteles y tours) | HU-V05, HU-V08 | CA-V05-06, CA-V08-04 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:28` | Pasó |
| TC-WEB-010 | Selección de 2 tipos y verificación del resumen con IGV | HU-V06 | CA-V06-02, CA-V06-03 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:66` | Pasó |
| TC-WEB-011 | Flujo completo búsqueda → selección → pago → comprobante PENDIENTE | HU-V07 | CA-V07-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:83` | Pasó |
| TC-WEB-012 | Validaciones del formulario de pago | HU-V07 | CA-V07-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:114` | Pasó |
| TC-WEB-013 | Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | HU-V12, HU-P13 | CA-V12-01, CA-P13-01, CA-P13-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java:26` | Pasó |
| TC-WEB-014 | Proveedor rechaza con motivo y el viajero ve RECHAZADA | HU-V12, HU-P14 | CA-V12-02, CA-P14-01, CA-P14-03 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java:55` | Pasó |
| TC-WEB-015 | Filtrar por categoría y destino | HU-V08 | CA-V08-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:28` | Pasó |
| TC-WEB-032 | Sin resultados vs. error con Reintentar (tours y bandeja del proveedor) | HU-V08, HU-P12 | CA-V08-03, CA-P12-04 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:59` | Pasó |
| TC-WEB-016 | Elegir salida y personas, verificar total con IGV | HU-V09 | CA-V09-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:41` | Pasó |
| TC-WEB-017 | Reserva de tour end-to-end con comprobante CONFIRMADA | HU-V10 | CA-V10-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:56` | Pasó |
| TC-WEB-018 | Mis reservas: pestañas hotel/tour, filtro por estado y paginación | HU-V11 | CA-V11-01, CA-V11-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:26` | Pasó |
| TC-WEB-019 | Reserva rechazada muestra motivo | HU-V12 | CA-V12-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:59` | Pasó |
| TC-WEB-020 | Cancelar desde Mis reservas con diálogo de confirmación | HU-V13 | CA-V13-01, CA-V13-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:76` | Pasó |
| TC-WEB-021 | Publicar, editar y eliminar reseña en el detalle | HU-V14 | CA-V14-02, CA-V14-04 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java:30` | Pasó |
| TC-WEB-022 | Generar itinerario desde el detalle del hotel | HU-V15 | CA-V15-01, CA-V15-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java:60` | Pasó |
| TC-WEB-023 | Panel: paginación y tarjetas de indicadores | HU-P03, HU-P04 | CA-P03-01, CA-P04-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:29` | Pasó |
| TC-WEB-024 | Crear hotel desde el formulario del proveedor | HU-P05 | CA-P05-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:56` | Pasó |
| TC-WEB-025 | Agregar y editar tipo de habitación | HU-P07 | CA-P07-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:75` | Pasó |
| TC-WEB-026 | Crear tour con salida desde el formulario | HU-P08 | CA-P08-01 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:96` | Pasó |
| TC-WEB-027 | Agregar, reordenar y eliminar actividades | HU-P10 | CA-P10-01, CA-P10-02, CA-P10-03 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:118` | Pasó |
| TC-WEB-028 | Crear, cerrar y eliminar salida desde el panel | HU-P11 | CA-P11-01, CA-P11-04, CA-P11-05, CA-P11-06 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:140` | Pasó |
| TC-WEB-029 | Bandeja de reservas con filtros y paginación | HU-P12 | CA-P12-01, CA-P12-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:162` | Pasó |
| TC-WEB-030 | Eliminar publicación con diálogo de confirmación | HU-P15 | CA-P15-01, CA-P15-02 | `qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:198` | Pasó |

## Móvil · Appium (TC-MOB)

| Caso | Qué comprueba | Historias | Criterios | Código | Resultado |
|---|---|---|---|---|---|
| TC-MOB-001 | Registro de viajero desde la app y sesión iniciada | HU-V01 | CA-V01-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:23` | Pendiente (ejecutar en PC) |
| TC-MOB-002 | Botón Registrarme deshabilitado con contraseñas distintas | HU-V01, HU-P01 | CA-V01-03, CA-P01-04 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:41` | Pendiente (ejecutar en PC) |
| TC-MOB-003 | Registro de proveedor desde la app y acceso al panel | HU-P01 | CA-P01-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:55` | Pendiente (ejecutar en PC) |
| TC-MOB-004 | Login, cierre de sesión y bloqueo de pestañas privadas | HU-V02 | CA-V02-01, CA-V02-03, CA-V02-05 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:70` | Pendiente (ejecutar en PC) |
| TC-MOB-005 | Reservar sin sesión → login → regreso al detalle | HU-V02 | CA-V02-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:92` | Pendiente (ejecutar en PC) |
| TC-MOB-006 | Editar perfil en la app y verificarlo en la pestaña Perfil | HU-V03 | CA-V03-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:111` | Pendiente (ejecutar en PC) |
| TC-MOB-007 | Cambio de contraseña con confirmación distinta deshabilita Guardar | HU-V03 | CA-V03-04 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:127` | Pendiente (ejecutar en PC) |
| TC-MOB-008 | Eliminar cuenta desde Perfil y volver a la pantalla de login | HU-V04 | CA-V04-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:146` | Pendiente (ejecutar en PC) |
| TC-MOB-009 | Búsqueda por ciudad y fechas en la app | HU-V05 | CA-V05-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:26` | Pendiente (ejecutar en PC) |
| TC-MOB-010 | Estado vacío vs error con Reintentar | HU-V05 | CA-V05-05 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:39` | Pendiente (ejecutar en PC) |
| TC-MOB-011 | Selección de habitaciones y total con IGV en la app | HU-V06 | CA-V06-02, CA-V06-03 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:61` | Pendiente (ejecutar en PC) |
| TC-MOB-012 | Botón + deshabilitado al llegar a las unidades disponibles | HU-V06 | CA-V06-04 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:78` | Pendiente (ejecutar en PC) |
| TC-MOB-013 | Flujo completo de reserva de hotel en la app | HU-V07 | CA-V07-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:92` | Pendiente (ejecutar en PC) |
| TC-MOB-014 | Formulario de pago rechaza tarjeta vencida | HU-V07 | CA-V07-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:116` | Pendiente (ejecutar en PC) |
| TC-MOB-015 | Proveedor acepta una solicitud desde la app | HU-P13 | CA-P13-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java:25` | Pendiente (ejecutar en PC) |
| TC-MOB-016 | Proveedor rechaza con motivo desde la app | HU-P14 | CA-P14-01, CA-P14-03 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java:55` | Pendiente (ejecutar en PC) |
| TC-MOB-017 | Chips de categoría y búsqueda por destino | HU-V08 | CA-V08-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:26` | Pendiente (ejecutar en PC) |
| TC-MOB-018 | Elegir salida y personas en la app; + se deshabilita al tope de cupos | HU-V09 | CA-V09-02, CA-V09-03 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:48` | Pendiente (ejecutar en PC) |
| TC-MOB-019 | Reserva de tour end-to-end en la app | HU-V10 | CA-V10-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:62` | Pendiente (ejecutar en PC) |
| TC-MOB-020 | Mis reservas en la app coincide con la web (mismo código y total) | HU-V11 | CA-V11-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:24` | Pendiente (ejecutar en PC) |
| TC-MOB-021 | Scroll infinito / paginación de reservas | HU-V11 | CA-V11-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:44` | Pendiente (ejecutar en PC) |
| TC-MOB-022 | Cancelar desde la app y ver estado CANCELADA | HU-V13 | CA-V13-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:66` | Pendiente (ejecutar en PC) |
| TC-MOB-023 | Publicar reseña desde la app | HU-V14 | CA-V14-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java:21` | Pendiente (ejecutar en PC) |
| TC-MOB-024 | Generar itinerario en la app y continuar con la reserva | HU-V15 | CA-V15-01, CA-V15-03 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java:46` | Pendiente (ejecutar en PC) |
| TC-MOB-025 | Panel del proveedor en la app con indicadores | HU-P03, HU-P04 | CA-P03-01, CA-P04-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:31` | Pendiente (ejecutar en PC) |
| TC-MOB-026 | Crear hotel desde la app del proveedor | HU-P05 | CA-P05-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:50` | Pendiente (ejecutar en PC) |
| TC-MOB-027 | Agregar tipo de habitación en la app | HU-P07 | CA-P07-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:70` | Pendiente (ejecutar en PC) |
| TC-MOB-028 | Crear tour desde la app | HU-P08 | CA-P08-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:92` | Pendiente (ejecutar en PC) |
| TC-MOB-029 | Agregar actividad desde la app | HU-P10 | CA-P10-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:114` | Pendiente (ejecutar en PC) |
| TC-MOB-030 | Crear y cerrar salida desde la app | HU-P11 | CA-P11-01, CA-P11-04, CA-P11-06 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:129` | Pendiente (ejecutar en PC) |
| TC-MOB-031 | Bandeja de reservas en la app | HU-P12 | CA-P12-01 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:148` | Pendiente (ejecutar en PC) |
| TC-MOB-032 | Eliminar publicación desde la app | HU-P15 | CA-P15-02 | `qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:170` | Pendiente (ejecutar en PC) |

## Rendimiento · JMeter (TC-PERF)

| Caso | Qué comprueba | Historias | Criterios | Código | Resultado |
|---|---|---|---|---|---|
| TC-PERF-001 | Login con 50 usuarios concurrentes desde CSV: p95 < 800 ms, 0% errores | HU-V02 | CA-V02-07 | `qa/jmeter/trouvels360.jmx` | Pendiente (ejecutar en PC) |
| TC-PERF-002 | Carga de catálogo: 100 usuarios, ramp-up 60 s, ciudades desde CSV, p95 < 1 s | HU-V05 | CA-V05-07 | `qa/jmeter/trouvels360.jmx` | Pendiente (ejecutar en PC) |
| TC-PERF-003 | 20 hilos simultáneos (Synchronizing Timer) por la última unidad: exactamente 1×201 y 19×409 | HU-V07 | CA-V07-05 | `qa/jmeter/trouvels360.jmx` | Pasó (smoke Python) |
| TC-PERF-004 | Carga de búsqueda de tours: 100 usuarios, categorías desde CSV, p95 < 1 s | HU-V08 | CA-V08-05 | `qa/jmeter/trouvels360.jmx` | Pendiente (ejecutar en PC) |
| TC-PERF-005 | 30 hilos simultáneos sobre salida de 10 cupos: suma de personas confirmadas <= 10 | HU-V10 | CA-V10-04 | `qa/jmeter/trouvels360.jmx` | Pasó (smoke Python) |
| TC-PERF-006 | Panel del proveedor con 30 usuarios concurrentes, p95 < 1,5 s | HU-P03 | CA-P03-03 | `qa/jmeter/trouvels360.jmx` | Pendiente (ejecutar en PC) |

## Manuales (TC-MAN)

| Caso | Qué comprueba | Historias | Criterios | Código | Resultado |
|---|---|---|---|---|---|
| TC-MAN-001 | Error de servidor al guardar el perfil: mensaje con reintentar y sin éxito falso | HU-V03 | CA-V03-06 | `Manual (pasos en el catálogo)` | Pendiente (manual) |
| TC-MAN-002 | Corte de conexión al cancelar: al refrescar se ve el estado real | HU-V13 | CA-V13-05 | `Manual (pasos en el catálogo)` | Pendiente (manual) |
| TC-MAN-003 | Microservicio de itinerarios caído: aviso con Reintentar y se puede seguir con el hotel | HU-V15 | CA-V15-05 | `Manual (pasos en el catálogo)` | Pendiente (manual) |

### Pasos de los casos manuales

**TC-MAN-001 · Error de servidor al guardar el perfil: mensaje con reintentar y sin éxito falso** (HU-V03)

Con la sesión abierta en Mi cuenta, detener el backend (o bloquear /api/auth/me en DevTools > Network request blocking), cambiar el nombre y Guardar. Esperado: mensaje de error, el nombre del menú no cambia; al reactivar y reintentar se guarda.

**TC-MAN-002 · Corte de conexión al cancelar: al refrescar se ve el estado real** (HU-V13)

En Mis reservas, con DevTools en 'Offline' justo después de pulsar 'Sí, cancelar', volver a 'Online' y refrescar. Esperado: no aparece un mensaje de éxito falso; la tarjeta muestra el estado guardado en la API (TC-API-049 confirma que repetir la cancelación es seguro).

**TC-MAN-003 · Microservicio de itinerarios caído: aviso con Reintentar y se puede seguir con el hotel** (HU-V15)

Detener el microservicio Python (:8001), en el detalle del hotel pulsar Reservar → 'Sí, generar itinerario'. Esperado: 'No pudimos generar el itinerario…' con opción de reintentar y 'No, continuar al pago' funciona. La API responde 503 con success=false.
