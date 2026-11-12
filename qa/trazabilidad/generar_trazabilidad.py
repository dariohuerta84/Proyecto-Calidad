#!/usr/bin/env python3
"""
Matriz de trazabilidad: historia → criterio de aceptación → caso → herramienta → código → último resultado.

Entradas
  datos/historias.json   historias y criterios vigentes (los mismos de docs/1-historias-de-usuario), con sus casos
  datos/casos.json       título y herramienta de cada caso (y pasos de los manuales)
  datos/resultados.json  último resultado por caso (lo escribe recolectar_resultados.py)
  el código de las suites (../karate, ../selenium, ../appium, ../jmeter) para ubicar cada caso

Salidas en salida/
  matriz_trazabilidad.csv / .md / .html   la matriz (el HTML tiene filtros)
  jira_ejecuciones.csv                    resultado de cada caso para actualizar Jira (ver docs/4-jira)

Uso: python generar_trazabilidad.py
"""
import csv, glob, html, json, os, re
from collections import Counter, defaultdict

AQUI = os.path.dirname(os.path.abspath(__file__))
QA = os.path.normpath(os.path.join(AQUI, ".."))
SALIDA = os.path.join(AQUI, "salida")
os.makedirs(SALIDA, exist_ok=True)


def leer(nombre, defecto=None):
    p = os.path.join(AQUI, "datos", nombre)
    return json.load(open(p, encoding="utf-8")) if os.path.exists(p) else defecto


datos = leer("historias.json")
casos = leer("casos.json")["casos"]
resultados = leer("resultados.json", {})
epicas = {e["id"]: e for e in datos["epicas"]}

# ------------------------------------------------------------------ dónde está automatizado cada caso
ubicacion = defaultdict(list)
for f in glob.glob(os.path.join(QA, "karate/src/test/java/**/*.feature"), recursive=True):
    lineas = open(f, encoding="utf-8").read().splitlines()
    for i, l in enumerate(lineas):
        for tc in re.findall(r"@(TC-API-\d+)", l):
            titulo = next((x.strip() for x in lineas[i + 1:i + 3] if x.strip().startswith("Scenario")), "")
            ubicacion[tc].append(f"qa/karate/{os.path.relpath(f, os.path.join(QA, 'karate'))}:{i + 2} — {titulo.split(':', 1)[-1].strip()}")
for carpeta in ("selenium", "appium"):
    for f in glob.glob(os.path.join(QA, carpeta, "src/test/java/**/*.java"), recursive=True):
        s = open(f, encoding="utf-8").read()
        for m in re.finditer(r'@Tag\("(TC-(?:WEB|MOB)-\d+)"\)[\s\S]*?void\s+(\w+)\(', s):
            linea = s[:m.start()].count("\n") + 1
            ubicacion[m.group(1)].append(f"qa/{carpeta}/{os.path.relpath(f, os.path.join(QA, carpeta))}:{linea} — {os.path.basename(f)[:-5]}#{m.group(2)}")
jmx = os.path.join(QA, "jmeter/trouvels360.jmx")
if os.path.exists(jmx):
    for m in re.finditer(r'testclass="ThreadGroup" testname="(TC-PERF-\d+)([^"]*)"', open(jmx, encoding="utf-8").read()):
        ubicacion[m.group(1)].append(f"qa/jmeter/trouvels360.jmx — {html.unescape(m.group(1) + m.group(2))}")

# ------------------------------------------------------------------ filas: una por historia y caso
filas = []
for h in datos["historias"]:
    por_caso = defaultdict(list)
    for c in h["criterios"]:
        for tc in c["casos"]:
            por_caso[tc].append(c)
    for tc, crits in por_caso.items():
        info = casos[tc]
        r = resultados.get(tc, {"estado": "No ejecutado"})
        if info["herramienta"] == "Manual":
            auto = "Manual: " + info.get("pasos", "")
        else:
            auto = " | ".join(dict.fromkeys(ubicacion.get(tc, []))) or "PENDIENTE"
        e = epicas[h["epica"]]
        filas.append({
            "Rol": h["rol"], "Épica": f'{e["id"]} {e["nombre"]}', "Historia": h["id"], "Título de la historia": h["titulo"],
            "Criterios": "; ".join(f'{c["id"]} {c["titulo"]}' for c in crits),
            "Caso": tc, "Título del caso": info["titulo"], "Herramienta": info["herramienta"], "Automatización": auto,
            "Estado": r["estado"], "Fecha": r.get("fecha", ""), "Evidencia": r.get("evidencia", ""), "Detalle": r.get("detalle", ""),
        })

