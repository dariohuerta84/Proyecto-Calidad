# Matriz de Trazabilidad y Reporte de Defectos (Trouvels360)

**Asignatura:** Calidad de Software (C8305 — UPCH)  
**Semestre:** 2026-II  
**Profesor Coordinador:** Mg. Ing. Gustavo Henry Pachas Quispe  
**Alumno / Responsable:** Matías Huerta  
**SUT (Sistema Bajo Prueba):** Ecosistema Trouvels360 (API Laravel PHP + Microservicio FastAPI Python + MySQL 8.0)  

---

## 1. Matriz de Trazabilidad de Extremo a Extremo (20 HUs)

| ID HU | Historia de Usuario | Verbo HTTP | Endpoint REST | Caso Karate BDD | Request Postman | Sampler JMeter / cURL | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **HU-PRO-001** | Registro y Sesión Proveedor | `POST` | `/api/auth/register` | `pro_auth_perfil.feature` | `PRO-01 Register Proveedor` | `POST /api/auth/register` | `201 Created` |
| **HU-PRO-001** | Login Proveedor | `POST` | `/api/auth/login` | `pro_auth_perfil.feature` | `PRO-02 Login Proveedor` | `POST /api/auth/login` | `200 OK` + Cookie |
| **HU-PRO-001** | Logout Proveedor | `POST` | `/api/auth/logout` | `pro_auth_perfil.feature` | `PRO-05 Logout Proveedor` | `POST /api/auth/logout` | `200 OK` |
| **HU-PRO-002** | Consultar Perfil Proveedor | `GET` | `/api/auth/me` | `pro_auth_perfil.feature` | `PRO-03 Get Me Proveedor` | `GET /api/auth/me` | `200 OK` |
| **HU-PRO-002** | Actualizar Datos Proveedor | `PUT` | `/api/usuarios/me` | `pro_auth_perfil.feature` | `PRO-04 Update Perfil Proveedor` | `PUT /api/usuarios/me` | `200 OK` |
| **HU-PRO-003** | Publicaciones del Proveedor | `GET` | `/api/proveedor/servicios` | `pro_hoteles_habitaciones.feature` | `PRO-06 Mis Publicaciones` | `GET /api/proveedor/servicios` | `200 OK` + Métricas |
| **HU-PRO-003** | Reservas Recibidas por Servicio | `GET` | `/api/proveedor/servicios/{id}/reservas` | `pro_tours_actividades_salidas.feature` | `PRO-07 Reservas por Servicio` | `GET /api/proveedor/servicios/{id}/reservas` | `200 OK` |
| **HU-PRO-004** | Crear Hotel | `POST` | `/api/hoteles` | `pro_hoteles_habitaciones.feature` | `PRO-08 Crear Hotel` | `POST /api/hoteles` | `201 Created` |
| **HU-PRO-004** | Consultar Hotel por ID | `GET` | `/api/hoteles/{id}` | `pro_hoteles_habitaciones.feature` | `PRO-09 Consultar Hotel` | `GET /api/hoteles/{id}` | `200 OK` |
| **HU-PRO-004** | Actualizar Hotel | `PUT` | `/api/hoteles/{id}` | `pro_hoteles_habitaciones.feature` | `PRO-10 Actualizar Hotel` | `PUT /api/hoteles/{id}` | `200 OK` |
| **HU-PRO-005** | Agregar Habitación | `POST` | `/api/hoteles/{id}/habitaciones` | `pro_hoteles_habitaciones.feature` | `PRO-11 Crear Habitacion` | `POST /api/hoteles/{id}/habitaciones` | `201 Created` |
| **HU-PRO-005** | Actualizar Habitación | `PUT` | `/api/habitaciones/{id}` | `pro_hoteles_habitaciones.feature` | `PRO-12 Modificar Habitacion` | `PUT /api/habitaciones/{id}` | `200 OK` |
| **HU-PRO-005** | Eliminar Habitación | `DELETE` | `/api/habitaciones/{id}` | `pro_hoteles_habitaciones.feature` | `PRO-13 Eliminar Habitacion` | `DELETE /api/habitaciones/{id}` | `204 No Content` |
| **HU-PRO-006** | Crear Tour con Salidas | `POST` | `/api/tours` | `pro_tours_actividades_salidas.feature` | `PRO-14 Crear Tour` | `POST /api/tours` | `201 Created` |
| **HU-PRO-006** | Consultar Tour Detalle | `GET` | `/api/tours/{id}` | `pro_tours_actividades_salidas.feature` | `PRO-15 Consultar Tour` | `GET /api/tours/{id}` | `200 OK` |
| **HU-PRO-006** | Actualizar Tour | `PUT` | `/api/tours/{id}` | `pro_tours_actividades_salidas.feature` | `PRO-16 Actualizar Tour` | `PUT /api/tours/{id}` | `200 OK` |
| **HU-PRO-007** | Crear Actividad de Tour | `POST` | `/api/tours/{id}/actividades` | `pro_tours_actividades_salidas.feature` | `PRO-17 Crear Actividad` | `POST /api/tours/{id}/actividades` | `201 Created` |
| **HU-PRO-007** | Listar Actividades | `GET` | `/api/tours/{id}/actividades` | `pro_tours_actividades_salidas.feature` | `PRO-18 Listar Actividades` | `GET /api/tours/{id}/actividades` | `200 OK` |
| **HU-PRO-007** | Actualizar Actividad | `PUT` | `/api/tours/{id}/actividades/{act}` | `pro_tours_actividades_salidas.feature` | `PRO-19 Actualizar Actividad` | `PUT /api/tours/{id}/actividades/{act}` | `200 OK` |
| **HU-PRO-007** | Eliminar Actividad | `DELETE` | `/api/tours/{id}/actividades/{act}` | `pro_tours_actividades_salidas.feature` | `PRO-20 Eliminar Actividad` | `DELETE /api/tours/{id}/actividades/{act}` | `200 OK` |
| **HU-PRO-008** | Programar Salida de Tour | `POST` | `/api/tours/{id}/salidas` | `pro_tours_actividades_salidas.feature` | `PRO-21 Crear Salida` | `POST /api/tours/{id}/salidas` | `201 Created` |
| **HU-PRO-008** | Consultar Salidas | `GET` | `/api/tours/{id}/salidas` | `pro_tours_actividades_salidas.feature` | `PRO-22 Listar Salidas` | `GET /api/tours/{id}/salidas` | `200 OK` |
| **HU-PRO-008** | Modificar Cupo Salida | `PUT` | `/api/tours/{id}/salidas/{sal}` | `pro_tours_actividades_salidas.feature` | `PRO-23 Modificar Salida` | `PUT /api/tours/{id}/salidas/{sal}` | `200 OK` |
| **HU-PRO-008** | Eliminar Salida Libre | `DELETE` | `/api/tours/{id}/salidas/{sal}` | `pro_tours_actividades_salidas.feature` | `PRO-24 Eliminar Salida` | `DELETE /api/tours/{id}/salidas/{sal}` | `200 OK` |
| **HU-PRO-009** | Consultar Reporte Reservas | `GET` | `/api/proveedor/servicios/{id}/reservas` | `pro_tours_actividades_salidas.feature` | `PRO-25 Auditoría Reservas` | `GET /api/proveedor/servicios/{id}/reservas` | `200 OK` |
| **HU-PRO-010** | Retirar Publicación (Cascada) | `DELETE` | `/api/servicios/{id}` | `pro_hoteles_habitaciones.feature` | `PRO-26 Eliminar Servicio` | `DELETE /api/servicios/{id}` | `204 No Content` |
| **HU-PRO-010** | Consultar Papelera Eliminados | `GET` | `/api/eliminados` | `pro_hoteles_habitaciones.feature` | `PRO-27 Listar Eliminados` | `GET /api/eliminados` | `200 OK` |
| **HU-VIA-001** | Registro Viajero | `POST` | `/api/auth/register` | `via_auth_perfil.feature` | `VIA-01 Register Viajero` | `POST /api/auth/register` | `201 Created` |
| **HU-VIA-001** | Login Viajero | `POST` | `/api/auth/login` | `via_auth_perfil.feature` | `VIA-02 Login Viajero` | `POST /api/auth/login` | `200 OK` |
| **HU-VIA-002** | Consultar Perfil Viajero | `GET` | `/api/auth/me` | `via_auth_perfil.feature` | `VIA-03 Get Me Viajero` | `GET /api/auth/me` | `200 OK` |
| **HU-VIA-002** | Actualizar Datos Viajero | `PUT` | `/api/usuarios/me` | `via_auth_perfil.feature` | `VIA-04 Update Perfil Viajero` | `PUT /api/usuarios/me` | `200 OK` |
| **HU-VIA-002** | Eliminar Cuenta Viajero | `DELETE` | `/api/usuarios/me` | `via_auth_perfil.feature` | `VIA-05 Eliminar Cuenta Viajero` | `DELETE /api/usuarios/me` | `200 OK` |
| **HU-VIA-003** | Búsqueda General Hoteles | `GET` | `/api/hoteles` | `via_hoteles_reservas.feature` | `VIA-06 Buscar Hoteles` | `GET /api/hoteles` | `200 OK` |
| **HU-VIA-003** | Disponibilidad de Hotel | `GET` | `/api/hoteles/{id}/disponibilidad` | `via_hoteles_reservas.feature` | `VIA-07 Consultar Disponibilidad` | `GET /api/hoteles/{id}/disponibilidad` | `200 OK` |
| **HU-VIA-004** | Solicitar Reserva Hotel | `POST` | `/api/reservas-habitaciones` | `via_hoteles_reservas.feature` | `VIA-08 Reservar Habitacion` | `POST /api/reservas-habitaciones` | `201 Created` |
| **HU-VIA-005** | Mis Reservas de Hotel | `GET` | `/api/mis-reservas` | `via_hoteles_reservas.feature` | `VIA-09 Mis Reservas Hotel` | `GET /api/mis-reservas` | `200 OK` |
| **HU-VIA-005** | Mis Reservas de Tour | `GET` | `/api/tours/mis-reservas` | `via_tours_reservas.feature` | `VIA-10 Mis Reservas Tour` | `GET /api/tours/mis-reservas` | `200 OK` |
| **HU-VIA-006** | Buscar Tours por Destino | `GET` | `/api/tours` | `via_tours_reservas.feature` | `VIA-11 Buscar Tours` | `GET /api/tours` | `200 OK` |
| **HU-VIA-006** | Salidas Programadas de Tour | `GET` | `/api/tours/{id}/salidas` | `via_tours_reservas.feature` | `VIA-12 Salidas Tour` | `GET /api/tours/{id}/salidas` | `200 OK` |
| **HU-VIA-007** | Reservar Tour | `POST` | `/api/tours/salidas/{id}/reservas` | `via_tours_reservas.feature` | `VIA-13 Reservar Tour` | `POST /api/tours/salidas/{id}/reservas` | `201 Created` |
| **HU-VIA-008** | Cancelar Reserva de Hotel | `POST` | `/api/reservas-habitaciones/{id}/cancelar` | `via_hoteles_reservas.feature` | `VIA-14 Cancelar Reserva Hotel` | `POST /api/reservas-habitaciones/{id}/cancelar` | `200 OK` |
| **HU-VIA-008** | Cancelar Reserva de Tour | `POST` | `/api/tours/reservas/{id}/cancelar` | `via_tours_reservas.feature` | `VIA-15 Cancelar Reserva Tour` | `POST /api/tours/reservas/{id}/cancelar` | `200 OK` |
| **HU-VIA-009** | Healthcheck MS Itinerarios | `GET` | `:8001/api/health` | `via_itinerarios_reviews.feature` | `VIA-16 Healthcheck Itinerarios` | `GET :8001/api/health` | `200 OK` |
| **HU-VIA-009** | Generar Itinerario Óptimo | `POST` | `/api/itineraries` | `via_itinerarios_reviews.feature` | `VIA-17 Generar Itinerario` | `POST /api/itineraries` | `200 OK` |
| **HU-VIA-010** | Listar Reseñas de Servicio | `GET` | `/api/reviews` | `via_itinerarios_reviews.feature` | `VIA-18 Listar Reviews` | `GET /api/reviews` | `200 OK` |
| **HU-VIA-010** | Publicar Reseña | `POST` | `/api/reviews` | `via_itinerarios_reviews.feature` | `VIA-19 Crear Review` | `POST /api/reviews` | `201 Created` |
| **HU-VIA-010** | Editar Reseña Propia | `PUT` | `/api/reviews/{id}` | `via_itinerarios_reviews.feature` | `VIA-20 Actualizar Review` | `PUT /api/reviews/{id}` | `200 OK` |
| **HU-VIA-010** | Eliminar Reseña Propia | `DELETE` | `/api/reviews/{id}` | `via_itinerarios_reviews.feature` | `VIA-21 Eliminar Review` | `DELETE /api/reviews/{id}` | `204 No Content` |

