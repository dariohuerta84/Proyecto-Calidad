# Registro de defectos

[← Documentación](../README.md) · [Matriz de trazabilidad](matriz-de-trazabilidad.md)

Defectos encontrados al auditar el sistema y al ejecutar las suites. Cada uno indica el criterio que incumplía y el caso
que lo detecta, así queda cubierto por la regresión.

| Id | Resumen | Severidad | Componente | Criterio · caso | Etapa | Estado |
|---|---|---|---|---|---|---|
| DEF-001 | El total de las reservas de tour se guardaba en S/ 0.00 | Mayor | Backend | CA-V10-01 · TC-API-041 | Auditoría inicial | Corregido (etapa 2) |
| DEF-002 | No existían endpoints para que el proveedor acepte o rechace solicitudes de hotel | Mayor | Backend | CA-P13-01, CA-P14-01 · TC-API-031, TC-API-032 | Auditoría inicial | Corregido (etapa 2) |
| DEF-003 | `DELETE /api/hoteles/{id}` dejaba huérfano el registro en `servicios` | Menor | Backend | CA-P15-02 · TC-WEB-030 | Auditoría inicial | Corregido (verificado en el código actual) |
| DEF-004 | Cualquier usuario con sesión podía eliminar cualquier servicio | Crítica | Backend | CA-P15-04 · TC-API-076 | Etapa 2 | Corregido (etapa 2) |
| DEF-005 | Borrar un servicio dejaba reservas huérfanas y «Mis reservas» respondía 500 | Mayor | Backend | CA-P15-03 · TC-API-078 | Etapa 2 | Corregido (etapa 2) |
| DEF-006 | Editar un tour borraba y recreaba sus salidas (ids nuevos, error 500) | Mayor | Backend | CA-P09-01 · TC-API-066 | Etapa 2 | Corregido (etapa 2) |
| DEF-007 | Editar un hotel borraba habitaciones que tenían reservas | Mayor | Backend | CA-P06-01, CA-P07-02 · TC-API-083, TC-API-062 | Etapa 2 | Corregido (etapa 2) |
| DEF-008 | La reserva de hotel no bloqueaba filas: dos solicitudes podían vender la misma habitación | Crítica | Backend | CA-V07-05 · TC-PERF-003 | Etapa 2 | Corregido (etapa 2) |
| DEF-009 | La disponibilidad sumaba reservas aunque no se cruzaran en fechas | Mayor | Backend | CA-V05-01, CA-V05-02 · TC-API-020, TC-API-022 | Etapa 2 | Corregido (etapa 2) |
| DEF-010 | `GET /tours/{id}/salidas` devolvía salidas de todos los tours | Media | Backend | CA-V09-01, CA-P11-06 · TC-API-038 | Etapa 2 | Corregido (etapa 2) |
| DEF-011 | Una actividad sin `orden` chocaba con el índice único (500) | Media | Backend | CA-P10-02 · TC-API-068 | Etapa 2 | Corregido (etapa 2) |
| DEF-012 | `GET /api/usuarios` y `/api/eliminados` eran públicos (exponían correos y datos borrados) | Crítica | Backend | — (seguridad) | Etapa 2 | Corregido (etapa 2): rutas eliminadas |
| DEF-013 | El middleware JWT escribía un log en cada petición | Menor | Backend | CA-V02-07 · TC-PERF-001 | Etapa 2 | Corregido (etapa 2) |
| DEF-014 | Microservicio: no exigía el JWT, tenía columnas inexistentes, no filtraba eliminados y registraba parte de la clave | Mayor | Microservicio | CA-V15-01 · TC-API-054 | Etapa 2 | Corregido (etapa 2) |
| DEF-015 | Algunos comentarios y documentos de la web citaban historias equivocadas | Trivial | Web | — (trazabilidad) | Etapa 4 | Corregido (etapa 4) |
| DEF-016 | La lista de tours mostraba «No se encontraron tours» también cuando la API fallaba, y pedía los tours dos veces | Media | Web | CA-V08-03 · TC-WEB-032 | Etapa 5 | Corregido (etapa 5) |
| DEF-017 | Con la sesión vencida, una página privada quedaba en «No autenticado» en vez de llevar al login | Media | Web | CA-V02-04 · TC-WEB-033 | Etapa 5 | Corregido (etapa 5) |
| DEF-018 | La app dejaba pulsar «Registrarme» y «Guardar contraseña» con la confirmación distinta | Menor | Móvil | CA-V01-03, CA-V03-04 · TC-MOB-002, TC-MOB-007 | Etapa 5 | Corregido (etapa 5) |
| DEF-019 | El límite de registros por minuto era fijo y no se podía subir en el entorno de pruebas | Menor | Backend | — (capacidad de prueba) | Etapa 5 | Corregido (etapa 5) |
| DEF-020 | El título del reporte HTML de JMeter cita la tarea TRV-604 en vez de la de rendimiento (TRV-602, ahora QA-02) | Trivial | QA | — (reporte) | Revisión de documentación | Abierto |
| DEF-021 | La matriz de `qa/trazabilidad/salida/` muestra el criterio CA-TRV-102-04 sin título en los casos que se le vincularon después | Trivial | QA | — (reporte) | Revisión de documentación | Abierto |

