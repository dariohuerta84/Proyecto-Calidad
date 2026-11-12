package com.trouvels360.web.paginas;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PanelProveedorPage extends BasePage {
    public PanelProveedorPage(WebDriver d) { super(d); }

    public PanelProveedorPage abrir() { irA("/proveedor"); visible("panel-proveedor"); visible("indicadores"); return this; }

    public List<WebElement> publicaciones() { return esperarAlMenos("publicacion-card", 1); }

    public PanelProveedorPage buscar(String nombre) {
        escribir("filtro-busqueda", nombre);
        visible("filtro-busqueda").sendKeys(Keys.ENTER);
        espera.until(x -> todos("publicacion-card").size() == 1
                && dentro(todos("publicacion-card").get(0), "publicacion-nombre").getText().contains(nombre));
        return this;
    }

    public WebElement publicacion(long id) { return porAtributo("publicacion-card", "data-id", id); }

    public void pedirEliminar(long id) { click(dentro(publicacion(id), "btn-eliminar")); visible("modal-eliminar"); }

    public PanelProveedorPage pagina(int n) {
        String primera = publicaciones().get(0).getAttribute("data-id");
        click("pagina-" + n);
        espera.until(x -> !todos("publicacion-card").isEmpty() && !todos("publicacion-card").get(0).getAttribute("data-id").equals(primera));
        return this;
    }
}
