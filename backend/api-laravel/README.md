# API Laravel de Trouvels360

[← Backend](../README.md)

API REST principal del marketplace. La usan la web Angular y la app Android. Cubre las 30 historias de usuario salvo el
cálculo del itinerario, que delega en la [API Python](../api-python/README.md).

| | |
|---|---|
| Tecnología | Laravel 12 · PHP 8.2 · Apache · MySQL 8 |
| Dirección local | http://localhost:8000/api (salud: `/api/ping`) |
| Autenticación | JWT en cookie HttpOnly `access_token` (60 min) + CSRF de doble envío (`X-XSRF-TOKEN`) |
| Referencia de endpoints | [`docs/API.md`](docs/API.md) |
| Datos de prueba | [`docs/DATOS_DE_PRUEBA.md`](docs/DATOS_DE_PRUEBA.md) |
| Defectos corregidos en la etapa 2 | [`docs/CAMBIOS_ETAPA2.md`](docs/CAMBIOS_ETAPA2.md) |

## Qué hace

- **Viajero:** registro y sesión, perfil, búsqueda de hoteles y tours, cotización con IGV (18 %), reserva con pago
  simulado, mis reservas, cancelación, reseñas e itinerario sugerido.
- **Proveedor:** registro de empresa, panel con indicadores, hoteles y tipos de habitación, tours con actividades y
  salidas, reservas recibidas y **aprobación o rechazo** de las solicitudes de hotel.
- **Reglas que protege:** nunca vende de más (bloqueo de fila), no duplica reservas (`Idempotency-Key`), cada usuario solo
  toca lo suyo (403/404) y los importes se guardan al céntimo.

Cada ruta de `routes/api.php` y cada prueba de `tests/Feature` indica la historia (`HU-V05`) o el criterio (`CA-V07-02`)
que cubre, con los mismos códigos de [`docs/1-historias-de-usuario`](../../docs/1-historias-de-usuario/README.md).

## Estructura

```
api-laravel/
├── app/
│   ├── Http/Controllers/Api/   un controlador por recurso (Auth, Hotel, ReservaHabitacion, Tour, ProveedorReserva…)
│   ├── Http/Middleware/        AttachJwtFromCookie (sesión), VerifyApiCsrf (CSRF), Idempotency (reservas)
│   ├── Services/               Disponibilidad por noche, EliminacionServicio, ReservaPresenter
│   ├── Support/                Precio (IGV en céntimos), PagoSimulado, Telefono
│   └── Rules/                  TarjetaLuhn, VencimientoTarjeta
├── routes/api.php              todas las rutas, con su historia
├── database/
│   ├── migrations/
│   └── seeders/                DatabaseSeeder (catálogo de demostración) + QaSeeder (datos fijos para qa/)
├── tests/Feature/              61 pruebas Pest, nombradas por criterio de aceptación
├── docker/
│   ├── apache/000-default.conf sitio de Apache apuntando a /public
│   └── entrypoint.sh           espera a MySQL, prepara la base y arranca Apache
├── Dockerfile                  imagen única para local y nube
├── docker-compose.yml          API + MySQL (+ phpMyAdmin opcional)
├── .env.example                configuración comentada, lista para desarrollo local
└── levantar.bat · detener.bat · reiniciar-datos.bat · probar.bat
```

## Levantar con Docker

**Windows:** doble clic en `levantar.bat`. Copia `.env.example` a `.env` si falta, construye la imagen, levanta MySQL y la
API, y espera a que `/api/ping` responda.

**Terminal:**

```bash
cp .env.example .env          # solo la primera vez
docker compose up -d --build
docker compose logs -f app    # ver el arranque (Ctrl+C para salir)
```

Qué pasa al arrancar el contenedor (`docker/entrypoint.sh`):

1. Comprueba que existan `APP_KEY` y `JWT_SECRET`.
2. Espera hasta 2 minutos a que MySQL acepte conexiones.
3. Prepara la base según `BD_AL_INICIAR`: `migrar` (por defecto: aplica migraciones y carga los datos de prueba solo
   si la base está vacía), `reiniciar` (borra y recrea todo en cada inicio) o `nada`.
4. Inicia Apache en el puerto `PORT` (80 dentro del contenedor; publicado en `APP_PORT`, 8000 por defecto).

| Acción | Windows | Terminal |
|---|---|---|
| Levantar o aplicar cambios de código | `levantar.bat` | `docker compose up -d --build` |
| Aplicar cambios del `.env` | — | `docker compose up -d` |
| Recrear los datos de prueba | `reiniciar-datos.bat` | `docker compose exec app php artisan migrate:fresh --seed --force` |
| Ejecutar las pruebas | `probar.bat` | `docker compose exec app php artisan test` |
| Ver registros | — | `docker compose logs -f app` |
| phpMyAdmin (http://localhost:8080) | — | `docker compose --profile herramientas up -d` |
| Detener (conserva los datos) | `detener.bat` | `docker compose --profile herramientas down` |
| Borrar también la base | — | `docker compose down -v` |

## Configuración (`.env`)

`.env.example` explica cada variable. Las más importantes:

| Variable | Local | En la nube |
|---|---|---|
| `APP_URL` | `http://localhost:8000` | `https://api.midominio.com` |
| `APP_PORT` | `8000` | — (la plataforma usa `PORT`) |
| `APP_KEY`, `JWT_SECRET` | valores de ejemplo, solo para local | generar nuevos (ver [backend](../README.md#1-generar-claves-nuevas-nunca-uses-las-del-ejemplo-en-producción)) |
| `DB_HOST` … `DB_PASSWORD` | `mysql` (el contenedor) / `trouvels360` / `trouvels` / `trouvels123` | los de la base administrada |
| `BD_AL_INICIAR` | `migrar` (o `reiniciar` antes de correr `qa/`) | `migrar` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | `https://app.midominio.com` |
| `COOKIE_SECURE` / `COOKIE_DOMAIN` / `COOKIE_SAMESITE` | `false` / vacío / `Lax` | `true` / vacío (la web reenvía `/api`) / `Lax` |
| `LOGIN_RATE_LIMIT` / `REGISTER_RATE_LIMIT` | `6` / `10` (`1000` solo para correr `qa/`) | `6` / `10` |
| `PYTHON_MICROSERVICE_URL` | `http://host.docker.internal:8001` | `https://itinerarios.midominio.com` |
| `JWT_MS_SECRET` | igual a `JWT_SECRET` de la API Python | clave compartida nueva |
| `JWT_MS_ISSUER` | `http://localhost:8000` (igual a `JWT_ISSUER` de la API Python) | `https://api.midominio.com` |

## Pruebas

```bash
docker compose exec app php artisan test
```

61 pruebas de integración con Pest (SQLite en memoria, no tocan tu base). Su nombre empieza con el criterio que validan,
por ejemplo `CA-V07-01 registra la solicitud PENDIENTE con los importes cotizados`. Las pruebas de API de punta a punta
de la rúbrica están en `qa/karate`.

## Desplegar en la nube

El `Dockerfile` sirve tal cual en cualquier plataforma con Docker: instala dependencias al construir, espera a la base
y migra al iniciar, y escucha en la variable `PORT` si la plataforma la define. Pasos y variables en
[backend → Desplegar en la nube](../README.md#desplegar-en-la-nube-cada-una-en-su-dominio).
