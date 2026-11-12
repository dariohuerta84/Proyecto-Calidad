package com.trouvels360.movil.pantallas;

import io.appium.java_client.android.AndroidDriver;

public class ReservasPantalla extends BasePantalla {
    public ReservasPantalla(AndroidDriver d) { super(d); }

    public ReservasPantalla abrir() { tocar("nav_reservas"); visible("pantalla_mis_reservas"); return this; }

    public String estado(String codigo) { return texto("reserva_estado_" + codigo).toLowerCase(); }

    public void cancelar(String codigo) {
        tocar("btn_cancelar_" + codigo);
        confirmar("dialogo_cancelar_reserva");
        visible("reservas_aviso");
    }
}
