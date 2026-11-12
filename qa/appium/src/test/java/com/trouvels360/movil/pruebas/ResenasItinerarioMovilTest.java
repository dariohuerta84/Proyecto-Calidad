package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.HotelPantallas;
import com.trouvels360.movil.soporte.Api;
import com.trouvels360.movil.soporte.Config;
import com.trouvels360.movil.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("App móvil")
@Feature("Reseñas e itinerario")
class ResenasItinerarioMovilTest extends BaseTestMovil {

    @Test
    @Tag("HU-V14") @Tag("TC-MOB-023")
    @DisplayName("TC-MOB-023 Publicar una reseña desde la app")
    void publicarResena() {
        long hotel = Api.idPorNombre("hotel", "Cusco", "Hotel QA Concurrencia");
        Api.Sesion v1 = Api.viajeroQa(1); // estadía finalizada en el Hotel QA (QaSeeder)
        Api.borrarMiResena(v1, hotel);
        cuenta().iniciarSesion(v1.email(), Config.get("viajero.qa.password"));
        HotelPantallas hp = new HotelPantallas(d);
        hp.abrirHotel(hotel, "Cusco", 10, 1);
        hp.traer("seccion_resenas");
        hp.tocar("btn_dejar_opinion");
        hp.visible("dialogo_resena");
        hp.tocar("resena_estrella_5");
        String texto = "Desde la app: todo impecable " + Datos.sufijo();
        hp.escribir("resena_comentario", texto);
        paso("Reseña lista para enviar");
        hp.tocar("resena_enviar");
        hp.desaparece("dialogo_resena");
        hp.visible("resenas_aviso");
        paso("Publicada");
        assertTrue(v1.get("/reviews?servicio_id=" + hotel).esperar(200).dataLista().stream().anyMatch(r -> texto.equals(r.get("comentario"))));
        Api.borrarMiResena(v1, hotel);
    }

    @Test
    @Tag("HU-V15") @Tag("TC-MOB-024")
    @DisplayName("TC-MOB-024 Generar el itinerario en la app y continuar con la reserva")
    void itinerario() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        int dia = Datos.diaLibre();
        Api.Tour tour = Api.crearTour(prov, 8, 90, "Aventura", h.ciudad(), Datos.fecha(dia + 1));
        cuenta().iniciarSesion(Config.get("viajero.email"), Config.get("viajero.password"));
        HotelPantallas hp = new HotelPantallas(d);
        hp.abrirHotel(h.id(), h.ciudad(), dia, 3);
        hp.sumar(h.hab("Doble"), 1);
        hp.tocar("btn_reservar_hotel");
        hp.visible("dialogo_itinerario_pregunta");
        hp.tocar("interes_Aventura");
        hp.tocar("btn_generar_itinerario");
        hp.visible("dialogo_itinerario");
        paso("Itinerario sugerido");
        assertTrue(hp.existe("itinerario_ver_tour_" + tour.id()), "Sugiere el tour de la ciudad");
        hp.tocar("btn_itinerario_continuar");
        hp.visible("pantalla_pago_hotel");
        assertEquals(Datos.digitos(531), Datos.digitos(hp.texto("resumen_total")));
    }
}
