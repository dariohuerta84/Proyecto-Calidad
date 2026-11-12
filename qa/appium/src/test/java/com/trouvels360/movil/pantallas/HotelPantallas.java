package com.trouvels360.movil.pantallas;

import io.appium.java_client.android.AndroidDriver;

/** Búsqueda de hoteles, detalle con habitaciones, itinerario y pago. */
public class HotelPantallas extends BasePantalla {
    public HotelPantallas(AndroidDriver d) { super(d); }

    public void buscar(String ciudad, int diaEntrada, int noches) {
        tocar("nav_hoteles");
        visible("pantalla_hoteles");
        escribir("buscar_ciudad", ciudad);
        elegirRango("buscar_fechas", diaEntrada, diaEntrada + noches);
        tocar("btn_buscar_hoteles");
    }

    /** Busca la ciudad única del hotel y abre su detalle. */
    public void abrirHotel(long hotelId, String ciudad, int diaEntrada, int noches) {
        buscar(ciudad, diaEntrada, noches);
        tocar("hotel_card_" + hotelId);
        visible("pantalla_detalle_hotel");
    }

    public void sumar(long habitacionId, int veces) {
        for (int i = 0; i < veces; i++) {
            String antes = texto("habitacion_seleccionadas_" + habitacionId);
            tocar("habitacion_mas_" + habitacionId);
            espera.until(x -> !texto("habitacion_seleccionadas_" + habitacionId).equals(antes));
        }
    }

    public void reservarSinItinerario() {
        tocar("btn_reservar_hotel");
        visible("dialogo_itinerario_pregunta");
        tocar("btn_sin_itinerario");
        visible("pantalla_pago_hotel");
        visible("resumen_total");
    }

    public PagoPantalla pagar() { tocar("btn_confirmar_pagar"); return new PagoPantalla(d); }
}
