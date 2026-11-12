package com.trouvels360.web.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class RegistroPage extends BasePage {
    public RegistroPage(WebDriver d) { super(d); }

    public RegistroPage abrir(String rol) {
        irA("/auth/registro" + (rol == null ? "" : "?rol=" + rol));
        visible("registro-email");
        if (rol != null) seleccionar("registro-rol", rol);
        return this;
    }

    public RegistroPage viajero(String nombre, String apellido) {
        escribir("registro-nombre", nombre);
        escribir("registro-apellido", apellido);
        return this;
    }

    public RegistroPage proveedor(String empresa, String ruc, String telefono) {
        escribir("registro-empresa_nombre", empresa);
        escribir("registro-ruc", ruc);
        escribir("registro-telefono", telefono);
        return this;
    }

    public RegistroPage credenciales(String email, String password, String confirmacion) {
        escribir("registro-email", email);
        escribir("registro-password", password);
        escribir("registro-password-confirmacion", confirmacion);
        return this;
    }

    public void enviar() { click("registro-submit"); }

    public boolean botonHabilitado() { return presente("registro-submit").isEnabled(); }

    /** Mensajes de validación visibles bajo los campos (small.mensaje-error). */
    public List<String> mensajesCampo() {
        return d.findElements(By.cssSelector("form small.mensaje-error")).stream().filter(WebElement::isDisplayed).map(WebElement::getText).toList();
    }

    public boolean marcadoInvalido(String testId) { return presente(testId).getAttribute("class").contains("invalido"); }

    /** Sale del campo para que Angular lo marque como "touched". */
    public void tocar(String testId) { WebElement e = visible(testId); e.click(); e.sendKeys(org.openqa.selenium.Keys.TAB); }
}
