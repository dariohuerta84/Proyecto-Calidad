# Documentación de Trouvels360

Trouvels360 es un marketplace de turismo: los **viajeros** buscan, cotizan con IGV y reservan hoteles y tours con pago
simulado; los **proveedores** publican su oferta y aceptan o rechazan las solicitudes de hotel (los tours se confirman al
instante). Se usa desde una web Angular y una app Android que consumen la API Laravel; un microservicio FastAPI arma
itinerarios sugeridos.

| Historias de usuario | Criterios de aceptación | Casos de prueba | Criterios cubiertos | Defectos registrados |
|---|---|---|---|---|
| **30** (15 viajero + 15 proveedor) | **130** | **160** (157 automatizados + 3 manuales) | **130 / 130** | **21** (19 corregidos) |

## Contenido

| Carpeta | Qué hay |
|---|---|
| [`1-historias-de-usuario/`](1-historias-de-usuario/README.md) | Las 30 historias ([viajero](1-historias-de-usuario/viajero.md) y [proveedor](1-historias-de-usuario/proveedor.md)) con criterios Dado / Cuando / Entonces y los casos que prueban cada uno; las 5 [tareas de QA](1-historias-de-usuario/tareas-qa.md); las [equivalencias](1-historias-de-usuario/equivalencias.md) con los ids que aún usa el código (`TRV-xxx`); y todo en [Word](1-historias-de-usuario/Trouvels360_Historias_de_Usuario.docx) |
| [`2-pruebas/`](2-pruebas/estrategia-de-pruebas.md) | [Estrategia](2-pruebas/estrategia-de-pruebas.md) (niveles, técnicas, entornos, datos, cómo ejecutar, estado) y [catálogo de los 160 casos](2-pruebas/casos-de-prueba.md) |
| [`3-trazabilidad/`](3-trazabilidad/matriz-de-trazabilidad.md) | [Matriz](3-trazabilidad/matriz-de-trazabilidad.md) historia → criterio → caso → resultado (también en [CSV](3-trazabilidad/matriz-de-trazabilidad.csv)) y [registro de defectos](3-trazabilidad/defectos.md) |
| [`4-jira/`](4-jira/guia-de-importacion.md) | [CSV](4-jira/jira_importacion.csv) que crea en Jira las épicas, historias, tareas y casos ya enlazados, y la [guía](4-jira/guia-de-importacion.md) para importar, registrar ejecuciones y defectos |

## Dónde está cada criterio de la rúbrica

| Criterio | Qué se pide para «Excelente» | Dónde se cumple |
|---|---|---|
| **1. Pruebas de API (Karate)** | Esquemas JSON, códigos de estado, tiempos de respuesta, variables dinámicas y reutilización con `call` | `qa/karate`: 86 casos en 136 escenarios; 11 esquemas en `schemas/*.json` con `match` y `match each`; `responseTime < maxMs` en cada escenario; correos e `Idempotency-Key` generados con funciones JS; 8 CSV con `Scenario Outline`; 11 helpers reutilizados con `call` / `callSingle`; ejecución en paralelo. Tarea [QA-01](1-historias-de-usuario/tareas-qa.md#qa-01--suite-de-pruebas-de-api-con-karate) |
| **2. Rendimiento (JMeter)** | Thread Groups con escenarios realistas, listeners, aserciones de tiempo, CSV data-driven y reporte HTML | `qa/jmeter/trouvels360.jmx`: 6 grupos (login 50 usuarios, catálogo 100 con rampa de 60 s y 5 min, tours 100, panel 30, carreras de 20 y 30 hilos con Synchronizing Timer); Summary y Aggregate Report; Duration Assertion con el p95 de cada grupo; 3 CSV Data Set Config; `ejecutar.bat` en modo no-GUI genera el reporte HTML. Tarea [QA-02](1-historias-de-usuario/tareas-qa.md#qa-02--plan-de-rendimiento-y-concurrencia-con-jmeter) |
| **3. Web (Selenium)** | Selectores robustos sin XPath absolutos, esperas explícitas o fluidas, flujos end-to-end críticos | `qa/selenium`: 33 casos con Page Object (17 páginas); selectores `data-testid`; solo `WebDriverWait` (espera fluida que ignora `StaleElementReference`), sin `Thread.sleep`; flujos de registro, login con retorno, reserva con pago, aprobación entre dos navegadores, tours, cancelación y reseñas. **33 / 33 OK.** Tarea [QA-03](1-historias-de-usuario/tareas-qa.md#qa-03--automatización-web-con-selenium) |
| **4. Móvil (Appium)** | Capabilities correctas, interacción nativa (swipe, tap) y flujo end-to-end validado | `qa/appium`: 32 casos; capabilities UiAutomator2 parametrizadas en `config.properties`; resource-ids de Compose; tap, scroll con UiScrollable y swipe con W3C Actions; flujo reservar → PENDIENTE → proveedor acepta → CONFIRMADA. Tarea [QA-04](1-historias-de-usuario/tareas-qa.md#qa-04--automatización-móvil-con-appium) |
| **5. Trazabilidad y reportes (Jira)** | Evidencia completa y casos automatizados vinculados a tickets de Jira | Cada caso lleva su `TC-xxx` en la etiqueta del código; [matriz](3-trazabilidad/matriz-de-trazabilidad.md) con 130 / 130 criterios cubiertos; [CSV de Jira](4-jira/jira_importacion.csv) con cada caso como subtarea de su historia; [defectos](3-trazabilidad/defectos.md) enlazados a criterio y caso; reportes HTML en `qa/evidencias/etapa-5/`. Tarea [QA-05](1-historias-de-usuario/tareas-qa.md#qa-05--trazabilidad-y-reportes-en-jira) |

**Pendiente para la nota completa:** correr Appium en un emulador y JMeter con su reporte HTML (ver
[estado actual](2-pruebas/estrategia-de-pruebas.md#10-estado-actual)), importar el CSV en Jira y adjuntar los reportes a
la ejecución.

## Documentación técnica de cada componente

Está junto a su código: [`backend/`](../backend/README.md) (cómo levantar y desplegar las dos APIs; en `backend/api-laravel/docs/` la referencia de endpoints y los datos de prueba),
[`frontend/`](../frontend/README.md) (cómo levantar y desplegar la web; en `frontend/docs/` los selectores para Selenium),
[`android/docs/`](../android/docs/SELECTORES_APPIUM.md) (selectores y guía de Appium) y el `README.md` de cada suite en
[`qa/`](../qa/).
