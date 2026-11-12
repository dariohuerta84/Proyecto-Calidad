package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class MisReservasPage extends BasePage {
    public MisReservasPage(WebDriver d) { super(d); }

    public MisReservasPage abrir() { irA("/mis-reservas"); visible("mis-reservas"); esperarFinCarga(); return this; }

    public void esperarFinCarga() { desaparece("cargando"); }

    public List<WebElement> tarjetas() { esperarFinCarga(); return todos("reserva-card"); }

    public WebElement tarjeta(String codigo) { return porAtributo("reserva-card", "data-codigo", codigo); }

    public String estado(String codigo) { return tarjeta(codigo).getAttribute("data-estado"); }

    public MisReservasPage pestanaTours() { click("tab-tours"); esperarFinCarga(); return this; }

    public MisReservasPage filtro(String estado) { click("filtro-" + estado); esperarFinCarga(); return this; }

    public MisReservasPage pagina(int n) { click("pagina-" + n); esperarFinCarga(); return this; }
}
