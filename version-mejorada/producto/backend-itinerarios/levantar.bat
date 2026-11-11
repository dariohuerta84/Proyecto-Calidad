@echo off
REM ===== Trouvels360 - Microservicio de itinerarios (FastAPI) =====
REM Requisito: el backend Laravel ya levantado (crea la red trouvels360_network y la base MySQL).
cd /d "%~dp0"
if not exist .env.fastapi (
  echo [ERROR] Falta .env.fastapi. Copia .env.fastapi.example y pon en JWT_SECRET el mismo valor que JWT_MS_SECRET del backend.
  pause & exit /b 1
)
docker network inspect trouvels360_network >nul 2>&1
if errorlevel 1 (
  echo [ERROR] No existe la red trouvels360_network. Levanta primero el backend Laravel.
  pause & exit /b 1
)
docker compose up --build -d
if errorlevel 1 ( echo [ERROR] Fallo docker compose. & pause & exit /b 1 )
echo.
echo Microservicio en http://localhost:8001/api/health  (documentacion: http://localhost:8001/docs)
start http://localhost:8001/api/health
pause
