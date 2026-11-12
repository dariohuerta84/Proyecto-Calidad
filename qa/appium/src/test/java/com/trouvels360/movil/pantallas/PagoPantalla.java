package com.trouvels360.movil.pantallas;

import io.appium.java_client.android.AndroidDriver;

/** Diálogo de pago simulado (hoteles y tours). */
public class PagoPantalla extends BasePantalla {
    public PagoPantalla(AndroidDriver d) { super(d); visible("dialogo_pago"); }

    public PagoPantalla llenar(String titular, String numero, String vencimiento, String cvc) {
        escribir("pago_titular", titular);
        escribir("pago_numero", numero);
        escribir("pago_vencimiento", vencimiento);
        escribir("pago_cvc", cvc);
        return this;
    }

    public PagoPantalla tarjetaDePrueba() { return llenar("QA Appium", "4242424242424242", "12/39", "123"); }

    public void confirmar() { tocar("pago_confirmar"); }

    public void confirmarYEsperar() { confirmar(); desaparece("dialogo_pago"); }
}
