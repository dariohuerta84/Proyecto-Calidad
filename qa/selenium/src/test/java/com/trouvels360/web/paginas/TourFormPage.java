package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

public class TourFormPage extends BasePage {
    public TourFormPage(WebDriver d) { super(d); }

    public TourFormPage crear() { irA("/proveedor/crear-tour"); visible("tour-nombre"); return this; }

    public TourFormPage datos(String nombre, String ciudad, String categoria, double precio, int duracionMin) {
        escribir("tour-nombre", nombre);
        escribir("tour-descripcion", "Recorrido guiado publicado desde la suite Selenium.");
        escribir("tour-direccion", "Plaza principal");
        escribir("tour-ciudad", ciudad);
        escribir("tour-pais", "Perú");
        escribir("tour-precio", String.valueOf((int) precio));
        seleccionar("tour-categoria", categoria);
        escribir("tour-duracion", String.valueOf(duracionMin));
        escribir("tour-imagen_url", "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800");
        if (existe("tour-item-nombre")) escribir(todos("tour-item-nombre").get(0), "Guía local");
        return this;
    }

    public TourFormPage salida(int i, String fecha, String hora, int cupo) {
        esperarAlMenos("tour-fecha", i + 1);
        fijarValor(todos("tour-fecha").get(i), fecha);
        fijarValor(todos("tour-hora").get(i), hora);
        escribir(todos("tour-cupo_total").get(i), String.valueOf(cupo));
        return this;
    }

    public boolean puedePublicar() { return presente("btn-publicar-tour").isEnabled(); }

    public void publicar() { click("btn-publicar-tour"); }

    public String ok() { return texto("form-ok"); }
}
