package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

public class TourDetallePage extends BasePage {
    public TourDetallePage(WebDriver d) { super(d); }

    public TourDetallePage abrir(long tourId) { irA("/tour/detalle/" + tourId); esperarAlMenos("salida-card", 1); return this; }

    public TourDetallePage elegirSalida(long salidaId) {
        click(porAtributo("salida-card", "data-salida-id", salidaId));
        visible("adultos-mas");
        return this;
    }

    public TourDetallePage adultos(int n) {
        while (Integer.parseInt(texto("adultos-cantidad")) < n) {
            String antes = texto("adultos-cantidad");
            click("adultos-mas");
            espera.until(x -> !texto("adultos-cantidad").equals(antes));
        }
        return this;
    }

    public String subtotal() { return texto("tour-subtotal"); }

    public PagoTourPage reservar() {
        click("btn-reservar-tour");
        esperarUrl("/tour/pagos");
        visible("resumen-total");
        return new PagoTourPage(d);
    }
}
