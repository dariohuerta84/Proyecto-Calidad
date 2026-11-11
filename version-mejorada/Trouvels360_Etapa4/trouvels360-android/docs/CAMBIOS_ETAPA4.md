# Etapa 4 — App móvil (Kotlin + Compose)

Objetivo: que la app tenga las mismas historias que la web (viajero **y** proveedor), use la API de las
etapas 2 y 3 sin atajos, y quede lista para Appium.

## 1. Qué cambió respecto al port inicial

| Antes (port básico) | Ahora |
|---|---|
| Solo viajero | Viajero y proveedor, con barra inferior según el rol |
| Reservaba una habitación sin pagar, contra la API antigua | Selección de varias habitaciones → cotización oficial (IGV 18 %) → pago simulado → solicitud **PENDIENTE** |
| Reserva de tour sin pago | Cotización por personas → pago → **CONFIRMADA** |
| Listas sin paginar | Paginación en hoteles, tours, reservas, reseñas, panel y reservas recibidas |
| Calificaciones calculadas en la app | Promedio y cantidad reales de la API ("Sin reseñas" si no hay) |
| Sin reseñas, sin itinerario, sin perfil editable | Reseñas (publicar/editar/eliminar), itinerario sugerido, mi cuenta completa |
| Sin manejo de expiración | Refresco automático del JWT; si no se puede, login con aviso y vuelta a la pantalla |
| Errores genéricos | Mensaje de la API y errores por campo bajo cada input |
| Log de red con cuerpos | Solo método/URL/código (sin contraseñas ni tarjetas) |
| Sin selectores de prueba | 267 `testTag` expuestos como resource-id, también en diálogos |
| Sin pruebas | 20 pruebas unitarias JVM (validaciones y contrato con la API) |

## 2. Pantallas nuevas del proveedor

- **Panel** (TRV-501): indicadores (por decidir, confirmadas, rechazadas, canceladas, ingresos del mes,
  calificación), publicaciones paginadas con filtro por tipo y nombre, conteos por estado.
- **Reservas recibidas** (TRV-507, TRV-204): filtros por tipo, estado y publicación; aceptar; rechazar con
  motivo de 10–300 caracteres (se valida en la app y en la API).
- **Hotel** (TRV-502, TRV-503): publicar con sus tipos de habitación en un solo envío; al editar, cada tipo
  se agrega, edita o quita por separado (la API responde 409 si tiene reservas activas).
- **Tour** (TRV-504): publicar con salidas iniciales; al editar no se tocan las salidas.
- **Salidas y actividades** (TRV-505, TRV-506): programar, editar cupo (no por debajo de lo reservado),
  cerrar, reabrir, cancelar y eliminar salidas; actividades con orden, subir/bajar.
- **Eliminar publicación** (TRV-508): confirmación y mensaje del 409.

## 3. Backend

No se cambió nada para el móvil: usa la misma sesión que la web (cookie HttpOnly + CSRF) y los mismos
endpoints. Los ajustes que salieron al probar la web y el itinerario ya están en el zip de la etapa 3.

## 4. Verificación realizada

| Verificación | Resultado |
|---|---|
| Cada endpoint de `ApiService.kt` existe en `php artisan route:list` | 52 de 52 |
| Respuestas reales del backend → modelos de la app (`ContratoApiTest`) | 11 de 11 |
| Reglas de validación (`ValidacionesTest`) | 9 de 9 |
| Compilación de todo el código Kotlin con `kotlinc 2.0.21` (datos con Gson real; UI contra las firmas de Compose/Material 3) | Sin errores |

**Límite honesto:** en el entorno donde se preparó esta etapa no hay SDK de Android ni acceso a los
repositorios de Google, así que no se pudo generar el APK ni abrir la app en un emulador. El código se
compiló con kotlinc usando las firmas de las librerías, pero la **primera compilación real se hace en
Android Studio**. Si aparece cualquier error al sincronizar o compilar, basta con copiar el mensaje para corregirlo.

## 5. Cómo probar a mano (5 minutos)

1. Viajero: Cuenta → Iniciar sesión → Hoteles → Cusco + fechas → hotel → +1 habitación → Reservar →
   "No, ir al pago" → Confirmar y pagar con `4242 4242 4242 4242` → comprobante **PENDIENTE**.
2. Cerrar sesión → proveedor (`edwinproveedor@gmail.com`) → Recibidas → filtro Pendiente → Aceptar.
3. Volver como viajero → Reservas → la reserva aparece **CONFIRMADA**.
4. Repetir con Rechazar (motivo corto → error; motivo válido → el viajero ve el motivo).
