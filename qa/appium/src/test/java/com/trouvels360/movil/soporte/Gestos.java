package com.trouvels360.movil.soporte;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;

/** Gestos táctiles con W3C Actions (los TouchAction antiguos están deprecados en java-client 9). */
public final class Gestos {
    private Gestos() { }

    /** Desplaza la lista desplazable hasta que aparece el resource-id (UiScrollable del propio UiAutomator). */
    public static WebElement scrollHasta(AndroidDriver d, String resourceId) {
        return d.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).setMaxSearchSwipes(15)"
                        + ".scrollIntoView(new UiSelector().resourceId(\"" + resourceId + "\"))"));
    }

    /** Desliza el dedo de abajo hacia arriba (contenido sube) en el centro de la pantalla. */
    public static void deslizarArriba(AndroidDriver d) {
        Dimension t = d.manage().window().getSize();
        deslizar(d, new Point(t.width / 2, (int) (t.height * 0.75)), new Point(t.width / 2, (int) (t.height * 0.30)), 450);
    }

    /** Pull-to-refresh / volver arriba. */
    public static void deslizarAbajo(AndroidDriver d) {
        Dimension t = d.manage().window().getSize();
        deslizar(d, new Point(t.width / 2, (int) (t.height * 0.30)), new Point(t.width / 2, (int) (t.height * 0.75)), 450);
    }

    public static void deslizar(AndroidDriver d, Point desde, Point hasta, int ms) {
        PointerInput dedo = new PointerInput(PointerInput.Kind.TOUCH, "dedo");
        Sequence s = new Sequence(dedo, 1)
                .addAction(dedo.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), desde.x, desde.y))
                .addAction(dedo.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(dedo.createPointerMove(Duration.ofMillis(ms), PointerInput.Origin.viewport(), hasta.x, hasta.y))
                .addAction(dedo.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        d.perform(List.of(s));
    }

    /** Toque largo (p. ej. para menús contextuales). */
    public static void tocarLargo(AndroidDriver d, WebElement el) {
        Point c = el.getRect().getPoint().moveBy(el.getSize().width / 2, el.getSize().height / 2);
        PointerInput dedo = new PointerInput(PointerInput.Kind.TOUCH, "dedo");
        Sequence s = new Sequence(dedo, 1)
                .addAction(dedo.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), c.x, c.y))
                .addAction(dedo.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new org.openqa.selenium.interactions.Pause(dedo, Duration.ofMillis(900)))
                .addAction(dedo.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        d.perform(List.of(s));
    }
}
