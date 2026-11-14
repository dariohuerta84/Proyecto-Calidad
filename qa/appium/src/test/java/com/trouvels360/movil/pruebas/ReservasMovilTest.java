package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.ReservasPantalla;
import com.trouvels360.movil.soporte.Api;
import com.trouvels360.movil.soporte.Datos;
import com.trouvels360.movil.soporte.Gestos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("App móvil")
@Feature("Mis reservas")
class ReservasMovilTest extends BaseTestMovil {

    @Test
    @Tag("HU-V11") @Tag("TC-MOB-020")
    @DisplayName("TC-MOB-020 Mis reservas en la app coincide con la web/API (mismo código y total)")
    void coincideConApi() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Celia");
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 2, 1);
        Map<String, Object> api = Api.miReservaHotel(v, r.get("id"));
        cuenta().iniciarSesion(v.email(), "Clave123");
        ReservasPantalla m = new ReservasPantalla(d).abrir();
        String codigo = (String) r.get("codigo");
        var tarjeta = m.visible("reserva_card_" + codigo);
        paso("Tarjeta con el mismo código");
        assertEquals(codigo, tarjeta.findElement(ReservasPantalla.id("reserva_codigo")).getText());
        String total = tarjeta.findElement(ReservasPantalla.id("reserva_total")).getText();
        assertEquals(Datos.digitos(((Number) api.get("total")).doubleValue()), Datos.digitos(total));
        assertTrue(m.estado(codigo).contains("pendiente"));
    }

    @Test
    @Tag("HU-V11") @Tag("TC-MOB-021")
    @DisplayName("TC-MOB-021 Paginación de reservas con desplazamiento")
    void paginacion() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 20, 150);
        Api.Sesion v = Api.registrarViajero("Dora");
        int dia = Datos.diaLibre();
        List<String> codigos = new ArrayList<>();
        for (int i = 0; i < 11; i++) codigos.add((String) Api.reservarHotel(v, h.hab("Doble"), dia, 1, 1).get("codigo"));
        cuenta().iniciarSesion(v.email(), "Clave123");
        ReservasPantalla m = new ReservasPantalla(d).abrir();
        m.visible("reserva_card_" + codigos.get(10)); // la más reciente va primero
        for (int desplazamiento = 0; desplazamiento < 15 && !m.existe("reservas_pagina_siguiente"); desplazamiento++) {
            Gestos.deslizarArriba(d);
        }
        paso("Fin de la página 1");
        m.tocar("reservas_pagina_siguiente");
        m.visible("reserva_card_" + codigos.get(0));
        paso("Página 2 con la reserva más antigua");
        assertTrue(m.texto("reservas_pagina_actual").contains("2"));
        assertEquals(1, m.cantidad("reserva_card_"));
    }

    @Test
    @Tag("HU-V13") @Tag("TC-MOB-022")
    @DisplayName("TC-MOB-022 Cancelar desde la app y ver el estado CANCELADA")
    void cancelar() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 1, 150);
        Api.Sesion v = Api.registrarViajero("Elsa");
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 2, 1);
        String codigo = (String) r.get("codigo");
        cuenta().iniciarSesion(v.email(), "Clave123");
        ReservasPantalla m = new ReservasPantalla(d).abrir();
        m.tocar("btn_cancelar_" + codigo);
        m.visible("dialogo_cancelar_reserva");
        paso("Confirmación de cancelación");
        m.confirmar("dialogo_cancelar_reserva");
        m.espera.until(x -> m.estado(codigo).contains("cancelada"));
        paso("Estado CANCELADA y sin botón Cancelar");
        assertTrue(!m.existe("btn_cancelar_" + codigo));
        assertEquals("cancelada", Api.miReservaHotel(v, r.get("id")).get("estado"));
    }
}
