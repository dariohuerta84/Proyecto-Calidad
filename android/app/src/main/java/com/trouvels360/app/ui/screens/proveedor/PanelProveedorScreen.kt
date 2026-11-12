package com.trouvels360.app.ui.screens.proveedor

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.onOk
import com.trouvels360.app.data.remote.IndicadoresDto
import com.trouvels360.app.data.remote.ServicioProveedorDto
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.CalificacionTexto
import com.trouvels360.app.ui.components.Campo
import com.trouvels360.app.ui.components.ConfirmDialog
import com.trouvels360.app.ui.components.EmptyState
import com.trouvels360.app.ui.components.ErrorState
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.NetImage
import com.trouvels360.app.ui.components.PaginationBar
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch
import java.util.Locale

class PanelProveedorViewModel : ViewModel() {
    var indicadores by mutableStateOf<IndicadoresDto?>(null); private set
    var items by mutableStateOf<List<ServicioProveedorDto>>(emptyList()); private set
    var page by mutableIntStateOf(1); private set
    var lastPage by mutableIntStateOf(1); private set
    var total by mutableIntStateOf(0); private set
    var tipo by mutableStateOf<String?>(null); private set
    var busqueda by mutableStateOf("")
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set
    var mensaje by mutableStateOf<String?>(null)
    var mensajeError by mutableStateOf<String?>(null)
    var aEliminar by mutableStateOf<ServicioProveedorDto?>(null)
    var eliminando by mutableStateOf(false); private set

    fun refrescar() { cargarIndicadores(); cargar(page) }

    fun cargarIndicadores() {
        viewModelScope.launch { Repository.indicadores().onOk { indicadores = it } }
    }

    fun filtrarTipo(t: String?) { tipo = t; cargar(1) }

    /** HU-P03 — Publicaciones propias paginadas (15), con conteos por estado y reseñas. */
    fun cargar(p: Int) {
        viewModelScope.launch {
            loading = true; error = null
            when (val r = Repository.misServicios(p, tipo, busqueda)) {
                is Result.Ok -> { items = r.value.items; page = r.value.page; lastPage = r.value.lastPage; total = r.value.total }
                is Result.Err -> error = r.message
            }
            loading = false
        }
    }

