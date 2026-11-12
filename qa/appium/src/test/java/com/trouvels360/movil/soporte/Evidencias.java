package com.trouvels360.movil.soporte;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extensión JUnit 5: toma la captura final del dispositivo ANTES de cerrar la sesión de Appium (AfterTestExecution corre
 * antes que @AfterEach), la adjunta a Allure y registra la fila (con las capturas por paso) en el reporte HTML propio.
 */
public class Evidencias implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestWatcher {

    /** Lo implementa la clase base: sesiones abiertas y capturas tomadas en cada paso. */
    public interface ConNavegadores {
        List<WebDriver> navegadores();
        default List<String> capturasDePasos() { return List.of(); }
    }

    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(Evidencias.class);

    @Override
    public void beforeTestExecution(ExtensionContext ctx) {
        ctx.getStore(NS).put("inicio", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext ctx) {
        List<String> capturas = new ArrayList<>();
        if (ctx.getRequiredTestInstance() instanceof ConNavegadores cn0) capturas.addAll(cn0.capturasDePasos());
        boolean fallo = ctx.getExecutionException().isPresent();
        Object inst = ctx.getRequiredTestInstance();
        if (inst instanceof ConNavegadores cn) {
            int i = 1;
            for (WebDriver d : cn.navegadores()) {
                try {
                    byte[] png = ((TakesScreenshot) d).getScreenshotAs(OutputType.BYTES);
                    String nombre = caso(ctx) + "-" + (fallo ? "FALLA" : "final") + "-" + (i++) + ".png";
                    Files.write(ReporteHtml.carpeta().resolve("capturas").resolve(nombre), png);
                    capturas.add("capturas/" + nombre);
                    Allure.addAttachment((fallo ? "Captura al fallar" : "Captura final") + " (sesión " + (i - 1) + ")", "image/png",
                            new ByteArrayInputStream(png), "png");
                    if (fallo) Allure.addAttachment("URL al fallar", d.getCurrentUrl());
                } catch (Exception e) {
                    System.err.println("No se pudo tomar la captura: " + e.getMessage());
                }
            }
        }
        ctx.getStore(NS).put("capturas", capturas);
    }

    @Override
    public void testSuccessful(ExtensionContext ctx) { registrar(ctx, "OK", null); }

    @Override
    public void testFailed(ExtensionContext ctx, Throwable causa) { registrar(ctx, "FALLA", causa); }

    @Override
    public void testAborted(ExtensionContext ctx, Throwable causa) { registrar(ctx, "OMITIDA", causa); }

    @Override
    public void testDisabled(ExtensionContext ctx, Optional<String> motivo) { registrar(ctx, "OMITIDA", null); }

    @SuppressWarnings("unchecked")
    private void registrar(ExtensionContext ctx, String estado, Throwable causa) {
        Long inicio = ctx.getStore(NS).get("inicio", Long.class);
        List<String> capturas = ctx.getStore(NS).get("capturas", List.class);
        String hu = ctx.getTags().stream().filter(t -> t.startsWith("TRV-")).findFirst().orElse("");
        String error = causa == null ? null : causa.getClass().getSimpleName() + ": " + String.valueOf(causa.getMessage()).lines().limit(8)
                .reduce((a, b) -> a + "\n" + b).orElse("");
        ReporteHtml.agregar(new ReporteHtml.Fila(caso(ctx), hu, ctx.getRequiredTestClass().getSimpleName(), ctx.getDisplayName(), estado,
                inicio == null ? 0 : System.currentTimeMillis() - inicio, error, capturas == null ? List.of() : capturas));
    }

    private static String caso(ExtensionContext ctx) {
        return ctx.getTags().stream().filter(t -> t.startsWith("TC-")).findFirst().orElse(ctx.getRequiredTestMethod().getName());
    }
}
