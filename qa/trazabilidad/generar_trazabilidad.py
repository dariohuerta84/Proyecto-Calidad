#!/usr/bin/env python3
"""
Genera la matriz de trazabilidad HU → criterio → caso → herramienta → automatización → resultado, y los CSV para Jira.
Entradas:  datos/historias.json (HU y casos de la etapa 1)  ·  datos/resultados.json (recolectar_resultados.py)
           y el código de las suites (../karate, ../selenium, ../appium, ../jmeter) para ubicar cada caso.
Salidas en salida/: matriz_trazabilidad.csv/.md/.html, jira_casos_prueba.csv, jira_ejecuciones.csv
Uso: python generar_trazabilidad.py
"""
import csv, glob, html, json, os, re
from collections import Counter, defaultdict

AQUI = os.path.dirname(os.path.abspath(__file__))
QA = os.path.normpath(os.path.join(AQUI, ".."))
SALIDA = os.path.join(AQUI, "salida")
os.makedirs(SALIDA, exist_ok=True)

datos = json.load(open(os.path.join(AQUI, "datos/historias.json"), encoding="utf-8"))
# Casos agregados en la etapa 5 y criterios que ya verificaban casos existentes
extra_path = os.path.join(AQUI, "datos/casos_etapa5.json")
if os.path.exists(extra_path):
    extra = json.load(open(extra_path, encoding="utf-8"))
    for h in datos["historias"]:
        for c in extra.get("casos_nuevos", {}).get(h["key"], []):
            h["casos"].append(dict(c, nuevo=True))
        for c in h["casos"]:
            for ca in extra.get("criterios_adicionales", {}).get(c["id"], []):
                if ca not in c["criterios"]:
                    c["criterios"] = c["criterios"] + [ca]
res_path = os.path.join(AQUI, "datos/resultados.json")
resultados = json.load(open(res_path, encoding="utf-8")) if os.path.exists(res_path) else {}

# ------------------------------------------------------------------ dónde está automatizado cada caso
ubicacion = defaultdict(list)
for f in glob.glob(os.path.join(QA, "karate/src/test/java/**/*.feature"), recursive=True):
    lineas = open(f, encoding="utf-8").read().splitlines()
    for i, l in enumerate(lineas):
        for tc in re.findall(r"@(TC-API-\d+)", l):
            titulo = next((x.strip() for x in lineas[i + 1:i + 3] if x.strip().startswith("Scenario")), "")
            ubicacion[tc].append(f"karate/{os.path.relpath(f, os.path.join(QA, 'karate'))}:{i + 2} — {titulo.split(':', 1)[-1].strip()}")
for carpeta in ("selenium", "appium"):
    for f in glob.glob(os.path.join(QA, carpeta, "src/test/java/**/*.java"), recursive=True):
        s = open(f, encoding="utf-8").read()
        for m in re.finditer(r'@Tag\("(TC-(?:WEB|MOB)-\d+)"\)[\s\S]*?void\s+(\w+)\(', s):
            linea = s[:m.start()].count("\n") + 1
            ubicacion[m.group(1)].append(f"{carpeta}/{os.path.relpath(f, os.path.join(QA, carpeta))}:{linea} — {os.path.basename(f)[:-5]}#{m.group(2)}")
jmx = os.path.join(QA, "jmeter/trouvels360.jmx")
if os.path.exists(jmx):
    for m in re.finditer(r'testname="(TC-PERF-\d+)([^"]*)"', open(jmx, encoding="utf-8").read()):
        if "Verificación" in m.group(2) or "(" not in m.group(2) and " - " not in m.group(2):
            continue
        ubicacion[m.group(1)].append(f"jmeter/trouvels360.jmx — {html.unescape(m.group(1) + m.group(2))}")

# ------------------------------------------------------------------ filas de la matriz
filas = []
epica_nombre = {e["key"]: e["nombre"] for e in datos["epicas"]}
for h in datos["historias"]:
    ca_titulo = {c["id"]: c["titulo"] for c in h["criterios"]}
    for c in h["casos"]:
        r = resultados.get(c["id"], {"estado": "No ejecutado"})
        filas.append({
            "Épica": f'{h["epica"]} {epica_nombre.get(h["epica"], "")}', "HU": h["key"], "Historia": h["resumen"],
            "Criterios": "; ".join(f'{ca} {ca_titulo.get(ca, "")}' for ca in c["criterios"]),
            "Caso": c["id"], "Título del caso": c["titulo"], "Herramienta": c["herramienta"],
            "Automatización": (" | ".join(dict.fromkeys(ubicacion.get(c["id"], []))) or "PENDIENTE") if c["herramienta"] != "Manual"
                              else "Manual: " + c.get("pasos", ""),
            "Estado": r["estado"], "Fecha": r.get("fecha", ""), "Evidencia": r.get("evidencia", ""), "Detalle": r.get("detalle", ""),
        })

