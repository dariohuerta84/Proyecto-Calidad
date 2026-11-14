# Pruebas de Trouvels360 (`qa/`)

Las cuatro suites de la rúbrica más la trazabilidad. Cada caso tiene un id (`TC-API-026`) y la etiqueta de su historia
(`HU-V07`), los mismos que en [`docs/`](../docs/README.md).

| Carpeta | Herramienta | Casos | Qué prueba | Último resultado |
|---|---|---|---|---|
| [`karate/`](karate/README.md) | Karate 1.5.2 | 86 (136 escenarios) | API Laravel: contratos, reglas, permisos, idempotencia | **136 / 136 OK** |
| [`selenium/`](selenium/README.md) | Selenium 4.25 + JUnit 5 | 33 | Web Angular de punta a punta | **33 / 33 OK** (web servida por nginx) |
| [`appium/`](appium/README.md) | Appium 2 + JUnit 5 | 32 | App Android de punta a punta | Ver la [matriz actualizada](trazabilidad/salida/matriz_trazabilidad.html) |
| [`jmeter/`](jmeter/README.md) | JMeter 5.6.3 | 6 | Carga y carreras de concurrencia | 6 / 6 OK en la corrida completa (12/11/2026) |
| [`trazabilidad/`](trazabilidad/README.md) | Python | 160 | Matriz historia → criterio → caso → resultado | 130 / 130 criterios con caso |
| `evidencias/` | — | — | Reportes de la última corrida (Karate, Selenium, concurrencia) | — |
| `postman/` | Postman + Newman | — | Colección de la versión base (20 historias); complementaria, fuera de la trazabilidad | — |

Además hay 3 casos manuales (`TC-MAN-001` a `003`) con sus pasos en
[`docs/2-pruebas/casos-de-prueba.md`](../docs/2-pruebas/casos-de-prueba.md#pasos-de-los-casos-manuales).

## Requisitos

| Para | Necesitas |
|---|---|
| Karate, Selenium, Appium | **JDK 21** (también sirve 17) y **Maven 3.9+**. Comprueba con `java -version` y `mvn -v`. |
| Selenium | Google Chrome. Selenium Manager descarga el driver correcto. |
| Appium | Android Studio con un emulador, Node 18+ y Appium 2 (ver [appium/README.md](appium/README.md)). |
| JMeter | Apache JMeter 5.6.3 (ver [jmeter/README.md](jmeter/README.md)). |
| Trazabilidad | Python 3.9+ (sin librerías extra). |

> **Java 25 no sirve todavía** para Gradle (Android) ni para algunos plugins de Maven. Si tienes Java 25 instalado,
> instala también el JDK 21 (Temurin o el de Android Studio) y apunta `JAVA_HOME` a él solo para estas suites:
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21"   # o: C:\Program Files\Android\Android Studio\jbr
> $env:Path = "$env:JAVA_HOME\bin;$env:Path"
> java -version                                                 # debe decir 21
> ```

## Preparar el entorno de pruebas

1. **Límites altos en la API** (las suites crean muchas cuentas). En `backend/api-laravel/.env`:
   ```
   LOGIN_RATE_LIMIT=1000
   REGISTER_RATE_LIMIT=1000
   ```
2. **Levantar todo:** `backend\levantar-todo.bat` y `frontend\levantar.bat` (si la API ya estaba arriba, ejecuta
   `docker compose up -d` en `backend/` para que tome los nuevos límites).
3. **Datos limpios:** `backend\api-laravel\reiniciar-datos.bat` (recrea la base con el `QaSeeder`).
4. Comprobar: http://localhost:8000/api/ping, http://localhost:8001/api/health y http://localhost:4200.

## Ejecutar

Todo de una vez, desde la raíz del repositorio:

```powershell
.\qa\ejecutar-todo.bat                  # Karate, Selenium, JMeter y Appium
.\qa\ejecutar-todo.bat web              # Karate y Selenium
.\qa\ejecutar-todo.bat matriz           # Actualiza la matriz con los reportes existentes
```

La corrida completa necesita JMeter, el APK, un dispositivo Android disponible en `adb devices` y el servidor Appium encendido. Los argumentos `jmeter` y `appium` permiten añadir solo la suite indicada a Karate y Selenium.

Si preparaste el entorno local con `QA-Trouvels360.cmd` y el emulador se apagó, abre dos terminales con ese lanzador y deja un comando en ejecución en cada una.

```powershell
emulator -avd Trouvels360_QA -port 5554 -accel on -gpu swiftshader -no-window
appium --address 127.0.0.1 --port 4723
```

Comprueba que `adb devices` muestre `emulator-5554` como `device` y ejecuta la suite desde una tercera terminal.

Ejecuta los tres casos manuales siguiendo el catálogo. Registra el veredicto y la evidencia de cada uno en `trazabilidad/datos/resultados_manuales.json`. El ejecutor incorpora esos resultados al regenerar la matriz.

O cada suite desde su carpeta con `ejecutar.bat` / `mvn test`. Para una sola historia o caso:

```bash
mvn test "-Dkarate.options=--tags @HU-V07"     # Karate: una historia
mvn test "-Dkarate.options=--tags @TC-API-026" # Karate: un caso
mvn test -Dgroups=HU-V07                       # Selenium o Appium: una historia
mvn test -Dgroups=TC-WEB-011                   # Selenium o Appium: un caso
```

Al terminar, `ejecutar-todo` regenera la matriz y la abre (`trazabilidad/salida/matriz_trazabilidad.html`).

## Reportes

| Suite | Dónde |
|---|---|
| Karate | `karate/target/karate-reports/karate-summary.html` |
| Selenium | `selenium/target/reporte-selenium/index.html` (con capturas) y Allure |
| Appium | `appium/target/reporte-appium/index.html` (captura por paso) |
| JMeter | `jmeter/resultados/reporte-html/index.html` y `jmeter/resultados/verificacion.txt` |
| Trazabilidad | `trazabilidad/salida/` (matriz en HTML, Markdown y CSV, y `jira_ejecuciones.csv`) |

Cómo subirlos a Jira: [`docs/4-jira/guia-de-importacion.md`](../docs/4-jira/guia-de-importacion.md).

## Evidencias de ejecución

- **Appium.** El reporte incluye el error y las capturas disponibles para cada caso.
- **Manuales.** Los tres casos pasaron el 12/11/2026. Sus capturas están en `evidencias/manuales/` y sus resultados en `trazabilidad/datos/resultados_manuales.json`.
