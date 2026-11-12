package com.trouvels360.movil.pruebas;

import com.trouvels360.movil.pantallas.CuentaPantallas;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("App móvil")
@Feature("Cuentas y acceso")
class CuentaMovilTest extends BaseTestMovil {

    @Test
    @Tag("TRV-101") @Tag("TC-MOB-001")
    @DisplayName("TC-MOB-001 Registro de viajero desde la app y sesión iniciada")
    void registroViajero() {
        CuentaPantallas c = cuenta();
        String correo = Datos.correo("app.registro");
        c.abrirRegistro();
        c.registroViajero("Valeria", "Ríos", correo, "Clave123", "Clave123");
        paso("Formulario de registro completo");
        c.tocar("registro_submit");
        c.desaparece("pantalla_registro");
        paso("Sesión iniciada tras el registro");
        c.perfil();
        assertTrue(c.texto("perfil_nombre_visible").contains("Valeria"));
        assertTrue(c.texto("perfil_rol").toLowerCase().contains("viajero"));
        assertEquals("viajero", Api.login(correo, "Clave123").usuario.get("rol"));
    }

    @Test
    @Tag("TRV-101") @Tag("TC-MOB-002")
    @DisplayName("TC-MOB-002 Botón Registrarme deshabilitado con contraseñas distintas")
    void registroContrasenasDistintas() {
        CuentaPantallas c = cuenta();
        c.abrirRegistro();
        c.registroViajero("Ana", "Soto", Datos.correo("app.val"), "Clave123", "Clave999");
        paso("Contraseñas distintas");
        assertFalse(c.habilitado("registro_submit"), "No debe permitir enviar");
        assertTrue(c.existe("registro_password_confirmacion_error"), "Mensaje bajo la confirmación");
        c.escribir("registro_password_confirmacion", "Clave123");
        assertTrue(c.habilitado("registro_submit"), "Con contraseñas iguales se habilita");
    }

    @Test
    @Tag("TRV-102") @Tag("TC-MOB-003")
    @DisplayName("TC-MOB-003 Registro de proveedor desde la app y acceso al panel")
    void registroProveedor() {
        CuentaPantallas c = cuenta();
        c.abrirRegistro();
        c.registroProveedor("Andes App " + Datos.sufijo() + " SAC", Datos.ruc(), Datos.celular(), Datos.correo("app.prov"), "Clave123");
        c.tocar("registro_submit");
        c.visible("pantalla_panel_proveedor");
        paso("Panel del proveedor con su barra de navegación");
        assertTrue(c.existe("nav_panel") && c.existe("nav_recibidas"));
        assertFalse(c.existe("nav_reservas"), "El proveedor no tiene 'Reservas' de viajero");
        assertTrue(c.existe("panel_vacio"));
    }

    @Test
    @Tag("TRV-103") @Tag("TC-MOB-004")
    @DisplayName("TC-MOB-004 Login, cierre de sesión y bloqueo de pestañas privadas")
    void loginLogoutPestanasPrivadas() {
        CuentaPantallas c = cuenta();
        paso("Sin sesión, Reservas pide iniciar sesión");
        c.tocar("nav_reservas");
        c.visible("login_requerido");
        c.tocar("btn_ir_login");
        c.ingresar(Config.get("viajero.email"), "incorrecta");
        c.visible("login_error");
        paso("Credenciales correctas: ve sus reservas");
        c.escribir("login_password", Config.get("viajero.password"));
        c.tocar("login_submit");
        c.tocar("nav_reservas");
        c.visible("pantalla_mis_reservas");
        paso("Cerrar sesión vuelve a bloquear la pestaña");
        c.cerrarSesion();
        c.tocar("nav_reservas");
        c.visible("login_requerido");
    }

    @Test
    @Tag("TRV-103") @Tag("TC-MOB-005")
    @DisplayName("TC-MOB-005 Reservar sin sesión → login → regreso al detalle con la selección")
    void reservarSinSesion() {
        Api.Hotel h = Api.crearHotel(Api.proveedor(), "Doble", 3, 150);
        HotelPantallas hp = new HotelPantallas(d);
        hp.abrirHotel(h.id(), h.ciudad(), Datos.diaLibre(), 2);
        long hab = h.hab("Doble");
        hp.sumar(hab, 2);
        paso("Anónimo con 2 habitaciones elegidas pulsa Reservar");
        hp.tocar("btn_reservar_hotel");
        cuenta().ingresar(Config.get("viajero.email"), Config.get("viajero.password"));
        paso("Vuelve al detalle con las mismas 2 habitaciones");
        hp.visible("pantalla_detalle_hotel");
        assertEquals("2", hp.texto("habitacion_seleccionadas_" + hab));
        hp.reservarSinItinerario();
        assertEquals(Datos.digitos(708), Datos.digitos(hp.texto("resumen_total")));
    }

    @Test
    @Tag("TRV-104") @Tag("TC-MOB-006")
    @DisplayName("TC-MOB-006 Editar perfil en la app y verlo en la pestaña Perfil")
    void editarPerfil() {
        Api.Sesion v = Api.registrarViajero("Tomás");
        CuentaPantallas c = cuenta();
        c.iniciarSesion(v.email(), "Clave123");
        c.perfil();
        c.escribir("perfil_nombre", "Tomasito");
        c.tocar("perfil_guardar");
        c.visible("perfil_mensaje");
        paso("Nombre actualizado");
        c.esperarTexto("perfil_nombre_visible", "Tomasito");
        assertEquals("Tomasito", v.get("/auth/me").esperar(200).data().get("nombre"));
    }

    @Test
    @Tag("TRV-104") @Tag("TC-MOB-007")
    @DisplayName("TC-MOB-007 Cambio de contraseña: confirmación distinta deshabilita Guardar")
    void cambioContrasena() {
        Api.Sesion v = Api.registrarViajero("Lía");
        CuentaPantallas c = cuenta();
        c.iniciarSesion(v.email(), "Clave123");
        c.perfil();
        c.escribir("password_actual", "Clave123");
        c.escribir("password_nueva", "Nueva1234");
        c.escribir("password_confirmacion", "Otra1234");
        paso("Confirmación distinta");
        assertFalse(c.habilitado("password_guardar"));
        c.escribir("password_confirmacion", "Nueva1234");
        c.tocar("password_guardar");
        c.visible("password_mensaje");
        assertEquals(200, Api.login(v.email(), "Nueva1234").get("/auth/me").status());
    }

    @Test
    @Tag("TRV-105") @Tag("TC-MOB-008")
    @DisplayName("TC-MOB-008 Eliminar cuenta desde Perfil y volver al inicio de sesión")
    void eliminarCuenta() {
        Api.Sesion v = Api.registrarViajero("Iván");
        CuentaPantallas c = cuenta();
        c.iniciarSesion(v.email(), "Clave123");
        c.perfil();
        c.tocar("btn_eliminar_cuenta");
        paso("Diálogo de confirmación");
        c.confirmar("dialogo_eliminar_cuenta");
        c.tocar("nav_cuenta");
        c.visible("btn_ir_login");
        paso("La cuenta ya no puede iniciar sesión");
        Api.Sesion s = new Api.Sesion();
        s.get("/auth/csrf");
        assertEquals(401, s.post("/auth/login", java.util.Map.of("email", v.email(), "password", "Clave123")).status());
    }
}
