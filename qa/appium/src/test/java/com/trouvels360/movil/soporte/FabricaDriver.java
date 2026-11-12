package com.trouvels360.movil.soporte;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

/** Crea la sesión de Appium (UiAutomator2) a partir de config.properties. */
public final class FabricaDriver {
    private FabricaDriver() { }

    public static AndroidDriver crear() {
        UiAutomator2Options o = new UiAutomator2Options()
                .setPlatformName(Config.get("plataforma"))
                .setDeviceName(Config.get("dispositivo"))
                .setAppPackage(Config.get("app.package"))
                .setAppActivity(Config.get("app.activity"))
                .setNoReset(Config.bool("no.reset"))
                .setNewCommandTimeout(Duration.ofSeconds(Config.entero("nuevo.comando.timeout", 120)))
                .setAutoGrantPermissions(true)
                // la app expone sus testTag como resource-id (testTagsAsResourceId); no hace falta el paquete
                .setDisableIdLocatorAutocompletion(true);
        if (!Config.get("udid").isEmpty()) o.setUdid(Config.get("udid"));
        if (!Config.get("version.android").isEmpty()) o.setPlatformVersion(Config.get("version.android"));
        if (!Config.get("app").isEmpty()) o.setApp(Path.of(Config.get("app")).toAbsolutePath().normalize().toString());
        try {
            AndroidDriver d = new AndroidDriver(URI.create(Config.get("appium.url")).toURL(), o);
            d.manage().timeouts().implicitlyWait(Duration.ZERO); // solo esperas explícitas
            return d;
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("appium.url inválida: " + Config.get("appium.url"), e);
        }
    }
}
