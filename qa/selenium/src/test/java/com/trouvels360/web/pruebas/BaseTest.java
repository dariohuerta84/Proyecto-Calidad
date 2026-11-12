package com.trouvels360.web.pruebas;

import com.trouvels360.web.soporte.Evidencias;
import com.trouvels360.web.soporte.FabricaNavegador;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

/** Un navegador nuevo por prueba (independientes entre sí); evidencias y reporte por la extensión Evidencias. */
@ExtendWith(Evidencias.class)
public abstract class BaseTest implements Evidencias.ConNavegadores {
    protected WebDriver d;
    private final List<WebDriver> abiertos = new ArrayList<>();

    @BeforeEach
    void abrirNavegador() { d = nuevoNavegador(); }

    /** Segundo navegador (por ejemplo, proveedor y viajero a la vez en TC-WEB-013). */
    protected WebDriver nuevoNavegador() {
        WebDriver x = FabricaNavegador.crear();
        abiertos.add(x);
        return x;
    }

    @AfterEach
    void cerrarNavegadores() {
        for (WebDriver x : abiertos) {
            try { x.quit(); } catch (Exception ignored) { }
        }
        abiertos.clear();
    }

    @Override
    public List<WebDriver> navegadores() { return abiertos; }

    protected static void paso(String descripcion) { Allure.step(descripcion); }
}
