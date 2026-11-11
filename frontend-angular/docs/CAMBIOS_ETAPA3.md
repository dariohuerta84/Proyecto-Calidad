# Etapa 3 — Cambios en la web Angular

Objetivo: que la web use la API de la etapa 2 como única fuente de verdad, cubra todas las HU de viajero y
proveedor, y quede preparada para Selenium (selectores estables, sin diálogos nativos del navegador).

## 1. Correcciones generales

| Problema anterior | Corrección |
|---|---|
| Las reservas se guardaban en el navegador (servicio local) y podían no existir en la base | Eliminado. Reservas, cotización, cancelación y estados vienen de la API |
| Importes calculados en el navegador, sin IGV | `/cotizar` de la API: subtotal + IGV 18 % = total, el mismo que se guarda |
| Calificaciones y número de reseñas inventados en los resultados | Promedio y cantidad reales (`calificacion` de la API); "Sin reseñas" si no hay |
| Itinerario "de prueba" con actividades fijas en los resultados | Eliminado; el itinerario real (microservicio Python) se ofrece al reservar |
| URL `http://localhost:8000` fija en servicios | Todo usa `environment.apiUrl` (`/api` + proxy) |
| `HttpClientModule` importado en componentes (creaba un cliente sin interceptor) | Eliminado; se usa el `HttpClient` global con interceptor |
| `alert()` / `confirm()` del navegador | Avisos y modales en el DOM con `data-testid` |
| Servicio que listaba usuarios por el endpoint público `/usuarios` | Eliminado (el endpoint ya no existe) |
| `console.log` con payloads y `window.alert` de depuración en formularios | Eliminados |
| Interceptor sin CSRF ni manejo de sesión expirada | CSRF doble envío, reintento, refresh y regreso al login con `returnUrl` |
| Registro sin `password_confirmation` | Se envía; `?rol=proveedor` preselecciona la cuenta de proveedor |
| Build de producción fallaba (prerender con parámetros, fuentes externas, presupuestos) | `RenderMode.Client`, `fonts:false`, presupuestos ajustados |

## 2. Viajero

- **Búsqueda (TRV-201):** paginada (10 por página); distingue "sin resultados" de "error" con *Reintentar*.
- **Detalle de hotel (TRV-202):** unidades libres reales para las fechas elegidas; al cambiar fechas se recalcula.
- **Pago de hotel (TRV-203):** cotización oficial, formulario de pago simulado (Luhn, vencimiento, CVC),
  `Idempotency-Key`, comprobante con estado **PENDIENTE**.
- **Pago de tour (TRV-302/303):** cotización por personas, comprobante **CONFIRMADA**.
- **Mis reservas (TRV-401/402):** pestañas hotel/tour, filtro por estado, paginación, cancelación con
  confirmación, **motivo del rechazo** visible.
- **Reseñas (TRV-403):** resumen real (promedio y total), lista paginada, botón solo si
  `/reviews/puedo-opinar` lo permite (si no, muestra el motivo), editar y eliminar la propia, 10–300 caracteres.
- **Mi cuenta (TRV-104, TRV-105):** edita solo campos permitidos por rol (nunca rol ni contraseña por PATCH),
  foto de perfil (`avatar_url`), cambio de contraseña con la actual, eliminación con aviso 409 si hay reservas activas.

## 3. Proveedor

- **Panel (TRV-501):** indicadores desde `/proveedor/indicadores` (pendientes, confirmadas, rechazadas,
  canceladas, ingresos del mes, calificación), publicaciones paginadas (15) con filtro por tipo y nombre,
  conteos por estado y reseñas en una sola consulta (sin N+1).
- **Reservas recibidas (TRV-507/204) — nueva:** filtros por tipo, estado y publicación; pendientes primero;
  **Aceptar**; **Rechazar** con motivo de 10 a 300 caracteres.
