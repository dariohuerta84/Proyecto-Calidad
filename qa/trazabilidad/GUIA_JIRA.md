# Trazabilidad en Jira (TRV-605)

Objetivo: que desde cada historia (TRV-1xx…5xx) se vean sus casos de prueba, el último resultado y la evidencia, y que
cualquier falla quede como bug enlazado.

## 1. Archivos

| Archivo | Para qué |
|---|---|
| `salida/matriz_trazabilidad.html` | Vista navegable: HU → criterios → caso → herramienta → archivo del código → resultado (con filtros) |
| `salida/matriz_trazabilidad.csv` / `.md` | La misma matriz para Excel/Confluence (`;` como separador) |
| `salida/jira_casos_prueba.csv` | Importa los **160** casos como issues tipo **Test** (157 automatizados + 3 manuales con sus pasos), enlazados a su historia |
| `salida/jira_ejecuciones.csv` | Resultado de la última corrida por caso (para actualizar estados o adjuntar a la ejecución) |
| `recolectar_resultados.py` → `generar_trazabilidad.py` | Regeneran todo después de cada corrida |
| `datos/casos_etapa5.json` | Los 14 casos agregados en esta etapa para cubrir los 22 criterios que no tenían caso, y los criterios que ya verificaban casos existentes |

Cobertura: **119 de 119 criterios** de aceptación con al menos un caso.

Regenerar tras ejecutar las suites:

```bash
cd qa/trazabilidad
python recolectar_resultados.py      # lee karate-reports, reporte-selenium/resultados.json, reporte-appium, jmeter
python generar_trazabilidad.py       # escribe salida/
```

## 2. Preparar el proyecto (una vez)

1. **Tipo de issue "Test"**: *Configuración del proyecto → Tipos de incidencia → Añadir* (si usan Xray o Zephyr, ya existe).
2. **Tipo de vínculo "Tests / is tested by"**: *Configuración de Jira → Incidencias → Vincular incidencias*.
   Si no se puede crear, usar "Relates".
3. **Componente** `QA` y las etiquetas se crean solas al importar.
4. **Estados del Test** (flujo simple): `Por ejecutar → Pasó / Falló → Por ejecutar`.

## 3. Importar los casos

*Configuración del sistema → Importación externa del sistema → CSV* (o *Filtros → Importar incidencias desde CSV* en
Jira Cloud) con `salida/jira_casos_prueba.csv` (UTF-8, separador `,`). Mapeo:

| Columna CSV | Campo Jira |
|---|---|
| Issue Type | Tipo de incidencia (Test) |
| Summary | Resumen (`TC-API-026 Reserva válida…`) |
| Description | Descripción (criterios Dado/Cuando/Entonces + archivo y línea del código) |
| Priority | Prioridad (la de la HU) |
| Labels ×3 | Etiquetas: herramienta (`karate`, `selenium`, `appium`, `jmeter`), HU y caso |
| Component/s | Componente `QA` |
| Epic Link | Épica de la HU (en proyectos *team-managed*: campo **Parent**) |
| Link Tests | Vínculo **Tests** → la historia (`TRV-203`) |

Tras importar, cada historia muestra en "Incidencias vinculadas" sus casos (p. ej. TRV-203 → TC-API-026…030,
TC-WEB-011/012, TC-MOB-013/014, TC-PERF-003).

## 4. Registrar cada ejecución

1. Crear una incidencia **Tarea** "Ejecución de regresión <fecha>" bajo la épica TRV-6 (o un *Test Execution* si usan Xray).
2. Desde la raíz del repositorio, adjuntar los reportes (ver tabla) y `qa/trazabilidad/salida/jira_ejecuciones.csv`.
3. Pasar a **Pasó/Falló** los Test según `jira_ejecuciones.csv` (edición masiva: filtro por etiqueta + *Transición*).

| Suite | Qué adjuntar |
|---|---|
| Karate (TRV-601) | `qa/karate/target/karate-reports/` comprimido (abrir `karate-summary.html`) |
| JMeter (TRV-602) | `qa/jmeter/resultados/reporte-html/` comprimido + `qa/jmeter/resultados/verificacion.txt` |
| Selenium (TRV-603) | `qa/selenium/target/reporte-selenium/` comprimido (incluye capturas) + Allure si se generó |
| Appium (TRV-604) | `qa/appium/target/reporte-appium/` comprimido (capturas por paso) |

Con Xray: `mvn test` deja JUnit XML (`target/surefire-reports`, `karate-reports/*.xml`) que se importan con
*Importar resultados de ejecución → JUnit XML*; el nombre del caso (TC-xxx) relaciona cada resultado con su Test.

## 5. Plantilla de bug

```
Resumen:   [TC-WEB-011] <qué falla en una línea>
Tipo:      Error            Prioridad: según impacto      Componente: Backend / Web / Móvil
Vínculos:  "is blocked by"/"relates to" → TC-WEB-011 (Test) y TRV-203 (Historia)
Entorno:   Web http://localhost:4200 · API :8000 · Chrome 13x · commit <hash> · fecha
Precondiciones:  datos usados (hotel/tour/usuario de prueba)
Pasos para reproducir:
  1. …
  2. …
Resultado esperado:  (criterio CA-TRV-203-01 de la historia)
Resultado obtenido:  …
Evidencia:  captura del reporte (capturas/TC-WEB-011-FALLA-1.png), respuesta de la API, log
```

Regla: un bug abierto deja el Test en **Falló**; al cerrarlo se vuelve a ejecutar el caso y se pasa a **Pasó**.

## 6. Filtros (JQL) y tablero

| Filtro | JQL |
|---|---|
| Casos de una historia | `project = TRV AND issuetype = Test AND issue in linkedIssues("TRV-203", "Tests")` |
| Historias sin casos | `project = TRV AND issuetype = Story AND issueFunction not in hasLinks("is tested by")` *(ScriptRunner)*; sin plugins: revisar `matriz_trazabilidad.html` (todas tienen) |
| Casos que fallaron | `project = TRV AND issuetype = Test AND status = "Falló"` |
| Casos sin ejecutar | `project = TRV AND issuetype = Test AND status = "Por ejecutar"` |
| Por herramienta | `project = TRV AND issuetype = Test AND labels = selenium` |
| Bugs abiertos de pruebas | `project = TRV AND issuetype = Error AND statusCategory != Done AND issue in linkedIssues("TRV-6")` |

Tablero sugerido ("QA Trouvels360"): *Estadísticas de filtro bidimensional* (Tests: Estado × Etiqueta de herramienta),
*Gráfico circular* (Tests por estado), *Resultados de filtro* (Bugs abiertos), *Creados vs. resueltos* (Errores, 30 días).
