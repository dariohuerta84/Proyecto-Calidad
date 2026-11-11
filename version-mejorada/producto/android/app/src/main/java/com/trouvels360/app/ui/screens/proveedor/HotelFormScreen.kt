package com.trouvels360.app.ui.screens.proveedor

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.trouvels360.app.data.remote.HabitacionDto
import com.trouvels360.app.data.remote.HabitacionUpsert
import com.trouvels360.app.data.remote.HotelUpsert
import com.trouvels360.app.ui.components.AppDialog
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.Campo
import com.trouvels360.app.ui.components.ConfirmDialog
import com.trouvels360.app.ui.components.ErrorState
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.Stepper
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch

/** Fila editable de tipo de habitación (texto para los campos numéricos). */
class HabitacionForm(
    val id: Int? = null,
    nombre: String = "",
    adultos: String = "2",
    ninos: String = "0",
    cantidad: String = "1",
    precio: String = "",
    descripcion: String = "",
) {
    var nombre by mutableStateOf(nombre)
    var adultos by mutableStateOf(adultos)
    var ninos by mutableStateOf(ninos)
    var cantidad by mutableStateOf(cantidad)
    var precio by mutableStateOf(precio)
    var descripcion by mutableStateOf(descripcion)

    fun errores(): Map<String, String> = buildMap {
        if (nombre.isBlank() || nombre.length > 100) put("nombre", "Nombre obligatorio (máx. 100).")
        if ((adultos.toIntOrNull() ?: 0) !in 1..20) put("adultos", "Entre 1 y 20.")
        if ((ninos.toIntOrNull() ?: -1) !in 0..20) put("ninos", "Entre 0 y 20.")
        if ((cantidad.toIntOrNull() ?: 0) !in 1..500) put("cantidad", "Entre 1 y 500.")
        if ((precio.replace(',', '.').toDoubleOrNull() ?: 0.0) <= 0.0) put("precio", "Precio mayor que 0.")
    }

    fun toUpsert() = HabitacionUpsert(
        nombre.trim(), adultos.toInt(), ninos.toInt(), cantidad.toInt(), precio.replace(',', '.').toDouble(), descripcion.trim().ifBlank { null },
    )

    companion object {
        fun from(h: HabitacionDto) = HabitacionForm(
            h.id, h.nombre.orEmpty(), "${h.capacidadAdultos ?: 1}", "${h.capacidadNinos ?: 0}",
            "${h.cantidad ?: h.unidadesTotales ?: 1}", "${h.precioPorNoche ?: ""}", h.descripcion.orEmpty(),
        )
    }
}

class HotelFormViewModel : ViewModel() {
    var hotelId by mutableStateOf<Int?>(null); private set
    var nombre by mutableStateOf(""); var descripcion by mutableStateOf("")
    var ciudad by mutableStateOf(""); var pais by mutableStateOf("Perú")
    var direccion by mutableStateOf(""); var imagen by mutableStateOf("")
    var estrellas by mutableIntStateOf(3)

    /** Al crear: habitaciones del POST. Al editar: tipos existentes (se guardan uno a uno). */
    val habitaciones = mutableStateListOf(HabitacionForm())
    var errores by mutableStateOf(mapOf<String, String>()); private set
    var cargando by mutableStateOf(false); private set
    var errorCarga by mutableStateOf<String?>(null); private set
    var guardando by mutableStateOf(false); private set
    var mensaje by mutableStateOf<String?>(null); private set
    var mensajeError by mutableStateOf<String?>(null); private set

    // Edición de un tipo (TRV-503)
    var habEditando by mutableStateOf<HabitacionForm?>(null); private set
    var erroresHab by mutableStateOf(mapOf<String, String>()); private set
    var guardandoHab by mutableStateOf(false); private set
    var habAEliminar by mutableStateOf<HabitacionForm?>(null)
    var errorEliminarHab by mutableStateOf<String?>(null); private set
    private var iniciado = false

