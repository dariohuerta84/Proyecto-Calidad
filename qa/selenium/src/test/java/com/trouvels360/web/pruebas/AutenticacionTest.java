package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.HotelDetallePage;
import com.trouvels360.web.paginas.LoginPage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.paginas.PagoHotelPage;
import com.trouvels360.web.paginas.PerfilPage;
import com.trouvels360.web.paginas.RegistroPage;
import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Config;
import com.trouvels360.web.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Cuentas y acceso")
@Feature("Registro, login y perfil")
class AutenticacionTest extends BaseTest {

    @Test
    @Tag("TRV-101") @Tag("TC-WEB-001")
    @DisplayName("TC-WEB-001 Registro de viajero end-to-end y aterrizaje en el catálogo")
    void registroViajero() {
        String correo = Datos.correo("web.registro");
        paso("Completar el formulario de registro como viajero");
        RegistroPage r = new RegistroPage(d).abrir("viajero").viajero("Lucía", "Quispe").credenciales(correo, "Clave123", "Clave123");
        r.enviar();
        paso("Se inicia sesión y aterriza en el catálogo de hoteles");
        r.visible("registro-ok");
        r.esperarUrl("/hoteles");
        Navegacion nav = new Navegacion(d);
        nav.esperarTexto("menu-usuario", "Lucía");
        assertTrue(nav.existe("buscar-destino"), "Debe ver el buscador del catálogo");
        assertFalse(nav.existe("nav-crear-hotel"), "Un viajero no ve opciones del proveedor");
        paso("La cuenta existe en la API con rol viajero");
        assertEquals("viajero", Api.login(correo, "Clave123").usuario.get("rol"));
    }

    @Test
    @Tag("TRV-101") @Tag("TC-WEB-002")
    @DisplayName("TC-WEB-002 Mensajes de validación en el formulario de registro")
    void validacionesRegistro() {
        RegistroPage r = new RegistroPage(d).abrir("viajero");
        paso("Campos obligatorios vacíos: mensajes bajo cada campo y botón deshabilitado");
        r.tocar("registro-nombre");
        r.tocar("registro-apellido");
        assertTrue(r.mensajesCampo().contains("El nombre es requerido."), "Mensaje de nombre: " + r.mensajesCampo());
        assertTrue(r.mensajesCampo().contains("El apellido es requerido."), "Mensaje de apellido");
        assertFalse(r.botonHabilitado(), "No se puede enviar un formulario inválido");
        paso("Correo con formato inválido y contraseña corta");
        r.viajero("Ana", "Pérez").credenciales("correo-invalido", "123", "123");
        r.tocar("registro-password-confirmacion");
        assertTrue(r.marcadoInvalido("registro-email"), "El correo queda marcado como inválido");
        assertTrue(r.marcadoInvalido("registro-password"), "La contraseña corta queda marcada");
        assertFalse(r.botonHabilitado());
        paso("Contraseñas que no coinciden no permiten enviar");
        r.credenciales(Datos.correo("web.val"), "Clave123", "Clave999");
        r.tocar("registro-password-confirmacion");
        assertFalse(r.botonHabilitado(), "Confirmación distinta: no se envía");
        paso("Correo ya registrado: el error de la API se muestra en el formulario");
        r.credenciales(Config.get("viajero.email"), "Clave123", "Clave123");
        r.enviar();
        assertTrue(r.texto("registro-error").toLowerCase().contains("correo"), "Debe explicar que el correo ya existe");
    }

    @Test
    @Tag("TRV-102") @Tag("TC-WEB-003")
    @DisplayName("TC-WEB-003 Registro de proveedor y acceso a su panel")
    void registroProveedor() {
        String empresa = "Turismo Selenium " + Datos.sufijo() + " SAC";
        RegistroPage r = new RegistroPage(d).abrir("proveedor").proveedor(empresa, Datos.ruc(), Datos.celular())
                .credenciales(Datos.correo("web.prov"), "Clave123", "Clave123");
        r.enviar();
        r.esperarUrl("/proveedor");
        Navegacion nav = new Navegacion(d);
        nav.visible("panel-proveedor");
        nav.esperarTexto("menu-usuario", empresa);
        assertTrue(nav.existe("nav-crear-hotel") && nav.existe("nav-reservas-recibidas"), "Menú del proveedor visible");
        assertTrue(nav.existe("sin-publicaciones"), "Un proveedor nuevo no tiene publicaciones");
    }

