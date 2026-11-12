package com.trouvels360.web.soporte;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/** Datos únicos por ejecución: así las pruebas no dependen del orden ni de corridas anteriores. */
public final class Datos {
    private static final AtomicLong SEC = new AtomicLong(System.currentTimeMillis() % 1_000_000_000L);

    private Datos() { }

    public static String sufijo() { return Long.toString(SEC.incrementAndGet(), 36).toUpperCase(); }

    public static String correo(String prefijo) { return prefijo + "." + sufijo().toLowerCase() + "@trouvels360.test"; }

    public static String ciudad() { return "WEB-" + sufijo(); }

    /** RUC válido de 11 dígitos que empieza en 20. */
    public static String ruc() { return "20" + String.format("%09d", ThreadLocalRandom.current().nextLong(1_000_000_000L)); }

    public static String celular() { return "9" + String.format("%08d", ThreadLocalRandom.current().nextLong(100_000_000L)); }

    public static String fecha(int dias) { return LocalDate.now().plusDays(dias).toString(); }

    /** Día lejano y aleatorio: evita choques de disponibilidad con otras pruebas. */
    public static int diaLibre() { return ThreadLocalRandom.current().nextInt(400, 3400); }

    /** "S/ 1,234.50" como lo muestra el pipe currency de Angular. */
    public static String soles(double v) { return String.format(java.util.Locale.US, "S/ %,.2f", v); }
}
