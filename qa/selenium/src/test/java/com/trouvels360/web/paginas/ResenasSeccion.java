package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Optional;

public class ResenasSeccion extends BasePage {
    public ResenasSeccion(WebDriver d) { super(d); visible("reviews-section"); }

    public void publicar(int estrellas, String comentario) {
        click("btn-dejar-opinion");
        llenar(estrellas, comentario);
    }

    public void llenar(int estrellas, String comentario) {
        visible("review-form");
        click("review-estrella-" + estrellas);
        escribir("review-comentario", comentario);
        click("review-enviar");
        desaparece("review-form");
    }

    public Optional<WebElement> resenaConTexto(String texto) {
        return todos("review-card").stream().filter(c -> dentro(c, "review-texto").getText().contains(texto)).findFirst();
    }

    public WebElement esperarResena(String texto) { return espera.until(x -> resenaConTexto(texto).orElse(null)); }
}
