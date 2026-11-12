package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

public class PerfilPage extends BasePage {
    public PerfilPage(WebDriver d) { super(d); }

    public PerfilPage abrir() { irA("/editar-perfil"); visible("form-perfil"); espera.until(x -> !visible("perfil-email").getAttribute("value").isEmpty()); return this; }

    public void cambiarNombre(String nombre) {
        escribir("perfil-nombre", nombre);
        click("perfil-guardar");
        visible("perfil-mensaje");
    }
}
