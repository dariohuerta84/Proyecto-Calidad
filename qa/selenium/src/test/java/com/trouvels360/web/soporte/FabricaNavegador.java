package com.trouvels360.web.soporte;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.Map;

/** Crea el navegador según config (chrome por defecto, headless en CI). Sin esperas implícitas: solo explícitas. */
public final class FabricaNavegador {
    private FabricaNavegador() { }

    public static WebDriver crear() {
        boolean headless = Config.bool("headless");
        String[] tam = Config.get("ventana").split(",");
        WebDriver d;
        switch (Config.get("navegador").toLowerCase()) {
            case "firefox" -> {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) o.addArguments("-headless");
                d = new FirefoxDriver(o);
            }
            case "edge" -> {
                EdgeOptions o = new EdgeOptions();
                if (headless) o.addArguments("--headless=new");
                d = new EdgeDriver(o);
            }
            default -> {
                if (!Config.get("chromedriver").isEmpty()) System.setProperty("webdriver.chrome.driver", Config.get("chromedriver"));
                ChromeOptions o = new ChromeOptions();
                if (!Config.get("chrome.binario").isEmpty()) o.setBinary(Config.get("chrome.binario"));
                if (headless) o.addArguments("--headless=new");
                o.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--lang=es-PE", "--disable-search-engine-choice-screen");
                o.setExperimentalOption("prefs", Map.of("credentials_enable_service", false, "profile.password_manager_enabled", false));
                d = new ChromeDriver(o);
            }
        }
        d.manage().timeouts().implicitlyWait(Duration.ZERO);
        d.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        d.manage().window().setSize(new Dimension(Integer.parseInt(tam[0].trim()), Integer.parseInt(tam[1].trim())));
        return d;
    }
}
