@echo off
REM Corre las suites en orden y regenera la trazabilidad. Requiere backend (:8000), itinerarios (:8001) y web (:4200).
REM Uso: ejecutar-todo.bat [appium] [jmeter]
cd /d "%~dp0"
set "ESTADO=0"
echo == Karate (API)
pushd karate
call mvn -q test
if errorlevel 1 set "ESTADO=1"
popd
echo == Selenium (web)
pushd selenium
call mvn -q test
if errorlevel 1 set "ESTADO=1"
popd
echo(%*| findstr /i "jmeter" >nul
if not errorlevel 1 (
	pushd jmeter
	call ejecutar.bat
	if errorlevel 1 set "ESTADO=1"
	popd
)
echo(%*| findstr /i "appium" >nul
if not errorlevel 1 (
	pushd appium
	call mvn -q test
	if errorlevel 1 set "ESTADO=1"
	popd
)
echo == Trazabilidad
pushd trazabilidad
python recolectar_resultados.py
if errorlevel 1 set "ESTADO=1"
python generar_trazabilidad.py
if errorlevel 1 set "ESTADO=1"
popd
start "" "trazabilidad\salida\matriz_trazabilidad.html"
exit /b %ESTADO%
