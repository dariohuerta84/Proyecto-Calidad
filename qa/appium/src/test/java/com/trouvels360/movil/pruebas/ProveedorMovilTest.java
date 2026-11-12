package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.ProveedorPantallas;
import com.trouvels360.movil.soporte.Api;
import com.trouvels360.movil.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("App móvil")
@Feature("Proveedor")
class ProveedorMovilTest extends BaseTestMovil {

    /** Proveedor nuevo (sin historial) con sesión iniciada en la app. */
    private ProveedorPantallas entrar(Api.Sesion p) {
        cuenta().iniciarSesion(p.email(), "Clave123");
        ProveedorPantallas pp = new ProveedorPantallas(d);
        pp.visible("pantalla_panel_proveedor");
        return pp;
    }

    @Test
    @Tag("HU-P03") @Tag("HU-P04") @Tag("TC-MOB-025")
    @DisplayName("TC-MOB-025 Panel del proveedor con indicadores")
    void panel() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Hotel h = Api.crearHotel(p, "Doble", 5, 150);
        Api.Sesion v = Api.registrarViajero("Fede");
        Map<String, Object> r1 = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1);
        Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1);
        Api.aceptar(p, r1.get("id"));
        ProveedorPantallas pp = entrar(p);
        pp.visible("indicadores");
        paso("Indicadores y publicación");
        String ind = d.findElement(ProveedorPantallas.id("indicadores")).getText();
        assertTrue(ind.contains("1"), "Indicadores: " + ind);
        assertTrue(pp.existe("publicacion_" + h.id()));
        assertTrue(pp.texto("panel_total").contains("1"));
    }

    @Test
    @Tag("HU-P05") @Tag("TC-MOB-026")
    @DisplayName("TC-MOB-026 Crear hotel desde la app del proveedor")
    void crearHotel() {
        Api.Sesion p = Api.registrarProveedor();
        ProveedorPantallas pp = entrar(p);
        String ciudad = Datos.ciudad();
        String nombre = "Hotel App " + ciudad;
        pp.tocar("btn_crear_hotel");
        pp.datosHotel(nombre, ciudad);
        pp.habitacion("_0", "Matrimonial", 2, 4, 180);
        paso("Formulario completo");
        pp.tocar("btn_guardar_hotel");
        pp.visible("pantalla_panel_proveedor");
        long id = Api.idPorNombre("hotel", ciudad, nombre);
        assertTrue(id > 0, "El hotel debe estar en el catálogo");
        pp.visible("publicacion_" + id);
        paso("Publicado y visible en el panel");
    }

    @Test
    @Tag("HU-P07") @Tag("TC-MOB-027")
    @DisplayName("TC-MOB-027 Agregar un tipo de habitación en la app")
    void agregarTipo() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Hotel h = Api.crearHotel(p, "Doble", 3, 150);
        ProveedorPantallas pp = entrar(p);
        pp.tocar("btn_editar_" + h.id());
        pp.visible("pantalla_form_hotel");
        pp.tocar("btn_nueva_habitacion");
        pp.visible("dialogo_habitacion");
        pp.habitacion("", "Suite", 2, 1, 320);
        paso("Nuevo tipo");
        pp.tocar("hab_guardar");
        pp.desaparece("dialogo_habitacion");
        pp.visible("form_ok");
        List<Map<String, Object>> habs = Api.detalleHotel(p, h.id());
        assertEquals(2, habs.size());
        long suite = habs.stream().filter(x -> "Suite".equals(x.get("nombre"))).map(x -> Api.num(x.get("id"))).findFirst().orElseThrow();
        pp.visible("hab_item_" + suite);
    }

    @Test
    @Tag("HU-P08") @Tag("TC-MOB-028")
    @DisplayName("TC-MOB-028 Crear tour con su salida desde la app")
    void crearTour() {
        Api.Sesion p = Api.registrarProveedor();
        ProveedorPantallas pp = entrar(p);
        String ciudad = Datos.ciudad();
        String nombre = "Tour App " + ciudad;
        pp.tocar("btn_crear_tour");
        pp.datosTour(nombre, ciudad, "Aventura", 75, 150);
        pp.salidaDelFormulario(0, 30, "08:30", 15);
        paso("Formulario de tour con salida");
        pp.tocar("btn_guardar_tour");
        pp.visible("pantalla_panel_proveedor");
        long id = Api.idPorNombre("tour", ciudad, nombre);
        assertTrue(id > 0);
        List<Map<String, Object>> salidas = p.get("/tours/" + id + "/salidas").esperar(200).lista();
        assertEquals(1, salidas.size());
        assertEquals(Datos.fecha(30), salidas.get(0).get("fecha"));
        assertEquals("08:30", salidas.get(0).get("hora"));
    }

    @Test
    @Tag("HU-P10") @Tag("TC-MOB-029")
    @DisplayName("TC-MOB-029 Agregar una actividad al tour desde la app")
    void actividad() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Tour t = Api.crearTour(p, 10, 60, "Cultura");
        ProveedorPantallas pp = entrar(p);
        pp.programa(t.id());
        pp.nuevaActividad("Mirador de San Cristóbal", 40);
        pp.visible("actividad_item_1");
        paso("Actividad agregada con orden 1");
        assertEquals("Mirador de San Cristóbal", pp.texto("actividad_titulo_1"));
        assertEquals(1, p.get("/tours/" + t.id() + "/actividades").esperar(200).lista().size());
    }

    @Test
    @Tag("HU-P11") @Tag("TC-MOB-030")
    @DisplayName("TC-MOB-030 Crear y cerrar una salida desde la app")
    void salida() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Tour t = Api.crearTour(p, 10, 60, "Cultura");
        ProveedorPantallas pp = entrar(p);
        pp.programa(t.id());
        pp.nuevaSalida(40, "16:00", 12);
        long nueva = p.get("/tours/" + t.id() + "/salidas").esperar(200).lista().stream()
                .filter(s -> Datos.fecha(40).equals(s.get("fecha"))).map(s -> Api.num(s.get("id"))).findFirst().orElseThrow();
        pp.visible("salida_item_" + nueva);
        paso("Salida programada");
        pp.tocar("salida_cerrar_" + nueva);
        pp.esperarTexto("salida_estado_" + nueva, "errada");
        paso("Salida cerrada");
        assertEquals("cerrada", Api.salida(p, t.id(), nueva).get("estado"));
    }

    @Test
    @Tag("HU-P12") @Tag("TC-MOB-031")
    @DisplayName("TC-MOB-031 Bandeja de reservas en la app: solo las propias y filtro por tipo")
    void bandeja() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Hotel h = Api.crearHotel(p, "Doble", 3, 150);
        Api.Tour t = Api.crearTour(p, 10, 60, "Cultura");
        Api.Sesion v = Api.registrarViajero("Hilda");
        String codHotel = (String) Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1).get("codigo");
        String codTour = (String) Api.reservarTour(v, t.salidaId(), 2).get("codigo");
        ProveedorPantallas pp = entrar(p);
        pp.recibidas();
        pp.visible("rr_card_" + codHotel);
        pp.traer("rr_card_" + codTour);
        paso("Las dos reservas del proveedor");
        assertEquals(2, pp.cantidad("rr_card_"));
        pp.tocar("rr_tipo_tour");
        pp.desaparece("rr_card_" + codHotel);
        paso("Filtro tours");
        assertTrue(pp.existe("rr_card_" + codTour));
    }

    @Test
    @Tag("HU-P15") @Tag("TC-MOB-032")
    @DisplayName("TC-MOB-032 Eliminar una publicación desde la app")
    void eliminar() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Hotel h = Api.crearHotel(p, "Doble", 2, 150);
        ProveedorPantallas pp = entrar(p);
        pp.tocar("btn_eliminar_" + h.id());
        pp.visible("dialogo_eliminar_publicacion");
        paso("Confirmación");
        pp.confirmar("dialogo_eliminar_publicacion");
        pp.visible("panel_mensaje");
        pp.desaparece("publicacion_" + h.id());
        paso("Eliminada");
        assertEquals(404, new Api.Sesion().get("/hoteles/" + h.id()).status());
    }
}
