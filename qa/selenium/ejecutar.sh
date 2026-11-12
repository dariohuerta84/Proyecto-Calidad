#!/usr/bin/env bash
# Ejecuta toda la suite web. Uso: ./ejecutar.sh [-Dgroups=TRV-203] [-Dheadless=false]
cd "$(dirname "$0")"
mvn -q test "$@"; code=$?
echo "Reporte: target/reporte-selenium/index.html  ·  Allure: mvn allure:serve"
exit $code
