package com.trouvels360.app.ui.screens.tours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.nuevaClaveIdempotencia
import com.trouvels360.app.data.remote.CotizacionTourDto
import com.trouvels360.app.data.remote.PagoDto
import com.trouvels360.app.data.remote.ReservaDto
import com.trouvels360.app.ui.Carrito
import com.trouvels360.app.ui.SeleccionTour
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.EstadoChip
import com.trouvels360.app.ui.components.FilaDato
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.PagoDialog
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.fechaConDia
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch

class TourCheckoutViewModel : ViewModel() {
    var seleccion by mutableStateOf<SeleccionTour?>(null); private set
    var cotizacion by mutableStateOf<CotizacionTourDto?>(null); private set
    var cotizando by mutableStateOf(true); private set
    var errorCotizacion by mutableStateOf<String?>(null); private set
    var pagoAbierto by mutableStateOf(false)
    var procesando by mutableStateOf(false); private set
    var errorPago by mutableStateOf<String?>(null); private set
    var reserva by mutableStateOf<ReservaDto?>(null); private set
    private var clave = nuevaClaveIdempotencia()
    private var iniciado = false

    fun iniciar(s: SeleccionTour?) {
        if (iniciado) return
        iniciado = true
        seleccion = s
        cotizar()
    }

    fun cotizar() {
        val s = seleccion
        if (s == null) { cotizando = false; errorCotizacion = "Faltan datos. Vuelve al tour y elige una salida."; return }
        viewModelScope.launch {
            cotizando = true; errorCotizacion = null
            when (val r = Repository.cotizarTour(s.salidaId, s.personas)) {
                is Result.Ok -> cotizacion = r.value
                is Result.Err -> errorCotizacion = r.message
            }
            cotizando = false
        }
    }

    /** TRV-303: la reserva de tour se confirma automáticamente y descuenta cupos. */
    fun pagar(pago: PagoDto) {
        val s = seleccion ?: return
        viewModelScope.launch {
            procesando = true; errorPago = null
            when (val r = Repository.reservarTour(s.salidaId, s.personas, pago, clave)) {
                is Result.Ok -> { reserva = r.value; pagoAbierto = false; clave = nuevaClaveIdempotencia(); Carrito.tour = null }
                is Result.Err -> {
                    errorPago = r.message
                    if (r.code == 409 || r.code == 422) { clave = nuevaClaveIdempotencia(); if (r.code == 409) cotizar() }
                }
            }
            procesando = false
        }
    }
}

@Composable
fun TourCheckoutScreen(onMisReservas: () -> Unit, onVolver: () -> Unit, vm: TourCheckoutViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.iniciar(Carrito.tour) }
    val res = vm.reserva
    if (res != null) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("comprobante_tour"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("¡Reserva confirmada!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            EstadoChip(res.estado, Modifier.tag("comprobante_estado"))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    FilaDato("Código", res.codigo ?: "—", valueTag = "comprobante_codigo")
                    FilaDato("Tour", res.servicio?.nombre ?: "—")
                    FilaDato("Salida", "${fechaConDia(res.salida?.fecha)} · ${res.salida?.hora ?: ""}")
                    FilaDato("Personas", "${res.personas ?: 0}")
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    FilaDato("Subtotal", formatPrice(res.subtotal))
                    FilaDato("IGV (18 %)", formatPrice(res.impuesto))
                    FilaDato("Total pagado", formatPrice(res.total), bold = true, valueTag = "comprobante_total")
                    FilaDato("Tarjeta", "•••• ${res.tarjetaUltimos4 ?: ""}")
                }
            }
            Button(onClick = onMisReservas, modifier = Modifier.fillMaxWidth().tag("btn_ir_mis_reservas")) { Text("Ver mis reservas") }
        }
        return
    }

    val s = vm.seleccion
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("pantalla_pago_tour"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Confirma y paga", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (s != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(s.tourNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    FilaDato("Salida", "${fechaConDia(s.fecha)} · ${s.hora}")
                    FilaDato("Personas", "${s.personas}")
                }
            }
        }
        when {
            vm.cotizando -> Loading(Modifier.height(120.dp))
            vm.errorCotizacion != null -> {
                Banner(vm.errorCotizacion!!, error = true, tagId = "error_cotizacion")
                OutlinedButton(onClick = onVolver, modifier = Modifier.tag("btn_volver_tour")) { Text("Volver al tour") }
            }
            else -> vm.cotizacion?.let { c ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        SectionTitle("Resumen")
                        FilaDato("${c.personas} × ${formatPrice(c.precioUnitario)}", formatPrice(c.subtotal), valueTag = "resumen_subtotal")
                        FilaDato("IGV (${c.igvPorcentaje ?: 18} %)", formatPrice(c.impuesto), valueTag = "resumen_impuesto")
                        FilaDato("Total a pagar", formatPrice(c.total), bold = true, valueTag = "resumen_total")
                        FilaDato("Cupos libres en la salida", "${c.salida?.cuposDisponibles ?: 0}")
                    }
                }
                Button(onClick = { vm.pagoAbierto = true }, modifier = Modifier.fillMaxWidth().height(50.dp).tag("btn_confirmar_pagar")) { Text("Confirmar y pagar") }
                Text("Pago simulado. La reserva de tour se confirma al instante.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (vm.pagoAbierto) {
        PagoDialog(vm.cotizacion?.total, vm.procesando, vm.errorPago, onConfirmar = { vm.pagar(it) }, onCancelar = { if (!vm.procesando) vm.pagoAbierto = false })
    }
}