with open(os.path.join(SALIDA, "matriz_trazabilidad.csv"), "w", newline="", encoding="utf-8-sig") as f:
    w = csv.DictWriter(f, fieldnames=list(filas[0].keys()), delimiter=";")
    w.writeheader(); w.writerows(filas)

# ------------------------------------------------------------------ cobertura (sobre casos distintos)
criterios = [c for h in datos["historias"] for c in h["criterios"]]
sin_caso = [c["id"] for c in criterios if not c["casos"]]
unicos = {}
for x in filas:
    unicos.setdefault(x["Caso"], x)
sin_auto = [tc for tc, x in unicos.items() if x["Automatización"] == "PENDIENTE"]
automatizables = [tc for tc, x in unicos.items() if x["Herramienta"] != "Manual"]
por_herr = Counter(x["Herramienta"] for x in unicos.values())
por_estado = Counter(x["Estado"] for x in unicos.values())
estado_herr = defaultdict(Counter)
for x in unicos.values():
    estado_herr[x["Herramienta"]][x["Estado"]] += 1
resumen = (f"{len(datos['historias'])} historias · {len(criterios)} criterios ({len(criterios) - len(sin_caso)} con caso) · "
           f"{len(unicos)} casos · automatizados con ubicación {len(automatizables) - len(sin_auto)}/{len(automatizables)} "
           f"+ {len(unicos) - len(automatizables)} manuales")

# ------------------------------------------------------------------ Markdown
md = [f"# Matriz de trazabilidad — {datos['proyecto']}", "", f"- {resumen}",
      "- Último resultado: " + ", ".join(f"{k}: {v}" for k, v in por_estado.most_common()), "",
      "| Herramienta | Pasó | Falló | No ejecutado |", "|---|---|---|---|"]
for herr, c in estado_herr.items():
    md.append(f"| {herr} | {c['Pasó']} | {c['Falló']} | {c['No ejecutado']} |")
if sin_caso:
    md += ["", "**Criterios sin caso:** " + ", ".join(sin_caso)]
actual = None
for x in filas:
    if x["Historia"] != actual:
        actual = x["Historia"]
        md += ["", f"## {x['Historia']} — {x['Título de la historia']}", "", "| Caso | Herramienta | Criterios | Código | Estado |", "|---|---|---|---|---|"]
    auto = "manual" if x["Herramienta"] == "Manual" else x["Automatización"].split(" | ")[0].split(" — ")[0]
    md.append(f"| {x['Caso']} {x['Título del caso']} | {x['Herramienta']} | {x['Criterios'].replace('; ', '<br>')} | `{auto}` | {x['Estado']} |")
open(os.path.join(SALIDA, "matriz_trazabilidad.md"), "w", encoding="utf-8").write("\n".join(md) + "\n")


# ------------------------------------------------------------------ HTML autocontenido con filtros
def esc(s): return html.escape(str(s))


filas_html = []
for x in filas:
    clase = {"Pasó": "ok", "Falló": "ko"}.get(x["Estado"], "pend")
    auto = "<br>".join(f"<code>{esc(a)}</code>" for a in x["Automatización"].split(" | "))
    filas_html.append(
        f'<tr data-r="{esc(x["Rol"])}" data-h="{esc(x["Herramienta"])}" data-e="{esc(x["Estado"])}" '
        f'data-t="{esc((x["Historia"] + x["Caso"] + x["Título del caso"] + x["Título de la historia"] + x["Criterios"]).lower())}">'
        f'<td><b>{esc(x["Historia"])}</b><br><small>{esc(x["Título de la historia"])}</small></td><td>{esc(x["Criterios"]).replace("; ", "<br>")}</td>'
        f'<td><b>{esc(x["Caso"])}</b><br>{esc(x["Título del caso"])}</td><td>{esc(x["Herramienta"])}</td><td class="auto">{auto}</td>'
        f'<td><span class="st {clase}">{esc(x["Estado"])}</span><br><small>{esc(x["Fecha"])}</small></td></tr>')
tarjetas = "".join(f'<div class="card"><div class="n">{v}</div><div>{esc(k)}</div><small>'
                   f'{estado_herr[k]["Pasó"]} pasó · {estado_herr[k]["Falló"]} falló · {estado_herr[k]["No ejecutado"]} sin ejecutar</small></div>'
                   for k, v in por_herr.items())
