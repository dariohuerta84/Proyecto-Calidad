# Historias de usuario del proveedor

[← Índice de historias](README.md) · [Historias del viajero](viajero.md)

**15 historias** en 4 épicas · **54 criterios de aceptación** · **67 casos de prueba** · **52 story points**

El proveedor es una empresa que publica hoteles y tours, abre salidas con cupos, decide sobre las solicitudes de hotel (aceptar o rechazar) y sigue sus indicadores. Todas sus pantallas y rutas de la API son exclusivas de su rol.

## Resumen

| Épica | Id | Historia | Prioridad | Pts | Criterios | Casos |
|---|---|---|---|---|---|---|
| EP1 Mi cuenta y mi panel | [HU-P01](#hu-p01--crear-mi-cuenta-de-proveedor) | Crear mi cuenta de proveedor | Alta | 3 | 4 | 8 |
| EP1 Mi cuenta y mi panel | [HU-P02](#hu-p02--entrar-a-mi-panel-y-mantener-los-datos-de-mi-empresa) | Entrar a mi panel y mantener los datos de mi empresa | Alta | 3 | 3 | 4 |
| EP1 Mi cuenta y mi panel | [HU-P03](#hu-p03--ver-mis-publicaciones) | Ver mis publicaciones | Alta | 3 | 3 | 5 |
| EP1 Mi cuenta y mi panel | [HU-P04](#hu-p04--ver-los-indicadores-de-mi-negocio) | Ver los indicadores de mi negocio | Media | 3 | 3 | 4 |
| EP2 Mis hoteles | [HU-P05](#hu-p05--publicar-un-hotel) | Publicar un hotel | Alta | 5 | 3 | 5 |
| EP2 Mis hoteles | [HU-P06](#hu-p06--editar-un-hotel) | Editar un hotel | Media | 3 | 2 | 2 |
| EP2 Mis hoteles | [HU-P07](#hu-p07--gestionar-los-tipos-de-habitación) | Gestionar los tipos de habitación | Media | 5 | 5 | 6 |
| EP3 Mis tours | [HU-P08](#hu-p08--publicar-un-tour) | Publicar un tour | Alta | 3 | 2 | 4 |
| EP3 Mis tours | [HU-P09](#hu-p09--editar-un-tour) | Editar un tour | Media | 2 | 2 | 2 |
| EP3 Mis tours | [HU-P10](#hu-p10--armar-el-programa-de-actividades-del-tour) | Armar el programa de actividades del tour | Baja | 3 | 4 | 4 |
| EP3 Mis tours | [HU-P11](#hu-p11--programar-salidas-y-cupos) | Programar salidas y cupos | Alta | 5 | 6 | 5 |
| EP4 Reservas recibidas | [HU-P12](#hu-p12--ver-las-reservas-que-recibo) | Ver las reservas que recibo | Alta | 3 | 5 | 6 |
| EP4 Reservas recibidas | [HU-P13](#hu-p13--aceptar-una-solicitud-de-hotel) | Aceptar una solicitud de hotel | Más alta | 5 | 4 | 5 |
| EP4 Reservas recibidas | [HU-P14](#hu-p14--rechazar-una-solicitud-de-hotel) | Rechazar una solicitud de hotel | Más alta | 3 | 3 | 4 |
| EP4 Reservas recibidas | [HU-P15](#hu-p15--eliminar-una-publicación) | Eliminar una publicación | Media | 3 | 5 | 6 |

## Épica EP1 · Mi cuenta y mi panel

_Crear la cuenta de empresa, entrar al panel y ver cómo va el negocio._

### HU-P01 · Crear mi cuenta de proveedor

> **Como** proveedor, **quiero** registrar mi empresa con su RUC y celular **para** publicar mis hoteles y tours en la plataforma.

**En pocas palabras:** Me registro como empresa peruana: nombre, RUC de 11 dígitos, celular, correo y contraseña. Al terminar entro a mi panel de proveedor.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP1 Mi cuenta y mi panel | Alta | 3 | Web · Móvil · API | TRV-102 |

**Reglas de negocio**

1. Obligatorios: nombre de la empresa (máximo 150), RUC, celular, correo, contraseña y confirmación.
2. RUC: exactamente 11 dígitos y único en el sistema.
3. Celular: se aceptan `987654321`, `+51987654321` o `+51 987 654 321`; se guarda como `+51 9XXXXXXXX`.
4. Al proveedor no se le piden nombre ni apellido personales.
5. Mismas reglas de correo y contraseña que HU-V01.

**Criterios de aceptación**

**CA-P01-01 · Registro exitoso**

- **Dado** que ingreso empresa, un RUC nuevo de 11 dígitos, un celular peruano, un correo nuevo y la contraseña confirmada
- **Cuando** me registro como proveedor
- **Entonces** se crea una sola cuenta de proveedor, el celular queda guardado como `+51 9XXXXXXXX` y entro a mi panel

  _Se prueba con:_ TC-API-005, TC-WEB-003, TC-MOB-003

**CA-P01-02 · RUC inválido o repetido**

- **Dado** que el RUC no tiene 11 dígitos o ya está registrado
- **Cuando** envío el registro
- **Entonces** la API responde 422 indicando el problema del RUC y no se crea la cuenta

  _Se prueba con:_ TC-API-006

**CA-P01-03 · Celular inválido**

- **Dado** que el teléfono no es un celular peruano (9 dígitos que empiezan con 9)
- **Cuando** envío el registro
- **Entonces** veo «El teléfono debe tener el formato +51 9########» y no se crea la cuenta

  _Se prueba con:_ TC-API-007

**CA-P01-04 · Correo repetido o contraseñas distintas**

- **Dado** que el correo ya existe o la confirmación no coincide
- **Cuando** envío el registro
- **Entonces** se me informa el error y no se crea ninguna cuenta

  _Se prueba con:_ TC-API-003, TC-WEB-002, TC-MOB-002

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-003 | API (Karate) | Correo duplicado en mayúsculas devuelve 422 | CA-P01-04 | Pasó |
| TC-API-005 | API (Karate) | Registro proveedor normaliza '987654321' a '+51 987654321' | CA-P01-01 | Pasó |
| TC-API-006 | API (Karate) | Data-driven de RUC inválidos y duplicado (422) | CA-P01-02 | Pasó |
| TC-API-007 | API (Karate) | Teléfonos inválidos devuelven 422 con mensaje | CA-P01-03 | Pasó |
| TC-WEB-002 | Web (Selenium) | Mensajes de validación en el formulario | CA-P01-04 | Pasó |
| TC-WEB-003 | Web (Selenium) | Registro de proveedor y acceso al panel del proveedor | CA-P01-01 | Pasó |
| TC-MOB-002 | Móvil (Appium) | Botón Registrarme deshabilitado con contraseñas distintas | CA-P01-04 | Pendiente (ejecutar en PC) |
| TC-MOB-003 | Móvil (Appium) | Registro de proveedor desde la app y acceso al panel | CA-P01-01 | Pendiente (ejecutar en PC) |

**API:** `GET /api/auth/csrf` · `POST /api/auth/register`

---

### HU-P02 · Entrar a mi panel y mantener los datos de mi empresa

> **Como** proveedor, **quiero** iniciar sesión, llegar directo a mi panel y mantener los datos de mi empresa **para** gestionar mi negocio sin que otros usuarios accedan a él.

**En pocas palabras:** Al iniciar sesión llego a mi panel de proveedor. Un viajero nunca puede entrar a las pantallas ni a la API del proveedor. Desde mi perfil actualizo el nombre de la empresa y el celular.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP1 Mi cuenta y mi panel | Alta | 3 | Web · Móvil · API | TRV-103, TRV-104 |

**Reglas de negocio**

1. El inicio de sesión, la sesión vencida y el cierre de sesión funcionan igual que en HU-V02.
2. Las rutas `/api/proveedor/*` y las de crear o editar publicaciones son solo para el rol proveedor (otro rol: 403).
3. El proveedor edita nombre de la empresa, celular (mismo formato que el registro) y foto. El rol no se puede cambiar (HU-V03).

**Criterios de aceptación**

**CA-P02-01 · Llego a mi panel**

- **Dado** que tengo una cuenta de proveedor activa
- **Cuando** inicio sesión con datos correctos
- **Entonces** veo el menú de proveedor y mi panel de publicaciones

  _Se prueba con:_ TC-API-008, TC-WEB-004

**CA-P02-02 · El panel es solo para proveedores**

- **Dado** que soy viajero
- **Cuando** intento entrar a una pantalla o ruta de proveedor
- **Entonces** la API responde 403, la web no muestra el panel y no se expone ningún dato

  _Se prueba con:_ TC-API-012, TC-WEB-004

**CA-P02-03 · Celular de la empresa inválido**

- **Dado** que cambio el celular por uno que no es peruano
- **Cuando** guardo mi perfil
- **Entonces** la API responde 422 indicando el campo y se conservan los datos anteriores

  _Se prueba con:_ TC-API-014

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-008 | API (Karate) | Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | CA-P02-01 | Pasó |
| TC-API-012 | API (Karate) | Viajero en ruta de proveedor devuelve 403 | CA-P02-02 | Pasó |
| TC-API-014 | API (Karate) | Avatar y teléfono inválidos devuelven 422 | CA-P02-03 | Pasó |
| TC-WEB-004 | Web (Selenium) | Login viajero y proveedor muestran el menú de su rol | CA-P02-01, CA-P02-02 | Pasó |

**API:** `POST /api/auth/login` · `GET /api/auth/me` · `PATCH /api/usuarios/me`

---

### HU-P03 · Ver mis publicaciones

> **Como** proveedor, **quiero** ver en mi panel la lista de todos mis hoteles y tours **para** tener a mano lo que tengo publicado.

**En pocas palabras:** Mi panel lista solo mis publicaciones, de 15 en 15, y puedo filtrar por tipo o buscar por nombre. Si aún no publiqué nada, el panel me lo dice.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP1 Mi cuenta y mi panel | Alta | 3 | Web · Móvil · API | TRV-501 |

**Reglas de negocio**

1. Lista paginada (15 por página) solo de servicios propios, filtrable por tipo (hotel o tour) y por texto.
2. Es la pantalla de inicio del proveedor.

**Criterios de aceptación**

**CA-P03-01 · Solo las mías, por páginas**

- **Dado** que tengo 20 publicaciones
- **Cuando** abro mi panel
- **Entonces** veo solo las mías y en la página 2 están los registros 16 a 20

  _Se prueba con:_ TC-API-056, TC-WEB-023, TC-MOB-025

**CA-P03-02 · Panel vacío o acceso no autorizado**

- **Dado** que todavía no tengo publicaciones, o quien entra es un viajero
- **Cuando** se abre el panel
- **Entonces** veo «Aún no tienes publicaciones», o la API responde 403 al viajero

  _Se prueba con:_ TC-API-058

**CA-P03-03 · Panel con muchos proveedores a la vez**

- **Dado** que 30 proveedores consultan su panel al mismo tiempo
- **Cuando** se procesan las consultas
- **Entonces** el 95 % responde en menos de 1,5 segundos

  _Se prueba con:_ TC-PERF-006

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-056 | API (Karate) | Paginación: meta.total, last_page y página 2 con los registros 16+ | CA-P03-01 | Pasó |
| TC-API-058 | API (Karate) | Viajero en /proveedor/servicios → 403 | CA-P03-02 | Pasó |
| TC-WEB-023 | Web (Selenium) | Panel: paginación y tarjetas de indicadores | CA-P03-01 | Pasó |
| TC-MOB-025 | Móvil (Appium) | Panel del proveedor en la app con indicadores | CA-P03-01 | Pendiente (ejecutar en PC) |
| TC-PERF-006 | Rendimiento (JMeter) | Panel del proveedor con 30 usuarios concurrentes, p95 < 1,5 s | CA-P03-03 | Pendiente (ejecutar en PC) |

**API:** `GET /api/proveedor/servicios?tipo&search&page`

---

### HU-P04 · Ver los indicadores de mi negocio

> **Como** proveedor, **quiero** ver cuántas reservas tengo en cada estado, mis ingresos y mis reseñas **para** saber cómo va cada publicación.

**En pocas palabras:** Cada publicación muestra sus reservas pendientes, confirmadas, rechazadas y canceladas por separado, los ingresos confirmados y su promedio de reseñas. Los números se actualizan al recargar.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP1 Mi cuenta y mi panel | Media | 3 | Web · Móvil · API | TRV-501 |

**Reglas de negocio**

1. Por publicación: reservas pendientes, confirmadas, rechazadas y canceladas (cada estado por separado), ingresos confirmados, promedio y cantidad de reseñas.
2. Globales: solicitudes pendientes por decidir, ingresos confirmados del mes y ocupación.
3. Los ingresos solo suman reservas confirmadas.

**Criterios de aceptación**

**CA-P04-01 · Cada estado por separado**

- **Dado** que una publicación tiene reservas pendientes, confirmadas, rechazadas y canceladas
- **Cuando** veo sus indicadores
- **Entonces** cada estado se cuenta por separado y las pendientes o canceladas no suman como confirmadas ni como ingresos

  _Se prueba con:_ TC-API-057, TC-WEB-023, TC-MOB-025

**CA-P04-02 · Reseñas sobre el total**

- **Dado** que mi publicación tiene 25 reseñas
- **Cuando** veo su promedio
- **Entonces** el promedio y la cantidad corresponden a las 25 reseñas, no a una sola página

  _Se prueba con:_ TC-API-050

**CA-P04-03 · Los números se actualizan**

- **Dado** que un viajero reservó o yo acepté una solicitud
- **Cuando** recargo el panel
- **Entonces** los indicadores se recalculan con el cambio

  _Se prueba con:_ TC-API-057

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-050 | API (Karate) | Resumen: promedio y total coinciden con el conjunto completo | CA-P04-02 | Pasó |
| TC-API-057 | API (Karate) | Indicadores por estado tras crear reservas en 4 estados | CA-P04-01, CA-P04-03 | Pasó |
| TC-WEB-023 | Web (Selenium) | Panel: paginación y tarjetas de indicadores | CA-P04-01 | Pasó |
| TC-MOB-025 | Móvil (Appium) | Panel del proveedor en la app con indicadores | CA-P04-01 | Pendiente (ejecutar en PC) |

**API:** `GET /api/proveedor/servicios` · `GET /api/proveedor/indicadores`

---

## Épica EP2 · Mis hoteles

_Publicar y mantener hoteles y sus tipos de habitación._

### HU-P05 · Publicar un hotel

> **Como** proveedor, **quiero** publicar un hotel con sus fotos y tipos de habitación **para** que los viajeros puedan encontrarlo y reservarlo.

**En pocas palabras:** Completo los datos del hotel, sus fotos (hasta 5 en la galería) y al menos un tipo de habitación. Al guardar, el hotel aparece en el catálogo. Si algo falla, no queda un hotel a medias.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP2 Mis hoteles | Alta | 5 | Web · Móvil · API | TRV-502 |

**Reglas de negocio**

1. Obligatorios: nombre (máximo 150), dirección, ciudad, país, estrellas de 1 a 5, imagen principal (URL) y al menos un tipo de habitación.
2. Galería opcional de hasta 5 URLs; con 6 o más responde 422 (no se recorta en silencio).
3. Se crea el servicio (tipo hotel), su ficha y sus habitaciones en una sola transacción.

**Criterios de aceptación**

**CA-P05-01 · Hotel publicado**

- **Dado** que completo los datos y un tipo de habitación
- **Cuando** publico
- **Entonces** se crea mi hotel y aparece en el catálogo con los mismos datos e imágenes

  _Se prueba con:_ TC-API-059, TC-WEB-024, TC-MOB-026

**CA-P05-02 · Máximo 5 imágenes**

- **Dado** que agrego 6 imágenes a la galería
- **Cuando** guardo
- **Entonces** veo «Máximo 5 imágenes» (422) y no se guarda nada

  _Se prueba con:_ TC-API-060

**CA-P05-03 · Todo o nada**

- **Dado** que uno de los tipos de habitación es inválido
- **Cuando** se procesa la publicación
- **Entonces** no queda un hotel a medias ni duplicado

  _Se prueba con:_ TC-API-084

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-059 | API (Karate) | Crear hotel con habitaciones y verificar en el catálogo | CA-P05-01 | Pasó |
| TC-API-060 | API (Karate) | 6 imágenes → 422 | CA-P05-02 | Pasó |
| TC-API-084 | API (Karate) | Tipo de habitación inválido: no queda hotel a medias | CA-P05-03 | Pasó |
| TC-WEB-024 | Web (Selenium) | Crear hotel desde el formulario del proveedor | CA-P05-01 | Pasó |
| TC-MOB-026 | Móvil (Appium) | Crear hotel desde la app del proveedor | CA-P05-01 | Pendiente (ejecutar en PC) |

**API:** `POST /api/hoteles` · `GET /api/hoteles/{id}`

---

### HU-P06 · Editar un hotel

> **Como** proveedor, **quiero** cambiar la descripción, fotos y datos de mi hotel **para** mantener la información al día sin afectar las reservas ya hechas.

**En pocas palabras:** Edito mi hotel cuando quiera; conserva su identificador y las reservas existentes no cambian. Nadie más puede editar mis hoteles.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP2 Mis hoteles | Media | 3 | Web · Móvil · API | TRV-502 |

**Reglas de negocio**

1. Editar conserva el id y nunca borra habitaciones con reservas activas (ver HU-P07).
2. Editar no modifica códigos, fechas ni importes de reservas existentes.
3. Solo el dueño edita (otro: 403).

**Criterios de aceptación**

**CA-P06-01 · Cambios sin afectar reservas**

- **Dado** que mi hotel tiene reservas
- **Cuando** cambio la descripción y una imagen
- **Entonces** se conserva el mismo id y las reservas no cambian

  _Se prueba con:_ TC-API-083

**CA-P06-02 · Datos inválidos o hotel ajeno**

- **Dado** que faltan datos obligatorios o el hotel es de otro proveedor
- **Cuando** guardo
- **Entonces** la API responde 422 o 403 y no se reemplaza nada

  _Se prueba con:_ TC-API-061

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-061 | API (Karate) | Editar hotel ajeno → 403 | CA-P06-02 | Pasó |
| TC-API-083 | API (Karate) | Editar descripción e imágenes conserva id y reservas | CA-P06-01 | Pasó |

**API:** `PUT /api/hoteles/{id}` · `GET /api/hoteles/{id}`

---

### HU-P07 · Gestionar los tipos de habitación

> **Como** proveedor, **quiero** agregar, editar y quitar tipos de habitación de mi hotel **para** ofrecer lo que realmente tengo sin dejar mal a quien ya reservó.

**En pocas palabras:** Agrego o edito tipos (Doble, Suite…) con capacidad, unidades y tarifa. El sistema no me deja bajar unidades por debajo de lo ya reservado ni borrar un tipo con reservas activas. Cambiar la tarifa no altera reservas anteriores.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP2 Mis hoteles | Media | 5 | Web · Móvil · API | TRV-503 |

**Reglas de negocio**

1. Campos: nombre único en el hotel, capacidad de adultos ≥ 1, niños ≥ 0, unidades ≥ 1, tarifa > 0.
2. No se pueden bajar las unidades por debajo del máximo ocupado en cualquier fecha futura (409 con el mínimo).
3. No se puede eliminar un tipo con reservas activas (409); sin ellas, borrado lógico tras confirmar.
4. Cambiar la tarifa no altera reservas existentes.

**Criterios de aceptación**

**CA-P07-01 · Agregar o editar**

- **Dado** que mi hotel existe
- **Cuando** guardo un tipo de habitación válido
- **Entonces** queda asociado al hotel y se ve igual en web y app

  _Se prueba con:_ TC-WEB-025, TC-MOB-027

**CA-P07-02 · No bajar de lo reservado**

- **Dado** que hay 3 unidades ocupadas el 15/12
- **Cuando** intento dejar el tipo con 2 unidades
- **Entonces** la API responde 409 «Mínimo permitido: 3»

  _Se prueba con:_ TC-API-062

**CA-P07-03 · Eliminar solo si no hay reservas**

- **Dado** que el tipo tiene reservas activas
- **Cuando** intento eliminarlo
- **Entonces** no se permite (409); sin reservas, se elimina después de confirmar (204)

  _Se prueba con:_ TC-API-063

**CA-P07-04 · El precio de reservas pasadas no cambia**

- **Dado** que edito la tarifa
- **Cuando** consulto reservas anteriores
- **Entonces** conservan su precio original

  _Se prueba con:_ TC-API-064

**CA-P07-05 · Datos inválidos o tipo ajeno**

- **Dado** que la tarifa es 0 o el tipo es de otro hotel
- **Cuando** guardo
- **Entonces** la API responde 422 o 403 y no cambia nada

  _Se prueba con:_ TC-API-085

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-062 | API (Karate) | Reducir unidades bajo ocupación → 409 con mínimo | CA-P07-02 | Pasó |
| TC-API-063 | API (Karate) | Eliminar con reservas → 409; sin reservas → 204 | CA-P07-03 | Pasó |
| TC-API-064 | API (Karate) | Cambio de tarifa no altera total de reservas previas | CA-P07-04 | Pasó |
| TC-API-085 | API (Karate) | Tarifa 0 → 422 y tipo ajeno → 403 sin cambios | CA-P07-05 | Pasó |
| TC-WEB-025 | Web (Selenium) | Agregar y editar tipo de habitación | CA-P07-01 | Pasó |
| TC-MOB-027 | Móvil (Appium) | Agregar tipo de habitación en la app | CA-P07-01 | Pendiente (ejecutar en PC) |

**API:** `POST /api/hoteles/{id}/habitaciones` · `PUT /api/habitaciones/{id}` · `DELETE /api/habitaciones/{id}`

---

## Épica EP3 · Mis tours

_Publicar tours, armar su programa y abrir salidas con cupos._

### HU-P08 · Publicar un tour

> **Como** proveedor, **quiero** publicar un tour con su categoría, precio y al menos una salida **para** que los viajeros puedan reservarlo.

**En pocas palabras:** Completo nombre, descripción, destino, categoría, duración, precio, fotos, lo que hay que llevar y la primera salida. Al guardar, el tour aparece en el catálogo.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP3 Mis tours | Alta | 3 | Web · Móvil · API | TRV-504 |

**Reglas de negocio**

1. Obligatorios: nombre, descripción, ciudad, país, categoría permitida, duración > 0 (minutos), precio ≥ 0, imagen principal y al menos una salida futura.
2. Hasta 5 imágenes de galería y hasta 10 elementos de «qué llevar».

**Criterios de aceptación**

**CA-P08-01 · Tour publicado**

- **Dado** que completo los datos y una salida
- **Cuando** publico
- **Entonces** se crea mi tour y aparece en el catálogo

  _Se prueba con:_ TC-API-065, TC-WEB-026, TC-MOB-028

**CA-P08-02 · Se respetan los límites**

- **Dado** que agrego 11 elementos de «qué llevar» o 6 imágenes
- **Cuando** guardo
- **Entonces** la API responde 422 indicando el límite y no se guarda

  _Se prueba con:_ TC-API-067

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-065 | API (Karate) | Crear tour y verificar en catálogo | CA-P08-01 | Pasó |
| TC-API-067 | API (Karate) | Data-driven de límites (imágenes, items, categoría) → 422 | CA-P08-02 | Pasó |
| TC-WEB-026 | Web (Selenium) | Crear tour con salida desde el formulario | CA-P08-01 | Pasó |
| TC-MOB-028 | Móvil (Appium) | Crear tour desde la app | CA-P08-01 | Pendiente (ejecutar en PC) |

**API:** `POST /api/tours` · `GET /api/tours/{id}`

---

### HU-P09 · Editar un tour

> **Como** proveedor, **quiero** cambiar los datos de mi tour **para** corregir o mejorar la oferta sin perjudicar a quien ya reservó.

**En pocas palabras:** Edito los datos generales del tour (por ejemplo, el precio). Las salidas y las reservas existentes no se tocan: las salidas se gestionan en HU-P11.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP3 Mis tours | Media | 2 | Web · Móvil · API | TRV-504 |

**Reglas de negocio**

1. Editar conserva el id y no recrea salidas ni altera cupos reservados ni importes ya cobrados.
2. Solo el dueño edita (otro: 403).

**Criterios de aceptación**

**CA-P09-01 · Cambios sin afectar reservas**

- **Dado** que el tour tiene reservas
- **Cuando** cambio el precio
- **Entonces** las salidas conservan sus ids y las reservas su importe original

  _Se prueba con:_ TC-API-066

**CA-P09-02 · Datos inválidos o tour ajeno**

- **Dado** que la categoría no existe o el tour es de otro proveedor
- **Cuando** guardo
- **Entonces** la API responde 422 o 403 y no cambia nada

  _Se prueba con:_ TC-API-067

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-066 | API (Karate) | Editar precio: ids de salidas y totales de reservas sin cambio | CA-P09-01 | Pasó |
| TC-API-067 | API (Karate) | Data-driven de límites (imágenes, items, categoría) → 422 | CA-P09-02 | Pasó |

**API:** `PUT /api/tours/{id}`

---

### HU-P10 · Armar el programa de actividades del tour

> **Como** proveedor, **quiero** definir las actividades del tour en orden **para** que el viajero sepa qué hará en cada momento.

**En pocas palabras:** Agrego, edito, reordeno y quito actividades. Si no indico el orden, la nueva va al final. El programa se muestra separado de la lista de «qué llevar».

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP3 Mis tours | Baja | 3 | Web · Móvil · API | TRV-505 |

**Reglas de negocio**

1. Campos: título (máximo 150), descripción, orden ≥ 1 (si falta, se asigna el siguiente) y duración en minutos ≥ 0.
2. El orden no se repite en el tour; insertar en un orden ocupado desplaza a las siguientes.
3. Eliminar pide confirmación y no afecta salidas ni reservas.

**Criterios de aceptación**

**CA-P10-01 · Programa en orden**

- **Dado** que abro mi tour
- **Cuando** veo sus actividades
- **Entonces** aparecen en su orden y separadas de «qué llevar»

  _Se prueba con:_ TC-WEB-027

**CA-P10-02 · Agregar al final**

- **Dado** que guardo una actividad válida sin indicar el orden
- **Cuando** se registra
- **Entonces** queda al final con el número siguiente

  _Se prueba con:_ TC-API-068, TC-WEB-027, TC-MOB-029

**CA-P10-03 · Quitar una actividad**

- **Dado** que confirmo eliminar una actividad
- **Cuando** termina
- **Entonces** deja de aparecer sin tocar salidas ni reservas

  _Se prueba con:_ TC-WEB-027

**CA-P10-04 · Datos inválidos o tour ajeno**

- **Dado** que el título está vacío o el tour es de otro proveedor
- **Cuando** guardo
- **Entonces** la API responde 422 o 403

  _Se prueba con:_ TC-API-069

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-068 | API (Karate) | Dos actividades sin orden reciben 1 y 2 | CA-P10-02 | Pasó |
| TC-API-069 | API (Karate) | Actividad en tour ajeno → 403 | CA-P10-04 | Pasó |
| TC-WEB-027 | Web (Selenium) | Agregar, reordenar y eliminar actividades | CA-P10-01, CA-P10-02, CA-P10-03 | Pasó |
| TC-MOB-029 | Móvil (Appium) | Agregar actividad desde la app | CA-P10-02 | Pendiente (ejecutar en PC) |

**API:** `GET /api/tours/{id}/actividades` · `POST /api/tours/{id}/actividades` · `PUT /api/tours/{id}/actividades/{a}` · `DELETE /api/tours/{id}/actividades/{a}`

---

### HU-P11 · Programar salidas y cupos

> **Como** proveedor, **quiero** abrir, editar, cerrar y quitar salidas de mis tours con su cupo **para** controlar cuántas personas pueden ir en cada fecha.

**En pocas palabras:** Cada salida tiene fecha, hora y cupo total. El sistema lleva la cuenta de lo reservado: no puedo dejar el cupo por debajo de eso. Cerrar o cancelar una salida impide nuevas reservas pero respeta las que ya existen.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP3 Mis tours | Alta | 5 | Web · Móvil · API | TRV-506 |

**Reglas de negocio**

1. Estados de salida: programada, cerrada (no admite nuevas reservas) y cancelada.
2. Crear: fecha desde hoy, hora HH:MM y cupo total ≥ 1; empieza programada con 0 reservados.
3. No se repite fecha + hora en el mismo tour (409).
4. `cupo_reservado` solo lo cambia el sistema; el cupo total nunca queda por debajo de lo reservado.
5. No se elimina una salida con reservas confirmadas (409).

**Criterios de aceptación**

**CA-P11-01 · Nueva salida**

- **Dado** que mi tour existe
- **Cuando** creo una salida válida
- **Entonces** queda programada con 0 cupos reservados

  _Se prueba con:_ TC-WEB-028, TC-MOB-030

**CA-P11-02 · Salida repetida o pasada**

- **Dado** que ya existe esa fecha y hora, o la fecha ya pasó
- **Cuando** creo la salida
- **Entonces** la API responde 409 o 422 y no se duplica

  _Se prueba con:_ TC-API-070

**CA-P11-03 · No bajar de lo reservado**

- **Dado** que la salida tiene 6 cupos reservados
- **Cuando** pongo cupo total 5
- **Entonces** la API responde 422 «No menor a lo reservado (6)»; si envío `cupo_reservado`, se ignora

  _Se prueba con:_ TC-API-071

**CA-P11-04 · Cerrar o cancelar**

- **Dado** que cierro la salida
- **Cuando** un viajero intenta reservarla
- **Entonces** recibe 409 y las reservas anteriores siguen vigentes

  _Se prueba con:_ TC-API-072, TC-WEB-028, TC-MOB-030

**CA-P11-05 · Eliminar solo si no hay reservas**

- **Dado** que la salida tiene reservas confirmadas
- **Cuando** intento eliminarla
- **Entonces** la API responde 409; sin reservas, se elimina después de confirmar

  _Se prueba con:_ TC-WEB-028

**CA-P11-06 · Lo mismo en web y app**

- **Dado** que guardé un cambio en una salida
- **Cuando** la consulto en web y en la app
- **Entonces** veo los mismos estados y cupos

  _Se prueba con:_ TC-WEB-028, TC-MOB-030

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-070 | API (Karate) | Salida duplicada → 409; fecha pasada → 422 | CA-P11-02 | Pasó |
| TC-API-071 | API (Karate) | cupo_total menor a reservado → 422; cupo_reservado enviado se ignora | CA-P11-03 | Pasó |
| TC-API-072 | API (Karate) | Salida cerrada rechaza reservas (call a reserva de viajero) | CA-P11-04 | Pasó |
| TC-WEB-028 | Web (Selenium) | Crear, cerrar y eliminar salida desde el panel | CA-P11-01, CA-P11-04, CA-P11-05, CA-P11-06 | Pasó |
| TC-MOB-030 | Móvil (Appium) | Crear y cerrar salida desde la app | CA-P11-01, CA-P11-04, CA-P11-06 | Pendiente (ejecutar en PC) |

**API:** `GET /api/tours/{id}/salidas` · `POST /api/tours/{id}/salidas` · `PUT /api/tours/{id}/salidas/{s}` · `DELETE /api/tours/{id}/salidas/{s}`

---

## Épica EP4 · Reservas recibidas

_Atender las solicitudes de los viajeros y retirar publicaciones._

### HU-P12 · Ver las reservas que recibo

> **Como** proveedor, **quiero** ver todas las reservas de mis hoteles y tours **para** saber quién viene, cuándo y qué solicitudes debo atender.

**En pocas palabras:** En «Reservas recibidas» veo las reservas de mis servicios con los datos del cliente. Las solicitudes de hotel pendientes aparecen primero y con sus botones Aceptar y Rechazar; las de tour ya vienen confirmadas.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP4 Reservas recibidas | Alta | 3 | Web · Móvil · API | TRV-507, TRV-204 |

**Reglas de negocio**

1. Lista paginada (15 por página) solo de reservas de servicios propios, filtrable por tipo, estado y servicio.
2. Cada reserva: código, cliente (nombre y correo), servicio, habitación o salida, fechas, cantidades, subtotal, IGV, total, estado y motivo si fue rechazada.
3. Las pendientes se destacan y van primero cuando el filtro es «Todas».
4. Las reservas de tour están confirmadas desde el inicio y no tienen botones de decisión.

**Criterios de aceptación**

**CA-P12-01 · Listado completo**

- **Dado** que recibí reservas
- **Cuando** abro «Reservas recibidas»
- **Entonces** veo solo las de mis servicios, con todos sus datos, y puedo recorrer todas las páginas

  _Se prueba con:_ TC-API-073, TC-WEB-029, TC-MOB-031

**CA-P12-02 · Filtrar pendientes**

- **Dado** que filtro por Pendiente
- **Cuando** aplico el filtro
- **Entonces** solo veo solicitudes de hotel pendientes, con sus botones Aceptar y Rechazar

  _Se prueba con:_ TC-API-074, TC-WEB-029

**CA-P12-03 · Tours sin aprobación**

- **Dado** que es una reserva de tour
- **Cuando** la consulto
- **Entonces** aparece confirmada y no tiene botones de aceptar o rechazar

  _Se prueba con:_ TC-API-073

**CA-P12-04 · Vacío no es lo mismo que un error**

- **Dado** que no tengo reservas o la API falla
- **Cuando** abro la bandeja
- **Entonces** distingo «sin reservas» del error con Reintentar

  _Se prueba con:_ TC-WEB-032

**CA-P12-05 · Reserva de otro proveedor**

- **Dado** que pido el detalle de una reserva de otro proveedor
- **Cuando** entro
- **Entonces** la API responde 404

  _Se prueba con:_ TC-API-075

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-073 | API (Karate) | Solo reservas de servicios propios (match each servicio.proveedor_id) | CA-P12-01, CA-P12-03 | Pasó |
| TC-API-074 | API (Karate) | Filtro estado=pendiente | CA-P12-02 | Pasó |
| TC-API-075 | API (Karate) | Detalle ajeno → 404 | CA-P12-05 | Pasó |
| TC-WEB-029 | Web (Selenium) | Bandeja de reservas con filtros y paginación | CA-P12-01, CA-P12-02 | Pasó |
| TC-WEB-032 | Web (Selenium) | Sin resultados vs. error con Reintentar (tours y bandeja del proveedor) | CA-P12-04 | Pasó |
| TC-MOB-031 | Móvil (Appium) | Bandeja de reservas en la app | CA-P12-01 | Pendiente (ejecutar en PC) |

**API:** `GET /api/proveedor/reservas?tipo&estado&servicio_id&page` · `GET /api/proveedor/reservas/hotel/{id}`

---

### HU-P13 · Aceptar una solicitud de hotel

> **Como** proveedor, **quiero** aceptar una solicitud de reserva de mi hotel **para** confirmarle la estadía al viajero.

**En pocas palabras:** Al aceptar, la solicitud pasa de PENDIENTE a CONFIRMADA con el mismo importe y el viajero la ve confirmada. Solo puedo decidir sobre solicitudes pendientes de mis propios hoteles.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP4 Reservas recibidas | Más alta | 5 | Web · Móvil · API | TRV-204 |

**Reglas de negocio**

1. Estados: pendiente → confirmada | rechazada | cancelada; confirmada → cancelada.
2. Solo el proveedor dueño del hotel decide (otro: 403).
3. Solo se decide sobre reservas pendientes; en otro estado responde 409 con el estado actual.
4. Aceptar conserva datos e importe y no vuelve a ocupar unidades. Se registra quién decidió y cuándo.
5. La decisión usa bloqueo de fila: dos decisiones simultáneas no se pisan.

**Criterios de aceptación**

**CA-P13-01 · Aceptar**

- **Dado** que tengo una solicitud pendiente de mi hotel
- **Cuando** la acepto
- **Entonces** la misma reserva queda CONFIRMADA con su importe y el viajero la ve confirmada en web y app

  _Se prueba con:_ TC-API-031, TC-WEB-013, TC-MOB-015

**CA-P13-02 · Si no decido, sigue pendiente**

- **Dado** que no he decidido sobre una solicitud
- **Cuando** actualizo la lista
- **Entonces** sigue PENDIENTE

  _Se prueba con:_ TC-WEB-013

**CA-P13-03 · No se decide dos veces**

- **Dado** que la solicitud ya fue aceptada, rechazada o cancelada por el viajero
- **Cuando** intento aceptarla
- **Entonces** la API responde 409 con el estado actual y no cambia nada

  _Se prueba con:_ TC-API-033

**CA-P13-04 · Solo mis solicitudes**

- **Dado** que la solicitud es de un hotel de otro proveedor
- **Cuando** intento decidir
- **Entonces** la API responde 403

  _Se prueba con:_ TC-API-034

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-031 | API (Karate) | Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | CA-P13-01 | Pasó |
| TC-API-033 | API (Karate) | Aceptar dos veces → 409; aceptar una cancelada → 409 | CA-P13-03 | Pasó |
| TC-API-034 | API (Karate) | Proveedor B sobre reserva de proveedor A → 403 | CA-P13-04 | Pasó |
| TC-WEB-013 | Web (Selenium) | Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | CA-P13-01, CA-P13-02 | Pasó |
| TC-MOB-015 | Móvil (Appium) | Proveedor acepta una solicitud desde la app | CA-P13-01 | Pendiente (ejecutar en PC) |

**API:** `POST /api/proveedor/reservas-habitaciones/{id}/aceptar`

---

### HU-P14 · Rechazar una solicitud de hotel

> **Como** proveedor, **quiero** rechazar una solicitud explicando el motivo **para** liberar la habitación y que el viajero sepa por qué.

**En pocas palabras:** Al rechazar escribo un motivo; la solicitud queda RECHAZADA, sus habitaciones vuelven a estar libres para otros viajeros y el viajero ve el motivo.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP4 Reservas recibidas | Más alta | 3 | Web · Móvil · API | TRV-204 |

**Reglas de negocio**

1. Motivo obligatorio de 10 a 300 caracteres.
2. Rechazar libera las unidades al instante y una sola vez.
3. Mismas reglas de dueño, estado y concurrencia que HU-P13.

**Criterios de aceptación**

**CA-P14-01 · Rechazar libera las habitaciones**

- **Dado** que tengo una solicitud pendiente
- **Cuando** la rechazo con un motivo
- **Entonces** queda RECHAZADA y sus unidades vuelven a estar disponibles en la búsqueda

  _Se prueba con:_ TC-API-032, TC-WEB-014, TC-MOB-016

**CA-P14-02 · Motivo obligatorio**

- **Dado** que no escribo motivo o tiene menos de 10 caracteres
- **Cuando** intento rechazar
- **Entonces** la API responde 422 y la solicitud sigue PENDIENTE

  _Se prueba con:_ TC-API-035

**CA-P14-03 · El viajero ve el motivo**

- **Dado** que rechacé una solicitud
- **Cuando** el viajero abre «Mis reservas»
- **Entonces** la ve RECHAZADA con mi motivo

  _Se prueba con:_ TC-WEB-014, TC-MOB-016

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-032 | API (Karate) | Rechazo libera unidades: disponibilidad antes y después | CA-P14-01 | Pasó |
| TC-API-035 | API (Karate) | Rechazo sin motivo → 422 | CA-P14-02 | Pasó |
| TC-WEB-014 | Web (Selenium) | Proveedor rechaza con motivo y el viajero ve RECHAZADA | CA-P14-01, CA-P14-03 | Pasó |
| TC-MOB-016 | Móvil (Appium) | Proveedor rechaza con motivo desde la app | CA-P14-01, CA-P14-03 | Pendiente (ejecutar en PC) |

**API:** `POST /api/proveedor/reservas-habitaciones/{id}/rechazar {motivo}`

---

### HU-P15 · Eliminar una publicación

> **Como** proveedor, **quiero** retirar un hotel o tour que ya no ofrezco **para** que deje de aparecer sin borrar el historial de reservas.

**En pocas palabras:** Elimino una publicación después de confirmar. Si tiene reservas por cumplir, no se permite. Las reservas pasadas siguen visibles para mí y para el viajero, con el nombre del servicio.

| Épica | Prioridad | Story points | Canales | Reemplaza a |
|---|---|---|---|---|
| EP4 Reservas recibidas | Media | 3 | Web · Móvil · API | TRV-508 |

**Reglas de negocio**

1. Solo el dueño elimina (otro: 403).
2. Se bloquea si hay reservas activas (pendientes o confirmadas futuras): 409.
3. Se pide confirmación mostrando el nombre de la publicación.
4. Es un borrado lógico: las reservas históricas conservan código, servicio, fechas, cantidades e importes.
5. Eliminar dos veces responde 404 sin efectos adicionales.

**Criterios de aceptación**

**CA-P15-01 · Me arrepiento**

- **Dado** que pulsé Eliminar
- **Cuando** cancelo en el diálogo
- **Entonces** la publicación sigue igual

  _Se prueba con:_ TC-WEB-030

**CA-P15-02 · Eliminación permitida**

- **Dado** que la publicación no tiene reservas activas
- **Cuando** confirmo
- **Entonces** deja de aparecer en el catálogo y en mi panel, sin afectar mis otras publicaciones

  _Se prueba con:_ TC-WEB-030, TC-MOB-032

**CA-P15-03 · El historial se conserva**

- **Dado** que la publicación tenía reservas pasadas
- **Cuando** la elimino
- **Entonces** el viajero y yo seguimos viendo esas reservas con el nombre del servicio

  _Se prueba con:_ TC-API-078

**CA-P15-04 · Publicación ajena o con reservas activas**

- **Dado** que la publicación no es mía o tiene reservas por cumplir
- **Cuando** intento eliminarla
- **Entonces** la API responde 403 o 409 y no cambia nada

  _Se prueba con:_ TC-API-076, TC-API-077

**CA-P15-05 · Eliminar dos veces**

- **Dado** que ya eliminé la publicación
- **Cuando** repito la eliminación
- **Entonces** la API responde 404 sin efectos adicionales

  _Se prueba con:_ TC-API-086

**Cómo se prueba**

| Caso | Nivel | Qué comprueba | Criterios | Último resultado |
|---|---|---|---|---|
| TC-API-076 | API (Karate) | Eliminar servicio ajeno → 403 (seguridad) | CA-P15-04 | Pasó |
| TC-API-077 | API (Karate) | Con reserva activa → 409 | CA-P15-04 | Pasó |
| TC-API-078 | API (Karate) | Tras eliminar, GET /mis-reservas del viajero sigue 200 con el nombre del servicio | CA-P15-03 | Pasó |
| TC-API-086 | API (Karate) | Repetir la eliminación devuelve 404 sin efectos | CA-P15-05 | Pasó |
| TC-WEB-030 | Web (Selenium) | Eliminar publicación con diálogo de confirmación | CA-P15-01, CA-P15-02 | Pasó |
| TC-MOB-032 | Móvil (Appium) | Eliminar publicación desde la app | CA-P15-02 | Pendiente (ejecutar en PC) |

**API:** `DELETE /api/servicios/{id}`

---
