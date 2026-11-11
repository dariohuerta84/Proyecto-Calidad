package com.trouvels360.app.ui.screens.tours

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.trouvels360.app.data.remote.SalidaDto
import com.trouvels360.app.data.remote.TourServicioDto
import com.trouvels360.app.ui.BusquedaTour
import com.trouvels360.app.ui.Carrito
import com.trouvels360.app.ui.SeleccionTour
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.ErrorState
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.NetImage
import com.trouvels360.app.ui.components.ReviewsSection
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.Stepper
import com.trouvels360.app.ui.components.fechaConDia
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch

class TourDetailViewModel : ViewModel() {
    var tour by mutableStateOf<TourServicioDto?>(null); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set
    var salidaId by mutableStateOf<Int?>(null)
    var personas by mutableIntStateOf(BusquedaTour.personas)
    var aviso by mutableStateOf<String?>(null)
    private var loadedId: Int? = null

    fun load(id: Int) {
        if (loadedId == id) return
        loadedId = id
        viewModelScope.launch {
            loading = true; error = null
            when (val r = Repository.tour(id)) {
                is Result.Ok -> tour = r.value
                is Result.Err -> error = r.message
            }
            loading = false
        }
    }

    /** Solo salidas programadas, futuras y con cupo (TRV-302). */
    val salidasReservables: List<SalidaDto>
        get() {
            val hoy = java.time.LocalDate.now().toString()
            return tour?.salidas.orEmpty().filter { (it.estado ?: "programada") == "programada" && (it.fecha ?: "") >= hoy }
        }

    val salida: SalidaDto? get() = salidasReservables.firstOrNull { it.id == salidaId }

    fun elegir(s: SalidaDto) {
        if (s.libres <= 0) { aviso = "Esta salida no tiene cupos disponibles."; return }
        salidaId = s.id; aviso = null
        if (personas > s.libres) personas = s.libres
    }
}

/** TRV-302 — Detalle del tour: actividades, salidas con cupos y elección de participantes. */
@Composable
fun TourDetailScreen(id: Int, onLogin: () -> Unit, onCheckout: () -> Unit, vm: TourDetailViewModel = viewModel(key = "tour_$id")) {
    LaunchedEffect(id) { vm.load(id) }
    val user by Session.user.collectAsState()

    when {
        vm.loading -> Loading()
        vm.error != null -> ErrorState(vm.error!!)
        else -> {
            val t = vm.tour ?: return
            val precio = t.tour?.precio ?: 0.0
            LazyColumn(Modifier.fillMaxSize().tag("pantalla_detalle_tour"), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { NetImage(t.imagenUrl, Modifier.fillMaxWidth().height(210.dp)) }
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(t.nombre ?: "Tour", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.tag("tour_nombre"))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, Modifier.size(16.dp))
                            Text(" " + listOfNotNull(t.ciudad, t.pais).joinToString(", ") + (t.tour?.categoria?.let { " · $it" } ?: ""))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("${formatPrice(precio)} por persona + IGV 18 %", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.tag("tour_precio"))
                        val desc = t.descripcion
                        if (!desc.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(desc) }
                    }
                }
                val acts = t.actividades.orEmpty().sortedBy { it.orden ?: 0 }
                if (acts.isNotEmpty()) {
                    item { SectionTitle("Qué incluye", Modifier.padding(horizontal = 16.dp)) }
                    items(acts, key = { "act_${it.id}" }) { a ->
                        Row(Modifier.padding(horizontal = 16.dp).tag("actividad_${a.orden}"), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("  ${a.titulo}" + (a.duracionMin?.let { " · $it min" } ?: ""))
                        }
                    }
                }

                item { SectionTitle("Elige tu salida", Modifier.padding(horizontal = 16.dp)) }
                val salidas = vm.salidasReservables
                if (salidas.isEmpty()) {
                    item { Text("No hay salidas programadas por ahora.", modifier = Modifier.padding(horizontal = 16.dp).tag("sin_salidas")) }
                }
                items(salidas, key = { "sal_${it.id}" }) { s ->
                    val sel = s.id == vm.salidaId
                    Card(
                        onClick = { vm.elegir(s) },
                        border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        colors = CardDefaults.cardColors(containerColor = if (s.libres > 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).tag("salida_card_${s.id}"),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(fechaConDia(s.fecha), fontWeight = FontWeight.SemiBold)
                                Text("Sale a las ${s.hora ?: "—"}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                if (s.libres > 0) "${s.libres} cupos" else "Agotado",
                                color = if (s.libres > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.tag("salida_cupos_${s.id}"),
                            )
                            if (sel) Icon(Icons.Filled.CheckCircle, "Seleccionada", Modifier.padding(start = 8.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                val s = vm.salida
                if (s != null) {
                    item {
                        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(14.dp))) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Stepper("Personas", vm.personas, 1, s.libres.coerceAtMost(50), "tour_personas") { vm.personas = it }
                                HorizontalDivider()
                                Text("${vm.personas} × ${formatPrice(precio)} = ${formatPrice(precio * vm.personas)} + IGV", modifier = Modifier.tag("tour_subtotal"))
                            }
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        vm.aviso?.let { Banner(it, error = true, tagId = "aviso_tour") }
                        Button(
                            onClick = {
                                val sal = vm.salida
                                when {
                                    sal == null -> vm.aviso = "Elige una fecha de salida."
                                    user == null -> onLogin()
                                    user?.esViajero != true -> vm.aviso = "Solo las cuentas de viajero pueden reservar."
                                    else -> {
                                        Carrito.tour = SeleccionTour(id, t.nombre ?: "Tour", sal.id ?: 0, sal.fecha ?: "", sal.hora ?: "", vm.personas)
                                        onCheckout()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).tag("btn_reservar_tour"),
                        ) { Text("Continuar con la reserva") }
                    }
                }
                item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }
                item { ReviewsSection(servicioId = id, onLogin = onLogin) }
            }
        }
    }
}
