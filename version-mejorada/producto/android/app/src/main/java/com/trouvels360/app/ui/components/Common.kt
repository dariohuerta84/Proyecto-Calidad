package com.trouvels360.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.trouvels360.app.ui.theme.Star
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ============================================================ Appium

/**
 * Expone los testTag de Compose como resource-id de Android para que Appium (UiAutomator2)
 * los encuentre con By.id("…"). Se aplica en la raíz de la app y en la raíz de cada diálogo,
 * porque los diálogos son ventanas aparte y no heredan la propiedad.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.appiumRoot(): Modifier = this.semantics { testTagsAsResourceId = true }

/** Atajo para Modifier.testTag. */
fun Modifier.tag(id: String): Modifier = this.testTag(id)

// ============================================================ Formatos

private val soles: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
private val fechaLarga = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale("es", "PE"))
private val fechaMedia = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "PE"))

fun formatPrice(value: Double?): String = value?.let { soles.format(it) } ?: "—"

/** "2026-12-01" o "2026-12-01T00:00:00Z" → "1 dic 2026". */
fun fecha(value: String?): String = value?.take(10)?.let { s ->
    runCatching { LocalDate.parse(s).format(fechaMedia) }.getOrDefault(s)
} ?: "—"

fun fechaConDia(value: String?): String = value?.take(10)?.let { s ->
    runCatching { LocalDate.parse(s).format(fechaLarga) }.getOrDefault(s)
} ?: "—"

// ============================================================ Estados

/** Etiqueta de color por estado de reserva o salida. */
@Composable
fun EstadoChip(estado: String?, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (estado) {
        "pendiente" -> Color(0xFFFFF4E5) to Color(0xFF8A5300)
        "confirmada", "programada" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        "rechazada" -> Color(0xFFFDECEA) to Color(0xFFB71C1C)
        "cerrada" -> Color(0xFFFFF4E5) to Color(0xFF8A5300)
        else -> Color(0xFFECEFF1) to Color(0xFF455A64)
    }
    Surface(color = fondo, shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Text(
            (estado ?: "—").uppercase(),
            color = texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

// ============================================================ Estados de pantalla

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().tag("cargando"), Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp).tag("estado_error"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error, modifier = Modifier.tag("estado_error_texto"))
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRetry, modifier = Modifier.tag("btn_reintentar")) { Text("Reintentar") }
        }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier, tagId: String = "estado_vacio") {
    Box(modifier.fillMaxWidth().padding(24.dp).tag(tagId), Alignment.Center) {
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LoginRequired(message: String, buttonLabel: String = "Iniciar sesión", onLogin: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).tag("login_requerido"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogin, modifier = Modifier.tag("btn_ir_login")) { Text(buttonLabel) }
    }
}

/** Aviso en línea (verde si ok, rojo si error). */
@Composable
fun Banner(text: String, error: Boolean, modifier: Modifier = Modifier, tagId: String = if (error) "aviso_error" else "aviso_ok") {
    val fondo = if (error) Color(0xFFFDECEA) else Color(0xFFE8F5E9)
    val color = if (error) Color(0xFFB71C1C) else Color(0xFF1B5E20)
    Text(
        text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(fondo).padding(12.dp).tag(tagId),
    )
}

// ============================================================ Paginación

@Composable
fun PaginationBar(page: Int, lastPage: Int, onPage: (Int) -> Unit, tagPrefix: String = "pagina", modifier: Modifier = Modifier) {
    if (lastPage <= 1) return
    Row(
        modifier.fillMaxWidth().padding(vertical = 8.dp).tag("${tagPrefix}_barra"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = { onPage(page - 1) }, enabled = page > 1, modifier = Modifier.tag("${tagPrefix}_anterior")) { Text("‹ Anterior") }
        Text(
            "$page de $lastPage",
            modifier = Modifier.padding(horizontal = 12.dp).tag("${tagPrefix}_actual"),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(onClick = { onPage(page + 1) }, enabled = page < lastPage, modifier = Modifier.tag("${tagPrefix}_siguiente")) { Text("Siguiente ›") }
    }
}

// ============================================================ Diálogos

/**
 * Diálogo propio (en vez de AlertDialog) para que TODO su contenido, botones incluidos,
 * quede bajo appiumRoot() y Appium lo pueda localizar por resource-id.
 */
@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    tagId: String,
    buttons: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 6.dp, modifier = Modifier.appiumRoot().tag(tagId)) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                content()
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    buttons()
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    tagId: String = "dialogo_confirmar",
    working: Boolean = false,
    error: String? = null,
) {
    AppDialog(
        title = title,
        onDismiss = { if (!working) onDismiss() },
        tagId = tagId,
        buttons = {
            TextButton(onClick = onDismiss, enabled = !working, modifier = Modifier.tag("${tagId}_no")) { Text("Volver") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onConfirm, enabled = !working, modifier = Modifier.tag("${tagId}_si")) {
                if (working) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text(confirmLabel)
            }
        },
    ) {
        Text(message)
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Banner(error, error = true, tagId = "${tagId}_error")
        }
    }
}

