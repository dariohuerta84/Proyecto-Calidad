@echo off
REM Ejecuta las 60 pruebas de integracion de la API (Pest)
cd /d "%~dp0"
docker compose exec app php artisan test
pause
