# Equivalencias con los identificadores anteriores

[← Índice de historias](README.md)

Las historias se reorganizaron en 15 del viajero y 15 del proveedor. **No se perdió ningún criterio**: los 119 criterios de la versión anterior (CA-TRV-xxx-yy) están dentro de los 130 criterios nuevos (algunos se dividieron para que cada uno diga una sola cosa, y se agregaron criterios de rendimiento que antes estaban implícitos). Los 160 casos de prueba conservan su identificador.

> **En el código ya se usan los identificadores nuevos:** las suites de `qa/` etiquetan cada caso con sus historias (`@HU-V07 @TC-API-026` en Karate, `@Tag("HU-V07")` en Selenium y Appium), y los comentarios del backend, la web y la app citan `HU-xxx` o `CA-xxx`. Estas tablas sirven para leer los documentos y entregas anteriores.

## Historias anteriores → historias nuevas

| Anterior | Título anterior | Ahora |
|---|---|---|
| TRV-101 | Registro de cuenta de viajero | HU-V01 Crear mi cuenta de viajero |
| TRV-102 | Registro de cuenta de proveedor | HU-P01 Crear mi cuenta de proveedor |
| TRV-103 | Inicio y cierre de sesión con retorno al destino | HU-V02 Iniciar sesión y volver a lo que estaba haciendo, HU-P02 Entrar a mi panel y mantener los datos de mi empresa |
| TRV-104 | Gestionar perfil y contraseña | HU-V03 Actualizar mis datos y mi contraseña, HU-P02 Entrar a mi panel y mantener los datos de mi empresa |
| TRV-105 | Eliminar mi cuenta conservando el historial | HU-V04 Eliminar mi cuenta |
| TRV-201 | Buscar hoteles por destino, fechas y huéspedes | HU-V05 Buscar hoteles disponibles |
| TRV-202 | Ver detalle del hotel y cotizar habitaciones con IGV | HU-V06 Ver un hotel y cotizar mi estadía |
| TRV-203 | Solicitar reserva de hotel con pago simulado | HU-V07 Reservar un hotel con pago simulado |
| TRV-204 | Aprobar o rechazar solicitudes de hotel | HU-V12 Saber si el hotel aceptó o rechazó mi solicitud, HU-P12 Ver las reservas que recibo, HU-P13 Aceptar una solicitud de hotel, HU-P14 Rechazar una solicitud de hotel |
| TRV-301 | Buscar tours por destino, categoría y fechas | HU-V08 Buscar tours |
| TRV-302 | Ver detalle del tour y elegir salida y participantes | HU-V09 Ver un tour y cotizar |
| TRV-303 | Reservar tour con confirmación automática | HU-V10 Reservar un tour |
| TRV-401 | Consultar mis reservas y sus estados | HU-V11 Ver mis reservas, HU-V12 Saber si el hotel aceptó o rechazó mi solicitud |
| TRV-402 | Cancelar mis reservas | HU-V13 Cancelar una reserva |
| TRV-403 | Publicar, editar y eliminar reseñas | HU-V14 Leer y escribir reseñas |
| TRV-404 | Generar un itinerario sugerido para mi estadía | HU-V15 Recibir un itinerario sugerido |
| TRV-501 | Panel de publicaciones e indicadores | HU-P03 Ver mis publicaciones, HU-P04 Ver los indicadores de mi negocio |
| TRV-502 | Publicar y editar hoteles | HU-P05 Publicar un hotel, HU-P06 Editar un hotel |
| TRV-503 | Gestionar tipos de habitación | HU-P07 Gestionar los tipos de habitación |
| TRV-504 | Publicar y editar tours | HU-P08 Publicar un tour, HU-P09 Editar un tour |
| TRV-505 | Gestionar actividades del tour | HU-P10 Armar el programa de actividades del tour |
| TRV-506 | Gestionar salidas y cupos | HU-P11 Programar salidas y cupos |
| TRV-507 | Consultar reservas recibidas | HU-P12 Ver las reservas que recibo |
| TRV-508 | Eliminar publicaciones propias | HU-P15 Eliminar una publicación |

| Tarea anterior | Ahora |
|---|---|
| TRV-601 | QA-01 Suite de pruebas de API con Karate |
| TRV-602 | QA-02 Plan de rendimiento y concurrencia con JMeter |
| TRV-603 | QA-03 Automatización web con Selenium |
| TRV-604 | QA-04 Automatización móvil con Appium |
| TRV-605 | QA-05 Trazabilidad y reportes en Jira |

## Historias nuevas → anteriores

