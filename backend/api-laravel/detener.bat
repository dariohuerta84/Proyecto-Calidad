@echo off
REM Detiene la API y MySQL. Los datos se conservan en el volumen mysql_data.
cd /d "%~dp0"
docker compose --profile herramientas down
echo API Laravel detenida.
pause
