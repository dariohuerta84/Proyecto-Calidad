package com.trouvels360.movil.pantallas;

import io.appium.java_client.android.AndroidDriver;

public class TourPantallas extends BasePantalla {
    public TourPantallas(AndroidDriver d) { super(d); }

    public void buscar(String destino, String categoria) {
        tocar("nav_tours");
        visible("pantalla_tours");
        escribir("buscar_destino_tour", destino);
        tocar(categoria == null ? "categoria_todas" : "categoria_" + categoria);
        tocar("btn_buscar_tours");
    }

    public void abrirTour(long tourId, String ciudad) {
        buscar(ciudad, null);
        tocar("tour_card_" + tourId);
        visible("pantalla_detalle_tour");
    }

    public void elegirSalida(long salidaId) { tocar("salida_card_" + salidaId); visible("tour_personas_mas"); }

    public void reservar() { tocar("btn_reservar_tour"); visible("pantalla_pago_tour"); visible("resumen_total"); }

    public PagoPantalla pagar() { tocar("btn_confirmar_pagar"); return new PagoPantalla(d); }
}
