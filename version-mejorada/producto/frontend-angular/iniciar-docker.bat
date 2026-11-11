@echo off
cd /d "%~dp0"
chcp 65001 >nul
echo === Trouvels360 - Frontend Angular (Docker) ===
echo Requisito: el backend Laravel debe estar levantado en http://localhost:8000
docker compose up --build -d
if errorlevel 1 (
  echo No se pudo levantar el contenedor. Abre Docker Desktop y vuelve a intentarlo.
  pause
  exit /b 1
)
echo.
echo Compilando la web (la primera vez tarda 1-2 minutos)...
echo Abre http://localhost:4200 cuando aparezca "Local: http://localhost:4200" en:
echo    docker compose logs -f frontend
pause
