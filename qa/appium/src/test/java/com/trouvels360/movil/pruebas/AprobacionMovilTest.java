package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.CuentaPantallas;
import com.trouvels360.movil.pantallas.ProveedorPantallas;
import com.trouvels360.movil.pantallas.ReservasPantalla;
import com.trouvels360.movil.soporte.Api;
import com.trouvels360.movil.soporte.Config;
import com.trouvels360.movil.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("App móvil")
@Feature("Aprobación de solicitudes por el proveedor")
class AprobacionMovilTest extends BaseTestMovil {

    @Test
    @Tag("TRV-204") @Tag("TC-MOB-015")
    @DisplayName("TC-MOB-015 El proveedor acepta una solicitud desde la app y el viajero la ve CONFIRMADA")
    void aceptar() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Pía");
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 2, 1);
        String codigo = (String) r.get("codigo");

        CuentaPantallas c = cuenta();
        c.iniciarSesion(Config.get("proveedor.email"), Config.get("proveedor.password"));
        ProveedorPantallas p = new ProveedorPantallas(d);
        p.recibidas();
        p.tocar("rr_estado_pendiente");
        p.visible("rr_card_" + codigo);
        paso("Solicitud pendiente en la bandeja");
        p.tocar("btn_aceptar_" + codigo);
        p.visible("rr_mensaje_ok");
        paso("Aceptada");
        assertEquals("confirmada", Api.miReservaHotel(v, r.get("id")).get("estado"));

        c.cerrarSesion();
        c.iniciarSesion(v.email(), "Clave123");
        ReservasPantalla mis = new ReservasPantalla(d).abrir();
        mis.visible("reserva_card_" + codigo);
        paso("El viajero la ve confirmada");
        assertTrue(mis.estado(codigo).contains("confirmada"));
    }

    @Test
    @Tag("TRV-204") @Tag("TC-MOB-016")
    @DisplayName("TC-MOB-016 El proveedor rechaza con motivo desde la app")
    void rechazar() {
        Api.Sesion prov = Api.proveedor();
        Api.Hotel h = Api.crearHotel(prov, "Doble", 3, 150);
        Api.Sesion v = Api.registrarViajero("Gus");
        Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1);
        String codigo = (String) r.get("codigo");

        cuenta().iniciarSesion(Config.get("proveedor.email"), Config.get("proveedor.password"));
        ProveedorPantallas p = new ProveedorPantallas(d);
        p.recibidas();
        p.tocar("btn_rechazar_" + codigo);
        p.visible("dialogo_rechazo");
        p.escribir("rechazo_motivo", "corto");
        p.tocar("rechazo_confirmar");
        p.visible("rechazo_motivo_error");
        paso("Motivo corto: error en el diálogo");
        String motivo = "Cierre por mantenimiento en esas fechas.";
        p.escribir("rechazo_motivo", motivo);
        p.tocar("rechazo_confirmar");
        p.desaparece("dialogo_rechazo");
        p.esperarTexto("rr_motivo_" + codigo, motivo);
        paso("Rechazada con motivo");
        Map<String, Object> api = Api.miReservaHotel(v, r.get("id"));
        assertEquals("rechazada", api.get("estado"));
        assertEquals(motivo, api.get("motivo_rechazo"));
    }
}