// ============================================================ Campos

@Composable
fun Campo(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    tagId: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String? = null,
    enabled: Boolean = true,
    supporting: String? = null,
) {
    val placeholderSlot: (@Composable () -> Unit)? =
        if (placeholder != null) { { Text(placeholder) } } else null
    val supportingSlot: (@Composable () -> Unit)? = when {
        error != null -> { { Text(error, modifier = Modifier.tag("${tagId}_error")) } }
        supporting != null -> { { Text(supporting) } }
        else -> null
    }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = placeholderSlot,
        isError = error != null,
        supportingText = supportingSlot,
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.fillMaxWidth().tag(tagId),
    )
}

// ============================================================ Piezas visuales

@Composable
fun NetImage(url: String?, modifier: Modifier = Modifier, contentDescription: String? = null) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        loading = { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp)) } },
        error = {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.outline)
            }
        },
    )
}

@Composable
fun Stars(count: Int?, size: Dp = 16.dp) {
    val n = (count ?: 0).coerceIn(0, 5)
    Row {
        repeat(5) { i ->
            Icon(
                if (i < n) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = Star,
                modifier = Modifier.size(size),
            )
        }
    }
}

/** "4.5 ★ · 12 reseñas" o "Sin reseñas" (datos reales de la API). */
@Composable
fun CalificacionTexto(promedio: Double?, cantidad: Int?, modifier: Modifier = Modifier) {
    val n = cantidad ?: 0
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (n > 0) {
            Icon(Icons.Filled.Star, null, Modifier.size(14.dp), tint = Star)
            Text(
                " %.1f · %d %s".format(Locale.US, promedio ?: 0.0, n, if (n == 1) "reseña" else "reseñas"),
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Text("Sin reseñas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Tarjeta de servicio (hotel o tour) en listados. */
@Composable
fun ServiceCard(
    title: String,
    imageUrl: String?,
    location: String,
    price: String,
    priceSuffix: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    stars: Int? = null,
    promedio: Double? = null,
    cantidadResenas: Int? = null,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box {
            NetImage(imageUrl, Modifier.fillMaxWidth().height(160.dp), title)
            if (badge != null) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(10.dp)) {
                    Text(badge, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
        Column(Modifier.padding(14.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.tag("tarjeta_nombre"),
            )
            if (stars != null && stars > 0) {
                Spacer(Modifier.height(2.dp))
                Stars(stars, 14.dp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(4.dp))
            CalificacionTexto(promedio, cantidadResenas)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.tag("tarjeta_precio"))
                Text(" $priceSuffix", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Banner superior con degradado. */
@Composable
fun HeroHeader(title: String, subtitle: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(com.trouvels360.app.ui.theme.Navy, com.trouvels360.app.ui.theme.MintDark),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column {
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

/** Fila etiqueta / valor para comprobantes y resúmenes. */
@Composable
fun FilaDato(label: String, value: String, modifier: Modifier = Modifier, bold: Boolean = false, valueTag: String? = null) {
    Row(modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = if (valueTag != null) Modifier.tag(valueTag) else Modifier,
        )
    }
}

/** Contador +/- (adultos, niños, habitaciones, personas). */
@Composable
fun Stepper(label: String, value: Int, min: Int, max: Int = 20, tagId: String, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { if (value > min) onChange(value - 1) }, enabled = value > min, modifier = Modifier.tag("${tagId}_menos")) { Text("−") }
        Text("$value", modifier = Modifier.width(40.dp).tag("${tagId}_valor"), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
        OutlinedButton(onClick = { if (value < max) onChange(value + 1) }, enabled = value < max, modifier = Modifier.tag("${tagId}_mas")) { Text("+") }
    }
}
