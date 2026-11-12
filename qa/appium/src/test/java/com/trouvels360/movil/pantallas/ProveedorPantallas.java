package com.trouvels360.movil.pantallas;

import io.appium.java_client.android.AndroidDriver;

/** Panel, reservas recibidas, formularios de hotel/tour y programa (salidas y actividades). */
public class ProveedorPantallas extends BasePantalla {
    public ProveedorPantallas(AndroidDriver d) { super(d); }

    public void panel() { tocar("nav_panel"); visible("pantalla_panel_proveedor"); }

    public void recibidas() { tocar("nav_recibidas"); visible("pantalla_reservas_recibidas"); }

    public void filtrarPublicacion(String nombre) {
        escribir("filtro_busqueda", nombre);
        tocar("btn_filtrar");
    }

    // ---------------- hotel
    public void datosHotel(String nombre, String ciudad) {
        visible("pantalla_form_hotel");
        escribir("hotel_nombre", nombre);
        escribir("hotel_descripcion", "Hotel publicado desde la app (Appium).");
        escribir("hotel_direccion", "Av. Móvil 45");
        escribir("hotel_ciudad", ciudad);
        escribir("hotel_pais", "Perú");
        escribir("hotel_imagen_url", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800");
    }

    /** Campos de un tipo de habitación: sufijo "_0", "_1"... en el alta; "" en el diálogo de edición. */
    public void habitacion(String sufijo, String nombre, int adultos, int unidades, int precio) {
        escribir("hab_nombre" + sufijo, nombre);
        escribir("hab_adultos" + sufijo, String.valueOf(adultos));
        escribir("hab_ninos" + sufijo, "0");
        escribir("hab_cantidad" + sufijo, String.valueOf(unidades));
        escribir("hab_precio" + sufijo, String.valueOf(precio));
    }

    // ---------------- tour
    public void datosTour(String nombre, String ciudad, String categoria, int precio, int duracion) {
        visible("pantalla_form_tour");
        escribir("tour_nombre", nombre);
        escribir("tour_descripcion", "Tour publicado desde la app (Appium).");
        escribir("tour_ciudad", ciudad);
        escribir("tour_pais", "Perú");
        escribir("tour_imagen_url", "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800");
        tocar("tour_categoria_" + categoria);
        escribir("tour_duracion", String.valueOf(duracion));
        escribir("tour_precio", String.valueOf(precio));
    }

    public void salidaDelFormulario(int i, int dias, String hora, int cupo) {
        elegirFecha("salida_fecha_" + i, dias);
        escribir("salida_hora_" + i, hora);
        escribir("salida_cupo_" + i, String.valueOf(cupo));
    }

    // ---------------- programa
    public void programa(long tourId) { panel(); tocar("btn_programa_" + tourId); visible("pantalla_programa_tour"); }

    public void nuevaSalida(int dias, String hora, int cupo) {
        tocar("btn_nueva_salida");
        visible("dialogo_salida");
        elegirFecha("salida_form_fecha", dias);
        escribir("salida_form_hora", hora);
        escribir("salida_form_cupo", String.valueOf(cupo));
        tocar("salida_form_guardar");
        desaparece("dialogo_salida");
    }

    public void nuevaActividad(String titulo, int duracion) {
        tocar("btn_nueva_actividad");
        visible("dialogo_actividad");
        escribir("actividad_form_titulo", titulo);
        escribir("actividad_form_duracion", String.valueOf(duracion));
        tocar("actividad_form_guardar");
        desaparece("dialogo_actividad");
    }
}
