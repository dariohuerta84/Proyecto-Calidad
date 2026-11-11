package com.trouvels360.app.ui.screens.proveedor

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.trouvels360.app.data.Validaciones
import com.trouvels360.app.data.remote.ReservaDto
import com.trouvels360.app.ui.components.AppDialog
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.Campo
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

class ReservasRecibidasViewModel : ViewModel() {
    var tipo by mutableStateOf<String?>(null); private set
    var estado by mutableStateOf<String?>(null); private set
    var servicioId by mutableStateOf<Int?>(null); private set
    var items by mutableStateOf<List<ReservaDto>>(emptyList()); private set
    var page by mutableIntStateOf(1); private set
    var lastPage by mutableIntStateOf(1); private set
    var total by mutableIntStateOf(0); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set
    var mensaje by mutableStateOf<String?>(null)
    var mensajeError by mutableStateOf<String?>(null)
    var procesandoId by mutableStateOf<Int?>(null); private set

    var aRechazar by mutableStateOf<ReservaDto?>(null); private set
    var motivo by mutableStateOf("")
    var errorMotivo by mutableStateOf<String?>(null); private set
    private var iniciado = false

    fun iniciar(servicio: Int?, est: String?) {
        if (iniciado) return
        iniciado = true
        servicioId = servicio; estado = est
        cargar(1)
    }

    fun filtrarTipo(t: String?) { tipo = t; cargar(1) }
    fun filtrarEstado(e: String?) { estado = e; cargar(1) }
    fun quitarServicio() { servicioId = null; cargar(1) }

    /** TRV-507 — Pendientes primero; incluye datos del cliente. */
    fun cargar(p: Int = page) {
        viewModelScope.launch {
            loading = true; error = null
            when (val r = Repository.reservasRecibidas(tipo, estado, servicioId, p)) {
                is Result.Ok -> { items = r.value.items; page = r.value.page; lastPage = r.value.lastPage; total = r.value.total }
                is Result.Err -> error = r.message
            }
            loading = false
        }
    }

    /** TRV-204 — Aceptar: la reserva pasa a CONFIRMADA (409 si ya no está pendiente). */
    fun aceptar(r: ReservaDto) {
        val id = r.id ?: return
        viewModelScope.launch {
            procesandoId = id; mensaje = null; mensajeError = null
            when (val res = Repository.aceptarReserva(id)) {
                is Result.Ok -> mensaje = res.value.message ?: "Reserva ${r.codigo} confirmada."
                is Result.Err -> mensajeError = res.message
            }
            procesandoId = null
            cargar()
        }
    }

    fun pedirRechazo(r: ReservaDto) { aRechazar = r; motivo = ""; errorMotivo = null; mensaje = null; mensajeError = null }
    fun cerrarRechazo() { if (procesandoId == null) aRechazar = null }

    /** TRV-204 — Rechazar con motivo de 10 a 300 caracteres; las unidades se liberan. */
    fun confirmarRechazo() {
        val r = aRechazar ?: return
        val id = r.id ?: return
        if (!Validaciones.motivoValido(motivo)) { errorMotivo = "El motivo debe tener entre 10 y 300 caracteres."; return }
        viewModelScope.launch {
            procesandoId = id; errorMotivo = null
            when (val res = Repository.rechazarReserva(id, motivo)) {
                is Result.Ok -> { aRechazar = null; mensaje = res.value.message ?: "Reserva ${r.codigo} rechazada."; cargar() }
                is Result.Err -> if (res.code == 422) errorMotivo = res.message else { aRechazar = null; mensajeError = res.message; cargar() }
            }
            procesandoId = null
        }
    }
}

