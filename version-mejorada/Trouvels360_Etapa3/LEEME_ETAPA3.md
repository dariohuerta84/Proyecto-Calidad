# Trouvels360 — Etapa 3: web Angular

Contenido:

| Carpeta | Qué es |
|---|---|
| `trouvels360-frontend-angular/` | Web Angular 19 completa y corregida (sin `node_modules`) |
| `trouvels360-backend-laravel/` | Backend de la etapa 2 **con 5 ajustes** detectados al probar la web (reemplaza al de la etapa 2) |

El microservicio Python (`trouvels360-itinerarios`) no cambia: sigue valiendo el de la etapa 2.

## Orden para levantar todo

1. Backend: dentro de `trouvels360-backend-laravel`, `levantar.bat` (Docker). Si ya tenías la base de la etapa 2,
   ejecuta `docker compose exec app php artisan migrate:fresh --seed` para cargar los datos corregidos.
2. Microservicio de itinerarios (etapa 2): `iniciar.bat`.
3. Web: dentro de `trouvels360-frontend-angular`, `iniciar-docker.bat` → http://localhost:4200
   (sin Docker: `iniciar-local.bat`, requiere Node 20+).

Usuarios: viajero `edwinviajero@gmail.com` / `viajero`, proveedor `edwinproveedor@gmail.com` / `proveedor`.
Tarjeta de prueba: `4242 4242 4242 4242`, `12/30`, CVC `123`.

## Documentos

- `trouvels360-frontend-angular/README.md` — pantallas, rutas y la HU que cubre cada una.
- `trouvels360-frontend-angular/docs/CAMBIOS_ETAPA3.md` — todo lo corregido y cómo se verificó.
- `trouvels360-frontend-angular/docs/SELECTORES_SELENIUM.md` — 373 `data-testid` para la etapa 5.

Siguiente: etapa 4 (app móvil Kotlin para viajero y proveedor, con `testTag` para Appium).
