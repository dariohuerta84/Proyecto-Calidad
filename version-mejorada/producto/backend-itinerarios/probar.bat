@echo off
REM Ejecuta las pruebas del microservicio dentro del contenedor
cd /d "%~dp0"
docker compose exec itinerarios-api sh -c "pip install -q pytest==8.3.3 httpx==0.25.2 && python -m pytest -q tests"
pause
