package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.HotelDetallePage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.paginas.PagoHotelPage;
import com.trouvels360.web.paginas.ResenasSeccion;
import com.trouvels360.web.soporte.Api;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Experiencia del viajero")
@Feature("Reseñas e itinerario sugerido")
class ResenasItinerarioTest extends BaseTest {

    private static long hotelQa() {
        long id = Api.idPorNombre("hotel", "Cusco", "Hotel QA Concurrencia");
        if (id < 0) throw new IllegalStateException("Falta el QaSeeder: no existe 'Hotel QA Concurrencia'");
        return id;
    }

    @Test
    @Tag("TRV-403") @Tag("TC-WEB-021")
    @DisplayName("TC-WEB-021 Publicar, editar y eliminar una reseña desde el detalle del hotel")
    void ciclodeResena() {
        long hotel = hotelQa();
        Api.Sesion v1 = Api.viajeroQa(1); // tiene una estadía finalizada en el Hotel QA (QaSeeder)
        Api.borrarMiResena(v1, hotel);
        new Navegacion(d).entrarComo(v1, "/hoteles");
        HotelDetallePage det = new HotelDetallePage(d).abrir(hotel, 10, 1);
        ResenasSeccion rs = det.resenas();
        String texto = "Muy buena atención y desayuno variado. " + System.currentTimeMillis();
        paso("Publicar con 4 estrellas");
        rs.publicar(4, texto);
        WebElement card = rs.esperarResena(texto);
        assertEquals("4", card.getAttribute("data-calificacion"));
        assertTrue(rs.existe("reviews-aviso"));
        paso("Editar: 5 estrellas y nuevo texto");
        rs.click(rs.dentro(card, "review-editar"));
        String editado = "Actualizo: excelente en todo. " + System.currentTimeMillis();
        rs.llenar(5, editado);
        assertEquals("5", rs.esperarResena(editado).getAttribute("data-calificacion"));
        paso("Eliminar con confirmación");
        rs.click(rs.dentro(rs.esperarResena(editado), "review-eliminar"));
        rs.visible("modal-eliminar-review");
        rs.click("review-eliminar-si");
        rs.desaparece("modal-eliminar-review");
        rs.espera.until(x -> rs.resenaConTexto(editado).isEmpty());
        assertTrue(rs.visible("btn-dejar-opinion").isDisplayed(), "Puede volver a opinar");
    }

    @Test
    @Tag("TRV-404") @Tag("TC-WEB-022")
    @DisplayName("TC-WEB-022 Generar el itinerario sugerido desde el detalle del hotel y llevarlo al pago")
    void itinerarioSugerido() {
        // Datos propios: hotel y tour de Aventura en la misma ciudad, con salida el segundo día de la estadía
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        int dia = com.trouvels360.web.soporte.Datos.diaLibre();
        Api.Tour t = Api.crearTour(prov, 8, 90, "Aventura", h.ciudad(), com.trouvels360.web.soporte.Datos.fecha(dia + 1));
        new Navegacion(d).entrarComo(Api.viajero(), "/hoteles");
        HotelDetallePage det = new HotelDetallePage(d).abrir(h.id(), dia, 3).sumar("Doble", 1);
        det.pedirItinerario("Aventura");
        paso("El itinerario muestra el resumen y cada tour con su salida y cupos");
        assertFalse(det.texto("itinerario-resumen").isEmpty());
        det.esperarAlMenos("itinerario-salida", 1);
        det.todos("itinerario-salida").forEach(s -> assertTrue(s.getText().contains("cupos"), "Salida sin cupos: " + s.getText()));
        assertTrue(det.texto("modal-itinerario").contains(t.nombre()), "El itinerario sugiere el tour de la ciudad");
        paso("Continuar lleva al pago con el itinerario adjunto");
        det.click("btn-itinerario-continuar");
        det.esperarUrl("/hoteles/pagos");
        PagoHotelPage pago = new PagoHotelPage(d).listo();
        assertTrue(pago.visible("pago-itinerario").isDisplayed());
    }
}