| Nueva | Título | Viene de |
|---|---|---|
| HU-V01 | Crear mi cuenta de viajero | TRV-101 |
| HU-V02 | Iniciar sesión y volver a lo que estaba haciendo | TRV-103 |
| HU-V03 | Actualizar mis datos y mi contraseña | TRV-104 |
| HU-V04 | Eliminar mi cuenta | TRV-105 |
| HU-V05 | Buscar hoteles disponibles | TRV-201 |
| HU-V06 | Ver un hotel y cotizar mi estadía | TRV-202 |
| HU-V07 | Reservar un hotel con pago simulado | TRV-203 |
| HU-V08 | Buscar tours | TRV-301 |
| HU-V09 | Ver un tour y cotizar | TRV-302 |
| HU-V10 | Reservar un tour | TRV-303 |
| HU-V11 | Ver mis reservas | TRV-401 |
| HU-V12 | Saber si el hotel aceptó o rechazó mi solicitud | TRV-401, TRV-204 |
| HU-V13 | Cancelar una reserva | TRV-402 |
| HU-V14 | Leer y escribir reseñas | TRV-403 |
| HU-V15 | Recibir un itinerario sugerido | TRV-404 |
| HU-P01 | Crear mi cuenta de proveedor | TRV-102 |
| HU-P02 | Entrar a mi panel y mantener los datos de mi empresa | TRV-103, TRV-104 |
| HU-P03 | Ver mis publicaciones | TRV-501 |
| HU-P04 | Ver los indicadores de mi negocio | TRV-501 |
| HU-P05 | Publicar un hotel | TRV-502 |
| HU-P06 | Editar un hotel | TRV-502 |
| HU-P07 | Gestionar los tipos de habitación | TRV-503 |
| HU-P08 | Publicar un tour | TRV-504 |
| HU-P09 | Editar un tour | TRV-504 |
| HU-P10 | Armar el programa de actividades del tour | TRV-505 |
| HU-P11 | Programar salidas y cupos | TRV-506 |
| HU-P12 | Ver las reservas que recibo | TRV-507, TRV-204 |
| HU-P13 | Aceptar una solicitud de hotel | TRV-204 |
| HU-P14 | Rechazar una solicitud de hotel | TRV-204 |
| HU-P15 | Eliminar una publicación | TRV-508 |

## Criterios anteriores → criterios nuevos

