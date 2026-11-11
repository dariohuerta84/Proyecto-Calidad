# Especificación de Historias de Usuario: Proveedor (Trouvels360)

**Curso:** Calidad de Software (C8305 — UPCH)  
**Sistema Bajo Prueba (SUT):** Plataforma Trouvels360 (Backend Laravel + MySQL + FastAPI)  
**Rol:** Proveedor de Servicios Turísticos  
**Cantidad de Historias:** 10 HUs consolidadas (`HU-PRO-001` a `HU-PRO-010`)  

---

## HU-PRO-001 — Registrarme y gestionar mi sesión como proveedor

**Como** proveedor de servicios turísticos,  
**quiero** registrar mi empresa, e iniciar y cerrar sesión de manera segura,  
**para** acceder a las funciones administrativas de publicación y control de mis alojamientos y tours.

### Criterios de Aceptación
- **CA-PRO-001-01 (Registro Válido):** Dados `empresa_nombre`, `ruc` de 11 dígitos, `telefono` peruano (`+51 9########`), `email` no registrado, `password` de al menos 6 caracteres y confirmación coincidente, cuando se envía `POST /api/auth/register`, entonces se crea una sola cuenta con rol `proveedor` y responde `201 Created` con el token JWT en cookie.
- **CA-PRO-001-02 (Validación de Datos y Duplicados):** Dados campos obligatorios ausentes, contraseñas no coincidentes, o `ruc`/`email` ya registrados, cuando se envía el registro, entonces responde `422 Unprocessable Content` detallando los errores específicos sin persistir cuentas duplicadas.
- **CA-PRO-001-03 (Login Exitoso):** Dadas credenciales válidas y el token CSRF previo, cuando se envía `POST /api/auth/login`, entonces responde `200 OK`, emite la cookie HttpOnly `access_token` y retorna los datos del proveedor.
- **CA-PRO-001-04 (Credenciales Inválidas):** Dadas credenciales erróneas o usuario inexistente, cuando se intenta login, responde `401 Unauthorized` con el mensaje `"Credenciales inválidas"`.
- **CA-PRO-001-05 (Cierre de Sesión):** Dada una sesión activa, cuando se envía `POST /api/auth/logout`, entonces se invalida el token (blacklist), se limpia la cookie de autenticación y peticiones privadas posteriores responden `401 Unauthorized`.

### Mapeo API REST
| Método | Endpoint | Headers / Seguridad | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/auth/csrf` | Ninguno | `200 OK` (emite cookie `XSRF-TOKEN`) |
| `POST` | `/api/auth/register` | `X-XSRF-TOKEN`, Cookie `XSRF-TOKEN` | `201 Created`, `422 Unprocessable Content` |
| `POST` | `/api/auth/login` | `X-XSRF-TOKEN`, Cookie `XSRF-TOKEN` | `200 OK`, `401 Unauthorized`, `422` |
| `GET` | `/api/auth/me` | Cookie `access_token` o Bearer JWT | `200 OK`, `401 Unauthorized` |
| `POST` | `/api/auth/logout` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `401 Unauthorized` |

---

## HU-PRO-002 — Gestionar mi perfil y cuenta de proveedor

**Como** proveedor,  
**quiero** consultar y actualizar los datos de mi empresa, credenciales y estado de mi cuenta,  
**para** mantener mi información comercial actualizada y gestionar la continuidad de mi cuenta.

### Criterios de Aceptación
- **CA-PRO-002-01 (Consulta de Perfil):** Dada mi sesión activa, cuando consulto `GET /api/auth/me`, entonces obtengo únicamente los datos de mi empresa (`empresa_nombre`, `ruc`, `telefono`, `email`, `rol`).
- **CA-PRO-002-02 (Actualización de Datos):** Dadas modificaciones válidas en `empresa_nombre` o `telefono`, cuando envío `PUT /api/usuarios/me`, responde `200 OK` y persiste los cambios.
- **CA-PRO-002-03 (Protección de Rol):** Si en el payload se intenta cambiar el campo `rol`, la validación de `UpdateUsuarioRequest` lo prohíbe o descarta, conservando inmutable el rol `proveedor`.
- **CA-PRO-002-04 (Eliminación de Cuenta):** Dada una confirmación de baja, cuando envío `DELETE /api/usuarios/me`, la cuenta es eliminada (`200 OK`), invalidando el acceso futuro del proveedor.

### Mapeo API REST
| Método | Endpoint | Headers / Seguridad | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/auth/me` | Cookie `access_token` | `200 OK`, `401 Unauthorized` |
| `PUT` | `/api/usuarios/me` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `422 Unprocessable Content` |
| `DELETE` | `/api/usuarios/me` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `401 Unauthorized` |

