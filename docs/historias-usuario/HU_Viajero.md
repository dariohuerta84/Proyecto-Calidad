# Especificación de Historias de Usuario: Viajero (Trouvels360)

**Curso:** Calidad de Software (C8305 — UPCH)  
**Sistema Bajo Prueba (SUT):** Plataforma Trouvels360 (Backend Laravel + MySQL + FastAPI)  
**Rol:** Viajero / Turista  
**Cantidad de Historias:** 10 HUs consolidadas (`HU-VIA-001` a `HU-VIA-010`)  

---

## HU-VIA-001 — Registrarme y gestionar mi sesión como viajero

**Como** viajero,  
**quiero** crear una cuenta personal, e iniciar y cerrar sesión,  
**para** explorar la oferta turística, gestionar reservas de hoteles y tours y guardar itinerarios.

### Criterios de Aceptación
- **CA-VIA-001-01 (Registro Válido):** Dados `nombre`, `apellido`, `email` válido, `password` de al menos 6 caracteres y rol `viajero`, al enviar `POST /api/auth/register`, se crea la cuenta, responde `201 Created` y emite cookie `access_token`.
- **CA-VIA-001-02 (Validación de Duplicados e Inválidos):** Si el email ya existe o faltan campos obligatorios, responde `422 Unprocessable Content` sin persistir el usuario.
- **CA-VIA-001-03 (Inicio de Sesión):** Dadas credenciales válidas y el token CSRF, al enviar `POST /api/auth/login`, responde `200 OK` retornando el objeto `user` con rol `viajero`.
- **CA-VIA-001-04 (Credenciales Incorrectas):** Al enviar clave incorrecta, responde `401 Unauthorized` con el mensaje `"Credenciales inválidas"`.
- **CA-VIA-001-05 (Cierre de Sesión):** Al enviar `POST /api/auth/logout`, se elimina la cookie `access_token` y se impide el acceso a rutas privadas (`/api/mis-reservas`, `/api/auth/me`).

### Mapeo API REST
| Método | Endpoint | Headers / Parámetros | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/auth/csrf` | Ninguno | `200 OK` (emite cookie `XSRF-TOKEN`) |
| `POST` | `/api/auth/register` | `X-XSRF-TOKEN`, Cookie `XSRF-TOKEN` | `201 Created`, `422 Unprocessable Content` |
| `POST` | `/api/auth/login` | `X-XSRF-TOKEN`, Cookie `XSRF-TOKEN` | `200 OK`, `401 Unauthorized` |
| `POST` | `/api/auth/logout` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `401 Unauthorized` |

---

## HU-VIA-002 — Gestionar mi perfil y cuenta de viajero

**Como** viajero,  
**quiero** consultar mis datos registrados, modificarlos o eliminar mi cuenta,  
**para** mantener mi información personal actualizada y ejercer el control sobre mis datos.

### Criterios de Aceptación
- **CA-VIA-002-01 (Consulta de Perfil):** Con sesión activa, `GET /api/auth/me` devuelve los datos personales (`nombre`, `apellido`, `email`, `rol = 'viajero'`).
- **CA-VIA-002-02 (Actualizar Datos):** Al enviar `PUT /api/usuarios/me` con nuevos valores para `nombre` y `apellido`, responde `200 OK` y persisten los cambios.
- **CA-VIA-002-03 (Inmutabilidad de Rol):** Cualquier intento de mutar el rol a `proveedor` es rechazado o descartado por `UpdateUsuarioRequest`.
- **CA-VIA-002-04 (Eliminación de Cuenta):** Al enviar `DELETE /api/usuarios/me`, responde `200 OK` con `{"message": "Cuenta eliminada exitosamente."}`.

### Mapeo API REST
| Método | Endpoint | Headers | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/auth/me` | Cookie `access_token` | `200 OK`, `401 Unauthorized` |
| `PUT` | `/api/usuarios/me` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `422 Unprocessable Content` |
| `DELETE` | `/api/usuarios/me` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `401 Unauthorized` |

