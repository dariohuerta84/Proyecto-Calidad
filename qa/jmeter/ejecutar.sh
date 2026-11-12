#!/usr/bin/env bash
# Ejecuta el plan en modo no GUI, genera el reporte HTML y verifica los criterios de TC-PERF-001..006.
# Uso: ./ejecutar.sh [-Jhost=localhost -Jport=8000 -Jduracion=300 ...]
set -euo pipefail
cd "$(dirname "$0")"
JMETER="${JMETER_HOME:+$JMETER_HOME/bin/}jmeter"
rm -rf resultados && mkdir -p resultados
"$JMETER" -n -t trouvels360.jmx -q user.properties -l resultados/resultados.jtl -j resultados/jmeter.log -e -o resultados/reporte-html "$@"
python3 verificar_resultados.py resultados/resultados.jtl | tee resultados/verificacion.txt
echo "Reporte HTML: resultados/reporte-html/index.html"