cols = list(filas[0].keys())
with open(os.path.join(SALIDA, "matriz_trazabilidad.csv"), "w", newline="", encoding="utf-8-sig") as f:
    w = csv.DictWriter(f, fieldnames=cols, delimiter=";")
    w.writeheader(); w.writerows(filas)

# ------------------------------------------------------------------ cobertura
criterios_total = [(h["key"], c["id"]) for h in datos["historias"] for c in h["criterios"]]
cubiertos = {ca for h in datos["historias"] for c in h["casos"] for ca in c["criterios"]}
sin_caso = [ca for _, ca in criterios_total if ca not in cubiertos]
sin_auto = [x["Caso"] for x in filas if x["Automatización"] == "PENDIENTE"]
automatizables = [x for x in filas if x["Herramienta"] != "Manual"]
por_herr = Counter(x["Herramienta"] for x in filas)
por_estado = Counter(x["Estado"] for x in filas)
estado_herr = defaultdict(Counter)
for x in filas:
    estado_herr[x["Herramienta"]][x["Estado"]] += 1

# ------------------------------------------------------------------ Markdown
md = [f"# Matriz de trazabilidad — {datos['proyecto']} (TRV-605)", "",
      f"- Historias: **{len(datos['historias'])}** · Criterios de aceptación: **{len(criterios_total)}** "
      f"(cubiertos por al menos un caso: **{len(criterios_total) - len(sin_caso)}**)",
      f"- Casos de prueba: **{len(filas)}** — " + ", ".join(f"{k}: {v}" for k, v in por_herr.items()),
      f"- Casos automatizados con su ubicación en el código: **{len(automatizables) - len(sin_auto)} / {len(automatizables)}** "
      f"(+ {len(filas) - len(automatizables)} manuales con pasos)",
      f"- Casos agregados en la etapa 5 (criterios sin caso en la etapa 1): {sum(1 for h in datos['historias'] for c in h['casos'] if c.get('nuevo'))}",
      "- Último resultado: " + ", ".join(f"{k}: {v}" for k, v in por_estado.most_common()), "",
      "| Herramienta | " + " | ".join(["Pasó", "Falló", "No ejecutado"]) + " |", "|---|---|---|---|"]
for herr, c in estado_herr.items():
    md.append(f"| {herr} | {c['Pasó']} | {c['Falló']} | {c['No ejecutado']} |")
if sin_caso:
    md += ["", "**Criterios sin caso:** " + ", ".join(sin_caso)]
actual = None
for x in filas:
    if x["HU"] != actual:
        actual = x["HU"]
        md += ["", f"## {x['HU']} — {x['Historia']}", "", "| Caso | Herramienta | Criterios | Automatización | Estado |", "|---|---|---|---|---|"]
    auto = "manual" if x["Herramienta"] == "Manual" else x["Automatización"].split(" | ")[0].split(" — ")[0]
    md.append(f"| {x['Caso']} {x['Título del caso']} | {x['Herramienta']} | {x['Criterios'].replace(';', '<br>')} | `{auto}` | {x['Estado']} |")
open(os.path.join(SALIDA, "matriz_trazabilidad.md"), "w", encoding="utf-8").write("\n".join(md) + "\n")

# ------------------------------------------------------------------ HTML (autocontenido, con filtros)
def e(s): return html.escape(str(s))
filas_html = []
for x in filas:
    clase = {"Pasó": "ok", "Falló": "ko"}.get(x["Estado"], "pend")
    auto = "<br>".join(f"<code>{e(a)}</code>" for a in x["Automatización"].split(" | "))
    filas_html.append(
        f'<tr data-h="{e(x["Herramienta"])}" data-e="{e(x["Estado"])}" data-t="{e((x["HU"] + x["Caso"] + x["Título del caso"] + x["Historia"]).lower())}">'
        f'<td><b>{e(x["HU"])}</b><br><small>{e(x["Historia"])}</small></td><td>{e(x["Criterios"]).replace("; ", "<br>")}</td>'
        f'<td><b>{e(x["Caso"])}</b><br>{e(x["Título del caso"])}</td><td>{e(x["Herramienta"])}</td><td class="auto">{auto}</td>'
        f'<td><span class="st {clase}">{e(x["Estado"])}</span><br><small>{e(x["Fecha"])}</small></td></tr>')
tarjetas = "".join(f'<div class="card"><div class="n">{v}</div><div>{e(k)}</div><small>'
                   f'{estado_herr[k]["Pasó"]} pasó · {estado_herr[k]["Falló"]} falló · {estado_herr[k]["No ejecutado"]} sin ejecutar</small></div>'
                   for k, v in por_herr.items())
