package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.BuscadorPage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.paginas.PagoDialog;
import com.trouvels360.web.paginas.PagoTourPage;
import com.trouvels360.web.paginas.ResultadosPage;
import com.trouvels360.web.paginas.TourDetallePage;
import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Reservas de tour")
@Feature("Búsqueda, detalle y reserva de tours")
class ToursTest extends BaseTest {

    @Test
    @Tag("TRV-301") @Tag("TC-WEB-015")
    @DisplayName("TC-WEB-015 Filtrar tours por categoría y destino")
    void filtrarTours() {
        new BuscadorPage(d).tours().destinoTour("Cusco").categoria("Aventura").fechas(Datos.fecha(1), null).buscar();
        ResultadosPage r = new ResultadosPage(d);
        r.esperarUrl("/tour/resultados");
        List<String> categorias = r.textos("resultado-categoria");
        assertFalse(categorias.isEmpty());
        categorias.forEach(c -> assertTrue(c.contains("Aventura"), "Categoría distinta: " + c));
        r.textos("resultado-ubicacion").forEach(u -> assertTrue(u.toLowerCase().contains("cusco"), "Fuera de Cusco: " + u));
    }

    @Test
    @Tag("TRV-302") @Tag("TC-WEB-016")
    @DisplayName("TC-WEB-016 Elegir salida y 3 personas: total con IGV en el resumen")
    void totalTourConIgv() {
        Api.Tour t = Api.crearTour(Api.proveedor(), 10, 120, "Cultura");
        new Navegacion(d).entrarComo(Api.viajero(), "/tour");
        TourDetallePage det = new TourDetallePage(d).abrir(t.id()).elegirSalida(t.salidaId()).adultos(3);
        assertTrue(det.subtotal().replace(",", "").contains("360"), "Subtotal en el detalle: " + det.subtotal());
        PagoTourPage pago = det.reservar();
        assertEquals("3", pago.personas());
        assertEquals(Datos.soles(360), pago.subtotal());
        assertEquals(Datos.soles(64.80), pago.impuesto());
        assertEquals(Datos.soles(424.80), pago.total());
    }

    @Test
    @Tag("TRV-303") @Tag("TC-WEB-017")
    @DisplayName("TC-WEB-017 Reserva de tour end-to-end: comprobante CONFIRMADA y cupo descontado")
    void reservaTourCompleta() {
        Api.Sesion prov = Api.proveedor();
        Api.Tour t = Api.crearTour(prov, 10, 80, "Aventura");
        Api.Sesion v = Api.registrarViajero("Sofía");
        new Navegacion(d).entrarComo(v, "/tour");
        PagoTourPage pago = new TourDetallePage(d).abrir(t.id()).elegirSalida(t.salidaId()).adultos(2).reservar();
        PagoDialog dlg = pago.abrirPago();
        dlg.tarjetaDePrueba().confirmar();
        pago.visible("comprobante-tour");
        assertEquals("CONFIRMADA", pago.texto("comprobante-estado"));
        assertEquals(Datos.soles(188.80), pago.texto("comprobante-total"));
        paso("La salida descontó exactamente 2 cupos");
        assertEquals(2L, Api.num(Api.salida(prov, t.id(), t.salidaId()).get("cupo_reservado")));
        paso("Ir a Mis reservas abre la pestaña de tours con la reserva");
        pago.click("btn-ir-mis-reservas");
        pago.esperarUrl("/mis-reservas");
        pago.esperarAlMenos("reserva-card", 1);
        assertEquals("confirmada", pago.todos("reserva-card").get(0).getAttribute("data-estado"));
    }
}
