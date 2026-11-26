#!/usr/bin/env python3
"""
Consolida en datos/resultados.json el resultado más reciente con evidencia para cada caso.
Por defecto lee los reportes de Karate, Selenium y Appium en target/, los de JMeter
en resultados*/ y los veredictos manuales en datos/resultados_manuales.json.
Una ejecución filtrada conserva los resultados de los casos que quedaron fuera.
Uso: python recolectar_resultados.py [--karate DIR] [--selenium JSON] [--appium JSON] [--jmeter TXT]
"""
import argparse, glob, json, os, re
from datetime import datetime

AQUI = os.path.dirname(os.path.abspath(__file__))


def ruta(p): return os.path.normpath(os.path.join(AQUI, p))


def desde_qa(p):
    try:
        return os.path.relpath(p, ruta("..")).replace(os.sep, "/")
    except ValueError:
        return os.path.abspath(p).replace(os.sep, "/")


def fecha_hora(valor):
    valor = valor.replace("\xa0", " ").strip()
    valor = valor.replace("a. m.", "AM").replace("p. m.", "PM")
    if not valor:
        return datetime.min
    if valor.endswith(("AM", "PM")):
        return datetime.strptime(valor, "%Y-%m-%d %I:%M:%S %p")
    return datetime.fromisoformat(valor)


def actualizar(res, nuevos):
    for caso, nuevo in nuevos.items():
        if nuevo["estado"] == "No ejecutado":
            continue
        anterior = res.get(caso, {})
        fecha_nueva = fecha_hora(nuevo.get("fecha", ""))
        fecha_anterior = fecha_hora(anterior.get("fecha", ""))
        enlace_anterior = anterior.get("evidencia", "")
        evidencia_valida = enlace_anterior and os.path.isfile(
            ruta(os.path.join("..", enlace_anterior.removeprefix("qa/")))
        )
        if (anterior.get("estado") in (None, "No ejecutado")
                or fecha_nueva > fecha_anterior
                or (fecha_nueva == fecha_anterior and not evidencia_valida)):
            res[caso] = nuevo


def karate(carpeta, res):
    for f in glob.glob(os.path.join(carpeta, "*.karate-json.txt")):
        d = json.load(open(f, encoding="utf-8"))
        fecha = d.get("resultDate", "")
        for s in d.get("scenarioResults", []):
            for tc in [t for t in s.get("tags", []) if t.startswith("TC-API-")]:
                r = res.setdefault(tc, {"estado": "Pasó", "fecha": fecha, "ejecuciones": 0, "fallas": 0,
                                        "evidencia": desde_qa(f.replace(".karate-json.txt", ".html"))})
                r["ejecuciones"] += 1
                if s.get("failed"):
                    r["fallas"] += 1
                    r["estado"] = "Falló"
                    r["detalle"] = s.get("error", "")[:300] if isinstance(s.get("error"), str) else "Ver reporte de Karate"


def java(archivo, res):
    if not os.path.exists(archivo):
        return
    for fila in json.load(open(archivo, encoding="utf-8")):
        estado = {"OK": "Pasó", "FALLA": "Falló"}.get(fila["estado"], "No ejecutado")
        caso = fila["caso"]
        fecha = fila.get("fecha", "")
        detalle = "\n".join(linea.rstrip() for linea in (fila.get("error") or "").splitlines())
        res[caso] = {"estado": estado, "fecha": fecha, "detalle": detalle,
                     "evidencia": desde_qa(os.path.join(os.path.dirname(archivo), "index.html")), "ms": fila.get("ms")}


def jmeter(archivo, res):
    if not os.path.exists(archivo):
        return
    fecha = datetime.fromtimestamp(os.path.getmtime(archivo)).strftime("%Y-%m-%d %H:%M")
    evidencia = desde_qa(os.path.join(os.path.dirname(archivo), "reporte-html", "index.html"))
    for linea in open(archivo, encoding="utf-8-sig"):
        m = re.match(r"(OK|FALLA|OMITIDO)\s+(TC-PERF-\d+)", linea)
        if m:
            res[m.group(2)] = {"estado": {"OK": "Pasó", "FALLA": "Falló"}.get(m.group(1), "No ejecutado"),
                               "fecha": fecha, "evidencia": evidencia}


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--karate", help="Carpeta de un reporte de Karate")
    ap.add_argument("--selenium", help="Archivo resultados.json de Selenium")
    ap.add_argument("--appium", help="Archivo resultados.json de Appium")
    ap.add_argument("--jmeter", help="Archivo verificacion.txt de JMeter")
    a = ap.parse_args()
    destino = ruta("datos/resultados.json")
    res = json.load(open(destino, encoding="utf-8")) if os.path.exists(destino) else {}
    manual = ruta("datos/resultados_manuales.json")
    if os.path.exists(manual):
        actualizar(res, json.load(open(manual, encoding="utf-8")))
    fuentes = (
        (karate, a.karate, "../karate/target/karate-reports*"),
        (java, a.selenium, "../selenium/target/**/resultados.json"),
        (java, a.appium, "../appium/target/**/resultados.json"),
        (jmeter, a.jmeter, "../jmeter/resultados*/verificacion.txt"),
    )
    for leer, fuente, patron in fuentes:
        for archivo in [fuente] if fuente else sorted(glob.glob(ruta(patron), recursive=True)):
            nuevos = {}
            leer(archivo, nuevos)
            actualizar(res, nuevos)
    with open(destino, "w", encoding="utf-8") as salida:
        json.dump(dict(sorted(res.items())), salida, ensure_ascii=False, indent=1)
    cuenta = {}
    for r in res.values():
        cuenta[r["estado"]] = cuenta.get(r["estado"], 0) + 1
    print("datos/resultados.json actualizado:", cuenta)
