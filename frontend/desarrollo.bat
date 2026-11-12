@echo off
REM Web en modo desarrollo (recarga al guardar), sin Docker. Requiere Node 20+ y la API en http://localhost:8000.
cd /d "%~dp0"
if not exist node_modules (
  echo Instalando dependencias...
  call npm ci
  if errorlevel 1 ( echo [ERROR] Fallo npm ci. & pause & exit /b 1 )
)
call npm start
