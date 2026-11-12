@echo off
REM Detiene las dos APIs. Los datos de MySQL se conservan.
cd /d "%~dp0"
docker compose --profile herramientas down
echo Backend detenido.
pause