---

## HU-PRO-003 — Consultar mis publicaciones e indicadores

**Como** proveedor,  
**quiero** visualizar el listado completo de mis hoteles y tours junto con métricas de reservas,  
**para** monitorear el rendimiento de mis publicaciones y seleccionar cuáles gestionar.

### Criterios de Aceptación
- **CA-PRO-003-01 (Listado de Servicios Propios):** Dada mi sesión de proveedor, cuando consulto `GET /api/proveedor/servicios`, entonces recibo únicamente los hoteles y tours donde `proveedor_id = id_autenticado`, soportando paginación (`per_page`, `page`).
- **CA-PRO-003-02 (Métricas Agregadas):** Cada servicio incluye métricas calculadas por subquery: `reservas_totales.confirmadas`, `reservas_totales.canceladas`, cantidad de habitaciones o próximas salidas.
- **CA-PRO-003-03 (Reporte Detallado de Reservas):** Cuando consulto `GET /api/proveedor/servicios/{id}/reservas` de un servicio propio, responde `200 OK` con el desglose agrupado por habitación o salida.
- **CA-PRO-003-04 (Aislamiento de Proveedores):** Si un proveedor intenta consultar las reservas de un servicio ajeno, el backend responde `404 Not Found` o `403 Forbidden`.

### Mapeo API REST
| Método | Endpoint | Parámetros Clave | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/proveedor/servicios` | `tipo=hotel/tour`, `per_page=15` | `200 OK`, `401 Unauthorized` |
| `GET` | `/api/proveedor/servicios/{id}/reservas` | `id` (servicio_id) | `200 OK`, `404 Not Found` |

---

## HU-PRO-004 — Publicar y editar hoteles

**Como** proveedor,  
**quiero** registrar nuevos hoteles y actualizar su información general, estrellas y galería,  
**para** exhibir mi oferta de alojamiento a los viajeros en la plataforma.

### Criterios de Aceptación
- **CA-PRO-004-01 (Crear Hotel):** Con sesión de proveedor, enviando `nombre`, `descripcion`, `ciudad`, `pais`, `direccion`, `estrellas` (1-5) y galería de hasta 5 imágenes a `POST /api/hoteles`, responde `201 Created` y registra en transacción `servicios` y `hoteles`.
- **CA-PRO-004-02 (Validación de Campos):** Si faltan campos obligatorios (`direccion`, `ciudad`) o las estrellas están fuera de rango, responde `422 Unprocessable Content`.
- **CA-PRO-004-03 (Actualizar Hotel):** Al enviar `PUT /api/hoteles/{servicio_id}` con modificaciones válidas, responde `200 OK` y sincroniza datos de servicio, dirección y galería.
- **CA-PRO-004-04 (Restricción por Rol):** Si un usuario con rol `viajero` intenta crear o editar un hotel, el middleware/request responde `403 Forbidden`.

### Mapeo API REST
| Método | Endpoint | Body Payload Clave | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/hoteles` | `{ nombre, direccion, ciudad, pais, estrellas, imagenes: [] }` | `201 Created`, `403`, `422` |
| `GET` | `/api/hoteles/{servicio_id}` | Ninguno | `200 OK`, `404 Not Found` |
| `PUT` | `/api/hoteles/{servicio_id}` | `{ nombre, direccion, estrellas }` | `200 OK`, `403`, `422` |

---

## HU-PRO-005 — Gestionar habitaciones de un hotel

**Como** proveedor,  
**quiero** crear, modificar y eliminar tipos de habitación de mis hoteles,  
**para** definir las capacidades, inventario disponible y tarifas por noche.

### Criterios de Aceptación
- **CA-PRO-005-01 (Crear Habitación):** Dado un hotel propio, enviando `nombre`, `capacidad_adultos` >= 1, `cantidad` >= 1 y `precio_por_noche` a `POST /api/hoteles/{servicio_id}/habitaciones`, responde `201 Created`.
- **CA-PRO-005-02 (Actualizar Habitación):** Al enviar `PUT /api/habitaciones/{habitacion_id}` con nueva tarifa o cantidad, responde `200 OK` y actualiza el registro.
- **CA-PRO-005-03 (Eliminar Habitación):** Al enviar `DELETE /api/habitaciones/{habitacion_id}` sobre una habitación propia, responde `204 No Content` y se elimina del catálogo.
- **CA-PRO-005-04 (Validación de Propiedad):** Si el proveedor intenta gestionar habitaciones de un hotel que no le pertenece, responde `403 Forbidden`.

