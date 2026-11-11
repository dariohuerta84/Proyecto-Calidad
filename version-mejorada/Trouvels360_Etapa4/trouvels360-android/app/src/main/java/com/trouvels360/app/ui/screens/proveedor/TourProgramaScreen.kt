package com.trouvels360.app.ui.screens.proveedor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.onOk
import com.trouvels360.app.data.remote.ActividadDto
import com.trouvels360.app.data.remote.ActividadUpsert
import com.trouvels360.app.data.remote.SalidaDto
import com.trouvels360.app.data.remote.SalidaUpsert
import com.trouvels360.app.ui.components.AppDialog
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.Campo
import com.trouvels360.app.ui.components.ConfirmDialog
import com.trouvels360.app.ui.components.DateField
import com.trouvels360.app.ui.components.EstadoChip
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.fechaConDia
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch
import java.time.LocalDate

class TourProgramaViewModel : ViewModel() {
    var tourId = 0; private set
    var nombre by mutableStateOf(""); private set
    var salidas by mutableStateOf<List<SalidaDto>>(emptyList()); private set
    var actividades by mutableStateOf<List<ActividadDto>>(emptyList()); private set
    var cargando by mutableStateOf(true); private set
    var mensaje by mutableStateOf<String?>(null); private set
    var mensajeError by mutableStateOf<String?>(null); private set
    var trabajando by mutableStateOf(false); private set

    // Formulario de salida (null = cerrado; id null = nueva)
    var salidaForm by mutableStateOf<SalidaDto?>(null); private set
    val salida = SalidaForm()
    var errorSalida by mutableStateOf<String?>(null); private set
    var salidaAEliminar by mutableStateOf<SalidaDto?>(null)

    // Formulario de actividad
    var actForm by mutableStateOf<ActividadDto?>(null); private set
    var titulo by mutableStateOf(""); var descripcion by mutableStateOf(""); var duracion by mutableStateOf(""); var orden by mutableStateOf("")
    var errorAct by mutableStateOf<String?>(null); private set
    var actAEliminar by mutableStateOf<ActividadDto?>(null)

    fun iniciar(id: Int) {
        if (tourId == id) return
        tourId = id
        viewModelScope.launch { Repository.tour(id).onOk { nombre = it.nombre.orEmpty() } }
        recargar()
    }

    fun recargar() {
        viewModelScope.launch {
            cargando = true
            when (val r = Repository.salidas(tourId)) {
                is Result.Ok -> salidas = r.value
                is Result.Err -> mensajeError = r.message
            }
            when (val r = Repository.actividades(tourId)) {
                is Result.Ok -> actividades = r.value.sortedBy { it.orden ?: 0 }
                is Result.Err -> mensajeError = r.message
            }
            cargando = false
        }
    }

    private fun ok(m: String) { mensaje = m; mensajeError = null }
    private fun ko(m: String) { mensajeError = m; mensaje = null }

    // ------------------------------------------------ Salidas (TRV-506)

    fun abrirSalida(s: SalidaDto?) {
        salidaForm = s ?: SalidaDto()
        salida.fecha = s?.fecha?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
        salida.hora = s?.hora ?: "08:00"
        salida.cupo = "${s?.cupoTotal ?: 10}"
        errorSalida = null
    }

    fun cerrarSalida() { if (!trabajando) salidaForm = null }

    fun guardarSalida() {
        val original = salidaForm ?: return
        val err = salida.error() ?: run {
            val reservados = original.cupoReservado ?: 0
            if ((salida.cupo.toIntOrNull() ?: 0) < reservados) "Mínimo $reservados (cupos ya reservados)." else null
        }
        if (err != null) { errorSalida = err; return }
        val fecha = salida.fecha.toString()
        val hora = salida.hora.trim()
        val cupo = salida.cupo.toInt()
        viewModelScope.launch {
            trabajando = true; errorSalida = null
            val oid = original.id
            val r = if (oid == null) Repository.crearSalida(tourId, SalidaUpsert(fecha, hora, cupo))
            else {
                val cambios = mutableMapOf<String, Any>("cupo_total" to cupo)
                if (original.fecha?.take(10) != fecha) cambios["fecha"] = fecha
                if (original.hora != hora) cambios["hora"] = hora
                Repository.editarSalida(tourId, oid, cambios)
            }
            when (r) {
                is Result.Ok -> { salidaForm = null; ok(if (oid == null) "Salida programada." else "Salida actualizada."); recargar() }
                is Result.Err -> errorSalida = r.message
            }
            trabajando = false
        }
    }

    /** programada → cerrada / cancelada, o reabrir. */
    fun cambiarEstado(s: SalidaDto, estado: String) {
        val id = s.id ?: return
        viewModelScope.launch {
            trabajando = true
            when (val r = Repository.editarSalida(tourId, id, mapOf("estado" to estado))) {
                is Result.Ok -> { ok("Salida del ${fechaConDia(s.fecha)} ${if (estado == "programada") "reabierta" else estado}."); recargar() }
                is Result.Err -> ko(r.message)
            }
            trabajando = false
        }
    }

