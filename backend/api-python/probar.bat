@echo off
REM Pruebas del microservicio (pytest) dentro del contenedor. Usan SQLite en memoria: no necesitan MySQL.
cd /d "%~dp0"
docker compose exec api-python python -m pytest -q tests
pause
