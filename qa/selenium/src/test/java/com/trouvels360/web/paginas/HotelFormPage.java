package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Formulario de publicar (/proveedor/crear-hotel) y editar (/proveedor/editar-hotel/:id) hotel. */
public class HotelFormPage extends BasePage {
    public HotelFormPage(WebDriver d) { super(d); }

    public HotelFormPage crear() { irA("/proveedor/crear-hotel"); visible("hotel-nombre"); return this; }

    public HotelFormPage editar(long id) {
        irA("/proveedor/editar-hotel/" + id);
        espera.until(x -> !visible("hotel-nombre").getAttribute("value").isEmpty());
        return this;
    }

    public HotelFormPage datos(String nombre, String ciudad, String direccion, int estrellas) {
        escribir("hotel-nombre", nombre);
        escribir("hotel-descripcion", "Hotel publicado desde la suite Selenium con vista a la plaza.");
        escribir("hotel-direccion", direccion);
        escribir("hotel-ciudad", ciudad);
        escribir("hotel-pais", "Perú");
        escribir("hotel-estrellas", String.valueOf(estrellas));
        escribir("hotel-imagen_url", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800");
        return this;
    }

    public int tiposDeHabitacion() { return todos("hab-nombre").size(); }

    /** Llena el tipo de habitación en la posición i (0 = primero). */
    public HotelFormPage habitacion(int i, String nombre, int adultos, double precio, int unidades) {
        esperarAlMenos("hab-nombre", i + 1);
        escribir(todos("hab-nombre").get(i), nombre);
        List<WebElement> desc = todos("hab-descripcion");
        if (desc.size() > i) escribir(desc.get(i), "Habitación " + nombre + " con baño privado.");
        escribir(todos("hab-capacidad_adultos").get(i), String.valueOf(adultos));
        escribir(todos("hab-capacidad_ninos").get(i), "0");
        escribir(todos("hab-precio_por_noche").get(i), String.valueOf((int) precio));
        escribir(todos("hab-cantidad").get(i), String.valueOf(unidades));
        return this;
    }

    public HotelFormPage precio(int i, double precio) { escribir(todos("hab-precio_por_noche").get(i), String.valueOf((int) precio)); return this; }

    public HotelFormPage agregarTipo() {
        int antes = tiposDeHabitacion();
        click("btn-agregar-habitacion");
        esperarCantidad("hab-nombre", antes + 1);
        return this;
    }

    public void publicar() { click("btn-publicar"); }

    public void guardar() { click("btn-guardar-hotel"); }

    public String ok() { return texto("form-ok"); }
}
