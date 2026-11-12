@echo off
REM ===== Trouvels360 - API Laravel: construir y levantar (primera vez y dias siguientes) =====
REM Requisito: Docker Desktop abierto ("Engine running").
cd /d "%~dp0"

if not exist .env (
  copy .env.example .env >nul
  echo [AVISO] No habia .env: se copio .env.example con valores para desarrollo local.
)

echo === 1/2 Construyendo y levantando API + MySQL (la primera vez tarda varios minutos) ===
docker compose up -d --build
if errorlevel 1 (
  echo [ERROR] Docker fallo. Revisa que Docker Desktop este abierto y que los puertos 8000 y 3306 esten libres.
  pause
  exit /b 1
)

echo === 2/2 Esperando a que la API responda (migraciones y datos de prueba) ===
set intentos=0
:esperar
curl -fs http://localhost:8000/api/ping >nul 2>&1
if not errorlevel 1 goto lista
set /a intentos+=1
if %intentos% GEQ 60 (
  echo [ERROR] La API no respondio en 3 minutos. Revisa: docker compose logs app
  pause
  exit /b 1
)
timeout /t 3 /nobreak >nul
goto esperar

:lista
echo.
echo ============================================================
echo  LISTO. API Laravel en http://localhost:8000/api/ping
echo  Viajero:   edwinviajero@gmail.com   / viajero
echo  Proveedor: edwinproveedor@gmail.com / proveedor
echo  Siguiente paso: ..\api-python\levantar.bat (itinerarios)
echo ============================================================
start http://localhost:8000/api/ping
pause