    fun iniciar(id: Int?) {
        if (iniciado) return
        iniciado = true
        hotelId = id
        if (id != null) recargar()
    }

    private fun recargar() {
        val id = hotelId ?: return
        viewModelScope.launch {
            cargando = true; errorCarga = null
            when (val r = Repository.hotel(id)) {
                is Result.Ok -> {
                    val h = r.value.hotel
                    nombre = h?.nombre.orEmpty(); descripcion = h?.descripcion.orEmpty(); ciudad = h?.ciudad.orEmpty()
                    pais = h?.pais.orEmpty(); direccion = h?.direccion.orEmpty(); imagen = h?.imagenUrl.orEmpty(); estrellas = h?.estrellas ?: 3
                    habitaciones.clear(); habitaciones.addAll(r.value.habitaciones.orEmpty().map { HabitacionForm.from(it) })
                }
                is Result.Err -> errorCarga = r.message
            }
            cargando = false
        }
    }

    private fun validar(): Map<String, String> = buildMap {
        if (nombre.isBlank() || nombre.length > 150) put("nombre", "Nombre obligatorio (máx. 150).")
        if (ciudad.isBlank()) put("ciudad", "Ciudad obligatoria.")
        if (pais.isBlank()) put("pais", "País obligatorio.")
        if (direccion.isBlank()) put("direccion", "Dirección obligatoria.")
        if (!Validaciones.urlValida(imagen)) put("imagen_url", "La imagen principal es obligatoria (URL http/https).")
        if (hotelId == null) {
            if (habitaciones.isEmpty()) put("habitaciones", "Agrega al menos un tipo de habitación.")
            val nombres = habitaciones.map { it.nombre.trim().lowercase() }
            if (nombres.size != nombres.toSet().size) put("habitaciones", "Los nombres de habitación no se pueden repetir.")
            habitaciones.forEachIndexed { i, h -> if (h.errores().isNotEmpty()) put("habitaciones", "Revisa el tipo de habitación ${i + 1}: ${h.errores().values.first()}") }
        }
    }

    /** TRV-502 — Crear (un solo POST con habitaciones) o actualizar los datos del hotel. */
    fun guardar(onCreado: () -> Unit) {
        val e = validar()
        errores = e; mensaje = null; mensajeError = null
        if (e.isNotEmpty()) { mensajeError = e.values.first(); return }
        val id = hotelId
        val body = HotelUpsert(
            nombre.trim(), descripcion.trim().ifBlank { null }, ciudad.trim(), pais.trim(), imagen.trim(), direccion.trim(), estrellas,
            habitaciones = if (id == null) habitaciones.map { it.toUpsert() } else null,
        )
        viewModelScope.launch {
            guardando = true
            val r = if (id == null) Repository.crearHotel(body) else Repository.editarHotel(id, body)
            when (r) {
                is Result.Ok -> { mensaje = r.value; if (id == null) onCreado() }
                is Result.Err -> { errores = r.fields; mensajeError = r.message }
            }
            guardando = false
        }
    }

    fun nuevaHabitacionLocal() { habitaciones.add(HabitacionForm()) }
    fun quitarHabitacionLocal(h: HabitacionForm) { if (habitaciones.size > 1) habitaciones.remove(h) }

    fun abrirHabitacion(h: HabitacionForm?) { habEditando = h ?: HabitacionForm(); erroresHab = emptyMap() }
    fun cerrarHabitacion() { if (!guardandoHab) habEditando = null }

