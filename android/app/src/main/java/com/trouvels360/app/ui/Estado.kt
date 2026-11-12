package com.trouvels360.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.trouvels360.app.data.remote.ItemHabitacion
import java.time.LocalDate

/**
 * Criterios de búsqueda de hoteles compartidos entre la lista, el detalle y el pago
 * (equivalente a los query params de la web).
 */
object BusquedaHotel {
    var ciudad by mutableStateOf("")
    var checkIn by mutableStateOf<LocalDate?>(null)
    var checkOut by mutableStateOf<LocalDate?>(null)
    var adultos by mutableIntStateOf(2)
    var ninos by mutableIntStateOf(0)
    var habitaciones by mutableIntStateOf(1)

    val conFechas: Boolean get() = checkIn != null && checkOut != null && checkOut!!.isAfter(checkIn)
}

object BusquedaTour {
    var destino by mutableStateOf("")
    var categoria by mutableStateOf<String?>(null)
    var desde by mutableStateOf<LocalDate?>(null)
    var hasta by mutableStateOf<LocalDate?>(null)
    var personas by mutableIntStateOf(1)
}

/** Selección de habitaciones que pasa del detalle del hotel al pago (HU-V06 → HU-V07). */
data class SeleccionHotel(
    val hotelId: Int,
    val hotelNombre: String,
    val ciudad: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val adultos: Int,
    val ninos: Int,
    val items: List<ItemHabitacion>,
    val nombres: Map<Int, String>,
)

/** Salida y personas elegidas en el detalle del tour (HU-V09 → HU-V10). */
data class SeleccionTour(
    val tourId: Int,
    val tourNombre: String,
    val salidaId: Int,
    val fecha: String,
    val hora: String,
    val personas: Int,
)

object Carrito {
    var hotel by mutableStateOf<SeleccionHotel?>(null)
    var tour by mutableStateOf<SeleccionTour?>(null)
}

val CATEGORIAS_TOUR = listOf("Aventura", "Cultura", "Gastronomía", "Relajación")
