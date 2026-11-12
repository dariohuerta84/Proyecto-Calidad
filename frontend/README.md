# Web de Trouvels360 (Angular)

Aplicación web para los dos roles del marketplace: el **viajero** busca, cotiza y reserva hoteles y tours; el
**proveedor** publica su oferta y acepta o rechaza las solicitudes de hotel. Todos los datos salen de la
[API Laravel](../backend/api-laravel/README.md): la web no guarda reservas ni calcula importes por su cuenta.

| | |
|---|---|
| Tecnología | Angular 19 (componentes standalone) · TypeScript · nginx para servirla en Docker |
| Dirección local | http://localhost:4200 |
| Necesita | La API Laravel en http://localhost:8000 (ver [backend](../backend/README.md)) |
| Selectores para Selenium | [`docs/SELECTORES_SELENIUM.md`](docs/SELECTORES_SELENIUM.md) (387 `data-testid`) |

## Cómo habla con la API

La web siempre llama a **`/api` en su mismo dominio** (`environment.apiUrl = '/api'`), y algo delante la reenvía a la
API Laravel:

```
Navegador ──> http://localhost:4200/api/... ──> (nginx o servidor de desarrollo) ──> http://localhost:8000/api/...
```

| Modo | Quién reenvía `/api` | Configuración |
|---|---|---|
| Docker (`levantar.bat`) | nginx dentro del contenedor | `API_URL` en `.env` |
| Desarrollo (`desarrollo.bat`) | servidor de desarrollo de Angular | `proxy.conf.json` |

Ventaja: para el navegador la web y la API son **un solo origen**, así que la cookie de sesión (`access_token`,
HttpOnly) y el token CSRF funcionan sin configurar CORS, y la misma imagen sirve en local y en la nube cambiando solo
`API_URL`.

## Levantar con Docker

Requisitos: Docker Desktop abierto y la API levantada (`backend\levantar-todo.bat`).

**Windows:** doble clic en `levantar.bat`. Copia `.env.example` a `.env` si falta, compila la web dentro de Docker,
levanta nginx, comprueba que llegue a la API y abre http://localhost:4200. Para detener: `detener.bat`.

**Terminal:**

```bash
cp .env.example .env            # solo la primera vez
docker compose up -d --build    # compila (1-3 min la primera vez) y levanta → http://localhost:4200
docker compose logs -f web      # ver registros
docker compose down             # detener
```

Qué hace el `Dockerfile`, en dos etapas:

1. **Compilación** (Node 20): `npm ci` y `ng build` en modo producción.
2. **Servidor** (nginx 1.27): copia solo los archivos compilados y al iniciar arma su configuración desde
   `docker/default.conf.template` con `API_URL` y `PORT`. nginx sirve la web, reenvía `/api/` a la API, devuelve
   `index.html` en las rutas de Angular (`/mis-reservas`, `/proveedor`…) y responde `/salud` para el chequeo de salud.

## Configuración (`.env`)

| Variable | Valor local | En la nube |
|---|---|---|
| `API_URL` | `http://host.docker.internal:8000` | `https://api.midominio.com` |
| `PUERTO_WEB` | `4200` | — (la plataforma usa `PORT`) |

`API_URL` va **sin `/api` y sin barra final**. Se lee al iniciar el contenedor: para cambiarlo basta
`docker compose up -d`, sin volver a compilar.

## Modo desarrollo (sin Docker)

Para trabajar en el código con recarga automática al guardar. Requiere Node 20 o superior.

```bash
npm ci          # o doble clic en desarrollo.bat, que hace todo
npm start       # http://localhost:4200, reenvía /api a http://localhost:8000 (proxy.conf.json)
npm run build   # compilación de producción en dist/trouvels360-web/browser
```

## Desplegar en la nube

La misma imagen sirve en cualquier plataforma con Docker (Railway, Render, Fly.io, un VPS):

