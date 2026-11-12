package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Salidas y actividades de un tour (/proveedor/tour/:id/programa). */
public class ProgramaTourPage extends BasePage {
    public ProgramaTourPage(WebDriver d) { super(d); }

    public ProgramaTourPage abrir(long tourId) {
        irA("/proveedor/tour/" + tourId + "/programa");
        visible("gestionar-tour");
        espera.until(x -> existe("salida-fila") || existe("sin-salidas"));
        return this;
    }

    // ---------------- salidas
    public List<WebElement> salidas() { return todos("salida-fila"); }

    public void programarSalida(String fecha, String hora, int cupo) {
        int antes = salidas().size();
        fijarValor("salida-fecha", fecha);
        fijarValor("salida-hora", hora);
        escribir("salida-cupo", String.valueOf(cupo));
        click("salida-guardar");
        esperarCantidad("salida-fila", antes + 1);
    }

    public WebElement salida(Object id) { return porAtributo("salida-fila", "data-id", id); }

    public void cerrar(Object id) {
        click(dentro(salida(id), "salida-cerrar"));
        espera.until(x -> "cerrada".equals(salida(id).getAttribute("data-estado")));
    }

    public void eliminar(Object id) {
        int antes = salidas().size();
        click(dentro(salida(id), "salida-eliminar"));
        esperarCantidad("salida-fila", antes - 1);
    }

    // ---------------- actividades
    public List<String> actividades() { return todos("actividad-nombre").stream().map(e -> e.getText().trim()).toList(); }

    public void agregarActividad(String titulo, int duracion) {
        int antes = todos("actividad-item").size();
        escribir("actividad-titulo", titulo);
        escribir("actividad-duracion", String.valueOf(duracion));
        click("actividad-guardar");
        esperarCantidad("actividad-item", antes + 1);
    }

    public WebElement actividad(String titulo) {
        return espera.until(x -> todos("actividad-item").stream()
                .filter(a -> dentro(a, "actividad-nombre").getText().trim().equals(titulo)).findFirst().orElse(null));
    }

    public void subir(String titulo) {
        List<String> antes = actividades();
        click(dentro(actividad(titulo), "actividad-subir"));
        espera.until(x -> !actividades().equals(antes));
    }

    public void eliminarActividad(String titulo) {
        int antes = todos("actividad-item").size();
        click(dentro(actividad(titulo), "actividad-eliminar"));
        esperarCantidad("actividad-item", antes - 1);
    }
}
