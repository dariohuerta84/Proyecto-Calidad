# API Python de itinerarios (Trouvels360)

[← Backend](../README.md)

Microservicio que arma un **itinerario sugerido** día por día para la estadía de un viajero (historia
[HU-V15](../../docs/1-historias-de-usuario/viajero.md)). No reserva nada: es una propuesta que la web y la app muestran
rotulada como «Sugerencia». Solo la llama la [API Laravel](../api-laravel/README.md).

| | |
|---|---|
| Tecnología | FastAPI · Python 3.11 · SQLAlchemy (lectura de MySQL) |
| Dirección local | http://localhost:8001/api (salud: `/api/health`) |
| Documentación interactiva | http://localhost:8001/docs |
| Seguridad | Solo acepta peticiones firmadas por la API Laravel (JWT HS256, `aud=fastapi-itinerarios`) |

## Cómo funciona

```
Web / App ──> Laravel POST /api/itineraries ──(JWT de servicio, 5 min)──> esta API POST /api/itinerary ──> MySQL (solo lectura)
```

1. **Seguridad:** sin token, con firma, emisor o audiencia incorrectos, o vencido → 401.
2. **Validación:** de 1 a 14 días; interés Aventura, Relajación, Cultura o Gastronomía (otro → 422); el hotel debe estar
   activo en el destino (si no → 404).
3. **Selección:** toma los tours activos de la ciudad con ese interés **que tengan una salida programada con cupo durante
   la estadía**, y los puntúa por calificación, distancia al hotel y precio.
4. **Reparto:** asigna cada tour a un día en que realmente sale (vecino más cercano), con máximo 8 horas por día; un tour de
   día completo ocupa un día vacío entero. Cada actividad trae su salida (`salida_id`, `hora`, `cupos_disponibles`) para
   poder reservarla. Los días sin actividades tienen `fecha_sugerida` nula.

| Método | Ruta | Para qué |
|---|---|---|
| `GET` | `/api/health` | Comprobar que está viva (sin token) |
| `POST` | `/api/itinerary` | Generar la propuesta (requiere el JWT de servicio) |
| `GET` | `/docs` | Documentación interactiva (Swagger) |

## Estructura

```
api-python/
├── app/
│   ├── main.py              crea la app, CORS y rutas
│   ├── security.py          verificación del JWT de servicio
│   ├── config/              settings (variables de entorno) y conexión a la base
│   ├── models/              tablas que lee (servicios, hoteles, tours, salidas, reseñas)
│   ├── schemas/             entrada y salida validadas con Pydantic
│   ├── routers/itinerary.py endpoints
│   └── services/            algoritmo de selección y reparto
├── tests/                   13 pruebas pytest (SQLite en memoria)
├── Dockerfile               imagen única para local y nube
├── docker-compose.yml
├── .env.fastapi.example     configuración comentada, lista para desarrollo local
├── railway.json             despliegue en Railway
└── levantar.bat · detener.bat · probar.bat
```

## Levantar con Docker

Para **generar itinerarios** necesita la base de datos, así que levanta antes la [API Laravel](../api-laravel/README.md)
(su MySQL queda publicada en el puerto 3306 de tu equipo). `/api/health` y las pruebas funcionan sin ella.

**Windows:** doble clic en `levantar.bat` (copia `.env.fastapi.example` a `.env.fastapi` si falta, construye, levanta y
abre la documentación).

**Terminal:**

```bash
cp .env.fastapi.example .env.fastapi   # solo la primera vez
docker compose up -d --build
docker compose logs -f api-python
```

| Acción | Windows | Terminal |
|---|---|---|
| Levantar o aplicar cambios de código | `levantar.bat` | `docker compose up -d --build` |
| Aplicar cambios de `.env.fastapi` | — | `docker compose up -d` |
| Ejecutar las pruebas | `probar.bat` | `docker compose exec api-python python -m pytest -q tests` |
| Detener | `detener.bat` | `docker compose down` |

Para publicarla en otro puerto de tu equipo: `PUERTO_PYTHON=8101 docker compose up -d` (y ajusta
`PYTHON_MICROSERVICE_URL` en la API Laravel).

## Configuración (`.env.fastapi`)

| Variable | Local | En la nube |
|---|---|---|
| `DB_HOST` / `DB_PORT` | `host.docker.internal` / `3306` (la MySQL de api-laravel publicada en tu equipo) | host y puerto de la base administrada |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `trouvels360` / `trouvels` / `trouvels123` (iguales a api-laravel) | los mismos que use api-laravel (idealmente un usuario de solo lectura) |
| `DATABASE_URL` | vacío | opcional: una URL completa reemplaza a `DB_*` |
| `JWT_SECRET` | igual a `JWT_MS_SECRET` de api-laravel | la clave compartida nueva |
| `JWT_ISSUER` | `http://localhost:8000` (igual a `JWT_MS_ISSUER` de api-laravel) | `https://api.midominio.com` |
| `JWT_AUDIENCE` | `fastapi-itinerarios` | igual |
| `ALLOWED_ORIGINS` | `http://localhost:4200,http://localhost:8000` | `https://api.midominio.com` |
| `PORT` | 8001 (fijado en la imagen) | lo asigna la plataforma |

## Pruebas

```bash
docker compose exec api-python python -m pytest -q tests
```

| Prueba | Valida |
|---|---|
| `test_sin_token_responde_401`, `…_firma_o_audiencia…`, `…_expirado…` | El JWT de servicio es obligatorio |
| `test_itinerario_valido` | Estructura, días, máximo de horas y que no aparezcan tours eliminados |
| `test_interes_invalido_responde_422`, `test_mas_de_14_dias_responde_422` | Validaciones |
| `test_sin_actividades_…`, `test_hotel_de_otra_ciudad…` | Errores de negocio (404) |
| `test_solo_propone_tours_en_fechas_con_salida_y_cupo` | Cada tour cae en un día con salida; se excluyen salidas llenas, cerradas o fuera de la estadía |
| `test_usa_la_duracion_declarada_del_tour`, `test_tour_de_dia_completo_ocupa_un_dia_vacio` | Duración real y tours de día completo |
| `test_sin_salidas_en_la_estadia_responde_404` | Sin salidas en esas fechas → mensaje claro |

Las pruebas de API de la rúbrica (Karate) la ejercitan a través de Laravel: casos TC-API-054 y TC-API-055.

## Desplegar en la nube

El `Dockerfile` escucha en la variable `PORT` que asigna la plataforma, y `railway.json` deja configurado Railway con su
chequeo de salud. Pasos y variables en
[backend → Desplegar en la nube](../README.md#desplegar-en-la-nube-cada-una-en-su-dominio).
