package com.trouvels360.web.soporte;

import java.io.InputStream;
import java.util.Properties;

/** Lee config.properties y permite pisar cada clave con -Dclave=valor o con la variable de entorno CLAVE_CON_GUIONES. */
public final class Config {
    private static final Properties P = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in != null) P.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer config.properties", e);
        }
    }

    private Config() { }

    public static String get(String clave) {
        String sys = System.getProperty(clave);
        if (sys != null && !sys.isBlank()) return sys.trim();
        String env = System.getenv(clave.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env.trim();
        return P.getProperty(clave, "").trim();
    }

    public static int entero(String clave, int defecto) {
        String v = get(clave);
        return v.isEmpty() ? defecto : Integer.parseInt(v);
    }

    public static boolean bool(String clave) { return Boolean.parseBoolean(get(clave)); }

    public static String baseUrl() { return get("base.url").replaceAll("/$", ""); }

    public static String apiUrl() { return get("api.url").replaceAll("/$", ""); }
}
