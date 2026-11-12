package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.BuscadorPage;
import com.trouvels360.web.paginas.HotelDetallePage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.paginas.PagoDialog;
import com.trouvels360.web.paginas.PagoHotelPage;
import com.trouvels360.web.paginas.ResultadosPage;
import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Reservas de hotel")
@Feature("Búsqueda, detalle, pago simulado")
class HotelesTest extends BaseTest {

    @Test
    @Tag("HU-V05") @Tag("TC-WEB-008")
    @DisplayName("TC-WEB-008 Búsqueda en Cusco: todas las tarjetas son de Cusco y muestran precio")
    void busquedaCusco() {
        int dia = Datos.diaLibre();
        new BuscadorPage(d).hoteles().destinoHotel("Cusco").fechas(Datos.fecha(dia), Datos.fecha(dia + 2)).buscar();
        ResultadosPage r = new ResultadosPage(d);
        r.esperarUrl("/hoteles/resultados");
        assertTrue(d.getCurrentUrl().contains("ciudad=Cusco"));
        List<String> ubicaciones = r.textos("resultado-ubicacion");
        assertFalse(ubicaciones.isEmpty());
        ubicaciones.forEach(u -> assertTrue(u.toLowerCase().contains("cusco"), "Resultado fuera de Cusco: " + u));
        r.textos("resultado-precio").forEach(p -> assertTrue(p.matches("PEN [\\d,]+"), "Precio con formato: " + p));
        paso("Abrir el primer resultado conserva las fechas de la búsqueda");
        r.verDetalle(0);
        HotelDetallePage det = new HotelDetallePage(d);
        det.visible("hotel-detalle");
        assertTrue(d.getCurrentUrl().contains("checkIn=" + Datos.fecha(dia)));
    }

    @Test
    @Tag("HU-V05") @Tag("TC-WEB-009")
    @DisplayName("TC-WEB-009 El buscador no admite fechas pasadas")
    void fechasPasadas() {
        BuscadorPage b = new BuscadorPage(d).hoteles();
        paso("El selector de fecha de entrada no ofrece días anteriores a hoy");
        assertEquals(LocalDate.now().toString(), b.minimoCheckIn());
        paso("Sin fechas no se puede buscar");
        assertFalse(b.habilitado("btn-buscar"));
        paso("Una URL con fecha pasada muestra el mensaje de la API y no lista hoteles");
        new Navegacion(d).ir("/hoteles/resultados?ciudad=Cusco&checkIn=" + Datos.fecha(-3) + "&checkOut=" + Datos.fecha(2));
        ResultadosPage r = new ResultadosPage(d);
        assertTrue(r.error().contains("anterior a hoy"), "Mensaje: " + r.error());
        assertTrue(r.todos("resultado-card").isEmpty());
    }

    @Test
    @Tag("HU-V06") @Tag("TC-WEB-010")
    @DisplayName("TC-WEB-010 Elegir 2 tipos de habitación y verificar el resumen con IGV 18 %")
    void resumenConIgv() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150, "Suite", 2, 200);
        new Navegacion(d).entrarComo(Api.viajero(), "/hoteles");
        HotelDetallePage det = new HotelDetallePage(d).abrir(h.id(), Datos.diaLibre(), 2);
        assertEquals(3, det.unidades("Doble"));
        det.sumar("Doble", 1).sumar("Suite", 1);
        PagoHotelPage pago = det.reservarSinItinerario();
        paso("2 noches × (150 + 200) = 700; IGV 126; total 826");
        assertEquals(2, pago.lineas());
        assertEquals(Datos.soles(700), pago.subtotal());
        assertEquals(Datos.soles(126), pago.impuesto());
        assertEquals(Datos.soles(826), pago.total());
    }

    @Test
    @Tag("HU-V07") @Tag("TC-WEB-011")
    @DisplayName("TC-WEB-011 Flujo completo búsqueda → selección → pago → comprobante PENDIENTE")
    void flujoCompletoHotel() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Elena");
        new Navegacion(d).entrarComo(v, "/hoteles");
        int dia = Datos.diaLibre();
        paso("Buscar la ciudad del hotel");
        new BuscadorPage(d).hoteles().destinoHotel(h.ciudad()).fechas(Datos.fecha(dia), Datos.fecha(dia + 1)).buscar();
        ResultadosPage r = new ResultadosPage(d);
        assertEquals(List.of(h.nombre()), r.textos("resultado-nombre"));
        r.verDetalle(0);
        paso("Elegir 1 habitación y pagar con la tarjeta de prueba");
        PagoHotelPage pago = new HotelDetallePage(d).sumar("Doble", 1).reservarSinItinerario();
        assertEquals(Datos.soles(177), pago.total());
        PagoDialog dlg = pago.abrirPago();
        assertEquals(Datos.soles(177), dlg.total());
        dlg.tarjetaDePrueba().confirmar();
        paso("Comprobante: solicitud PENDIENTE con código y total pagado");
        pago.comprobante();
        assertEquals("PENDIENTE", pago.texto("comprobante-estado"));
        String codigo = pago.texto("comprobante-codigo");
        assertTrue(codigo.startsWith("SOL-"), codigo);
        assertEquals(Datos.soles(177), pago.texto("comprobante-total"));
        paso("La API registra la reserva pendiente del viajero");
        Map<String, Object> reserva = v.get("/mis-reservas").esperar(200).dataLista().get(0);
        assertEquals("pendiente", reserva.get("estado"));
        assertEquals(codigo, reserva.get("solicitud_codigo"));
    }

    @Test
    @Tag("HU-V07") @Tag("TC-WEB-012")
    @DisplayName("TC-WEB-012 Validaciones del formulario de pago; cancelar no crea la reserva")
    void validacionesPago() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Pablo");
        new Navegacion(d).entrarComo(v, "/hoteles");
        PagoHotelPage pago = new HotelDetallePage(d).abrir(h.id(), Datos.diaLibre(), 1).sumar("Doble", 1).reservarSinItinerario();
        PagoDialog dlg = pago.abrirPago();
        paso("Todo vacío: un error por campo");
        dlg.confirmar();
        for (String campo : List.of("titular", "numero", "vencimiento", "cvc")) assertFalse(dlg.error(campo).isEmpty(), "Falta error en " + campo);
        paso("Número que no pasa Luhn, tarjeta vencida y CVC corto");
        dlg.llenar("QA", "4242 4242 4242 4241", "01/20", "12").confirmar();
        assertTrue(dlg.existe("pago-error-numero"));
        assertTrue(dlg.existe("pago-error-vencimiento"));
        assertTrue(dlg.existe("pago-error-cvc"));
        assertFalse(dlg.existe("pago-error-titular"));
        paso("Cancelar cierra el diálogo y no se crea ninguna reserva");
        dlg.cancelar();
        assertFalse(pago.existe("comprobante-hotel"));
        assertEquals(0, Api.totalMisReservas(v));
    }
}
