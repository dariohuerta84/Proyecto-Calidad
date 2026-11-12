package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.HotelFormPage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.paginas.PanelProveedorPage;
import com.trouvels360.web.paginas.ProgramaTourPage;
import com.trouvels360.web.paginas.ReservasRecibidasPage;
import com.trouvels360.web.paginas.TourFormPage;
import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Proveedor")
@Feature("Panel, publicaciones, programación y reservas recibidas")
class ProveedorTest extends BaseTest {

    @Test
    @Tag("HU-P03") @Tag("HU-P04") @Tag("TC-WEB-023")
    @DisplayName("TC-WEB-023 Panel: indicadores iguales a la API y paginación de publicaciones")
    void panelIndicadoresPaginacion() {
        Api.Sesion p = Api.registrarProveedor();
        for (int i = 0; i < 16; i++) Api.crearTour(p, 5, 50 + i, "Cultura");
        Api.Hotel h = Api.crearHotel(p, "Doble", 5, 150);
        Api.Sesion v = Api.registrarViajero("Gala");
        Map<String, Object> r1 = Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1);
        Api.reservarHotel(v, h.hab("Doble"), Datos.diaLibre(), 1, 1);
        Api.aceptar(p, r1.get("id"));
        Map<String, Object> ind = p.get("/proveedor/indicadores").esperar(200).data();

