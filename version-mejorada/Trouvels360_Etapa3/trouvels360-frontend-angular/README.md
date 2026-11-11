# Trouvels360 — Frontend web (Angular 19)

Web de reservas de hoteles y tours con **flujo de aprobación**: el viajero paga y su solicitud de hotel queda
**PENDIENTE** hasta que el proveedor la **acepta o rechaza con motivo**. Los tours se confirman al pagar.
Toda la información sale de la API Laravel (no hay datos simulados en el navegador).

## Cómo levantarla

Primero levanta el backend (`trouvels360-backend-laravel`, puerto 8000). Luego elige una opción:

| Opción | Pasos | URL |
|---|---|---|
| **Docker** (recomendado) | Doble clic en `iniciar-docker.bat` (o `docker compose up --build`) | http://localhost:4200 |
| **Sin Docker** (Node 20+) | Doble clic en `iniciar-local.bat` (o `npm install` y `npm run start:local`) | http://localhost:4200 |

- Docker usa `proxy.conf.json` → `http://host.docker.internal:8000`.
- Sin Docker usa `proxy.conf.local.json` → `http://localhost:8000`.
- `compilar.bat` (o `npm run build:prod`) compila en modo producción para comprobar que no hay errores.

## Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Viajero | edwinviajero@gmail.com | `viajero` |
| Proveedor | edwinproveedor@gmail.com | `proveedor` |
| Proveedor 2 | qa.proveedor2@trouvels360.test | `Proveedor123` |
| Viajeros QA | qa.viajero1 … qa.viajero30@trouvels360.test | `Viajero123` |

Tarjeta de prueba (pago simulado): `4242 4242 4242 4242`, vencimiento futuro (`12/30`), CVC `123`.
Una tarjeta que no pasa Luhn (`4242 4242 4242 4241`) se rechaza en el formulario y en la API.

## Pantallas principales

| Ruta | Rol | Historia |
|---|---|---|
| `/auth/login`, `/auth/registro` | todos | TRV-101, TRV-102, TRV-103 |
| `/hoteles`, `/hoteles/resultados` | público | TRV-201 (búsqueda paginada, vacío ≠ error) |
| `/hoteles/detalle/:id` | público | TRV-202 (unidades libres por fechas), TRV-205 (itinerario opcional), TRV-401..404 (reseñas) |
| `/hoteles/pagos` | viajero | TRV-203 (cotización oficial con IGV 18 %, pago simulado, idempotencia) |
| `/tour`, `/tour/detalle/:id`, `/tour/pagos` | público / viajero | TRV-301, TRV-302, TRV-303 |
| `/mis-reservas` | viajero | TRV-206, TRV-304 (filtros, cancelar, motivo de rechazo) |
| `/editar-perfil` | ambos | TRV-104 (datos, contraseña, foto, eliminar cuenta) |
| `/proveedor` | proveedor | TRV-501 (indicadores por estado, publicaciones paginadas), TRV-508 (eliminar) |
| `/proveedor/reservas` | proveedor | TRV-507 y TRV-204 (aceptar / rechazar con motivo) |
| `/proveedor/crear-hotel`, `/proveedor/editar-hotel/:id` | proveedor | TRV-502, TRV-503 |
| `/proveedor/crear-tour`, `/proveedor/editar-tour/:id` | proveedor | TRV-504 |
| `/proveedor/tour/:id/programa` | proveedor | TRV-505 (actividades con orden), TRV-506 (salidas, cupos, cerrar/cancelar) |

## Seguridad en el navegador

- La sesión es una cookie **HttpOnly** (`access_token`); el JavaScript nunca ve el JWT.
- El interceptor `core/interceptors/auth.interceptor.ts` envía `withCredentials`, añade `X-XSRF-TOKEN`
  en POST/PUT/PATCH/DELETE (lo pide a `/api/auth/csrf` si falta), reintenta una vez ante un 403 de CSRF y,
  ante un 401, intenta `/api/auth/refresh`; si falla, lleva a `/auth/login?returnUrl=…&expirada=1`.
- Rutas protegidas con `authGuard`, `viajeroGuard` y `proveedorGuard`.
- `Idempotency-Key` en cada intento de pago: reintentar tras un corte de red no duplica la reserva.

## Selectores para Selenium

Todos los elementos que se prueban tienen `data-testid` (373 en total). Ver `docs/SELECTORES_SELENIUM.md`.
Ejemplo: `driver.findElement(By.cssSelector("[data-testid='btn-aceptar']"))`.
Las tarjetas exponen datos para aserciones: `[data-testid=reserva-card][data-codigo=…][data-estado=pendiente]`.
No se usan `alert()` ni `confirm()` del navegador: todos los avisos y confirmaciones son elementos del DOM.

## Estructura

```
src/app/
  core/        interceptor, guards, AuthService, utilidades (mensajes de error de la API, idempotencia)
  shared/      modelos, mis-reservas, pago-simulado, reseñas, buscador, header, editar-perfil
  features/
    auth/      login y registro
    hoteles/   búsqueda, detalle, pago
    tours/     búsqueda, detalle, pago
    proveedor/ panel, reservas recibidas, salidas y actividades, formularios de hotel y tour
```