**Severidad:** Crítica (bloquea el uso o pierde datos) · Mayor (una regla de negocio falla) · Media (la función sirve,
pero de forma incorrecta o confusa) · Menor (detalle con alternativa) · Trivial (texto o reporte).

## Detalle

### DEF-001 · El total de las reservas de tour se guardaba en S/ 0.00

- **Dónde:** `app/Http/Controllers/Api/ReservaTourController.php`.
- **Causa:** se leía `$serv->tour->precio_persona`, pero la columna del modelo y de la migración se llama `precio`.
- **Efecto:** las reservas de tour quedaban con `precio_unitario = 0` y `total = 0.00` aunque el tour tuviera precio.
- **Corrección:** se usa `$serv->tour->precio` y el total se calcula en el backend con IGV. TC-API-041 compara el total
  guardado con la cotización.

### DEF-002 · No había forma de aceptar o rechazar solicitudes de hotel

- **Dónde:** `routes/api.php` y `ReservaHabitacionController`.
- **Causa:** solo existían la cancelación del viajero y la consulta de reservas del proveedor.
- **Corrección:** `POST /api/proveedor/reservas-habitaciones/{id}/aceptar` y `…/rechazar {motivo}`, con control de dueño,
  de estado (409) y bloqueo de fila. Cubierto por HU-P13 y HU-P14 (TC-API-031 a TC-API-035).

### DEF-003 · Eliminar un hotel dejaba el servicio activo

- **Dónde:** `HotelController::destroy`.
- **Causa:** borraba la fila de `hoteles` pero no la de `servicios`, a diferencia de `DELETE /api/servicios/{id}`.
- **Corrección:** ambas rutas usan el mismo servicio `EliminacionServicio` (borrado lógico, bloqueo si hay reservas
  activas). Cubierto por HU-P15.

### DEF-004 a DEF-014 · Correcciones de la etapa 2

Encontrados al revisar el backend y el microservicio antes de integrarlos con la web. Antes y después de cada uno en
[`backend-laravel/docs/CAMBIOS_ETAPA2.md`](../../backend-laravel/docs/CAMBIOS_ETAPA2.md). Los casos de la columna
«Criterio · caso» fallarían si alguno reapareciera.

### DEF-015 · Referencias a historias equivocadas en la web

Corrección solo de comentarios y documentos (reseñas, itinerario, mis reservas y eliminar cuenta citaban otro código).
No cambió ningún comportamiento: reseñas citaba TRV-401..404 (correcto TRV-403), itinerario TRV-205 (TRV-404), mis reservas
y cancelar TRV-206/TRV-304 (TRV-401/402) y eliminar cuenta estaba dentro de TRV-104 (TRV-105). Hoy esas historias son
HU-V14, HU-V15, HU-V11/HU-V13 y HU-V04.

### DEF-016 · Tours: «sin resultados» y «error» se veían igual

- **Pasos:** detener el backend → buscar tours en la web.
- **Esperado (CA-V08-03):** un mensaje de error con «Reintentar», distinto de «No se encontraron tours».
- **Obtenido:** «No se encontraron tours». Además la página hacía dos peticiones por búsqueda.
- **Corrección:** `features/tours/components/resultados/` con estados de carga, error con Reintentar y vacío, y una sola
  suscripción. Lo verifica TC-WEB-032.

### DEF-017 · La sesión vencida no llevaba al login

- **Pasos:** abrir la web con un token vencido en una página privada.
- **Esperado (CA-V02-04):** 401, limpiar la sesión local, ir al login y volver a la misma página al entrar.
- **Obtenido:** `/auth/me` limpiaba la sesión y la página quedaba con «No autenticado».
- **Corrección:** `core/interceptors/auth.interceptor.ts` redirige al login con `returnUrl` cuando llega un 401 sin
  sesión local. Lo verifica TC-WEB-033.

### DEF-018 · La app permitía enviar con la confirmación distinta

- **Esperado (CA-V01-03, CA-V03-04):** el botón queda deshabilitado mientras la confirmación no coincida y el mensaje
  aparece al escribir.
- **Corrección:** `AuthScreens.kt` y `ProfileScreen.kt`. Lo verifican TC-MOB-002 y TC-MOB-007.

### DEF-019 · Límite de registros no configurable

- **Efecto:** las suites crean muchas cuentas y recibían 429 a los pocos minutos.
- **Corrección:** limitador `throttle:registro` con `REGISTER_RATE_LIMIT` (por defecto 10 por minuto). En producción no
  cambia nada.

### DEF-020 y DEF-021 · Detalles de los reportes de QA

Se corrigen al actualizar las etiquetas de `qa/` a los nuevos identificadores de historia (ver
[equivalencias](../1-historias-de-usuario/equivalencias.md)). No afectan ningún resultado.