| Criterio anterior | Título anterior | Criterio(s) nuevo(s) |
|---|---|---|
| CA-TRV-101-01 | Registro exitoso | CA-V01-01 |
| CA-TRV-101-02 | Campos inválidos | CA-V01-02 |
| CA-TRV-101-03 | Contraseñas distintas | CA-V01-03 |
| CA-TRV-101-04 | Correo duplicado | CA-V01-04 |
| CA-TRV-101-05 | Rol no manipulable | CA-V01-05 |
| CA-TRV-102-01 | Registro exitoso | CA-P01-01 |
| CA-TRV-102-02 | RUC inválido o duplicado | CA-P01-02 |
| CA-TRV-102-03 | Teléfono inválido | CA-P01-03 |
| CA-TRV-102-04 | Correo duplicado o contraseñas distintas | CA-P01-04 |
| CA-TRV-103-01 | Login correcto | CA-V02-01, CA-V02-06, CA-V02-07, CA-P02-01 |
| CA-TRV-103-02 | Retorno a la reserva | CA-V02-02 |
| CA-TRV-103-03 | Credenciales incorrectas | CA-V02-03 |
| CA-TRV-103-04 | Sesión vencida | CA-V02-04 |
| CA-TRV-103-05 | Cierre de sesión | CA-V02-05 |
| CA-TRV-103-06 | Separación de roles | CA-P02-02 |
| CA-TRV-104-01 | Editar datos | CA-V03-01 |
| CA-TRV-104-02 | Datos inválidos | CA-V03-02, CA-P02-03 |
| CA-TRV-104-03 | Intento de cambiar rol | CA-V03-03 |
| CA-TRV-104-04 | Cambiar contraseña | CA-V03-04 |
| CA-TRV-104-05 | Contraseña actual incorrecta | CA-V03-05 |
| CA-TRV-104-06 | Error de servidor | CA-V03-06 |
| CA-TRV-105-01 | Desistir | CA-V04-01 |
| CA-TRV-105-02 | Eliminación permitida | CA-V04-02 |
| CA-TRV-105-03 | Bloqueo por reservas activas | CA-V04-03 |
| CA-TRV-105-04 | Historial conservado | CA-V04-04 |
| CA-TRV-201-01 | Búsqueda válida | CA-V05-01, CA-V05-02, CA-V05-07 |
| CA-TRV-201-02 | Fechas inválidas | CA-V05-03 |
| CA-TRV-201-03 | Cantidades inválidas | CA-V05-04 |
| CA-TRV-201-04 | Sin resultados vs error | CA-V05-05 |
| CA-TRV-201-05 | Conservar búsqueda | CA-V05-06 |
| CA-TRV-202-01 | Detalle completo | CA-V06-01 |
| CA-TRV-202-02 | Selección múltiple | CA-V06-02 |
| CA-TRV-202-03 | Cálculo con IGV | CA-V06-03 |
| CA-TRV-202-04 | Cantidad excedida | CA-V06-04 |
| CA-TRV-202-05 | Recalcular sin reservar | CA-V06-05 |
| CA-TRV-203-01 | Solicitud registrada | CA-V07-01 |
| CA-TRV-203-02 | Pago inválido | CA-V07-02 |
| CA-TRV-203-03 | Unidades ocupadas | CA-V07-03 |
| CA-TRV-203-04 | Disponibilidad insuficiente | CA-V07-04 |
| CA-TRV-203-05 | Concurrencia | CA-V07-05 |
| CA-TRV-203-06 | Envío repetido | CA-V07-06 |
| CA-TRV-204-01 | Aceptar | CA-V12-01, CA-P13-01 |
| CA-TRV-204-02 | Rechazar | CA-V12-02, CA-P14-01, CA-P14-02, CA-P14-03 |
| CA-TRV-204-03 | Sin decisión | CA-P13-02 |
| CA-TRV-204-04 | Decisión repetida o estado incompatible | CA-P13-03 |
| CA-TRV-204-05 | Reserva ajena | CA-P13-04 |
| CA-TRV-204-06 | Tours sin aprobación | CA-V12-03, CA-P12-03 |
| CA-TRV-301-01 | Búsqueda válida | CA-V08-01, CA-V08-05 |
| CA-TRV-301-02 | Filtros inválidos | CA-V08-02 |
| CA-TRV-301-03 | Sin resultados o error | CA-V08-03 |
| CA-TRV-301-04 | Conservar búsqueda | CA-V08-04 |
| CA-TRV-302-01 | Detalle | CA-V09-01 |
| CA-TRV-302-02 | Cálculo | CA-V09-02 |
| CA-TRV-302-03 | Selección inválida | CA-V09-03 |
| CA-TRV-302-04 | Sin salidas | CA-V09-04 |
| CA-TRV-303-01 | Reserva confirmada | CA-V10-01 |
| CA-TRV-303-02 | Pago inválido o desistir | CA-V10-02 |
| CA-TRV-303-03 | Salida no válida | CA-V10-03 |
| CA-TRV-303-04 | Concurrencia | CA-V10-04 |
| CA-TRV-303-05 | Envío repetido | CA-V10-05 |
| CA-TRV-401-01 | Listado propio | CA-V11-01 |
| CA-TRV-401-02 | Pendiente y aceptada | CA-V12-01 |
| CA-TRV-401-03 | Rechazada | CA-V12-02 |
| CA-TRV-401-04 | Tour con salida cancelada | CA-V12-04 |
| CA-TRV-401-05 | Consistencia entre canales | CA-V11-02 |
| CA-TRV-401-06 | Vacío, error y ajeno | CA-V11-03 |
| CA-TRV-402-01 | Confirmar o desistir | CA-V13-01 |
| CA-TRV-402-02 | Cancelación exitosa | CA-V13-02 |
| CA-TRV-402-03 | Liberar oferta una vez | CA-V13-03 |
| CA-TRV-402-04 | No cancelable | CA-V13-04 |
| CA-TRV-402-05 | Respuesta incierta | CA-V13-05 |
| CA-TRV-403-01 | Consultar | CA-V14-01 |
| CA-TRV-403-02 | Publicar | CA-V14-02 |
| CA-TRV-403-03 | No permitido | CA-V14-03 |
| CA-TRV-403-04 | Editar y eliminar | CA-V14-04 |
| CA-TRV-403-05 | Sin duplicados | CA-V14-05 |
| CA-TRV-404-01 | Propuesta | CA-V15-01 |
| CA-TRV-404-02 | Sugerencia, no reserva | CA-V15-02 |
| CA-TRV-404-03 | Continuar | CA-V15-03 |
| CA-TRV-404-04 | Validación | CA-V15-04 |
| CA-TRV-404-05 | Servicio caído | CA-V15-05 |
| CA-TRV-501-01 | Listado propio | CA-P03-01, CA-P03-03 |
| CA-TRV-501-02 | Indicadores por estado | CA-P04-01 |
| CA-TRV-501-03 | Reseñas | CA-P04-02 |
| CA-TRV-501-04 | Actualización | CA-P04-03 |
| CA-TRV-501-05 | Vacío y no autorizado | CA-P03-02 |
| CA-TRV-502-01 | Publicar | CA-P05-01 |
| CA-TRV-502-02 | Galería | CA-P05-02 |
| CA-TRV-502-03 | Editar | CA-P06-01 |
| CA-TRV-502-04 | Inválido o ajeno | CA-P06-02 |
| CA-TRV-502-05 | Transacción | CA-P05-03 |
| CA-TRV-503-01 | Agregar o editar | CA-P07-01 |
| CA-TRV-503-02 | Reducir bajo ocupación | CA-P07-02 |
| CA-TRV-503-03 | Eliminar con reservas | CA-P07-03 |
| CA-TRV-503-04 | Historial | CA-P07-04 |
| CA-TRV-503-05 | Inválido o ajeno | CA-P07-05 |
| CA-TRV-504-01 | Publicar | CA-P08-01 |
| CA-TRV-504-02 | Límites | CA-P08-02 |
| CA-TRV-504-03 | Editar sin afectar reservas | CA-P09-01 |
| CA-TRV-504-04 | Inválido o ajeno | CA-P09-02 |
| CA-TRV-505-01 | Consultar | CA-P10-01 |
| CA-TRV-505-02 | Agregar o editar | CA-P10-02 |
| CA-TRV-505-03 | Eliminar | CA-P10-03 |
| CA-TRV-505-04 | Inválido o ajeno | CA-P10-04 |
| CA-TRV-506-01 | Crear | CA-P11-01 |
| CA-TRV-506-02 | Duplicado o fecha pasada | CA-P11-02 |
| CA-TRV-506-03 | Editar con reservas | CA-P11-03 |
| CA-TRV-506-04 | Cerrar o cancelar | CA-P11-04 |
| CA-TRV-506-05 | Eliminar | CA-P11-05 |
| CA-TRV-506-06 | Consistencia | CA-P11-06 |
| CA-TRV-507-01 | Listado y detalle | CA-P12-01 |
| CA-TRV-507-02 | Filtros | CA-P12-02 |
| CA-TRV-507-03 | Vacío y error | CA-P12-04 |
| CA-TRV-507-04 | Ajeno | CA-P12-05 |
| CA-TRV-508-01 | Confirmar o desistir | CA-P15-01 |
| CA-TRV-508-02 | Eliminación permitida | CA-P15-02 |
| CA-TRV-508-03 | Historial | CA-P15-03 |
| CA-TRV-508-04 | Ajeno o con reservas activas | CA-P15-04 |
| CA-TRV-508-05 | Repetición | CA-P15-05 |

