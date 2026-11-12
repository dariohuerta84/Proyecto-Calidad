#!/usr/bin/env bash
# Ejecuta la suite Karate. Uso: ./ejecutar.sh ["-Dkarate.options=--tags @TRV-203"]
cd "$(dirname "$0")"
mvn -q test "$@"; code=$?
echo "Reporte: target/karate-reports/karate-summary.html"
exit $code
