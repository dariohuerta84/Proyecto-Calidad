# Trouvels360 — Backlog de Historias de Usuario

> Proyecto Jira: **TRV** · 6 épicas · 24 historias · 5 tareas técnicas de QA · 119 criterios de aceptación · 146 casos de prueba · 142 story points

## 1. Contexto del proyecto

Marketplace de turismo que conecta viajeros con proveedores locales de hoteles y tours. El viajero busca, cotiza y reserva con pago simulado; el proveedor publica su oferta y aprueba o rechaza las solicitudes de hotel. Los tours se confirman automáticamente descontando cupos.

### ¿Por qué este rubro justifica Karate, JMeter, Selenium y Appium?

| Aspecto | Cómo se cumple en Trouvels360 |
| --- | --- |
| Manejo de datos | Catálogo de hoteles, habitaciones, tours, salidas, reservas, reseñas y usuarios con relaciones, estados y cálculos (noches × tarifa × unidades + 18% de IGV). |
| Flujo de aprobación | Toda reserva de hotel nace PENDIENTE y el proveedor la ACEPTA (confirmada) o la RECHAZA (libera unidades). El viajero ve el cambio de estado en web y móvil. |
| Condiciones de negocio | Disponibilidad por rango de fechas, cupos por salida, capacidad por habitación, reseñas solo tras estadía finalizada, bloqueos al eliminar con reservas activas, idempotencia y concurrencia sobre las últimas unidades. |
| Karate (API) | Contratos JSON, códigos de estado, reglas de negocio y autorización por rol sobre la API REST. |
| JMeter (rendimiento) | Búsqueda de catálogo con alta concurrencia y reserva simultánea de las últimas unidades/cupos. |
| Selenium (web) | Flujos end-to-end del viajero y del proveedor en la versión web Angular. |
| Appium (móvil) | Los mismos flujos en la app Android nativa (Kotlin + Compose). |

### Decisiones de negocio adoptadas

| Tema | Decisión |
| --- | --- |
| Eliminar cuenta con reservas activas | Se bloquea (HTTP 409) mientras existan reservas pendientes o confirmadas cuya estadía o salida no haya terminado. |
| Eliminar publicación con reservas activas | Se bloquea (HTTP 409). Sin reservas activas se hace borrado lógico y el historial se conserva. |
| Rechazo de solicitud de hotel | Libera las unidades al instante, una sola vez. El estado 'rechazada' es distinto de 'cancelada'. |
| Impuesto | IGV 18%. Se guardan subtotal, impuesto y total por separado, redondeados a 2 decimales (half-up). |
| Imagen de perfil | Se guarda como URL (avatar_url). La subida de archivos queda fuera de alcance. |
| Reserva de varios tipos de habitación | Transacción única: o se registran todos los tipos o ninguno (sin reservas parciales). |
| Envíos repetidos | Header Idempotency-Key en las reservas: el mismo key devuelve la misma reserva sin duplicar. |

### Definition of Done (aplica a todas las historias)

- [ ] Criterios de aceptación verificados en web y móvil (si el canal aplica).
- [ ] Casos de prueba automatizados vinculados a la historia y en verde en el último pipeline.
- [ ] Contrato de la API documentado y cubierto por Karate (códigos, esquema y reglas).
- [ ] Sin defectos abiertos de severidad Alta o Crítica asociados.
- [ ] Evidencia (reporte HTML / capturas) adjunta al ticket.

### Convenciones para Jira

- **Tipo de incidencia:** Épica → Historia → Subtarea (caso de prueba). Las tareas de QA usan el tipo *Tarea*.
- **Prioridad:** Más alta, Alta, Media, Baja. **Story points:** Fibonacci (1, 2, 3, 5, 8).
- **Criterios de aceptación:** formato Gherkin (Dado / Cuando / Entonces) con id `CA-<HU>-NN`.
- **Casos de prueba:** `TC-API` (Karate), `TC-PERF` (JMeter), `TC-WEB` (Selenium), `TC-MOB` (Appium), cada uno enlazado a los criterios que valida.
- **Componentes:** Backend, Web, Móvil, Microservicio, QA. **Etiquetas:** dominio + `rubrica-N` en las tareas de QA.

## 2. Mapa de épicas

| Épica | Nombre | Historias | Puntos |
| --- | --- | --- | --- |
| TRV-1 | Identidad y cuentas | TRV-101, TRV-102, TRV-103, TRV-104, TRV-105 | 19 |
| TRV-2 | Hoteles y aprobación de reservas | TRV-201, TRV-202, TRV-203, TRV-204 | 26 |
| TRV-3 | Tours y salidas | TRV-301, TRV-302, TRV-303 | 11 |
| TRV-4 | Postventa del viajero | TRV-401, TRV-402, TRV-403, TRV-404 | 18 |
| TRV-5 | Portal del proveedor | TRV-501, TRV-502, TRV-503, TRV-504, TRV-505, TRV-506, TRV-507, TRV-508 | 36 |
| TRV-6 | Calidad y automatización | TRV-601, TRV-602, TRV-603, TRV-604, TRV-605 | 32 |

## 3. Historias de usuario

### Épica TRV-1 — Identidad y cuentas

_Registro, sesión, perfil y eliminación de cuenta para viajeros y proveedores._

#### TRV-101 · Registro de cuenta de viajero

**Como** viajero, **quiero** crear mi cuenta con mis datos personales **para** poder reservar hoteles y tours a mi nombre.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-1 Identidad y cuentas |
| Prioridad | Alta |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | auth, viajero, validaciones |
| HU de origen | HU-VIA-001 |

**Contexto.** El formulario de registro permite elegir el rol. Esta historia cubre el rol viajero; el proveedor está en TRV-102.

**Reglas de negocio**

1. Campos obligatorios: nombre (máx. 100), apellido (máx. 100), correo válido (máx. 150), contraseña y confirmación.
2. La contraseña tiene mínimo 6 caracteres y debe coincidir con su confirmación (password_confirmation).
3. El correo se normaliza a minúsculas y es único, incluso frente a cuentas eliminadas lógicamente.
4. Tras registrarse, la sesión queda iniciada (cookie HttpOnly access_token) y se redirige al catálogo de hoteles.

**Criterios de aceptación**

- **CA-TRV-101-01 · Registro exitoso**  
  **Dado** que completo nombre, apellido, correo nuevo y contraseña de 6+ caracteres con confirmación igual  
  **Cuando** envío el formulario de registro como viajero  
  **Entonces** se crea una sola cuenta con rol viajero, la API responde 201 con el usuario sin contraseña y quedo con sesión iniciada
- **CA-TRV-101-02 · Campos inválidos**  
  **Dado** que dejo vacío un campo obligatorio o el correo no tiene formato válido  
  **Cuando** intento registrarme  
  **Entonces** se marca el campo con su mensaje, la API responde 422 con el detalle por campo y no se crea ninguna cuenta
- **CA-TRV-101-03 · Contraseñas distintas**  
  **Dado** que la confirmación no coincide con la contraseña  
  **Cuando** envío el registro  
  **Entonces** se muestra 'Las contraseñas no coinciden' y no se crea la cuenta
- **CA-TRV-101-04 · Correo duplicado**  
  **Dado** que ya existe una cuenta con el mismo correo (sin importar mayúsculas)  
  **Cuando** envío el registro  
  **Entonces** la API responde 422 'El correo ya está registrado' y no se crea una segunda cuenta
- **CA-TRV-101-05 · Rol no manipulable**  
  **Dado** que envío un rol distinto de viajero o proveedor  
  **Cuando** llamo a la API de registro  
  **Entonces** responde 422 y no se crea la cuenta

**Endpoints involucrados:** `GET /api/auth/csrf` · `POST /api/auth/register`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-001 | Karate | Registro viajero válido devuelve 201 y esquema de usuario sin password | CA-TRV-101-01 |
| TC-API-002 | Karate | Data-driven (CSV) de campos inválidos devuelve 422 con error por campo | CA-TRV-101-02, CA-TRV-101-03 |
| TC-API-003 | Karate | Correo duplicado en mayúsculas devuelve 422 | CA-TRV-101-04 |
| TC-API-004 | Karate | Rol 'admin' devuelve 422 | CA-TRV-101-05 |
| TC-WEB-001 | Selenium | Registro de viajero end-to-end y aterrizaje en catálogo | CA-TRV-101-01 |
| TC-WEB-002 | Selenium | Mensajes de validación en el formulario | CA-TRV-101-02, CA-TRV-101-03 |
| TC-MOB-001 | Appium | Registro de viajero desde la app y sesión iniciada | CA-TRV-101-01 |
| TC-MOB-002 | Appium | Botón Registrarme deshabilitado con contraseñas distintas | CA-TRV-101-03 |

---

#### TRV-102 · Registro de cuenta de proveedor

**Como** proveedor, **quiero** registrar mi empresa con RUC y teléfono **para** publicar hoteles y tours en la plataforma.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-1 Identidad y cuentas |
| Prioridad | Alta |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | auth, proveedor, validaciones |
| HU de origen | HU-PRO-001 |

**Contexto.** Los proveedores son empresas peruanas: se valida RUC de 11 dígitos y celular peruano.

**Reglas de negocio**

1. Obligatorios: nombre de empresa (máx. 150), RUC, teléfono, correo, contraseña y confirmación.
2. RUC: exactamente 11 dígitos, único en el sistema.
3. Teléfono: se aceptan '987654321', '+51987654321' o '+51 987 654 321'; se guarda normalizado como '+51 9XXXXXXXX'.
4. Nombre y apellido personales no se guardan para el proveedor.

**Criterios de aceptación**

- **CA-TRV-102-01 · Registro exitoso**  
  **Dado** que ingreso empresa, RUC de 11 dígitos nuevo, celular peruano válido, correo nuevo y contraseña confirmada  
  **Cuando** me registro como proveedor  
  **Entonces** se crea una sola cuenta con rol proveedor y el teléfono queda guardado como '+51 9XXXXXXXX'
- **CA-TRV-102-02 · RUC inválido o duplicado**  
  **Dado** que el RUC no tiene 11 dígitos o ya está registrado  
  **Cuando** envío el registro  
  **Entonces** la API responde 422 indicando el problema del RUC y no se crea la cuenta
- **CA-TRV-102-03 · Teléfono inválido**  
  **Dado** que el teléfono no es un celular peruano (9 dígitos que empiezan en 9)  
  **Cuando** envío el registro  
  **Entonces** se informa 'El teléfono debe tener el formato +51 9########' y no se crea la cuenta
- **CA-TRV-102-04 · Correo duplicado o contraseñas distintas**  
  **Dado** que el correo ya existe o la confirmación no coincide  
  **Cuando** envío el registro  
  **Entonces** se informa el error y no se crea ninguna cuenta

**Endpoints involucrados:** `POST /api/auth/register`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-005 | Karate | Registro proveedor normaliza '987654321' a '+51 987654321' | CA-TRV-102-01 |
| TC-API-006 | Karate | Data-driven de RUC inválidos y duplicado (422) | CA-TRV-102-02 |
| TC-API-007 | Karate | Teléfonos inválidos devuelven 422 con mensaje | CA-TRV-102-03 |
| TC-WEB-003 | Selenium | Registro de proveedor y acceso al panel del proveedor | CA-TRV-102-01 |
| TC-MOB-003 | Appium | Registro de proveedor desde la app y acceso al panel | CA-TRV-102-01 |

