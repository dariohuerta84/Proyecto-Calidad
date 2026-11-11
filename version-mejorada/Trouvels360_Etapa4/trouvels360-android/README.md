# Trouvels360 — App Android (Kotlin + Jetpack Compose)

App nativa con **las mismas historias que la web** (viajero y proveedor), sobre el mismo backend Laravel
y el microservicio Python de itinerarios. La API es la única fuente de verdad: la app no guarda reservas
ni calcula importes por su cuenta.

## Qué cubre

| Rol | Pantalla | Historia | Endpoints |
|---|---|---|---|
| Todos | Login (vuelve a la pantalla que lo pidió; aviso si la sesión expiró) | TRV-103 | `auth/csrf`, `auth/login`, `auth/refresh`, `auth/logout` |
| Todos | Registro de viajero o proveedor (RUC, celular +51) | TRV-101, TRV-102 | `auth/register` |
| Todos | Mi cuenta: datos por rol, foto (URL), contraseña, eliminar cuenta | TRV-104, TRV-105 | `usuarios/me`, `usuarios/me/password` |
| Viajero | Búsqueda de hoteles por ciudad, fechas y huéspedes (paginada) | TRV-201 | `GET hoteles` |
| Viajero | Detalle: unidades libres por fechas, selección de habitaciones | TRV-202 | `hoteles/{id}`, `hoteles/{id}/disponibilidad` |
| Viajero | Itinerario sugerido opcional (solo tours con salida en tu estadía) | TRV-404 | `POST itineraries` → FastAPI |
| Viajero | Pago: cotización oficial con IGV 18 %, pago simulado, comprobante **PENDIENTE** | TRV-203 | `reservas-habitaciones/cotizar`, `POST reservas-habitaciones` + `Idempotency-Key` |
| Viajero | Tours por destino, categoría, fechas y personas | TRV-301 | `GET tours` |
| Viajero | Detalle del tour: actividades, salidas con cupos, personas | TRV-302 | `tours/{id}` |
| Viajero | Pago de tour, comprobante **CONFIRMADA** | TRV-303 | `tours/salidas/{s}/cotizar`, `.../reservas` |
| Viajero | Mis reservas (hotel/tour, filtro por estado, motivo de rechazo) y cancelar | TRV-401, TRV-402 | `mis-reservas`, `tours/mis-reservas`, `.../cancelar` |
| Viajero | Reseñas: resumen real, paginación, publicar/editar/eliminar la propia | TRV-403 | `reviews`, `reviews/resumen`, `reviews/puedo-opinar` |
| Proveedor | Panel: indicadores por estado, ingresos, publicaciones paginadas | TRV-501 | `proveedor/indicadores`, `proveedor/servicios` |
| Proveedor | Reservas recibidas: filtros, **aceptar** y **rechazar con motivo** | TRV-507, TRV-204 | `proveedor/reservas`, `.../aceptar`, `.../rechazar` |
| Proveedor | Publicar/editar hotel (con tipos de habitación) | TRV-502, TRV-503 | `hoteles`, `hoteles/{id}/habitaciones`, `habitaciones/{id}` |
| Proveedor | Publicar/editar tour (con salidas iniciales) | TRV-504 | `tours`, `tours/{id}` |
| Proveedor | Salidas (cupo, cerrar, reabrir, cancelar) y actividades ordenadas | TRV-505, TRV-506 | `tours/{id}/salidas`, `tours/{id}/actividades` |
| Proveedor | Eliminar publicación (muestra el 409 si tiene reservas activas) | TRV-508 | `DELETE servicios/{id}` |

La barra inferior cambia según el rol: **viajero/visitante** → Hoteles · Tours · Reservas · Cuenta;
**proveedor** → Panel · Recibidas · Cuenta.

## Ejecutar

1. Levanta el backend Laravel (puerto 8000) y el microservicio de itinerarios (etapa 3).
2. Abre esta carpeta en **Android Studio** (Ladybug o más reciente) y deja que Gradle sincronice.
3. URL de la API en `gradle.properties`:
   ```properties
   API_BASE_URL=http://10.0.2.2:8000/api/   # emulador → localhost de tu PC
   ```
   En un celular físico usa la IP de tu PC (`http://192.168.x.x:8000/api/`) y agrégala en
   `app/src/main/res/xml/network_security_config.xml`.
4. Run ▶ en un emulador con API 26 o superior.

Usuarios: viajero `edwinviajero@gmail.com` / `viajero` · proveedor `edwinproveedor@gmail.com` / `proveedor`.
Tarjeta de prueba: `4242 4242 4242 4242`, `12/30`, CVC `123` (una que no pasa Luhn, como `…4241`, se rechaza).

## Sesión y seguridad

- La sesión es la misma que en la web: cookie HttpOnly `access_token` + CSRF de doble envío.
  `PersistentCookieJar` guarda las cookies; antes de cada POST/PUT/PATCH/DELETE se pide `auth/csrf`
  y `CsrfInterceptor` copia `XSRF-TOKEN` al header `X-XSRF-TOKEN`. No hizo falta cambiar el backend.
- Ante un 401 la app intenta `auth/refresh` una vez; si falla, cierra la sesión y abre el login con aviso.
- Cada pago lleva su `Idempotency-Key`: reintentar tras un corte de red no duplica la reserva.
- El log HTTP en debug es nivel BASIC (nunca el cuerpo: contraseñas y tarjetas no quedan en Logcat).

## Pruebas

```bash
./gradlew testDebugUnitTest
```

| Prueba | Qué valida |
|---|---|
| `ValidacionesTest` (9) | Luhn, vencimiento, CVC, teléfono +51, RUC, correo, comentario/motivo 10–300, hora |
| `ContratoApiTest` (11) | Respuestas **reales** del backend (`src/test/resources/api/*.json`) se convierten a los modelos sin perder campos |

## Appium (etapa 5)

Todos los elementos que se prueban tienen `testTag`, expuesto como **resource-id** (`testTagsAsResourceId`),
también dentro de los diálogos. Catálogo completo en `docs/SELECTORES_APPIUM.md`; capabilities y flujos en
`docs/APPIUM.md`.

## Estructura

```
app/src/main/java/com/trouvels360/app/
├── MainActivity.kt                # Application + Activity (red, sesión)
├── data/
│   ├── Repository.kt              # API, errores de Laravel por campo, refresh, Session
│   ├── Validaciones.kt            # Reglas iguales a la API (pago, teléfono, RUC, textos)
│   └── remote/ApiService.kt · Dto.kt · Network.kt
└── ui/
    ├── Estado.kt                  # Búsquedas y selección (detalle → pago)
    ├── components/                # Common, DateRangeField, PagoDialog, ReviewsSection
    ├── navigation/AppNavHost.kt   # Rutas y barra inferior por rol
    └── screens/
        ├── auth/ hotels/ tours/ reservas/ profile/
        └── proveedor/             # Panel, ReservasRecibidas, HotelForm, TourForm, TourPrograma
```

Stack: Kotlin 2.0.21, Compose BOM 2024.12.01 (Material 3), Navigation Compose 2.8, Retrofit 2.11,
OkHttp 4.12, Gson, Coil 2.7. minSdk 26, targetSdk 35.