pagina = f"""<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Matriz de trazabilidad</title><style>
:root{{--bg:#f7f8fa;--fg:#1c2230;--mut:#5d6677;--bd:#dfe3ea;--card:#fff;--ok:#1a7f4b;--ko:#b3261e;--pend:#8a6a00;--acc:#0b5cad}}
@media (prefers-color-scheme:dark){{:root{{--bg:#12151b;--fg:#e7e9ee;--mut:#9aa3b2;--bd:#2a303a;--card:#1a1e26;--ok:#5cc98d;--ko:#ff8a80;--pend:#e3c15a;--acc:#7db7ff}}}}
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
<h1>Matriz de trazabilidad — {e(datos['proyecto'])}</h1>
<p class="sub">{len(datos['historias'])} historias · {len(criterios_total)} criterios ({len(criterios_total) - len(sin_caso)} cubiertos) · {len(filas)} casos ·
automatizados {len(automatizables) - len(sin_auto)}/{len(automatizables)} + {len(filas) - len(automatizables)} manuales</p>
<div class="cards">{tarjetas}</div>
<div class="filtros"><input id="q" placeholder="Buscar HU, caso o texto" aria-label="Buscar">
<select id="fh" aria-label="Herramienta"><option value="">Todas las herramientas</option>{''.join(f'<option>{e(k)}</option>' for k in por_herr)}</select>
<select id="fe" aria-label="Estado"><option value="">Todos los estados</option><option>Pasó</option><option>Falló</option><option>No ejecutado</option></select>
<span id="n"></span></div>
<div class="wrap"><table><thead><tr><th>Historia</th><th>Criterios</th><th>Caso</th><th>Herramienta</th><th>Automatización</th><th>Resultado</th></tr></thead>
<tbody>{''.join(filas_html)}</tbody></table></div>
<script>
const q=document.getElementById('q'),fh=document.getElementById('fh'),fe=document.getElementById('fe'),n=document.getElementById('n');
function f(){{let c=0;document.querySelectorAll('tbody tr').forEach(r=>{{const v=(!fh.value||r.dataset.h===fh.value)&&(!fe.value||r.dataset.e===fe.value)&&(!q.value||r.dataset.t.includes(q.value.toLowerCase()));r.hidden=!v;if(v)c++}});n.textContent=c+' casos'}}
[q,fh,fe].forEach(x=>x.addEventListener('input',f));f();
</script></body></html>"""
open(os.path.join(SALIDA, "matriz_trazabilidad.html"), "w", encoding="utf-8").write(pagina)

# ------------------------------------------------------------------ CSV de importación a Jira
prioridad = {h["key"]: h["prioridad"] for h in datos["historias"]}
with open(os.path.join(SALIDA, "jira_casos_prueba.csv"), "w", newline="", encoding="utf-8-sig") as f:
    w = csv.writer(f)
    w.writerow(["Issue Type", "Summary", "Description", "Priority", "Labels", "Labels", "Labels", "Component/s", "Epic Link", "Link Tests"])
    for h in datos["historias"]:
        for c in h["casos"]:
            crit = {x["id"]: x for x in h["criterios"]}
            desc = [f"*Historia:* {h['key']} — {h['resumen']}", f"*Herramienta:* {c['herramienta']}", "", "*Criterios cubiertos:*"]
            for ca in c["criterios"]:
                x = crit.get(ca)
                if x:
                    desc.append(f"- {ca} {x['titulo']}: Dado {x['dado']}, cuando {x['cuando']}, entonces {x['entonces']}.")
            if c["herramienta"] == "Manual":
                desc += ["", "*Pasos (prueba manual):*", c.get("pasos", "")]
            else:
                desc += ["", "*Automatización:*"] + [f"- {{{{{a}}}}}" for a in dict.fromkeys(ubicacion.get(c["id"], ["pendiente"]))]
            w.writerow(["Test", f"{c['id']} {c['titulo']}", "\n".join(desc), prioridad.get(h["key"], "Media"),
                        c["herramienta"].lower(), h["key"], c["id"], "QA", h["epica"], h["key"]])

with open(os.path.join(SALIDA, "jira_ejecuciones.csv"), "w", newline="", encoding="utf-8-sig") as f:
    w = csv.writer(f)
    w.writerow(["Caso", "Resumen", "HU", "Herramienta", "Estado", "Fecha", "Evidencia", "Detalle"])
    for x in filas:
        w.writerow([x["Caso"], x["Título del caso"], x["HU"], x["Herramienta"], x["Estado"], x["Fecha"], x["Evidencia"], x["Detalle"]])

print(f"{len(filas)} casos · criterios {len(criterios_total) - len(sin_caso)}/{len(criterios_total)} cubiertos · "
      f"automatizados {len(automatizables) - len(sin_auto)}/{len(automatizables)} · estados {dict(por_estado)}")
if sin_caso: print("Criterios sin caso:", sin_caso)
if sin_auto: print("Casos sin automatización ubicada:", sin_auto)