---

#### TRV-103 · Inicio y cierre de sesión con retorno al destino

**Como** usuario (viajero o proveedor), **quiero** iniciar y cerrar sesión de forma segura **para** acceder solo a las funciones de mi rol.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-1 Identidad y cuentas |
| Prioridad | Más alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | auth, seguridad, jwt |
| HU de origen | HU-VIA-001, HU-PRO-001 |

**Contexto.** Autenticación JWT en cookie HttpOnly + CSRF de doble envío (cookie XSRF-TOKEN y header X-XSRF-TOKEN).

**Reglas de negocio**

1. El token dura 60 minutos; /auth/refresh lo renueva dentro de la ventana de refresco.
2. Máximo 6 intentos de login por minuto por IP (HTTP 429 al exceder).
3. Toda petición que modifica datos exige el header X-XSRF-TOKEN; sin él la API responde 403.
4. Si el login se inició desde una reserva, al autenticarse se vuelve a esa pantalla conservando la selección.
5. Cerrar sesión invalida el token en el servidor (lista negra), no solo en el cliente.

**Criterios de aceptación**

- **CA-TRV-103-01 · Login correcto**  
  **Dado** que tengo una cuenta activa  
  **Cuando** ingreso correo y contraseña correctos  
  **Entonces** accedo con mi rol y veo el menú de viajero o el panel de proveedor según corresponda
- **CA-TRV-103-02 · Retorno a la reserva**  
  **Dado** que pulsé 'Reservar' sin sesión con fechas y habitaciones elegidas  
  **Cuando** inicio sesión correctamente  
  **Entonces** vuelvo al detalle del hotel o tour con la misma selección si sigue disponible
- **CA-TRV-103-03 · Credenciales incorrectas**  
  **Dado** que la contraseña es incorrecta o falta un campo  
  **Cuando** intento ingresar  
  **Entonces** veo 'Credenciales inválidas' (401) o el campo faltante (422) y no se crea sesión
- **CA-TRV-103-04 · Sesión vencida**  
  **Dado** que mi token expiró  
  **Cuando** intento una acción privada  
  **Entonces** la API responde 401, se limpia la sesión local y se me pide iniciar sesión
- **CA-TRV-103-05 · Cierre de sesión**  
  **Dado** que tengo sesión iniciada  
  **Cuando** cierro sesión y reutilizo el token anterior  
  **Entonces** la API responde 401: el token quedó invalidado
- **CA-TRV-103-06 · Separación de roles**  
  **Dado** que soy viajero  
  **Cuando** llamo a una ruta exclusiva de proveedor  
  **Entonces** la API responde 403 y no se expone información

**Endpoints involucrados:** `GET /api/auth/csrf` · `POST /api/auth/login` · `GET /api/auth/me` · `POST /api/auth/refresh` · `POST /api/auth/logout`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-008 | Karate | Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | CA-TRV-103-01 |
| TC-API-009 | Karate | POST sin X-XSRF-TOKEN devuelve 403 | CA-TRV-103-01 |
| TC-API-010 | Karate | Credenciales inválidas 401 y faltantes 422 | CA-TRV-103-03 |
| TC-API-011 | Karate | Token reutilizado tras logout devuelve 401 | CA-TRV-103-05 |
| TC-API-012 | Karate | Viajero en ruta de proveedor devuelve 403 | CA-TRV-103-06 |
| TC-PERF-001 | JMeter | Login con 50 usuarios concurrentes desde CSV: p95 < 800 ms, 0% errores | CA-TRV-103-01 |
| TC-WEB-004 | Selenium | Login viajero y proveedor muestran el menú de su rol | CA-TRV-103-01, CA-TRV-103-06 |
| TC-WEB-005 | Selenium | Login desde 'Reservar' vuelve al detalle con la selección | CA-TRV-103-02 |
| TC-MOB-004 | Appium | Login, cierre de sesión y bloqueo de pestañas privadas | CA-TRV-103-01, CA-TRV-103-05 |
| TC-MOB-005 | Appium | Reservar sin sesión → login → regreso al detalle | CA-TRV-103-02 |

---

#### TRV-104 · Gestionar perfil y contraseña

**Como** usuario (viajero o proveedor), **quiero** actualizar mis datos, mi foto y mi contraseña **para** mantener mi información al día.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-1 Identidad y cuentas |
| Prioridad | Media |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | perfil, seguridad |
| HU de origen | HU-VIA-002, HU-PRO-002 |

**Contexto.** Cada usuario solo puede leer y modificar su propia cuenta (/usuarios/me). El rol nunca cambia.

**Reglas de negocio**

1. Viajero edita: nombre, apellido, avatar_url. Proveedor edita: empresa_nombre, teléfono (mismo formato que el registro), avatar_url.
2. avatar_url debe ser una URL http(s) válida de máximo 500 caracteres.
3. Cambio de contraseña: requiere contraseña actual, nueva de 6+ caracteres y confirmación igual.
4. El campo rol está prohibido en la petición (422 si se envía).

**Criterios de aceptación**

- **CA-TRV-104-01 · Editar datos**  
  **Dado** que tengo sesión  
  **Cuando** guardo datos válidos  
  **Entonces** los cambios persisten y se ven igual al consultar desde web y desde móvil
- **CA-TRV-104-02 · Datos inválidos**  
  **Dado** que envío un teléfono o URL de imagen inválidos  
  **Cuando** guardo  
  **Entonces** se informa el campo con error y se conservan los datos anteriores
- **CA-TRV-104-03 · Intento de cambiar rol**  
  **Dado** que envío el campo rol  
  **Cuando** llamo a PATCH /usuarios/me  
  **Entonces** la API responde 422 y el rol no cambia
- **CA-TRV-104-04 · Cambiar contraseña**  
  **Dado** que ingreso la contraseña actual correcta y una nueva confirmada  
  **Cuando** guardo y vuelvo a iniciar sesión  
  **Entonces** la nueva contraseña funciona y la anterior responde 401
- **CA-TRV-104-05 · Contraseña actual incorrecta**  
  **Dado** que la contraseña actual no coincide  
  **Cuando** intento cambiarla  
  **Entonces** se informa 'La contraseña actual no es correcta' y no cambia
- **CA-TRV-104-06 · Error de servidor**  
  **Dado** que la API falla al guardar  
  **Cuando** termina la operación  
  **Entonces** se muestra un error con opción de reintentar y no se anuncia un cambio que no se guardó

**Endpoints involucrados:** `GET /api/auth/me` · `PATCH /api/usuarios/me` · `PUT /api/usuarios/me/password`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-013 | Karate | PATCH /usuarios/me actualiza y GET /auth/me devuelve lo mismo | CA-TRV-104-01 |
| TC-API-014 | Karate | Avatar y teléfono inválidos devuelven 422 | CA-TRV-104-02 |
| TC-API-015 | Karate | Enviar rol devuelve 422 | CA-TRV-104-03 |
| TC-API-016 | Karate | Cambio de contraseña: login con nueva 200 y con anterior 401 | CA-TRV-104-04, CA-TRV-104-05 |
| TC-WEB-006 | Selenium | Editar perfil y ver el nombre actualizado en el menú | CA-TRV-104-01 |
| TC-MOB-006 | Appium | Editar perfil en la app y verificarlo en la pestaña Perfil | CA-TRV-104-01 |
| TC-MOB-007 | Appium | Cambio de contraseña con confirmación distinta deshabilita Guardar | CA-TRV-104-04 |

---

#### TRV-105 · Eliminar mi cuenta conservando el historial

**Como** usuario (viajero o proveedor), **quiero** eliminar mi cuenta cuando ya no la use **para** dejar de tener acceso sin perder el historial de reservas.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-1 Identidad y cuentas |
| Prioridad | Baja |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | perfil, condiciones |
| HU de origen | HU-VIA-002, HU-PRO-002 |

**Contexto.** Borrado lógico (soft delete). Las reservas, importes y decisiones históricas no se borran.

**Reglas de negocio**

1. Se pide confirmación explícita antes de eliminar.
2. Viajero: bloqueado (409) si tiene reservas pendientes o confirmadas con fecha de fin o salida futura.
3. Proveedor: bloqueado (409) si tiene publicaciones activas con reservas activas.
4. Tras eliminar, el token se invalida y el correo no puede volver a iniciar sesión.

**Criterios de aceptación**

- **CA-TRV-105-01 · Desistir**  
  **Dado** que pulsé 'Eliminar cuenta'  
  **Cuando** cancelo en el diálogo de confirmación  
  **Entonces** no cambia nada
- **CA-TRV-105-02 · Eliminación permitida**  
  **Dado** que no tengo reservas activas  
  **Cuando** confirmo la eliminación  
  **Entonces** se cierra mi sesión y no puedo volver a ingresar con esa cuenta (401)
- **CA-TRV-105-03 · Bloqueo por reservas activas**  
  **Dado** que tengo una reserva confirmada futura  
  **Cuando** intento eliminar  
  **Entonces** la API responde 409 'Tienes reservas activas' y la cuenta sigue activa
- **CA-TRV-105-04 · Historial conservado**  
  **Dado** que mi cuenta fue eliminada  
  **Cuando** el proveedor consulta sus reservas  
  **Entonces** las reservas siguen con su código, importe y estado

**Endpoints involucrados:** `DELETE /api/usuarios/me`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-017 | Karate | DELETE con reserva activa devuelve 409 | CA-TRV-105-03 |
| TC-API-018 | Karate | DELETE sin reservas devuelve 200 y login posterior 401 | CA-TRV-105-02 |
| TC-API-019 | Karate | Reservas de la cuenta eliminada siguen visibles para el proveedor | CA-TRV-105-04 |
| TC-WEB-007 | Selenium | Diálogo de confirmación: cancelar conserva la cuenta | CA-TRV-105-01 |
| TC-MOB-008 | Appium | Eliminar cuenta desde Perfil y volver a la pantalla de login | CA-TRV-105-02 |

---

### Épica TRV-2 — Hoteles y aprobación de reservas

_Búsqueda, cotización con IGV, solicitud con pago simulado y decisión del proveedor._

#### TRV-201 · Buscar hoteles por destino, fechas y huéspedes

**Como** viajero, **quiero** buscar hoteles por ciudad, fechas, huéspedes y habitaciones **para** ver solo opciones con disponibilidad real.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-2 Hoteles y aprobación de reservas |
| Prioridad | Más alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | catalogo, hoteles, busqueda, rendimiento |
| HU de origen | HU-VIA-003 |

**Contexto.** La búsqueda es pública (no requiere sesión). Es el endpoint con más tráfico: objetivo de rendimiento definido.

**Reglas de negocio**

1. Ciudad: coincidencia sin distinguir mayúsculas ni tildes.
2. check_in >= hoy y check_out > check_in; máximo 30 noches.
3. Adultos 1–10, niños 0–10, habitaciones 1–10, todos enteros.
4. Un hotel aparece si tiene al menos un tipo de habitación con capacidad suficiente y unidades libres >= habitaciones pedidas en todo el rango.
5. Ocupan unidades las reservas en estado pendiente o confirmada que se cruzan con el rango.
6. Resultados paginados (12 por página) y ordenables por precio o estrellas.

