package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.HotelPantallas;
import com.trouvels360.movil.pantallas.PagoPantalla;
import com.trouvels360.movil.soporte.Api;
import com.trouvels360.movil.soporte.Config;
import com.trouvels360.movil.soporte.Datos;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("App móvil")
@Feature("Reservas de hotel")
class HotelesMovilTest extends BaseTestMovil {

    @Test
    @Tag("HU-V05") @Tag("TC-MOB-009")
    @DisplayName("TC-MOB-009 Búsqueda por ciudad y fechas")
    void busqueda() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        HotelPantallas hp = new HotelPantallas(d);
        hp.buscar(h.ciudad(), Datos.diaLibre(), 2);
        hp.visible("hotel_card_" + h.id());
        paso("Resultado de la ciudad buscada");
        assertEquals(1, hp.cantidad("hotel_card_"));
        assertTrue(hp.texto("hoteles_total").contains("1"));
    }

    @Test
    @Tag("HU-V05") @Tag("TC-MOB-010")
    @DisplayName("TC-MOB-010 Estado vacío frente a error de red con Reintentar")
    void vacioVsError() {
        HotelPantallas hp = new HotelPantallas(d);
        int dia = Datos.diaLibre();
        hp.buscar("CiudadQueNoExiste" + Datos.sufijo(), dia, 1);
        hp.visible("hoteles_vacio");
        paso("Sin resultados: estado vacío (no es un error)");
        assertFalse(hp.existe("btn_reintentar"));
        Api.Hotel hotel = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        d.setConnection(new ConnectionStateBuilder().withAirplaneModeEnabled().build());
        try {
            hp.escribir("buscar_ciudad", hotel.ciudad());
            hp.tocar("btn_buscar_hoteles");
            // La API móvil puede tardar hasta 30 segundos en detectar la pérdida de conexión.
            new WebDriverWait(d, Duration.ofSeconds(45)).until(
                    ExpectedConditions.visibilityOfElementLocated(HotelPantallas.id("estado_error_texto")));
            paso("Sin conexión: error con botón Reintentar");
            assertTrue(hp.existe("btn_reintentar"));
        } finally {
            d.setConnection(new ConnectionStateBuilder().withAirplaneModeDisabled().withWiFiEnabled().withDataEnabled().build());
        }
        hp.espera.until(x -> !d.getConnection().isAirplaneModeEnabled());
        hp.tocar("btn_reintentar");
        hp.visible("hotel_card_" + hotel.id());
        paso("Con la conexión restaurada aparece el hotel al reintentar");
    }

    @Test
    @Tag("HU-V06") @Tag("TC-MOB-011")
    @DisplayName("TC-MOB-011 Selección de dos tipos y total con IGV")
    void totalConIgv() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150, "Suite", 2, 200);
        cuenta().iniciarSesion(Config.get("viajero.email"), Config.get("viajero.password"));
        HotelPantallas hp = new HotelPantallas(d);
        hp.abrirHotel(h.id(), h.ciudad(), Datos.diaLibre(), 2);
        hp.sumar(h.hab("Doble"), 1);
        hp.sumar(h.hab("Suite"), 1);
        hp.reservarSinItinerario();
        paso("Resumen: 700 + IGV 126 = 826");
        assertEquals(Datos.digitos(700), Datos.digitos(hp.texto("resumen_subtotal")));
        assertEquals(Datos.digitos(126), Datos.digitos(hp.texto("resumen_impuesto")));
        assertEquals(Datos.digitos(826), Datos.digitos(hp.texto("resumen_total")));
    }

    @Test
    @Tag("HU-V06") @Tag("TC-MOB-012")
    @DisplayName("TC-MOB-012 El botón + se deshabilita al llegar a las unidades disponibles")
    void topeDeUnidades() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 2, 150);
        HotelPantallas hp = new HotelPantallas(d);
        hp.abrirHotel(h.id(), h.ciudad(), Datos.diaLibre(), 1);
        long hab = h.hab("Doble");
        hp.sumar(hab, 2);
        paso("2 de 2 unidades elegidas");
        assertFalse(hp.habilitado("habitacion_mas_" + hab));
        assertTrue(hp.habilitado("habitacion_menos_" + hab));
    }

    @Test
    @Tag("HU-V07") @Tag("TC-MOB-013")
    @DisplayName("TC-MOB-013 Flujo completo de reserva de hotel: comprobante PENDIENTE")
    void flujoCompleto() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Marta");
        cuenta().iniciarSesion(v.email(), "Clave123");
        HotelPantallas hp = new HotelPantallas(d);
        hp.abrirHotel(h.id(), h.ciudad(), Datos.diaLibre(), 1);
        hp.sumar(h.hab("Doble"), 1);
        hp.reservarSinItinerario();
        PagoPantalla pago = hp.pagar();
        pago.tarjetaDePrueba();
        paso("Datos de la tarjeta de prueba");
        pago.confirmarYEsperar();
        hp.visible("comprobante_hotel");
        paso("Comprobante");
        assertEquals("PENDIENTE", hp.texto("comprobante_estado").toUpperCase());
        String codigo = hp.texto("comprobante_codigo");
        Map<String, Object> r = v.get("/mis-reservas").esperar(200).dataLista().get(0);
        assertEquals(r.get("solicitud_codigo"), codigo);
        assertEquals(Datos.digitos(177), Datos.digitos(hp.texto("comprobante_total")));
    }

    @Test
    @Tag("HU-V07") @Tag("TC-MOB-014")
    @DisplayName("TC-MOB-014 El formulario de pago rechaza una tarjeta vencida")
    void tarjetaVencida() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Raquel");
        cuenta().iniciarSesion(v.email(), "Clave123");
        HotelPantallas hp = new HotelPantallas(d);
        hp.abrirHotel(h.id(), h.ciudad(), Datos.diaLibre(), 1);
        hp.sumar(h.hab("Doble"), 1);
        hp.reservarSinItinerario();
        PagoPantalla pago = hp.pagar();
        pago.llenar("QA", "4242424242424242", "01/20", "123").confirmar();
        pago.visible("pago_vencimiento_error");
        paso("Error bajo el vencimiento");
        assertTrue(pago.existe("dialogo_pago"));
        pago.tocar("pago_cancelar");
        assertEquals(0, Api.totalMisReservas(v));
    }
}
