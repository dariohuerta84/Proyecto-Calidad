package com.trouvels360.movil.pantallas;

import com.trouvels360.movil.soporte.Config;
import com.trouvels360.movil.soporte.Datos;
import com.trouvels360.movil.soporte.Gestos;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Base de las pantallas (Screen Object). Localiza por resource-id (= testTag de Compose) y usa solo esperas explícitas.
 * Si un elemento está fuera de la vista, lo trae con UiScrollable antes de tocarlo.
 */
public abstract class BasePantalla {
    protected final AndroidDriver d;
    public final WebDriverWait espera;

    protected BasePantalla(AndroidDriver d) {
        this.d = d;
        this.espera = new WebDriverWait(d, Duration.ofSeconds(Config.entero("espera.segundos", 20)));
        this.espera.ignoring(StaleElementReferenceException.class);
    }

    public static By id(String tag) { return AppiumBy.id(tag); }

    /** Primer elemento cuyo resource-id empieza con el prefijo (p. ej. "hotel_card_"). */
    public static By prefijo(String p) { return By.xpath("//*[starts-with(@resource-id,'" + p + "')]"); }

    public WebElement visible(String tag) {
        try {
            return espera.until(ExpectedConditions.visibilityOfElementLocated(id(tag)));
        } catch (TimeoutException e) {
            return traer(tag);
        }
    }

    /** Desplaza hasta el elemento (listas largas y formularios). */
    public WebElement traer(String tag) {
        List<WebElement> ya = d.findElements(id(tag));
        if (!ya.isEmpty() && ya.get(0).isDisplayed()) return ya.get(0);
        try {
            Gestos.scrollHasta(d, tag);
            return d.findElement(id(tag));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("No apareció el elemento con resource-id '" + tag + "' ni desplazando la pantalla", e);
        }
    }

    public boolean existe(String tag) { return !d.findElements(id(tag)).isEmpty(); }

    public boolean apareceEn(String tag, int segundos) {
        try {
            new WebDriverWait(d, Duration.ofSeconds(segundos)).until(ExpectedConditions.presenceOfElementLocated(id(tag)));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void desaparece(String tag) { espera.until(ExpectedConditions.invisibilityOfElementLocated(id(tag))); }

    public void tocar(String tag) {
        visible(tag); // Desplaza la pantalla cuando el elemento está fuera de la vista.
        RemoteWebElement el = (RemoteWebElement) espera.until(ExpectedConditions.elementToBeClickable(id(tag)));
        d.executeScript("mobile: clickGesture", Map.of("elementId", el.getId()));
    }

    public void escribir(String tag, String valor) {
        WebElement el = visible(tag);
        el.click();
        el.clear();
        el.sendKeys(valor);
        ocultarTeclado();
    }

    private void ocultarTeclado() {
        espera.until(driver -> {
            try {
                if (!d.isKeyboardShown()) return true;
                d.hideKeyboard();
                return !d.isKeyboardShown();
            } catch (WebDriverException e) {
                String error = e.getMessage();
                if (!error.contains("Error finding softkeyboard") && !error.contains("The software keyboard cannot be hidden")) {
                    throw e;
                }
                return false;
            }
        });
    }

    public String texto(String tag) { return visible(tag).getText().trim(); }

    public void esperarTexto(String tag, String contiene) {
        espera.until(x -> existe(tag) && d.findElement(id(tag)).getText().contains(contiene));
    }

    public boolean habilitado(String tag) { return visible(tag).isEnabled(); }

    public void esperarDeshabilitado(String tag) { espera.until(x -> !habilitado(tag)); }

    public int cantidad(String prefijoId) { return d.findElements(prefijo(prefijoId)).size(); }

    /** Toca el botón de confirmación de un ConfirmDialog (tagId_si) y espera a que se cierre. */
    public void confirmar(String dialogo) {
        visible(dialogo);
        tocar(dialogo + "_si");
        desaparece(dialogo + "_si");
    }

    public void volverAtras(String dialogo) { tocar(dialogo + "_no"); desaparece(dialogo + "_no"); }

    /** Rango de fechas con el DateRangePicker en modo teclado ("Escribir las fechas"). */
    public void elegirRango(String campo, int diasEntrada, int diasSalida) {
        tocar(campo);
        String dlg = campo + "_dialogo";
        visible(dlg);
        tocar(dlg + "_teclado");
        List<WebElement> cajas = espera.until(x -> {
            List<WebElement> l = d.findElements(By.xpath("//*[@resource-id='" + dlg + "']//android.widget.EditText"));
            return l.size() >= 2 ? l : null;
        });
        cajas.get(0).click();
        cajas.get(0).clear();
        cajas.get(0).sendKeys(Datos.fechaTeclado(diasEntrada));
        cajas.get(1).click();
        cajas.get(1).clear();
        cajas.get(1).sendKeys(Datos.fechaTeclado(diasSalida));
        ocultarTeclado();
        tocar(dlg + "_aceptar");
        desaparece(dlg);
    }

    /** Una sola fecha (DateField) en modo teclado. */
    public void elegirFecha(String campo, int dias) {
        tocar(campo);
        String dlg = campo + "_dialogo";
        visible(dlg);
        tocar(dlg + "_teclado");
        WebElement caja = espera.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@resource-id='" + dlg + "']//android.widget.EditText")));
        caja.click();
        caja.clear();
        caja.sendKeys(Datos.fechaTeclado(dias));
        ocultarTeclado();
        tocar(dlg + "_aceptar");
        desaparece(dlg);
    }

    /** Suma con un Stepper ({tag}_mas / {tag}_valor) hasta llegar al valor pedido. */
    public void stepper(String tag, int valor) {
        while (Integer.parseInt(texto(tag + "_valor").replaceAll("\\D", "")) < valor) {
            String antes = texto(tag + "_valor");
            tocar(tag + "_mas");
            espera.until(x -> !texto(tag + "_valor").equals(antes));
        }
    }

    public void irA(String pestana) { tocar(pestana); }
}