- **Salidas y actividades (TRV-505/506) — nueva:** programar, editar, cerrar, reabrir, cancelar y eliminar
  salidas (409 si está duplicada o tiene reservas); actividades con orden, subir/bajar.
- **Crear hotel (TRV-502):** un solo POST con las habitaciones; imagen principal obligatoria (URL http/https).
- **Crear/editar tour (TRV-504):** el formulario de creación pide fecha, hora y cupo (sin cupo reservado ni
  estado); la edición ya no reenvía salidas (se gestionan en su pantalla, sin tocar reservas confirmadas).
- **Eliminar (TRV-508):** modal de confirmación; muestra el 409/403 de la API.
- Todas las rutas `/proveedor/**` exigen sesión y rol proveedor.

## 4. Ajustes en el backend (incluido actualizado en este paquete)

| Archivo | Cambio |
|---|---|
| `TourActividadController.php` | Mover una actividad de orden dejaba huecos y no cambiaba la posición; ahora reordena correctamente |
| `ServicioController.php` | `habitaciones_count` y `salidas_count` salían en 0 en el panel (un `select()` pisaba los `withCount`) |
| `ReservaTourFactory.php` | Generaba reservas de tour "pendiente" (no existen: se confirman al pagar); ahora con IGV y tarjeta |
| Migración `2026_11_10_000001` | Convierte reservas de tour antiguas "pendiente" a "confirmada" |
| `tests/Feature/ProveedorTest.php` | 2 pruebas nuevas (reordenar actividades, conteo de tipos de habitación) |

Resultado: **61 pruebas Pest en verde** (333 aserciones).

## 5. Ajustes en el microservicio Python (incluido actualizado en este paquete)

Al probar el itinerario de punta a punta (web → Laravel → FastAPI) aparecieron dos fallas de lógica:

| Problema | Corrección |
|---|---|
| Proponía tours en días en que **no tienen salida** (o con la salida llena, cerrada o cancelada), así que el viajero no podía reservarlos | Solo propone tours con salida programada y cupo durante la estadía, en el día exacto de esa salida, e informa `salida_id`, hora y cupos. La web lo muestra ("Sale vie 20 nov a las 09:00 · 10 cupos · Ver tour") |
| Los tours de día completo (Machu Picchu, Vinicunca: 12–14 h) **nunca se proponían** porque no cabían en 8 h, y la duración se calculaba sumando actividades (Machu Picchu salía con 4,6 h) | Usa la duración declarada del tour; un tour de más de 8 h ocupa un día vacío completo |

Si no hay salidas en las fechas elegidas, responde 404 con un mensaje claro que la web muestra en el modal.
Pruebas del microservicio: **13 en verde** (4 nuevas).

## 6. Verificación realizada

- `ng build` de producción sin errores.
- Recorridos de extremo a extremo en navegador (Chromium) contra la API real:
  1. Búsqueda Cusco con fechas → resultados → detalle con unidades libres; ciudad sin hoteles → mensaje vacío.
  2. Hotel: login exigido al pagar (vuelve con `returnUrl`), tarjeta inválida rechazada, total S/ 708.00 (600 + 108 IGV), estado PENDIENTE.
  3. Tour: 2 personas → S/ 240.00 + S/ 43.20 = S/ 283.20, CONFIRMADA; cancelación desde Mis reservas.
  4. Proveedor: indicadores, eliminar con reservas activas (409), aceptar y rechazar (motivo corto rechazado),
     el viajero ve el motivo; nueva salida, salida duplicada (409), actividad insertada en orden 1 y movida al 2.
  5. Itinerario real con los tres servicios levantados: solo sugiere tours en días con salida, muestra hora y cupos, y no suma nada al pago del hotel.
  6. Reseñas: comentario corto rechazado, publicar, editar y eliminar la propia.
  7. Perfil: actualización de nombre (se refleja en la cabecera), contraseña actual incorrecta, eliminar cuenta con reservas activas (409).
- Ningún error de JavaScript ni diálogo nativo durante los recorridos.