    fun eliminarSalida() {
        val s = salidaAEliminar ?: return
        val id = s.id ?: return
        viewModelScope.launch {
            trabajando = true
            when (val r = Repository.eliminarSalida(tourId, id)) {
                is Result.Ok -> { ok("Salida eliminada."); recargar() }
                is Result.Err -> ko(r.message)
            }
            salidaAEliminar = null
            trabajando = false
        }
    }

    // ------------------------------------------------ Actividades (TRV-505)

    fun abrirActividad(a: ActividadDto?) {
        actForm = a ?: ActividadDto()
        titulo = a?.titulo.orEmpty(); descripcion = a?.descripcion.orEmpty()
        duracion = a?.duracionMin?.toString().orEmpty(); orden = a?.orden?.toString().orEmpty()
        errorAct = null
    }

    fun cerrarActividad() { if (!trabajando) actForm = null }

    fun guardarActividad() {
        val original = actForm ?: return
        val err = when {
            titulo.isBlank() || titulo.length > 150 -> "El título es obligatorio (máx. 150)."
            duracion.isNotBlank() && (duracion.toIntOrNull() ?: 0) !in 1..1440 -> "Duración entre 1 y 1440 minutos."
            orden.isNotBlank() && (orden.toIntOrNull() ?: 0) < 1 -> "El orden empieza en 1."
            else -> null
        }
        if (err != null) { errorAct = err; return }
        val body = ActividadUpsert(titulo.trim(), descripcion.trim().ifBlank { null }, duracion.toIntOrNull(), orden.toIntOrNull())
        viewModelScope.launch {
            trabajando = true; errorAct = null
            val oid = original.id
            val r = if (oid == null) Repository.crearActividad(tourId, body) else Repository.editarActividad(tourId, oid, body)
            when (r) {
                is Result.Ok -> { actForm = null; ok(if (oid == null) "Actividad agregada." else "Actividad actualizada."); recargar() }
                is Result.Err -> errorAct = r.message
            }
            trabajando = false
        }
    }

    fun mover(a: ActividadDto, delta: Int) {
        val id = a.id ?: return
        val nuevo = (a.orden ?: 1) + delta
        if (nuevo < 1 || nuevo > actividades.size) return
        viewModelScope.launch {
            trabajando = true
            when (val r = Repository.moverActividad(tourId, id, nuevo)) {
                is Result.Ok -> recargar()
                is Result.Err -> ko(r.message)
            }
            trabajando = false
        }
    }

    fun eliminarActividad() {
        val a = actAEliminar ?: return
        val id = a.id ?: return
        viewModelScope.launch {
            trabajando = true
            when (val r = Repository.eliminarActividad(tourId, id)) {
                is Result.Ok -> { ok("Actividad eliminada."); recargar() }
                is Result.Err -> ko(r.message)
            }
            actAEliminar = null
            trabajando = false
        }
    }
}

