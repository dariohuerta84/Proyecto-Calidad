package com.trouvels360.web.soporte;

import org.openqa.selenium.json.Json;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cliente de la API para preparar datos ANTES de cada prueba (no se prueba la API aquí: eso lo hace Karate).
 * Maneja la sesión igual que el navegador: cookie HttpOnly access_token + CSRF de doble envío (XSRF-TOKEN).
 */
public final class Api {
    private static final Json JSON = new Json();
    public static final Map<String, Object> PAGO = Map.of("titular", "QA Selenium", "numero", "4242424242424242", "vencimiento", "12/39", "cvc", "123");

    private Api() { }

    // ------------------------------------------------------------------ sesión
    public record Resp(int status, Object cuerpo) {
        @SuppressWarnings("unchecked")
        public Map<String, Object> mapa() { return (Map<String, Object>) cuerpo; }
        @SuppressWarnings("unchecked")
        public List<Map<String, Object>> lista() { return (List<Map<String, Object>>) cuerpo; }
        @SuppressWarnings("unchecked")
        public Map<String, Object> data() { return (Map<String, Object>) mapa().get("data"); }
        @SuppressWarnings("unchecked")
        public List<Map<String, Object>> dataLista() { return (List<Map<String, Object>>) mapa().get("data"); }
        public Resp esperar(int esperado) {
            if (status != esperado) throw new IllegalStateException("La API respondió " + status + " (se esperaba " + esperado + "): " + cuerpo);
            return this;
        }
    }

    public static final class Sesion {
        private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        private final HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).connectTimeout(Duration.ofSeconds(10)).build();
        public Map<String, Object> usuario;

        public String cookie(String nombre) {
            for (HttpCookie c : cookies.getCookieStore().getCookies()) {
                if (c.getName().equals(nombre)) return URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
            }
            return "";
        }

        public String token() { return cookie("access_token"); }
        public String xsrf() { return cookie("XSRF-TOKEN"); }
        public long id() { return ((Number) usuario.get("id")).longValue(); }
        public String email() { return (String) usuario.get("email"); }

        public Resp get(String ruta) { return enviar("GET", ruta, null, Map.of()); }
        public Resp post(String ruta, Object cuerpo) { return enviar("POST", ruta, cuerpo, Map.of()); }
        public Resp post(String ruta, Object cuerpo, Map<String, String> cab) { return enviar("POST", ruta, cuerpo, cab); }
        public Resp put(String ruta, Object cuerpo) { return enviar("PUT", ruta, cuerpo, Map.of()); }
        public Resp delete(String ruta) { return enviar("DELETE", ruta, null, Map.of()); }

