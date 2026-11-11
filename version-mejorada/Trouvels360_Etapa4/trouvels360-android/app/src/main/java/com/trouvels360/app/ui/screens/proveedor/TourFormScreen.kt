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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.trouvels360.app.data.Validaciones
import com.trouvels360.app.data.remote.SalidaUpsert
import com.trouvels360.app.data.remote.TourUpsert
import com.trouvels360.app.ui.CATEGORIAS_TOUR
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.Campo
import com.trouvels360.app.ui.components.DateField
import com.trouvels360.app.ui.components.ErrorState
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch
import java.time.LocalDate

class SalidaForm(fecha: LocalDate? = null, hora: String = "08:00", cupo: String = "10") {
    var fecha by mutableStateOf(fecha)
    var hora by mutableStateOf(hora)
    var cupo by mutableStateOf(cupo)
    fun error(): String? = when {
        fecha == null -> "Elige la fecha."
        fecha!!.isBefore(LocalDate.now()) -> "La fecha no puede ser pasada."
        !Validaciones.horaValida(hora) -> "Hora en formato HH:MM."
        (cupo.toIntOrNull() ?: 0) !in 1..1000 -> "Cupo entre 1 y 1000."
        else -> null
    }
}

class TourFormViewModel : ViewModel() {
    var tourId by mutableStateOf<Int?>(null); private set
    var nombre by mutableStateOf(""); var descripcion by mutableStateOf("")
    var ciudad by mutableStateOf(""); var pais by mutableStateOf("Perú"); var imagen by mutableStateOf("")
    var categoria by mutableStateOf("Aventura"); var duracion by mutableStateOf("240"); var precio by mutableStateOf("")
    /** Solo al crear (TRV-504). Después se gestionan en "Salidas y actividades" (TRV-506). */
    val salidas = mutableStateListOf(SalidaForm())
    var errores by mutableStateOf(mapOf<String, String>()); private set
    var cargando by mutableStateOf(false); private set
    var errorCarga by mutableStateOf<String?>(null); private set
    var guardando by mutableStateOf(false); private set
    var mensaje by mutableStateOf<String?>(null); private set
    var mensajeError by mutableStateOf<String?>(null); private set
    private var iniciado = false

    fun iniciar(id: Int?) {
        if (iniciado) return
        iniciado = true
        tourId = id
        if (id == null) return
        viewModelScope.launch {
            cargando = true
            when (val r = Repository.tour(id)) {
                is Result.Ok -> {
                    val t = r.value
                    nombre = t.nombre.orEmpty(); descripcion = t.descripcion.orEmpty(); ciudad = t.ciudad.orEmpty(); pais = t.pais.orEmpty()
                    imagen = t.imagenUrl.orEmpty(); categoria = t.tour?.categoria ?: "Aventura"
                    duracion = "${t.tour?.duracion ?: 60}"; precio = "${t.tour?.precio ?: ""}"
                }
                is Result.Err -> errorCarga = r.message
            }
            cargando = false
        }
    }

    private fun validar(): Map<String, String> = buildMap {
        if (nombre.isBlank() || nombre.length > 150) put("nombre", "Nombre obligatorio (máx. 150).")
        if (descripcion.isBlank()) put("descripcion", "Describe el tour.")
        if (ciudad.isBlank()) put("ciudad", "Ciudad obligatoria.")
        if (pais.isBlank()) put("pais", "País obligatorio.")
        if (!Validaciones.urlValida(imagen)) put("imagen_url", "La imagen es obligatoria (URL http/https).")
        if ((duracion.toIntOrNull() ?: 0) !in 1..10080) put("duracion", "Duración en minutos (1 a 10080).")
        if ((precio.replace(',', '.').toDoubleOrNull() ?: -1.0) < 0) put("precio", "Precio por persona válido.")
        if (tourId == null) {
            if (salidas.isEmpty()) put("salidas", "Programa al menos una salida.")
            salidas.forEachIndexed { i, s -> s.error()?.let { put("salidas", "Salida ${i + 1}: $it") } }
            val claves = salidas.map { "${it.fecha}_${it.hora}" }
            if (claves.size != claves.toSet().size) put("salidas", "Hay salidas repetidas (misma fecha y hora).")
        }
    }