/** TRV-505 y TRV-506 — Salidas (cupos y estados) y actividades ordenadas de un tour. */
@Composable
fun TourProgramaScreen(tourId: Int, vm: TourProgramaViewModel = viewModel(key = "programa_$tourId")) {
    LaunchedEffect(tourId) { vm.iniciar(tourId) }
    if (vm.cargando && vm.salidas.isEmpty() && vm.actividades.isEmpty()) { Loading(); return }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("pantalla_programa_tour"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(vm.nombre.ifBlank { "Tour #$tourId" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.tag("programa_nombre"))
        vm.mensaje?.let { Banner(it, error = false, tagId = "programa_ok") }
        vm.mensajeError?.let { Banner(it, error = true, tagId = "programa_error") }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Salidas", Modifier.weight(1f))
            Button(onClick = { vm.abrirSalida(null) }, modifier = Modifier.tag("btn_nueva_salida")) { Text("+ Salida") }
        }
        if (vm.salidas.isEmpty()) Text("Aún no hay salidas.", modifier = Modifier.tag("sin_salidas"))
        vm.salidas.forEach { s ->
            Card(Modifier.fillMaxWidth().tag("salida_item_${s.id}")) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${fechaConDia(s.fecha)} · ${s.hora ?: ""}", fontWeight = FontWeight.SemiBold)
                            Text("${s.cupoReservado ?: 0}/${s.cupoTotal ?: 0} reservados · ${s.libres} libres", style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.tag("salida_ocupacion_${s.id}"))
                        }
                        EstadoChip(s.estado, Modifier.tag("salida_estado_${s.id}"))
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        TextButton(onClick = { vm.abrirSalida(s) }, modifier = Modifier.tag("salida_editar_${s.id}")) { Text("Editar") }
                        if (s.estado == "programada") TextButton(onClick = { vm.cambiarEstado(s, "cerrada") }, modifier = Modifier.tag("salida_cerrar_${s.id}")) { Text("Cerrar") }
                        else TextButton(onClick = { vm.cambiarEstado(s, "programada") }, modifier = Modifier.tag("salida_reabrir_${s.id}")) { Text("Reabrir") }
                        if (s.estado != "cancelada") TextButton(onClick = { vm.cambiarEstado(s, "cancelada") }, modifier = Modifier.tag("salida_cancelar_${s.id}")) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.error)
                        }
                        TextButton(onClick = { vm.salidaAEliminar = s }, modifier = Modifier.tag("salida_eliminar_${s.id}")) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Actividades", Modifier.weight(1f))
            Button(onClick = { vm.abrirActividad(null) }, modifier = Modifier.tag("btn_nueva_actividad")) { Text("+ Actividad") }
        }
        if (vm.actividades.isEmpty()) Text("Sin actividades todavía.", modifier = Modifier.tag("sin_actividades"))
        vm.actividades.forEachIndexed { i, a ->
            Card(Modifier.fillMaxWidth().tag("actividad_item_${a.orden}")) {
                Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${a.orden}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(28.dp))
                    Column(Modifier.weight(1f)) {
                        Text(a.titulo.orEmpty(), fontWeight = FontWeight.SemiBold, modifier = Modifier.tag("actividad_titulo_${a.orden}"))
                        a.duracionMin?.let { Text("$it min", style = MaterialTheme.typography.bodySmall) }
                    }
                    IconButton(onClick = { vm.mover(a, -1) }, enabled = i > 0 && !vm.trabajando, modifier = Modifier.tag("actividad_subir_${a.orden}")) {
                        Icon(Icons.Filled.KeyboardArrowUp, "Subir")
                    }
                    IconButton(onClick = { vm.mover(a, 1) }, enabled = i < vm.actividades.size - 1 && !vm.trabajando, modifier = Modifier.tag("actividad_bajar_${a.orden}")) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Bajar")
                    }
                    TextButton(onClick = { vm.abrirActividad(a) }, modifier = Modifier.tag("actividad_editar_${a.orden}")) { Text("Editar") }
                    TextButton(onClick = { vm.actAEliminar = a }, modifier = Modifier.tag("actividad_eliminar_${a.orden}")) { Text("✕", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    // ------------------------------------------------ Diálogos
    val sf = vm.salidaForm
    if (sf != null) {
        AppDialog(
            title = if (sf.id == null) "Nueva salida" else "Editar salida",
            onDismiss = { vm.cerrarSalida() },
            tagId = "dialogo_salida",
            buttons = {
                TextButton(onClick = { vm.cerrarSalida() }, modifier = Modifier.tag("salida_form_cancelar")) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.guardarSalida() }, enabled = !vm.trabajando, modifier = Modifier.tag("salida_form_guardar")) {
                    if (vm.trabajando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Guardar")
                }
            },
        ) {
            DateField(vm.salida.fecha, { vm.salida.fecha = it }, "Fecha", "salida_form_fecha", Modifier.fillMaxWidth())
            Campo(vm.salida.hora, { vm.salida.hora = it.take(5) }, "Hora (HH:MM)", "salida_form_hora")
            Campo(vm.salida.cupo, { vm.salida.cupo = it.filter(Char::isDigit) }, "Cupo total", "salida_form_cupo", keyboardType = KeyboardType.Number,
                supporting = sf.cupoReservado?.takeIf { it > 0 }?.let { "Ya hay $it cupos reservados." })
            vm.errorSalida?.let { Banner(it, error = true, tagId = "salida_form_error") }
        }
    }
    val af = vm.actForm
    if (af != null) {
        AppDialog(
            title = if (af.id == null) "Nueva actividad" else "Editar actividad",
            onDismiss = { vm.cerrarActividad() },
            tagId = "dialogo_actividad",
            buttons = {
                TextButton(onClick = { vm.cerrarActividad() }, modifier = Modifier.tag("actividad_form_cancelar")) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.guardarActividad() }, enabled = !vm.trabajando, modifier = Modifier.tag("actividad_form_guardar")) { Text("Guardar") }
            },
        ) {
            Campo(vm.titulo, { vm.titulo = it }, "Título", "actividad_form_titulo")
            Campo(vm.descripcion, { vm.descripcion = it }, "Descripción (opcional)", "actividad_form_descripcion", singleLine = false, minLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Campo(vm.duracion, { vm.duracion = it.filter(Char::isDigit) }, "Minutos", "actividad_form_duracion", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                Campo(vm.orden, { vm.orden = it.filter(Char::isDigit) }, "Orden", "actividad_form_orden", Modifier.weight(1f), keyboardType = KeyboardType.Number,
                    placeholder = "al final")
            }
            vm.errorAct?.let { Banner(it, error = true, tagId = "actividad_form_error") }
        }
    }
    vm.salidaAEliminar?.let { s ->
        ConfirmDialog("¿Eliminar la salida del ${fechaConDia(s.fecha)}?", "No se puede eliminar si tiene reservas confirmadas; en ese caso cancélala.",
            "Eliminar", { vm.eliminarSalida() }, { vm.salidaAEliminar = null }, tagId = "dialogo_eliminar_salida", working = vm.trabajando)
    }
    vm.actAEliminar?.let { a ->
        ConfirmDialog("¿Eliminar «${a.titulo}»?", "Las demás actividades se reordenan.", "Eliminar",
            { vm.eliminarActividad() }, { vm.actAEliminar = null }, tagId = "dialogo_eliminar_actividad", working = vm.trabajando)
    }
}
