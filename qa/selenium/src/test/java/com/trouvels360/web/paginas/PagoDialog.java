package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

/** Diálogo de pago simulado compartido por hoteles y tours. */
public class PagoDialog extends BasePage {
    public PagoDialog(WebDriver d) { super(d); visible("modal-pago"); }

    public PagoDialog llenar(String titular, String numero, String vencimiento, String cvc) {
        escribir("pago-titular", titular);
        escribir("pago-numero", numero);
        escribir("pago-vencimiento", vencimiento);
        escribir("pago-cvc", cvc);
        return this;
    }

    public PagoDialog tarjetaDePrueba() { return llenar("QA Selenium", "4242 4242 4242 4242", "12/39", "123"); }

    public void confirmar() { click("pago-confirmar"); }

    public void cancelar() { click("pago-cancelar"); desaparece("modal-pago"); }

    public String error(String campo) { return texto("pago-error-" + campo); }

    public String total() { return texto("pago-total"); }
}