    fun guardar(onCreado: () -> Unit) {
        val e = validar()
        errores = e; mensaje = null; mensajeError = null
        if (e.isNotEmpty()) { mensajeError = e.values.first(); return }
        val id = tourId
        val body = TourUpsert(
            nombre.trim(), descripcion.trim(), ciudad.trim(), pais.trim(), imagen.trim(), categoria,
            duracion.toInt(), precio.replace(',', '.').toDouble(),
            salidas = if (id == null) salidas.map { SalidaUpsert(it.fecha.toString(), it.hora.trim(), it.cupo.toInt()) } else null,
        )
        viewModelScope.launch {
            guardando = true
            val r = if (id == null) Repository.crearTour(body) else Repository.editarTour(id, body)
            when (r) {
                is Result.Ok -> { mensaje = r.value; if (id == null) onCreado() }
                is Result.Err -> { errores = r.fields; mensajeError = r.message }
            }
            guardando = false
        }
    }
}

/** TRV-504 — Publicar y editar tours. Al editar no se tocan las salidas (se gestionan aparte). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourFormScreen(tourId: Int?, onCreado: () -> Unit, onPrograma: (Int) -> Unit, vm: TourFormViewModel = viewModel(key = "tour_form_${tourId ?: "nuevo"}")) {
    LaunchedEffect(tourId) { vm.iniciar(tourId) }
    if (vm.cargando) { Loading(); return }
    val errorCarga = vm.errorCarga
    if (errorCarga != null) { ErrorState(errorCarga); return }
    val editando = vm.tourId != null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("pantalla_form_tour"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(if (editando) "Editar tour" else "Publicar tour", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Campo(vm.nombre, { vm.nombre = it }, "Nombre", "tour_nombre", error = vm.errores["nombre"])
        Campo(vm.descripcion, { vm.descripcion = it }, "Descripción", "tour_descripcion", error = vm.errores["descripcion"], singleLine = false, minLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Campo(vm.ciudad, { vm.ciudad = it }, "Ciudad", "tour_ciudad", Modifier.weight(1f), error = vm.errores["ciudad"])
            Campo(vm.pais, { vm.pais = it }, "País", "tour_pais", Modifier.weight(1f), error = vm.errores["pais"])
        }
        Campo(vm.imagen, { vm.imagen = it }, "Imagen (URL)", "tour_imagen_url", error = vm.errores["imagen_url"], keyboardType = KeyboardType.Uri)
        Text("Categoría", fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CATEGORIAS_TOUR.forEach { c ->
                FilterChip(selected = vm.categoria == c, onClick = { vm.categoria = c }, label = { Text(c) }, modifier = Modifier.tag("tour_categoria_$c"))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Campo(vm.duracion, { vm.duracion = it.filter(Char::isDigit) }, "Duración (min)", "tour_duracion", Modifier.weight(1f), error = vm.errores["duracion"], keyboardType = KeyboardType.Number)
            Campo(vm.precio, { vm.precio = it.filter { c -> c.isDigit() || c == '.' || c == ',' } }, "S/ por persona", "tour_precio", Modifier.weight(1f),
                error = vm.errores["precio"], keyboardType = KeyboardType.Decimal)
        }

        if (!editando) {
            SectionTitle("Salidas", Modifier.padding(top = 8.dp))
            vm.salidas.forEachIndexed { i, s ->
                Card(Modifier.fillMaxWidth().tag("salida_form_$i")) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Salida ${i + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (vm.salidas.size > 1) TextButton(onClick = { vm.salidas.remove(s) }, modifier = Modifier.tag("salida_quitar_$i")) { Text("Quitar") }
                        }
                        DateField(s.fecha, { s.fecha = it }, "Fecha", "salida_fecha_$i", Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Campo(s.hora, { s.hora = it.take(5) }, "Hora (HH:MM)", "salida_hora_$i", Modifier.weight(1f))
                            Campo(s.cupo, { s.cupo = it.filter(Char::isDigit) }, "Cupo", "salida_cupo_$i", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                        }
                    }
                }
            }
            OutlinedButton(onClick = { vm.salidas.add(SalidaForm()) }, modifier = Modifier.tag("btn_agregar_salida")) { Text("+ Agregar salida") }
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Salidas y actividades", fontWeight = FontWeight.SemiBold)
                    Text("Se gestionan en su propia pantalla para no alterar reservas confirmadas.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { vm.tourId?.let(onPrograma) }, modifier = Modifier.tag("btn_ir_programa")) { Text("Gestionar salidas y actividades →") }
                }
            }
        }

        vm.mensaje?.let { Banner(it, error = false, tagId = "form_ok") }
        vm.mensajeError?.let { Banner(it, error = true, tagId = "form_error") }
        Button(onClick = { vm.guardar(onCreado) }, enabled = !vm.guardando, modifier = Modifier.fillMaxWidth().height(50.dp).tag("btn_guardar_tour")) {
            if (vm.guardando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text(if (editando) "Guardar cambios" else "Publicar tour")
        }
        Spacer(Modifier.height(24.dp))
    }
}
