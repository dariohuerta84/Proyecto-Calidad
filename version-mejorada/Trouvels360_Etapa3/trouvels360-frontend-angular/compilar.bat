@echo off
chcp 65001 >nul
echo === Compilacion de produccion (verifica que no haya errores) ===
if not exist node_modules call npm install
call npm run build:prod
pause