**Criterios de aceptación**

- **CA-TRV-201-01 · Búsqueda válida**  
  **Dado** que ingreso Cusco, 2 noches futuras, 2 adultos y 1 habitación  
  **Cuando** busco  
  **Entonces** solo veo hoteles de Cusco con habitaciones de capacidad >= 2 y unidades libres en esas fechas, con precio 'desde' por noche
- **CA-TRV-201-02 · Fechas inválidas**  
  **Dado** que el check-in es pasado o el check-out es igual o anterior  
  **Cuando** intento buscar  
  **Entonces** se indica qué corregir, el botón no continúa y la API responde 422 si se llama directo
- **CA-TRV-201-03 · Cantidades inválidas**  
  **Dado** que pido 0 adultos, 11 habitaciones o valores no enteros  
  **Cuando** intento buscar  
  **Entonces** se informa el rango permitido y no se ejecuta la búsqueda
- **CA-TRV-201-04 · Sin resultados vs error**  
  **Dado** que no hay hoteles disponibles o la API falla  
  **Cuando** termina la búsqueda  
  **Entonces** distingo 'No encontramos hoteles' de 'No pudimos cargar los resultados' con botón Reintentar
- **CA-TRV-201-05 · Conservar búsqueda**  
  **Dado** que abro un resultado  
  **Cuando** vuelvo atrás  
  **Entonces** conservo los filtros y la página donde estaba

**Endpoints involucrados:** `GET /api/hoteles?ciudad&check_in&check_out&adultos&ninos&habitaciones&page&sort`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-020 | Karate | Búsqueda válida: todos los resultados cumplen ciudad, capacidad y unidades (match each) | CA-TRV-201-01 |
| TC-API-021 | Karate | Data-driven de fechas y cantidades inválidas devuelve 422 | CA-TRV-201-02, CA-TRV-201-03 |
| TC-API-022 | Karate | Hotel con unidades ocupadas por reservas pendientes no aparece | CA-TRV-201-01 |
| TC-PERF-002 | JMeter | Carga de catálogo: 100 usuarios, ramp-up 60 s, ciudades desde CSV, p95 < 1 s | CA-TRV-201-01 |
| TC-WEB-008 | Selenium | Búsqueda en Cusco y validación de tarjetas de resultado | CA-TRV-201-01 |
| TC-WEB-009 | Selenium | Validación de fechas pasadas en el buscador | CA-TRV-201-02 |
| TC-MOB-009 | Appium | Búsqueda por ciudad y fechas en la app | CA-TRV-201-01 |
| TC-MOB-010 | Appium | Estado vacío vs error con Reintentar | CA-TRV-201-04 |

---

#### TRV-202 · Ver detalle del hotel y cotizar habitaciones con IGV

**Como** viajero, **quiero** ver el hotel y elegir tipos y cantidades de habitación **para** conocer el importe exacto antes de reservar.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-2 Hoteles y aprobación de reservas |
| Prioridad | Alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | hoteles, cotizacion, calculo |
| HU de origen | HU-VIA-003 |

**Contexto.** La cotización la calcula el backend para que web, móvil y lo guardado coincidan al céntimo.

**Reglas de negocio**

1. Detalle: ubicación, estrellas, galería, descripción, tipos de habitación (capacidad, tarifa por noche, unidades disponibles) y reseñas con promedio.
2. Por tipo: subtotal = tarifa_por_noche × noches × unidades.
3. Subtotal general = suma de tipos; IGV = 18% del subtotal; total = subtotal + IGV; todo a 2 decimales (half-up).
4. Unidades por tipo: enteros de 1 a las unidades disponibles en el rango.
5. Cambiar fechas o cantidades recalcula la cotización; cotizar no reserva ni bloquea unidades.

**Criterios de aceptación**

- **CA-TRV-202-01 · Detalle completo**  
  **Dado** que abro un hotel desde la búsqueda  
  **Cuando** carga el detalle  
  **Entonces** veo ubicación, tipos con capacidad, tarifa, unidades disponibles para mis fechas y opiniones
- **CA-TRV-202-02 · Selección múltiple**  
  **Dado** que elijo 2 unidades del tipo Doble y 1 del tipo Suite  
  **Cuando** reviso el resumen  
  **Entonces** el resumen lista cada tipo con unidades, fechas y noches
- **CA-TRV-202-03 · Cálculo con IGV**  
  **Dado** que una Doble cuesta S/ 150.00, 2 noches, 2 unidades  
  **Cuando** veo el importe  
  **Entonces** subtotal S/ 600.00, IGV S/ 108.00 y total S/ 708.00, igual en web, móvil y en la respuesta de /cotizar
- **CA-TRV-202-04 · Cantidad excedida**  
  **Dado** que un tipo tiene 3 unidades libres  
  **Cuando** intento elegir 4  
  **Entonces** el selector no lo permite y /cotizar responde 422 indicando el máximo
- **CA-TRV-202-05 · Recalcular sin reservar**  
  **Dado** que cambio la fecha de salida  
  **Cuando** se actualiza la cotización  
  **Entonces** el total cambia y no se crea ninguna reserva

**Endpoints involucrados:** `GET /api/hoteles/{id}` · `GET /api/hoteles/{id}/disponibilidad` · `POST /api/reservas-habitaciones/cotizar`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-023 | Karate | Esquema JSON del detalle de hotel (habitaciones, calificación, reseñas) | CA-TRV-202-01 |
| TC-API-024 | Karate | Cotización data-driven (CSV) con tarifas, noches y unidades → subtotal, igv, total exactos | CA-TRV-202-03 |
| TC-API-025 | Karate | Cotizar más unidades que las libres devuelve 422 | CA-TRV-202-04 |
| TC-WEB-010 | Selenium | Selección de 2 tipos y verificación del resumen con IGV | CA-TRV-202-02, CA-TRV-202-03 |
| TC-MOB-011 | Appium | Selección de habitaciones y total con IGV en la app | CA-TRV-202-02, CA-TRV-202-03 |
| TC-MOB-012 | Appium | Botón + deshabilitado al llegar a las unidades disponibles | CA-TRV-202-04 |

---

#### TRV-203 · Solicitar reserva de hotel con pago simulado

**Como** viajero, **quiero** enviar mi selección con un pago simulado **para** registrar una solicitud que el proveedor aprobará.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-2 Hoteles y aprobación de reservas |
| Prioridad | Más alta |
| Story points | 8 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | reservas, hoteles, pago-simulado, concurrencia, idempotencia, aprobacion |
| HU de origen | HU-VIA-004 |

**Contexto.** Inicio del flujo de aprobación: la reserva nace PENDIENTE y ocupa unidades hasta que el proveedor decida (TRV-204).

**Reglas de negocio**

1. Requiere sesión de viajero (proveedor → 403).
2. Pago simulado: titular (2–100 letras), número de 16 dígitos que pase Luhn, vencimiento MM/AA no vencido, CVC de 3 dígitos. No se guardan datos de tarjeta, solo los 4 últimos dígitos.
3. Una solicitud puede incluir varios tipos de habitación; se registra en una transacción: todos o ninguno.
4. Cada tipo genera una reserva con código único de 10 caracteres agrupadas por un código de solicitud.
5. Se guardan precio por noche, subtotal, IGV y total vigentes al momento de reservar.
6. Las unidades se verifican con bloqueo de fila: dos solicitudes simultáneas nunca superan las unidades.
7. Header Idempotency-Key obligatorio: repetir el envío con el mismo key devuelve la misma solicitud (200) sin duplicar.

**Criterios de aceptación**

- **CA-TRV-203-01 · Solicitud registrada**  
  **Dado** que tengo sesión, selección válida y datos de pago válidos  
  **Cuando** confirmo el pago  
  **Entonces** se registra la solicitud con código, estado PENDIENTE y el mismo total mostrado, y veo el comprobante
- **CA-TRV-203-02 · Pago inválido**  
  **Dado** que el número no tiene 16 dígitos, la tarjeta está vencida o el CVC no tiene 3 dígitos  
  **Cuando** intento pagar  
  **Entonces** se señala el campo, la API responde 422 y no se crea reserva; cerrar el formulario conserva la selección
- **CA-TRV-203-03 · Unidades ocupadas**  
  **Dado** que mi solicitud quedó pendiente  
  **Cuando** otro viajero consulta las mismas fechas  
  **Entonces** las unidades de mi solicitud ya no figuran como disponibles
- **CA-TRV-203-04 · Disponibilidad insuficiente**  
  **Dado** que otro viajero tomó las últimas unidades mientras pagaba  
  **Cuando** envío  
  **Entonces** la API responde 409 indicando qué tipo ya no tiene unidades y no se registra ningún tipo
- **CA-TRV-203-05 · Concurrencia**  
  **Dado** que 20 viajeros envían a la vez por la última unidad  
  **Cuando** se procesan  
  **Entonces** solo una solicitud se registra y las demás reciben 409
- **CA-TRV-203-06 · Envío repetido**  
  **Dado** que la respuesta no llegó y reintento con la misma Idempotency-Key  
  **Cuando** reenvío  
  **Entonces** recibo la misma solicitud ya registrada sin duplicados

**Endpoints involucrados:** `POST /api/reservas-habitaciones (header Idempotency-Key)`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-026 | Karate | Reserva válida 201: estado pendiente, código único, subtotal/igv/total = cotización | CA-TRV-203-01 |
| TC-API-027 | Karate | Data-driven de tarjetas inválidas (Luhn, vencida, CVC) → 422 sin reserva creada | CA-TRV-203-02 |
| TC-API-028 | Karate | Disponibilidad tras reservar descuenta las unidades | CA-TRV-203-03 |
| TC-API-029 | Karate | Varios tipos con uno sin stock → 409 y ninguno registrado | CA-TRV-203-04 |
| TC-API-030 | Karate | Mismo Idempotency-Key devuelve la misma solicitud | CA-TRV-203-06 |
| TC-PERF-003 | JMeter | 20 hilos simultáneos (Synchronizing Timer) por la última unidad: exactamente 1×201 y 19×409 | CA-TRV-203-05 |
| TC-WEB-011 | Selenium | Flujo completo búsqueda → selección → pago → comprobante PENDIENTE | CA-TRV-203-01 |
| TC-WEB-012 | Selenium | Validaciones del formulario de pago | CA-TRV-203-02 |
| TC-MOB-013 | Appium | Flujo completo de reserva de hotel en la app | CA-TRV-203-01 |
| TC-MOB-014 | Appium | Formulario de pago rechaza tarjeta vencida | CA-TRV-203-02 |

---

#### TRV-204 · Aprobar o rechazar solicitudes de hotel

**Como** proveedor, **quiero** aceptar o rechazar las solicitudes pendientes de mis hoteles **para** confirmar solo las estadías que puedo atender.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-2 Hoteles y aprobación de reservas |
| Prioridad | Más alta |
| Story points | 8 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | aprobacion, proveedor, reservas, estados |
| HU de origen | HU-PRO-009 |