### Mapeo API REST
| Método | Endpoint | Payload / Params | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/hoteles/{servicio_id}/habitaciones` | `{ nombre, capacidad_adultos, cantidad, precio_por_noche }` | `201 Created`, `403`, `422` |
| `PUT` | `/api/habitaciones/{habitacion}` | `{ nombre, precio_por_noche, cantidad }` | `200 OK`, `403`, `422` |
| `DELETE` | `/api/habitaciones/{habitacion}` | Ninguno | `204 No Content`, `403` |

---

## HU-PRO-006 — Publicar y editar tours

**Como** proveedor,  
**quiero** registrar y actualizar tours con categoría, duración, precio base y salidas iniciales,  
**para** ofrecer experiencias y actividades guiadas a los turistas.

### Criterios de Aceptación
- **CA-PRO-006-01 (Crear Tour):** Enviando `nombre`, `ciudad`, `pais`, `categoria` (Gastronomía, Aventura, Cultura, Relajación), `duracion`, `precio` y arreglo `salidas` a `POST /api/tours`, responde `201 Created` y guarda en transacción `servicios`, `tours` y `tour_salidas`.
- **CA-PRO-006-02 (Actualizar Tour):** Al enviar `PUT /api/tours/{tour_id}` con cambios de precio o descripción, responde `200 OK` conservando las relaciones existentes.
- **CA-PRO-006-03 (Control de Propiedad):** Solo el proveedor dueño del tour puede modificarlo (`assertOwnership`); de lo contrario responde `403 Forbidden`.

### Mapeo API REST
| Método | Endpoint | Body Payload Clave | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/tours` | `{ nombre, ciudad, categoria, duracion, precio, salidas: [...] }` | `201 Created`, `403`, `422` |
| `GET` | `/api/tours/{tour}` | Ninguno | `200 OK`, `404 Not Found` |
| `PUT` | `/api/tours/{tour}` | `{ nombre, precio, duracion, categoria }` | `200 OK`, `403`, `422` |

---

## HU-PRO-007 — Gestionar actividades del itinerario del tour

**Como** proveedor,  
**quiero** estructurar el programa de actividades secuenciales de un tour,  
**para** informar a los viajeros la ruta, paradas, duración y contenido de la excursión.

### Criterios de Aceptación
- **CA-PRO-007-01 (Crear Actividad):** Enviando `titulo`, `orden`, `duracion_min` y `descripcion` a `POST /api/tours/{tour}/actividades`, responde `201 Created`.
- **CA-PRO-007-02 (Listar Actividades):** Al consultar `GET /api/tours/{tour}/actividades`, responde `200 OK` con las actividades ordenadas por la columna `orden`.
- **CA-PRO-007-03 (Actualizar Actividad):** Al enviar `PUT /api/tours/{tour}/actividades/{actividad}`, responde `200 OK` con los datos actualizados.
- **CA-PRO-007-04 (Eliminar Actividad):** Al enviar `DELETE /api/tours/{tour}/actividades/{actividad}`, responde `200 OK` con `{"message": "Actividad eliminada"}`.