## Versión base (20 historias HU-VIA / HU-PRO)

La primera versión del repositorio tenía 10 historias por rol, centradas en endpoints (están en el historial de Git).

| Versión base | Ahora |
|---|---|
| HU-VIA-001 Registrarme y gestionar mi sesión como viajero | HU-V01, HU-V02 |
| HU-VIA-002 Gestionar mi perfil y cuenta de viajero | HU-V03, HU-V04 |
| HU-VIA-003 Buscar hoteles y consultar disponibilidad | HU-V05, HU-V06 |
| HU-VIA-004 Solicitar reserva de hotel | HU-V07 |
| HU-VIA-005 Consultar mis reservas y estados actuales | HU-V11, HU-V12 |
| HU-VIA-006 Buscar tours y consultar salidas programadas | HU-V08, HU-V09 |
| HU-VIA-007 Reservar un tour y asegurar cupos | HU-V10 |
| HU-VIA-008 Cancelar reservas propias | HU-V13 |
| HU-VIA-009 Generar sugerencias de itinerario optimizado | HU-V15 |
| HU-VIA-010 Consultar, publicar y gestionar reseñas | HU-V14 |
| HU-PRO-001 Registrarme y gestionar mi sesión como proveedor | HU-P01, HU-P02 |
| HU-PRO-002 Gestionar mi perfil y cuenta de proveedor | HU-P02 (y HU-V03, HU-V04, que aplican a ambos roles) |
| HU-PRO-003 Consultar mis publicaciones e indicadores | HU-P03, HU-P04 |
| HU-PRO-004 Publicar y editar hoteles | HU-P05, HU-P06 |
| HU-PRO-005 Gestionar habitaciones de un hotel | HU-P07 |
| HU-PRO-006 Publicar y editar tours | HU-P08, HU-P09 |
| HU-PRO-007 Gestionar actividades del itinerario del tour | HU-P10 |
| HU-PRO-008 Gestionar salidas y cupos del tour | HU-P11 |
| HU-PRO-009 Consultar reservas recibidas de mis servicios | HU-P12, HU-P13, HU-P14 |
| HU-PRO-010 Retirar y eliminar publicaciones propias | HU-P15 |