pagina = f"""<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Matriz de trazabilidad</title><style>
:root{{--bg:#f7f8fa;--fg:#1c2230;--mut:#5d6677;--bd:#dfe3ea;--card:#fff;--ok:#1a7f4b;--ko:#b3261e;--pend:#8a6a00}}
@media (prefers-color-scheme:dark){{:root{{--bg:#12151b;--fg:#e7e9ee;--mut:#9aa3b2;--bd:#2a303a;--card:#1a1e26;--ok:#5cc98d;--ko:#ff8a80;--pend:#e3c15a}}}}
body{{margin:0;padding:24px 16px;background:var(--bg);color:var(--fg);font:14px/1.5 system-ui,Segoe UI,Roboto,sans-serif}}
h1{{font-size:22px;margin:0 0 4px}}p.sub{{color:var(--mut);margin:0 0 16px}}
.cards{{display:flex;gap:12px;flex-wrap:wrap;margin-bottom:16px}}.card{{background:var(--card);border:1px solid var(--bd);border-radius:10px;padding:12px 16px;flex:1 1 140px;max-width:220px}}
.card .n{{font-size:26px;font-weight:700}}.card small{{color:var(--mut)}}
.filtros{{display:flex;gap:8px;flex-wrap:wrap;margin:8px 0 12px}}select,input{{font:inherit;padding:6px 8px;border:1px solid var(--bd);border-radius:6px;background:var(--card);color:var(--fg)}}
.wrap{{overflow-x:auto;background:var(--card);border:1px solid var(--bd);border-radius:10px}}table{{border-collapse:collapse;width:100%;min-width:980px}}
th,td{{padding:8px 10px;border-bottom:1px solid var(--bd);text-align:left;vertical-align:top}}th{{position:sticky;top:0;background:var(--card);font-size:12px;text-transform:uppercase;color:var(--mut)}}
small{{color:var(--mut)}}code{{font-size:11.5px;word-break:break-all}}td.auto{{max-width:360px}}
.st{{font-weight:600}}.st.ok{{color:var(--ok)}}.st.ko{{color:var(--ko)}}.st.pend{{color:var(--pend)}}
</style></head><body>
<h1>Matriz de trazabilidad — {esc(datos['proyecto'])}</h1>
<p class="sub">{esc(resumen)}</p>
<div class="cards">{tarjetas}</div>
<div class="filtros"><input id="q" placeholder="Buscar historia, criterio, caso o texto" aria-label="Buscar">
<select id="fr" aria-label="Rol"><option value="">Viajero y proveedor</option><option>Viajero</option><option>Proveedor</option></select>
<select id="fh" aria-label="Herramienta"><option value="">Todas las herramientas</option>{''.join(f'<option>{esc(k)}</option>' for k in por_herr)}</select>
<select id="fe" aria-label="Estado"><option value="">Todos los estados</option><option>Pasó</option><option>Falló</option><option>No ejecutado</option></select>
<span id="n"></span></div>
<div class="wrap"><table><thead><tr><th>Historia</th><th>Criterios</th><th>Caso</th><th>Herramienta</th><th>Código</th><th>Resultado</th></tr></thead>
<tbody>{''.join(filas_html)}</tbody></table></div>
<script>
const q=document.getElementById('q'),fr=document.getElementById('fr'),fh=document.getElementById('fh'),fe=document.getElementById('fe'),n=document.getElementById('n');
function f(){{let c=0;document.querySelectorAll('tbody tr').forEach(r=>{{const v=(!fr.value||r.dataset.r===fr.value)&&(!fh.value||r.dataset.h===fh.value)&&(!fe.value||r.dataset.e===fe.value)&&(!q.value||r.dataset.t.includes(q.value.toLowerCase()));r.hidden=!v;if(v)c++}});n.textContent=c+' filas'}}
[q,fr,fh,fe].forEach(x=>x.addEventListener('input',f));f();
</script></body></html>"""
open(os.path.join(SALIDA, "matriz_trazabilidad.html"), "w", encoding="utf-8").write(pagina)

# ------------------------------------------------------------------ resultados para Jira
with open(os.path.join(SALIDA, "jira_ejecuciones.csv"), "w", newline="", encoding="utf-8-sig") as f:
    w = csv.writer(f)
    w.writerow(["Caso", "Resumen", "Historias", "Herramienta", "Estado", "Fecha", "Evidencia", "Detalle"])
    historias_de = defaultdict(list)
    for x in filas:
        historias_de[x["Caso"]].append(x["Historia"])
    for tc, x in unicos.items():
        w.writerow([tc, x["Título del caso"], ", ".join(historias_de[tc]), x["Herramienta"], x["Estado"], x["Fecha"], x["Evidencia"], x["Detalle"]])

print(resumen + " · estados " + str(dict(por_estado)))
if sin_caso: print("Criterios sin caso:", sin_caso)
if sin_auto: print("Casos sin automatización ubicada:", sin_auto)
