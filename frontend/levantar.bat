@echo off
REM ===== Trouvels360 - Web: construir y levantar con Docker =====
REM Requisitos: Docker Desktop abierto y la API Laravel levantada (backend\levantar-todo.bat).
cd /d "%~dp0"
if not exist .env (
  copy .env.example .env >nul
  echo [AVISO] No habia .env: se copio .env.example.
)
echo === Compilando la web y levantando nginx (la primera vez tarda unos minutos) ===
docker compose up -d --build
if errorlevel 1 (
  echo [ERROR] Docker fallo. Revisa que Docker Desktop este abierto y que el puerto 4200 este libre.
  pause
  exit /b 1
)
set intentos=0
:esperar
curl -fs http://localhost:4200/salud >nul 2>&1
if not errorlevel 1 goto lista
set /a intentos+=1
if %intentos% GEQ 30 (
  echo [ERROR] La web no respondio. Revisa: docker compose logs web
  pause
  exit /b 1
)
timeout /t 2 /nobreak >nul
goto esperar
:lista
curl -fs http://localhost:4200/api/ping >nul 2>&1
if errorlevel 1 echo [AVISO] La web esta arriba, pero no llega a la API en http://localhost:8000. Levanta el backend.
echo.
echo ============================================================
echo  LISTO. Web en http://localhost:4200
echo  Viajero:   edwinviajero@gmail.com   / viajero
echo  Proveedor: edwinproveedor@gmail.com / proveedor
echo ============================================================
start http://localhost:4200
pause
