package com.trouvels360.web.paginas;

import com.trouvels360.web.soporte.Datos;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HotelDetallePage extends BasePage {
    public HotelDetallePage(WebDriver d) { super(d); }

    public HotelDetallePage abrir(long hotelId, int dia, int noches) {
        irA("/hoteles/detalle/" + hotelId + "?checkIn=" + Datos.fecha(dia) + "&checkOut=" + Datos.fecha(dia + noches) + "&adultos=1&habitaciones=1");
        visible("hotel-detalle");
        esperarAlMenos("habitacion-card", 1);
        return this;
    }

    public WebElement habitacion(String nombre) {
        return espera.until(x -> todos("habitacion-card").stream()
                .filter(c -> dentro(c, "habitacion-nombre").getText().trim().equals(nombre)).findFirst().orElse(null));
    }

    public HotelDetallePage sumar(String nombre, int veces) {
        for (int i = 0; i < veces; i++) {
            WebElement card = habitacion(nombre);
            String antes = dentro(card, "habitacion-seleccionadas").getText();
            click(dentro(card, "habitacion-mas"));
            espera.until(x -> !dentro(habitacion(nombre), "habitacion-seleccionadas").getText().equals(antes));
        }
        return this;
    }

    public int unidades(String nombre) {
        return Integer.parseInt(dentro(habitacion(nombre), "habitacion-unidades").getText().replaceAll("\\D+", ""));
    }

    /** Reservar → pregunta de itinerario → "No, ir al pago". */
    public PagoHotelPage reservarSinItinerario() {
        click("btn-reservar-hotel");
        click("btn-sin-itinerario");
        esperarUrl("/hoteles/pagos");
        return new PagoHotelPage(d).listo();
    }

    public void pedirItinerario(String interes) {
        click("btn-reservar-hotel");
        seleccionar("itinerario-interes", interes);
        click("btn-generar-itinerario");
        visible("modal-itinerario");
    }

    public ResenasSeccion resenas() { return new ResenasSeccion(d); }

    public String fechasMostradas() { return texto("fechas-actuales"); }

    public By card() { return tid("habitacion-card"); }
}
