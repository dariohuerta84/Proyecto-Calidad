package com.trouvels360.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.trouvels360.app.data.Validaciones
import com.trouvels360.app.data.onOk
import com.trouvels360.app.data.remote.MiResena
import com.trouvels360.app.data.remote.PuedoOpinar
import com.trouvels360.app.data.remote.ResumenResenas
import com.trouvels360.app.data.remote.ReviewDto
import com.trouvels360.app.ui.theme.Star
import kotlinx.coroutines.launch
import java.util.Locale

/** Estado de la sección de reseñas de un servicio (TRV-403). */
class ReviewsViewModel : ViewModel() {
    var servicioId = 0; private set
    var reviews by mutableStateOf<List<ReviewDto>>(emptyList()); private set
    var page by mutableIntStateOf(1); private set
    var lastPage by mutableIntStateOf(1); private set
    var resumen by mutableStateOf<ResumenResenas?>(null); private set
    var elegibilidad by mutableStateOf<PuedoOpinar?>(null); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set
    var aviso by mutableStateOf<String?>(null)
    var avisoError by mutableStateOf<String?>(null)

    // Formulario
    var editando by mutableStateOf<MiResena?>(null); private set
    var formAbierto by mutableStateOf(false); private set
    var enviando by mutableStateOf(false); private set
    var errorForm by mutableStateOf<String?>(null); private set

    var aEliminar by mutableStateOf<ReviewDto?>(null)
    var eliminando by mutableStateOf(false); private set

    fun cargar(id: Int) {
        if (servicioId == id) return
        servicioId = id
        refrescar(1)
    }

    fun refrescar(p: Int = page) {
        cargarPagina(p)
        viewModelScope.launch { Repository.resumenReviews(servicioId).onOk { resumen = it } }
        cargarElegibilidad()
    }

    fun cargarPagina(p: Int) {
        viewModelScope.launch {
            loading = true; error = null
            when (val r = Repository.reviews(servicioId, p)) {
                is Result.Ok -> { reviews = r.value.items; page = r.value.page; lastPage = r.value.lastPage }
                is Result.Err -> error = r.message
            }
            loading = false
        }
    }

    fun cargarElegibilidad() {
        val u = Session.user.value
        if (u == null || !u.esViajero) { elegibilidad = null; return }
        viewModelScope.launch { Repository.puedoOpinar(servicioId).onOk { elegibilidad = it } }
    }

    fun abrirNueva() { editando = elegibilidad?.miResena; errorForm = null; formAbierto = true }
    fun abrirEdicion(r: ReviewDto) {
        editando = MiResena(r.id, r.calificacion, r.comentario); errorForm = null; formAbierto = true
    }
    fun cerrarForm() { if (!enviando) formAbierto = false }

    fun guardar(calificacion: Int, comentario: String) {
        if (calificacion !in 1..5) { errorForm = "Selecciona de 1 a 5 estrellas."; return }
        if (!Validaciones.comentarioValido(comentario)) { errorForm = "El comentario debe tener entre 10 y 300 caracteres."; return }
        val ed = editando
        viewModelScope.launch {
            enviando = true; errorForm = null
            val r = if (ed?.id != null) Repository.editarReview(ed.id, calificacion, comentario)
            else Repository.crearReview(servicioId, calificacion, comentario)
            when (r) {
                is Result.Ok -> {
                    formAbierto = false; avisoError = null
                    aviso = if (ed != null) "Tu reseña se actualizó." else "¡Gracias! Tu reseña se publicó."
                    refrescar(if (ed != null) page else 1)
                }
                is Result.Err -> errorForm = r.message
            }
            enviando = false
        }
    }

    fun eliminar() {
        val r = aEliminar ?: return
        val id = r.id ?: return
        viewModelScope.launch {
            eliminando = true
            when (val res = Repository.eliminarReview(id)) {
                is Result.Ok -> { aviso = "Reseña eliminada."; avisoError = null; refrescar(if (reviews.size == 1 && page > 1) page - 1 else page) }
                is Result.Err -> { avisoError = res.message; aviso = null }
            }
            eliminando = false
            aEliminar = null
        }
    }
}

/**
 * Sección de reseñas para el detalle de hotel o tour: resumen real, lista paginada (5 por página),
 * botón "Deja tu opinión" solo si la API lo permite (si no, muestra el motivo) y edición/eliminación
 * de la reseña propia.
 */
