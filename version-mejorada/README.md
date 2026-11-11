# Trouvels360 - Version mejorada

Esta carpeta representa la evolución del mismo producto. El código vigente vive una sola vez en `producto/`; `etapas/` conserva los entregables propios y explica qué cambió en cada hito. Los snapshots completos anteriores permanecen en el historial de Git.

## Producto vigente

| Componente | Ruta |
| --- | --- |
| API principal Laravel | `producto/backend-laravel/` |
| Microservicio de itinerarios FastAPI | `producto/backend-itinerarios/` |
| Aplicación web Angular | `producto/frontend-angular/` |
| Aplicación Android Kotlin | `producto/android/` |

Para levantar cada componente, sigue su README. La web y Android usan los backends incluidos en este mismo `producto/`.

## Evolución

| Etapa | Aporte conservado | Registro |
| --- | --- | --- |
| 1 | Historias de usuario y archivos de importación para Jira | `etapas/etapa-1-requisitos/` |
| 2 | HUs actualizadas y evolución de las APIs | `etapas/etapa-2/` |
| 3 | Integración de la web Angular y ajustes de compatibilidad en las APIs | `etapas/etapa-3/` |
| 4 | Aplicación Android y corrección de trazabilidad de la web | `etapas/etapa-4/` |

Cada etapa reemplaza o amplía la anterior; no se mantienen copias completas de backends y frontend en cada carpeta. Para recuperar un snapshot de código anterior, usa los commits de Git correspondientes a esa etapa.