    /** TRV-503 — Crear o editar un tipo de habitación de un hotel existente. */
    fun guardarHabitacion() {
        val h = habEditando ?: return
        val id = hotelId ?: return
        val e = h.errores()
        erroresHab = e
        if (e.isNotEmpty()) return
        viewModelScope.launch {
            guardandoHab = true
            val r = if (h.id == null) Repository.crearHabitacion(id, h.toUpsert()) else Repository.editarHabitacion(h.id, h.toUpsert())
            when (r) {
                is Result.Ok -> { habEditando = null; mensaje = "Tipo de habitación guardado."; mensajeError = null; recargar() }
                is Result.Err -> erroresHab = r.fields.ifEmpty { mapOf("general" to r.message) }
            }
            guardandoHab = false
        }
    }

    /** 409 si el tipo tiene reservas activas. */
    fun eliminarHabitacion() {
        val h = habAEliminar ?: return
        val hid = h.id ?: return
        viewModelScope.launch {
            guardandoHab = true; errorEliminarHab = null
            when (val r = Repository.eliminarHabitacion(hid)) {
                is Result.Ok -> { habAEliminar = null; mensaje = "Tipo de habitación eliminado."; recargar() }
                is Result.Err -> errorEliminarHab = r.message
            }
            guardandoHab = false
        }
    }
}

@Composable
fun HotelFormScreen(hotelId: Int?, onCreado: () -> Unit, vm: HotelFormViewModel = viewModel(key = "hotel_form_${hotelId ?: "nuevo"}")) {
    LaunchedEffect(hotelId) { vm.iniciar(hotelId) }
    if (vm.cargando) { Loading(); return }
    val errorCarga = vm.errorCarga
    if (errorCarga != null) { ErrorState(errorCarga); return }
    val editando = vm.hotelId != null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("pantalla_form_hotel"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(if (editando) "Editar hotel" else "Publicar hotel", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Campo(vm.nombre, { vm.nombre = it }, "Nombre", "hotel_nombre", error = vm.errores["nombre"])
        Campo(vm.descripcion, { vm.descripcion = it }, "Descripción", "hotel_descripcion", singleLine = false, minLines = 3)
        Campo(vm.direccion, { vm.direccion = it }, "Dirección", "hotel_direccion", error = vm.errores["direccion"])
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Campo(vm.ciudad, { vm.ciudad = it }, "Ciudad", "hotel_ciudad", Modifier.weight(1f), error = vm.errores["ciudad"])
            Campo(vm.pais, { vm.pais = it }, "País", "hotel_pais", Modifier.weight(1f), error = vm.errores["pais"])
        }
        Campo(vm.imagen, { vm.imagen = it }, "Imagen principal (URL)", "hotel_imagen_url", error = vm.errores["imagen_url"], keyboardType = KeyboardType.Uri)
        Stepper("Estrellas", vm.estrellas, 1, 5, "hotel_estrellas") { vm.estrellas = it }

        SectionTitle("Tipos de habitación", Modifier.padding(top = 8.dp))
        if (!editando) {
            vm.habitaciones.forEachIndexed { i, h -> HabitacionEditor(h, i, onQuitar = if (vm.habitaciones.size > 1) { { vm.quitarHabitacionLocal(h) } } else null) }
            OutlinedButton(onClick = { vm.nuevaHabitacionLocal() }, modifier = Modifier.tag("btn_agregar_habitacion")) { Text("+ Agregar tipo de habitación") }
        } else {
            vm.habitaciones.forEach { h ->
                Card(Modifier.fillMaxWidth().tag("hab_item_${h.id}")) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(h.nombre, fontWeight = FontWeight.SemiBold)
                            Text("${h.cantidad} unidades · ${h.adultos} ad. / ${h.ninos} niños · ${formatPrice(h.precio.toDoubleOrNull())}", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { vm.abrirHabitacion(h) }, modifier = Modifier.tag("hab_editar_${h.id}")) { Text("Editar") }
                        TextButton(onClick = { vm.habAEliminar = h }, modifier = Modifier.tag("hab_eliminar_${h.id}")) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
            OutlinedButton(onClick = { vm.abrirHabitacion(null) }, modifier = Modifier.tag("btn_nueva_habitacion")) { Text("+ Nuevo tipo de habitación") }
        }

        vm.mensaje?.let { Banner(it, error = false, tagId = "form_ok") }
        vm.mensajeError?.let { Banner(it, error = true, tagId = "form_error") }
        Button(onClick = { vm.guardar(onCreado) }, enabled = !vm.guardando, modifier = Modifier.fillMaxWidth().height(50.dp).tag("btn_guardar_hotel")) {
            if (vm.guardando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text(if (editando) "Guardar cambios" else "Publicar hotel")
        }
        Spacer(Modifier.height(24.dp))
    }

    val h = vm.habEditando
    if (h != null) {
        AppDialog(
            title = if (h.id == null) "Nuevo tipo de habitación" else "Editar ${h.nombre}",
            onDismiss = { vm.cerrarHabitacion() },
            tagId = "dialogo_habitacion",
            buttons = {
                TextButton(onClick = { vm.cerrarHabitacion() }, modifier = Modifier.tag("hab_cancelar")) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.guardarHabitacion() }, enabled = !vm.guardandoHab, modifier = Modifier.tag("hab_guardar")) { Text("Guardar") }
            },
        ) {
            HabitacionCampos(h, vm.erroresHab)
            vm.erroresHab["general"]?.let { Banner(it, error = true, tagId = "hab_error") }
        }
    }
    val del = vm.habAEliminar
    if (del != null) {
        ConfirmDialog(
            title = "¿Quitar «${del.nombre}»?",
            message = "No se puede quitar si tiene reservas activas.",
            confirmLabel = "Quitar",
            onConfirm = { vm.eliminarHabitacion() },
            onDismiss = { vm.habAEliminar = null },
            tagId = "dialogo_eliminar_habitacion",
            working = vm.guardandoHab,
            error = vm.errorEliminarHab,
        )
    }
}

