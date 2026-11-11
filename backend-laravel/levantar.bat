@echo off
REM ===== Trouvels360 - Backend Laravel: primera instalacion =====
REM Doble clic con Docker Desktop abierto ("Engine running").
cd /d "%~dp0"

if not exist .env (
  if exist .env.example (
    copy .env.example .env >nul
    echo [AVISO] No habia .env: se copio .env.example. Se generaran las claves.
    set GENERAR_CLAVES=1
  ) else (
    echo [ERROR] No encuentro .env ni .env.example en esta carpeta.
    pause & exit /b 1
  )
)

echo.
echo === 1/4 Construyendo y levantando contenedores (la primera vez tarda varios minutos) ===
docker compose up --build -d
if errorlevel 1 (
  echo [ERROR] Docker fallo. Revisa que Docker Desktop este abierto y diga "Engine running".
  pause & exit /b 1
)

echo.
echo === 2/4 Instalando dependencias de Laravel ===
docker compose exec app composer install --no-interaction
if errorlevel 1 ( echo [ERROR] Fallo composer install. & pause & exit /b 1 )

if defined GENERAR_CLAVES (
  docker compose exec app php artisan key:generate --force
  docker compose exec app php artisan jwt:secret --force
)

echo.
echo === 3/4 Creando base de datos con datos de prueba ===
set intentos=0
:migrar
docker compose exec app php artisan migrate:fresh --seed --force
if errorlevel 1 (
  set /a intentos+=1
  if %intentos% GEQ 8 ( echo [ERROR] No se pudo conectar a MySQL. Revisa: docker compose logs mysql & pause & exit /b 1 )
  echo MySQL aun no esta listo, reintentando en 10 segundos...
  timeout /t 10 /nobreak >nul
  goto migrar
)

echo.
echo === 4/4 Limpiando cache de configuracion ===
docker compose exec app php artisan config:clear

echo.
echo ============================================================
echo  LISTO. Backend en http://localhost:8000/api/ping
echo  Usuario viajero:   edwinviajero@gmail.com   / viajero
echo  Usuario proveedor: edwinproveedor@gmail.com / proveedor
echo  Siguiente paso: levantar el microservicio (carpeta itinerarios\levantar.bat)
echo  Pruebas: docker compose exec app php artisan test
echo ============================================================
start http://localhost:8000/api/ping
pause
