#!/usr/bin/env bash
set -e

# ==============================================================================
# Script de Ejecución no-GUI de Apache JMeter para Trouvels360
# Asignatura: Calidad de Software (C8305 - UPCH)
# Genera métricas JTL y Dashboard HTML Oficial de JMeter
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORTS_DIR="$SCRIPT_DIR/reports"
PLANS_DIR="$SCRIPT_DIR/plans"

mkdir -p "$REPORTS_DIR"

# 1. Resolver binario de JMeter
JMETER_CMD="jmeter"
if ! command -v jmeter &>/dev/null; then
    JMETER_HOME="/tmp/apache-jmeter-5.6.3"
    if [ ! -d "$JMETER_HOME" ]; then
        echo "⬇️ Descargando Apache JMeter 5.6.3 portable..."
        curl -sSL "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz" -o /tmp/jmeter.tgz
        tar -xzf /tmp/jmeter.tgz -C /tmp/
        rm -f /tmp/jmeter.tgz
    fi
    JMETER_CMD="$JMETER_HOME/bin/jmeter"
fi

echo "=========================================================="
echo " [Apache JMeter] Ejecutando Plan Funcional CRUD cURL"
echo "=========================================================="

rm -rf "$REPORTS_DIR/crud_results.jtl" "$REPORTS_DIR/crud-dashboard"

"$JMETER_CMD" -n \
  -t "$PLANS_DIR/Trouvels360_CRUD_cURL.jmx" \
  -l "$REPORTS_DIR/crud_results.jtl" \
  -e -o "$REPORTS_DIR/crud-dashboard"

echo "✅ Dashboard CRUD generado en: $REPORTS_DIR/crud-dashboard/index.html"

echo -e "\n=========================================================="
echo " [Apache JMeter] Ejecutando Plan de Carga y Rendimiento"
echo "=========================================================="

rm -rf "$REPORTS_DIR/load_results.jtl" "$REPORTS_DIR/load-dashboard"

"$JMETER_CMD" -n \
  -t "$PLANS_DIR/Trouvels360_Carga_y_Stress.jmx" \
  -Jusers=10 -Jrampup=3 -Jloops=3 \
  -l "$REPORTS_DIR/load_results.jtl" \
  -e -o "$REPORTS_DIR/load-dashboard"

echo "✅ Dashboard de Carga generado en: $REPORTS_DIR/load-dashboard/index.html"
echo "=========================================================="
echo "🎉 ¡Ejecución de JMeter finalizada con éxito!"
echo "=========================================================="
