# Trouvels360 — Etapa 3: Integración de la web Angular

Este hito incorporó la web y actualizó los backends de la etapa 2. El código integrado vigente está en la raíz; no se conserva una segunda copia completa en esta carpeta. Los snapshots de cada entrega están en el historial de Git.

Contenido:

| Carpeta | Qué es |
|---|---|
| `../../../frontend-angular/` | Web Angular 19 integrada; incluye la corrección de trazabilidad de la etapa 4 |
| `../../../backend-laravel/` | Backend con los ajustes detectados al probar la web |
| `../../../backend-itinerarios/` | Microservicio Python con las correcciones de itinerarios |

Estos componentes son la base de la versión integrada; no uses copias antiguas de las etapas anteriores para ejecutar la aplicación.

## Orden para levantar todo

1. Backend: en `../../../backend-laravel/`, ejecuta `levantar.bat` (Docker). Si ya tenías la base de la etapa 2,
   ejecuta `docker compose exec app php artisan migrate:fresh --seed` para cargar los datos corregidos.
2. Microservicio de itinerarios: en `../../../backend-itinerarios/`, ejecuta `levantar.bat`.
3. Web: en `../../../frontend-angular/`, ejecuta `iniciar-docker.bat` → http://localhost:4200
   (sin Docker: `iniciar-local.bat`, requiere Node 20+).

Usuarios: viajero `edwinviajero@gmail.com` / `viajero`, proveedor `edwinproveedor@gmail.com` / `proveedor`.
Tarjeta de prueba: `4242 4242 4242 4242`, `12/30`, CVC `123`.

## Documentos

- `../../../frontend-angular/README.md` — pantallas, rutas y la HU que cubre cada una.
- `../../../frontend-angular/docs/CAMBIOS_ETAPA3.md` — cambios y verificación.
- `../../../frontend-angular/docs/SELECTORES_SELENIUM.md` — selectores `data-testid` para la etapa 5.

La etapa 4 añade Android y la corrección de trazabilidad; consulta [Etapa 4](../etapa-4/LEEME_ETAPA4.md).
