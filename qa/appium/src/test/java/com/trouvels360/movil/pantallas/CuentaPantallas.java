package com.trouvels360.movil.pantallas;

import io.appium.java_client.android.AndroidDriver;

/** Login, registro, perfil y barra inferior. */
public class CuentaPantallas extends BasePantalla {
    public CuentaPantallas(AndroidDriver d) { super(d); }

    /** Desde cualquier pestaña: Cuenta → Iniciar sesión → credenciales. */
    public void iniciarSesion(String email, String password) {
        if (!existe("pantalla_login")) {
            tocar("nav_cuenta");
            if (existe("btn_logout")) cerrarSesion();
            tocar("btn_ir_login");
        }
        ingresar(email, password);
    }

    public void ingresar(String email, String password) {
        visible("pantalla_login");
        escribir("login_email", email);
        escribir("login_password", password);
        tocar("login_submit");
        desaparece("pantalla_login");
    }

    public void cerrarSesion() {
        tocar("nav_cuenta");
        tocar("btn_logout");
        visible("btn_ir_login");
    }

    public void abrirRegistro() {
        tocar("nav_cuenta");
        if (existe("btn_logout")) cerrarSesion();
        tocar("btn_ir_login");
        tocar("link_registro");
        visible("pantalla_registro");
    }

    public void registroViajero(String nombre, String apellido, String email, String pass, String confirmacion) {
        tocar("registro_rol_viajero");
        escribir("registro_nombre", nombre);
        escribir("registro_apellido", apellido);
        escribir("registro_email", email);
        escribir("registro_password", pass);
        escribir("registro_password_confirmacion", confirmacion);
    }

    public void registroProveedor(String empresa, String ruc, String telefono, String email, String pass) {
        tocar("registro_rol_proveedor");
        escribir("registro_empresa", empresa);
        escribir("registro_ruc", ruc);
        escribir("registro_telefono", telefono);
        escribir("registro_email", email);
        escribir("registro_password", pass);
        escribir("registro_password_confirmacion", pass);
    }

    public void perfil() { tocar("nav_cuenta"); visible("pantalla_perfil"); }
}