### Mapeo API REST
| Método | Endpoint | Payload / Params | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/tours/{tour}/actividades` | `{ titulo, orden, duracion_min, descripcion }` | `201 Created`, `403`, `422` |
| `GET` | `/api/tours/{tour}/actividades` | Ninguno | `200 OK`, `404 Not Found` |
| `PUT` | `/api/tours/{tour}/actividades/{actividad}` | `{ titulo, duracion_min }` | `200 OK`, `403` |
| `DELETE` | `/api/tours/{tour}/actividades/{actividad}` | Ninguno | `200 OK`, `403` |

---

## HU-PRO-008 — Gestionar salidas y cupos del tour

**Como** proveedor,  
**quiero** programar fechas, horarios y cupos de salidas para mis tours,  
**para** controlar la capacidad operativa y disponibilidad en tiempo real.

### Criterios de Aceptación
- **CA-PRO-008-01 (Crear Salida):** Enviando `fecha` (>= hoy), `hora`, `cupo_total` y `estado` (`programada`) a `POST /api/tours/{tour}/salidas`, responde `201 Created`.
- **CA-PRO-008-02 (Listar Salidas):** Al consultar `GET /api/tours/{tour}/salidas`, responde `200 OK` con las salidas y el cálculo `cupo_disponible = cupo_total - cupo_reservado`.
- **CA-PRO-008-03 (Actualizar Salida):** Al enviar `PUT /api/tours/{tour}/salidas/{salida}`, responde `200 OK`. Si se intenta fijar un `cupo_total < cupo_reservado`, responde `422 Unprocessable Content`.
- **CA-PRO-008-04 (Eliminar Salida sin Reservas):** Si no tiene reservas confirmadas, `DELETE /api/tours/{tour}/salidas/{salida}` elimina la salida (`200 OK`). Si ya cuenta con reservas confirmadas, responde `422 Unprocessable Content`.

### Mapeo API REST
| Método | Endpoint | Payload / Params | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/tours/{tour}/salidas` | `{ fecha, hora, cupo_total, estado }` | `201 Created`, `403`, `422` |
| `GET` | `/api/tours/{tour}/salidas` | Query: `desde`, `hasta`, `estado` | `200 OK`, `404 Not Found` |
| `PUT` | `/api/tours/{tour}/salidas/{salida}` | `{ cupo_total, estado }` | `200 OK`, `422 Unprocessable Content` |
| `DELETE` | `/api/tours/{tour}/salidas/{salida}` | Ninguno | `200 OK`, `422 Unprocessable Content` |

---

## HU-PRO-009 — Consultar reservas recibidas de mis servicios

**Como** proveedor,  
**quiero** revisar el estado y desglose de las reservas efectuadas sobre mis hoteles y tours,  
**para** conocer la ocupación y preparar la atención de los huéspedes y participantes.

### Criterios de Aceptación
- **CA-PRO-009-01 (Totales y Detalle por Habitación/Salida):** Al consultar `GET /api/proveedor/servicios/{id}/reservas`, devuelve el conteo de reservas confirmadas y canceladas, y la lista agrupada con montos totales.
- **CA-PRO-009-02 (Aislamiento de Información):** Si el servicio consultado pertenece a otro proveedor, responde `404 Not Found` (o `403 Forbidden`), garantizando la privacidad de los clientes.
- **CA-PRO-009-03 (Auditoría de Estados):** Las reservas con estado `cancelada` no suman en ocupación activa de habitaciones ni en cupos reservados de tours.

### Mapeo API REST
| Método | Endpoint | Headers | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/proveedor/servicios/{id}/reservas` | Cookie `access_token` | `200 OK`, `404 Not Found` |

---

## HU-PRO-010 — Retirar y eliminar publicaciones propias

**Como** proveedor,  
**quiero** retirar publicaciones de servicios que ya no comercializo,  
**para** mantener mi catálogo limpio y evitar que los viajeros sigan reservándolos.

### Criterios de Aceptación
- **CA-PRO-010-01 (Eliminar Servicio en Cascada):** Al enviar `DELETE /api/servicios/{id}` sobre un servicio propio, responde `204 No Content` y efectúa soft delete de imágenes, reseñas, habitaciones/tours asociados.
- **CA-PRO-010-02 (Eliminar Tour Específico):** Al enviar `DELETE /api/tours/{tour}`, responde `200 OK` con `{"message": "Tour eliminado"}`.
- **CA-PRO-010-03 (Eliminar Hotel Específico):** Al enviar `DELETE /api/hoteles/{servicio_id}`, responde `204 No Content`.
- **CA-PRO-010-04 (Consulta de Eliminados):** Al consultar `GET /api/eliminados`, responde `200 OK` con la relación histórica de registros en papelera (`withTrashed`).

### Mapeo API REST
| Método | Endpoint | Headers | Códigos HTTP |
| :--- | :--- | :--- | :--- |
| `DELETE` | `/api/servicios/{id}` | `X-XSRF-TOKEN`, Cookie `access_token` | `204 No Content`, `403` |
| `DELETE` | `/api/tours/{tour}` | `X-XSRF-TOKEN`, Cookie `access_token` | `200 OK`, `403` |
| `DELETE` | `/api/hoteles/{servicio_id}` | `X-XSRF-TOKEN`, Cookie `access_token` | `204 No Content`, `403` |
| `GET` | `/api/eliminados` | Público | `200 OK` |
