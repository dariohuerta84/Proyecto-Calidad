@echo off
REM Requiere Appium 2 en marcha (appium) y el emulador encendido. Uso: ejecutar.bat [-Dgroups=HU-V07]
cd /d "%~dp0"
call mvn -q test %*
start "" "target\reporte-appium\index.html"
