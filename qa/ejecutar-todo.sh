#!/usr/bin/env bash
# Corre las suites en orden y regenera la trazabilidad. Requiere backend (:8000), itinerarios (:8001) y web (:4200).
# Uso: ./ejecutar-todo.sh [--con-appium] [--con-jmeter]
cd "$(dirname "$0")"
estado=0
echo "== Karate (API)";      (cd karate && mvn -q test) || estado=1
echo "== Selenium (web)";    (cd selenium && mvn -q test) || estado=1
if [[ " $* " == *" --con-jmeter "* ]]; then echo "== JMeter";  (cd jmeter && ./ejecutar.sh) || estado=1; fi
if [[ " $* " == *" --con-appium "* ]]; then echo "== Appium";  (cd appium && mvn -q test) || estado=1; fi
echo "== Trazabilidad";      (cd trazabilidad && python3 recolectar_resultados.py && python3 generar_trazabilidad.py) || estado=1
echo "Matriz: trazabilidad/salida/matriz_trazabilidad.html"
exit $estado
