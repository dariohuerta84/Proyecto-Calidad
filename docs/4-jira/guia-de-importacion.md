# Guía de Jira: importar, ejecutar y reportar

[← Documentación](../README.md)

Con un solo archivo, [`jira_importacion.csv`](jira_importacion.csv), se crea todo el backlog en Jira, ya enlazado:

| Tipo de incidencia | Cantidad | Qué es |
|---|---|---|
| Epic | 9 | 4 del viajero (EV1–EV4), 4 del proveedor (EP1–EP4) y 1 de calidad (QA) |
| Story | 30 | HU-V01 a HU-V15 y HU-P01 a HU-P15, con reglas, criterios Dado / Cuando / Entonces, endpoints y story points |
| Task | 5 | QA-01 a QA-05, bajo la épica QA |
| Sub-task | 160 | Un caso de prueba por subtarea, colgado de su historia principal, con los criterios que verifica, el archivo donde está automatizado (o los pasos, si es manual) y su último resultado |

## 1. Preparar el proyecto (una vez)

1. Crear un proyecto de software **Scrum** con la clave `TRV` (o la que prefieran).
2. Comprobar que existan los tipos **Epic, Story, Task y Sub-task** (vienen por defecto). En español aparecen como
   Épica, Historia, Tarea y Subtarea.
3. Habilitar el campo **Story Points** en la pantalla de la historia (en *team-managed* se llama *Story point estimate*).
4. Crear los componentes **Backend, Web, Móvil, Microservicio y QA** (opcional: el importador puede crearlos).

## 2. Importar el CSV

1. Jira Cloud: *Filtros → Ver todas las incidencias → ⋯ → Importar incidencias desde CSV*. Jira Server/Data Center:
   *Administración → Sistema → Importación externa del sistema → CSV*.
2. Elegir `jira_importacion.csv`, codificación **UTF-8**, separador **coma**.
3. Mapear las columnas:

| Columna del CSV | Campo de Jira |
|---|---|
| Issue Id | Issue Id (no se importa como campo; sirve para enlazar padres e hijos dentro del archivo) |
| Parent Id | Parent Id (épica de la historia, historia de la subtarea) |
| Issue Type | Tipo de incidencia |
| Summary | Resumen (`[HU-V07] Reservar un hotel con pago simulado`) |
| Description | Descripción (en formato wiki de Jira: `h3.`, `*negrita*`, listas) |
| Priority | Prioridad (Highest = Más alta, High = Alta, Medium = Media, Low = Baja) |
| Story Points | Story Points / Story point estimate |
| Labels (×4) | Etiquetas: rol, épica, historia y herramienta (`viajero`, `ev2`, `hu-v07`, `karate`…) |
| Component (×3) | Componentes |

4. Si Jira pregunta por los valores de **Issue Type**, asignar `Epic → Épica`, `Story → Historia`, `Task → Tarea` y
   `Sub-task → Subtarea`.
5. Al terminar, cada historia muestra sus casos como subtareas y cada épica muestra sus historias.

> Si el proyecto es *company-managed* y la épica no se enlaza por `Parent Id`, mapear `Parent Id` a **Epic Link** para las
> historias. Las subtareas siempre usan `Parent Id`.

## 3. Organizar el tablero

- **Sprints sugeridos** (según prioridad y dependencias):

| Sprint | Historias | Objetivo |
|---|---|---|
| 1 | HU-V01, HU-V02, HU-P01, HU-P02, HU-V05, HU-P05 | Cuentas, acceso y primer catálogo |
| 2 | HU-V06, HU-V07, HU-P07, HU-P12, HU-P13, HU-P14 | Flujo completo de reserva de hotel con aprobación |
| 3 | HU-V08, HU-V09, HU-V10, HU-P08, HU-P09, HU-P10, HU-P11 | Tours y salidas |
| 4 | HU-V11, HU-V12, HU-V13, HU-P03, HU-P04, HU-P06, HU-P15 | Postventa y panel del proveedor |
| 5 | HU-V03, HU-V04, HU-V14, HU-V15 + QA-01 a QA-05 | Perfil, reseñas, itinerario y regresión completa |

