package com.trouvels360.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val displayFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun LocalDate.toUtcMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
private object FromToday : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= LocalDate.now().toUtcMillis()
}

/**
 * Contenedor de selector de fechas dentro de un Dialog propio (con appiumRoot) para que Appium
 * pueda tocar los días y los botones por resource-id. El DatePicker de Material 3 también permite
 * cambiar a modo texto (ícono de lápiz), útil para escribir la fecha en las pruebas.
 */
@Composable
private fun DialogoFecha(
    tagId: String,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onTeclado: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 6.dp, modifier = Modifier.width(360.dp).appiumRoot().tag(tagId)) {
            Column {
                // Modo texto: permite escribir las fechas (accesibilidad y pruebas Appium sin depender del calendario)
                TextButton(onClick = onTeclado, modifier = Modifier.padding(start = 12.dp, top = 8.dp).tag("${tagId}_teclado")) {
                    Text("Escribir las fechas")
                }
                content()
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, modifier = Modifier.tag("${tagId}_cancelar")) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm, enabled = confirmEnabled, modifier = Modifier.tag("${tagId}_aceptar")) { Text("Aceptar") }
                }
            }
        }
    }
}

/** Botón que abre un selector de rango (check-in / check-out). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeField(
    start: LocalDate?,
    end: LocalDate?,
    onChange: (LocalDate, LocalDate) -> Unit,
    tagId: String = "fechas",
    placeholder: String = "Elegir fechas de entrada y salida",
) {
    var open by remember { mutableStateOf(false) }
    val label = if (start != null && end != null) "${start.format(displayFmt)}  →  ${end.format(displayFmt)}" else placeholder

    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().tag(tagId)) {
        Icon(Icons.Filled.DateRange, null)
        Text("  $label")
    }

    if (open) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = start?.toUtcMillis(),
            initialSelectedEndDateMillis = end?.toUtcMillis(),
            selectableDates = FromToday,
        )
        val ini = state.selectedStartDateMillis
        val fin = state.selectedEndDateMillis
        DialogoFecha(
            tagId = "${tagId}_dialogo",
            onDismiss = { open = false },
            confirmEnabled = ini != null && fin != null && fin > ini,
            onConfirm = {
                if (ini != null && fin != null) onChange(ini.toLocalDate(), fin.toLocalDate())
                open = false
            },
            onTeclado = { state.displayMode = DisplayMode.Input },
        ) {
            DateRangePicker(state = state, modifier = Modifier.height(500.dp), title = null)
        }
    }
}

/** Botón que abre el selector de una sola fecha. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(value: LocalDate?, onChange: (LocalDate) -> Unit, label: String, tagId: String, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = modifier.tag(tagId)) {
        Icon(Icons.Filled.DateRange, null)
        Text("  " + (value?.format(displayFmt) ?: label))
    }
    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = value?.toUtcMillis(), selectableDates = FromToday)
        val sel = state.selectedDateMillis
        DialogoFecha(
            tagId = "${tagId}_dialogo",
            onDismiss = { open = false },
            confirmEnabled = sel != null,
            onConfirm = {
                if (sel != null) onChange(sel.toLocalDate())
                open = false
            },
            onTeclado = { state.displayMode = DisplayMode.Input },
        ) { DatePicker(state = state) }
    }
}