    /** HU-P15 — 409 si la publicación tiene reservas activas (se muestra el mensaje de la API). */
    fun eliminar() {
        val s = aEliminar ?: return
        val id = s.id ?: return
        viewModelScope.launch {
            eliminando = true
            when (val r = Repository.eliminarServicio(id)) {
                is Result.Ok -> {
                    mensaje = "«${s.nombre}» se eliminó. Su historial de reservas se conserva."; mensajeError = null
                    cargar(if (items.size == 1 && page > 1) page - 1 else page); cargarIndicadores()
                }
                is Result.Err -> { mensajeError = r.message; mensaje = null }
            }
            eliminando = false
            aEliminar = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelProveedorScreen(
    onEditarHotel: (Int) -> Unit,
    onEditarTour: (Int) -> Unit,
    onPrograma: (Int) -> Unit,
    onReservas: (servicioId: Int?, estado: String?) -> Unit,
    onCrearHotel: () -> Unit,
    onCrearTour: () -> Unit,
    vm: PanelProveedorViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { vm.refrescar() }

    LazyColumn(Modifier.fillMaxSize().tag("pantalla_panel_proveedor"), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCrearHotel, modifier = Modifier.weight(1f).tag("btn_crear_hotel")) { Text("+ Hotel") }
                Button(onClick = onCrearTour, modifier = Modifier.weight(1f).tag("btn_crear_tour")) { Text("+ Tour") }
            }
        }
        vm.indicadores?.let { ind ->
            item {
                val rh = ind.reservasHotel.orEmpty()
                val rt = ind.reservasTour.orEmpty()
                Column(Modifier.fillMaxWidth().tag("indicadores"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Indicador("Por decidir", "${ind.solicitudesPendientes ?: 0}", "ind_pendientes", Modifier.weight(1f), onClick = { onReservas(null, "pendiente") })
                        Indicador("Confirmadas", "${(rh["confirmada"] ?: 0) + (rt["confirmada"] ?: 0)}", "ind_confirmadas", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Indicador("Rechazadas", "${rh["rechazada"] ?: 0}", "ind_rechazadas", Modifier.weight(1f))
                        Indicador("Canceladas", "${(rh["cancelada"] ?: 0) + (rt["cancelada"] ?: 0)}", "ind_canceladas", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Indicador("Ingresos del mes", formatPrice(ind.ingresosMes), "ind_ingresos", Modifier.weight(1f))
                        Indicador(
                            "Calificación",
                            if ((ind.resenas?.cantidad ?: 0) > 0) "%.1f ★ (%d)".format(Locale.US, ind.resenas?.promedio ?: 0.0, ind.resenas?.cantidad ?: 0) else "Sin reseñas",
                            "ind_resenas", Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = vm.tipo == null, onClick = { vm.filtrarTipo(null) }, label = { Text("Todo") }, modifier = Modifier.tag("filtro_tipo_todo"))
                    FilterChip(selected = vm.tipo == "hotel", onClick = { vm.filtrarTipo("hotel") }, label = { Text("Hoteles") }, modifier = Modifier.tag("filtro_tipo_hotel"))
                    FilterChip(selected = vm.tipo == "tour", onClick = { vm.filtrarTipo("tour") }, label = { Text("Tours") }, modifier = Modifier.tag("filtro_tipo_tour"))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Campo(vm.busqueda, { vm.busqueda = it }, "Buscar por nombre", "filtro_busqueda", modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { vm.cargar(1) }, modifier = Modifier.tag("btn_filtrar")) { Text("Buscar") }
                }
            }
        }
        vm.mensaje?.let { m -> item { Banner(m, error = false, tagId = "panel_mensaje") } }
        vm.mensajeError?.let { m -> item { Banner(m, error = true, tagId = "panel_mensaje_error") } }

        when {
            vm.loading -> item { Loading(Modifier.height(160.dp)) }
            vm.error != null -> item { ErrorState(vm.error!!, onRetry = { vm.cargar(vm.page) }) }
            vm.items.isEmpty() -> item { EmptyState("Aún no tienes publicaciones con ese filtro.", tagId = "panel_vacio") }
            else -> {
                item { Text("${vm.total} publicaciones", style = MaterialTheme.typography.bodySmall, modifier = Modifier.tag("panel_total")) }
                items(vm.items, key = { "pub_${it.id}" }) { s ->
                    PublicacionCard(
                        s,
                        onEditar = { s.id?.let { if (s.esHotel) onEditarHotel(it) else onEditarTour(it) } },
                        onPrograma = { s.id?.let(onPrograma) },
                        onReservas = { onReservas(s.id, null) },
                        onEliminar = { vm.aEliminar = s },
                    )
                }
                item { PaginationBar(vm.page, vm.lastPage, { vm.cargar(it) }, tagPrefix = "panel_pagina") }
            }
        }
    }

    val s = vm.aEliminar
    if (s != null) {
        ConfirmDialog(
            title = "¿Eliminar «${s.nombre}»?",
            message = "Dejará de aparecer en el catálogo. El historial de reservas se conserva. No se puede eliminar si tiene reservas activas.",
            confirmLabel = "Sí, eliminar",
            onConfirm = { vm.eliminar() },
            onDismiss = { vm.aEliminar = null },
            tagId = "dialogo_eliminar_publicacion",
            working = vm.eliminando,
        )
    }
}

@Composable
private fun Indicador(titulo: String, valor: String, tagId: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val contenido: @Composable () -> Unit = {
        Column(Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.tag(tagId))
        }
    }
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    if (onClick != null) Card(onClick = onClick, colors = colores, modifier = modifier) { contenido() }
    else Card(colors = colores, modifier = modifier) { contenido() }
}

@Composable
private fun PublicacionCard(
    s: ServicioProveedorDto,
    onEditar: () -> Unit,
    onPrograma: () -> Unit,
    onReservas: () -> Unit,
    onEliminar: () -> Unit,
) {
    Card(Modifier.fillMaxWidth().tag("publicacion_${s.id}")) {
        Row(Modifier.padding(12.dp)) {
            NetImage(s.imagenUrl, Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (s.esHotel) "HOTEL" else "TOUR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(s.nombre ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.tag("publicacion_nombre"))
                Text(listOfNotNull(s.ciudad, s.pais).joinToString(", "), style = MaterialTheme.typography.bodySmall)
                CalificacionTexto(s.resenas?.promedio, s.resenas?.cantidad)
                val m = s.metaTipo
                Text(
                    if (s.esHotel) "Desde ${formatPrice(m?.tarifaMinDesde)} · ${m?.habitacionesCount ?: 0} tipos de habitación"
                    else "${formatPrice(m?.precio)} · ${m?.proximasSalidas ?: 0} salidas próximas",
                    style = MaterialTheme.typography.bodySmall,
                )
                val t = s.reservasTotales
                Text(
                    (if (s.esHotel) "Pendientes ${t?.pendientes ?: 0} · " else "") + "Confirmadas ${t?.confirmadas ?: 0} · " +
                        (if (s.esHotel) "Rechazadas ${t?.rechazadas ?: 0} · " else "") + "Canceladas ${t?.canceladas ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.tag("publicacion_conteos"),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 4.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onEditar, modifier = Modifier.tag("btn_editar_${s.id}")) { Text("Editar") }
            if (!s.esHotel) TextButton(onClick = onPrograma, modifier = Modifier.tag("btn_programa_${s.id}")) { Text("Salidas") }
            TextButton(onClick = onReservas, modifier = Modifier.tag("btn_reservas_${s.id}")) { Text("Reservas") }
            TextButton(onClick = onEliminar, modifier = Modifier.tag("btn_eliminar_${s.id}")) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
        }
    }
}
