#!/usr/bin/env python3
"""
Lee el resultados.jtl (CSV de JMeter) y aplica los criterios de aceptación de TC-PERF-001..006.
Sale con código 1 si alguno falla (sirve para CI). Uso: python verificar_resultados.py resultados/resultados.jtl
"""
import csv, sys
from collections import defaultdict

# prefijo de etiqueta -> (p95 máximo en ms, % de error máximo)
CRITERIOS = {
    "TC-PERF-001 POST /auth/login": (800, 0.0),
    "TC-PERF-002": (1000, 1.0),
    "TC-PERF-004": (1000, 1.0),
    "TC-PERF-006": (1500, 1.0),
}
NOMBRES = {
    "TC-PERF-001": "Login 50 usuarios (TRV-103)", "TC-PERF-002": "Catálogo de hoteles 100 usuarios (TRV-201)",
    "TC-PERF-003": "Última habitación 20 hilos (TRV-203)", "TC-PERF-004": "Búsqueda de tours 100 usuarios (TRV-301)",
    "TC-PERF-005": "Cupos de tour 30 hilos (TRV-303)", "TC-PERF-006": "Panel del proveedor 30 usuarios (TRV-501)",
}


def p95(v):
    v = sorted(v)
    return v[max(0, -(-95 * len(v) // 100) - 1)] if v else 0


def main(ruta):
    filas = list(csv.DictReader(open(ruta, encoding="utf-8")))
    tiempos, errores, codigos = defaultdict(list), defaultdict(int), defaultdict(lambda: defaultdict(int))
    for f in filas:
        et = f["label"]
        for pref in CRITERIOS:
            if et.startswith(pref):
                tiempos[pref].append(int(f["elapsed"]))
                errores[pref] += f["success"].lower() != "true"
        if "(simultáneo)" in et:
            codigos[et[:11]][f["responseCode"]] += 1
        if et.startswith(("TC-PERF-003 Verificación", "TC-PERF-005 Verificación")):
            codigos[et[:11] + " verificación"]["ok" if f["success"].lower() == "true" else "falla"] += 1

    resultado, ok_total = [], True
    for pref, (max_ms, max_err) in CRITERIOS.items():
        n = len(tiempos[pref])
        if not n:
            resultado.append((pref, "OMITIDO", "sin muestras")); continue
        p, err = p95(tiempos[pref]), 100.0 * errores[pref] / n
        ok = p <= max_ms and err <= max_err
        ok_total &= ok
        resultado.append((pref, "OK" if ok else "FALLA", f"{n} muestras · p95 {p} ms (máx {max_ms}) · error {err:.2f} % (máx {max_err} %)"))
    for tc in ("TC-PERF-003", "TC-PERF-005"):
        c, v = codigos.get(tc, {}), codigos.get(tc + " verificación", {})
        if not c:
            resultado.append((tc, "OMITIDO", "sin muestras")); continue
        ok = v.get("ok", 0) == 1 and set(c) <= {"201", "409"}
        ok_total &= ok
        resultado.append((tc, "OK" if ok else "FALLA", f"201: {c.get('201', 0)} · 409: {c.get('409', 0)} · otros: "
                          f"{ {k: n for k, n in c.items() if k not in ('201', '409')} or 0} · verificación tearDown: {'OK' if v.get('ok') else 'FALLA'}"))

    for tc, estado, det in sorted(resultado):
        print(f"{estado:8} {tc}  {NOMBRES.get(tc[:11], '')}\n         {det}")
    print("\nRESULTADO GLOBAL:", "OK" if ok_total else "FALLA")
    return 0 if ok_total else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "resultados/resultados.jtl"))