**Contexto.** Núcleo del flujo de aprobación. Máquina de estados: pendiente → confirmada | rechazada | cancelada; confirmada → cancelada.

**Reglas de negocio**

1. Solo el proveedor dueño del hotel puede decidir (otro → 403).
2. Solo se decide sobre reservas en estado pendiente; otro estado → 409 con el estado vigente.
3. Aceptar: pasa a confirmada, conserva datos e importe y no vuelve a ocupar unidades.
4. Rechazar: motivo obligatorio (10–300 caracteres); pasa a rechazada y libera sus unidades una sola vez.
5. Se registra quién decidió y cuándo (decidido_por, decidido_en).
6. La decisión usa bloqueo de fila: dos decisiones simultáneas no se pisan.

**Criterios de aceptación**

- **CA-TRV-204-01 · Aceptar**  
  **Dado** que tengo una solicitud pendiente de mi hotel  
  **Cuando** la acepto  
  **Entonces** la misma reserva queda CONFIRMADA con su importe y el viajero la ve confirmada en web y móvil
- **CA-TRV-204-02 · Rechazar**  
  **Dado** que tengo una solicitud pendiente  
  **Cuando** la rechazo con un motivo  
  **Entonces** queda RECHAZADA, sus unidades vuelven a estar disponibles y el viajero ve el motivo
- **CA-TRV-204-03 · Sin decisión**  
  **Dado** que no decido sobre una solicitud  
  **Cuando** actualizo la lista  
  **Entonces** sigue PENDIENTE
- **CA-TRV-204-04 · Decisión repetida o estado incompatible**  
  **Dado** que la reserva ya fue aceptada, rechazada o cancelada por el viajero  
  **Cuando** intento decidir  
  **Entonces** la API responde 409 con el estado actual y no cambia nada
- **CA-TRV-204-05 · Reserva ajena**  
  **Dado** que la reserva pertenece a otro proveedor  
  **Cuando** intento decidir  
  **Entonces** la API responde 403
- **CA-TRV-204-06 · Tours sin aprobación**  
  **Dado** que es una reserva de tour  
  **Cuando** la consulto  
  **Entonces** aparece confirmada automáticamente y no tiene acciones de aceptar o rechazar