---

## HU-VIA-003 — Buscar hoteles y consultar disponibilidad de habitaciones

**Como** viajero,  
**quiero** filtrar alojamientos por ciudad, fechas de estadía y capacidad de huéspedes,  
**para** encontrar habitaciones que coincidan con mi plan de viaje.

### Criterios de Aceptación
- **CA-VIA-003-01 (Búsqueda General con Fechas):** Al consultar `GET /api/hoteles?check_in=YYYY-MM-DD&check_out=YYYY-MM-DD&ciudad=Cusco&habitaciones=1`, responde `200 OK` con la lista de hoteles disponibles y sus habitaciones libres.
- **CA-VIA-003-02 (Disponibilidad por Hotel):** Al consultar `GET /api/hoteles/{servicio_id}/disponibilidad?check_in=...&check_out=...&adultos=2`, responde `200 OK` calculando `unidades_disponibles = cantidad - unidades_ocupadas`.
- **CA-VIA-003-03 (Fechas Inválidas):** Si `check_out <= check_in`, responde `422 Unprocessable Content` informando el error de validación.
- **CA-VIA-003-04 (Detalle de Hotel y Reseñas):** Al consultar `GET /api/hoteles/{servicio_id}`, responde `200 OK` con dirección, estrellas, lista de habitaciones y las últimas reseñas con calificación promedio.

### Mapeo API REST
| Método | Endpoint | Parámetros Query | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/hoteles` | `check_in`, `check_out`, `ciudad`, `adultos`, `habitaciones` | `200 OK`, `422` |
| `GET` | `/api/hoteles/{id}` | Ninguno | `200 OK`, `404 Not Found` |
| `GET` | `/api/hoteles/{id}/disponibilidad` | `check_in`, `check_out`, `adultos`, `habitaciones` | `200 OK`, `404`, `422` |

---

## HU-VIA-004 — Solicitar reserva de hotel

**Como** viajero,  
**quiero** solicitar la reserva de una o más unidades de habitación para un rango de fechas,  
**para** apartar mi hospedaje en el destino seleccionado.

### Criterios de Aceptación
- **CA-VIA-004-01 (Creación Exitosa):** Estando autenticado como viajero, enviando `habitacion_id`, `fecha_inicio`, `fecha_fin` (`after:fecha_inicio`) y `cantidad` a `POST /api/reservas-habitaciones`, responde `201 Created` con `estado = 'pendiente'`, código único y cálculo `total = precio_por_noche * cantidad * noches`.
- **CA-VIA-004-02 (Validación de Sobreventa):** Si la cantidad solicitada supera las unidades disponibles en el rango de fechas, responde `422 Unprocessable Content` con el detalle de unidades disponibles.
- **CA-VIA-004-03 (Restricción por Rol):** Si un usuario con rol `proveedor` o sin autenticación intenta reservar, responde `403 Forbidden` o `401 Unauthorized`.

### Mapeo API REST
| Método | Endpoint | Body Payload Clave | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reservas-habitaciones` | `{ habitacion_id, fecha_inicio, fecha_fin, cantidad }` | `201 Created`, `403`, `422` |

---

## HU-VIA-005 — Consultar mis reservas y estados actuales

**Como** viajero,  
**quiero** ver el historial y estado vigente de mis reservas de hotel y tour,  
**para** hacer seguimiento a mis confirmaciones y detalles de viaje.

### Criterios de Aceptación
- **CA-VIA-005-01 (Mis Reservas de Hotel):** Con sesión de viajero, `GET /api/mis-reservas` devuelve la lista de reservas propias con código, fechas, cantidad, precio, total, nombre del hotel y estado (`pendiente`, `confirmada`, `cancelada`).
- **CA-VIA-005-02 (Mis Reservas de Tour):** Con sesión de viajero, `GET /api/tours/mis-reservas` devuelve la relación paginada de reservas de tours asociadas al usuario autenticado.
- **CA-VIA-005-03 (Aislamiento de Clientes):** Un viajero nunca puede visualizar las reservas de otros usuarios.

