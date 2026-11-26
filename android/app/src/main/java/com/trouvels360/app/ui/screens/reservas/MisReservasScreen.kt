package com.trouvels360.app.ui.screens.reservas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.remote.ReservaDto
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.ConfirmDialog
import com.trouvels360.app.ui.components.EmptyState
import com.trouvels360.app.ui.components.ErrorState
import com.trouvels360.app.ui.components.EstadoChip
import com.trouvels360.app.ui.components.FilaDato
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.PaginationBar
import com.trouvels360.app.ui.components.fecha
import com.trouvels360.app.ui.components.fechaConDia
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch

class MisReservasViewModel : ViewModel() {
    var tab by mutableIntStateOf(0); private set          // 0 = hoteles, 1 = tours
    var estado by mutableStateOf<String?>(null); private set
    var items by mutableStateOf<List<ReservaDto>>(emptyList()); private set
    var page by mutableIntStateOf(1); private set
    var lastPage by mutableIntStateOf(1); private set
    var total by mutableIntStateOf(0); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set
    var aviso by mutableStateOf<String?>(null)
    var avisoError by mutableStateOf<String?>(null)
    var aCancelar by mutableStateOf<ReservaDto?>(null)
    var cancelando by mutableStateOf(false); private set
    var errorCancelar by mutableStateOf<String?>(null); private set

    fun cambiarTab(t: Int) { tab = t; estado = null; cargar(1) }
    fun filtrar(e: String?) { estado = e; cargar(1) }

    fun cargar(p: Int = page) {
        viewModelScope.launch {
            loading = true; error = null
            val r = if (tab == 0) Repository.misReservasHotel(estado, p) else Repository.misReservasTour(estado, p)
            when (r) {
                is Result.Ok -> { items = r.value.items; page = r.value.page; lastPage = r.value.lastPage; total = r.value.total }
                is Result.Err -> error = r.message
            }
            loading = false
        }
    }

    /** HU-V13 — Cancelar (solo si la API la marca como cancelable). */
    fun cancelar() {
        val res = aCancelar ?: return
        val id = res.id ?: return
        viewModelScope.launch {
            cancelando = true; errorCancelar = null
            val r = if (res.esHotel) Repository.cancelarReservaHotel(id) else Repository.cancelarReservaTour(id)
            when (r) {
                is Result.Ok -> { aCancelar = null; aviso = "Reserva ${res.codigo} cancelada."; avisoError = null; cargar() }
                is Result.Err -> errorCancelar = r.message
            }
            cancelando = false
        }
    }
}

private val ESTADOS_HOTEL = listOf(null, "pendiente", "confirmada", "rechazada", "cancelada")
private val ESTADOS_TOUR = listOf(null, "confirmada", "cancelada")

/** HU-V11 — Mis reservas de hotel y tour con su estado (fuente de verdad: la API). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(vm: MisReservasViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.cargar(1) }

    Column(Modifier.fillMaxSize().tag("pantalla_mis_reservas")) {
        TabRow(selectedTabIndex = vm.tab) {
            Tab(selected = vm.tab == 0, onClick = { vm.cambiarTab(0) }, text = { Text("Hoteles") }, modifier = Modifier.tag("tab_hoteles"))
            Tab(selected = vm.tab == 1, onClick = { vm.cambiarTab(1) }, text = { Text("Tours") }, modifier = Modifier.tag("tab_tours"))
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (if (vm.tab == 0) ESTADOS_HOTEL else ESTADOS_TOUR).forEach { e ->
                FilterChip(
                    selected = vm.estado == e,
                    onClick = { vm.filtrar(e) },
                    label = { Text(e?.replaceFirstChar { it.uppercase() } ?: "Todas") },
                    modifier = Modifier.tag("filtro_${e ?: "todas"}"),
                )
            }
        }
        vm.aviso?.let { Banner(it, error = false, modifier = Modifier.padding(horizontal = 12.dp), tagId = "reservas_aviso") }
        vm.avisoError?.let { Banner(it, error = true, modifier = Modifier.padding(horizontal = 12.dp), tagId = "reservas_aviso_error") }

        when {
            vm.loading -> Loading()
            vm.error != null -> ErrorState(vm.error!!, onRetry = { vm.cargar() })
            vm.items.isEmpty() -> EmptyState(
                if (vm.estado != null) "No tienes reservas en estado ${vm.estado}." else "Aún no tienes reservas de ${if (vm.tab == 0) "hotel" else "tour"}.",
                tagId = "reservas_vacio",
            )
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("${vm.total} reservas", style = MaterialTheme.typography.bodySmall, modifier = Modifier.tag("reservas_total")) }
                items(vm.items, key = { "${it.tipo}_${it.id}" }) { r -> ReservaCard(r, onCancelar = { vm.aCancelar = r }) }
                item { PaginationBar(vm.page, vm.lastPage, { vm.cargar(it) }, tagPrefix = "reservas_pagina") }
            }
        }
    }

    val c = vm.aCancelar
    if (c != null) {
        ConfirmDialog(
            title = "¿Cancelar la reserva ${c.codigo}?",
            message = if (c.esHotel) "Las habitaciones vuelven a quedar disponibles. Esta acción no se puede deshacer."
            else "Los cupos vuelven a quedar libres. Esta acción no se puede deshacer.",
            confirmLabel = "Sí, cancelar",
            onConfirm = { vm.cancelar() },
            onDismiss = { vm.aCancelar = null },
            tagId = "dialogo_cancelar_reserva",
            working = vm.cancelando,
            error = vm.errorCancelar,
        )
    }
}

@Composable
private fun ReservaCard(r: ReservaDto, onCancelar: () -> Unit) {
    Card(Modifier.fillMaxWidth().tag("reserva_card_${r.codigo}")) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.servicio?.nombre ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                EstadoChip(r.estado, Modifier.tag("reserva_estado_${r.codigo}"))
            }
            Spacer(Modifier.height(6.dp))
            FilaDato("Código", r.codigo ?: "—", valueTag = "reserva_codigo")
            if (r.esHotel) {
                FilaDato("Habitación", "${r.habitacion?.nombre ?: "—"} × ${r.cantidad ?: 1}")
                FilaDato("Estadía", "${fecha(r.fechaInicio)} → ${fecha(r.fechaFin)} (${r.noches ?: 0} noches)")
            } else {
                FilaDato("Salida", "${fechaConDia(r.salida?.fecha)} · ${r.salida?.hora ?: ""}")
                FilaDato("Personas", "${r.personas ?: 0}")
            }
            FilaDato("Total", formatPrice(r.total), bold = true, valueTag = "reserva_total")
            if (r.estado == "pendiente") {
                Text("Esperando la confirmación del hotel.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val motivo = r.motivoRechazo
            if (r.estado == "rechazada" && !motivo.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Banner("Motivo del rechazo: $motivo", error = true, tagId = "motivo_rechazo_${r.codigo}")
            }
            val aviso = r.aviso
            if (!aviso.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Banner(aviso, error = true, tagId = "aviso_salida_${r.codigo}")
            }
            if (r.servicio?.eliminado == true) {
                Text("Esta publicación ya no está disponible; tu historial se conserva.", style = MaterialTheme.typography.bodySmall)
            }
            if (r.cancelable == true) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onCancelar, modifier = Modifier.tag("btn_cancelar_${r.codigo}")) { Text("Cancelar reserva") }
            }
        }
    }
}