**Endpoints involucrados:** `POST /api/proveedor/reservas-habitaciones/{id}/aceptar` · `POST /api/proveedor/reservas-habitaciones/{id}/rechazar {motivo}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-031 | Karate | Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | CA-TRV-204-01 |
| TC-API-032 | Karate | Rechazo libera unidades: disponibilidad antes y después | CA-TRV-204-02 |
| TC-API-033 | Karate | Aceptar dos veces → 409; aceptar una cancelada → 409 | CA-TRV-204-04 |
| TC-API-034 | Karate | Proveedor B sobre reserva de proveedor A → 403 | CA-TRV-204-05 |
| TC-API-035 | Karate | Rechazo sin motivo → 422 | CA-TRV-204-02 |
| TC-WEB-013 | Selenium | Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | CA-TRV-204-01 |
| TC-WEB-014 | Selenium | Proveedor rechaza con motivo y el viajero ve RECHAZADA | CA-TRV-204-02 |
| TC-MOB-015 | Appium | Proveedor acepta una solicitud desde la app | CA-TRV-204-01 |
| TC-MOB-016 | Appium | Proveedor rechaza con motivo desde la app | CA-TRV-204-02 |

---

### Épica TRV-3 — Tours y salidas

_Búsqueda de tours, selección de salida y reserva con confirmación automática._

#### TRV-301 · Buscar tours por destino, categoría y fechas

**Como** viajero, **quiero** encontrar tours por ciudad, categoría y fechas **para** ver actividades con cupos disponibles.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-3 Tours y salidas |
| Prioridad | Alta |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | catalogo, tours, busqueda |
| HU de origen | HU-VIA-006 |

**Contexto.** Categorías permitidas: Aventura, Gastronomía, Cultura, Relajación.

**Reglas de negocio**

1. Destino: coincide con ciudad o país, sin distinguir mayúsculas ni tildes.
2. Fechas: desde >= hoy, hasta >= desde.
3. Un tour aparece si tiene al menos una salida programada en el rango con cupos libres >= participantes pedidos.
4. Resultados paginados (12 por página).

**Criterios de aceptación**

- **CA-TRV-301-01 · Búsqueda válida**  
  **Dado** que busco 'Cusco', categoría Aventura y un rango de fechas futuro  
  **Cuando** busco  
  **Entonces** solo veo tours de Cusco, de Aventura y con salidas con cupo en ese rango
- **CA-TRV-301-02 · Filtros inválidos**  
  **Dado** que el rango de fechas es pasado o la categoría no existe  
  **Cuando** busco  
  **Entonces** se informa el error (422 en la API) y no se muestran resultados
- **CA-TRV-301-03 · Sin resultados o error**  
  **Dado** que no hay tours o la API falla  
  **Cuando** termina la búsqueda  
  **Entonces** distingo ambos casos y puedo cambiar filtros o reintentar
- **CA-TRV-301-04 · Conservar búsqueda**  
  **Dado** que abro un tour  
  **Cuando** vuelvo  
  **Entonces** conservo los filtros

**Endpoints involucrados:** `GET /api/tours?destino&categoria&checkIn&checkOut&cupos&page`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-036 | Karate | Filtros combinados: todos los resultados cumplen destino y categoría (match each) | CA-TRV-301-01 |
| TC-API-037 | Karate | Data-driven de filtros inválidos → 422 | CA-TRV-301-02 |
| TC-PERF-004 | JMeter | Carga de búsqueda de tours: 100 usuarios, categorías desde CSV, p95 < 1 s | CA-TRV-301-01 |
| TC-WEB-015 | Selenium | Filtrar por categoría y destino | CA-TRV-301-01 |
| TC-MOB-017 | Appium | Chips de categoría y búsqueda por destino | CA-TRV-301-01 |

---

#### TRV-302 · Ver detalle del tour y elegir salida y participantes

**Como** viajero, **quiero** revisar el tour y elegir una salida y la cantidad de personas **para** saber el importe antes de reservar.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-3 Tours y salidas |
| Prioridad | Alta |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | tours, cotizacion, calculo |
| HU de origen | HU-VIA-006 |

**Contexto.** El detalle muestra solo salidas futuras programadas.

**Reglas de negocio**

1. Detalle: ubicación, categoría, duración, precio por persona, qué incluye, actividades en orden, reseñas y salidas futuras con cupos libres.
2. Participantes: entero de 1 al cupo libre de la salida.
3. Subtotal = precio × participantes; IGV 18%; total = subtotal + IGV; 2 decimales.
4. Salidas pasadas, cerradas, canceladas o sin cupo no se pueden elegir.

**Criterios de aceptación**

- **CA-TRV-302-01 · Detalle**  
  **Dado** que abro un tour  
  **Cuando** carga  
  **Entonces** veo ubicación, categoría, duración, precio por persona, actividades, opiniones y salidas futuras con cupos
- **CA-TRV-302-02 · Cálculo**  
  **Dado** que el precio es S/ 120.00 y elijo 3 personas  
  **Cuando** veo el resumen  
  **Entonces** subtotal S/ 360.00, IGV S/ 64.80 y total S/ 424.80
- **CA-TRV-302-03 · Selección inválida**  
  **Dado** que elijo 0 personas, más que el cupo o una salida agotada  
  **Cuando** intento continuar  
  **Entonces** no se permite y se indica el motivo
- **CA-TRV-302-04 · Sin salidas**  
  **Dado** que el tour no tiene salidas futuras  
  **Cuando** lo consulto  
  **Entonces** veo 'No hay salidas programadas' y no puedo reservar

**Endpoints involucrados:** `GET /api/tours/{id}` · `POST /api/tours/salidas/{salida}/cotizar`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-038 | Karate | Esquema del detalle; ninguna salida devuelta es pasada ni no programada | CA-TRV-302-01 |
| TC-API-039 | Karate | Cotización de tour data-driven (precio × personas + 18%) | CA-TRV-302-02 |
| TC-API-040 | Karate | Cotizar más personas que el cupo → 422 | CA-TRV-302-03 |
| TC-WEB-016 | Selenium | Elegir salida y personas, verificar total con IGV | CA-TRV-302-02 |
| TC-MOB-018 | Appium | Elegir salida y personas en la app; + se deshabilita al tope de cupos | CA-TRV-302-02, CA-TRV-302-03 |

---

#### TRV-303 · Reservar tour con confirmación automática

**Como** viajero, **quiero** reservar cupos de una salida con pago simulado **para** asegurar mi lugar de inmediato.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-3 Tours y salidas |
| Prioridad | Más alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | reservas, tours, pago-simulado, concurrencia, idempotencia |
| HU de origen | HU-VIA-007 |

**Contexto.** A diferencia del hotel, el tour no requiere aprobación: se confirma y descuenta cupos en la misma transacción.

**Reglas de negocio**

1. Requiere sesión de viajero. Mismas reglas de pago simulado que TRV-203.
2. Valida en el servidor: salida existente, programada, fecha futura y cupo libre >= personas.
3. Bloqueo de fila sobre la salida: los cupos nunca quedan negativos ni se sobrevenden.
4. Guarda precio unitario, subtotal, IGV y total; estado CONFIRMADA.
5. Idempotency-Key obligatorio, igual que en hoteles.

**Criterios de aceptación**

- **CA-TRV-303-01 · Reserva confirmada**  
  **Dado** que tengo sesión y elijo una salida con cupo  
  **Cuando** pago  
  **Entonces** se registra con código único, estado CONFIRMADA, el total mostrado, y el cupo libre baja exactamente en mis participantes
- **CA-TRV-303-02 · Pago inválido o desistir**  
  **Dado** que el pago es inválido o cierro el formulario  
  **Cuando** no completo el envío  
  **Entonces** no se crea la reserva y los cupos no cambian
- **CA-TRV-303-03 · Salida no válida**  
  **Dado** que la salida está cerrada, cancelada, es pasada o no tiene cupo suficiente  
  **Cuando** envío  
  **Entonces** la API responde 409/422 con el motivo y los cupos no cambian
- **CA-TRV-303-04 · Concurrencia**  
  **Dado** que 30 viajeros reservan a la vez una salida con 10 cupos  
  **Cuando** se procesan  
  **Entonces** nunca se reservan más de 10 cupos en total
- **CA-TRV-303-05 · Envío repetido**  
  **Dado** que reintento con la misma Idempotency-Key  
  **Cuando** reenvío  
  **Entonces** obtengo la misma reserva sin descontar cupos otra vez

**Endpoints involucrados:** `POST /api/tours/salidas/{salida}/reservas (header Idempotency-Key)`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-041 | Karate | Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | CA-TRV-303-01 |
| TC-API-042 | Karate | Salida cerrada / pasada / sin cupo → 409 o 422 y cupos sin cambio | CA-TRV-303-03 |
| TC-API-043 | Karate | Idempotencia: dos envíos, una sola reserva y un solo descuento | CA-TRV-303-05 |
| TC-PERF-005 | JMeter | 30 hilos simultáneos sobre salida de 10 cupos: suma de personas confirmadas <= 10 | CA-TRV-303-04 |
| TC-WEB-017 | Selenium | Reserva de tour end-to-end con comprobante CONFIRMADA | CA-TRV-303-01 |
| TC-MOB-019 | Appium | Reserva de tour end-to-end en la app | CA-TRV-303-01 |

---

### Épica TRV-4 — Postventa del viajero

_Mis reservas, cancelación, reseñas e itinerario sugerido._

#### TRV-401 · Consultar mis reservas y sus estados

**Como** viajero, **quiero** ver mis reservas de hotel y tour con su estado vigente **para** saber si fueron confirmadas, rechazadas o canceladas.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-4 Postventa del viajero |
| Prioridad | Alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | reservas, estados, paginacion |
| HU de origen | HU-VIA-005 |

**Contexto.** La fuente de verdad es la API; ningún canal guarda reservas localmente.

**Reglas de negocio**

1. Lista paginada (10 por página), más reciente primero, con filtro por estado.
2. Cada reserva muestra: código, servicio, habitación o salida, fechas, cantidades, subtotal, IGV, total y estado.
3. Estados de hotel: Pendiente, Confirmada, Rechazada (con motivo), Cancelada. Tour: Confirmada, Cancelada.
4. Si la salida del tour fue cerrada o cancelada por el proveedor se muestra como aviso aparte, sin cambiar el estado de la reserva.
5. Una reserva ajena responde 404 (no se revela su existencia).

**Criterios de aceptación**

- **CA-TRV-401-01 · Listado propio**  
  **Dado** que tengo reservas  
  **Cuando** abro Mis reservas  
  **Entonces** veo solo las mías con todos sus datos y estado persistido
- **CA-TRV-401-02 · Pendiente y aceptada**  
  **Dado** que el proveedor aún no decide o ya aceptó  
  **Cuando** actualizo  
  **Entonces** la solicitud figura PENDIENTE o CONFIRMADA según corresponda, con los mismos datos
- **CA-TRV-401-03 · Rechazada**  
  **Dado** que el proveedor rechazó mi solicitud  
  **Cuando** actualizo  
  **Entonces** figura RECHAZADA con el motivo, distinta de una cancelación mía, y puedo iniciar otra búsqueda
- **CA-TRV-401-04 · Tour con salida cancelada**  
  **Dado** que el proveedor canceló la salida  
  **Cuando** consulto  
  **Entonces** veo el aviso 'Salida cancelada por el proveedor' sin que mi reserva aparezca como cancelada por mí
- **CA-TRV-401-05 · Consistencia entre canales**  
  **Dado** que reservé en la web  
  **Cuando** abro la app  
  **Entonces** veo los mismos códigos, estados e importes y puedo recorrer todas las páginas
- **CA-TRV-401-06 · Vacío, error y ajeno**  
  **Dado** que no tengo reservas, la API falla o pido una reserva de otro  
  **Cuando** accedo  
  **Entonces** distingo 'Aún no tienes reservas' de un error, y la ajena responde 404

**Endpoints involucrados:** `GET /api/mis-reservas?estado&page` · `GET /api/mis-reservas/{id}` · `GET /api/tours/mis-reservas?page`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-044 | Karate | Esquema paginado (data, meta.total, meta.last_page) y solo reservas propias | CA-TRV-401-01, CA-TRV-401-05 |
| TC-API-045 | Karate | Estado tras aceptar y tras rechazar (con motivo) | CA-TRV-401-02, CA-TRV-401-03 |
| TC-API-046 | Karate | Reserva ajena → 404 | CA-TRV-401-06 |
| TC-WEB-018 | Selenium | Mis reservas: pestañas hotel/tour, filtro por estado y paginación | CA-TRV-401-01, CA-TRV-401-05 |
| TC-WEB-019 | Selenium | Reserva rechazada muestra motivo | CA-TRV-401-03 |
| TC-MOB-020 | Appium | Mis reservas en la app coincide con la web (mismo código y total) | CA-TRV-401-05 |
| TC-MOB-021 | Appium | Scroll infinito / paginación de reservas | CA-TRV-401-05 |

---

#### TRV-402 · Cancelar mis reservas

**Como** viajero, **quiero** cancelar una reserva propia **para** liberar la oferta que no usaré.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-4 Postventa del viajero |
| Prioridad | Alta |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | reservas, cancelacion, estados |
| HU de origen | HU-VIA-008 |

**Contexto.** Sin plazos ni reembolsos reales (fuera de alcance).

**Reglas de negocio**

1. Cancelables: hotel pendiente o confirmada; tour confirmada; siempre que la estadía o salida no haya empezado.
2. Se pide confirmación antes de ejecutar.
3. Hotel: libera sus unidades. Tour: devuelve exactamente sus participantes al cupo (bloqueo de fila).
4. Rechazada o ya cancelada → 409 sin volver a liberar.
5. Se conservan código, fechas, cantidades e importes.

**Criterios de aceptación**

- **CA-TRV-402-01 · Confirmar o desistir**  
  **Dado** que pulso Cancelar  
  **Cuando** elijo No en el diálogo  
  **Entonces** la reserva no cambia
- **CA-TRV-402-02 · Cancelación exitosa**  
  **Dado** que confirmo  
  **Cuando** termina  
  **Entonces** la reserva figura CANCELADA en web y móvil con su historial intacto
- **CA-TRV-402-03 · Liberar oferta una vez**  
  **Dado** que mi reserva de tour tenía 3 personas  
  **Cuando** la cancelo  
  **Entonces** el cupo libre de la salida sube exactamente 3
- **CA-TRV-402-04 · No cancelable**  
  **Dado** que la reserva es ajena, ya está cancelada o fue rechazada  
  **Cuando** intento cancelar  
  **Entonces** la API responde 404 o 409 y la oferta no se libera de nuevo
- **CA-TRV-402-05 · Respuesta incierta**  
  **Dado** que se cortó la conexión al cancelar  
  **Cuando** refresco  
  **Entonces** veo el estado real guardado y no un mensaje de éxito falso

**Endpoints involucrados:** `POST /api/reservas-habitaciones/{id}/cancelar` · `POST /api/tours/reservas/{id}/cancelar`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-047 | Karate | Cancelar hotel: estado cancelada y unidades liberadas | CA-TRV-402-02 |
| TC-API-048 | Karate | Cancelar tour devuelve exactamente las personas al cupo | CA-TRV-402-03 |
| TC-API-049 | Karate | Cancelar dos veces → 409; cancelar rechazada → 409; ajena → 404 | CA-TRV-402-04 |
| TC-WEB-020 | Selenium | Cancelar desde Mis reservas con diálogo de confirmación | CA-TRV-402-01, CA-TRV-402-02 |
| TC-MOB-022 | Appium | Cancelar desde la app y ver estado CANCELADA | CA-TRV-402-02 |

---

#### TRV-403 · Publicar, editar y eliminar reseñas

**Como** viajero, **quiero** calificar y comentar los servicios que usé **para** compartir mi experiencia y consultar la de otros.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-4 Postventa del viajero |
| Prioridad | Media |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | resenas, condiciones |
| HU de origen | HU-VIA-010 |

**Contexto.** Solo opina quien realmente usó el servicio.

**Reglas de negocio**

1. Lectura pública y paginada (10 por página) con promedio y cantidad del total, no de la página.
2. Publicar requiere una reserva propia CONFIRMADA cuya estadía terminó (hotel) o cuya salida ya pasó (tour).
3. Calificación entera 1–5; comentario de 10 a 300 caracteres.
4. Una reseña por usuario y servicio (la segunda → 409).
5. Solo el autor edita o elimina; eliminar pide confirmación.

**Criterios de aceptación**

- **CA-TRV-403-01 · Consultar**  
  **Dado** que abro un servicio  
  **Cuando** veo opiniones  
  **Entonces** son solo de ese servicio, puedo ver todas las páginas y el promedio corresponde al total
- **CA-TRV-403-02 · Publicar**  
  **Dado** que tengo una reserva confirmada y finalizada  
  **Cuando** envío 4 estrellas y un comentario de 50 caracteres  
  **Entonces** se publica a mi nombre y el promedio se recalcula
- **CA-TRV-403-03 · No permitido**  
  **Dado** que no tengo reserva finalizada o el comentario tiene 5 caracteres  
  **Cuando** intento publicar  
  **Entonces** se informa la causa exacta (403 'Debes haber completado una reserva' o 422) y no se publica
- **CA-TRV-403-04 · Editar y eliminar**  
  **Dado** que es mi reseña  
  **Cuando** la edito o la elimino tras confirmar  
  **Entonces** se actualiza la misma reseña o desaparece; no puedo gestionar reseñas ajenas (403)
- **CA-TRV-403-05 · Sin duplicados**  
  **Dado** que reintento la publicación  
  **Cuando** se procesa  
  **Entonces** existe una sola reseña mía para ese servicio

**Endpoints involucrados:** `GET /api/reviews?servicio_id&page` · `GET /api/reviews/resumen?servicio_id` · `POST /api/reviews` · `PUT /api/reviews/{id}` · `DELETE /api/reviews/{id}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-050 | Karate | Resumen: promedio y total coinciden con el conjunto completo | CA-TRV-403-01 |
| TC-API-051 | Karate | Publicar con reserva finalizada 201; sin reserva 403 con mensaje | CA-TRV-403-02, CA-TRV-403-03 |
| TC-API-052 | Karate | Data-driven de longitudes de comentario y calificaciones fuera de rango → 422 | CA-TRV-403-03 |
| TC-API-053 | Karate | Editar/eliminar reseña ajena → 403; segunda reseña → 409 | CA-TRV-403-04, CA-TRV-403-05 |
| TC-WEB-021 | Selenium | Publicar, editar y eliminar reseña en el detalle | CA-TRV-403-02, CA-TRV-403-04 |
| TC-MOB-023 | Appium | Publicar reseña desde la app | CA-TRV-403-02 |

---

#### TRV-404 · Generar un itinerario sugerido para mi estadía

**Como** viajero, **quiero** recibir una propuesta de actividades para mis fechas **para** planificar mi viaje antes de confirmar el hotel.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-4 Postventa del viajero |
| Prioridad | Baja |
| Story points | 5 |
| Componentes | Backend, Microservicio, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | itinerario, microservicio, integracion |
| HU de origen | HU-VIA-009 |

**Contexto.** El backend firma un JWT de servicio y llama al microservicio Python (FastAPI) que arma la propuesta con los tours del destino.

**Reglas de negocio**

1. Entrada: hotel, destino, check-in, check-out (máx. 14 días) e interés (Aventura, Gastronomía, Cultura, Relajación).
2. Salida: días con actividades, duración y costo estimado por persona.
3. Se rotula como 'Sugerencia'; nada se reserva automáticamente.
4. Si el microservicio no responde en 10 s → 503 y se puede continuar solo con el hotel.

**Criterios de aceptación**

- **CA-TRV-404-01 · Propuesta**  
  **Dado** que ingreso datos válidos  
  **Cuando** genero el itinerario  
  **Entonces** veo días, actividades, duraciones y costos estimados
