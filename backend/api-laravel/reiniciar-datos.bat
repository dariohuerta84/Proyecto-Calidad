@echo off
REM Borra la base y la recrea con los datos de prueba (incluye QaSeeder). Util antes de correr las suites de qa\.
cd /d "%~dp0"
docker compose exec app php artisan migrate:fresh --seed --force
echo Datos de prueba recreados.
pause