        public Resp enviar(String metodo, String ruta, Object cuerpo, Map<String, String> cab) {
            try {
                HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(Config.apiUrl() + ruta))
                        .timeout(Duration.ofSeconds(30))
                        .header("Accept", "application/json").header("Content-Type", "application/json");
                if (!xsrf().isEmpty()) b.header("X-XSRF-TOKEN", xsrf());
                cab.forEach(b::header);
                b.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(JSON.toJson(cuerpo), StandardCharsets.UTF_8));
                HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                Object c = r.body() == null || r.body().isBlank() ? null : JSON.toType(r.body(), Object.class);
                return new Resp(r.statusCode(), c);
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo llamar a " + metodo + " " + ruta + ": " + e.getMessage(), e);
            }
        }
    }

    // ------------------------------------------------------------------ autenticación
    public static Sesion login(String email, String password) {
        Sesion s = new Sesion();
        s.get("/auth/csrf").esperar(200);
        Resp r = s.post("/auth/login", Map.of("email", email, "password", password)).esperar(200);
        s.usuario = asMap(r.data().get("user"));
        return s;
    }

    public static Sesion viajero() { return login(Config.get("viajero.email"), Config.get("viajero.password")); }
    public static Sesion viajeroQa(int n) { return login("qa.viajero" + n + "@trouvels360.test", Config.get("viajero.qa.password")); }
    public static Sesion proveedor() { return login(Config.get("proveedor.email"), Config.get("proveedor.password")); }
    public static Sesion proveedor2() { return login(Config.get("proveedor2.email"), Config.get("proveedor2.password")); }

    public static Sesion registrarViajero(String nombre) {
        Sesion s = new Sesion();
        s.get("/auth/csrf").esperar(200);
        Resp r = s.post("/auth/register", Map.of("rol", "viajero", "nombre", nombre, "apellido", "Selenium",
                "email", Datos.correo("web.viajero"), "password", "Clave123", "password_confirmation", "Clave123")).esperar(201);
        s.usuario = asMap(r.data().get("user"));
        return s;
    }

    public static Sesion registrarProveedor() {
        Sesion s = new Sesion();
        s.get("/auth/csrf").esperar(200);
        Resp r = s.post("/auth/register", Map.of("rol", "proveedor", "empresa_nombre", "Web QA " + Datos.sufijo() + " SAC",
                "ruc", Datos.ruc(), "telefono", Datos.celular(), "email", Datos.correo("web.prov"),
                "password", "Clave123", "password_confirmation", "Clave123")).esperar(201);
        s.usuario = asMap(r.data().get("user"));
        return s;
    }

    // ------------------------------------------------------------------ publicaciones del proveedor
    public record Hotel(long id, String nombre, String ciudad, List<Map<String, Object>> habitaciones) {
        public long hab(String nombre) {
            return habitaciones.stream().filter(h -> nombre.equals(h.get("nombre"))).map(h -> ((Number) h.get("id")).longValue())
                    .findFirst().orElseThrow();
        }
    }

    /** tipos: pares nombre, unidades, precio (p. ej. "Doble", 3, 150). */
    public static Hotel crearHotel(Sesion prov, Object... tipos) {
        String ciudad = Datos.ciudad();
        List<Map<String, Object>> habs = new ArrayList<>();
        for (int i = 0; i < tipos.length; i += 3) {
            habs.add(Map.of("nombre", tipos[i], "capacidad_adultos", 2, "capacidad_ninos", 0, "cantidad", tipos[i + 1], "precio_por_noche", tipos[i + 2]));
        }
        String nombre = "Hotel " + ciudad;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("nombre", nombre); cuerpo.put("descripcion", "Hotel creado por la suite Selenium"); cuerpo.put("ciudad", ciudad);
        cuerpo.put("pais", "Perú"); cuerpo.put("imagen_url", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800");
        cuerpo.put("direccion", "Av. Selenium 100"); cuerpo.put("estrellas", 4); cuerpo.put("latitud", -13.52); cuerpo.put("longitud", -71.97);
        cuerpo.put("habitaciones", habs);
        Resp r = prov.post("/hoteles", cuerpo).esperar(201);
        long id = num(r.data().getOrDefault("servicio_id", r.data().get("id")));
        return new Hotel(id, nombre, ciudad, detalleHotel(prov, id));
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> detalleHotel(Sesion s, long id) {
        return (List<Map<String, Object>>) s.get("/hoteles/" + id).esperar(200).mapa().get("habitaciones");
    }

    public record Tour(long id, String nombre, String ciudad, long salidaId, String fechaSalida) { }

    public static Tour crearTour(Sesion prov, int cupo, double precio, String categoria) {
        return crearTour(prov, cupo, precio, categoria, Datos.ciudad(), Datos.fecha(Datos.diaLibre()));
    }

    /** Tour en una ciudad y fecha dadas (p. ej. la misma ciudad de un hotel, para el itinerario). */
    public static Tour crearTour(Sesion prov, int cupo, double precio, String categoria, String ciudad, String fecha) {
        String nombre = "Tour " + ciudad;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("nombre", nombre); cuerpo.put("descripcion", "Tour creado por la suite Selenium"); cuerpo.put("ciudad", ciudad);
        cuerpo.put("pais", "Perú"); cuerpo.put("imagen_url", "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800");
        cuerpo.put("categoria", categoria); cuerpo.put("duracion", 120); cuerpo.put("precio", precio);
        cuerpo.put("latitud", -13.52); cuerpo.put("longitud", -71.97);
        cuerpo.put("salidas", List.of(Map.of("fecha", fecha, "hora", "08:00", "cupo_total", cupo)));
        long id = num(prov.post("/tours", cuerpo).esperar(201).data().get("id"));
        long salida = num(prov.get("/tours/" + id + "/salidas").esperar(200).lista().get(0).get("id"));
        return new Tour(id, nombre, ciudad, salida, fecha);
    }

    public static Map<String, Object> salida(Sesion prov, long tourId, long salidaId) {
        return prov.get("/tours/" + tourId + "/salidas").esperar(200).lista().stream()
                .filter(s -> num(s.get("id")) == salidaId).findFirst().orElseThrow();
    }

    // ------------------------------------------------------------------ reservas
    /** Crea una solicitud de reserva de hotel y devuelve la primera reserva (id, codigo...). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> reservarHotel(Sesion viajero, long habitacionId, int dia, int noches, int unidades) {
        Resp r = viajero.post("/reservas-habitaciones", Map.of("fecha_inicio", Datos.fecha(dia), "fecha_fin", Datos.fecha(dia + noches),
                "items", List.of(Map.of("habitacion_id", habitacionId, "cantidad", unidades)), "pago", PAGO),
                Map.of("Idempotency-Key", UUID.randomUUID().toString())).esperar(201);
        return ((List<Map<String, Object>>) r.data().get("reservas")).get(0);
    }

    public static Map<String, Object> reservarTour(Sesion viajero, long salidaId, int personas) {
        return viajero.post("/tours/salidas/" + salidaId + "/reservas", Map.of("personas", personas, "pago", PAGO),
                Map.of("Idempotency-Key", UUID.randomUUID().toString())).esperar(201).data();
    }

    public static void aceptar(Sesion prov, Object reservaId) { prov.post("/proveedor/reservas-habitaciones/" + reservaId + "/aceptar", Map.of()).esperar(200); }

    public static void rechazar(Sesion prov, Object reservaId, String motivo) {
        prov.post("/proveedor/reservas-habitaciones/" + reservaId + "/rechazar", Map.of("motivo", motivo)).esperar(200);
    }

    public static Map<String, Object> miReservaHotel(Sesion viajero, Object id) { return viajero.get("/mis-reservas/" + id).esperar(200).data(); }

    public static int totalMisReservas(Sesion viajero) {
        return ((Number) asMap(viajero.get("/mis-reservas").esperar(200).mapa().get("meta")).get("total")).intValue();
    }

    /** Borra la reseña propia del servicio, si existe, para que la prueba sea repetible. */
    public static void borrarMiResena(Sesion viajero, long servicioId) {
        Map<String, Object> d = viajero.get("/reviews/puedo-opinar?servicio_id=" + servicioId).esperar(200).data();
        if (d.get("mi_resena") != null) viajero.delete("/reviews/" + num(asMap(d.get("mi_resena")).get("id")));
    }

    /** Busca un servicio activo por nombre exacto en el catálogo público. */
    public static long idPorNombre(String tipo, String ciudad, String nombre) {
        Sesion anon = new Sesion();
        String ruta = tipo.equals("hotel") ? "/hoteles?per_page=100&ciudad=" : "/tours?per_page=100&destino=";
        return anon.get(ruta + java.net.URLEncoder.encode(ciudad, StandardCharsets.UTF_8)).esperar(200).dataLista().stream()
                .filter(x -> nombre.equals(x.get("nombre"))).map(x -> num(x.get("id"))).findFirst().orElse(-1L);
    }

    // ------------------------------------------------------------------ utilidades
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object o) { return (Map<String, Object>) o; }

    public static long num(Object o) { return ((Number) o).longValue(); }
}