        new Navegacion(d).entrarComo(p, "/proveedor");
        PanelProveedorPage panel = new PanelProveedorPage(d).abrir();
        paso("Las tarjetas de indicadores muestran lo mismo que la API");
        assertEquals("1", panel.texto("ind-pendientes"));
        assertEquals(String.valueOf(ind.get("solicitudes_pendientes")), panel.texto("ind-pendientes"));
        assertEquals("1", panel.texto("ind-confirmadas"));
        assertEquals("1", panel.texto("pendientes-badge"));
        assertEquals("Sin reseñas", panel.texto("ind-resenas"));
        paso("17 publicaciones: 15 en la página 1 y 2 en la página 2");
        assertEquals(15, panel.publicaciones().size());
        panel.pagina(2);
        assertEquals(2, panel.esperarCantidad("publicacion-card", 2).size());
    }

    @Test
    @Tag("HU-P05") @Tag("TC-WEB-024")
    @DisplayName("TC-WEB-024 Publicar un hotel con dos tipos de habitación desde el formulario")
    void crearHotel() {
        Api.Sesion p = Api.registrarProveedor();
        String ciudad = Datos.ciudad();
        String nombre = "Hotel Selenium " + ciudad;
        new Navegacion(d).entrarComo(p, "/proveedor/crear-hotel");
        HotelFormPage f = new HotelFormPage(d).crear().datos(nombre, ciudad, "Jr. Las Flores 123", 4).habitacion(0, "Matrimonial", 2, 180, 4);
        f.agregarTipo().habitacion(1, "Familiar", 4, 260, 2);
        f.publicar();
        assertFalse(f.ok().isEmpty());
        paso("El hotel aparece en el catálogo con sus dos tipos");
        long id = Api.idPorNombre("hotel", ciudad, nombre);
        assertTrue(id > 0, "No aparece en el catálogo");
        List<String> tipos = Api.detalleHotel(p, id).stream().map(x -> (String) x.get("nombre")).sorted().toList();
        assertEquals(List.of("Familiar", "Matrimonial"), tipos);
    }

    @Test
    @Tag("HU-P07") @Tag("TC-WEB-025")
    @DisplayName("TC-WEB-025 Editar hotel: cambiar la tarifa de un tipo y agregar otro")
    void editarHabitaciones() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Hotel h = Api.crearHotel(p, "Doble", 3, 150);
        new Navegacion(d).entrarComo(p, "/proveedor");
        HotelFormPage f = new HotelFormPage(d).editar(h.id());
        assertEquals(1, f.tiposDeHabitacion());
        f.precio(0, 175);
        f.agregarTipo().habitacion(1, "Suite", 2, 320, 1);
        f.guardar();
        assertFalse(f.ok().isEmpty());
        paso("La API refleja la nueva tarifa y el nuevo tipo");
        List<Map<String, Object>> habs = Api.detalleHotel(p, h.id());
        assertEquals(2, habs.size());
        Map<String, Object> doble = habs.stream().filter(x -> "Doble".equals(x.get("nombre"))).findFirst().orElseThrow();
        assertEquals(175.0, ((Number) doble.get("precio_por_noche")).doubleValue());
        assertTrue(habs.stream().anyMatch(x -> "Suite".equals(x.get("nombre"))));
    }

    @Test
    @Tag("HU-P08") @Tag("TC-WEB-026")
    @DisplayName("TC-WEB-026 Publicar un tour con su primera salida desde el formulario")
    void crearTour() {
        Api.Sesion p = Api.registrarProveedor();
        String ciudad = Datos.ciudad();
        String nombre = "Tour Selenium " + ciudad;
        String fecha = Datos.fecha(30);
        new Navegacion(d).entrarComo(p, "/proveedor/crear-tour");
        TourFormPage f = new TourFormPage(d).crear().datos(nombre, ciudad, "Gastronomía", 95, 180).salida(0, fecha, "09:30", 12);
        assertTrue(f.puedePublicar(), "El formulario completo debe habilitar Publicar");
        f.publicar();
        assertFalse(f.ok().isEmpty());
        paso("El tour aparece en el catálogo con la salida programada");
        long id = Api.idPorNombre("tour", ciudad, nombre);
        assertTrue(id > 0, "No aparece en el catálogo");
        List<Map<String, Object>> salidas = p.get("/tours/" + id + "/salidas").esperar(200).lista();
        assertEquals(1, salidas.size());
        assertEquals(fecha, salidas.get(0).get("fecha"));
        assertEquals(12L, Api.num(salidas.get(0).get("cupo_total")));
    }

    @Test
    @Tag("HU-P10") @Tag("TC-WEB-027")
    @DisplayName("TC-WEB-027 Agregar, reordenar y eliminar actividades del tour")
    void actividades() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Tour t = Api.crearTour(p, 10, 60, "Cultura");
        new Navegacion(d).entrarComo(p, "/proveedor");
        ProgramaTourPage prog = new ProgramaTourPage(d).abrir(t.id());
        prog.agregarActividad("Plaza de Armas", 30);
        prog.agregarActividad("Qorikancha", 60);
        prog.agregarActividad("Mercado San Pedro", 45);
        assertEquals(List.of("Plaza de Armas", "Qorikancha", "Mercado San Pedro"), prog.actividades());
        paso("Subir 'Mercado San Pedro' una posición");
        prog.subir("Mercado San Pedro");
        assertEquals(List.of("Plaza de Armas", "Mercado San Pedro", "Qorikancha"), prog.actividades());
        paso("Eliminar 'Plaza de Armas' renumera el resto");
        prog.eliminarActividad("Plaza de Armas");
        assertEquals(List.of("Mercado San Pedro", "Qorikancha"), prog.actividades());
        List<Map<String, Object>> api = p.get("/tours/" + t.id() + "/actividades").esperar(200).lista();
        assertEquals(List.of(1L, 2L), api.stream().map(a -> Api.num(a.get("orden"))).toList());
    }

    @Test
    @Tag("HU-P11") @Tag("TC-WEB-028")
    @DisplayName("TC-WEB-028 Programar, cerrar y eliminar una salida desde el panel")
    void salidas() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Tour t = Api.crearTour(p, 10, 60, "Cultura");
        new Navegacion(d).entrarComo(p, "/proveedor");
        ProgramaTourPage prog = new ProgramaTourPage(d).abrir(t.id());
        assertEquals(1, prog.salidas().size());
        String fecha = Datos.fecha(45);
        prog.programarSalida(fecha, "15:00", 20);
        long nueva = p.get("/tours/" + t.id() + "/salidas").esperar(200).lista().stream()
                .filter(s -> fecha.equals(s.get("fecha"))).map(s -> Api.num(s.get("id"))).findFirst().orElseThrow();
        assertEquals("0/20", prog.dentro(prog.salida(nueva), "salida-ocupacion").getText().trim());
        paso("Cerrar la salida: deja de aceptar reservas");
        prog.cerrar(nueva);
        assertEquals("cerrada", Api.salida(p, t.id(), nueva).get("estado"));
        paso("Eliminar la salida sin reservas");
        prog.eliminar(nueva);
        assertEquals(1, p.get("/tours/" + t.id() + "/salidas").esperar(200).lista().size());
    }

    @Test
    @Tag("HU-P12") @Tag("TC-WEB-029")
    @DisplayName("TC-WEB-029 Bandeja de reservas recibidas: filtros por estado y tipo, y paginación")
    void bandejaFiltros() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Hotel h = Api.crearHotel(p, "Doble", 20, 150);
        Api.Tour t = Api.crearTour(p, 10, 60, "Cultura");
        Api.Sesion v = Api.registrarViajero("Olga");
        int dia = Datos.diaLibre();
        Map<String, Object> primera = null;
        for (int i = 0; i < 16; i++) {
            Map<String, Object> r = Api.reservarHotel(v, h.hab("Doble"), dia, 1, 1);
            if (primera == null) primera = r;
        }
        Api.aceptar(p, primera.get("id"));
        Api.reservarTour(v, t.salidaId(), 2);

        new Navegacion(d).entrarComo(p, "/proveedor");
        ReservasRecibidasPage rr = new ReservasRecibidasPage(d).abrir(null);
        paso("17 reservas: 15 en la primera página, pendientes primero");
        assertEquals(15, rr.esperarCantidad("rr-card", 15).size());
        assertEquals("pendiente", rr.tarjetas().get(0).getAttribute("data-estado"));
        assertTrue(rr.existe("rr-paginacion"));
        rr.click("rr-pagina-2");
        rr.esperarCantidad("rr-card", 2);
        paso("Filtro pendiente: solo pendientes (15 de 15)");
        rr.filtrarEstado("pendiente");
        assertEquals(15, rr.esperarCantidad("rr-card", 15).size());
        paso("Filtro confirmada + tipo tour: la reserva de tour");
        rr.filtrarEstado("confirmada");
        assertEquals(2, rr.esperarCantidad("rr-card", 2).size());
        rr.filtrarTipo("tour");
        assertEquals(1, rr.esperarCantidad("rr-card", 1).size());
        assertEquals("tour", rr.tarjetas().get(0).getAttribute("data-tipo"));
    }

    @Test
    @Tag("HU-P15") @Tag("TC-WEB-030")
    @DisplayName("TC-WEB-030 Eliminar publicación: confirmación, éxito sin reservas y bloqueo con reservas activas")
    void eliminarPublicacion() {
        Api.Sesion p = Api.registrarProveedor();
        Api.Hotel libre = Api.crearHotel(p, "Doble", 2, 150);
        Api.Hotel ocupado = Api.crearHotel(p, "Doble", 2, 150);
        Api.reservarHotel(Api.registrarViajero("Raúl"), ocupado.hab("Doble"), Datos.diaLibre(), 1, 1);

        new Navegacion(d).entrarComo(p, "/proveedor");
        PanelProveedorPage panel = new PanelProveedorPage(d).abrir();
        paso("Cancelar en el diálogo no elimina");
        panel.pedirEliminar(libre.id());
        panel.click("eliminar-no");
        panel.desaparece("modal-eliminar");
        assertTrue(panel.publicacion(libre.id()).isDisplayed());
        paso("Confirmar elimina la publicación sin reservas");
        panel.pedirEliminar(libre.id());
        panel.click("eliminar-si");
        panel.visible("mensaje-ok");
        panel.espera.until(x -> panel.todos("publicacion-card").stream().noneMatch(c -> String.valueOf(libre.id()).equals(c.getAttribute("data-id"))));
        assertEquals(404, new Api.Sesion().get("/hoteles/" + libre.id()).status());
        paso("Con una reserva activa la API responde 409 y se muestra el motivo");
        panel.pedirEliminar(ocupado.id());
        panel.click("eliminar-si");
        assertTrue(panel.texto("mensaje-error").contains("reserva"), panel.texto("mensaje-error"));
        assertEquals(200, new Api.Sesion().get("/hoteles/" + ocupado.id()).status());
    }
}
