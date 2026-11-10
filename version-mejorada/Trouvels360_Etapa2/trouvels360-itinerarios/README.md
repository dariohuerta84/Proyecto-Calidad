# Trouvels360 — Microservicio de itinerarios (FastAPI)

Genera **itinerarios sugeridos** (TRV-404) a partir de los tours del destino, sus reseñas y la distancia al hotel.
No reserva nada: es una propuesta que la web y la app muestran rotulada como "Sugerencia".

```
App / Web ──> Laravel POST /api/itineraries ──(JWT de servicio HS256, 5 min)──> FastAPI POST /api/itinerary ──> MySQL (lectura)
```

- **Seguridad:** solo acepta peticiones firmadas por Laravel (`aud=fastapi-itinerarios`, `iss=APP_URL`). Sin token o con token inválido → 401.
- **Algoritmo:** filtra tours activos de la ciudad por interés, puntúa por calificación, distancia y precio, y reparte por días (máx. 8 h/día) con vecino más cercano.
- **Validaciones:** 1 a 14 días, interés en Aventura · Relajación · Cultura · Gastronomía, hotel activo en el destino.

## Levantar (Docker)

1. Levanta antes el backend Laravel (crea la red `trouvels360_network` y la base MySQL).
2. Revisa `.env.fastapi`: `JWT_SECRET` debe ser **igual** a `JWT_MS_SECRET` del `.env` de Laravel (el zip ya los trae iguales).
3. Windows: doble clic en `levantar.bat`. Manual: `docker compose up --build -d`.
4. Comprobar: http://localhost:8001/api/health · documentación interactiva: http://localhost:8001/docs

## Pruebas

```bash
docker compose exec itinerarios-api sh -c "pip install -q pytest==8.3.3 httpx==0.25.2 && python -m pytest -q tests"
```
(o `probar.bat`). Usan SQLite en memoria: no necesitan MySQL.

| Prueba | Valida |
| --- | --- |
| `test_sin_token_responde_401`, `..._firma_o_audiencia...`, `..._expirado...` | JWT de servicio obligatorio |
| `test_itinerario_valido` | Estructura, días, máximo de horas y que no aparezcan tours eliminados |
| `test_interes_invalido_responde_422`, `test_mas_de_14_dias_responde_422` | Validaciones |
| `test_sin_actividades_...`, `test_hotel_de_otra_ciudad...` | Errores de negocio (404) |

## Configuración (`.env.fastapi`)

| Variable | Valor local |
| --- | --- |
| `DB_HOST` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `mysql` / `trouvels360` / `trouvels` / `trouvels123` (los mismos de Laravel) |
| `JWT_SECRET` | igual a `JWT_MS_SECRET` de Laravel |
| `JWT_ISSUER` | `http://localhost:8000` (el `APP_URL` de Laravel) |
| `DATABASE_URL` | opcional, reemplaza a `DB_*` (p. ej. `sqlite:///ruta.db`) |
