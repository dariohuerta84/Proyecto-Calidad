@echo off
REM Pruebas de integracion de la API (Pest) dentro del contenedor. Usan SQLite en memoria: no tocan tu base.
docker exec trouvels360-api-laravel php artisan test
pause
