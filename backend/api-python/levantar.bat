@echo off
REM ===== Trouvels360 - API Python (itinerarios): construir y levantar =====
REM Requisito: Docker Desktop abierto. Para generar itinerarios necesita la base de api-laravel levantada.
cd /d "%~dp0"
if not exist .env.fastapi (
  copy .env.fastapi.example .env.fastapi >nul
  echo [AVISO] No habia .env.fastapi: se copio .env.fastapi.example con valores para desarrollo local.
)
docker compose up -d --build
if errorlevel 1 (
  echo [ERROR] Fallo docker compose. Revisa que Docker Desktop este abierto y que el puerto 8001 este libre.
  pause
  exit /b 1
)
set intentos=0
:esperar
curl -fs http://localhost:8001/api/health >nul 2>&1
if not errorlevel 1 goto lista
set /a intentos+=1
if %intentos% GEQ 40 (
  echo [ERROR] La API no respondio. Revisa: docker compose logs api-python
  pause
  exit /b 1
)
timeout /t 2 /nobreak >nul
goto esperar
:lista
echo.
echo ============================================================
echo  LISTO. API Python en http://localhost:8001/api/health
echo  Documentacion interactiva: http://localhost:8001/docs
echo ============================================================
start http://localhost:8001/docs
pause