@Composable
fun ReviewsSection(servicioId: Int, onLogin: () -> Unit, modifier: Modifier = Modifier) {
    val vm: ReviewsViewModel = viewModel(key = "reviews_$servicioId")
    LaunchedEffect(servicioId) { vm.cargar(servicioId) }
    val user by Session.user.collectAsState()
    LaunchedEffect(user?.id) { if (vm.servicioId != 0) vm.cargarElegibilidad() }

    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp).tag("seccion_resenas"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Opiniones")
        val res = vm.resumen
        if (res != null && (res.total ?: 0) > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.tag("resenas_resumen")) {
                Icon(Icons.Filled.Star, null, Modifier.size(20.dp), tint = Star)
                Text(" %.1f".format(Locale.US, res.promedio ?: 0.0), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.tag("resenas_promedio"))
                Text("  ·  ${res.total} ${if (res.total == 1) "reseña" else "reseñas"}", modifier = Modifier.tag("resenas_total"))
            }
        }

        val e = vm.elegibilidad
        when {
            user == null -> OutlinedButton(onClick = onLogin, modifier = Modifier.tag("btn_login_para_opinar")) { Text("Inicia sesión para opinar") }
            user?.esViajero == true && e?.miResena != null ->
                OutlinedButton(onClick = { vm.abrirNueva() }, modifier = Modifier.tag("btn_editar_mi_opinion")) { Text("Editar mi opinión") }
            user?.esViajero == true && e?.puede == true ->
                Button(onClick = { vm.abrirNueva() }, modifier = Modifier.tag("btn_dejar_opinion")) { Text("Deja tu opinión") }
            user?.esViajero == true && e != null && e.puede != true ->
                Text(e.motivo ?: "Aún no puedes opinar sobre este servicio.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.tag("resena_no_elegible"))
        }
        vm.aviso?.let { Banner(it, error = false, tagId = "resenas_aviso") }
        vm.avisoError?.let { Banner(it, error = true, tagId = "resenas_aviso_error") }

        when {
            vm.loading -> CircularProgressIndicator(Modifier.size(24.dp))
            vm.error != null -> {
                Text(vm.error!!, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { vm.cargarPagina(vm.page) }, modifier = Modifier.tag("resenas_reintentar")) { Text("Reintentar") }
            }
            vm.reviews.isEmpty() -> Text("Aún no hay reseñas.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.tag("resenas_vacio"))
            else -> vm.reviews.forEach { r ->
                ReviewItem(r, esMia = user?.id != null && r.usuario?.id == user?.id, onEditar = { vm.abrirEdicion(r) }, onEliminar = { vm.aEliminar = r })
            }
        }
        PaginationBar(vm.page, vm.lastPage, { vm.cargarPagina(it) }, tagPrefix = "resenas_pagina")
    }

    if (vm.formAbierto) {
        ReviewFormDialog(
            inicial = vm.editando,
            enviando = vm.enviando,
            error = vm.errorForm,
            onGuardar = { c, t -> vm.guardar(c, t) },
            onCerrar = { vm.cerrarForm() },
        )
    }
    if (vm.aEliminar != null) {
        ConfirmDialog(
            title = "¿Eliminar tu reseña?",
            message = "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            onConfirm = { vm.eliminar() },
            onDismiss = { vm.aEliminar = null },
            tagId = "dialogo_eliminar_resena",
            working = vm.eliminando,
        )
    }
}

@Composable
private fun ReviewItem(r: ReviewDto, esMia: Boolean, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp)
            .tag("resena_${r.id}"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(r.usuario?.nombreCompleto ?: "Viajero", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Stars(r.calificacion, 14.dp)
            if (esMia) {
                IconButton(onClick = onEditar, modifier = Modifier.size(36.dp).tag("resena_editar")) { Icon(Icons.Filled.Edit, "Editar reseña") }
                IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp).tag("resena_eliminar")) { Icon(Icons.Filled.Delete, "Eliminar reseña") }
            }
        }
        if (!r.comentario.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(r.comentario, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.tag("resena_texto"))
        }
        val editada = r.updatedAt != null && r.updatedAt != r.createdAt
        Text(
            (r.fechaFormateada ?: fecha(r.createdAt)) + if (editada) " · editada" else "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReviewFormDialog(
    inicial: MiResena?,
    enviando: Boolean,
    error: String?,
    onGuardar: (Int, String) -> Unit,
    onCerrar: () -> Unit,
) {
    var estrellas by remember { mutableIntStateOf(inicial?.calificacion ?: 0) }
    var texto by remember { mutableStateOf(inicial?.comentario ?: "") }
    val largo = texto.trim().length

    AppDialog(
        title = if (inicial != null) "Edita tu reseña" else "Cuéntanos tu experiencia",
        onDismiss = onCerrar,
        tagId = "dialogo_resena",
        buttons = {
            TextButton(onClick = onCerrar, enabled = !enviando, modifier = Modifier.tag("resena_cancelar")) { Text("Cancelar") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onGuardar(estrellas, texto) }, enabled = !enviando, modifier = Modifier.tag("resena_enviar")) {
                if (enviando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text(if (inicial != null) "Guardar" else "Publicar")
            }
        },
    ) {
        Row {
            (1..5).forEach { n ->
                IconButton(onClick = { estrellas = n }, modifier = Modifier.tag("resena_estrella_$n")) {
                    Icon(if (n <= estrellas) Icons.Filled.Star else Icons.Outlined.StarOutline, "$n estrellas", tint = Star)
                }
            }
        }
        Campo(texto, { if (it.length <= 300) texto = it }, "Comentario (10 a 300 caracteres)", "resena_comentario",
            singleLine = false, minLines = 4, supporting = "$largo/300")
        if (error != null) Banner(error, error = true, tagId = "resena_error")
    }
}
