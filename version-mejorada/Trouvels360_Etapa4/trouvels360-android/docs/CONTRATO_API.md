# Contrato app ↔ API

Cómo se verificó que la app habla el mismo idioma que el backend (etapa 4):

1. **Rutas:** cada anotación de `ApiService.kt` se comparó con `php artisan route:list`: **52 endpoints, 0 sin ruta**.
2. **Formato de respuestas:** se capturaron respuestas reales del backend (`app/src/test/resources/api/*.json`) y
   `ContratoApiTest` comprueba que se convierten a los modelos de `Dto.kt` sin perder campos (11 pruebas).
3. **Reglas de validación:** `ValidacionesTest` (9 pruebas) usa las mismas reglas que Laravel (Luhn, vencimiento, +51, RUC, 10–300).

## Endpoints usados por la app

| Método | Ruta |
|---|---|
| GET | `auth/csrf` |
| POST | `auth/login` |
| POST | `auth/register` |
| GET | `auth/me` |
| POST | `auth/refresh` |
| POST | `auth/logout` |
| GET | `usuarios/me` |
| PATCH | `usuarios/me` |
| PUT | `usuarios/me/password` |
| DELETE | `usuarios/me` |
| GET | `hoteles` |
| GET | `hoteles/{id}` |
| GET | `hoteles/{id}/disponibilidad` |
| POST | `reservas-habitaciones/cotizar` |
| POST | `reservas-habitaciones` |
| GET | `tours` |
| GET | `tours/{id}` |
| POST | `tours/salidas/{salida}/cotizar` |
| POST | `tours/salidas/{salida}/reservas` |
| GET | `mis-reservas` |
| POST | `reservas-habitaciones/{id}/cancelar` |
| GET | `tours/mis-reservas` |
| POST | `tours/reservas/{id}/cancelar` |
| GET | `reviews` |
| GET | `reviews/resumen` |
| GET | `reviews/puedo-opinar` |
| POST | `reviews` |
| PUT | `reviews/{id}` |
| DELETE | `reviews/{id}` |
| POST | `itineraries` |
| GET | `proveedor/indicadores` |
| GET | `proveedor/servicios` |
| DELETE | `servicios/{id}` |
| GET | `proveedor/reservas` |
| POST | `proveedor/reservas-habitaciones/{id}/aceptar` |
| POST | `proveedor/reservas-habitaciones/{id}/rechazar` |
| POST | `hoteles` |
| PUT | `hoteles/{id}` |
| POST | `hoteles/{id}/habitaciones` |
| PUT | `habitaciones/{id}` |
| DELETE | `habitaciones/{id}` |
| POST | `tours` |
| PUT | `tours/{id}` |
| GET | `tours/{id}/salidas` |
| POST | `tours/{id}/salidas` |
| PATCH | `tours/{id}/salidas/{salida}` |
| DELETE | `tours/{id}/salidas/{salida}` |
| GET | `tours/{id}/actividades` |
| POST | `tours/{id}/actividades` |
| PUT | `tours/{id}/actividades/{act}` |
| PATCH | `tours/{id}/actividades/{act}` |
| DELETE | `tours/{id}/actividades/{act}` |

## Errores que la app interpreta

| Código | Uso en la app |
|---|---|
| 401 | Intenta `auth/refresh` una vez; si falla, cierra sesión y abre el login con aviso |
| 403 | Muestra el mensaje (p. ej. reseña sin estadía confirmada, publicación ajena) |
| 409 | Muestra el mensaje (sin unidades/cupos, reserva ya decidida, eliminar con reservas activas, salida duplicada) y recotiza si aplica |
| 422 | Errores por campo bajo cada input (`errors.{campo}[0]`) |
| 400 | Falta `Idempotency-Key` (no debería ocurrir: la app siempre lo envía) |
