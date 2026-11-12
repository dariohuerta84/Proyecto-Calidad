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
2. **Android Studio** con un emulador API 26 o superior encendido (`adb devices` debe listarlo como `emulator-5554`).
3. **APK de depuración:** en `android/`, `gradlew.bat assembleDebug` (Windows) o `./gradlew assembleDebug`. Queda en
   `android/app/build/outputs/apk/debug/app-debug.apk`, la ruta que ya trae `config.properties` (`app`). Cómo compilar con
   JDK 21: [`android/README.md`](../../android/README.md#compilar-el-apk).
4. **Appium 2:** Node 18+, `npm i -g appium`, `appium driver install uiautomator2` y dejar corriendo `appium`
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

## Estado de verificación (honesto)

En el entorno donde se preparó la etapa no hay emulador ni acceso a Maven Central, así que **la suite no se pudo
ejecutar contra un dispositivo**. Sí se verificó que:

- compila completa (`javac`) contra las firmas de java-client 9.3 / Selenium 4.25 / JUnit 5;
- los **184 resource-id** que usa existen en el código de la app (cruce automático contra los `testTag`);
- los datos que prepara por API son los mismos que usan Karate y Selenium (que sí corren en verde);
- la app trae el botón «Escribir las fechas» (`<campo>_dialogo_teclado`) que la suite usa para elegir fechas; faltaba en
  la copia del repositorio y se restauró.

Si Maven avisa de un conflicto de versiones entre `java-client` y `selenium-java`, quita la dependencia `selenium-java`
del `pom.xml` (java-client ya trae la versión compatible).

Para la primera corrida real conviene empezar por `mvn test -Dtest=CuentaMovilTest` y ajustar `fecha.patron` y el
`dispositivo` si hace falta.