@Composable
private fun HabitacionEditor(h: HabitacionForm, index: Int, onQuitar: (() -> Unit)?) {
    Card(Modifier.fillMaxWidth().tag("hab_form_$index")) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tipo ${index + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (onQuitar != null) TextButton(onClick = onQuitar, modifier = Modifier.tag("hab_quitar_$index")) { Text("Quitar") }
            }
            HabitacionCampos(h, h.errores().takeIf { h.nombre.isNotEmpty() || h.precio.isNotEmpty() } ?: emptyMap(), "_$index")
        }
    }
}

@Composable
private fun HabitacionCampos(h: HabitacionForm, errores: Map<String, String>, sufijo: String = "") {
    Campo(h.nombre, { h.nombre = it }, "Nombre (p. ej. Doble)", "hab_nombre$sufijo", error = errores["nombre"])
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Campo(h.adultos, { h.adultos = it.filter(Char::isDigit) }, "Adultos", "hab_adultos$sufijo", Modifier.weight(1f), error = errores["adultos"] ?: errores["capacidad_adultos"], keyboardType = KeyboardType.Number)
        Campo(h.ninos, { h.ninos = it.filter(Char::isDigit) }, "Niños", "hab_ninos$sufijo", Modifier.weight(1f), error = errores["ninos"] ?: errores["capacidad_ninos"], keyboardType = KeyboardType.Number)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Campo(h.cantidad, { h.cantidad = it.filter(Char::isDigit) }, "Unidades", "hab_cantidad$sufijo", Modifier.weight(1f), error = errores["cantidad"], keyboardType = KeyboardType.Number)
        Campo(h.precio, { h.precio = it.filter { c -> c.isDigit() || c == '.' || c == ',' } }, "S/ por noche", "hab_precio$sufijo", Modifier.weight(1f),
            error = errores["precio"] ?: errores["precio_por_noche"], keyboardType = KeyboardType.Decimal)
    }
    Campo(h.descripcion, { h.descripcion = it }, "Descripción (opcional)", "hab_descripcion$sufijo")
}
