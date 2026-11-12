package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ReservasRecibidasPage extends BasePage {
    public ReservasRecibidasPage(WebDriver d) { super(d); }

    public ReservasRecibidasPage abrir(String query) {
        irA("/proveedor/reservas" + (query == null ? "" : "?" + query));
        visible("reservas-recibidas");
        espera.until(x -> existe("rr-card") || existe("rr-vacio") || existe("rr-error"));
        return this;
    }

    public List<WebElement> tarjetas() { return todos("rr-card"); }

    public WebElement tarjeta(Object reservaId) { return porAtributo("rr-card", "data-id", reservaId); }

    public void aceptar(Object reservaId) {
        click(dentro(tarjeta(reservaId), "btn-aceptar"));
        visible("rr-mensaje-ok");
        // con el filtro "pendiente" la tarjeta sale de la lista; sin filtro cambia a confirmada
        espera.until(x -> d.findElements(org.openqa.selenium.By.cssSelector("[data-testid='rr-card'][data-id='" + reservaId + "']"))
                .stream().allMatch(t -> "confirmada".equals(t.getAttribute("data-estado"))));
    }

    public void abrirRechazo(Object reservaId) { click(dentro(tarjeta(reservaId), "btn-rechazar")); visible("modal-rechazo"); }

    public void escribirMotivo(String motivo) { escribir("rechazo-motivo", motivo); click("rechazo-confirmar"); }

    /** Cambia un filtro y espera a que la lista se recargue (cambia la URL). */
    public ReservasRecibidasPage filtrarEstado(String estado) {
        click("rr-estado-" + estado);
        esperarUrl("estado=" + estado);
        espera.until(x -> tarjetas().stream().allMatch(t -> estado.equals(t.getAttribute("data-estado"))));
        return this;
    }

    public ReservasRecibidasPage filtrarTipo(String tipo) {
        seleccionar("rr-filtro-tipo", tipo);
        esperarUrl("tipo=" + tipo);
        espera.until(x -> !tarjetas().isEmpty() && tarjetas().stream().allMatch(t -> tipo.equals(t.getAttribute("data-tipo"))));
        return this;
    }
}
