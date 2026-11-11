package com.trouvels360.app.ui.screens.hotels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.Session
import com.trouvels360.app.data.remote.HabitacionDto
import com.trouvels360.app.data.remote.HotelDetailResponse
import com.trouvels360.app.data.remote.ItemHabitacion
import com.trouvels360.app.data.remote.ItinerarioData
import com.trouvels360.app.ui.BusquedaHotel
import com.trouvels360.app.ui.CATEGORIAS_TOUR
import com.trouvels360.app.ui.Carrito
import com.trouvels360.app.ui.SeleccionHotel
import com.trouvels360.app.ui.components.AppDialog
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.CalificacionTexto
import com.trouvels360.app.ui.components.DateRangeField
import com.trouvels360.app.ui.components.ErrorState
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.NetImage
import com.trouvels360.app.ui.components.ReviewsSection
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.Stars
import com.trouvels360.app.ui.components.Stepper
import com.trouvels360.app.ui.components.fechaConDia
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit

class HotelDetailViewModel : ViewModel() {
    var detail by mutableStateOf<HotelDetailResponse?>(null); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    /** Habitaciones con unidades libres para las fechas (null = sin fechas, se muestran todas). */
    var disponibles by mutableStateOf<List<HabitacionDto>?>(null); private set
    var checking by mutableStateOf(false); private set
    var errorDisponibilidad by mutableStateOf<String?>(null); private set

    /** habitacion_id → cantidad elegida. */
    val seleccion = mutableStateMapOf<Int, Int>()
    var aviso by mutableStateOf<String?>(null)

    // Itinerario sugerido (TRV-404)
    var preguntarItinerario by mutableStateOf(false)
    var interes by mutableStateOf("Cultura")
    var generando by mutableStateOf(false); private set
    var errorItinerario by mutableStateOf<String?>(null); private set
    var itinerario by mutableStateOf<ItinerarioData?>(null)

    private var loadedId: Int? = null

    fun load(id: Int) {
        if (loadedId == id) return
        loadedId = id
        viewModelScope.launch {
            loading = true; error = null
            when (val r = Repository.hotel(id)) {
                is Result.Ok -> { detail = r.value; consultarDisponibilidad() }
                is Result.Err -> error = r.message
            }
            loading = false
        }
    }

    val noches: Long
        get() {
            val a = BusquedaHotel.checkIn ?: return 0
            val b = BusquedaHotel.checkOut ?: return 0
            return ChronoUnit.DAYS.between(a, b)
        }

    /** TRV-202: unidades libres reales para las fechas (se recalcula al cambiarlas). */
    fun consultarDisponibilidad() {
        val id = loadedId ?: return
        val b = BusquedaHotel
        val ci = b.checkIn
        val co = b.checkOut
        seleccion.clear()
        if (ci == null || co == null || !co.isAfter(ci)) { disponibles = null; return }
        viewModelScope.launch {
            checking = true; errorDisponibilidad = null
            when (val r = Repository.disponibilidad(id, ci.toString(), co.toString(), b.adultos, b.ninos, b.habitaciones)) {
                is Result.Ok -> disponibles = r.value
                is Result.Err -> { errorDisponibilidad = r.message; disponibles = null }
            }
            checking = false
        }
    }

    fun cambiar(h: HabitacionDto, delta: Int) {
        val id = h.id ?: return
        val nuevo = ((seleccion[id] ?: 0) + delta).coerceIn(0, h.maxSeleccionable)
        if (nuevo == 0) seleccion.remove(id) else seleccion[id] = nuevo
        aviso = null
    }

    val totalUnidades: Int get() = seleccion.values.sum()

    private fun capacidadSeleccionada(): Int {
        val rooms = disponibles ?: detail?.habitaciones.orEmpty()
        return seleccion.entries.sumOf { (id, n) ->
            val h = rooms.firstOrNull { it.id == id }
            ((h?.capacidadAdultos ?: 0) + (h?.capacidadNinos ?: 0)) * n
        }
    }

    /** Comprueba fechas, selección y capacidad antes de seguir al pago. Devuelve un mensaje si falta algo. */
    fun validarParaReservar(): String? {
        val b = BusquedaHotel
        return when {
            !b.conFechas -> "Elige las fechas de entrada y salida."
            disponibles == null -> "Consultando disponibilidad… vuelve a intentarlo."
            totalUnidades == 0 -> "Selecciona al menos una habitación."
            capacidadSeleccionada() < b.adultos + b.ninos ->
                "Las habitaciones elegidas no alcanzan para ${b.adultos + b.ninos} huéspedes. Agrega otra."
            else -> null
        }
    }

    fun armarSeleccion(): SeleccionHotel? {
        val d = detail ?: return null
        val b = BusquedaHotel
        val ci = b.checkIn ?: return null
        val co = b.checkOut ?: return null
        val rooms = disponibles ?: d.habitaciones.orEmpty()
        return SeleccionHotel(
            hotelId = loadedId ?: return null,
            hotelNombre = d.hotel?.nombre ?: "Hotel",
            ciudad = d.hotel?.ciudad ?: "",
            checkIn = ci, checkOut = co, adultos = b.adultos, ninos = b.ninos,
            items = seleccion.map { (id, n) -> ItemHabitacion(id, n) },
            nombres = rooms.associate { (it.id ?: 0) to (it.nombre ?: "Habitación") },
        )
    }

