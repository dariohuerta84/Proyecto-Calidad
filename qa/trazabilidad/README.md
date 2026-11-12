# Trazabilidad (QA-05)

Une cada **historia** con sus **criterios**, los **casos** que los prueban, el **archivo** donde está cada caso y su
**último resultado**. Usa los mismos identificadores que [`docs/`](../../docs/README.md) (`HU-V07`, `CA-V07-05`,
`TC-API-029`).

| Archivo | Qué es |
|---|---|
| `datos/historias.json` | Las 30 historias y sus 130 criterios, cada uno con los casos que lo verifican |
| `datos/casos.json` | Título y herramienta de los 160 casos (y pasos de los 3 manuales) |
| `datos/resultados_manuales.json` | Resultados que no salen de un reporte (la prueba rápida de concurrencia, los manuales) |
| `datos/resultados.json` | Último resultado de cada caso; lo escribe `recolectar_resultados.py` |
| `recolectar_resultados.py` | Lee los reportes de Karate, Selenium, Appium y JMeter |
| `generar_trazabilidad.py` | Escribe `salida/`: la matriz y los resultados para Jira |
| `salida/matriz_trazabilidad.html` | Matriz con filtros por rol, herramienta, estado y texto |
| `salida/matriz_trazabilidad.csv` / `.md` | La misma matriz para Excel (`;`) o Markdown |
| `salida/jira_ejecuciones.csv` | Estado de cada caso para actualizar Jira después de una corrida |

## Uso

`qa\ejecutar-todo.bat` lo hace solo al terminar. A mano, desde esta carpeta y después de correr las suites:

```bash
python recolectar_resultados.py    # lee los reportes en ../karate/target, ../selenium/target, ../appium/target, ../jmeter/resultados
python generar_trazabilidad.py     # escribe salida/
```

Ubica cada caso en el código por su etiqueta (`@TC-API-026` en Karate, `@Tag("TC-WEB-011")` en Selenium y Appium, el
nombre del grupo en JMeter). Si un caso no aparece, lo informa como «sin automatización ubicada».

## Estado actual

130 / 130 criterios con al menos un caso · 157 / 157 casos automatizados ubicados en el código + 3 manuales ·
121 casos en «Pasó» (Karate 86, Selenium 33 y las 2 carreras de concurrencia con el script de Python) y 39 pendientes de
ejecutar (Appium 32, JMeter 4, manuales 3).

## Jira

La importación de historias y casos y el registro de ejecuciones y defectos están en
[`docs/4-jira/guia-de-importacion.md`](../../docs/4-jira/guia-de-importacion.md). Este `jira_ejecuciones.csv` sirve para
pasar cada caso a «Pasó» o «Falló» después de una corrida.
