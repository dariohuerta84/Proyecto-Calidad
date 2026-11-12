package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.MisReservasPage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Mis reservas")
@Feature("Consulta y cancelación")
class MisReservasTest extends BaseTest {

    @Test
    @Tag("TRV-401") @Tag("TC-WEB-018")
    @DisplayName("TC-WEB-018 Mis reservas: pestañas hotel/tour, filtro por estado y paginación")
    void pestanasFiltrosPaginacion() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 20, 150);
        Api.Tour t = Api.crearTour(prov, 10, 50, "Cultura");
        Api.Sesion v = Api.registrarViajero("Iris");
        int dia = Datos.diaLibre();
        List<Map<String, Object>> creadas = new ArrayList<>();
        for (int i = 0; i < 11; i++) creadas.add(Api.reservarHotel(v, h.hab("Doble"), dia, 1, 1));
        Api.aceptar(prov, creadas.get(0).get("id"));
        Api.reservarTour(v, t.salidaId(), 1);

        new Navegacion(d).entrarComo(v, "/mis-reservas");
        MisReservasPage m = new MisReservasPage(d).abrir();
        paso("Pestaña hoteles: 10 por página y una segunda página con 1");
        assertEquals(10, m.tarjetas().size());
        m.pagina(2);
        m.esperarCantidad("reserva-card", 1);
        paso("Filtro Confirmadas: solo la aceptada");
        m.filtro("confirmada");
        List<WebElement> conf = m.esperarCantidad("reserva-card", 1);
        assertEquals(creadas.get(0).get("codigo"), conf.get(0).getAttribute("data-codigo"));
        m.filtro("pendiente");
        m.esperarCantidad("reserva-card", 10);
        m.tarjetas().forEach(c -> assertEquals("pendiente", c.getAttribute("data-estado")));
        paso("Pestaña tours: la reserva de tour confirmada");
        m.pestanaTours();
        List<WebElement> tours = m.esperarCantidad("reserva-card", 1);
        assertTrue(m.dentro(tours.get(0), "reserva-servicio").getText().contains(t.nombre()));
    }

    @Test
    @Tag("TRV-401") @Tag("TC-WEB-019")
    @DisplayName("TC-WEB-019 Una reserva rechazada muestra el motivo del proveedor")
    void rechazadaConMotivo() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Hugo");
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1);
        String motivo = "El hotel cierra por remodelación esa semana.";
        Api.rechazar(prov, r.get("id"), motivo);
        new Navegacion(d).entrarComo(v, "/mis-reservas");
        MisReservasPage m = new MisReservasPage(d).abrir().filtro("rechazada");
        String codigo = (String) r.get("codigo");
        assertEquals("rechazada", m.estado(codigo));
        assertTrue(m.dentro(m.tarjeta(codigo), "motivo-rechazo").getText().contains(motivo));
    }

    @Test
    @Tag("TRV-402") @Tag("TC-WEB-020")
    @DisplayName("TC-WEB-020 Cancelar desde Mis reservas pide confirmación y libera la habitación")
    void cancelarConConfirmacion() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 1, 150);
        Api.Sesion v = Api.registrarViajero("Nora");
        int dia = Datos.diaLibre();
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), dia, 2, 1);
        String codigo = (String) r.get("codigo");
        new Navegacion(d).entrarComo(v, "/mis-reservas");
        MisReservasPage m = new MisReservasPage(d).abrir();
        paso("'No, mantener' cierra el diálogo sin cambios");
        m.click(m.dentro(m.tarjeta(codigo), "btn-cancelar-reserva"));
        m.visible("modal-cancelar");
        m.click("cancelar-no");
        m.desaparece("modal-cancelar");
        assertEquals("pendiente", m.estado(codigo));
        paso("Confirmar cancela la reserva");
        m.click(m.dentro(m.tarjeta(codigo), "btn-cancelar-reserva"));
        m.click("cancelar-si");
        m.visible("mensaje-ok");
        m.espera.until(x -> "cancelada".equals(m.estado(codigo)));
        paso("La única unidad vuelve a estar disponible para esas fechas");
        Map<String, Object> disp = Api.asMap(new Api.Sesion().get("/hoteles/" + h.id() + "/disponibilidad?check_in=" + Datos.fecha(dia)
                + "&check_out=" + Datos.fecha(dia + 2) + "&adultos=1&habitaciones=1").esperar(200).data());
        assertEquals(1L, Api.num(((List<Map<String, Object>>) disp.get("habitaciones")).get(0).get("unidades_disponibles")));
    }
}
