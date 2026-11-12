package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

/** Buscador de la portada de hoteles (/hoteles) y de tours (/tour). */
public class BuscadorPage extends BasePage {
    public BuscadorPage(WebDriver d) { super(d); }

    public BuscadorPage hoteles() { irA("/hoteles"); visible("buscar-destino"); return this; }

    public BuscadorPage tours() { irA("/tour"); visible("buscar-destino-tour"); return this; }

    public BuscadorPage destinoHotel(String ciudad) { escribir("buscar-destino", ciudad); return this; }

    public BuscadorPage destinoTour(String ciudad) { escribir("buscar-destino-tour", ciudad); return this; }

    public BuscadorPage categoria(String categoria) { seleccionar("buscar-categoria", categoria); return this; }

    public BuscadorPage fechas(String checkIn, String checkOut) {
        fijarValor("buscar-checkin", checkIn);
        if (checkOut != null) fijarValor("buscar-checkout", checkOut);
        return this;
    }

    public String minimoCheckIn() { return presente("buscar-checkin").getAttribute("min"); }

    public void buscar() { click("btn-buscar"); }
}