### Mapeo API REST
| Método | Endpoint | Headers | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/mis-reservas` | Cookie `access_token` | `200 OK`, `401 Unauthorized` |
| `GET` | `/api/tours/mis-reservas` | Cookie `access_token` | `200 OK`, `401 Unauthorized` |

---

## HU-VIA-006 — Buscar tours y consultar salidas programadas

**Como** viajero,  
**quiero** explorar tours turísticos por destino o categoría y revisar sus próximas salidas,  
**para** elegir experiencias que se adapten a mi itinerario.

### Criterios de Aceptación
- **CA-VIA-006-01 (Búsqueda y Filtros de Tour):** Al consultar `GET /api/tours?destino=Cusco&categoria=Aventura`, responde `200 OK` con los tours paginados y su precio base.
- **CA-VIA-006-02 (Detalle del Tour):** Al consultar `GET /api/tours/{tour}`, responde `200 OK` con información del servicio, ítems para llevar, actividades programadas y salidas futuras abiertas.
- **CA-VIA-006-03 (Salidas y Cupos Disponibles):** Al consultar `GET /api/tours/{tour}/salidas?desde=YYYY-MM-DD`, responde `200 OK` con fechas, horas y `cupo_disponible`.

### Mapeo API REST
| Método | Endpoint | Parámetros Query | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/tours` | `destino`, `categoria`, `checkIn`, `cupos`, `sort` | `200 OK` |
| `GET` | `/api/tours/{tour}` | Ninguno | `200 OK`, `404 Not Found` |
| `GET` | `/api/tours/{tour}/salidas` | `desde`, `hasta`, `estado` | `200 OK`, `404 Not Found` |

---

## HU-VIA-007 — Reservar un tour y asegurar cupos

**Como** viajero,  
**quiero** reservar cupos para una salida programada de un tour,  
**para** garantizar la participación de mis acompañantes en la actividad.

### Criterios de Aceptación
- **CA-VIA-007-01 (Confirmación Inmediata):** Enviando `personas` >= 1 a `POST /api/tours/salidas/{salida}/reservas`, si hay cupo disponible, responde `201 Created` con `estado = 'confirmada'`, código alfanumérico y actualiza `cupo_reservado` en la salida.
- **CA-VIA-007-02 (Validación de Capacidad):** Si `personas > cupo_disponible`, responde `422 Unprocessable Content` con mensaje `"Cupo insuficiente"`.
- **CA-VIA-007-03 (Salida no Programada):** Si la salida está en estado `cerrada` o `cancelada`, responde `422 Unprocessable Content`.

### Mapeo API REST
| Método | Endpoint | Body Payload | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/tours/salidas/{salida}/reservas` | `{ personas }` | `201 Created`, `403`, `422` |

---

## HU-VIA-008 — Cancelar reservas propias

**Como** viajero,  
**quiero** cancelar una reserva propia de hotel o tour antes de su ejecución,  
**para** liberar las habitaciones o cupos que no podré utilizar.

### Criterios de Aceptación
- **CA-VIA-008-01 (Cancelar Reserva Hotel):** Al enviar `POST /api/reservas-habitaciones/{id}/cancelar`, si la reserva pertenece al viajero y no estaba cancelada, responde `200 OK`, cambia su estado a `cancelada` y libera disponibilidad.
- **CA-VIA-008-02 (Cancelar Reserva Tour):** Al enviar `POST /api/tours/reservas/{id}/cancelar`, responde `200 OK`, actualiza el estado a `cancelada` y descuenta las personas de `cupo_reservado` en la salida.
- **CA-VIA-008-03 (Idempotencia y Validación):** Si ya estaba cancelada, responde `422 Unprocessable Content` (en hotel) o mensaje informativo (en tour).
- **CA-VIA-008-04 (Control de Propiedad):** Si se intenta cancelar una reserva ajena, responde `403 Forbidden`.

### Mapeo API REST
| Método | Endpoint | Headers | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reservas-habitaciones/{id}/cancelar` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `403`, `422` |
| `POST` | `/api/tours/reservas/{id}/cancelar` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `403`, `422` |

