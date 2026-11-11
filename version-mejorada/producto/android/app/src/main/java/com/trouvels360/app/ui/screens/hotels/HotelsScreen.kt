package com.trouvels360.app.ui.screens.hotels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.trouvels360.app.data.remote.HotelItemDto
import com.trouvels360.app.ui.BusquedaHotel
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

class HotelsViewModel : ViewModel() {
    var items by mutableStateOf<List<HotelItemDto>>(emptyList()); private set
    var total by mutableIntStateOf(0); private set
    var page by mutableIntStateOf(1); private set
    var lastPage by mutableIntStateOf(1); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    private var cargado = false

    fun primeraCarga() { if (!cargado) buscar(1) }

    /** TRV-201: con fechas, la API devuelve solo hoteles con unidades libres en todo el rango. */
    fun buscar(p: Int) {
        cargado = true
        viewModelScope.launch {
            loading = true; error = null
            val b = BusquedaHotel
            val ci = b.checkIn?.toString()
            val co = b.checkOut?.toString()
            when (val r = Repository.buscarHoteles(b.ciudad, ci, co, b.adultos, b.ninos, b.habitaciones, p)) {
                is Result.Ok -> {
                    items = r.value.items; total = r.value.total; page = r.value.page; lastPage = r.value.lastPage
                }
                is Result.Err -> { error = r.message; items = emptyList() }
            }
            loading = false
        }
    }
}

@Composable
fun HotelsScreen(onOpen: (Int) -> Unit, vm: HotelsViewModel = viewModel()) {
    LaunchedEffect(Unit) { vm.primeraCarga() }
    val b = BusquedaHotel

    LazyColumn(
        Modifier.fillMaxSize().tag("pantalla_hoteles"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HeroHeader("Encuentra tu alojamiento", "Busca por ciudad y fechas; solo verás hoteles con disponibilidad real.") }
        item {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Campo(b.ciudad, { b.ciudad = it }, "Ciudad", "buscar_ciudad", placeholder = "Cusco")
                    DateRangeField(b.checkIn, b.checkOut, { a, c -> b.checkIn = a; b.checkOut = c }, tagId = "buscar_fechas")
                    Stepper("Adultos", b.adultos, 1, 10, "buscar_adultos") { b.adultos = it }
                    Stepper("Niños", b.ninos, 0, 10, "buscar_ninos") { b.ninos = it }
                    Stepper("Habitaciones", b.habitaciones, 1, 10, "buscar_habitaciones") { b.habitaciones = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.buscar(1) }, modifier = Modifier.weight(1f).tag("btn_buscar_hoteles")) { Text("Buscar") }
                        TextButton(
                            onClick = { b.ciudad = ""; b.checkIn = null; b.checkOut = null; b.adultos = 2; b.ninos = 0; b.habitaciones = 1; vm.buscar(1) },
                            modifier = Modifier.tag("btn_limpiar_busqueda"),
                        ) { Text("Limpiar") }
                    }
                }
            }
        }
        when {
            vm.loading -> item { Loading(Modifier.padding(32.dp)) }
            vm.error != null -> item { ErrorState(vm.error!!, onRetry = { vm.buscar(vm.page) }) }
            vm.items.isEmpty() -> item {
                EmptyState(
                    if (b.conFechas) "No hay hoteles con disponibilidad para esas fechas. Prueba otras fechas, otra ciudad o menos huéspedes."
                    else "No encontramos hoteles con ese criterio.",
                    tagId = "hoteles_vacio",
                )
            }
            else -> {
                item {
                    Text(
                        "${vm.total} hoteles" + if (b.conFechas) " con disponibilidad" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp).tag("hoteles_total"),
                    )
                }
                items(vm.items, key = { it.id ?: it.hashCode() }) { h ->
                    ServiceCard(
                        title = h.nombre ?: "Hotel",
                        imageUrl = h.imagenUrl,
                        location = listOfNotNull(h.ciudad, h.pais).joinToString(", "),
                        price = formatPrice(h.precioPorNoche),
                        priceSuffix = "/ noche + IGV",
                        onClick = { h.id?.let(onOpen) },
                        stars = h.estrellas,
                        promedio = h.calificacion?.promedio,
                        cantidadResenas = h.calificacion?.cantidad,
                        modifier = Modifier.padding(horizontal = 16.dp).tag("hotel_card_${h.id}"),
                    )
                }
                item { PaginationBar(vm.page, vm.lastPage, { vm.buscar(it) }, tagPrefix = "hoteles_pagina") }
            }
        }
    }
}
