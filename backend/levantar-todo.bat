@echo off
REM ===== Trouvels360 - levanta las DOS APIs a la vez (Laravel + MySQL y Python) =====
REM Requisito: Docker Desktop abierto. Si alguna API ya estaba levantada por separado, se detiene primero.
cd /d "%~dp0"
if not exist api-laravel\.env copy api-laravel\.env.example api-laravel\.env >nul
if not exist api-python\.env.fastapi copy api-python\.env.fastapi.example api-python\.env.fastapi >nul

pushd api-laravel & docker compose down >nul 2>&1 & popd
pushd api-python & docker compose down >nul 2>&1 & popd

echo === Construyendo y levantando las dos APIs (la primera vez tarda varios minutos) ===
docker compose up -d --build
if errorlevel 1 (
  echo [ERROR] Docker fallo. Revisa que Docker Desktop este abierto y que los puertos 8000, 8001 y 3306 esten libres.
  pause
  exit /b 1
)

echo === Esperando a que respondan ===
set intentos=0
:esperar
curl -fs http://localhost:8000/api/ping >nul 2>&1 || goto otra
curl -fs http://localhost:8001/api/health >nul 2>&1 || goto otra
goto lista
:otra
set /a intentos+=1
if %intentos% GEQ 60 (
  echo [ERROR] No respondieron en 3 minutos. Revisa: docker compose logs
  pause
  exit /b 1
)
timeout /t 3 /nobreak >nul
goto esperar

:lista
echo.
echo ============================================================
echo  LISTO.
echo  API Laravel: http://localhost:8000/api/ping
echo  API Python:  http://localhost:8001/api/health  (docs: /docs)
echo  Viajero:   edwinviajero@gmail.com   / viajero
echo  Proveedor: edwinproveedor@gmail.com / proveedor
echo ============================================================
pause
