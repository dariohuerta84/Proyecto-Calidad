# Tareas técnicas de QA

[← Índice de historias](README.md)

Las cinco tareas corresponden a los entregables de calidad de la rúbrica. No son historias de usuario: las pide el equipo para verificar las 30 historias. Cada una indica su herramienta, carpeta y casos.

| Id | Tarea | Herramienta | Carpeta | Casos | Prioridad | Pts |
|---|---|---|---|---|---|---|
| QA-01 | Suite de pruebas de API con Karate | Karate 1.4.1 | `qa/karate` | 86 | Más alta | 8 |
| QA-02 | Plan de rendimiento y concurrencia con JMeter | Apache JMeter 5.6.3 | `qa/jmeter` | 6 | Alta | 5 |
| QA-03 | Automatización web con Selenium | Selenium 4.25 + JUnit 5 | `qa/selenium` | 33 | Más alta | 8 |
| QA-04 | Automatización móvil con Appium | Appium 2 (UiAutomator2) + java-client 9.3 | `qa/appium` | 32 | Más alta | 8 |
| QA-05 | Trazabilidad y reportes en Jira | Jira + scripts Python | `qa/trazabilidad` | 160 | Alta | 3 |

## QA-01 · Suite de pruebas de API con Karate

**Objetivo.** Cubrir las rutas de la API con validación de esquema, códigos HTTP, tiempos y reglas de negocio.

| Herramienta | Carpeta | Reemplaza a | Prioridad | Story points |
|---|---|---|---|---|
| Karate 1.4.1 | `qa/karate` | TRV-601 | Más alta | 8 |

**Criterios de aceptación**

- [x] Un `.feature` por recurso: auth, cuenta, hoteles, reservas-hotel, aprobacion, tours, reservas-tour, mis-reservas, resenas, itinerario, proveedor y cobertura-adicional.
- [x] Esquemas JSON reutilizables en `schemas/*.json` y `match each` en las listas.
- [x] Tiempo de respuesta: `responseTime < maxMs` (1000 ms en local, 2000 ms en `ci`).
- [x] Reutilización con `call` / `callSingle`: helpers `csrf`, `sesion`, `sesiones`, `registrar`, `reservar-hotel`, `reservar-tour`, `hotel-propio`, `tour-propio`, `salida`.
- [x] Datos dinámicos: correos e `Idempotency-Key` únicos generados con funciones JS; data-driven con `Scenario Outline` y 8 archivos CSV.
- [x] `karate-config.js` con entornos `local` y `ci`; ejecución en paralelo con el runner JUnit 5 y reporte HTML de Karate.

**Entregable:** `qa/karate` (Maven) y `target/karate-reports/karate-summary.html`.

---

## QA-02 · Plan de rendimiento y concurrencia con JMeter

**Objetivo.** Medir el catálogo bajo carga y demostrar que no hay sobreventa en las últimas habitaciones ni en los cupos.

| Herramienta | Carpeta | Reemplaza a | Prioridad | Story points |
|---|---|---|---|---|
| Apache JMeter 5.6.3 | `qa/jmeter` | TRV-602 | Alta | 5 |

**Criterios de aceptación**

- [x] Grupos de hilos: login (50 usuarios), catálogo de hoteles (100 usuarios, rampa 60 s, 5 min), búsqueda de tours (100), panel del proveedor (30), carrera por la última habitación (20 hilos con Synchronizing Timer) y carrera por cupos (30 hilos sobre 10 cupos).
- [x] Datos desde CSV Data Set Config (`usuarios.csv`, `ciudades.csv`, `categorias.csv`).
- [x] HTTP Cookie Manager, copia del `XSRF-TOKEN` al header y `Idempotency-Key` con `${__UUID()}`.
- [x] Aserciones de código y JSON, y Duration Assertion con el p95 objetivo de cada grupo.
- [x] Ejecución en modo no-GUI con reporte HTML (`jmeter -n -t trouvels360.jmx -l resultados.jtl -e -o reporte/`).
- [x] `verificar_resultados.py` comprueba p95 y conteos 201/409; `concurrencia_smoke.py` reproduce las carreras sin JMeter.

**Entregable:** `qa/jmeter/trouvels360.jmx`, `datos/*.csv` y reporte HTML en `qa/jmeter/resultados/`.

---

## QA-03 · Automatización web con Selenium

**Objetivo.** Automatizar los flujos de punta a punta del viajero y del proveedor en la web Angular.

| Herramienta | Carpeta | Reemplaza a | Prioridad | Story points |
|---|---|---|---|---|
| Selenium 4.25 + JUnit 5 | `qa/selenium` | TRV-603 | Más alta | 8 |

**Criterios de aceptación**

- [x] Patrón Page Object (17 páginas) y selectores por atributo `data-testid` (sin XPath absolutos).
- [x] Solo esperas explícitas (`WebDriverWait`); sin `Thread.sleep`.
- [x] Flujos: registro, login con retorno, búsqueda, reserva de hotel con pago, aprobación por el proveedor y verificación del viajero, reserva de tour, cancelación, reseña e itinerario.
- [x] Los datos de cada prueba se crean por API antes del flujo (pruebas independientes).
- [x] Ejecución headless, captura de pantalla automática y reporte HTML propio más Allure.

**Entregable:** `qa/selenium` (Maven) y `target/reporte-selenium/index.html`.

---

## QA-04 · Automatización móvil con Appium

**Objetivo.** Automatizar los mismos flujos en la app Android nativa (Kotlin + Jetpack Compose).

| Herramienta | Carpeta | Reemplaza a | Prioridad | Story points |
|---|---|---|---|---|
| Appium 2 (UiAutomator2) + java-client 9.3 | `qa/appium` | TRV-604 | Más alta | 8 |

**Criterios de aceptación**

- [x] Capabilities parametrizadas en `config.properties` (platformName, automationName UiAutomator2, deviceName, app, appPackage, appActivity, noReset).
- [x] Localizadores por resource-id (los `testTag` de Compose se exponen con `testTagsAsResourceId`).
- [x] Gestos nativos: tap, scroll con UiScrollable, swipe W3C, pulsación larga y fechas en el selector.
- [x] Esperas explícitas para sincronizar con la red y el emulador.
- [x] Flujo validado: login → buscar hotel → elegir → pagar → ver PENDIENTE; el proveedor acepta; el viajero ve CONFIRMADA.
- [x] Reporte HTML con captura por paso.

**Entregable:** `qa/appium` (Maven) y `target/reporte-appium/index.html`.

---

## QA-05 · Trazabilidad y reportes en Jira

**Objetivo.** Vincular historia → criterio → caso → ejecución → evidencia o defecto.

| Herramienta | Carpeta | Reemplaza a | Prioridad | Story points |
|---|---|---|---|---|
| Jira + scripts Python | `qa/trazabilidad` | TRV-605 | Alta | 3 |

**Criterios de aceptación**

- [x] Cada caso automatizado lleva su identificador en el nombre o la etiqueta (por ejemplo `@TC-API-021`).
- [x] Matriz de trazabilidad publicada (este documento y `qa/trazabilidad/salida/`).
- [x] Los reportes de Karate, JMeter, Selenium y Appium se adjuntan a la ejecución en Jira.
- [x] Cada fallo genera un Bug enlazado a la historia, con pasos, evidencia y severidad.
- [x] Tablero de Jira: avance por épica, casos aprobados frente a fallidos y bugs abiertos por severidad.

**Entregable:** CSV de importación para Jira, matriz de trazabilidad y ejecuciones con reportes adjuntos.

---
