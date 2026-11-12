package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

public class PagoTourPage extends BasePage {
    public PagoTourPage(WebDriver d) { super(d); }

    public String personas() { return texto("pago-personas"); }
    public String subtotal() { return texto("resumen-subtotal"); }
    public String impuesto() { return texto("resumen-impuesto"); }
    public String total() { return texto("resumen-total"); }

    public PagoDialog abrirPago() { click("btn-confirmar-pagar"); return new PagoDialog(d); }
}
