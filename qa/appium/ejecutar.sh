#!/usr/bin/env bash
# Requiere Appium 2 en marcha (appium) y el emulador encendido. Uso: ./ejecutar.sh [-Dgroups=HU-V07]
cd "$(dirname "$0")"
mvn -q test "$@"; code=$?
echo "Reporte: target/reporte-appium/index.html  ·  Allure: mvn allure:serve"
exit $code
