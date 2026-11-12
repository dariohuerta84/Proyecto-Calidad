@echo off
REM Borra la base y la recrea con los datos de prueba (incluye QaSeeder). Util antes de correr las suites de qa\.
REM Usa el nombre fijo del contenedor: funciona tanto si levantaste con backend\levantar-todo.bat como con levantar.bat.
docker exec trouvels360-api-laravel php artisan migrate:fresh --seed --force
if errorlevel 1 (
  echo.
  echo ERROR: no se pudieron recrear los datos. Revisa que la API este levantada: docker ps
  pause
  exit /b 1
)
echo Datos de prueba recreados.
pause