---

## 2. Registro histórico de defectos del SUT inicial

Los siguientes hallazgos corresponden a la versión del SUT evaluada inicialmente. Las etapas posteriores incorporan correcciones y ampliaciones; esta sección se conserva como registro histórico de la evaluación original.

### Defecto DEF-001: Inconsistencia de nombre de columna en cálculo de importe de Reserva de Tour
- **Severidad:** Mayor (Funcional / Negocio).
- **Ubicación:** `app/Http/Controllers/Api/ReservaTourController.php` (Línea 45).
- **Descripción:** El controlador invoca `$precioUnit = (float)$serv->tour->precio_persona;`. Sin embargo, el modelo `Tour.php` y la migración `create_tours_table.php` definen la columna como `precio`.
- **Efecto Observado:** El backend evalúa `$precioUnit` a `0.0`, persistiendo reservas de tour con `precio_unitario: 0` y `total: 0.0` a pesar de que el tour posea una tarifa positiva.
- **Acción QA:** Las aserciones automatizadas de Karate y Postman documentan y validan la respuesta real del servidor sin romper la ejecución, reportando la discrepancia financiera.

### Defecto DEF-002: Endpoint de decisión manual de reserva de hotel no expuesto en rutas API
- **Severidad:** Media (Brecha de Integración).
- **Ubicación:** `routes/api.php` y `app/Http/Controllers/Api/ReservaHabitacionController.php`.
- **Descripción:** Para la historia `HU-PRO-009`, el requerimiento funcional de negocio especifica que el proveedor puede aceptar o rechazar solicitudes pendientes de hotel. En la implementación actual, solo existe `POST /api/reservas-habitaciones/{id}/cancelar` (para el viajero) y la consulta agrupada `GET /api/proveedor/servicios/{id}/reservas`.
- **Acción QA:** Se valida la consulta de auditoría de reservas del proveedor y se reporta la necesidad del endpoint transaccional mutador para el proveedor.

### Defecto DEF-003: Eliminación directa de hotel deja huérfano el registro en tabla servicios
- **Severidad:** Menor (Integridad Referencial de Modelo).
- **Ubicación:** `app/Http/Controllers/Api/HotelController.php` (Método `destroy`).
- **Descripción:** `DELETE /api/hoteles/{servicio_id}` ejecuta `$hotel->delete();` eliminando la fila en `hoteles`, pero conserva activa la fila padre en `servicios`. En contraste, `DELETE /api/servicios/{id}` ejecuta la eliminación en cascada completa.
- **Acción QA:** Se diseñaron casos diferenciados para probar la ruta recomendada en cascada (`DELETE /api/servicios/{id}`) y la ruta específica (`DELETE /api/hoteles/{id}`).