- **CA-TRV-404-02 · Sugerencia, no reserva**  
  **Dado** que reviso la propuesta  
  **Cuando** la miro  
  **Entonces** está rotulada como sugerencia y su costo se muestra separado del total del hotel
- **CA-TRV-404-03 · Continuar**  
  **Dado** que vi o ignoré la propuesta  
  **Cuando** sigo  
  **Entonces** puedo terminar la reserva del hotel; reservar un tour requiere su propio flujo
- **CA-TRV-404-04 · Validación**  
  **Dado** que el interés no existe o las fechas son inválidas  
  **Cuando** genero  
  **Entonces** se informa (422) y no se muestra propuesta
- **CA-TRV-404-05 · Servicio caído**  
  **Dado** que el microservicio falla  
  **Cuando** genero  
  **Entonces** veo 'No pudimos generar el itinerario' con Reintentar y puedo seguir con el hotel

**Endpoints involucrados:** `POST /api/itineraries`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-054 | Karate | Itinerario válido: esquema de días y actividades | CA-TRV-404-01 |
| TC-API-055 | Karate | Interés inválido → 422 | CA-TRV-404-04 |
| TC-WEB-022 | Selenium | Generar itinerario desde el detalle del hotel | CA-TRV-404-01, CA-TRV-404-02 |
| TC-MOB-024 | Appium | Generar itinerario en la app y continuar con la reserva | CA-TRV-404-01, CA-TRV-404-03 |

---

### Épica TRV-5 — Portal del proveedor

_Panel, publicaciones, habitaciones, tours, actividades, salidas y reservas recibidas._

#### TRV-501 · Panel de publicaciones e indicadores

**Como** proveedor, **quiero** ver todas mis publicaciones con sus indicadores **para** decidir qué gestionar.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, indicadores, paginacion |
| HU de origen | HU-PRO-003 |

**Contexto.** Pantalla de inicio del proveedor.

**Reglas de negocio**

1. Lista paginada (15 por página) solo de servicios propios, filtrable por tipo y texto.
2. Indicadores por publicación: reservas pendientes, confirmadas, rechazadas y canceladas (cada estado por separado), ingresos confirmados, promedio y cantidad de reseñas.
3. Indicadores globales: solicitudes pendientes por decidir, ingresos confirmados del mes, ocupación.
4. Los ingresos solo suman reservas confirmadas.

**Criterios de aceptación**

- **CA-TRV-501-01 · Listado propio**  
  **Dado** que tengo 20 publicaciones  
  **Cuando** abro el panel  
  **Entonces** veo solo las mías y puedo ir a la página 2 (registros 16–20)
- **CA-TRV-501-02 · Indicadores por estado**  
  **Dado** que una publicación tiene reservas en varios estados  
  **Cuando** veo sus indicadores  
  **Entonces** cada estado se cuenta por separado y pendientes o canceladas no suman como confirmadas
- **CA-TRV-501-03 · Reseñas**  
  **Dado** que mi publicación tiene 25 reseñas  
  **Cuando** veo su promedio  
  **Entonces** el promedio y la cantidad corresponden a las 25, no a una página
- **CA-TRV-501-04 · Actualización**  
  **Dado** que el viajero reservó o el proveedor aceptó  
  **Cuando** recargo  
  **Entonces** los indicadores se recalculan
- **CA-TRV-501-05 · Vacío y no autorizado**  
  **Dado** que no tengo publicaciones o soy viajero  
  **Cuando** accedo  
  **Entonces** veo 'Aún no tienes publicaciones' o la API responde 403

**Endpoints involucrados:** `GET /api/proveedor/servicios?tipo&search&page` · `GET /api/proveedor/indicadores`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-056 | Karate | Paginación: meta.total, last_page y página 2 con los registros 16+ | CA-TRV-501-01 |
| TC-API-057 | Karate | Indicadores por estado tras crear reservas en 4 estados | CA-TRV-501-02 |
| TC-API-058 | Karate | Viajero en /proveedor/servicios → 403 | CA-TRV-501-05 |
| TC-PERF-006 | JMeter | Panel del proveedor con 30 usuarios concurrentes, p95 < 1,5 s | CA-TRV-501-01 |
| TC-WEB-023 | Selenium | Panel: paginación y tarjetas de indicadores | CA-TRV-501-01, CA-TRV-501-02 |
| TC-MOB-025 | Appium | Panel del proveedor en la app con indicadores | CA-TRV-501-01, CA-TRV-501-02 |

---

#### TRV-502 · Publicar y editar hoteles

**Como** proveedor, **quiero** registrar y actualizar mis hoteles **para** mantener mi oferta de alojamiento.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, hoteles, validaciones |
| HU de origen | HU-PRO-004 |

**Contexto.** Crear un hotel crea el servicio (tipo hotel) y su ficha en una transacción.

**Reglas de negocio**

1. Obligatorios: nombre (máx. 150), dirección, ciudad, país, estrellas 1–5, imagen principal (URL) y al menos un tipo de habitación.
2. Galería opcional de hasta 5 URLs (6 o más → 422, no se recorta en silencio).
3. Editar conserva el id y nunca borra habitaciones con reservas activas (ver TRV-503).
4. Editar no modifica códigos, fechas ni importes de reservas existentes.
5. Solo el dueño edita (otro → 403).

**Criterios de aceptación**

- **CA-TRV-502-01 · Publicar**  
  **Dado** que completo los datos y una habitación  
  **Cuando** publico  
  **Entonces** se crea mi hotel y aparece en el catálogo con los mismos datos e imágenes
- **CA-TRV-502-02 · Galería**  
  **Dado** que agrego 6 imágenes  
  **Cuando** guardo  
  **Entonces** se informa 'Máximo 5 imágenes' y no se guarda
- **CA-TRV-502-03 · Editar**  
  **Dado** que cambio la descripción y una imagen  
  **Cuando** guardo  
  **Entonces** se conserva el id y las reservas no cambian
- **CA-TRV-502-04 · Inválido o ajeno**  
  **Dado** que faltan datos o el hotel es de otro  
  **Cuando** guardo  
  **Entonces** 422 o 403 y no se reemplaza nada
- **CA-TRV-502-05 · Transacción**  
  **Dado** que falla la creación de habitaciones  
  **Cuando** se procesa  
  **Entonces** no queda un hotel a medias ni duplicado

**Endpoints involucrados:** `POST /api/hoteles` · `PUT /api/hoteles/{id}` · `GET /api/hoteles/{id}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-059 | Karate | Crear hotel con habitaciones y verificar en el catálogo | CA-TRV-502-01 |
| TC-API-060 | Karate | 6 imágenes → 422 | CA-TRV-502-02 |
| TC-API-061 | Karate | Editar hotel ajeno → 403 | CA-TRV-502-04 |
| TC-WEB-024 | Selenium | Crear hotel desde el formulario del proveedor | CA-TRV-502-01 |
| TC-MOB-026 | Appium | Crear hotel desde la app del proveedor | CA-TRV-502-01 |

---

#### TRV-503 · Gestionar tipos de habitación

**Como** proveedor, **quiero** agregar, editar y eliminar tipos de habitación **para** administrar capacidades, unidades y tarifas.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Media |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, habitaciones, condiciones |
| HU de origen | HU-PRO-005 |

**Contexto.** Las condiciones protegen las reservas ya comprometidas.

**Reglas de negocio**

1. Campos: nombre único en el hotel, capacidad adultos >= 1, niños >= 0, unidades >= 1, tarifa > 0.
2. No se puede bajar las unidades por debajo del máximo ocupado en cualquier fecha futura (409 indicando el mínimo).
3. No se puede eliminar un tipo con reservas activas (409); sin ellas, borrado lógico tras confirmación.
4. Cambiar la tarifa no altera reservas existentes.

**Criterios de aceptación**

- **CA-TRV-503-01 · Agregar o editar**  
  **Dado** que mi hotel existe  
  **Cuando** guardo un tipo válido  
  **Entonces** queda asociado al hotel y se ve en web y móvil
- **CA-TRV-503-02 · Reducir bajo ocupación**  
  **Dado** que hay 3 unidades ocupadas el 15/12  
  **Cuando** intento dejar 2 unidades  
  **Entonces** la API responde 409 'Mínimo permitido: 3'
- **CA-TRV-503-03 · Eliminar con reservas**  
  **Dado** que el tipo tiene reservas activas  
  **Cuando** intento eliminar  
  **Entonces** se impide (409); sin reservas se elimina tras confirmar
- **CA-TRV-503-04 · Historial**  
  **Dado** que edito la tarifa  
  **Cuando** consulto reservas pasadas  
  **Entonces** conservan su precio original
- **CA-TRV-503-05 · Inválido o ajeno**  
  **Dado** que la tarifa es 0 o el tipo es de otro hotel  
  **Cuando** guardo  
  **Entonces** 422 o 403 sin cambios

**Endpoints involucrados:** `POST /api/hoteles/{id}/habitaciones` · `PUT /api/habitaciones/{id}` · `DELETE /api/habitaciones/{id}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-062 | Karate | Reducir unidades bajo ocupación → 409 con mínimo | CA-TRV-503-02 |
| TC-API-063 | Karate | Eliminar con reservas → 409; sin reservas → 204 | CA-TRV-503-03 |
| TC-API-064 | Karate | Cambio de tarifa no altera total de reservas previas | CA-TRV-503-04 |
| TC-WEB-025 | Selenium | Agregar y editar tipo de habitación | CA-TRV-503-01 |
| TC-MOB-027 | Appium | Agregar tipo de habitación en la app | CA-TRV-503-01 |

---

#### TRV-504 · Publicar y editar tours

**Como** proveedor, **quiero** registrar y actualizar mis tours **para** mantener mi oferta de actividades.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, tours, validaciones |
| HU de origen | HU-PRO-006 |

**Contexto.** Editar los datos generales del tour no toca las salidas: las salidas se gestionan en TRV-506.

**Reglas de negocio**

1. Obligatorios: nombre, descripción, ciudad, país, categoría permitida, duración > 0 (minutos), precio >= 0, imagen principal y al menos una salida futura al crear.
2. Hasta 5 imágenes de galería y hasta 10 elementos 'qué llevar' con nombre.
3. Editar conserva el id y no recrea salidas ni altera cupos reservados ni importes ya cobrados.
4. Solo el dueño edita (otro → 403).

**Criterios de aceptación**

- **CA-TRV-504-01 · Publicar**  
  **Dado** que completo los datos y una salida  
  **Cuando** publico  
  **Entonces** se crea mi tour y aparece en el catálogo
- **CA-TRV-504-02 · Límites**  
  **Dado** que agrego 11 elementos o 6 imágenes  
  **Cuando** guardo  
  **Entonces** 422 con el límite correspondiente
- **CA-TRV-504-03 · Editar sin afectar reservas**  
  **Dado** que el tour tiene reservas  
  **Cuando** cambio el precio  
  **Entonces** las salidas conservan sus ids y las reservas su importe
- **CA-TRV-504-04 · Inválido o ajeno**  
  **Dado** que la categoría no existe o el tour es de otro  
  **Cuando** guardo  
  **Entonces** 422 o 403

