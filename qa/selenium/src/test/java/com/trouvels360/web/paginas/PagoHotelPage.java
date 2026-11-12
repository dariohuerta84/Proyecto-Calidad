package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

public class PagoHotelPage extends BasePage {
    public PagoHotelPage(WebDriver d) { super(d); }

    public PagoHotelPage listo() { visible("pago-hotel"); visible("resumen-total"); return this; }

    public int lineas() { return esperarAlMenos("pago-linea", 1).size(); }

    public String subtotal() { return texto("resumen-subtotal"); }
    public String impuesto() { return texto("resumen-impuesto"); }
    public String total() { return texto("resumen-total"); }

    public PagoDialog abrirPago() { click("btn-confirmar-pagar"); return new PagoDialog(d); }

    public void comprobante() { visible("comprobante-hotel"); }
}
