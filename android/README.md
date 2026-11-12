# App Android de Trouvels360 (Kotlin + Jetpack Compose)

App nativa con **las mismas historias que la web** (viajero y proveedor), sobre la misma
[API Laravel](../backend/README.md) que usa la web (y, a través de ella, la API Python de itinerarios). La API es la única fuente de verdad: la app no guarda reservas
ni calcula importes por su cuenta.

## Qué cubre

| Rol | Pantalla | Historia | Endpoints |
|---|---|---|---|
| Todos | Login (vuelve a la pantalla que lo pidió; aviso si la sesión expiró) | HU-V02 | `auth/csrf`, `auth/login`, `auth/refresh`, `auth/logout` |
| Todos | Registro de viajero o proveedor (RUC, celular +51) | HU-V01, HU-P01 | `auth/register` |
| Todos | Mi cuenta: datos por rol, foto (URL), contraseña, eliminar cuenta | HU-V03, HU-V04, HU-P02 | `usuarios/me`, `usuarios/me/password` |
| Viajero | Búsqueda de hoteles por ciudad, fechas y huéspedes (paginada) | HU-V05 | `GET hoteles` |
| Viajero | Detalle: unidades libres por fechas, selección de habitaciones | HU-V06 | `hoteles/{id}`, `hoteles/{id}/disponibilidad` |
| Viajero | Itinerario sugerido opcional (solo tours con salida en tu estadía) | HU-V15 | `POST itineraries` → FastAPI |
| Viajero | Pago: cotización oficial con IGV 18 %, pago simulado, comprobante **PENDIENTE** | HU-V07 | `reservas-habitaciones/cotizar`, `POST reservas-habitaciones` + `Idempotency-Key` |
| Viajero | Tours por destino, categoría, fechas y personas | HU-V08 | `GET tours` |
| Viajero | Detalle del tour: actividades, salidas con cupos, personas | HU-V09 | `tours/{id}` |
| Viajero | Pago de tour, comprobante **CONFIRMADA** | HU-V10 | `tours/salidas/{s}/cotizar`, `.../reservas` |
| Viajero | Mis reservas (hotel/tour, filtro por estado, motivo de rechazo) y cancelar | HU-V11, HU-V12, HU-V13 | `mis-reservas`, `tours/mis-reservas`, `.../cancelar` |
| Viajero | Reseñas: resumen real, paginación, publicar/editar/eliminar la propia | HU-V14 | `reviews`, `reviews/resumen`, `reviews/puedo-opinar` |
| Proveedor | Panel: indicadores por estado, ingresos, publicaciones paginadas | HU-P03, HU-P04 | `proveedor/indicadores`, `proveedor/servicios` |
| Proveedor | Reservas recibidas: filtros, **aceptar** y **rechazar con motivo** | HU-P12, HU-P13 | `proveedor/reservas`, `.../aceptar`, `.../rechazar` |
| Proveedor | Publicar/editar hotel (con tipos de habitación) | HU-P05, HU-P06, HU-P07 | `hoteles`, `hoteles/{id}/habitaciones`, `habitaciones/{id}` |
| Proveedor | Publicar/editar tour (con salidas iniciales) | HU-P08, HU-P09 | `tours`, `tours/{id}` |
| Proveedor | Salidas (cupo, cerrar, reabrir, cancelar) y actividades ordenadas | HU-P10, HU-P11 | `tours/{id}/salidas`, `tours/{id}/actividades` |
| Proveedor | Eliminar publicación (muestra el 409 si tiene reservas activas) | HU-P15 | `DELETE servicios/{id}` |

La barra inferior cambia según el rol: **viajero/visitante** → Hoteles · Tours · Reservas · Cuenta;
**proveedor** → Panel · Recibidas · Cuenta.

## Requisitos

- **Android Studio** Ladybug (2024.2) o más reciente, con el SDK de Android 35.
- **JDK 21** para Gradle. Android Studio ya trae uno (JBR 21): en *Settings → Build, Execution, Deployment → Build Tools
  → Gradle → Gradle JDK* elige **jbr-21** («JetBrains Runtime 21»). **No uses Java 25**: Gradle 8.11 no lo admite y la
  sincronización falla al leer la versión de Java. El proyecto compila para Java 17 (`jvmTarget = "17"`), compatible con
  JDK 17 y 21.
- Un emulador con Android 8.0 (API 26) o superior, o un celular con depuración USB.

## Ejecutar

1. Levanta el backend: `backend\levantar-todo.bat` (API Laravel en el puerto 8000 y API Python en el 8001).
2. Abre esta carpeta (`android/`) en Android Studio y deja que Gradle sincronice (la primera vez descarga dependencias).
3. Revisa la URL de la API en `gradle.properties` (debe terminar en `/api/`):

   | Dónde corre la app | `API_BASE_URL` |
   |---|---|
   | Emulador (por defecto) | `http://10.0.2.2:8000/api/` — `10.0.2.2` es tu PC vista desde el emulador |
   | Celular en la misma red Wi-Fi | `http://192.168.x.x:8000/api/` (IP de tu PC); agrégala en `app/src/main/res/xml/network_security_config.xml` |
   | API publicada en la nube | `https://api.midominio.com/api/` |

   También se puede pasar al compilar: `gradlew.bat assembleDebug -PAPI_BASE_URL=https://api.midominio.com/api/`.
4. Run ▶ sobre el emulador.

### Compilar el APK

Desde `android/`, con `JAVA_HOME` apuntando a un JDK 21 (por ejemplo el de Android Studio):

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug        # → app\build\outputs\apk\debug\app-debug.apk (lo usa qa/appium)
.\gradlew.bat testDebugUnitTest    # pruebas unitarias
```

En Linux o macOS: `./gradlew assembleDebug`.

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

## Appium

Todos los elementos que se prueban tienen `testTag`, expuesto como **resource-id** (`testTagsAsResourceId`),
también dentro de los diálogos. Los selectores de fecha tienen el botón «Escribir las fechas» (`<campo>_dialogo_teclado`)
para que las pruebas no dependan del calendario. Catálogo completo en `docs/SELECTORES_APPIUM.md`; capabilities y flujos en
`docs/APPIUM.md`; la suite está en [`qa/appium`](../qa/appium/README.md).

## Verificación al preparar la entrega

Aquí no había SDK de Android ni emulador, así que no se generó el APK. Sí se verificó que todo el código Kotlin compila
(contra firmas de las librerías), que los resource-id que usa Appium existen y que la app usa exactamente los endpoints
de la API actual (los mismos que Karate y Selenium prueban en verde). Si Android Studio muestra un error al sincronizar,
revisa primero que el *Gradle JDK* sea 21.

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
