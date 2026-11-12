package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ResultadosPage extends BasePage {
    public ResultadosPage(WebDriver d) { super(d); }

    public List<WebElement> tarjetas() { return esperarAlMenos("resultado-card", 1); }

    public List<String> textos(String testId) { return tarjetas().stream().map(t -> dentro(t, testId).getText().trim()).toList(); }

    public void verDetalle(int indice) { click(dentro(tarjetas().get(indice), "btn-ver-detalles")); }

    public String error() { return texto("resultados-error"); }
}
