@echo off
REM Pruebas de integracion de la API (Pest) dentro del contenedor. Usan SQLite en memoria: no tocan tu base.
cd /d "%~dp0"
docker compose exec app php artisan test
pause
