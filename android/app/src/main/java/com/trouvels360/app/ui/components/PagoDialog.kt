package com.trouvels360.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.trouvels360.app.data.remote.PagoDto
import com.trouvels360.app.data.ValidacionPago

/**
 * Formulario de pago simulado (HU-V07, HU-V10). No guarda la tarjeta: la API solo conserva los
 * últimos 4 dígitos. Cerrar el diálogo antes de confirmar no crea ninguna reserva.
 */
@Composable
fun PagoDialog(
    total: Double?,
    procesando: Boolean,
    errorServidor: String?,
    onConfirmar: (PagoDto) -> Unit,
    onCancelar: () -> Unit,
) {
    var titular by rememberSaveable { mutableStateOf("") }
    // Número y CVC no se guardan en el estado restaurable (Bundle).
    var numero by remember { mutableStateOf("") }
    var venc by rememberSaveable { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf(mapOf<String, String>()) }

    AppDialog(
        title = "Pago simulado",
        onDismiss = { if (!procesando) onCancelar() },
        tagId = "dialogo_pago",
        buttons = {
            TextButton(onClick = onCancelar, enabled = !procesando, modifier = Modifier.tag("pago_cancelar")) { Text("Cancelar") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    errores = ValidacionPago.errores(titular, numero, venc, cvc)
                    if (errores.isEmpty()) {
                        onConfirmar(PagoDto(titular.trim(), ValidacionPago.soloDigitos(numero), venc.trim(), cvc.trim()))
                    }
                },
                enabled = !procesando,
                modifier = Modifier.tag("pago_confirmar"),
            ) {
                if (procesando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Pagar ${formatPrice(total)}")
            }
        },
    ) {
        Text(
            "Prueba: 4242 4242 4242 4242, vencimiento futuro (12/30) y CVC 123.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Campo(titular, { titular = it }, "Titular", "pago_titular", error = errores["titular"])
        Campo(numero, { numero = it.filter { c -> c.isDigit() || c == ' ' }.take(23) }, "Número de tarjeta", "pago_numero",
            error = errores["numero"], keyboardType = KeyboardType.Number)
        Campo(venc, { venc = it.take(5) }, "Vencimiento (MM/AA)", "pago_vencimiento", error = errores["vencimiento"], placeholder = "12/30")
        Campo(cvc, { cvc = it.filter { c -> c.isDigit() }.take(4) }, "CVC", "pago_cvc", error = errores["cvc"],
            keyboardType = KeyboardType.NumberPassword, password = true)
        if (errorServidor != null) {
            Spacer(Modifier.height(8.dp))
            Banner(errorServidor, error = true, tagId = "pago_error_servidor")
        }
    }
}