**Endpoints involucrados:** `POST /api/tours` · `PUT /api/tours/{id}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-065 | Karate | Crear tour y verificar en catálogo | CA-TRV-504-01 |
| TC-API-066 | Karate | Editar precio: ids de salidas y totales de reservas sin cambio | CA-TRV-504-03 |
| TC-API-067 | Karate | Data-driven de límites (imágenes, items, categoría) → 422 | CA-TRV-504-02, CA-TRV-504-04 |
| TC-WEB-026 | Selenium | Crear tour con salida desde el formulario | CA-TRV-504-01 |
| TC-MOB-028 | Appium | Crear tour desde la app | CA-TRV-504-01 |

---

#### TRV-505 · Gestionar actividades del tour

**Como** proveedor, **quiero** definir las actividades del tour en orden **para** mostrar el programa al viajero.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Baja |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, tours, actividades |
| HU de origen | HU-PRO-007 |

**Contexto.** Las actividades son el programa; los elementos 'qué llevar' son otra lista.

**Reglas de negocio**

1. Campos: título (máx. 150), descripción, orden >= 1 (si falta, se asigna el siguiente), duración en minutos >= 0.
2. El orden no se repite en el tour; insertar en un orden ocupado desplaza a las siguientes.
3. Eliminar pide confirmación y no afecta salidas ni reservas.

**Criterios de aceptación**

- **CA-TRV-505-01 · Consultar**  
  **Dado** que abro mi tour  
  **Cuando** veo actividades  
  **Entonces** aparecen en su orden, separadas de 'qué llevar'
- **CA-TRV-505-02 · Agregar o editar**  
  **Dado** que guardo una actividad válida sin orden  
  **Cuando** se registra  
  **Entonces** queda al final con el siguiente número
- **CA-TRV-505-03 · Eliminar**  
  **Dado** que confirmo eliminar  
  **Cuando** termina  
  **Entonces** deja de aparecer sin tocar salidas ni reservas
- **CA-TRV-505-04 · Inválido o ajeno**  
  **Dado** que el título está vacío o el tour es de otro  
  **Cuando** guardo  
  **Entonces** 422 o 403

**Endpoints involucrados:** `GET /api/tours/{id}/actividades` · `POST /api/tours/{id}/actividades` · `PUT /api/tours/{id}/actividades/{a}` · `DELETE /api/tours/{id}/actividades/{a}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-068 | Karate | Dos actividades sin orden reciben 1 y 2 | CA-TRV-505-02 |
| TC-API-069 | Karate | Actividad en tour ajeno → 403 | CA-TRV-505-04 |
| TC-WEB-027 | Selenium | Agregar, reordenar y eliminar actividades | CA-TRV-505-01, CA-TRV-505-02, CA-TRV-505-03 |
| TC-MOB-029 | Appium | Agregar actividad desde la app | CA-TRV-505-02 |

---

#### TRV-506 · Gestionar salidas y cupos

**Como** proveedor, **quiero** administrar fechas, horas, estados y cupos de mis salidas **para** controlar la oferta de cada tour.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, salidas, cupos, condiciones |
| HU de origen | HU-PRO-008 |

**Contexto.** Estados de salida: programada, cerrada (no admite nuevas reservas) y cancelada.

**Reglas de negocio**

1. Crear: fecha >= hoy, hora HH:MM, cupo total >= 1; inicia programada con 0 reservados.
2. No se repite fecha + hora en el mismo tour (409).
3. cupo_reservado solo lo cambia el sistema; cupo_total nunca puede quedar por debajo de lo reservado.
4. Cerrar o cancelar impide nuevas reservas y conserva las existentes.
5. No se elimina una salida con reservas confirmadas (409).

**Criterios de aceptación**

- **CA-TRV-506-01 · Crear**  
  **Dado** que mi tour existe  
  **Cuando** creo una salida válida  
  **Entonces** se registra programada con 0 reservados
- **CA-TRV-506-02 · Duplicado o fecha pasada**  
  **Dado** que ya existe esa fecha y hora o la fecha es pasada  
  **Cuando** creo  
  **Entonces** 409 o 422 sin duplicar
- **CA-TRV-506-03 · Editar con reservas**  
  **Dado** que la salida tiene 6 reservados  
  **Cuando** pongo cupo total 5  
  **Entonces** 422 'No menor a lo reservado (6)'
- **CA-TRV-506-04 · Cerrar o cancelar**  
  **Dado** que cierro la salida  
  **Cuando** un viajero intenta reservarla  
  **Entonces** recibe 409 y las reservas previas siguen
- **CA-TRV-506-05 · Eliminar**  
  **Dado** que tiene reservas confirmadas  
  **Cuando** intento eliminar  
  **Entonces** 409; sin reservas se elimina tras confirmar
- **CA-TRV-506-06 · Consistencia**  
  **Dado** que guardé un cambio  
  **Cuando** consulto en web y móvil  
  **Entonces** veo los mismos estados y cupos

**Endpoints involucrados:** `GET/POST /api/tours/{id}/salidas` · `PUT/DELETE /api/tours/{id}/salidas/{s}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-070 | Karate | Salida duplicada → 409; fecha pasada → 422 | CA-TRV-506-02 |
| TC-API-071 | Karate | cupo_total menor a reservado → 422; cupo_reservado enviado se ignora | CA-TRV-506-03 |
| TC-API-072 | Karate | Salida cerrada rechaza reservas (call a reserva de viajero) | CA-TRV-506-04 |
| TC-WEB-028 | Selenium | Crear, cerrar y eliminar salida desde el panel | CA-TRV-506-01, CA-TRV-506-04, CA-TRV-506-05 |
| TC-MOB-030 | Appium | Crear y cerrar salida desde la app | CA-TRV-506-01, CA-TRV-506-04 |

---

#### TRV-507 · Consultar reservas recibidas

**Como** proveedor, **quiero** ver las reservas de mis hoteles y tours con su detalle **para** atender a mis clientes y decidir solicitudes.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Alta |
| Story points | 5 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, reservas, paginacion |
| HU de origen | HU-PRO-009 |

**Contexto.** Bandeja de entrada del flujo de aprobación (las acciones están en TRV-204).

**Reglas de negocio**

1. Lista paginada (15 por página) solo de reservas de servicios propios, filtrable por tipo, estado y servicio.
2. Cada reserva: código, cliente (nombre y correo), servicio, habitación o salida, fechas, cantidades, subtotal, IGV, total, estado y motivo si fue rechazada.
3. Las pendientes se destacan y van primero cuando el filtro es 'Todas'.

**Criterios de aceptación**

- **CA-TRV-507-01 · Listado y detalle**  
  **Dado** que recibí reservas  
  **Cuando** abro Reservas recibidas  
  **Entonces** veo todos los datos de cada una y puedo recorrer todas las páginas
- **CA-TRV-507-02 · Filtros**  
  **Dado** que filtro por Pendiente  
  **Cuando** aplico  
  **Entonces** solo veo pendientes de hotel con sus acciones
- **CA-TRV-507-03 · Vacío y error**  
  **Dado** que no tengo reservas o falla la API  
  **Cuando** abro  
  **Entonces** distingo vacío de error
- **CA-TRV-507-04 · Ajeno**  
  **Dado** que pido el detalle de una reserva de otro proveedor  
  **Cuando** accedo  
  **Entonces** 404

**Endpoints involucrados:** `GET /api/proveedor/reservas?tipo&estado&servicio_id&page` · `GET /api/proveedor/reservas/hotel/{id}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-073 | Karate | Solo reservas de servicios propios (match each servicio.proveedor_id) | CA-TRV-507-01 |
| TC-API-074 | Karate | Filtro estado=pendiente | CA-TRV-507-02 |
| TC-API-075 | Karate | Detalle ajeno → 404 | CA-TRV-507-04 |
| TC-WEB-029 | Selenium | Bandeja de reservas con filtros y paginación | CA-TRV-507-01, CA-TRV-507-02 |
| TC-MOB-031 | Appium | Bandeja de reservas en la app | CA-TRV-507-01 |

---

#### TRV-508 · Eliminar publicaciones propias

**Como** proveedor, **quiero** retirar una publicación que ya no ofrezco **para** que deje de aparecer sin perder el historial.

| Campo | Valor |
| --- | --- |
| Tipo | Historia |
| Épica | TRV-5 Portal del proveedor |
| Prioridad | Media |
| Story points | 3 |
| Componentes | Backend, Web, Móvil |
| Canales | Web, Móvil |
| Etiquetas | proveedor, condiciones, seguridad |
| HU de origen | HU-PRO-010 |

**Contexto.** Borrado lógico. Las reservas históricas siguen mostrando el nombre del servicio.

**Reglas de negocio**

1. Solo el dueño puede eliminar (otro usuario → 403).
2. Bloqueado si hay reservas activas (pendientes o confirmadas futuras) → 409.
3. Se pide confirmación mostrando el nombre de la publicación.
4. Las reservas históricas conservan código, servicio, fechas, cantidades e importes; Mis reservas del viajero sigue funcionando.
5. Eliminar dos veces devuelve 404 sin efectos extra.

**Criterios de aceptación**

- **CA-TRV-508-01 · Confirmar o desistir**  
  **Dado** que pulso Eliminar  
  **Cuando** cancelo  
  **Entonces** la publicación sigue igual
- **CA-TRV-508-02 · Eliminación permitida**  
  **Dado** que no hay reservas activas  
  **Cuando** confirmo  
  **Entonces** deja de aparecer en el catálogo y en mi panel, sin afectar otras publicaciones
- **CA-TRV-508-03 · Historial**  
  **Dado** que tenía reservas pasadas  
  **Cuando** la elimino  
  **Entonces** el viajero y yo seguimos viendo esas reservas con el nombre del servicio
- **CA-TRV-508-04 · Ajeno o con reservas activas**  
  **Dado** que no es mía o tiene reservas activas  
  **Cuando** intento eliminar  
  **Entonces** 403 o 409 sin cambios
- **CA-TRV-508-05 · Repetición**  
  **Dado** que repito la eliminación  
  **Cuando** se procesa  
  **Entonces** 404 sin efectos adicionales

**Endpoints involucrados:** `DELETE /api/servicios/{id}`

**Casos de prueba (subtareas)**

| Id | Herramienta | Caso | Valida |
| --- | --- | --- | --- |
| TC-API-076 | Karate | Eliminar servicio ajeno → 403 (seguridad) | CA-TRV-508-04 |
| TC-API-077 | Karate | Con reserva activa → 409 | CA-TRV-508-04 |
| TC-API-078 | Karate | Tras eliminar, GET /mis-reservas del viajero sigue 200 con el nombre del servicio | CA-TRV-508-03 |
| TC-WEB-030 | Selenium | Eliminar publicación con diálogo de confirmación | CA-TRV-508-01, CA-TRV-508-02 |
| TC-MOB-032 | Appium | Eliminar publicación desde la app | CA-TRV-508-02 |

---

### Épica TRV-6 — Calidad y automatización

_Suites Karate, JMeter, Selenium, Appium y trazabilidad en Jira._ Cada tarea corresponde a un criterio de la rúbrica.

#### TRV-601 · Suite de pruebas de API con Karate DSL

