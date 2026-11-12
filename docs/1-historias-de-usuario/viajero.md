# Historias de usuario del viajero

[← Índice de historias](README.md) · [Historias del proveedor](proveedor.md)

**15 historias** en 4 épicas · **76 criterios de aceptación** · **104 casos de prueba** · **59 story points**

El viajero busca, cotiza y reserva hoteles y tours con pago simulado, sigue el estado de sus reservas, las cancela, opina y recibe un itinerario sugerido. Buscar y ver el catálogo no requiere sesión; reservar sí.

## Resumen

| Épica | Id | Historia | Prioridad | Pts | Criterios | Casos |
|---|---|---|---|---|---|---|
| EV1 Mi cuenta | [HU-V01](#hu-v01--crear-mi-cuenta-de-viajero) | Crear mi cuenta de viajero | Alta | 3 | 5 | 8 |
| EV1 Mi cuenta | [HU-V02](#hu-v02--iniciar-sesión-y-volver-a-lo-que-estaba-haciendo) | Iniciar sesión y volver a lo que estaba haciendo | Más alta | 5 | 7 | 10 |
| EV1 Mi cuenta | [HU-V03](#hu-v03--actualizar-mis-datos-y-mi-contraseña) | Actualizar mis datos y mi contraseña | Media | 3 | 6 | 8 |
| EV1 Mi cuenta | [HU-V04](#hu-v04--eliminar-mi-cuenta) | Eliminar mi cuenta | Baja | 2 | 4 | 5 |
| EV2 Hoteles | [HU-V05](#hu-v05--buscar-hoteles-disponibles) | Buscar hoteles disponibles | Más alta | 5 | 7 | 9 |
| EV2 Hoteles | [HU-V06](#hu-v06--ver-un-hotel-y-cotizar-mi-estadía) | Ver un hotel y cotizar mi estadía | Alta | 3 | 5 | 7 |
| EV2 Hoteles | [HU-V07](#hu-v07--reservar-un-hotel-con-pago-simulado) | Reservar un hotel con pago simulado | Más alta | 8 | 6 | 10 |
| EV3 Tours | [HU-V08](#hu-v08--buscar-tours) | Buscar tours | Alta | 3 | 5 | 7 |
| EV3 Tours | [HU-V09](#hu-v09--ver-un-tour-y-cotizar) | Ver un tour y cotizar | Alta | 3 | 4 | 6 |
| EV3 Tours | [HU-V10](#hu-v10--reservar-un-tour) | Reservar un tour | Más alta | 5 | 5 | 7 |
| EV4 Mis reservas y mi viaje | [HU-V11](#hu-v11--ver-mis-reservas) | Ver mis reservas | Alta | 3 | 3 | 5 |
| EV4 Mis reservas y mi viaje | [HU-V12](#hu-v12--saber-si-el-hotel-aceptó-o-rechazó-mi-solicitud) | Saber si el hotel aceptó o rechazó mi solicitud | Alta | 3 | 4 | 7 |
| EV4 Mis reservas y mi viaje | [HU-V13](#hu-v13--cancelar-una-reserva) | Cancelar una reserva | Alta | 3 | 5 | 6 |
| EV4 Mis reservas y mi viaje | [HU-V14](#hu-v14--leer-y-escribir-reseñas) | Leer y escribir reseñas | Media | 5 | 5 | 6 |
| EV4 Mis reservas y mi viaje | [HU-V15](#hu-v15--recibir-un-itinerario-sugerido) | Recibir un itinerario sugerido | Baja | 5 | 5 | 5 |

## Épica EV1 · Mi cuenta

_Crear la cuenta, entrar y salir, mantener mis datos y darme de baja._

### HU-V01 · Crear mi cuenta de viajero

> **Como** viajero, **quiero** crear mi cuenta con mis datos personales **para** poder reservar hoteles y tours a mi nombre.

**En pocas palabras:** Me registro con nombre, apellido, correo y contraseña. Al terminar ya quedo con la sesión iniciada y veo el catálogo de hoteles.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV1 Mi cuenta | Alta | 3 | Web · Móvil · API | TRV-101 |

**Reglas de negocio**

1. Nombre y apellido son obligatorios (máximo 100 caracteres cada uno).
2. El correo debe ser válido (máximo 150 caracteres) y único, sin importar mayúsculas; tampoco se reutiliza el de una cuenta eliminada.
3. La contraseña tiene al menos 6 caracteres y se escribe dos veces igual.
4. Solo existen los roles `viajero` y `proveedor`.
5. Al registrarse, la sesión queda iniciada (cookie HttpOnly `access_token`).
6. Máximo 10 registros por minuto por IP (configurable con `REGISTER_RATE_LIMIT`); al exceder, 429.

**Criterios de aceptación**

**CA-V01-01 · Registro exitoso**

- **Dado** que completo nombre, apellido, un correo nuevo y una contraseña de 6 o más caracteres repetida igual
- **Cuando** envío el registro
- **Entonces** se crea una sola cuenta de viajero, la API responde 201 con mis datos (sin la contraseña) y quedo con la sesión iniciada

  _Se prueba con:_ TC-API-001, TC-WEB-001, TC-MOB-001

**CA-V01-02 · Datos incompletos o inválidos**

- **Dado** que dejo un campo obligatorio vacío o el correo no tiene formato válido
- **Cuando** intento registrarme
- **Entonces** el formulario marca el campo con su mensaje, la API responde 422 indicando el campo y no se crea la cuenta

  _Se prueba con:_ TC-API-002, TC-WEB-002

**CA-V01-03 · Contraseñas distintas**

- **Dado** que la confirmación no es igual a la contraseña
- **Cuando** reviso el formulario
- **Entonces** veo «Las contraseñas no coinciden», no puedo enviar el registro y no se crea la cuenta

  _Se prueba con:_ TC-API-002, TC-WEB-002, TC-MOB-002

**CA-V01-04 · Correo ya registrado**

- **Dado** que ya existe una cuenta con ese correo, aunque lo escriba con otras mayúsculas
- **Cuando** envío el registro
- **Entonces** la API responde 422 «El correo ya está registrado» y no se crea una segunda cuenta

  _Se prueba con:_ TC-API-003

**CA-V01-05 · Rol no permitido**

- **Dado** que alguien llama a la API con un rol que no es viajero ni proveedor (por ejemplo `admin`)
- **Cuando** envía el registro
- **Entonces** la API responde 422 y no se crea la cuenta

  _Se prueba con:_ TC-API-004

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-001 | API (Karate) | Registro viajero válido devuelve 201 y esquema de usuario sin password | CA-V01-01 | Pasó |
| TC-API-002 | API (Karate) | Data-driven (CSV) de campos inválidos devuelve 422 con error por campo | CA-V01-02, CA-V01-03 | Pasó |
| TC-API-003 | API (Karate) | Correo duplicado en mayúsculas devuelve 422 | CA-V01-04 | Pasó |
| TC-API-004 | API (Karate) | Rol 'admin' devuelve 422 | CA-V01-05 | Pasó |
| TC-WEB-001 | Web (Selenium) | Registro de viajero end-to-end y aterrizaje en catálogo | CA-V01-01 | Pasó |
| TC-WEB-002 | Web (Selenium) | Mensajes de validación en el formulario | CA-V01-02, CA-V01-03 | Pasó |
| TC-MOB-001 | Móvil (Appium) | Registro de viajero desde la app y sesión iniciada | CA-V01-01 | Pendiente (ejecutar en PC) |
| TC-MOB-002 | Móvil (Appium) | Botón Registrarme deshabilitado con contraseñas distintas | CA-V01-03 | Pendiente (ejecutar en PC) |

**API:** `GET /api/auth/csrf` · `POST /api/auth/register`

---

### HU-V02 · Iniciar sesión y volver a lo que estaba haciendo

> **Como** viajero, **quiero** iniciar y cerrar sesión de forma segura **para** reservar sin perder lo que ya había elegido.

**En pocas palabras:** Entro con mi correo y contraseña. Si me pidió iniciar sesión en medio de una reserva, vuelvo a esa misma pantalla con mi selección. Al salir, mi sesión queda cerrada de verdad.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV1 Mi cuenta | Más alta | 5 | Web · Móvil · API | TRV-103 |

**Reglas de negocio**

1. La sesión es un JWT en una cookie HttpOnly que dura 60 minutos; `/auth/refresh` la renueva.
2. Máximo 6 intentos de inicio de sesión por minuto por correo e IP (configurable con `LOGIN_RATE_LIMIT`); al exceder, 429.
3. Toda petición que cambia datos debe llevar el header `X-XSRF-TOKEN` igual a la cookie `XSRF-TOKEN` (protección CSRF de doble envío).
4. Cerrar sesión invalida el token en el servidor, no solo en el navegador o la app.

**Criterios de aceptación**

**CA-V02-01 · Ingreso correcto**

- **Dado** que tengo una cuenta de viajero activa
- **Cuando** ingreso mi correo y contraseña correctos
- **Entonces** la API responde 200 con mis datos y la cookie `access_token` HttpOnly, y veo el menú de viajero

  _Se prueba con:_ TC-API-008, TC-WEB-004, TC-MOB-004

**CA-V02-02 · Vuelvo a mi reserva**

- **Dado** que pulsé «Reservar» sin sesión, con fechas y habitaciones ya elegidas
- **Cuando** inicio sesión
- **Entonces** vuelvo al detalle del hotel o tour con la misma selección

  _Se prueba con:_ TC-WEB-005, TC-MOB-005

**CA-V02-03 · Datos incorrectos**

- **Dado** que la contraseña es incorrecta o falta un campo
- **Cuando** intento ingresar
- **Entonces** veo «Credenciales inválidas» (401) o el campo que falta (422) y no se crea ninguna sesión

  _Se prueba con:_ TC-API-010, TC-MOB-004

**CA-V02-04 · Sesión vencida**

- **Dado** que mi sesión venció
- **Cuando** intento hacer algo privado
- **Entonces** la API responde 401, se limpia la sesión local, me lleva al login y al entrar vuelvo a la misma página

  _Se prueba con:_ TC-WEB-033, TC-API-011

**CA-V02-05 · Cerrar sesión de verdad**

- **Dado** que cerré sesión
- **Cuando** alguien reutiliza mi token anterior
- **Entonces** la API responde 401 y las secciones privadas vuelven a pedir iniciar sesión

  _Se prueba con:_ TC-API-011, TC-MOB-004

**CA-V02-06 · Protección contra peticiones falsificadas**

- **Dado** que llega una petición que cambia datos sin el header `X-XSRF-TOKEN`
- **Cuando** la API la recibe
- **Entonces** responde 403 y no cambia nada

  _Se prueba con:_ TC-API-009

**CA-V02-07 · Login con muchos usuarios a la vez**

- **Dado** que 50 usuarios inician sesión al mismo tiempo
- **Cuando** se procesan los ingresos
- **Entonces** el 95 % responde en menos de 800 ms y no hay errores

  _Se prueba con:_ TC-PERF-001

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-008 | API (Karate) | Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | CA-V02-01 | Pasó |
| TC-API-009 | API (Karate) | POST sin X-XSRF-TOKEN devuelve 403 | CA-V02-06 | Pasó |
| TC-API-010 | API (Karate) | Credenciales inválidas 401 y faltantes 422 | CA-V02-03 | Pasó |
| TC-API-011 | API (Karate) | Token reutilizado tras logout devuelve 401 | CA-V02-04, CA-V02-05 | Pasó |
| TC-WEB-004 | Web (Selenium) | Login viajero y proveedor muestran el menú de su rol | CA-V02-01 | Pasó |
| TC-WEB-005 | Web (Selenium) | Login desde 'Reservar' vuelve al detalle con la selección | CA-V02-02 | Pasó |
| TC-WEB-033 | Web (Selenium) | Sesión vencida: 401, login y regreso a la página | CA-V02-04 | Pasó |
| TC-MOB-004 | Móvil (Appium) | Login, cierre de sesión y bloqueo de pestañas privadas | CA-V02-01, CA-V02-03, CA-V02-05 | Pendiente (ejecutar en PC) |
| TC-MOB-005 | Móvil (Appium) | Reservar sin sesión → login → regreso al detalle | CA-V02-02 | Pendiente (ejecutar en PC) |
| TC-PERF-001 | Rendimiento (JMeter) | Login con 50 usuarios concurrentes desde CSV: p95 < 800 ms, 0% errores | CA-V02-07 | Pendiente (ejecutar en PC) |

**API:** `GET /api/auth/csrf` · `POST /api/auth/login` · `GET /api/auth/me` · `POST /api/auth/refresh` · `POST /api/auth/logout`

---

### HU-V03 · Actualizar mis datos y mi contraseña

> **Como** viajero, **quiero** corregir mis datos y cambiar mi contraseña **para** mantener mi cuenta al día y segura.

**En pocas palabras:** Desde «Mi perfil» cambio mi nombre, apellido y foto, y puedo cambiar la contraseña escribiendo la actual. Nadie puede cambiar su propio rol.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV1 Mi cuenta | Media | 3 | Web · Móvil · API | TRV-104 |

**Reglas de negocio**

1. Cada usuario solo ve y modifica su propia cuenta (`/usuarios/me`).
2. El viajero edita nombre, apellido y foto (`avatar_url`: URL http(s) de hasta 500 caracteres). Los datos de empresa del proveedor están en HU-P02.
3. Enviar el campo `rol` está prohibido (422).
4. Cambiar la contraseña pide la actual, una nueva de 6 o más caracteres y su confirmación.

**Criterios de aceptación**

**CA-V03-01 · Guardar mis datos**

- **Dado** que tengo sesión iniciada
- **Cuando** guardo datos válidos
- **Entonces** los cambios quedan guardados, `/auth/me` devuelve lo mismo y la web y la app muestran mi nuevo nombre

  _Se prueba con:_ TC-API-013, TC-WEB-006, TC-MOB-006

**CA-V03-02 · Datos inválidos**

- **Dado** que escribo una dirección de foto que no es una URL válida
- **Cuando** guardo
- **Entonces** la API responde 422 indicando el campo y mis datos anteriores se conservan

  _Se prueba con:_ TC-API-014

**CA-V03-03 · No puedo cambiar mi rol**

- **Dado** que envío el campo `rol` con otro valor
- **Cuando** llamo a `PATCH /usuarios/me`
- **Entonces** la API responde 422 y mi rol sigue siendo viajero

  _Se prueba con:_ TC-API-015

**CA-V03-04 · Cambiar la contraseña**

- **Dado** que escribo mi contraseña actual correcta y una nueva repetida igual
- **Cuando** guardo y vuelvo a iniciar sesión
- **Entonces** la nueva funciona (200) y la anterior ya no (401); mientras la confirmación no coincida, «Guardar» está deshabilitado

  _Se prueba con:_ TC-API-016, TC-MOB-007

**CA-V03-05 · Contraseña actual incorrecta**

- **Dado** que la contraseña actual que escribo no es la mía
- **Cuando** intento cambiarla
- **Entonces** veo «La contraseña actual no es correcta» y la contraseña no cambia

  _Se prueba con:_ TC-API-016

**CA-V03-06 · Error del servidor**

- **Dado** que la API falla al guardar
- **Cuando** termina la operación
- **Entonces** veo un error con opción de reintentar y no se anuncia un cambio que no se guardó

  _Se prueba con:_ TC-MAN-001

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-013 | API (Karate) | PATCH /usuarios/me actualiza y GET /auth/me devuelve lo mismo | CA-V03-01 | Pasó |
| TC-API-014 | API (Karate) | Avatar y teléfono inválidos devuelven 422 | CA-V03-02 | Pasó |
| TC-API-015 | API (Karate) | Enviar rol devuelve 422 | CA-V03-03 | Pasó |
| TC-API-016 | API (Karate) | Cambio de contraseña: login con nueva 200 y con anterior 401 | CA-V03-04, CA-V03-05 | Pasó |
| TC-WEB-006 | Web (Selenium) | Editar perfil y ver el nombre actualizado en el menú | CA-V03-01 | Pasó |
| TC-MOB-006 | Móvil (Appium) | Editar perfil en la app y verificarlo en la pestaña Perfil | CA-V03-01 | Pendiente (ejecutar en PC) |
| TC-MOB-007 | Móvil (Appium) | Cambio de contraseña con confirmación distinta deshabilita Guardar | CA-V03-04 | Pendiente (ejecutar en PC) |
| TC-MAN-001 | Manual (Manual) | Error de servidor al guardar el perfil: mensaje con reintentar y sin éxito falso | CA-V03-06 | Pendiente (manual) |

**API:** `GET /api/auth/me` · `PATCH /api/usuarios/me` · `PUT /api/usuarios/me/password`

---

### HU-V04 · Eliminar mi cuenta

> **Como** viajero, **quiero** eliminar mi cuenta cuando ya no la use **para** dejar de aparecer en la plataforma sin afectar las reservas ya hechas.

**En pocas palabras:** Puedo darme de baja después de confirmar. Si tengo reservas por usar, no se permite. Las reservas pasadas no se borran: el proveedor las sigue viendo.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV1 Mi cuenta | Baja | 2 | Web · Móvil · API | TRV-105 |

**Reglas de negocio**

1. Se pide confirmación antes de eliminar.
2. Es un borrado lógico: la cuenta se desactiva, el historial se conserva.
3. Se bloquea (409) si tengo reservas pendientes o confirmadas cuya estadía o salida no terminó. Para el proveedor, si tiene publicaciones con reservas activas.
4. Tras eliminar, el token se invalida y ese correo ya no puede iniciar sesión.

**Criterios de aceptación**

**CA-V04-01 · Me arrepiento**

- **Dado** que pulsé «Eliminar cuenta»
- **Cuando** cancelo en el diálogo de confirmación
- **Entonces** no cambia nada y sigo con mi sesión

  _Se prueba con:_ TC-WEB-007

**CA-V04-02 · Eliminación permitida**

- **Dado** que no tengo reservas activas
- **Cuando** confirmo la eliminación
- **Entonces** se cierra mi sesión y ya no puedo volver a ingresar con esa cuenta (401)

  _Se prueba con:_ TC-API-018, TC-MOB-008

**CA-V04-03 · Bloqueo por reservas activas**

- **Dado** que tengo una reserva confirmada para una fecha futura
- **Cuando** intento eliminar mi cuenta
- **Entonces** la API responde 409 «Tienes reservas activas» y la cuenta sigue activa

  _Se prueba con:_ TC-API-017

**CA-V04-04 · El historial se conserva**

- **Dado** que mi cuenta fue eliminada
- **Cuando** el proveedor consulta sus reservas
- **Entonces** mis reservas siguen ahí con su código, importe y estado

  _Se prueba con:_ TC-API-019

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-017 | API (Karate) | DELETE con reserva activa devuelve 409 | CA-V04-03 | Pasó |
| TC-API-018 | API (Karate) | DELETE sin reservas devuelve 200 y login posterior 401 | CA-V04-02 | Pasó |
| TC-API-019 | API (Karate) | Reservas de la cuenta eliminada siguen visibles para el proveedor | CA-V04-04 | Pasó |
| TC-WEB-007 | Web (Selenium) | Diálogo de confirmación: cancelar conserva la cuenta | CA-V04-01 | Pasó |
| TC-MOB-008 | Móvil (Appium) | Eliminar cuenta desde Perfil y volver a la pantalla de login | CA-V04-02 | Pendiente (ejecutar en PC) |

**API:** `DELETE /api/usuarios/me`

---

## Épica EV2 · Hoteles

_Buscar un hotel, cotizar la estadía y solicitar la reserva._

### HU-V05 · Buscar hoteles disponibles

> **Como** viajero, **quiero** buscar hoteles por ciudad, fechas y número de huéspedes **para** ver solo los que realmente tienen habitaciones libres para mi viaje.

**En pocas palabras:** Escribo la ciudad, las fechas y cuántos somos. Solo aparecen hoteles con habitaciones donde quepamos y que estén libres todas esas noches. No necesito iniciar sesión.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV2 Hoteles | Más alta | 5 | Web · Móvil · API | TRV-201 |

**Reglas de negocio**

1. La ciudad se compara sin importar mayúsculas ni tildes.
2. Entrada desde hoy en adelante, salida posterior a la entrada y como máximo 30 noches.
3. Adultos de 1 a 10, niños de 0 a 10 y habitaciones de 1 a 10, todos enteros.
4. Un hotel aparece si al menos un tipo de habitación tiene capacidad suficiente y unidades libres en todo el rango.
5. Las reservas pendientes y confirmadas ocupan unidades.
6. 12 resultados por página, ordenables por precio o estrellas.

**Criterios de aceptación**

**CA-V05-01 · Búsqueda válida**

- **Dado** que busco Cusco, 2 noches futuras, 2 adultos y 1 habitación
- **Cuando** pulso Buscar
- **Entonces** solo veo hoteles de Cusco con habitaciones para 2 o más personas libres en esas fechas, cada uno con su precio «desde» por noche

  _Se prueba con:_ TC-API-020, TC-WEB-008, TC-MOB-009

**CA-V05-02 · Las solicitudes pendientes ocupan**

- **Dado** que un hotel tiene todas sus unidades tomadas por solicitudes pendientes en esas fechas
- **Cuando** busco esas fechas
- **Entonces** ese hotel no aparece en los resultados

  _Se prueba con:_ TC-API-022

**CA-V05-03 · Fechas inválidas**

- **Dado** que la entrada es una fecha pasada o la salida es igual o anterior a la entrada
- **Cuando** intento buscar
- **Entonces** se me indica qué corregir, no se ejecuta la búsqueda y la API responde 422 si se la llama directo

  _Se prueba con:_ TC-API-021, TC-WEB-009

**CA-V05-04 · Cantidades fuera de rango**

- **Dado** que pido 0 adultos, 11 habitaciones o un número con decimales
- **Cuando** intento buscar
- **Entonces** se me informa el rango permitido y no se ejecuta la búsqueda (422 en la API)

  _Se prueba con:_ TC-API-021

**CA-V05-05 · Sin resultados no es lo mismo que un error**

- **Dado** que no hay hoteles disponibles o la API falla
- **Cuando** termina la búsqueda
- **Entonces** distingo «No encontramos hoteles» de «No pudimos cargar los resultados», que tiene el botón Reintentar

  _Se prueba con:_ TC-MOB-010

**CA-V05-06 · Conservo mi búsqueda**

- **Dado** que abrí un resultado
- **Cuando** vuelvo atrás
- **Entonces** siguen mis filtros y la página donde estaba

  _Se prueba con:_ TC-WEB-031

**CA-V05-07 · Catálogo con mucho tráfico**

- **Dado** que 100 usuarios navegan el catálogo durante 5 minutos (rampa de 60 s)
- **Cuando** se procesan las búsquedas
- **Entonces** el 95 % responde en menos de 1 segundo

  _Se prueba con:_ TC-PERF-002

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-020 | API (Karate) | Búsqueda válida: todos los resultados cumplen ciudad, capacidad y unidades (match each) | CA-V05-01 | Pasó |
| TC-API-021 | API (Karate) | Data-driven de fechas y cantidades inválidas devuelve 422 | CA-V05-03, CA-V05-04 | Pasó |
| TC-API-022 | API (Karate) | Hotel con unidades ocupadas por reservas pendientes no aparece | CA-V05-02 | Pasó |
| TC-WEB-008 | Web (Selenium) | Búsqueda en Cusco y validación de tarjetas de resultado | CA-V05-01 | Pasó |
| TC-WEB-009 | Web (Selenium) | Validación de fechas pasadas en el buscador | CA-V05-03 | Pasó |
| TC-WEB-031 | Web (Selenium) | Volver desde el detalle conserva la búsqueda (hoteles y tours) | CA-V05-06 | Pasó |
| TC-MOB-009 | Móvil (Appium) | Búsqueda por ciudad y fechas en la app | CA-V05-01 | Pendiente (ejecutar en PC) |
| TC-MOB-010 | Móvil (Appium) | Estado vacío vs error con Reintentar | CA-V05-05 | Pendiente (ejecutar en PC) |
| TC-PERF-002 | Rendimiento (JMeter) | Carga de catálogo: 100 usuarios, ramp-up 60 s, ciudades desde CSV, p95 < 1 s | CA-V05-07 | Pendiente (ejecutar en PC) |

**API:** `GET /api/hoteles?ciudad&check_in&check_out&adultos&ninos&habitaciones&page&sort`

---

### HU-V06 · Ver un hotel y cotizar mi estadía

> **Como** viajero, **quiero** ver el detalle de un hotel y cuánto me costará la estadía con impuestos **para** decidir antes de pagar.

**En pocas palabras:** Veo fotos, ubicación, opiniones y los tipos de habitación con su precio. Elijo cuántas habitaciones quiero y el sistema calcula subtotal, IGV (18 %) y total. Cotizar no reserva nada.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV2 Hoteles | Alta | 3 | Web · Móvil · API | TRV-202 |

**Reglas de negocio**

1. El cálculo lo hace el backend, para que web, app y lo guardado coincidan al céntimo.
2. Por tipo: tarifa por noche × noches × unidades. Subtotal = suma de tipos; IGV = 18 % del subtotal; total = subtotal + IGV; 2 decimales (redondeo half-up).
3. Por tipo se eligen de 1 a las unidades libres en esas fechas.
4. Cambiar fechas o cantidades recalcula; cotizar no aparta habitaciones.

**Criterios de aceptación**

**CA-V06-01 · Detalle completo**

- **Dado** que abro un hotel desde la búsqueda
- **Cuando** carga la página
- **Entonces** veo ubicación, estrellas, fotos, tipos de habitación con capacidad, tarifa y unidades libres para mis fechas, y las opiniones

  _Se prueba con:_ TC-API-023

**CA-V06-02 · Elegir varios tipos**

- **Dado** que elijo 2 habitaciones Doble y 1 Suite
- **Cuando** reviso el resumen
- **Entonces** el resumen lista cada tipo con sus unidades, fechas y noches

  _Se prueba con:_ TC-WEB-010, TC-MOB-011

**CA-V06-03 · Cálculo con IGV**

- **Dado** que una Doble cuesta S/ 150.00 por noche y elijo 2 por 2 noches
- **Cuando** veo el importe
- **Entonces** subtotal S/ 600.00, IGV S/ 108.00 y total S/ 708.00, igual en web, app y en la respuesta de `/cotizar`

  _Se prueba con:_ TC-API-024, TC-WEB-010, TC-MOB-011

**CA-V06-04 · No puedo pedir más de lo disponible**

- **Dado** que un tipo tiene 3 unidades libres
- **Cuando** intento elegir 4
- **Entonces** el selector no lo permite y `/cotizar` responde 422 indicando el máximo

  _Se prueba con:_ TC-API-025, TC-MOB-012

**CA-V06-05 · Cotizar no reserva**

- **Dado** que cambio la fecha de salida
- **Cuando** se actualiza la cotización
- **Entonces** el total cambia y no se crea ninguna reserva

  _Se prueba con:_ TC-API-079

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-023 | API (Karate) | Esquema JSON del detalle de hotel (habitaciones, calificación, reseñas) | CA-V06-01 | Pasó |
| TC-API-024 | API (Karate) | Cotización data-driven (CSV) con tarifas, noches y unidades → subtotal, igv, total exactos | CA-V06-03 | Pasó |
| TC-API-025 | API (Karate) | Cotizar más unidades que las libres devuelve 422 | CA-V06-04 | Pasó |
| TC-API-079 | API (Karate) | Cambiar la fecha de salida recalcula el total y no crea reserva | CA-V06-05 | Pasó |
| TC-WEB-010 | Web (Selenium) | Selección de 2 tipos y verificación del resumen con IGV | CA-V06-02, CA-V06-03 | Pasó |
| TC-MOB-011 | Móvil (Appium) | Selección de habitaciones y total con IGV en la app | CA-V06-02, CA-V06-03 | Pendiente (ejecutar en PC) |
| TC-MOB-012 | Móvil (Appium) | Botón + deshabilitado al llegar a las unidades disponibles | CA-V06-04 | Pendiente (ejecutar en PC) |

**API:** `GET /api/hoteles/{id}` · `GET /api/hoteles/{id}/disponibilidad` · `POST /api/reservas-habitaciones/cotizar`

---

### HU-V07 · Reservar un hotel con pago simulado

> **Como** viajero, **quiero** pagar y enviar mi solicitud de reserva de hotel **para** asegurar mis habitaciones mientras el hotel la confirma.

**En pocas palabras:** Pago con una tarjeta de prueba y mi solicitud queda PENDIENTE hasta que el proveedor la acepte o la rechace (HU-P13, HU-P14). Mientras tanto, esas habitaciones quedan apartadas para mí. Aunque muchos reserven a la vez o yo envíe dos veces, nunca se vende de más ni se duplica.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV2 Hoteles | Más alta | 8 | Web · Móvil · API | TRV-203 |

**Reglas de negocio**

1. Requiere sesión de viajero (un proveedor recibe 403).
2. Pago simulado: titular de 2 a 100 letras, número de 16 dígitos válido (Luhn), vencimiento MM/AA no vencido y CVC de 3 dígitos. Solo se guardan los 4 últimos dígitos.
3. Una solicitud puede tener varios tipos de habitación y se registra todo o nada.
4. Cada tipo genera una reserva con código único de 10 caracteres; se guardan precio, subtotal, IGV y total del momento.
5. Las unidades se verifican con bloqueo de fila: dos solicitudes simultáneas nunca superan lo disponible.
6. El header `Idempotency-Key` es obligatorio: repetir con la misma clave devuelve la misma solicitud sin duplicarla.

**Criterios de aceptación**

**CA-V07-01 · Solicitud registrada**

- **Dado** que tengo sesión, una selección válida y datos de pago válidos
- **Cuando** confirmo el pago
- **Entonces** se registra la solicitud con código, estado PENDIENTE y el mismo total que vi, y veo el comprobante

  _Se prueba con:_ TC-API-026, TC-WEB-011, TC-MOB-013

**CA-V07-02 · Pago inválido**

- **Dado** que el número no tiene 16 dígitos, la tarjeta está vencida o el CVC no tiene 3 dígitos
- **Cuando** intento pagar
- **Entonces** se señala el campo, la API responde 422 y no se crea la reserva; si cierro el formulario conservo mi selección

  _Se prueba con:_ TC-API-027, TC-WEB-012, TC-MOB-014

**CA-V07-03 · Mis habitaciones quedan apartadas**

- **Dado** que mi solicitud quedó pendiente
- **Cuando** otro viajero consulta las mismas fechas
- **Entonces** las unidades de mi solicitud ya no figuran como disponibles

  _Se prueba con:_ TC-API-028

**CA-V07-04 · Se acabaron mientras pagaba**

- **Dado** que otro viajero tomó las últimas unidades de uno de mis tipos mientras yo pagaba
- **Cuando** envío mi solicitud
- **Entonces** la API responde 409 indicando qué tipo ya no tiene unidades y no se registra ningún tipo

  _Se prueba con:_ TC-API-029

**CA-V07-05 · Nunca se vende de más**

- **Dado** que 20 viajeros envían a la vez una solicitud por la última habitación
- **Cuando** se procesan
- **Entonces** solo una solicitud se registra (201) y las otras 19 reciben 409

  _Se prueba con:_ TC-PERF-003

**CA-V07-06 · Sin duplicados al reintentar**

- **Dado** que no me llegó la respuesta y reintento con la misma `Idempotency-Key`
- **Cuando** reenvío
- **Entonces** recibo la misma solicitud ya registrada, sin crear otra

  _Se prueba con:_ TC-API-030

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-026 | API (Karate) | Reserva válida 201: estado pendiente, código único, subtotal/igv/total = cotización | CA-V07-01 | Pasó |
| TC-API-027 | API (Karate) | Data-driven de tarjetas inválidas (Luhn, vencida, CVC) → 422 sin reserva creada | CA-V07-02 | Pasó |
| TC-API-028 | API (Karate) | Disponibilidad tras reservar descuenta las unidades | CA-V07-03 | Pasó |
| TC-API-029 | API (Karate) | Varios tipos con uno sin stock → 409 y ninguno registrado | CA-V07-04 | Pasó |
| TC-API-030 | API (Karate) | Mismo Idempotency-Key devuelve la misma solicitud | CA-V07-06 | Pasó |
| TC-WEB-011 | Web (Selenium) | Flujo completo búsqueda → selección → pago → comprobante PENDIENTE | CA-V07-01 | Pasó |
| TC-WEB-012 | Web (Selenium) | Validaciones del formulario de pago | CA-V07-02 | Pasó |
| TC-MOB-013 | Móvil (Appium) | Flujo completo de reserva de hotel en la app | CA-V07-01 | Pendiente (ejecutar en PC) |
| TC-MOB-014 | Móvil (Appium) | Formulario de pago rechaza tarjeta vencida | CA-V07-02 | Pendiente (ejecutar en PC) |
| TC-PERF-003 | Rendimiento (JMeter) | 20 hilos simultáneos (Synchronizing Timer) por la última unidad: exactamente 1×201 y 19×409 | CA-V07-05 | Pasó (smoke Python) |

**API:** `POST /api/reservas-habitaciones (header Idempotency-Key)`

---

## Épica EV3 · Tours

_Buscar un tour, elegir salida y reservarlo._

### HU-V08 · Buscar tours

> **Como** viajero, **quiero** buscar tours por destino, categoría y fechas **para** encontrar actividades con cupo para mis días de viaje.

**En pocas palabras:** Filtro por destino, categoría (Aventura, Gastronomía, Cultura o Relajación) y fechas. Solo aparecen tours con alguna salida con cupo en ese rango. No necesito iniciar sesión.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV3 Tours | Alta | 3 | Web · Móvil · API | TRV-301 |

**Reglas de negocio**

1. Categorías permitidas: Aventura, Gastronomía, Cultura, Relajación.
2. El destino coincide con la ciudad o el país, sin importar mayúsculas ni tildes.
3. Fecha desde hoy en adelante y hasta igual o posterior a desde.
4. Un tour aparece si tiene al menos una salida programada en el rango con cupos libres suficientes.
5. 12 resultados por página.

**Criterios de aceptación**

**CA-V08-01 · Búsqueda válida**

- **Dado** que busco Cusco, categoría Aventura y un rango de fechas futuro
- **Cuando** pulso Buscar
- **Entonces** solo veo tours de Cusco, de Aventura y con salidas con cupo en ese rango

  _Se prueba con:_ TC-API-036, TC-WEB-015, TC-MOB-017

**CA-V08-02 · Filtros inválidos**

- **Dado** que el rango de fechas es pasado o la categoría no existe
- **Cuando** busco
- **Entonces** se informa el error (422 en la API) y no se muestran resultados

  _Se prueba con:_ TC-API-037

**CA-V08-03 · Sin resultados no es lo mismo que un error**

- **Dado** que no hay tours o la API falla
- **Cuando** termina la búsqueda
- **Entonces** distingo «No se encontraron tours» del error con el botón Reintentar

  _Se prueba con:_ TC-WEB-032

**CA-V08-04 · Conservo mi búsqueda**

- **Dado** que abrí un tour
- **Cuando** vuelvo atrás
- **Entonces** siguen mis filtros

  _Se prueba con:_ TC-WEB-031

**CA-V08-05 · Búsqueda con mucho tráfico**

- **Dado** que 100 usuarios buscan tours durante 5 minutos
- **Cuando** se procesan las búsquedas
- **Entonces** el 95 % responde en menos de 1 segundo

  _Se prueba con:_ TC-PERF-004

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-036 | API (Karate) | Filtros combinados: todos los resultados cumplen destino y categoría (match each) | CA-V08-01 | Pasó |
| TC-API-037 | API (Karate) | Data-driven de filtros inválidos → 422 | CA-V08-02 | Pasó |
| TC-WEB-015 | Web (Selenium) | Filtrar por categoría y destino | CA-V08-01 | Pasó |
| TC-WEB-031 | Web (Selenium) | Volver desde el detalle conserva la búsqueda (hoteles y tours) | CA-V08-04 | Pasó |
| TC-WEB-032 | Web (Selenium) | Sin resultados vs. error con Reintentar (tours y bandeja del proveedor) | CA-V08-03 | Pasó |
| TC-MOB-017 | Móvil (Appium) | Chips de categoría y búsqueda por destino | CA-V08-01 | Pendiente (ejecutar en PC) |
| TC-PERF-004 | Rendimiento (JMeter) | Carga de búsqueda de tours: 100 usuarios, categorías desde CSV, p95 < 1 s | CA-V08-05 | Pendiente (ejecutar en PC) |

**API:** `GET /api/tours?destino&categoria&checkIn&checkOut&cupos&page`

---

### HU-V09 · Ver un tour y cotizar

> **Como** viajero, **quiero** ver el detalle de un tour, elegir una salida y cuántas personas van **para** saber el precio final antes de reservar.

**En pocas palabras:** Veo qué incluye el tour, su programa de actividades, opiniones y las salidas futuras con cupos. Elijo salida y personas, y veo subtotal, IGV y total.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV3 Tours | Alta | 3 | Web · Móvil · API | TRV-302 |

**Reglas de negocio**

1. Solo se muestran salidas futuras en estado programada.
2. Personas: de 1 al cupo libre de la salida.
3. Subtotal = precio por persona × personas; IGV 18 %; total = subtotal + IGV; 2 decimales.
4. Salidas pasadas, cerradas, canceladas o sin cupo no se pueden elegir.

**Criterios de aceptación**

**CA-V09-01 · Detalle**

- **Dado** que abro un tour
- **Cuando** carga la página
- **Entonces** veo ubicación, categoría, duración, precio por persona, actividades en orden, opiniones y salidas futuras con sus cupos

  _Se prueba con:_ TC-API-038

**CA-V09-02 · Cálculo con IGV**

- **Dado** que el precio es S/ 120.00 por persona y elijo 3 personas
- **Cuando** veo el resumen
- **Entonces** subtotal S/ 360.00, IGV S/ 64.80 y total S/ 424.80

  _Se prueba con:_ TC-API-039, TC-WEB-016, TC-MOB-018

**CA-V09-03 · Selección no válida**

- **Dado** que elijo 0 personas, más personas que el cupo o una salida agotada
- **Cuando** intento continuar
- **Entonces** no se permite y se me indica el motivo (422 en `/cotizar`)

  _Se prueba con:_ TC-API-040, TC-MOB-018

**CA-V09-04 · Tour sin salidas**

- **Dado** que el tour no tiene salidas futuras
- **Cuando** lo consulto
- **Entonces** veo «No hay salidas programadas» y no puedo reservar

  _Se prueba con:_ TC-API-080

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-038 | API (Karate) | Esquema del detalle; ninguna salida devuelta es pasada ni no programada | CA-V09-01 | Pasó |
| TC-API-039 | API (Karate) | Cotización de tour data-driven (precio × personas + 18%) | CA-V09-02 | Pasó |
| TC-API-040 | API (Karate) | Cotizar más personas que el cupo → 422 | CA-V09-03 | Pasó |
| TC-API-080 | API (Karate) | Tour sin salidas disponibles: detalle sin salidas y cotizar 409 | CA-V09-04 | Pasó |
| TC-WEB-016 | Web (Selenium) | Elegir salida y personas, verificar total con IGV | CA-V09-02 | Pasó |
| TC-MOB-018 | Móvil (Appium) | Elegir salida y personas en la app; + se deshabilita al tope de cupos | CA-V09-02, CA-V09-03 | Pendiente (ejecutar en PC) |

**API:** `GET /api/tours/{id}` · `POST /api/tours/salidas/{salida}/cotizar`

---

### HU-V10 · Reservar un tour

> **Como** viajero, **quiero** pagar y reservar un tour **para** tener mi lugar asegurado al instante.

**En pocas palabras:** A diferencia del hotel, el tour no espera aprobación: al pagar queda CONFIRMADO y se descuentan los cupos. Aunque muchos reserven a la vez, nunca se venden más cupos de los que hay.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV3 Tours | Más alta | 5 | Web · Móvil · API | TRV-303 |

**Reglas de negocio**

1. Requiere sesión de viajero; mismas reglas de pago simulado que HU-V07.
2. El servidor valida que la salida exista, esté programada, sea futura y tenga cupo suficiente.
3. Bloqueo de fila sobre la salida: los cupos nunca quedan negativos ni se sobrevenden.
4. Se guardan precio unitario, subtotal, IGV y total; el estado es CONFIRMADA.
5. `Idempotency-Key` obligatorio, igual que en hoteles.

**Criterios de aceptación**

**CA-V10-01 · Reserva confirmada**

- **Dado** que tengo sesión y elijo una salida con cupo
- **Cuando** pago
- **Entonces** se registra con código único, estado CONFIRMADA y el total que vi, y el cupo libre baja exactamente en mis personas

  _Se prueba con:_ TC-API-041, TC-WEB-017, TC-MOB-019

**CA-V10-02 · Pago inválido o desisto**

- **Dado** que el pago es inválido o cierro el formulario
- **Cuando** no completo el envío
- **Entonces** no se crea la reserva y los cupos no cambian

  _Se prueba con:_ TC-API-081

**CA-V10-03 · Salida no disponible**

- **Dado** que la salida está cerrada, cancelada, ya pasó o no tiene cupo suficiente
- **Cuando** envío la reserva
- **Entonces** la API responde 409 o 422 con el motivo y los cupos no cambian

  _Se prueba con:_ TC-API-042

**CA-V10-04 · Nunca se venden cupos de más**

- **Dado** que 30 viajeros reservan a la vez una salida con 10 cupos
- **Cuando** se procesan
- **Entonces** se confirman exactamente 10 cupos en total y el resto recibe 409

  _Se prueba con:_ TC-PERF-005

**CA-V10-05 · Sin duplicados al reintentar**

- **Dado** que reintento con la misma `Idempotency-Key`
- **Cuando** reenvío
- **Entonces** obtengo la misma reserva y los cupos no se descuentan otra vez

  _Se prueba con:_ TC-API-043

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-041 | API (Karate) | Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | CA-V10-01 | Pasó |
| TC-API-042 | API (Karate) | Salida cerrada / pasada / sin cupo → 409 o 422 y cupos sin cambio | CA-V10-03 | Pasó |
| TC-API-043 | API (Karate) | Idempotencia: dos envíos, una sola reserva y un solo descuento | CA-V10-05 | Pasó |
| TC-API-081 | API (Karate) | Pago de tour inválido (CSV) no crea reserva ni descuenta cupos | CA-V10-02 | Pasó |
| TC-WEB-017 | Web (Selenium) | Reserva de tour end-to-end con comprobante CONFIRMADA | CA-V10-01 | Pasó |
| TC-MOB-019 | Móvil (Appium) | Reserva de tour end-to-end en la app | CA-V10-01 | Pendiente (ejecutar en PC) |
| TC-PERF-005 | Rendimiento (JMeter) | 30 hilos simultáneos sobre salida de 10 cupos: suma de personas confirmadas <= 10 | CA-V10-04 | Pasó (smoke Python) |

**API:** `POST /api/tours/salidas/{salida}/reservas (header Idempotency-Key)`

---

## Épica EV4 · Mis reservas y mi viaje

_Seguir mis reservas, cancelarlas, opinar y planificar el viaje._

### HU-V11 · Ver mis reservas

> **Como** viajero, **quiero** ver todas mis reservas de hoteles y tours en un solo lugar **para** tener a mano códigos, fechas, importes y estados.

**En pocas palabras:** En «Mis reservas» veo solo las mías, con todos sus datos, y lo mismo en la web y en la app. Puedo filtrar por estado y recorrer todas las páginas.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV4 Mis reservas y mi viaje | Alta | 3 | Web · Móvil · API | TRV-401 |

**Reglas de negocio**

1. La fuente de verdad es la API: ningún canal guarda reservas localmente.
2. Lista paginada (10 por página), de la más reciente a la más antigua, con filtro por estado.
3. Cada reserva muestra código, servicio, habitación o salida, fechas, cantidades, subtotal, IGV, total y estado.
4. Una reserva ajena responde 404 (no se revela que existe).

**Criterios de aceptación**

**CA-V11-01 · Solo las mías, con todos sus datos**

- **Dado** que tengo reservas
- **Cuando** abro «Mis reservas»
- **Entonces** veo solo las mías, con todos sus datos y el estado guardado

  _Se prueba con:_ TC-API-044, TC-WEB-018

**CA-V11-02 · Lo mismo en la web y en la app**

- **Dado** que reservé en la web
- **Cuando** abro la app
- **Entonces** veo los mismos códigos, estados e importes y puedo recorrer todas las páginas

  _Se prueba con:_ TC-API-044, TC-WEB-018, TC-MOB-020, TC-MOB-021

**CA-V11-03 · Vacío, error y reservas ajenas**

- **Dado** que no tengo reservas, la API falla o pido una reserva de otra persona
- **Cuando** entro
- **Entonces** distingo «Aún no tienes reservas» de un error, y la reserva ajena responde 404

  _Se prueba con:_ TC-API-046

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-044 | API (Karate) | Esquema paginado (data, meta.total, meta.last_page) y solo reservas propias | CA-V11-01, CA-V11-02 | Pasó |
| TC-API-046 | API (Karate) | Reserva ajena → 404 | CA-V11-03 | Pasó |
| TC-WEB-018 | Web (Selenium) | Mis reservas: pestañas hotel/tour, filtro por estado y paginación | CA-V11-01, CA-V11-02 | Pasó |
| TC-MOB-020 | Móvil (Appium) | Mis reservas en la app coincide con la web (mismo código y total) | CA-V11-02 | Pendiente (ejecutar en PC) |
| TC-MOB-021 | Móvil (Appium) | Scroll infinito / paginación de reservas | CA-V11-02 | Pendiente (ejecutar en PC) |

**API:** `GET /api/mis-reservas?estado&page` · `GET /api/mis-reservas/{id}` · `GET /api/tours/mis-reservas?page`

---

### HU-V12 · Saber si el hotel aceptó o rechazó mi solicitud

> **Como** viajero, **quiero** ver la respuesta del proveedor a cada solicitud de hotel **para** saber si mi viaje está confirmado o si debo buscar otra opción.

**En pocas palabras:** Mis solicitudes de hotel pasan de PENDIENTE a CONFIRMADA o RECHAZADA (con el motivo). Los tours aparecen confirmados desde el inicio. Si el proveedor cancela una salida, me avisa sin marcar mi reserva como cancelada por mí.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV4 Mis reservas y mi viaje | Alta | 3 | Web · Móvil · API | TRV-401, TRV-204 |

**Reglas de negocio**

1. Estados de hotel: Pendiente, Confirmada, Rechazada (con motivo) y Cancelada. Tour: Confirmada y Cancelada.
2. «Rechazada» (decisión del proveedor) es distinto de «Cancelada» (decisión mía).
3. Si el proveedor cierra o cancela la salida de un tour, se muestra un aviso aparte sin cambiar el estado de mi reserva.

**Criterios de aceptación**

**CA-V12-01 · Pendiente o confirmada**

- **Dado** que el proveedor aún no decide o ya aceptó mi solicitud
- **Cuando** actualizo «Mis reservas»
- **Entonces** la solicitud figura PENDIENTE o CONFIRMADA, con los mismos datos e importe

  _Se prueba con:_ TC-API-045, TC-API-031, TC-WEB-013

**CA-V12-02 · Rechazada con motivo**

- **Dado** que el proveedor rechazó mi solicitud
- **Cuando** actualizo «Mis reservas»
- **Entonces** figura RECHAZADA con el motivo, distinta de una cancelación mía, y puedo iniciar otra búsqueda

  _Se prueba con:_ TC-API-045, TC-WEB-019, TC-WEB-014

**CA-V12-03 · Los tours no esperan aprobación**

- **Dado** que reservé un tour
- **Cuando** lo consulto
- **Entonces** aparece CONFIRMADO desde el primer momento

  _Se prueba con:_ TC-API-041

**CA-V12-04 · Salida cancelada por el proveedor**

- **Dado** que el proveedor canceló la salida de mi tour
- **Cuando** consulto mi reserva
- **Entonces** veo el aviso «Salida cancelada por el proveedor» sin que la reserva aparezca como cancelada por mí

  _Se prueba con:_ TC-API-082

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-031 | API (Karate) | Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | CA-V12-01 | Pasó |
| TC-API-041 | API (Karate) | Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | CA-V12-03 | Pasó |
| TC-API-045 | API (Karate) | Estado tras aceptar y tras rechazar (con motivo) | CA-V12-01, CA-V12-02 | Pasó |
| TC-API-082 | API (Karate) | Salida cancelada por el proveedor: reserva confirmada con aviso | CA-V12-04 | Pasó |
| TC-WEB-013 | Web (Selenium) | Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | CA-V12-01 | Pasó |
| TC-WEB-014 | Web (Selenium) | Proveedor rechaza con motivo y el viajero ve RECHAZADA | CA-V12-02 | Pasó |
| TC-WEB-019 | Web (Selenium) | Reserva rechazada muestra motivo | CA-V12-02 | Pasó |

**API:** `GET /api/mis-reservas?estado&page` · `GET /api/tours/mis-reservas?page`

---

### HU-V13 · Cancelar una reserva

> **Como** viajero, **quiero** cancelar una reserva que ya no voy a usar **para** liberar el lugar y dejar claro que no iré.

**En pocas palabras:** Cancelo después de confirmar en un diálogo. Al cancelar se liberan las habitaciones o los cupos una sola vez, y la reserva queda en mi historial como CANCELADA.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV4 Mis reservas y mi viaje | Alta | 3 | Web · Móvil · API | TRV-402 |

**Reglas de negocio**

1. Se pueden cancelar: hotel pendiente o confirmada; tour confirmada; siempre que no haya empezado.
2. Se pide confirmación antes de cancelar.
3. Hotel: libera sus unidades. Tour: devuelve exactamente sus personas al cupo (bloqueo de fila).
4. Una reserva rechazada o ya cancelada responde 409 y no se libera nada otra vez.
5. Se conservan código, fechas, cantidades e importes. Sin reembolsos reales (fuera de alcance).

**Criterios de aceptación**

**CA-V13-01 · Me arrepiento**

- **Dado** que pulsé Cancelar
- **Cuando** elijo «No» en el diálogo
- **Entonces** la reserva no cambia

  _Se prueba con:_ TC-WEB-020

**CA-V13-02 · Cancelación exitosa**

- **Dado** que confirmo la cancelación de mi reserva de hotel
- **Cuando** termina
- **Entonces** la reserva figura CANCELADA en web y app con su historial intacto y sus unidades vuelven a estar libres

  _Se prueba con:_ TC-API-047, TC-WEB-020, TC-MOB-022

**CA-V13-03 · El tour devuelve los cupos exactos**

- **Dado** que mi reserva de tour era para 3 personas
- **Cuando** la cancelo
- **Entonces** el cupo libre de la salida sube exactamente 3

  _Se prueba con:_ TC-API-048

**CA-V13-04 · Reserva no cancelable**

- **Dado** que la reserva es ajena, ya está cancelada o fue rechazada
- **Cuando** intento cancelarla
- **Entonces** la API responde 404 o 409 y no se libera nada otra vez

  _Se prueba con:_ TC-API-049

**CA-V13-05 · Se cortó la conexión**

- **Dado** que se cortó la conexión mientras cancelaba
- **Cuando** actualizo la pantalla
- **Entonces** veo el estado real guardado y no un mensaje de éxito falso

  _Se prueba con:_ TC-MAN-002, TC-API-049

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-047 | API (Karate) | Cancelar hotel: estado cancelada y unidades liberadas | CA-V13-02 | Pasó |
| TC-API-048 | API (Karate) | Cancelar tour devuelve exactamente las personas al cupo | CA-V13-03 | Pasó |
| TC-API-049 | API (Karate) | Cancelar dos veces → 409; cancelar rechazada → 409; ajena → 404 | CA-V13-04, CA-V13-05 | Pasó |
| TC-WEB-020 | Web (Selenium) | Cancelar desde Mis reservas con diálogo de confirmación | CA-V13-01, CA-V13-02 | Pasó |
| TC-MOB-022 | Móvil (Appium) | Cancelar desde la app y ver estado CANCELADA | CA-V13-02 | Pendiente (ejecutar en PC) |
| TC-MAN-002 | Manual (Manual) | Corte de conexión al cancelar: al refrescar se ve el estado real | CA-V13-05 | Pendiente (manual) |

**API:** `POST /api/reservas-habitaciones/{id}/cancelar` · `POST /api/tours/reservas/{id}/cancelar`

---

### HU-V14 · Leer y escribir reseñas

> **Como** viajero, **quiero** leer opiniones y publicar la mía después de usar un servicio **para** elegir mejor y ayudar a otros viajeros.

**En pocas palabras:** Cualquiera puede leer las reseñas. Solo puedo opinar si de verdad usé el servicio (reserva confirmada y terminada), una vez por servicio, y puedo editar o borrar solo la mía.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV4 Mis reservas y mi viaje | Media | 5 | Web · Móvil · API | TRV-403 |

**Reglas de negocio**

1. Lectura pública, 10 por página; promedio y cantidad se calculan sobre todas las reseñas, no sobre la página.
2. Para publicar necesito una reserva propia CONFIRMADA cuya estadía terminó (hotel) o cuya salida ya pasó (tour).
3. Calificación entera de 1 a 5 y comentario de 10 a 300 caracteres.
4. Una reseña por usuario y servicio (la segunda responde 409).
5. Solo el autor edita o elimina; eliminar pide confirmación.

**Criterios de aceptación**

**CA-V14-01 · Leer opiniones**

- **Dado** que abro un hotel o tour
- **Cuando** veo sus opiniones
- **Entonces** son solo de ese servicio, puedo ver todas las páginas y el promedio corresponde a todas las reseñas

  _Se prueba con:_ TC-API-050

**CA-V14-02 · Publicar mi reseña**

- **Dado** que tengo una reserva confirmada y terminada
- **Cuando** envío 4 estrellas y un comentario de 50 caracteres
- **Entonces** se publica a mi nombre y el promedio se recalcula

  _Se prueba con:_ TC-API-051, TC-WEB-021, TC-MOB-023

**CA-V14-03 · Reseña no permitida**

- **Dado** que no tengo una reserva terminada o el comentario tiene solo 5 caracteres
- **Cuando** intento publicar
- **Entonces** se me informa la causa exacta (403 «Debes haber completado una reserva» o 422) y no se publica

  _Se prueba con:_ TC-API-051, TC-API-052

**CA-V14-04 · Editar o borrar solo la mía**

- **Dado** que es mi reseña
- **Cuando** la edito, o la elimino tras confirmar
- **Entonces** se actualiza esa misma reseña o desaparece; con una reseña ajena la API responde 403

  _Se prueba con:_ TC-API-053, TC-WEB-021

**CA-V14-05 · Una reseña por servicio**

- **Dado** que ya publiqué una reseña de ese servicio
- **Cuando** intento publicar otra
- **Entonces** la API responde 409 y sigue existiendo una sola reseña mía

  _Se prueba con:_ TC-API-053

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-050 | API (Karate) | Resumen: promedio y total coinciden con el conjunto completo | CA-V14-01 | Pasó |
| TC-API-051 | API (Karate) | Publicar con reserva finalizada 201; sin reserva 403 con mensaje | CA-V14-02, CA-V14-03 | Pasó |
| TC-API-052 | API (Karate) | Data-driven de longitudes de comentario y calificaciones fuera de rango → 422 | CA-V14-03 | Pasó |
| TC-API-053 | API (Karate) | Editar/eliminar reseña ajena → 403; segunda reseña → 409 | CA-V14-04, CA-V14-05 | Pasó |
| TC-WEB-021 | Web (Selenium) | Publicar, editar y eliminar reseña en el detalle | CA-V14-02, CA-V14-04 | Pasó |
| TC-MOB-023 | Móvil (Appium) | Publicar reseña desde la app | CA-V14-02 | Pendiente (ejecutar en PC) |

**API:** `GET /api/reviews?servicio_id&page` · `GET /api/reviews/resumen?servicio_id` · `GET /api/reviews/puedo-opinar` · `POST /api/reviews` · `PUT /api/reviews/{id}` · `DELETE /api/reviews/{id}`

---

### HU-V15 · Recibir un itinerario sugerido

> **Como** viajero, **quiero** que me sugieran un plan de actividades para los días de mi estadía **para** aprovechar mejor el viaje.

**En pocas palabras:** Desde el detalle del hotel elijo mi interés y recibo una propuesta día por día con actividades, duración y costo estimado. Es solo una sugerencia: no reserva nada y puedo seguir con mi reserva de hotel aunque el servicio falle.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EV4 Mis reservas y mi viaje | Baja | 5 | Web · Móvil · API | TRV-404 |

**Reglas de negocio**

1. Datos: hotel, destino, entrada, salida (máximo 14 días) e interés (Aventura, Gastronomía, Cultura o Relajación).
2. El backend Laravel firma un JWT de servicio y llama al microservicio de itinerarios (FastAPI, puerto 8001).
3. La propuesta se rotula «Sugerencia» y su costo se muestra aparte del total del hotel. Los días libres no tienen fecha sugerida.
4. Si el microservicio no responde en 10 s, la API responde 503 y se puede continuar solo con el hotel.

**Criterios de aceptación**

**CA-V15-01 · Propuesta generada**

- **Dado** que ingreso datos válidos
- **Cuando** genero el itinerario
- **Entonces** veo los días con sus actividades, duraciones y costos estimados

  _Se prueba con:_ TC-API-054, TC-WEB-022, TC-MOB-024

**CA-V15-02 · Es una sugerencia, no una reserva**

- **Dado** que reviso la propuesta
- **Cuando** la miro
- **Entonces** está rotulada como sugerencia y su costo aparece separado del total del hotel

  _Se prueba con:_ TC-WEB-022

**CA-V15-03 · Sigo con mi reserva**

- **Dado** que vi o ignoré la propuesta
- **Cuando** continúo
- **Entonces** puedo terminar la reserva del hotel; para reservar un tour uso su propio flujo (HU-V10)

  _Se prueba con:_ TC-MOB-024

**CA-V15-04 · Datos inválidos**

- **Dado** que el interés no existe o las fechas no son válidas
- **Cuando** genero el itinerario
- **Entonces** se me informa el error (422) y no se muestra ninguna propuesta

  _Se prueba con:_ TC-API-055

**CA-V15-05 · Servicio de itinerarios caído**

- **Dado** que el microservicio no responde
- **Cuando** genero el itinerario
- **Entonces** veo «No pudimos generar el itinerario» con Reintentar y puedo seguir con el hotel

  _Se prueba con:_ TC-MAN-003

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-054 | API (Karate) | Itinerario válido: esquema de días y actividades | CA-V15-01 | Pasó |
| TC-API-055 | API (Karate) | Interés inválido → 422 | CA-V15-04 | Pasó |
| TC-WEB-022 | Web (Selenium) | Generar itinerario desde el detalle del hotel | CA-V15-01, CA-V15-02 | Pasó |
| TC-MOB-024 | Móvil (Appium) | Generar itinerario en la app y continuar con la reserva | CA-V15-01, CA-V15-03 | Pendiente (ejecutar en PC) |
| TC-MAN-003 | Manual (Manual) | Microservicio de itinerarios caído: aviso con Reintentar y se puede seguir con el hotel | CA-V15-05 | Pendiente (manual) |

**API:** `POST /api/itineraries` · `Microservicio: GET :8001/api/health`

---
