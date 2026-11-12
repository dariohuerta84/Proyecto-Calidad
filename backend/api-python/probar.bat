@echo off
REM Pruebas del microservicio (pytest) dentro del contenedor. Usan SQLite en memoria: no necesitan MySQL.
docker exec trouvels360-api-python python -m pytest -q tests
pause
