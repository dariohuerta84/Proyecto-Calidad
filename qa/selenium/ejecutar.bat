@echo off
REM Ejecuta toda la suite web y abre el reporte propio. Uso: ejecutar.bat [-Dgroups=TRV-203] [-Dheadless=false]
cd /d "%~dp0"
call mvn -q test %*
start "" "target\reporte-selenium\index.html"
