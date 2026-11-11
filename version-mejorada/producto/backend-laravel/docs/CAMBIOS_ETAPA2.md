# Etapa 2 — Cambios en el backend

Cada cambio indica la historia de usuario (TRV-xxx) que habilita.

## Defectos corregidos

| # | Antes | Ahora | HU |
| --- | --- | --- | --- |
| 1 | `DELETE /api/servicios/{id}` lo podía ejecutar cualquier usuario con sesión | Solo el proveedor dueño (403), bloqueado con reservas activas (409), borrado lógico | TRV-508 |
| 2 | Borrar un servicio dejaba reservas huérfanas y `GET /mis-reservas` respondía 500 | Las reservas conservan servicio y habitación (relaciones `withTrashed`) | TRV-401, TRV-508 |
| 3 | El total de reservas de tour se guardaba en 0 (`precio_persona` no existe) | Usa `precio`; se guardan precio unitario, subtotal, IGV y total | TRV-303 |
| 4 | Editar un tour borraba y recreaba sus salidas (ids nuevos, error 500 por índice único) | Editar un tour no toca las salidas; se gestionan en `/tours/{id}/salidas` | TRV-504, TRV-506 |
| 5 | Editar un hotel borraba habitaciones con reservas | 409 si se omite una habitación con reservas activas o se bajan unidades comprometidas | TRV-502, TRV-503 |
| 6 | La reserva de hotel no bloqueaba filas: dos solicitudes podían sobrevender | `lockForUpdate` sobre las habitaciones dentro de una transacción | TRV-203 |
| 7 | La disponibilidad sumaba reservas aunque no se cruzaran entre sí | Ocupación por noche (máximo en cualquier noche del rango) | TRV-201, TRV-203 |
| 8 | `GET /tours/{id}/salidas` devolvía salidas de todos los tours | Filtra por el tour | TRV-506 |
| 9 | Actividad sin `orden` chocaba con el índice único (500) | Se agrega al final; insertar en un orden ocupado desplaza a las siguientes | TRV-505 |
| 10 | `GET /api/usuarios` y `/api/eliminados` eran públicos (exponían correos y datos borrados) | Eliminados | Seguridad |
| 11 | El middleware JWT escribía un log en cada petición | Eliminado (afectaba el rendimiento en JMeter) | TRV-602 |
| 12 | Microservicio: la autenticación JWT no se aplicaba, el modelo `tours` tenía columnas inexistentes, no filtraba eliminados y registraba parte de la clave secreta | JWT obligatorio (401), modelos alineados a Laravel, filtros `deleted_at`, sin secretos en logs | TRV-404 |
| 13 | Microservicio: la red Docker dependía del nombre de la carpeta del backend | Red con nombre fijo `trouvels360_network` | TRV-404 |

## Funcionalidad nueva

| Funcionalidad | Endpoints | HU |
| --- | --- | --- |
| Estado `rechazada` y decisión del proveedor (quién y cuándo) | `POST /proveedor/reservas-habitaciones/{id}/aceptar` · `/rechazar` | TRV-204 |
| IGV 18% calculado en céntimos (half-up) y guardado: subtotal, impuesto, total | `POST /reservas-habitaciones/cotizar` · `/tours/salidas/{s}/cotizar` | TRV-202, TRV-302 |
| Pago simulado validado en el servidor (Luhn, vencimiento, CVC); solo se guardan los 4 últimos dígitos | reservas de hotel y tour | TRV-203, TRV-303 |
| Varios tipos de habitación en una sola solicitud transaccional (todos o ninguno) | `POST /reservas-habitaciones` | TRV-203 |
| Idempotencia con `Idempotency-Key` | reservas de hotel y tour | TRV-203, TRV-303 |
| Búsquedas validadas y paginadas, sin distinguir tildes | `GET /hoteles`, `GET /tours` | TRV-201, TRV-301 |
| Mis reservas paginado, con filtro por estado, detalle y aviso de salida cancelada | `GET /mis-reservas`, `/mis-reservas/{id}`, `/tours/mis-reservas` | TRV-401 |
| Cancelación con reglas de estado y fecha | `.../cancelar` | TRV-402 |
| Reseñas: causa exacta del 403, mínimo 10 caracteres, una por servicio, resumen del total | `/reviews`, `/reviews/resumen`, `/reviews/puedo-opinar` | TRV-403 |
| Perfil con avatar, cambio de contraseña y eliminación con bloqueo | `/usuarios/me`, `/usuarios/me/password` | TRV-104, TRV-105 |
| Registro con confirmación de contraseña y teléfono normalizado | `/auth/register` | TRV-101, TRV-102 |
| Bandeja de reservas e indicadores del proveedor | `/proveedor/reservas`, `/proveedor/indicadores` | TRV-501, TRV-507 |
| Límite de intentos de login (6/min por correo + IP) | `/auth/login` | TRV-103 |
| Errores siempre en JSON y en español | toda la API | — |
| Datos fijos para las suites (QaSeeder) | — | TRV-601 a TRV-604 |

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