1. Crea un servicio con la carpeta `frontend/` (tiene `Dockerfile`).
2. Define `API_URL=https://api.midominio.com` (la dirección pública de la API Laravel). La plataforma define `PORT`.
3. Asigna su dominio, por ejemplo `https://app.midominio.com`.
4. En la API Laravel: `COOKIE_SECURE=true` y `COOKIE_DOMAIN` vacío (la cookie queda en el dominio de la web, porque el
   navegador solo habla con ella). Variables completas en [backend](../backend/README.md#desplegar-en-la-nube-cada-una-en-su-dominio).

En un VPS, pon delante el mismo proxy con HTTPS del backend agregando
`app.midominio.com { reverse_proxy localhost:4200 }`.

## Pantallas

| Ruta | Rol | Historias |
|---|---|---|
| `/auth/login`, `/auth/registro` | todos | HU-V01, HU-V02, HU-P01, HU-P02 |
| `/hoteles`, `/hoteles/resultados` | público | HU-V05 (búsqueda paginada, vacío ≠ error) |
| `/hoteles/detalle/:id` | público | HU-V06 (unidades libres, cotización), HU-V14 (reseñas), HU-V15 (itinerario) |
| `/hoteles/pagos` | viajero | HU-V07 (pago simulado, IGV 18 %, idempotencia) |
| `/tour`, `/tour/resultados`, `/tour/detalle/:id`, `/tour/pagos` | público / viajero | HU-V08, HU-V09, HU-V10 |
| `/mis-reservas` | viajero | HU-V11, HU-V12 (estado y motivo de rechazo), HU-V13 (cancelar) |
| `/editar-perfil` | ambos | HU-V03, HU-V04, HU-P02 (datos, contraseña, foto, eliminar cuenta) |
| `/proveedor` | proveedor | HU-P03, HU-P04 (publicaciones e indicadores), HU-P15 (eliminar) |
| `/proveedor/reservas` | proveedor | HU-P12, HU-P13, HU-P14 (bandeja, aceptar, rechazar con motivo) |
| `/proveedor/crear-hotel`, `/proveedor/editar-hotel/:id` | proveedor | HU-P05, HU-P06, HU-P07 |
| `/proveedor/crear-tour`, `/proveedor/editar-tour/:id` | proveedor | HU-P08, HU-P09 |
| `/proveedor/tour/:id/programa` | proveedor | HU-P10 (actividades), HU-P11 (salidas y cupos) |

Usuarios de prueba: viajero `edwinviajero@gmail.com` / `viajero` · proveedor `edwinproveedor@gmail.com` / `proveedor`.
Tarjeta del pago simulado: `4242 4242 4242 4242`, vencimiento futuro, CVC `123`.

## Estructura

```
frontend/
├── src/app/
│   ├── core/          interceptor (cookie, CSRF, sesión vencida), guards por rol, AuthService, utilidades
│   ├── shared/        modelos y componentes comunes (buscador, mis reservas, pago simulado, reseñas, cabecera)
│   └── features/
│       ├── auth/      login y registro
│       ├── hoteles/   búsqueda, detalle y pago
│       ├── tours/     búsqueda, detalle y pago
│       └── proveedor/ panel, reservas recibidas, formularios de hotel y tour, salidas y actividades
├── docker/default.conf.template   configuración de nginx
├── Dockerfile · docker-compose.yml · .env.example
├── proxy.conf.json                 /api → localhost:8000 en modo desarrollo
├── docs/                           selectores para Selenium y cambios de la etapa 3
└── levantar.bat · detener.bat · desarrollo.bat
```

## Seguridad en el navegador

- La sesión es una cookie **HttpOnly**: el JavaScript nunca ve el JWT.
- `core/interceptors/auth.interceptor.ts` envía las cookies, agrega `X-XSRF-TOKEN` en POST/PUT/PATCH/DELETE (lo pide a
  `/api/auth/csrf` si falta), reintenta una vez ante un 403 de CSRF y, ante un 401, renueva la sesión o lleva al login
  conservando la página (`returnUrl`).
- Las rutas privadas usan `authGuard`, `viajeroGuard` y `proveedorGuard`.
- Cada pago envía un `Idempotency-Key`: reintentar tras un corte de red no duplica la reserva.
- Las fechas se manejan en la hora local del usuario (`shared/utils/date.utils.ts`), no en UTC.

## Pruebas

- **Selenium** (`qa/selenium`): 33 casos de punta a punta sobre esta web, con selectores `data-testid`. Verificados
  33 / 33 contra la web compilada y servida por nginx.
- `npm run build` sin errores ni advertencias.

## Problemas frecuentes

| Síntoma | Solución |
|---|---|
| La web carga pero no inicia sesión ni muestra datos | La API no está levantada o `API_URL` está mal. Prueba http://localhost:4200/api/ping (debe responder `{"pong":true}`). |
| `port is already allocated` | Otro programa usa el 4200 (por ejemplo `ng serve`). Ciérralo o cambia `PUERTO_WEB` en `.env`. |
| Cambié código y no se ve | `docker compose up -d --build` (el código va dentro de la imagen). |
| Cambié `API_URL` y no se nota | `docker compose up -d` (recrea el contenedor con el nuevo valor). |
