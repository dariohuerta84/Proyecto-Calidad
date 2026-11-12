package com.trouvels360.web.paginas;

import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Config;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

/** Cabecera común y atajo de sesión (las pruebas que no son de login entran con la cookie que ya obtuvo la API). */
public class Navegacion extends BasePage {
    public Navegacion(WebDriver d) { super(d); }

    /** Pone en el navegador la cookie HttpOnly del JWT y el XSRF-TOKEN de una sesión creada por API; la app la restaura con /auth/me. */
    public Navegacion entrarComo(Api.Sesion s, String ruta) {
        irA("/favicon.ico"); // hay que estar en el dominio de la app para poder fijar cookies y localStorage
        d.manage().deleteAllCookies();
        d.manage().addCookie(new Cookie.Builder("access_token", s.token()).path("/").isHttpOnly(true).build());
        d.manage().addCookie(new Cookie.Builder("XSRF-TOKEN", s.xsrf()).path("/").build());
        // La app restaura la sesión con /auth/me solo si hay un usuario local (igual que tras un login real)
        ((org.openqa.selenium.JavascriptExecutor) d).executeScript(
                "localStorage.setItem('current_user', arguments[0]); localStorage.setItem('user_role', arguments[1]);",
                new org.openqa.selenium.json.Json().toJson(s.usuario), String.valueOf(s.usuario.get("rol")));
        irA(ruta);
        visible("menu-usuario");
        return this;
    }

    public Navegacion ir(String ruta) { irA(ruta); return this; }

    public String saludo() { return texto("menu-usuario"); }

    public void abrirMenu() {
        click("menu-usuario");
        visible("btn-logout");
    }

    public void cerrarSesion() {
        abrirMenu();
        click("btn-logout");
        visible("link-login");
    }

    public static String base() { return Config.baseUrl(); }
}
