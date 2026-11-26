# Trazabilidad (QA-05)

Une cada **historia** con sus **criterios**, los **casos** que los prueban, el **archivo** donde está cada caso y su
**último resultado**. Usa los mismos identificadores que [`docs/`](../../docs/README.md) (`HU-V07`, `CA-V07-05`,
`TC-API-029`).

| Archivo | Qué es |
|---|---|
| `datos/historias.json` | Las 30 historias y sus 130 criterios, cada uno con los casos que lo verifican |
| `datos/casos.json` | Título y herramienta de los 160 casos (y pasos de los 3 manuales) |
| `datos/resultados_manuales.json` | Veredictos y evidencias de los tres casos manuales |
| `datos/resultados.json` | Último resultado de cada caso; lo escribe `recolectar_resultados.py` |
| `recolectar_resultados.py` | Consolida reportes completos y parciales de las cuatro suites. Conserva la evidencia de los casos omitidos en una corrida filtrada. |
| `generar_trazabilidad.py` | Escribe `salida/`: la matriz y los resultados para Jira |
| `salida/matriz_trazabilidad.html` | Matriz con filtros por rol, herramienta, estado y texto |
| `salida/matriz_trazabilidad.csv` / `.md` | La misma matriz para Excel (`;`) o Markdown |
| `salida/jira_ejecuciones.csv` | Estado de cada caso para actualizar Jira después de una corrida |

## Uso

`qa\ejecutar-todo.bat` lo hace solo al terminar. A mano, desde esta carpeta y después de correr las suites:

```bash
python recolectar_resultados.py    # consolida reportes bajo target/ y las carpetas resultados* de JMeter
python generar_trazabilidad.py     # escribe salida/
```

Ubica cada caso en el código por su etiqueta (`@TC-API-026` en Karate, `@Tag("TC-WEB-011")` en Selenium y Appium, el
nombre del grupo en JMeter). Si un caso no aparece, lo informa como «sin automatización ubicada».

## Estado actual

Los 130 criterios tienen al menos un caso. Al 25/11/2026 se registran resultados aprobados para los 157 casos automatizados
y los 3 manuales. Consulta la [matriz HTML](salida/matriz_trazabilidad.html) para ver el último veredicto por caso y
el [CSV](salida/matriz_trazabilidad.csv) para encontrar la ruta de su evidencia.

## Jira

La importación de historias y casos y el registro de ejecuciones y defectos están en
[`docs/4-jira/guia-de-importacion.md`](../../docs/4-jira/guia-de-importacion.md). Este `jira_ejecuciones.csv` sirve para
pasar cada caso a «Pasó» o «Falló» después de una corrida.
