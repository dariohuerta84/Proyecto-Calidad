#!/usr/bin/env python3
"""
Junta los resultados de las 4 suites en datos/resultados.json (un estado por caso TC-xxx).
Fuentes (rutas por defecto, relativas a esta carpeta):
  ../karate/target/karate-reports/*.karate-json.txt      (Karate)
  ../selenium/target/reporte-selenium/resultados.json     (Selenium)
  ../appium/target/reporte-appium/resultados.json         (Appium)
  ../appium/target/reporte-appium-repeticion/**/resultados.json (repeticiones de casos de Appium)
  ../jmeter/resultados/verificacion.txt                   (JMeter, salida de verificar_resultados.py)
  datos/resultados_manuales.json                         (veredictos de las pruebas manuales)
Uso: python recolectar_resultados.py [--karate DIR] [--selenium JSON] [--appium JSON] [--jmeter TXT]
"""
import argparse, glob, json, os, re
from datetime import datetime

AQUI = os.path.dirname(os.path.abspath(__file__))


def ruta(p): return os.path.normpath(os.path.join(AQUI, p))


def karate(carpeta, res):
    for f in glob.glob(os.path.join(carpeta, "*.karate-json.txt")):
        d = json.load(open(f, encoding="utf-8"))
        fecha = d.get("resultDate", "")
        for s in d.get("scenarioResults", []):
            for tc in [t for t in s.get("tags", []) if t.startswith("TC-API-")]:
                r = res.setdefault(tc, {"estado": "Pasó", "fecha": fecha, "ejecuciones": 0, "fallas": 0,
                                        "evidencia": "karate-reports/" + os.path.basename(f).replace(".karate-json.txt", ".html")})
                r["ejecuciones"] += 1
                if s.get("failed"):
                    r["fallas"] += 1
                    r["estado"] = "Falló"
                    r["detalle"] = s.get("error", "")[:300] if isinstance(s.get("error"), str) else "Ver reporte de Karate"


def java(archivo, res, carpeta_evid, solo_mas_recientes=False):
    if not os.path.exists(archivo):
        return
    for fila in json.load(open(archivo, encoding="utf-8")):
        estado = {"OK": "Pasó", "FALLA": "Falló"}.get(fila["estado"], "No ejecutado")
        caso = fila["caso"]
        fecha = fila.get("fecha", "")
        if solo_mas_recientes and (
            estado == "No ejecutado" or not fecha or fecha <= res.get(caso, {}).get("fecha", "")
        ):
            continue
        detalle = "\n".join(linea.rstrip() for linea in (fila.get("error") or "").splitlines())
        res[caso] = {"estado": estado, "fecha": fecha, "detalle": detalle,
                             "evidencia": carpeta_evid + "/index.html", "ms": fila.get("ms")}


def jmeter(archivo, res):
    if not os.path.exists(archivo):
        return
    fecha = datetime.fromtimestamp(os.path.getmtime(archivo)).strftime("%Y-%m-%d %H:%M")
    for linea in open(archivo, encoding="utf-8"):
        m = re.match(r"(OK|FALLA|OMITIDO)\s+(TC-PERF-\d+)", linea)
        if m:
            res[m.group(2)] = {"estado": {"OK": "Pasó", "FALLA": "Falló"}.get(m.group(1), "No ejecutado"),
                               "fecha": fecha, "evidencia": "jmeter/resultados/reporte-html/index.html"}


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--karate", default=ruta("../karate/target/karate-reports"))
    ap.add_argument("--selenium", default=ruta("../selenium/target/reporte-selenium/resultados.json"))
    ap.add_argument("--appium", default=ruta("../appium/target/reporte-appium/resultados.json"))
    ap.add_argument("--jmeter", default=ruta("../jmeter/resultados/verificacion.txt"))
    a = ap.parse_args()
    res = {}
    manual = ruta("datos/resultados_manuales.json")
    if os.path.exists(manual):
        res.update(json.load(open(manual, encoding="utf-8")))
    karate(a.karate, res)
    java(a.selenium, res, "selenium/target/reporte-selenium")
    java(a.appium, res, "appium/target/reporte-appium")
    if a.appium == ruta("../appium/target/reporte-appium/resultados.json"):
        repeticiones = glob.glob(ruta("../appium/target/reporte-appium-repeticion/**/resultados.json"), recursive=True)
        for archivo in sorted(repeticiones):
            evidencia = os.path.relpath(os.path.dirname(archivo), ruta("..")).replace(os.sep, "/")
            java(archivo, res, evidencia, solo_mas_recientes=True)
    jmeter(a.jmeter, res)
    json.dump(dict(sorted(res.items())), open(ruta("datos/resultados.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    cuenta = {}
    for r in res.values():
        cuenta[r["estado"]] = cuenta.get(r["estado"], 0) + 1
    print("datos/resultados.json actualizado:", cuenta)
