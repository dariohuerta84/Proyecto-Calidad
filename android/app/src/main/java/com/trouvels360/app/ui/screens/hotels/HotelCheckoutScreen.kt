package com.trouvels360.app.ui.screens.hotels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.trouvels360.app.data.remote.CotizacionHotelDto
import com.trouvels360.app.data.remote.PagoDto
import com.trouvels360.app.data.remote.SolicitudHotelDto
import com.trouvels360.app.ui.Carrito
import com.trouvels360.app.ui.SeleccionHotel
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.EstadoChip
import com.trouvels360.app.ui.components.FilaDato
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.PagoDialog
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.fecha
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch

class HotelCheckoutViewModel : ViewModel() {
    var seleccion by mutableStateOf<SeleccionHotel?>(null); private set
    var cotizacion by mutableStateOf<CotizacionHotelDto?>(null); private set
    var cotizando by mutableStateOf(true); private set
    var errorCotizacion by mutableStateOf<String?>(null); private set

    var pagoAbierto by mutableStateOf(false)
    var procesando by mutableStateOf(false); private set
    var errorPago by mutableStateOf<String?>(null); private set
    var solicitud by mutableStateOf<SolicitudHotelDto?>(null); private set

    /** Misma clave en reintentos tras un fallo de red: la API no duplica la reserva. */
    private var clave = nuevaClaveIdempotencia()
    private var iniciado = false

    fun iniciar(s: SeleccionHotel?) {
        if (iniciado) return
        iniciado = true
        seleccion = s
        cotizar()
    }

    /** HU-V06: el importe oficial (subtotal + IGV 18 % = total) lo calcula la API. */
    fun cotizar() {
        val s = seleccion
        if (s == null) { cotizando = false; errorCotizacion = "Faltan datos de la reserva. Vuelve al hotel y elige fechas y habitaciones."; return }
        viewModelScope.launch {
            cotizando = true; errorCotizacion = null
            when (val r = Repository.cotizarHotel(s.checkIn.toString(), s.checkOut.toString(), s.items)) {
                is Result.Ok -> cotizacion = r.value
                is Result.Err -> errorCotizacion = r.message
            }
            cotizando = false
        }
    }

    /** HU-V07: crea la solicitud; queda PENDIENTE hasta que el proveedor la acepte o rechace. */
    fun pagar(pago: PagoDto) {
        val s = seleccion ?: return
        viewModelScope.launch {
            procesando = true; errorPago = null
            when (val r = Repository.reservarHotel(s.checkIn.toString(), s.checkOut.toString(), s.items, pago, clave)) {
                is Result.Ok -> {
                    solicitud = r.value; pagoAbierto = false
                    clave = nuevaClaveIdempotencia()
                    Carrito.hotel = null
                }
                is Result.Err -> {
                    errorPago = r.message
                    // 409 (ya no hay unidades) o 422 (datos): no se registró nada → nueva clave.
                    if (r.code == 409 || r.code == 422) {
                        clave = nuevaClaveIdempotencia()
                        if (r.code == 409) cotizar()
                    }
                    // Error de red / 5xx: se conserva la clave para reintentar sin duplicar.
                }
            }
            procesando = false
        }
    }
}

@Composable
fun HotelCheckoutScreen(onMisReservas: () -> Unit, onVolver: () -> Unit, vm: HotelCheckoutViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.iniciar(Carrito.hotel) }
    val sol = vm.solicitud
    if (sol != null) { Comprobante(sol, onMisReservas); return }

    val s = vm.seleccion
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("pantalla_pago_hotel"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Confirma y paga", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("El hotel confirmará tu solicitud después del pago.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (s != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(s.hotelNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.tag("pago_hotel_nombre"))
                    FilaDato("Entrada", fecha(s.checkIn.toString()))
                    FilaDato("Salida", fecha(s.checkOut.toString()))
                    FilaDato("Huéspedes", "${s.adultos} adultos" + if (s.ninos > 0) ", ${s.ninos} niños" else "")
                }
            }
        }
        when {
            vm.cotizando -> Loading(Modifier.height(120.dp))
            vm.errorCotizacion != null -> {
                Banner(vm.errorCotizacion!!, error = true, tagId = "error_cotizacion")
                OutlinedButton(onClick = onVolver, modifier = Modifier.tag("btn_volver_hotel")) { Text("Volver al hotel") }
            }
            else -> vm.cotizacion?.let { c ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        SectionTitle("Resumen")
                        c.items.orEmpty().forEach { linea ->
                            FilaDato("${linea.nombre} × ${linea.cantidad} · ${c.noches} noches", formatPrice(linea.subtotal), valueTag = "pago_linea_${linea.habitacionId}")
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        FilaDato("Subtotal", formatPrice(c.subtotal), valueTag = "resumen_subtotal")
                        FilaDato("IGV (${c.igvPorcentaje ?: 18} %)", formatPrice(c.impuesto), valueTag = "resumen_impuesto")
                        FilaDato("Total a pagar", formatPrice(c.total), bold = true, valueTag = "resumen_total")
                    }
                }
                Button(onClick = { vm.pagoAbierto = true }, modifier = Modifier.fillMaxWidth().height(50.dp).tag("btn_confirmar_pagar")) {
                    Text("Confirmar y pagar")
                }
                Text(
                    "Pago simulado. La reserva quedará PENDIENTE hasta que el hotel la acepte.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (vm.pagoAbierto) {
        PagoDialog(
            total = vm.cotizacion?.total,
            procesando = vm.procesando,
            errorServidor = vm.errorPago,
            onConfirmar = { vm.pagar(it) },
            onCancelar = { if (!vm.procesando) vm.pagoAbierto = false },
        )
    }
}

@Composable
private fun Comprobante(sol: SolicitudHotelDto, onMisReservas: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("comprobante_hotel"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("¡Solicitud enviada!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        EstadoChip(sol.estado, Modifier.tag("comprobante_estado"))
        Text("El hotel revisará tu solicitud. Verás si la acepta o la rechaza en Mis reservas.")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                FilaDato("Código de solicitud", sol.solicitudCodigo ?: "—", valueTag = "comprobante_codigo")
                FilaDato("Hotel", sol.hotel?.nombre ?: "—")
                FilaDato("Fechas", "${fecha(sol.fechaInicio)} → ${fecha(sol.fechaFin)} (${sol.noches} noches)")
                sol.reservas.orEmpty().forEach { r -> FilaDato("${r.habitacion?.nombre} × ${r.cantidad}", r.codigo ?: "") }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                FilaDato("Subtotal", formatPrice(sol.subtotal))
                FilaDato("IGV (18 %)", formatPrice(sol.impuesto))
                FilaDato("Total pagado", formatPrice(sol.total), bold = true, valueTag = "comprobante_total")
                FilaDato("Tarjeta", "•••• ${sol.tarjetaUltimos4 ?: ""}")
            }
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onMisReservas, modifier = Modifier.fillMaxWidth().tag("btn_ir_mis_reservas")) { Text("Ver mis reservas") }
    }
}
