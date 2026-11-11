@echo off
REM Arranca el backend (dias siguientes a la instalacion)
cd /d "%~dp0"
docker compose up -d
echo Backend corriendo en http://localhost:8000
pause
