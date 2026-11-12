# Etapa 2 — Cambios en el backend

Cada cambio indica la historia de usuario (HU-xxx) que habilita.

## Defectos corregidos

| # | Antes | Ahora | HU |
| --- | --- | --- | --- |
| 1 | `DELETE /api/servicios/{id}` lo podía ejecutar cualquier usuario con sesión | Solo el proveedor dueño (403), bloqueado con reservas activas (409), borrado lógico | HU-P15 |
| 2 | Borrar un servicio dejaba reservas huérfanas y `GET /mis-reservas` respondía 500 | Las reservas conservan servicio y habitación (relaciones `withTrashed`) | HU-V11, HU-P15 |
| 3 | El total de reservas de tour se guardaba en 0 (`precio_persona` no existe) | Usa `precio`; se guardan precio unitario, subtotal, IGV y total | HU-V10 |
| 4 | Editar un tour borraba y recreaba sus salidas (ids nuevos, error 500 por índice único) | Editar un tour no toca las salidas; se gestionan en `/tours/{id}/salidas` | HU-P09, HU-P11 |
| 5 | Editar un hotel borraba habitaciones con reservas | 409 si se omite una habitación con reservas activas o se bajan unidades comprometidas | HU-P06, HU-P07 |
| 6 | La reserva de hotel no bloqueaba filas: dos solicitudes podían sobrevender | `lockForUpdate` sobre las habitaciones dentro de una transacción | HU-V07 |
| 7 | La disponibilidad sumaba reservas aunque no se cruzaran entre sí | Ocupación por noche (máximo en cualquier noche del rango) | HU-V05, HU-V07 |
| 8 | `GET /tours/{id}/salidas` devolvía salidas de todos los tours | Filtra por el tour | HU-P11 |
| 9 | Actividad sin `orden` chocaba con el índice único (500) | Se agrega al final; insertar en un orden ocupado desplaza a las siguientes | HU-P10 |
| 10 | `GET /api/usuarios` y `/api/eliminados` eran públicos (exponían correos y datos borrados) | Eliminados | Seguridad |
| 11 | El middleware JWT escribía un log en cada petición | Eliminado (afectaba el rendimiento en JMeter) | QA-02 |
| 12 | Microservicio: la autenticación JWT no se aplicaba, el modelo `tours` tenía columnas inexistentes, no filtraba eliminados y registraba parte de la clave secreta | JWT obligatorio (401), modelos alineados a Laravel, filtros `deleted_at`, sin secretos en logs | HU-V15 |
| 13 | Microservicio: la red Docker dependía del nombre de la carpeta del backend | Red con nombre fijo `trouvels360_network` (desde la carpeta `backend/` ya no hace falta: las dos APIs se comunican por URL) | HU-V15 |

## Funcionalidad nueva

| Funcionalidad | Endpoints | HU |
| --- | --- | --- |
| Estado `rechazada` y decisión del proveedor (quién y cuándo) | `POST /proveedor/reservas-habitaciones/{id}/aceptar` · `/rechazar` | HU-P13, HU-P14 |
| IGV 18% calculado en céntimos (half-up) y guardado: subtotal, impuesto, total | `POST /reservas-habitaciones/cotizar` · `/tours/salidas/{s}/cotizar` | HU-V06, HU-V09 |
| Pago simulado validado en el servidor (Luhn, vencimiento, CVC); solo se guardan los 4 últimos dígitos | reservas de hotel y tour | HU-V07, HU-V10 |
| Varios tipos de habitación en una sola solicitud transaccional (todos o ninguno) | `POST /reservas-habitaciones` | HU-V07 |
| Idempotencia con `Idempotency-Key` | reservas de hotel y tour | HU-V07, HU-V10 |
| Búsquedas validadas y paginadas, sin distinguir tildes | `GET /hoteles`, `GET /tours` | HU-V05, HU-V08 |
| Mis reservas paginado, con filtro por estado, detalle y aviso de salida cancelada | `GET /mis-reservas`, `/mis-reservas/{id}`, `/tours/mis-reservas` | HU-V11 |
| Cancelación con reglas de estado y fecha | `.../cancelar` | HU-V13 |
| Reseñas: causa exacta del 403, mínimo 10 caracteres, una por servicio, resumen del total | `/reviews`, `/reviews/resumen`, `/reviews/puedo-opinar` | HU-V14 |
| Perfil con avatar, cambio de contraseña y eliminación con bloqueo | `/usuarios/me`, `/usuarios/me/password` | HU-V03, HU-V04 |
| Registro con confirmación de contraseña y teléfono normalizado | `/auth/register` | HU-V01, HU-P01 |
| Bandeja de reservas e indicadores del proveedor | `/proveedor/reservas`, `/proveedor/indicadores` | HU-P04, HU-P12 |
| Límite de intentos de login (6/min por correo + IP) | `/auth/login` | HU-V02 |
| Errores siempre en JSON y en español | toda la API | — |
| Datos fijos para las suites (QaSeeder) | — | QA-01 a QA-04 |

## Verificación

- **Laravel:** 60 pruebas de integración (Pest) sobre la API, una o más por criterio de aceptación: `php artisan test`.
- **Microservicio:** 9 pruebas (pytest): autenticación, validaciones, filtros y armado del itinerario.
- **Integración real:** Laravel → Python con JWT de servicio probado de punta a punta.

## Cambios que afectan a la web y a la app móvil (etapas 3 y 4)

- `POST /reservas-habitaciones` ahora exige `pago` e `Idempotency-Key` (acepta `items[]` o el formato anterior `habitacion_id` + `cantidad`).
- `POST /tours/salidas/{s}/reservas` exige `pago` e `Idempotency-Key`.
- `GET /mis-reservas` ahora es paginado: `{data: [...], meta: {...}}`.
- `POST /auth/register` exige `password_confirmation` y responde 201.
- `PATCH /usuarios/me` ya no cambia la contraseña: usar `PUT /usuarios/me/password`.
- Crear hotel exige al menos una habitación en el mismo envío.
