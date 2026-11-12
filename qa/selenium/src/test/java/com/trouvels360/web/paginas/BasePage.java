package com.trouvels360.web.paginas;

import com.trouvels360.web.soporte.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Base de todos los Page Objects. Solo esperas explícitas (WebDriverWait) y selectores data-testid,
 * que no cambian con estilos ni textos.
 */
public abstract class BasePage {
    protected final WebDriver d;
    public final WebDriverWait espera;

    protected BasePage(WebDriver d) {
        this.d = d;
        this.espera = new WebDriverWait(d, Duration.ofSeconds(Config.entero("espera.segundos", 15)));
        this.espera.ignoring(StaleElementReferenceException.class);
    }

    public static By tid(String testId) { return By.cssSelector("[data-testid='" + testId + "']"); }

    public WebDriver driver() { return d; }

    protected void irA(String ruta) { d.get(Config.baseUrl() + ruta); }

    public WebElement visible(String testId) { return espera.until(ExpectedConditions.visibilityOfElementLocated(tid(testId))); }

    public WebElement presente(String testId) { return espera.until(ExpectedConditions.presenceOfElementLocated(tid(testId))); }

    public boolean existe(String testId) { return !d.findElements(tid(testId)).isEmpty(); }

    public boolean apareceEn(String testId, int segundos) {
        try {
            new WebDriverWait(d, Duration.ofSeconds(segundos)).until(ExpectedConditions.visibilityOfElementLocated(tid(testId)));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public void desaparece(String testId) { espera.until(ExpectedConditions.invisibilityOfElementLocated(tid(testId))); }

    public List<WebElement> todos(String testId) { return d.findElements(tid(testId)); }

    /** Espera a que haya exactamente n elementos con ese data-testid. */
    public List<WebElement> esperarCantidad(String testId, int n) {
        return espera.until(ExpectedConditions.numberOfElementsToBe(tid(testId), n));
    }

    public List<WebElement> esperarAlMenos(String testId, int n) {
        return espera.until(ExpectedConditions.numberOfElementsToBeMoreThan(tid(testId), n - 1));
    }

    public String texto(String testId) { return visible(testId).getText().trim(); }

    public void esperarTexto(String testId, String contiene) {
        espera.until(ExpectedConditions.textToBePresentInElementLocated(tid(testId), contiene));
    }

    public void click(String testId) { click(espera.until(ExpectedConditions.elementToBeClickable(tid(testId)))); }

    public void click(WebElement el) {
        ((JavascriptExecutor) d).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try {
            espera.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (ElementClickInterceptedException e) {
            // un toast o la cabecera fija tapa el botón: se hace clic por JS sobre el mismo elemento
            ((JavascriptExecutor) d).executeScript("arguments[0].click();", el);
        }
    }

    public void escribir(String testId, String valor) { escribir(visible(testId), valor); }

    public void escribir(WebElement el, String valor) {
        ((JavascriptExecutor) d).executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].focus();", el);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            // un elemento fijo (cabecera/pie) tapa el campo: ya tiene el foco por JS, se escribe igual
        }
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if (!el.getAttribute("value").isEmpty()) el.clear();
        el.sendKeys(valor);
    }

    /** Para input type=date/time: fija el valor ISO y dispara input/change, igual que el selector nativo. */
    public void fijarValor(WebElement el, String valor) {
        ((JavascriptExecutor) d).executeScript(
                "const e=arguments[0]; const set=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;"
                        + "set.call(e, arguments[1]); e.dispatchEvent(new Event('input',{bubbles:true}));"
                        + "e.dispatchEvent(new Event('change',{bubbles:true})); e.dispatchEvent(new Event('blur'));", el, valor);
    }

    public void fijarValor(String testId, String valor) { fijarValor(visible(testId), valor); }

    public void seleccionar(String testId, String textoOValor) {
        Select s = new Select(visible(testId));
        try { s.selectByValue(textoOValor); } catch (Exception e) { s.selectByVisibleText(textoOValor); }
    }

    public WebElement dentro(SearchContext padre, String testId) { return padre.findElement(tid(testId)); }

    public WebElement porAtributo(String testId, String atributo, Object valor) {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='" + testId + "'][" + atributo + "='" + valor + "']")));
    }

    public void esperarUrl(String contiene) { espera.until(ExpectedConditions.urlContains(contiene)); }

    public void esperarAtributo(WebElement el, String atributo, String valor) {
        espera.until(ExpectedConditions.attributeToBe(el, atributo, valor));
    }

    public boolean habilitado(String testId) { return visible(testId).isEnabled(); }
}
