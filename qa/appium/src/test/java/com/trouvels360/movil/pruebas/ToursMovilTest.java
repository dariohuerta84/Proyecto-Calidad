package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.PagoPantalla;
import com.trouvels360.movil.pantallas.TourPantallas;
import com.trouvels360.movil.soporte.Api;
import com.trouvels360.movil.soporte.Config;
import com.trouvels360.movil.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("App móvil")
@Feature("Tours")
class ToursMovilTest extends BaseTestMovil {

    @Test
    @Tag("TRV-301") @Tag("TC-MOB-017")
    @DisplayName("TC-MOB-017 Chips de categoría y búsqueda por destino")
    void categoriaYDestino() {
        Api.Sesion prov = Api.proveedor();
        String ciudad = Datos.ciudad();
        Api.Tour aventura = Api.crearTour(prov, 5, 90, "Aventura", ciudad, Datos.fecha(Datos.diaLibre()));
        Api.Tour cultura = Api.crearTour(prov, 5, 70, "Cultura", ciudad, Datos.fecha(Datos.diaLibre()));
        TourPantallas t = new TourPantallas(d);
        t.buscar(ciudad, null);
        t.visible("tour_card_" + aventura.id());
        assertTrue(t.existe("tour_card_" + cultura.id()));
        paso("Todas las categorías: 2 tours de la ciudad");
        t.tocar("categoria_Aventura");
        t.tocar("btn_buscar_tours");
        t.desaparece("tour_card_" + cultura.id());
        paso("Solo Aventura");
        assertTrue(t.existe("tour_card_" + aventura.id()));
        List<WebElement> cards = d.findElements(TourPantallas.prefijo("tour_card_"));
        assertEquals(1, cards.size());
    }

    @Test
    @Tag("TRV-302") @Tag("TC-MOB-018")
    @DisplayName("TC-MOB-018 Elegir salida y personas; el + se deshabilita al tope de cupos")
    void personasYCupo() {
        Api.Tour tour = Api.crearTour(Api.proveedor(), 3, 120, "Cultura");
        TourPantallas t = new TourPantallas(d);
        t.abrirTour(tour.id(), tour.ciudad());
        t.elegirSalida(tour.salidaId());
        t.stepper("tour_personas", 3);
        paso("3 personas = cupo total");
        assertFalse(t.habilitado("tour_personas_mas"));
        assertEquals(Datos.digitos(360), Datos.digitos(t.texto("tour_subtotal")));
    }

    @Test
    @Tag("TRV-303") @Tag("TC-MOB-019")
    @DisplayName("TC-MOB-019 Reserva de tour end-to-end en la app")
    void reservaTour() {
        Api.Sesion prov = Api.proveedor();
        Api.Tour tour = Api.crearTour(prov, 10, 80, "Aventura");
        Api.Sesion v = Api.registrarViajero("Bruno");
        cuenta().iniciarSesion(v.email(), "Clave123");
        TourPantallas t = new TourPantallas(d);
        t.abrirTour(tour.id(), tour.ciudad());
        t.elegirSalida(tour.salidaId());
        t.stepper("tour_personas", 2);
        t.reservar();
        assertEquals(Datos.digitos(188.80), Datos.digitos(t.texto("resumen_total")));
        PagoPantalla pago = t.pagar();
        pago.tarjetaDePrueba().confirmarYEsperar();
        t.visible("comprobante_tour");
        paso("Comprobante CONFIRMADA");
        assertEquals("CONFIRMADA", t.texto("comprobante_estado").toUpperCase());
        assertEquals(2L, Api.num(Api.salida(prov, tour.id(), tour.salidaId()).get("cupo_reservado")));
    }
}
