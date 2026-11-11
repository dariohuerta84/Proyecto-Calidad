# Trouvels360 — Backend (Laravel 12 + JWT)

API REST del marketplace de turismo Trouvels360. La consumen la web (Angular) y la app Android (Kotlin).
Se apoya en un **microservicio Python (FastAPI)** que genera itinerarios sugeridos.

```
Web Angular ─┐                         ┌─> MySQL 8
             ├─> Laravel API :8000 ────┤
App Android ─┘   (JWT cookie + CSRF)   └─> Microservicio itinerarios :8001 (JWT de servicio)
```

- **Roles:** viajero (busca, reserva, cancela, opina) y proveedor (publica, aprueba o rechaza solicitudes de hotel).
- **Flujo de aprobación:** reserva de hotel → PENDIENTE → el proveedor la ACEPTA (confirmada) o la RECHAZA (libera unidades).
- **Tours:** confirmación automática con descuento de cupos.
- **Importes:** subtotal + IGV 18% = total, guardados al céntimo.

## Levantar en local (Docker)

Requisito: Docker Desktop abierto ("Engine running").

**Windows:** doble clic en `levantar.bat` (primera vez) · `iniciar.bat` / `detener.bat` (días siguientes).

**Manual (cualquier sistema):**
```bash
cp .env.example .env          # el zip ya trae un .env listo
docker compose up --build -d
docker compose exec app composer install
docker compose exec app php artisan key:generate   # solo si APP_KEY está vacío
docker compose exec app php artisan jwt:secret     # solo si JWT_SECRET está vacío
docker compose exec app php artisan migrate:fresh --seed
```
Comprobar: http://localhost:8000/api/ping → `{"pong":true}` · phpMyAdmin: http://localhost:8080

Después levanta el microservicio de itinerarios (carpeta `itinerarios`, ver su README).

## Pruebas

```bash
docker compose exec app php artisan test          # 60 pruebas de integración de la API (Pest)
```
Cada prueba lleva en su nombre el criterio de aceptación que valida (`TRV-203-04 ...`).

## Documentación

| Archivo | Contenido |
| --- | --- |
| `docs/API.md` | Endpoints, parámetros, códigos de respuesta, CSRF e idempotencia |
| `docs/DATOS_DE_PRUEBA.md` | Cuentas, hoteles y tours fijos para las pruebas, tarjetas del pago simulado |
| `docs/CAMBIOS_ETAPA2.md` | Defectos corregidos y funcionalidad nueva, con su historia de usuario |

## Estructura relevante

```
app/
├── Http/Controllers/Api/   # Un controlador por recurso (HU indicada en cada método)
├── Http/Middleware/        # AttachJwtFromCookie, VerifyApiCsrf, Idempotency
├── Services/               # Disponibilidad (por noche), EliminacionServicio, ReservaPresenter
├── Support/                # Precio (IGV en céntimos), PagoSimulado, Telefono
└── Rules/                  # TarjetaLuhn, VencimientoTarjeta
database/seeders/QaSeeder.php   # Datos deterministas para las suites
tests/Feature/                  # Pruebas por épica
```
