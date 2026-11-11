package com.trouvels360.app.ui.screens.tours

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.remote.TourItemDto
import com.trouvels360.app.ui.BusquedaTour
import com.trouvels360.app.ui.CATEGORIAS_TOUR
import com.trouvels360.app.ui.components.Campo
import com.trouvels360.app.ui.components.DateRangeField
import com.trouvels360.app.ui.components.EmptyState
import com.trouvels360.app.ui.components.ErrorState
import com.trouvels360.app.ui.components.HeroHeader
import com.trouvels360.app.ui.components.Loading
import com.trouvels360.app.ui.components.PaginationBar
import com.trouvels360.app.ui.components.ServiceCard
import com.trouvels360.app.ui.components.Stepper
import com.trouvels360.app.ui.components.formatPrice
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch

class ToursViewModel : ViewModel() {
    var items by mutableStateOf<List<TourItemDto>>(emptyList()); private set
    var total by mutableIntStateOf(0); private set
    var page by mutableIntStateOf(1); private set
    var lastPage by mutableIntStateOf(1); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    private var cargado = false

    fun primeraCarga() { if (!cargado) buscar(1) }

    /** TRV-301: solo tours con al menos una salida programada con cupo para esas fechas y personas. */
    fun buscar(p: Int) {
        cargado = true
        viewModelScope.launch {
            loading = true; error = null
            val b = BusquedaTour
            when (val r = Repository.buscarTours(b.destino, b.categoria, b.desde?.toString(), b.hasta?.toString(), b.personas, p)) {
                is Result.Ok -> { items = r.value.items; total = r.value.total; page = r.value.page; lastPage = r.value.lastPage }
                is Result.Err -> { error = r.message; items = emptyList() }
            }
            loading = false
        }
    }
}

private fun duracionTexto(min: Int?): String = when {
    min == null || min <= 0 -> ""
    min % 60 == 0 -> "${min / 60} h"
    min > 60 -> "${min / 60} h ${min % 60} min"
    else -> "$min min"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToursScreen(onOpen: (Int) -> Unit, vm: ToursViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.primeraCarga() }
    val b = BusquedaTour

    LazyColumn(
        Modifier.fillMaxSize().tag("pantalla_tours"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HeroHeader("Vive experiencias", "Tours con salidas programadas y cupos en tiempo real.") }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Campo(b.destino, { b.destino = it }, "Destino", "buscar_destino_tour", placeholder = "Cusco")
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = b.categoria == null, onClick = { b.categoria = null }, label = { Text("Todas") }, modifier = Modifier.tag("categoria_todas"))
                        CATEGORIAS_TOUR.forEach { c ->
                            FilterChip(selected = b.categoria == c, onClick = { b.categoria = c }, label = { Text(c) }, modifier = Modifier.tag("categoria_$c"))
                        }
                    }
                    DateRangeField(b.desde, b.hasta, { a, c -> b.desde = a; b.hasta = c }, tagId = "buscar_fechas_tour", placeholder = "Rango de fechas (opcional)")
                    Stepper("Personas", b.personas, 1, 15, "buscar_personas") { b.personas = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.buscar(1) }, modifier = Modifier.weight(1f).tag("btn_buscar_tours")) { Text("Buscar") }
                        TextButton(onClick = { b.destino = ""; b.categoria = null; b.desde = null; b.hasta = null; b.personas = 1; vm.buscar(1) },
                            modifier = Modifier.tag("btn_limpiar_tours")) { Text("Limpiar") }
                    }
                }
            }
        }
        when {
            vm.loading -> item { Loading(Modifier.padding(32.dp)) }
            vm.error != null -> item { ErrorState(vm.error!!, onRetry = { vm.buscar(vm.page) }) }
            vm.items.isEmpty() -> item { EmptyState("No hay tours con salidas disponibles para ese criterio.", tagId = "tours_vacio") }
            else -> {
                item {
                    Text("${vm.total} tours", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp).tag("tours_total"))
                }
                items(vm.items, key = { it.id ?: it.hashCode() }) { t ->
                    ServiceCard(
                        title = t.nombre ?: "Tour",
                        imageUrl = t.imagenUrl,
                        location = listOfNotNull(t.ciudad, t.pais).joinToString(", ") + duracionTexto(t.tour?.duracion).let { if (it.isEmpty()) "" else " · $it" },
                        price = formatPrice(t.tour?.precio),
                        priceSuffix = "/ persona + IGV",
                        onClick = { t.id?.let(onOpen) },
                        badge = t.tour?.categoria,
                        promedio = t.calificacion?.promedio,
                        cantidadResenas = t.calificacion?.cantidad,
                        modifier = Modifier.padding(horizontal = 16.dp).tag("tour_card_${t.id}"),
                    )
                }
                item { PaginationBar(vm.page, vm.lastPage, { vm.buscar(it) }, tagPrefix = "tours_pagina") }
            }
        }
    }
}
