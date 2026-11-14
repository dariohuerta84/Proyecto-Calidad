@echo off
setlocal
REM Corre las suites en orden y regenera la trazabilidad. Requiere backend (:8000), itinerarios (:8001) y web (:4200).
REM Uso: ejecutar-todo.bat [web ^| matriz ^| jmeter ^| appium ^| jmeter appium]
cd /d "%~dp0" || exit /b 1
set "ESTADO=0"
set "CORRER_JMETER="
set "CORRER_APPIUM="
if /i "%~1"=="matriz" if "%~2"=="" goto trazabilidad
if /i "%~1"=="web" if "%~2"=="" goto suites
if "%~1"=="" (
  set "CORRER_JMETER=1"
  set "CORRER_APPIUM=1"
)
for %%S in (%*) do (
  if /i "%%~S"=="jmeter" (
    set "CORRER_JMETER=1"
  ) else if /i "%%~S"=="appium" (
    set "CORRER_APPIUM=1"
  ) else (
    echo Uso: ejecutar-todo.bat [web ^| matriz ^| jmeter ^| appium ^| jmeter appium]
    exit /b 2
  )
)

:suites
REM Aviso si la terminal no usa Java 21 (con Java 25 fallan Maven/Allure: ver qa\README.md).
java -version 2>&1 | findstr /r /c:"version .21[.]" >nul
if errorlevel 1 (
  echo AVISO: esta terminal no usa Java 21. Ver "Java" en qa\README.md.
  java -version
)
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
if defined CORRER_JMETER (
	echo == JMeter ^(rendimiento^)
	pushd jmeter
	call ejecutar.bat
	if errorlevel 1 set "ESTADO=1"
	popd
)
if defined CORRER_APPIUM call :appium

:trazabilidad
echo == Trazabilidad
pushd trazabilidad
python recolectar_resultados.py
if errorlevel 1 exit /b 1
python generar_trazabilidad.py
if errorlevel 1 exit /b 1
popd
start "" "trazabilidad\salida\matriz_trazabilidad.html"
exit /b %ESTADO%

:appium
echo == Appium ^(Android^)
set "SERVIDOR_APPIUM=http://127.0.0.1:4723"
if defined APPIUM_URL set "SERVIDOR_APPIUM=%APPIUM_URL%"
curl.exe -fsS --max-time 5 "%SERVIDOR_APPIUM%/status" >nul 2>&1
if errorlevel 1 (
  echo Appium pendiente: inicia el servidor en %SERVIDOR_APPIUM%.
  set "ESTADO=1"
  exit /b 0
)
set "ANDROID_DISPONIBLE="
for /f "skip=1 tokens=2" %%D in ('adb devices 2^>nul') do if "%%D"=="device" set "ANDROID_DISPONIBLE=1"
if not defined ANDROID_DISPONIBLE (
  echo Appium pendiente: conecta un dispositivo Android o inicia el emulador.
  set "ESTADO=1"
  exit /b 0
)
if not defined APP if not exist "..\android\app\build\outputs\apk\debug\app-debug.apk" (
  echo Appium pendiente: genera el APK con android\gradlew.bat assembleDebug desde android.
  set "ESTADO=1"
  exit /b 0
)
pushd appium
call mvn -q test "-Dappium.url=%SERVIDOR_APPIUM%"
if errorlevel 1 set "ESTADO=1"
popd
exit /b 0