    fun generarItinerario(onListo: () -> Unit) {
        val s = armarSeleccion() ?: return
        viewModelScope.launch {
            generando = true; errorItinerario = null
            when (val r = Repository.itinerario(s.ciudad, s.hotelId, s.checkIn.toString(), s.checkOut.toString(), interes)) {
                is Result.Ok -> { itinerario = r.value.data; preguntarItinerario = false; onListo() }
                is Result.Err -> errorItinerario = r.message
            }
            generando = false
        }
    }
}

/**
 * TRV-202 Detalle del hotel con disponibilidad por fechas y selección de habitaciones.
 * TRV-404 Antes de pagar ofrece (opcional) un itinerario sugerido que no se cobra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    id: Int,
    onLogin: () -> Unit,
    onCheckout: () -> Unit,
    onVerTour: (Int) -> Unit,
    vm: HotelDetailViewModel = viewModel(key = "hotel_$id"),
) {
    LaunchedEffect(id) { vm.load(id) }
    val user by Session.user.collectAsState()
    var verItinerario by androidx.compose.runtime.remember { mutableStateOf(false) }

    when {
        vm.loading -> Loading()
        vm.error != null -> ErrorState(vm.error!!)
        else -> {
            val d = vm.detail ?: return
            val h = d.hotel
            val gallery = (listOfNotNull(h?.imagenUrl) + h?.imagenes.orEmpty().mapNotNull { it.url }).distinct()
            val b = BusquedaHotel
            val rooms = vm.disponibles ?: d.habitaciones.orEmpty()

            LazyColumn(
                Modifier.fillMaxSize().tag("pantalla_detalle_hotel"),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(gallery.ifEmpty { listOf("") }) { url ->
                            NetImage(url, Modifier.width(300.dp).height(190.dp).clip(RoundedCornerShape(16.dp)))
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(h?.nombre ?: "Hotel", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.tag("hotel_nombre"))
                        Spacer(Modifier.height(4.dp))
                        Stars(h?.estrellas)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, Modifier.size(16.dp))
                            Text(" " + listOfNotNull(h?.direccion, h?.ciudad, h?.pais).joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(4.dp))
                        CalificacionTexto(h?.calificacion?.promedio, h?.calificacion?.cantidad)
                        val desc = h?.descripcion
                        if (!desc.isNullOrBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(desc, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // ---------- Fechas y huéspedes
                item {
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle("Tu estadía")
                            DateRangeField(b.checkIn, b.checkOut, { a, c -> b.checkIn = a; b.checkOut = c; vm.consultarDisponibilidad() }, tagId = "detalle_fechas")
                            Stepper("Adultos", b.adultos, 1, 10, "detalle_adultos") { b.adultos = it; vm.consultarDisponibilidad() }
                            Stepper("Niños", b.ninos, 0, 10, "detalle_ninos") { b.ninos = it; vm.consultarDisponibilidad() }
                            if (vm.checking) Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text("  Consultando disponibilidad…", modifier = Modifier.tag("consultando_disponibilidad"))
                            }
                            vm.errorDisponibilidad?.let { Banner(it, error = true, tagId = "error_disponibilidad") }
                            if (!b.conFechas) Text("Elige tus fechas para ver las unidades libres y reservar.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    SectionTitle(
                        if (vm.disponibles != null) "Habitaciones disponibles (${vm.noches} ${if (vm.noches == 1L) "noche" else "noches"})" else "Habitaciones",
                        Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (rooms.isEmpty()) {
                    item {
                        Text(
                            if (vm.disponibles != null) "No hay habitaciones libres para esas fechas y huéspedes." else "Este hotel aún no tiene habitaciones.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp).tag("sin_habitaciones"),
                        )
                    }
                }
                items(rooms, key = { it.id ?: it.hashCode() }) { hab ->
                    RoomCard(
                        hab = hab,
                        seleccionadas = vm.seleccion[hab.id ?: -1] ?: 0,
                        habilitado = vm.disponibles != null,
                        onMenos = { vm.cambiar(hab, -1) },
                        onMas = { vm.cambiar(hab, 1) },
                    )
                }

                item {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        vm.aviso?.let { Banner(it, error = true, tagId = "aviso_reserva") }
                        Button(
                            onClick = {
                                val problema = vm.validarParaReservar()
                                when {
                                    problema != null -> vm.aviso = problema
                                    user == null -> onLogin()
                                    user?.esViajero != true -> vm.aviso = "Solo las cuentas de viajero pueden reservar."
                                    else -> vm.preguntarItinerario = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).tag("btn_reservar_hotel"),
                        ) { Text(if (vm.totalUnidades > 0) "Reservar ${vm.totalUnidades} habitación(es)" else "Reservar") }
                    }
                }

                item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }
                item { ReviewsSection(servicioId = id, onLogin = onLogin) }
            }
        }
    }

    // ---------- ¿Itinerario sugerido? (opcional, no se cobra)
    if (vm.preguntarItinerario) {
        AppDialog(
            title = "¿Quieres un itinerario sugerido?",
            onDismiss = { if (!vm.generando) vm.preguntarItinerario = false },
            tagId = "dialogo_itinerario_pregunta",
            buttons = {
                TextButton(
                    onClick = {
                        vm.itinerario = null
                        vm.preguntarItinerario = false
                        Carrito.hotel = vm.armarSeleccion()
                        onCheckout()
                    },
                    enabled = !vm.generando,
                    modifier = Modifier.tag("btn_sin_itinerario"),
                ) { Text("No, ir al pago") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { vm.generarItinerario { verItinerario = true } },
                    enabled = !vm.generando,
                    modifier = Modifier.tag("btn_generar_itinerario"),
                ) {
                    if (vm.generando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Generar")
                }
            },
        ) {
            Text("Te proponemos tours con salidas durante tu estadía. Es opcional: no se reserva nada ni se suma a tu pago.")
            Spacer(Modifier.height(8.dp))
            Text("¿Qué tipo de viaje prefieres?", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CATEGORIAS_TOUR.take(2).forEach { c ->
                    FilterChip(selected = vm.interes == c, onClick = { vm.interes = c }, label = { Text(c) }, modifier = Modifier.tag("interes_$c"))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CATEGORIAS_TOUR.drop(2).forEach { c ->
                    FilterChip(selected = vm.interes == c, onClick = { vm.interes = c }, label = { Text(c) }, modifier = Modifier.tag("interes_$c"))
                }
            }
            vm.errorItinerario?.let { Banner(it, error = true, tagId = "itinerario_error") }
        }
    }

    // ---------- Resultado del itinerario
    val itin = vm.itinerario
    if (verItinerario && itin != null) {
        AppDialog(
            title = "Itinerario sugerido (${vm.interes})",
            onDismiss = { verItinerario = false },
            tagId = "dialogo_itinerario",
            buttons = {
                Button(
                    onClick = { verItinerario = false; Carrito.hotel = vm.armarSeleccion(); onCheckout() },
                    modifier = Modifier.tag("btn_itinerario_continuar"),
                ) { Text("Continuar al pago del hotel") }
            },
        ) {
            Text(
                "${itin.resumen?.totalTours ?: 0} actividades · costo estimado ${formatPrice(itin.resumen?.costoTotal)} (no incluido en tu pago)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.tag("itinerario_resumen"),
            )
            Spacer(Modifier.height(8.dp))
            itin.itinerario.orEmpty().forEach { dia ->
                Text("Día ${dia.dia}" + (dia.fechaSugerida?.let { f -> " · ${fechaConDia(f)}" } ?: ""), fontWeight = FontWeight.SemiBold)
                val acts = dia.actividades.orEmpty()
                if (acts.isEmpty()) Text("Día libre para disfrutar del hotel.", style = MaterialTheme.typography.bodySmall)
                acts.forEach { a ->
                    Column(Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp).tag("itinerario_actividad")) {
                        Text("${a.nombre} · ${formatPrice(a.precio)}")
                        Text(
                            "Sale a las ${a.salida?.hora ?: "—"} · ${a.salida?.cupos ?: 0} cupos · ${a.duracionHoras ?: 0.0} h · a ${a.distanciaKm ?: 0.0} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        a.tourId?.let { tid ->
                            TextButton(onClick = { verItinerario = false; onVerTour(tid) }, modifier = Modifier.tag("itinerario_ver_tour_$tid")) { Text("Ver tour") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomCard(hab: HabitacionDto, seleccionadas: Int, habilitado: Boolean, onMenos: () -> Unit, onMas: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp).tag("habitacion_card_${hab.id}"), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(hab.nombre ?: "Habitación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.tag("habitacion_nombre"))
                    Text("Hasta ${hab.capacidadAdultos ?: 0} adultos y ${hab.capacidadNinos ?: 0} niños", style = MaterialTheme.typography.bodySmall)
                    Text(
                        if (habilitado) "${hab.maxSeleccionable} unidades libres" else "${hab.unidadesTotales ?: hab.cantidad ?: 0} unidades",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.tag("habitacion_unidades"),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatPrice(hab.precioPorNoche), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.tag("habitacion_precio"))
                    Text("/ noche + IGV", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (habilitado) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad", modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = onMenos, enabled = seleccionadas > 0, modifier = Modifier.tag("habitacion_menos_${hab.id}")) { Text("−") }
                    Text("$seleccionadas", modifier = Modifier.padding(horizontal = 14.dp).tag("habitacion_seleccionadas_${hab.id}"), fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = onMas, enabled = seleccionadas < hab.maxSeleccionable, modifier = Modifier.tag("habitacion_mas_${hab.id}")) { Text("+") }
                }
            }
        }
    }
}
