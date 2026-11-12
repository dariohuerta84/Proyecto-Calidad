package com.trouvels360.app.ui.screens.auth

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.Validaciones
import com.trouvels360.app.data.remote.RegisterRequest
import com.trouvels360.app.data.remote.UserDto
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.Campo
import com.trouvels360.app.ui.components.tag
import kotlinx.coroutines.launch

@Composable
fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    tagId: String,
    error: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    val supporting: (@Composable () -> Unit)? = if (error != null) { { Text(error, modifier = Modifier.tag("${tagId}_error")) } } else null
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = supporting,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }, modifier = Modifier.tag("${tagId}_ver")) {
                Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Mostrar contraseña")
            }
        },
        modifier = Modifier.fillMaxWidth().tag(tagId),
    )
}

/**
 * HU-V02 — Inicio de sesión. Al terminar vuelve a la pantalla que lo pidió (retorno al destino).
 * Si la sesión había expirado, lo indica.
 */
@Composable
fun LoginScreen(sesionExpirada: Boolean, onDone: (UserDto?) -> Unit, onRegister: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fields by remember { mutableStateOf(mapOf<String, String>()) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).tag("pantalla_login"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("¡Hola de nuevo!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Inicia sesión para reservar o gestionar tus publicaciones.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (sesionExpirada) Banner("Tu sesión expiró. Vuelve a iniciar sesión para continuar.", error = true, tagId = "login_sesion_expirada")
        Spacer(Modifier.height(4.dp))
        Campo(email, { email = it }, "Correo electrónico", "login_email", error = fields["email"], keyboardType = KeyboardType.Email)
        PasswordField(password, { password = it }, "Contraseña", "login_password", error = fields["password"])
        error?.let { Banner(it, error = true, tagId = "login_error") }
        Button(
            onClick = {
                val f = buildMap {
                    if (!Validaciones.emailValido(email)) put("email", "Ingresa un correo válido.")
                    if (password.isEmpty()) put("password", "Ingresa tu contraseña.")
                }
                fields = f
                if (f.isNotEmpty()) return@Button
                scope.launch {
                    loading = true; error = null
                    when (val r = Repository.login(email, password)) {
                        is Result.Ok -> onDone(r.value)
                        is Result.Err -> { error = r.message; fields = r.fields }
                    }
                    loading = false
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(50.dp).tag("login_submit"),
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Iniciar sesión")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?")
            TextButton(onClick = onRegister, modifier = Modifier.tag("link_registro")) { Text("Regístrate") }
        }
    }
}

/** HU-V01 (viajero) y HU-P01 (proveedor) — Registro con las mismas reglas que la API. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onDone: (UserDto?) -> Unit, onLogin: () -> Unit) {
    var rol by rememberSaveable { mutableStateOf("viajero") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var empresa by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var ruc by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fields by remember { mutableStateOf(mapOf<String, String>()) }
    val scope = rememberCoroutineScope()
    val esViajero = rol == "viajero"

    fun validar(): Map<String, String> = buildMap {
        if (esViajero) {
            if (!Validaciones.nombreValido(nombre)) put("nombre", "Ingresa un nombre válido (solo letras).")
            if (!Validaciones.nombreValido(apellido)) put("apellido", "Ingresa un apellido válido (solo letras).")
        } else {
            if (empresa.trim().length < 2) put("empresa_nombre", "Ingresa el nombre de la empresa.")
            if (!Validaciones.rucValido(ruc)) put("ruc", "El RUC debe tener exactamente 11 dígitos.")
            if (!Validaciones.telefonoValido(telefono)) put("telefono", "Celular peruano: 9 dígitos que empiezan con 9.")
        }
        if (!Validaciones.emailValido(email)) put("email", "Ingresa un correo válido.")
        if (password.length < 6) put("password", "Mínimo 6 caracteres.")
        if (confirmacion != password) put("password_confirmation", "Las contraseñas no coinciden.")
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).tag("pantalla_registro"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Crea tu cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = esViajero,
                onClick = { rol = "viajero"; fields = emptyMap() },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                modifier = Modifier.tag("registro_rol_viajero"),
            ) { Text("Viajero") }
            SegmentedButton(
                selected = !esViajero,
                onClick = { rol = "proveedor"; fields = emptyMap() },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                modifier = Modifier.tag("registro_rol_proveedor"),
            ) { Text("Proveedor") }
        }
        if (esViajero) {
            Campo(nombre, { nombre = it }, "Nombre", "registro_nombre", error = fields["nombre"])
            Campo(apellido, { apellido = it }, "Apellido", "registro_apellido", error = fields["apellido"])
        } else {
            Campo(empresa, { empresa = it }, "Nombre de la empresa", "registro_empresa", error = fields["empresa_nombre"])
            Campo(ruc, { ruc = it.filter(Char::isDigit).take(11) }, "RUC (11 dígitos)", "registro_ruc", error = fields["ruc"], keyboardType = KeyboardType.Number)
            Campo(telefono, { telefono = it }, "Celular", "registro_telefono", error = fields["telefono"], keyboardType = KeyboardType.Phone, placeholder = "987654321")
        }
        Campo(email, { email = it }, "Correo electrónico", "registro_email", error = fields["email"], keyboardType = KeyboardType.Email)
        PasswordField(password, { password = it }, "Contraseña (mín. 6 caracteres)", "registro_password", fields["password"])
        PasswordField(confirmacion, { confirmacion = it }, "Repite la contraseña", "registro_password_confirmacion",
            fields["password_confirmation"] ?: if (confirmacion.isNotEmpty() && confirmacion != password) "Las contraseñas no coinciden." else null)
        error?.let { Banner(it, error = true, tagId = "registro_error") }
        Button(
            onClick = {
                val f = validar()
                fields = f
                if (f.isNotEmpty()) return@Button
                scope.launch {
                    loading = true; error = null
                    val req = if (esViajero) {
                        RegisterRequest(rol = rol, email = email.trim(), password = password, passwordConfirmation = confirmacion,
                            nombre = nombre.trim(), apellido = apellido.trim())
                    } else {
                        RegisterRequest(rol = rol, email = email.trim(), password = password, passwordConfirmation = confirmacion,
                            empresaNombre = empresa.trim(), ruc = ruc, telefono = Validaciones.normalizarTelefono(telefono))
                    }
                    when (val r = Repository.register(req)) {
                        is Result.Ok -> onDone(r.value)
                        is Result.Err -> { error = r.message; fields = r.fields }
                    }
                    loading = false
                }
            },
            enabled = !loading && confirmacion == password,
            modifier = Modifier.fillMaxWidth().height(50.dp).tag("registro_submit"),
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Registrarme")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?")
            TextButton(onClick = onLogin, modifier = Modifier.tag("link_login")) { Text("Inicia sesión") }
        }
    }
}