- **Filtros rápidos** por etiqueta: `viajero`, `proveedor`, `karate`, `selenium`, `appium`, `jmeter`, `manual`.

## 4. Registrar una ejecución

1. Ejecutar las suites (ver [estrategia de pruebas](../2-pruebas/estrategia-de-pruebas.md#7-cómo-ejecutar)).
2. Crear una **Tarea** «Ejecución de regresión <fecha>» bajo la épica QA.
3. Adjuntar los reportes:

| Suite | Qué adjuntar |
|---|---|
| Karate (QA-01) | `qa/karate/target/karate-reports/` comprimido (abrir `karate-summary.html`) |
| JMeter (QA-02) | `qa/jmeter/resultados/reporte-html/` comprimido y `qa/jmeter/resultados/verificacion.txt` |
| Selenium (QA-03) | `qa/selenium/target/reporte-selenium/` comprimido (incluye capturas) |
| Appium (QA-04) | `qa/appium/target/reporte-appium/` comprimido (captura por paso) |
| Trazabilidad (QA-05) | `docs/3-trazabilidad/matriz-de-trazabilidad.csv` regenerado |

4. Pasar cada subtarea de caso a **Listo** si pasó; si falló, dejarla en curso y crear el defecto (siguiente sección).
   Edición masiva: buscar con JQL, *Acciones → Editar en bloque → Transición*.

> Con Xray o Zephyr: las suites dejan JUnit XML (`target/surefire-reports`, `karate-reports/*.xml`) que se importan con
> *Importar resultados de ejecución → JUnit XML*; el nombre del caso (`TC-xxx`) relaciona cada resultado con su prueba.

## 5. Registrar un defecto

```
Resumen:    [TC-WEB-011] <qué falla, en una línea>
Tipo:       Error          Prioridad: según severidad        Componente: Backend / Web / Móvil
Vínculos:   «bloquea a» la historia (HU-V07) y «se relaciona con» la subtarea del caso (TC-WEB-011)
Entorno:    Web http://localhost:4200 · API :8000 · navegador y versión · commit · fecha
Precondiciones:  datos usados (usuario, hotel, tour)
Pasos para reproducir:
  1. …
  2. …
Resultado esperado:  el criterio de la historia (por ejemplo CA-V07-01)
Resultado obtenido:  …
Evidencia:  captura del reporte, respuesta de la API, log
Severidad:  Crítica / Mayor / Media / Menor / Trivial
```

Regla: mientras el defecto esté abierto, la subtarea del caso no pasa a Listo. Al cerrarlo se vuelve a ejecutar el caso.
Los defectos encontrados hasta ahora están en [defectos.md](../3-trazabilidad/defectos.md).

## 6. Consultas JQL útiles

| Para ver | JQL |
|---|---|
| Historias del viajero | `project = TRV AND issuetype = Story AND labels = viajero ORDER BY key` |
| Casos de una historia | `project = TRV AND issuetype = Sub-task AND labels = hu-v07` |
| Casos por herramienta | `project = TRV AND issuetype = Sub-task AND labels = appium` |
| Casos pendientes | `project = TRV AND issuetype = Sub-task AND statusCategory != Done` |
| Defectos abiertos por severidad | `project = TRV AND issuetype = Bug AND statusCategory != Done ORDER BY priority DESC` |
| Historias sin terminar de la épica de hoteles del viajero | `project = TRV AND issuetype = Story AND labels = ev2 AND statusCategory != Done` |

## 7. Panel sugerido («QA Trouvels360»)

- **Estadísticas de filtro bidimensional:** subtareas de casos por *Estado* × *Etiqueta* (herramienta).
- **Gráfico circular:** historias por estado.
- **Resultados de filtro:** defectos abiertos ordenados por prioridad.
- **Gráfico de creadas frente a resueltas:** defectos de los últimos 30 días.
- **Progreso de sprint / burndown** del tablero Scrum.
