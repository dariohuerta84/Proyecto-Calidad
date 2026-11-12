# Pruebas de Trouvels360 (`qa/`)

Las cuatro suites de la rúbrica más la trazabilidad. Cada caso tiene un id (`TC-API-026`) y la etiqueta de su historia
(`HU-V07`), los mismos que en [`docs/`](../docs/README.md).

| Carpeta | Herramienta | Casos | Qué prueba | Último resultado |
|---|---|---|---|---|
| [`karate/`](karate/README.md) | Karate 1.5.2 | 86 (136 escenarios) | API Laravel: contratos, reglas, permisos, idempotencia | **136 / 136 OK** |
| [`selenium/`](selenium/README.md) | Selenium 4.25 + JUnit 5 | 33 | Web Angular de punta a punta | **33 / 33 OK** (web servida por nginx) |
| [`appium/`](appium/README.md) | Appium 2 + JUnit 5 | 32 | App Android de punta a punta | Compila; **pendiente correr en un emulador** |
| [`jmeter/`](jmeter/README.md) | JMeter 5.6.3 | 6 | Carga y carreras de concurrencia | Carreras OK con el script de Python; **pendiente la corrida con JMeter** |
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
.\qa\ejecutar-todo.bat                  # Karate + Selenium + matriz de trazabilidad
.\qa\ejecutar-todo.bat jmeter appium    # además JMeter y Appium (con JMeter instalado y el emulador + Appium encendidos)
```

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

## Qué falta y por qué

- **Appium** no se pudo ejecutar al preparar la entrega (no había emulador). La suite compila, los resource-id que usa
  existen en la app y la app tiene el modo «Escribir las fechas» que la suite necesita. Si un selector o una espera
  falla en la primera corrida, el reporte indica el paso exacto.
- **JMeter** no se pudo descargar en ese entorno. El plan está generado y validado, y las dos carreras de concurrencia
  se comprobaron con `jmeter/concurrencia_smoke.py` contra la misma API: 1 × 201 + 19 × 409 por la última habitación, y
  exactamente 10 cupos confirmados de 30 intentos.
- Los **3 casos manuales** se marcan a mano en Jira después de seguir sus pasos.
