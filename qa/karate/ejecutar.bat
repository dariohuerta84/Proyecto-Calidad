@echo off
REM Ejecuta la suite Karate y abre el reporte. Uso: ejecutar.bat ["-Dkarate.options=--tags @HU-V07"]
cd /d "%~dp0"
call mvn -q test %*
start "" "target\karate-reports\karate-summary.html"