| Campo | Valor |
| --- | --- |
| Tipo | Tarea técnica |
| Prioridad | Más alta |
| Story points | 8 |
| Etiquetas | karate, api, rubrica-1 |

**Objetivo.** Cubrir todas las rutas principales con validación de esquema, códigos, tiempos y reglas de negocio.

**Criterios de aceptación**

- [ ] Un .feature por recurso (auth, hoteles, reservas-hotel, aprobacion, tours, reservas-tour, mis-reservas, resenas, proveedor, itinerario).
- [ ] Validación de esquema con match y archivos de esquema reutilizables (schemas/*.json); 'match each' en listas.
- [ ] Validación de tiempos: responseTime < 1000 ms en cada escenario.
- [ ] Reutilización con call/callonce: login.feature, crear-reserva.feature y csrf.feature como helpers.
- [ ] Datos dinámicos: correos y Idempotency-Key únicos generados con funciones JS; data-driven con Scenario Outline y CSV.
- [ ] karate-config.js con entornos local y ci; ejecución paralela con JUnit 5 Runner y reporte HTML de Karate.

**Entregable:** tests/karate (proyecto Maven) + reporte target/karate-reports/karate-summary.html

---

#### TRV-602 · Plan de rendimiento con Apache JMeter

| Campo | Valor |
| --- | --- |
| Tipo | Tarea técnica |
| Prioridad | Alta |
| Story points | 5 |
| Etiquetas | jmeter, rendimiento, rubrica-2 |

**Objetivo.** Medir el catálogo bajo carga y probar la concurrencia sobre las últimas unidades y cupos.

**Criterios de aceptación**

- [ ] Thread Groups: (1) Navegación de catálogo 100 usuarios, ramp-up 60 s, 5 min; (2) Login 50 usuarios; (3) Carrera por la última habitación 20 hilos con Synchronizing Timer; (4) Carrera por cupos de tour 30 hilos.
- [ ] Data-driven con CSV Data Set Config (usuarios.csv, ciudades.csv, categorias.csv).
- [ ] HTTP Cookie Manager + extractor de XSRF-TOKEN; Idempotency-Key con ${__UUID()}.
- [ ] Aserciones de respuesta (código, JSON) y Duration Assertion (p95 objetivo por grupo).
- [ ] Listeners: Summary Report y Aggregate Report en GUI de diseño; ejecución en modo no-GUI.
- [ ] Reporte HTML generado: jmeter -n -t trouvels360.jmx -l results.jtl -e -o reporte/.

**Entregable:** tests/jmeter/trouvels360.jmx + data/*.csv + reporte HTML

---

#### TRV-603 · Automatización web con Selenium

| Campo | Valor |
| --- | --- |
| Tipo | Tarea técnica |
| Prioridad | Más alta |
| Story points | 8 |
| Etiquetas | selenium, web, rubrica-3 |

**Objetivo.** Automatizar los flujos end-to-end críticos de viajero y proveedor en la web.

**Criterios de aceptación**

- [ ] Patrón Page Object; selectores por atributo data-testid (sin XPath absolutos).
- [ ] Solo esperas explícitas (WebDriverWait / FluentWait); prohibido Thread.sleep.
- [ ] Flujos: registro, login con retorno, búsqueda, reserva de hotel con pago, aprobación por proveedor y verificación del viajero, reserva de tour, cancelación, reseña.
- [ ] Datos de prueba creados por API antes de cada flujo (independencia entre tests).
- [ ] Ejecución headless en CI; captura de pantalla automática al fallar; reporte HTML (Allure o ExtentReports).

**Entregable:** tests/selenium (Java + JUnit 5 + Maven) + reporte HTML

---

#### TRV-604 · Automatización móvil con Appium

| Campo | Valor |
| --- | --- |
| Tipo | Tarea técnica |
| Prioridad | Más alta |
| Story points | 8 |
| Etiquetas | appium, mobile, rubrica-4 |

**Objetivo.** Automatizar los flujos end-to-end en la app Android nativa.

**Criterios de aceptación**

- [ ] Capabilities documentadas y parametrizadas (platformName, automationName UiAutomator2, deviceName, app, appPackage, appActivity, noReset).
- [ ] Localizadores por accessibility id / resource-id (testTag de Compose expuesto con testTagsAsResourceId).
- [ ] Gestos nativos: tap, swipe/scroll en listas, selección de fechas en el DatePicker.
- [ ] Esperas explícitas para sincronizar con la red y el emulador.
- [ ] Flujo end-to-end validado: login → buscar hotel → seleccionar → pagar → ver PENDIENTE; proveedor acepta; viajero ve CONFIRMADA.
- [ ] Reporte HTML con capturas por paso.

**Entregable:** tests/appium (Java + JUnit 5) + APK debug + reporte

---

#### TRV-605 · Trazabilidad y reportes en Jira

| Campo | Valor |
| --- | --- |
| Tipo | Tarea técnica |
| Prioridad | Alta |
| Story points | 3 |
| Etiquetas | jira, trazabilidad, rubrica-5 |

**Objetivo.** Vincular HU → criterio → caso automatizado → ejecución → evidencia / defecto.

**Criterios de aceptación**

- [ ] Cada caso automatizado lleva el key de la HU en su nombre o etiqueta (@TRV-203, @TC-API-021).
- [ ] Matriz de trazabilidad HU × casos × herramienta publicada y adjunta.
- [ ] Los reportes de Karate, JMeter, Selenium y Appium se adjuntan al ticket de ejecución en Jira.
- [ ] Cada fallo genera un Bug enlazado a la HU con pasos, evidencia y severidad.
- [ ] Dashboard de Jira: avance por épica, casos aprobados vs fallidos, bugs abiertos por severidad.

**Entregable:** Importación CSV a Jira + matriz de trazabilidad + tickets de ejecución con reportes adjuntos

---

## 4. Matriz de trazabilidad

| HU | Criterios | Karate | JMeter | Selenium | Appium |
| --- | --- | --- | --- | --- | --- |
| TRV-101 | 5 | TC-API-001, TC-API-002, TC-API-003, TC-API-004 | — | TC-WEB-001, TC-WEB-002 | TC-MOB-001, TC-MOB-002 |
| TRV-102 | 4 | TC-API-005, TC-API-006, TC-API-007 | — | TC-WEB-003 | TC-MOB-003 |
| TRV-103 | 6 | TC-API-008, TC-API-009, TC-API-010, TC-API-011, TC-API-012 | TC-PERF-001 | TC-WEB-004, TC-WEB-005 | TC-MOB-004, TC-MOB-005 |
| TRV-104 | 6 | TC-API-013, TC-API-014, TC-API-015, TC-API-016 | — | TC-WEB-006 | TC-MOB-006, TC-MOB-007 |
| TRV-105 | 4 | TC-API-017, TC-API-018, TC-API-019 | — | TC-WEB-007 | TC-MOB-008 |
| TRV-201 | 5 | TC-API-020, TC-API-021, TC-API-022 | TC-PERF-002 | TC-WEB-008, TC-WEB-009 | TC-MOB-009, TC-MOB-010 |
| TRV-202 | 5 | TC-API-023, TC-API-024, TC-API-025 | — | TC-WEB-010 | TC-MOB-011, TC-MOB-012 |
| TRV-203 | 6 | TC-API-026, TC-API-027, TC-API-028, TC-API-029, TC-API-030 | TC-PERF-003 | TC-WEB-011, TC-WEB-012 | TC-MOB-013, TC-MOB-014 |
| TRV-204 | 6 | TC-API-031, TC-API-032, TC-API-033, TC-API-034, TC-API-035 | — | TC-WEB-013, TC-WEB-014 | TC-MOB-015, TC-MOB-016 |
| TRV-301 | 4 | TC-API-036, TC-API-037 | TC-PERF-004 | TC-WEB-015 | TC-MOB-017 |
| TRV-302 | 4 | TC-API-038, TC-API-039, TC-API-040 | — | TC-WEB-016 | TC-MOB-018 |
| TRV-303 | 5 | TC-API-041, TC-API-042, TC-API-043 | TC-PERF-005 | TC-WEB-017 | TC-MOB-019 |
| TRV-401 | 6 | TC-API-044, TC-API-045, TC-API-046 | — | TC-WEB-018, TC-WEB-019 | TC-MOB-020, TC-MOB-021 |
| TRV-402 | 5 | TC-API-047, TC-API-048, TC-API-049 | — | TC-WEB-020 | TC-MOB-022 |
| TRV-403 | 5 | TC-API-050, TC-API-051, TC-API-052, TC-API-053 | — | TC-WEB-021 | TC-MOB-023 |
| TRV-404 | 5 | TC-API-054, TC-API-055 | — | TC-WEB-022 | TC-MOB-024 |
| TRV-501 | 5 | TC-API-056, TC-API-057, TC-API-058 | TC-PERF-006 | TC-WEB-023 | TC-MOB-025 |
| TRV-502 | 5 | TC-API-059, TC-API-060, TC-API-061 | — | TC-WEB-024 | TC-MOB-026 |
| TRV-503 | 5 | TC-API-062, TC-API-063, TC-API-064 | — | TC-WEB-025 | TC-MOB-027 |
| TRV-504 | 4 | TC-API-065, TC-API-066, TC-API-067 | — | TC-WEB-026 | TC-MOB-028 |
| TRV-505 | 4 | TC-API-068, TC-API-069 | — | TC-WEB-027 | TC-MOB-029 |
| TRV-506 | 6 | TC-API-070, TC-API-071, TC-API-072 | — | TC-WEB-028 | TC-MOB-030 |
| TRV-507 | 4 | TC-API-073, TC-API-074, TC-API-075 | — | TC-WEB-029 | TC-MOB-031 |
| TRV-508 | 5 | TC-API-076, TC-API-077, TC-API-078 | — | TC-WEB-030 | TC-MOB-032 |

| Herramienta | Casos |
| --- | --- |
| Karate (TC-API) | 78 |
| Selenium (TC-WEB) | 30 |
| Appium (TC-MOB) | 32 |
| JMeter (TC-PERF) | 6 |

## 5. Equivalencia con las HUs originales

| HU original | Historia nueva |
| --- | --- |
| HU-PRO-001 | TRV-102, TRV-103 |
| HU-PRO-002 | TRV-104, TRV-105 |
| HU-PRO-003 | TRV-501 |
| HU-PRO-004 | TRV-502 |
| HU-PRO-005 | TRV-503 |
| HU-PRO-006 | TRV-504 |
| HU-PRO-007 | TRV-505 |
| HU-PRO-008 | TRV-506 |
| HU-PRO-009 | TRV-204, TRV-507 |
| HU-PRO-010 | TRV-508 |
| HU-VIA-001 | TRV-101, TRV-103 |
| HU-VIA-002 | TRV-104, TRV-105 |
| HU-VIA-003 | TRV-201, TRV-202 |
| HU-VIA-004 | TRV-203 |
| HU-VIA-005 | TRV-401 |
| HU-VIA-006 | TRV-301, TRV-302 |
| HU-VIA-007 | TRV-303 |
| HU-VIA-008 | TRV-402 |
| HU-VIA-009 | TRV-404 |
| HU-VIA-010 | TRV-403 |
