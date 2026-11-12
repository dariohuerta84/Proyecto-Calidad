package com.trouvels360.web.soporte;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Reporte HTML propio (además de Allure): una fila por caso con HU, estado, duración, error y capturas.
 * Se reescribe después de cada prueba en target/reporte-selenium/index.html, así queda útil aunque la corrida se corte.
 */
public final class ReporteHtml {
    public record Fila(String caso, String hu, String clase, String nombre, String estado, long ms, String error, List<String> capturas) { }

    private static final List<Fila> FILAS = new ArrayList<>();
    private static final String INICIO = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    private final static String TITULO_DEF = "Trouvels360 · Pruebas E2E";

    private ReporteHtml() { }

    public static Path carpeta() {
        Path p = Path.of(System.getProperty("reporte.dir", "target/reporte-selenium"));
        try { Files.createDirectories(p.resolve("capturas")); } catch (Exception ignored) { }
        return p;
    }

    public static synchronized void agregar(Fila f) {
        FILAS.add(f);
        escribir();
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static synchronized void escribir() {
        long ok = FILAS.stream().filter(f -> f.estado().equals("OK")).count();
        long falla = FILAS.stream().filter(f -> f.estado().equals("FALLA")).count();
        long omit = FILAS.size() - ok - falla;
        StringBuilder h = new StringBuilder();
        String titulo = System.getProperty("reporte.titulo", TITULO_DEF);
        h.append("<!doctype html><html lang=\"es\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
         .append("<title>").append(esc(titulo)).append("</title><style>")
         .append(":root{--ok:#1b7f4b;--ko:#b3261e;--om:#8a6d00;--bg:#f6f7f9;--tx:#1d2330;--bd:#dde1e7}")
         .append("@media (prefers-color-scheme:dark){:root{--bg:#14171c;--tx:#e6e8eb;--bd:#2c3139}}")
         .append("body{font:14px/1.45 system-ui,Segoe UI,Roboto,sans-serif;margin:0;padding:24px 16px;background:var(--bg);color:var(--tx)}")
         .append("h1{font-size:20px;margin:0 0 4px}.res{display:flex;gap:12px;margin:16px 0;flex-wrap:wrap}.res div{padding:10px 14px;border:1px solid var(--bd);border-radius:8px}")
         .append("table{border-collapse:collapse;width:100%}th,td{border-bottom:1px solid var(--bd);padding:8px;text-align:left;vertical-align:top}")
         .append(".OK{color:var(--ok);font-weight:600}.FALLA{color:var(--ko);font-weight:600}.OMITIDA{color:var(--om);font-weight:600}")
         .append("pre{white-space:pre-wrap;margin:4px 0 0;font-size:12px;max-width:560px}img{max-width:260px;border:1px solid var(--bd);border-radius:4px;margin:2px}")
         .append(".scroll{overflow-x:auto}</style></head><body>")
         .append("<h1>").append(esc(titulo)).append("</h1><div>Inicio: ").append(INICIO).append(" · Base: ").append(esc(Config.baseUrl())).append("</div>")
         .append("<div class=\"res\"><div>Total <b>").append(FILAS.size()).append("</b></div><div class=\"OK\">OK ").append(ok)
         .append("</div><div class=\"FALLA\">Fallas ").append(falla).append("</div><div class=\"OMITIDA\">Omitidas ").append(omit).append("</div></div>")
         .append("<div class=\"scroll\"><table><thead><tr><th>Caso</th><th>HU</th><th>Prueba</th><th>Estado</th><th>Duración</th><th>Evidencia</th></tr></thead><tbody>");
        for (Fila f : FILAS) {
            h.append("<tr><td>").append(esc(f.caso())).append("</td><td>").append(esc(f.hu())).append("</td><td>").append(esc(f.nombre()))
             .append("<br><small>").append(esc(f.clase())).append("</small>");
            if (f.error() != null && !f.error().isBlank()) h.append("<pre>").append(esc(f.error())).append("</pre>");
            h.append("</td><td class=\"").append(f.estado()).append("\">").append(f.estado()).append("</td><td>")
             .append(String.format("%.1f s", f.ms() / 1000.0)).append("</td><td>");
            for (String c : f.capturas()) h.append("<a href=\"").append(esc(c)).append("\"><img src=\"").append(esc(c)).append("\" alt=\"captura\"></a>");
            h.append("</td></tr>");
        }
        h.append("</tbody></table></div></body></html>");
        try {
            Files.writeString(carpeta().resolve("index.html"), h.toString(), StandardCharsets.UTF_8);
            // Versión legible por máquina: la usa qa/trazabilidad/recolectar_resultados.py para la matriz y Jira
            List<java.util.Map<String, Object>> filas = new ArrayList<>();
            for (Fila f : FILAS) {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("caso", f.caso()); m.put("hu", f.hu()); m.put("prueba", f.nombre()); m.put("clase", f.clase());
                m.put("estado", f.estado()); m.put("ms", f.ms()); m.put("error", f.error()); m.put("capturas", f.capturas());
                m.put("fecha", INICIO);
                filas.add(m);
            }
            Files.writeString(carpeta().resolve("resultados.json"), new org.openqa.selenium.json.Json().toJson(filas), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("No se pudo escribir el reporte: " + e.getMessage());
        }
    }
}
