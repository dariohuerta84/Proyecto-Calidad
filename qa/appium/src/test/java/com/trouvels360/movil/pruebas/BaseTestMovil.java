package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.CuentaPantallas;
import com.trouvels360.movil.soporte.Evidencias;
import com.trouvels360.movil.soporte.FabricaDriver;
import com.trouvels360.movil.soporte.ReporteHtml;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Una sesión de Appium por prueba con la app limpia (noReset=false). Cada paso() deja una captura en Allure y en el
 * reporte HTML propio; la extensión Evidencias agrega la captura final (o la del fallo).
 */
@ExtendWith(Evidencias.class)
public abstract class BaseTestMovil implements Evidencias.ConNavegadores {
    protected AndroidDriver d;
    private final List<String> capturas = new ArrayList<>();
    private int numeroPaso;

    @BeforeEach
    void iniciarApp() {
        d = FabricaDriver.crear();
        capturas.clear();
        numeroPaso = 0;
    }

    @AfterEach
    void cerrarApp() {
        if (d != null) {
            try { d.quit(); } catch (Exception ignored) { }
        }
    }

    @Override
    public List<WebDriver> navegadores() { return d == null ? List.of() : List.of(d); }

    @Override
    public List<String> capturasDePasos() { return capturas; }

    /** Registra el paso en Allure y guarda una captura de pantalla del dispositivo. */
    protected void paso(String descripcion) {
        Allure.step(descripcion);
        try {
            byte[] png = d.getScreenshotAs(OutputType.BYTES);
            String nombre = getClass().getSimpleName() + "-" + System.nanoTime() + "-paso" + (++numeroPaso) + ".png";
            Files.write(ReporteHtml.carpeta().resolve("capturas").resolve(nombre), png);
            capturas.add("capturas/" + nombre);
            Allure.addAttachment("Paso " + numeroPaso + ": " + descripcion, "image/png", new ByteArrayInputStream(png), "png");
        } catch (Exception e) {
            System.err.println("No se pudo capturar el paso: " + e.getMessage());
        }
    }

    protected CuentaPantallas cuenta() { return new CuentaPantallas(d); }
}
