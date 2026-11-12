package com.trouvels360.web.pruebas;

import com.trouvels360.web.paginas.BuscadorPage;
import com.trouvels360.web.paginas.LoginPage;
import com.trouvels360.web.paginas.Navegacion;
import com.trouvels360.web.paginas.ReservasRecibidasPage;
import com.trouvels360.web.paginas.ResultadosPage;
import com.trouvels360.web.soporte.Api;
import com.trouvels360.web.soporte.Datos;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Criterios que no tenían caso en la etapa 1 (agregados en la etapa 5). */
@Epic("Cobertura adicional")
@Feature("Navegación, estados vacío/error y sesión vencida")
class CoberturaAdicionalTest extends BaseTest {

    @Test
    @Tag("HU-V05") @Tag("HU-V08") @Tag("TC-WEB-031")
    @DisplayName("TC-WEB-031 Volver desde el detalle conserva la búsqueda (hoteles y tours)")
    void conservarBusqueda() {
        int dia = Datos.diaLibre();
        new BuscadorPage(d).hoteles().destinoHotel("Cusco").fechas(Datos.fecha(dia), Datos.fecha(dia + 2)).buscar();
        ResultadosPage r = new ResultadosPage(d);
        r.esperarUrl("/hoteles/resultados");
        String url = d.getCurrentUrl();
        List<String> antes = r.textos("resultado-nombre");
        r.verDetalle(0);
        r.visible("hotel-detalle");
        paso("Atrás desde el detalle del hotel");
        d.navigate().back();
        r.esperarUrl("/hoteles/resultados");
        assertEquals(url, d.getCurrentUrl(), "Mismos filtros en la URL");
        assertEquals(antes, r.textos("resultado-nombre"), "Mismos resultados");

        paso("Lo mismo en tours (CA-V08-04)");
        new BuscadorPage(d).tours().destinoTour("Cusco").categoria("Aventura").fechas(Datos.fecha(1), null).buscar();
        r.esperarUrl("/tour/resultados");
        String urlTours = d.getCurrentUrl();
        List<String> tours = r.textos("resultado-nombre");
        r.verDetalle(0);
        r.esperarUrl("/tour/detalle/");
        d.navigate().back();
        r.esperarUrl("/tour/resultados");
        assertEquals(urlTours, d.getCurrentUrl());
        assertEquals(tours, r.textos("resultado-nombre"));
    }

    @Test
    @Tag("HU-V08") @Tag("HU-P12") @Tag("TC-WEB-032")
    @DisplayName("TC-WEB-032 Sin resultados se distingue de un error con Reintentar (tours y bandeja del proveedor)")
    void vacioVersusError() {
        Navegacion nav = new Navegacion(d);
        paso("Destino sin tours: mensaje de vacío, no de error");
        nav.ir("/tour/resultados?destino=NoExiste" + Datos.sufijo() + "&checkIn=" + Datos.fecha(1));
        nav.visible("tours-vacio");
        assertTrue(nav.todos("tours-error").isEmpty());
        paso("Filtro rechazado por la API: error con Reintentar, no 'sin resultados'");
        nav.ir("/tour/resultados?destino=Cusco&checkIn=" + Datos.fecha(-5));
        assertTrue(nav.texto("tours-error").contains("anterior a hoy"), nav.texto("tours-error"));
        assertTrue(nav.todos("tours-vacio").isEmpty());
        assertTrue(nav.visible("btn-reintentar").isDisplayed());
        paso("Proveedor sin reservas: bandeja vacía (CA-P12-04)");
        nav.entrarComo(Api.registrarProveedor(), "/proveedor");
        ReservasRecibidasPage rr = new ReservasRecibidasPage(d).abrir(null);
        assertTrue(rr.visible("rr-vacio").isDisplayed());
        assertTrue(rr.todos("rr-error").isEmpty());
    }

    @Test
    @Tag("HU-V02") @Tag("TC-WEB-033")
    @DisplayName("TC-WEB-033 Sesión vencida: la API responde 401, se pide iniciar sesión y se vuelve a la página")
    void sesionVencida() {
        Api.Sesion v = Api.registrarViajero("Vera");
        Map<String, Object> r = Api.reservarHotel(v, Api.crearHotel(Api.proveedor(), "Doble", 2, 150).hab("Doble"), Datos.diaLibre(), 1, 1);
        Navegacion nav = new Navegacion(d).entrarComo(v, "/hoteles");
        paso("El token se invalida en el servidor (como si hubiera vencido)");
        v.post("/auth/logout", Map.of()).esperar(200);
        nav.ir("/mis-reservas");
        nav.esperarUrl("/auth/login");
        assertTrue(d.getCurrentUrl().contains("expirada") || d.getCurrentUrl().contains("returnUrl"), d.getCurrentUrl());
        assertTrue(nav.existe("link-login"), "La sesión local se limpió");
        paso("Inicia sesión y vuelve a Mis reservas");
        new LoginPage(d).ingresar(v.email(), "Clave123");
        nav.esperarUrl("/mis-reservas");
        nav.visible("reserva-card");
        assertEquals(r.get("codigo"), nav.todos("reserva-card").get(0).getAttribute("data-codigo"));
    }
}