private val ESTADOS = listOf(null, "pendiente", "confirmada", "rechazada", "cancelada")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasRecibidasScreen(servicioId: Int?, estadoInicial: String?, vm: ReservasRecibidasViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.iniciar(servicioId, estadoInicial) }

    Column(Modifier.fillMaxSize().tag("pantalla_reservas_recibidas")) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Las solicitudes de hotel quedan pendientes hasta que las aceptes o rechaces. Las de tour se confirman solas.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = vm.tipo == null, onClick = { vm.filtrarTipo(null) }, label = { Text("Hoteles y tours") }, modifier = Modifier.tag("rr_tipo_todos"))
                FilterChip(selected = vm.tipo == "hotel", onClick = { vm.filtrarTipo("hotel") }, label = { Text("Hoteles") }, modifier = Modifier.tag("rr_tipo_hotel"))
                FilterChip(selected = vm.tipo == "tour", onClick = { vm.filtrarTipo("tour") }, label = { Text("Tours") }, modifier = Modifier.tag("rr_tipo_tour"))
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ESTADOS.forEach { e ->
                    FilterChip(selected = vm.estado == e, onClick = { vm.filtrarEstado(e) },
                        label = { Text(e?.replaceFirstChar { it.uppercase() } ?: "Todos") }, modifier = Modifier.tag("rr_estado_${e ?: "todos"}"))
                }
            }
            vm.servicioId?.let { sid ->
                InputChip(selected = true, onClick = { vm.quitarServicio() }, label = { Text("Publicación #$sid  ✕") }, modifier = Modifier.tag("rr_quitar_servicio"))
            }
            vm.mensaje?.let { Banner(it, error = false, tagId = "rr_mensaje_ok") }
            vm.mensajeError?.let { Banner(it, error = true, tagId = "rr_mensaje_error") }
        }
        when {
            vm.loading -> Loading()
            vm.error != null -> ErrorState(vm.error!!, onRetry = { vm.cargar() })
            vm.items.isEmpty() -> EmptyState("No hay reservas con estos filtros.", tagId = "rr_vacio")
            else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("${vm.total} reservas", style = MaterialTheme.typography.bodySmall, modifier = Modifier.tag("rr_total")) }
                items(vm.items, key = { "${it.tipo}_${it.id}" }) { r ->
                    ReservaRecibidaCard(r, procesando = vm.procesandoId == r.id, onAceptar = { vm.aceptar(r) }, onRechazar = { vm.pedirRechazo(r) })
                }
                item { PaginationBar(vm.page, vm.lastPage, { vm.cargar(it) }, tagPrefix = "rr_pagina") }
            }
        }
    }

    val r = vm.aRechazar
    if (r != null) {
        AppDialog(
            title = "Rechazar ${r.codigo}",
            onDismiss = { vm.cerrarRechazo() },
            tagId = "dialogo_rechazo",
            buttons = {
                TextButton(onClick = { vm.cerrarRechazo() }, enabled = vm.procesandoId == null, modifier = Modifier.tag("rechazo_cancelar")) { Text("Volver") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.confirmarRechazo() }, enabled = vm.procesandoId == null, modifier = Modifier.tag("rechazo_confirmar")) {
                    if (vm.procesandoId != null) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Confirmar rechazo")
                }
            },
        ) {
            Text("El viajero verá este motivo en Mis reservas. Las habitaciones vuelven a quedar disponibles.")
            Spacer(Modifier.height(8.dp))
            Campo(vm.motivo, { if (it.length <= 300) vm.motivo = it }, "Motivo (10 a 300 caracteres)", "rechazo_motivo",
                error = vm.errorMotivo, singleLine = false, minLines = 3, supporting = "${vm.motivo.trim().length}/300")
        }
    }
}

@Composable
private fun ReservaRecibidaCard(r: ReservaDto, procesando: Boolean, onAceptar: () -> Unit, onRechazar: () -> Unit) {
    Card(Modifier.fillMaxWidth().tag("rr_card_${r.codigo}")) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text((if (r.esHotel) "🏨 " else "🧭 ") + (r.servicio?.nombre ?: "—"), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                EstadoChip(r.estado, Modifier.tag("rr_estado_${r.codigo}"))
            }
            Spacer(Modifier.height(6.dp))
            FilaDato("Código", r.codigo ?: "—")
            FilaDato("Cliente", "${r.cliente?.nombre ?: "—"}\n${r.cliente?.email ?: ""}")
            if (r.esHotel) {
                FilaDato("Habitación", "${r.habitacion?.nombre ?: "—"} × ${r.cantidad ?: 1}")
                FilaDato("Estadía", "${fecha(r.fechaInicio)} → ${fecha(r.fechaFin)} (${r.noches ?: 0} noches)")
            } else {
                FilaDato("Salida", "${fechaConDia(r.salida?.fecha)} · ${r.salida?.hora ?: ""}")
                FilaDato("Personas", "${r.personas ?: 0}")
            }
            FilaDato("Total (IGV incl.)", formatPrice(r.total), bold = true)
            val motivo = r.motivoRechazo
            if (!motivo.isNullOrBlank()) Banner("Motivo del rechazo: $motivo", error = true, tagId = "rr_motivo_${r.codigo}")
            if (r.esHotel && r.estado == "pendiente") {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onRechazar, enabled = !procesando, modifier = Modifier.tag("btn_rechazar_${r.codigo}")) { Text("Rechazar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onAceptar, enabled = !procesando, modifier = Modifier.tag("btn_aceptar_${r.codigo}")) {
                        if (procesando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Aceptar")
                    }
                }
            }
        }
    }
}
