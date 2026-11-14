# Pruebas E2E móviles — Appium 2 + JUnit 5 (QA-04)

32 casos (**TC-MOB-001 a TC-MOB-032**) sobre la app Android (Kotlin + Compose) de la etapa 4, para viajero y proveedor.

## Cómo localiza

La app activa `testTagsAsResourceId`, así que cada `testTag` de Compose es un **resource-id** (incluidos los diálogos).
Se localiza con `AppiumBy.id("login_email")`; las listas usan ids con el dato (`hotel_card_42`, `btn_aceptar_<código>`).
La lista completa de selectores está en `android/docs/SELECTORES_APPIUM.md`.

## Estructura

```
src/test/java/com/trouvels360/movil/
├── soporte/    Config, FabricaDriver (UiAutomator2Options desde config.properties), Gestos (UiScrollable y W3C Actions:
│               deslizar, toque largo), Api (prepara datos), Datos, Evidencias, ReporteHtml
├── pantallas/  BasePantalla (esperas explícitas, scroll automático, fechas en modo teclado, stepper, diálogos)
│               CuentaPantallas, HotelPantallas, PagoPantalla, TourPantallas, ReservasPantalla, ProveedorPantallas
└── pruebas/    CuentaMovilTest (001–008), HotelesMovilTest (009–014), AprobacionMovilTest (015–016),
                ToursMovilTest (017–019), ReservasMovilTest (020–022), ResenasItinerarioMovilTest (023–024),
                ProveedorMovilTest (025–032)
```

- **Esperas:** solo `WebDriverWait` (implicit wait en 0).
- **Gestos:** `UiScrollable.scrollIntoView` para llegar a elementos fuera de pantalla, deslizamientos y toque largo con
  W3C Actions, selector de fechas de Material 3 en modo teclado (`<campo>_dialogo_teclado`).
- **Red:** TC-MOB-010 activa el modo avión para comprobar el estado de error con *Reintentar* y lo desactiva al terminar.
- **Evidencias:** cada `paso()` guarda una captura (Allure + reporte propio) y al final se toma la captura final o la del fallo.

## Requisitos

1. **JDK 21** (o 17) y **Maven 3.9+** (nota sobre Java 25 en [`qa/README.md`](../README.md#requisitos)).
2. **SDK de Android** con un emulador API 26 o superior encendido (`adb devices` debe mostrarlo en estado `device`).
3. **APK de depuración:** en `android/`, `gradlew.bat assembleDebug` (Windows) o `./gradlew assembleDebug`. Queda en
   `android/app/build/outputs/apk/debug/app-debug.apk`, la ruta que ya trae `config.properties` (`app`). Cómo compilar con
   JDK 21: [`android/README.md`](../../android/README.md#compilar-el-apk). Si recompilaste la app con el mismo código de versión, actualízala en el emulador antes de probar. Desde `android/` ejecuta `adb -s emulator-5554 install -r -t app/build/outputs/apk/debug/app-debug.apk`. Ajusta el identificador si usas otro dispositivo de pruebas.
4. **Appium 2:** Node 18+, `npm i -g appium@2`, `appium driver install uiautomator2@4.2.9` y dejar corriendo `appium`
   (escucha en http://127.0.0.1:4723).
5. Las dos APIs levantadas con datos limpios y `LOGIN_RATE_LIMIT=1000` / `REGISTER_RATE_LIMIT=1000`. La app llega a la
   API de tu PC por `http://10.0.2.2:8000/api/`; la suite prepara datos por `http://localhost:8000/api`.
6. `fecha.patron` en `config.properties` según el idioma del emulador (`MM/dd/yyyy` en inglés, `dd/MM/yyyy` en español).

## Ejecutar

```bash
mvn test                                    # todo
mvn test -Dgroups=HU-V07                   # una historia
mvn test -Dtest=ProveedorMovilTest          # una clase
mvn test -Dapp=C:/ruta/app-debug.apk -Ddispositivo=emulator-5556
```

Windows: `ejecutar.bat`. Reportes: `target/reporte-appium/index.html` (propio, con capturas por paso) y
`mvn allure:serve`.

## Revisar resultados

El reporte HTML contiene la corrida completa y sus capturas. Puedes repetir una clase y conservar el historial con
`mvn test "-Dtest=CuentaMovilTest" "-Dreporte.dir=target/reporte-appium-repeticion/cuentas"`.
Usa una subcarpeta por repetición. Al regenerar la matriz, se incorpora el veredicto más reciente de cada caso.
