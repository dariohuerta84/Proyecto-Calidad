# Trouvels360 - Historial de etapas

El código vigente vive en las carpetas de la raíz (`backend-laravel/`, `backend-itinerarios/`, `frontend-angular/` y `android/`). Esta carpeta conserva los entregables propios y explica qué cambió en cada hito. Los snapshots completos anteriores permanecen en el historial de Git.

## Producto vigente

| Componente | Ruta |
| --- | --- |
| API principal Laravel | `../../backend-laravel/` |
| Microservicio de itinerarios FastAPI | `../../backend-itinerarios/` |
| Aplicación web Angular | `../../frontend-angular/` |
| Aplicación Android Kotlin | `../../android/` |

Para levantar cada componente, sigue su README. La web y Android usan los backends de la raíz.

## Evolución

| Etapa | Aporte conservado | Registro |
| --- | --- | --- |
| 1 | Historias de usuario y archivos de importación para Jira | `etapa-1-requisitos/` |
| 2 | HUs actualizadas y evolución de las APIs | `etapa-2/` |
| 3 | Integración de la web Angular y ajustes de compatibilidad en las APIs | `etapa-3/` |
| 4 | Aplicación Android y corrección de trazabilidad de la web | `etapa-4/` |
| 5 | Automatización de API, web, Android y rendimiento; trazabilidad y correcciones detectadas por las pruebas | `etapa-5/LEEME_ETAPA5.md` |

Cada etapa reemplaza o amplía la anterior; no se mantienen copias completas de backends y frontend en cada carpeta. Para recuperar un snapshot de código anterior, usa los commits de Git correspondientes a esa etapa.