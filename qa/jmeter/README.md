# Pruebas de rendimiento y concurrencia — JMeter (QA-02)

Plan: `trouvels360.jmx` (JMeter 5.6.3). Cubre **TC-PERF-001 a TC-PERF-006**. Los grupos de hilos corren uno tras otro
(*Run Thread Groups consecutively*) para que cada medición sea limpia.

| Grupo | Caso | HU | Carga | Criterio |
|---|---|---|---|---|
| setUp | — | — | 1 hilo | Crea un hotel con **1 unidad** y un tour con **10 cupos** nuevos en cada corrida |
| Login concurrente | TC-PERF-001 | HU-V02 | 50 usuarios (CSV), ramp-up 10 s, 3 iteraciones | p95 < 800 ms, 0 % errores |
| Catálogo de hoteles | TC-PERF-002 | HU-V05/HU-V06 | 100 usuarios, ramp-up 60 s, 5 min, ciudades por CSV | p95 < 1 s |
| Búsqueda de tours | TC-PERF-004 | HU-V08/HU-V09 | 100 usuarios, categorías por CSV | p95 < 1 s |
| Última habitación | TC-PERF-003 | HU-V07 | 20 hilos + **Synchronizing Timer** | exactamente 1×201 y 19×409 |
| Cupos de tour | TC-PERF-005 | HU-V10 | 30 hilos + Synchronizing Timer sobre 10 cupos | confirmadas = 10, nunca más |
| Panel del proveedor | TC-PERF-006 | HU-P03/HU-P04/HU-P12 | 30 usuarios, 5 iteraciones | p95 < 1,5 s |
| tearDown | — | — | 1 hilo | Verifica los conteos de las carreras y el `cupo_reservado` final |

**Elementos usados:** HTTP Request Defaults, HTTP Cookie Manager (JWT en cookie HttpOnly + `XSRF-TOKEN`), Header Manager
(`X-XSRF-TOKEN: ${xsrf}`, copiado de la cookie por un JSR223 PreProcessor), CSV Data Set Config (`datos/usuarios.csv`,
`datos/ciudades.csv`, `datos/categorias.csv`), `${__UUID()}` para `Idempotency-Key`, JSON Extractor, Response/JSON/Duration
Assertions, If/Once Only Controllers, Uniform Random Timer (tiempo de lectura), Synchronizing Timer, Summary y Aggregate Report.

## Instalar JMeter

1. Java: **JDK 21** (o 17). JMeter 5.6.3 no está probado con Java 25.
2. Descarga *Apache JMeter 5.6.3* (Binaries, `.zip`) desde https://jmeter.apache.org/download_jmeter.cgi y descomprímelo,
   por ejemplo en `C:\apache-jmeter-5.6.3`.
3. Agrega `C:\apache-jmeter-5.6.3\bin` al `PATH` (o define `JMETER_HOME`) y comprueba con `jmeter -v`.

## Antes de ejecutar

1. Las dos APIs levantadas con Docker (`backend\levantar-todo.bat`) y datos limpios
   (`backend\api-laravel\reiniciar-datos.bat`): el `QaSeeder` crea `qa.viajero1..30` / `Viajero123`.
2. En `backend/api-laravel/.env` subir los límites (si no, la propia API corta el login con 429) y aplicar con
   `docker compose up -d` en `backend/`:
   ```
   LOGIN_RATE_LIMIT=1000
   REGISTER_RATE_LIMIT=1000
   ```
   En producción se dejan los valores por defecto (6 y 10 por minuto).
3. Mide contra Docker (Apache + MySQL), no contra `php artisan serve`: el servidor de desarrollo de PHP atiende pocas
   peticiones a la vez y los tiempos no serían representativos.

## Ejecutar

```bash
# Linux / macOS
./ejecutar.sh                                # todo, con user.properties
./ejecutar.sh -Jduracion=60 -Jusuarios.catalogo=20   # corrida corta
# Windows
ejecutar.bat
```

Genera `resultados/resultados.jtl`, `resultados/reporte-html/index.html` (dashboard de JMeter) y
`resultados/verificacion.txt`, donde `verificar_resultados.py` aplica los criterios de la tabla (p95 y % de error por
caso, conteo 201/409 de las carreras) y termina con código 1 si algo falla.

Para correr un solo grupo, poner en 0 los demás, por ejemplo solo las carreras:
`./ejecutar.sh -Jusuarios.login=0 -Jusuarios.catalogo=0 -Jusuarios.tours=0 -Jusuarios.panel=0`

Abrir en la GUI solo para editar o depurar (`jmeter -t trouvels360.jmx`); el *View Results Tree* viene desactivado.

## Prueba rápida sin JMeter

`concurrencia_smoke.py` (solo Python 3) reproduce TC-PERF-003 y TC-PERF-005 con hilos y una barrera, útil para comprobar
el backend antes de lanzar JMeter:

```
python concurrencia_smoke.py --api http://localhost:8000/api
TC-PERF-003  20 hilos por la última unidad -> 201: 1  409: 19  otros: []  p95: 255 ms  -> OK
TC-PERF-005  30 hilos sobre 10 cupos -> 201: 10  409: 20  otros: []  cupo_reservado final: 10  -> OK
```
(salida real obtenida al preparar esta etapa)

## Evidencia para Jira

Adjuntar a la ejecución de QA-02 el zip de `resultados/reporte-html`, `verificacion.txt` y una captura del Aggregate Report.
`generar_jmx.py` es el script con el que se armó el `.jmx`; no hace falta ejecutarlo.
