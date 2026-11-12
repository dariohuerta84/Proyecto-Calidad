@echo off
REM Ejecuta el plan en modo no GUI, genera el reporte HTML y verifica los criterios de TC-PERF-001..006.
REM Uso: ejecutar.bat [-Jhost=localhost -Jport=8000 -Jduracion=300 ...]   (JMETER_HOME debe apuntar a apache-jmeter-5.6.3)
cd /d "%~dp0"
if exist resultados rmdir /s /q resultados
mkdir resultados
set "JM=jmeter"
if defined JMETER_HOME set "JM=%JMETER_HOME%\bin\jmeter.bat"
call "%JM%" -n -t trouvels360.jmx -q user.properties -l resultados\resultados.jtl -j resultados\jmeter.log -e -o resultados\reporte-html %*
python verificar_resultados.py resultados\resultados.jtl > resultados\verificacion.txt
type resultados\verificacion.txt
echo Reporte HTML: resultados\reporte-html\index.html