---

## HU-VIA-009 — Generar sugerencias de itinerario optimizado

**Como** viajero,  
**quiero** solicitar un itinerario inteligente basado en mi hotel, fechas de viaje y categoría de interés,  
**para** obtener una propuesta de actividades optimizada por proximidad geográfica.

### Criterios de Aceptación
- **CA-VIA-009-01 (Generación Exitosa):** Enviando `destino`, `hotel_id`, `fecha_checkin`, `fecha_checkout` e `interes` (`Aventura`, `Cultura`, `Gastronomía`, `Relajación`) a `POST /api/itineraries` (o directo al microservicio en `:8001/api/itinerary`), responde `200 OK` con la distribución de días, actividades y distancias optimizadas (O(N log N)).
- **CA-VIA-009-02 (Validación de Parámetros):** Si falta el destino o `checkout <= checkin`, responde `400 Bad Request` o `422 Unprocessable Content`.
- **CA-VIA-009-03 (Healthcheck del Servicio):** Al consultar `GET /api/health` en el microservicio FastAPI (`:8001`), responde `200 OK` con `{"status": "healthy"}`.

### Mapeo API REST
| Método | Endpoint | Payload Clave | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `:8001/api/health` | Ninguno | `200 OK` |
| `POST` | `/api/itineraries` (Laravel Gateway) | `{ destino, hotel_id, fecha_checkin, fecha_checkout, interes }` | `200 OK`, `422`, `503` |
| `POST` | `:8001/api/itinerary` (FastAPI Directo) | `{ destino, hotel_id, fecha_checkin, fecha_checkout, interes }` | `200 OK`, `400`, `404` |

---

## HU-VIA-010 — Consultar, publicar y gestionar reseñas

**Como** viajero,  
**quiero** leer opiniones de otros turistas y publicar, editar o eliminar mis propias reseñas,  
**para** compartir mi experiencia y ayudar a la comunidad viajera.

### Criterios de Aceptación
- **CA-VIA-010-01 (Consultar Reseñas):** Al consultar `GET /api/reviews?servicio_id={id}`, responde `200 OK` con la lista paginada de reseñas, usuario y calificación.
- **CA-VIA-010-02 (Crear Reseña):** Estando autenticado como viajero y habiendo completado previamente una estadía/tour en el servicio (`fecha_fin < hoy`), enviando `servicio_id`, `comentario` (<= 300 caracteres) y `calificacion` (1 a 5) a `POST /api/reviews`, responde `201 Created`.
- **CA-VIA-010-03 (Restricción de Negocio):** Si el viajero no tiene reserva completada para ese servicio, `StoreReviewRequest` responde `403 Forbidden`.
- **CA-VIA-010-04 (Actualizar Reseña Propia):** Al enviar `PUT /api/reviews/{id}` siendo autor de la reseña, responde `200 OK` con la calificación o comentario modificado.
- **CA-VIA-010-05 (Eliminar Reseña Propia):** Al enviar `DELETE /api/reviews/{id}`, si el usuario autenticado es el autor, responde `204 No Content`. Si intenta borrar la reseña de otro viajero, responde `403 Forbidden`.

### Mapeo API REST
| Método | Endpoint | Payload / Headers | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/reviews` | Query: `servicio_id`, `calificacion` | `200 OK` |
| `POST` | `/api/reviews` | `{ servicio_id, comentario, calificacion }` | `201 Created`, `403`, `422` |
| `PUT` | `/api/reviews/{review}` | `{ comentario, calificacion }` | `200 OK`, `403`, `422` |
| `DELETE` | `/api/reviews/{review}` | Cookie `access_token` | `204 No Content`, `403` |
