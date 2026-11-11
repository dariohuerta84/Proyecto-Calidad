@echo off
cd /d "%~dp0"
chcp 65001 >nul
echo === Trouvels360 - Frontend Angular (sin Docker) ===
echo Requisitos: Node 20+ y el backend Laravel en http://localhost:8000
if not exist node_modules (
  echo Instalando dependencias...
  call npm install || (echo Error en npm install & pause & exit /b 1)
)
call npm run start:local
