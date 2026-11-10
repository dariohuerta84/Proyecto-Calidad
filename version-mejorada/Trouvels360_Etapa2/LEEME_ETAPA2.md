# Trouvels360 — Etapa 2: Backend (Laravel + microservicio Python)

| Carpeta | Qué es |
| --- | --- |
| `trouvels360-backend-laravel/` | API REST (Laravel 12). Reemplaza a la carpeta del backend anterior. |
| `trouvels360-itinerarios/` | Microservicio FastAPI de itinerarios (TRV-404). Reemplaza al anterior. |
| `HUs_actualizadas/` | Las HUs de la etapa 1 con el único ajuste de esta etapa (límite de login por correo + IP). |

## Cómo levantarlo (Windows)

1. Abre Docker Desktop y espera "Engine running".
2. Si tenías el backend anterior corriendo, entra a su carpeta y ejecuta `detener.bat` (o `docker compose down`).
3. En `trouvels360-backend-laravel`: doble clic en **`levantar.bat`**. Al terminar abre http://localhost:8000/api/ping.
4. En `trouvels360-itinerarios`: doble clic en **`levantar.bat`**. Al terminar abre http://localhost:8001/api/health.
5. (Opcional) `probar.bat` en cada carpeta ejecuta sus pruebas: 60 en Laravel y 9 en Python.

Los `.env` ya vienen listos y con la clave compartida entre Laravel (`JWT_MS_SECRET`) y Python (`JWT_SECRET`).
`levantar.bat` recrea la base de datos desde cero con los datos de prueba de `docs/DATOS_DE_PRUEBA.md`.

## Importante para las siguientes etapas

La web y la app móvil actuales todavía usan el contrato anterior de la API. Hasta las etapas 3 y 4:
- Reservar (hotel o tour) desde la web o la app fallará: ahora la API exige los datos del pago simulado y el header `Idempotency-Key`.
- "Mis reservas" de la app cambia de formato (ahora paginado).
- El registro exige `password_confirmation`.

El detalle de cada cambio está en `trouvels360-backend-laravel/docs/CAMBIOS_ETAPA2.md` y los endpoints en `docs/API.md`.
