package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.MisReservasPage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.paginas.ReservasRecibidasPage;
import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Reservas de hotel")
@Feature("Aprobación por el proveedor")
class AprobacionTest extends BaseTest {

    @Test
    @Tag("HU-V12") @Tag("HU-P13") @Tag("TC-WEB-013")
    @DisplayName("TC-WEB-013 El proveedor acepta en su bandeja y el viajero ve CONFIRMADA (dos navegadores)")
    void aceptarDosNavegadores() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Carla");
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 2, 1);
        String codigo = (String) r.get("codigo");

        paso("Navegador del viajero: la reserva aparece PENDIENTE");
        WebDriver viajero = nuevoNavegador();
        new Navegacion(viajero).entrarComo(v, "/mis-reservas");
        MisReservasPage mis = new MisReservasPage(viajero).abrir();
        assertEquals("pendiente", mis.estado(codigo));
        assertTrue(mis.dentro(mis.tarjeta(codigo), "nota-pendiente").isDisplayed());

        paso("Navegador del proveedor: filtra pendientes del hotel y acepta");
        new Navegacion(d).entrarComo(prov, "/proveedor");
        ReservasRecibidasPage rr = new ReservasRecibidasPage(d).abrir("servicio_id=" + h.id() + "&estado=pendiente");
        assertEquals("pendiente", rr.tarjeta(r.get("id")).getAttribute("data-estado"));
        rr.aceptar(r.get("id"));

        paso("El viajero recarga y la ve CONFIRMADA");
        mis.abrir();
        assertEquals("confirmada", mis.estado(codigo));
        assertEquals("CONFIRMADA", mis.dentro(mis.tarjeta(codigo), "reserva-estado").getText().trim().toUpperCase());
    }

    @Test
    @Tag("HU-V12") @Tag("HU-P14") @Tag("TC-WEB-014")
    @DisplayName("TC-WEB-014 El proveedor rechaza con motivo (10–300 caracteres) y el viajero ve RECHAZADA")
    void rechazarConMotivo() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Diego");
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1);

        new Navegacion(d).entrarComo(prov, "/proveedor");
        ReservasRecibidasPage rr = new ReservasRecibidasPage(d).abrir("servicio_id=" + h.id());
        rr.abrirRechazo(r.get("id"));
        paso("Motivo demasiado corto: error y el diálogo sigue abierto");
        rr.escribirMotivo("corto");
        assertFalse(rr.texto("rechazo-error").isEmpty());
        assertTrue(rr.existe("modal-rechazo"));
        paso("Motivo válido: queda rechazada y se muestra el motivo");
        String motivo = "Mantenimiento de la piscina en esas fechas.";
        rr.escribirMotivo(motivo);
        rr.desaparece("modal-rechazo");
        rr.espera.until(x -> "rechazada".equals(rr.tarjeta(r.get("id")).getAttribute("data-estado")));
        assertTrue(rr.dentro(rr.tarjeta(r.get("id")), "rr-motivo").getText().contains(motivo));

        paso("El viajero la ve RECHAZADA con el motivo y sin botón de cancelar");
        new Navegacion(d).cerrarSesion();
        new Navegacion(d).entrarComo(v, "/mis-reservas");
        MisReservasPage mis = new MisReservasPage(d).abrir();
        String codigo = (String) r.get("codigo");
        assertEquals("rechazada", mis.estado(codigo));
        assertTrue(mis.dentro(mis.tarjeta(codigo), "motivo-rechazo").getText().contains(motivo));
        assertTrue(mis.tarjeta(codigo).findElements(com.trouvels360.web.paginas.BasePage.tid("btn-cancelar-reserva")).isEmpty());
    }
}