    @Test
    @Tag("TRV-103") @Tag("TC-WEB-004")
    @DisplayName("TC-WEB-004 Login de viajero y de proveedor muestran el menú de su rol")
    void loginPorRol() {
        LoginPage login = new LoginPage(d).abrir();
        paso("Credenciales incorrectas muestran error sin revelar cuál falló");
        login.ingresar(Config.get("viajero.email"), "incorrecta");
        assertTrue(login.error().length() > 0);
        paso("Viajero: va al catálogo y ve 'Mis reservas'");
        login.ingresar(Config.get("viajero.email"), Config.get("viajero.password"));
        login.esperarUrl("/hoteles");
        Navegacion nav = new Navegacion(d);
        nav.abrirMenu();
        assertTrue(nav.existe("link-mis-reservas"));
        assertFalse(nav.existe("nav-crear-hotel"));
        nav.cerrarSesion();
        paso("Proveedor: va a su panel y ve el menú de publicaciones");
        login.abrir().ingresar(Config.get("proveedor.email"), Config.get("proveedor.password"));
        login.esperarUrl("/proveedor");
        nav.visible("panel-proveedor");
        assertTrue(nav.existe("nav-crear-hotel") && nav.existe("nav-crear-tour") && nav.existe("nav-reservas-recibidas"));
        nav.abrirMenu();
        assertFalse(nav.existe("link-mis-reservas"), "El proveedor no tiene 'Mis reservas'");
    }

    @Test
    @Tag("TRV-103") @Tag("TC-WEB-005")
    @DisplayName("TC-WEB-005 Login desde 'Confirmar y pagar' vuelve al resumen con la misma selección")
    void loginDesdeReservar() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        int dia = Datos.diaLibre();
        paso("Anónimo: elige 2 habitaciones y pulsa Reservar");
        HotelDetallePage det = new HotelDetallePage(d).abrir(h.id(), dia, 2).sumar("Doble", 2);
        det.click("btn-reservar-hotel");
        det.click("btn-sin-itinerario");
        paso("Sin sesión lo manda al login conservando la página de pago con la selección");
        det.esperarUrl("/auth/login");
        assertTrue(d.getCurrentUrl().contains("returnUrl") && d.getCurrentUrl().contains("reserva_0_cant%3D2"), "returnUrl con la selección");
        new LoginPage(d).ingresar(Config.get("viajero.email"), Config.get("viajero.password"));
        paso("Tras iniciar sesión vuelve al resumen con las 2 habitaciones y el total con IGV");
        det.esperarUrl("/hoteles/pagos");
        PagoHotelPage pago = new PagoHotelPage(d).listo();
        assertEquals(Datos.soles(708), pago.total());
        assertTrue(pago.texto("pago-linea").contains("Doble"));
        assertEquals(1, pago.lineas());
    }

    @Test
    @Tag("TRV-104") @Tag("TC-WEB-006")
    @DisplayName("TC-WEB-006 Editar perfil y ver el nombre actualizado en el menú")
    void editarPerfil() {
        Api.Sesion v = Api.registrarViajero("Mario");
        new Navegacion(d).entrarComo(v, "/editar-perfil");
        PerfilPage p = new PerfilPage(d).abrir();
        p.cambiarNombre("Mariano");
        assertTrue(p.texto("perfil-mensaje").length() > 0);
        new Navegacion(d).esperarTexto("menu-usuario", "Mariano");
        paso("El cambio quedó guardado en la API");
        assertEquals("Mariano", v.get("/auth/me").esperar(200).data().get("nombre"));
    }

    @Test
    @Tag("TRV-105") @Tag("TC-WEB-007")
    @DisplayName("TC-WEB-007 Eliminar cuenta: el diálogo pide confirmación y 'Cancelar' conserva la cuenta")
    void eliminarCuentaCancelar() {
        Api.Sesion v = Api.registrarViajero("Rosa");
        new Navegacion(d).entrarComo(v, "/editar-perfil");
        PerfilPage p = new PerfilPage(d).abrir();
        p.click("btn-eliminar-cuenta");
        p.visible("modal-eliminar-cuenta");
        p.click("eliminar-cuenta-no");
        p.desaparece("modal-eliminar-cuenta");
        paso("Sigue con sesión y la cuenta sigue activa");
        assertTrue(new Navegacion(d).saludo().contains("Rosa"));
        assertEquals(200, v.get("/auth/me").status());
    }
}
