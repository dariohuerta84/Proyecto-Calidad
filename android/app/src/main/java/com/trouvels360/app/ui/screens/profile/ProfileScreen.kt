package com.trouvels360.app.ui.screens.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trouvels360.app.data.Repository
import com.trouvels360.app.data.Result
import com.trouvels360.app.data.Validaciones
import com.trouvels360.app.data.onOk
import com.trouvels360.app.data.remote.UserDto
import com.trouvels360.app.ui.components.Banner
import com.trouvels360.app.ui.components.Campo
import com.trouvels360.app.ui.components.ConfirmDialog
import com.trouvels360.app.ui.components.NetImage
import com.trouvels360.app.ui.components.SectionTitle
import com.trouvels360.app.ui.components.tag
import com.trouvels360.app.ui.screens.auth.PasswordField
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var nombre by mutableStateOf(""); var apellido by mutableStateOf("")
    var empresa by mutableStateOf(""); var telefono by mutableStateOf(""); var avatar by mutableStateOf("")
    private var original: UserDto? = null
    var errores by mutableStateOf(mapOf<String, String>()); private set
    var guardando by mutableStateOf(false); private set
    var mensaje by mutableStateOf<String?>(null); private set
    var mensajeError by mutableStateOf<String?>(null); private set

    var actual by mutableStateOf(""); var nueva by mutableStateOf(""); var confirmacion by mutableStateOf("")
    var erroresPass by mutableStateOf(mapOf<String, String>()); private set
    var cambiandoPass by mutableStateOf(false); private set
    var mensajePass by mutableStateOf<String?>(null); private set
    var mensajePassError by mutableStateOf<String?>(null); private set

    var confirmarEliminar by mutableStateOf(false)
    var eliminando by mutableStateOf(false); private set
    var errorEliminar by mutableStateOf<String?>(null); private set

    fun iniciar(u: UserDto) {
        if (original?.id == u.id) return
        cargar(u)
        // Trae los datos frescos del servidor (por si cambiaron desde la web)
        viewModelScope.launch { Repository.perfil().onOk { fresco -> if (fresco != null) cargar(fresco) } }
    }

    private fun cargar(u: UserDto) {
        original = u
        nombre = u.nombre.orEmpty(); apellido = u.apellido.orEmpty()
        empresa = u.empresaNombre.orEmpty(); telefono = u.telefono.orEmpty(); avatar = u.avatarUrl.orEmpty()
    }

    /** HU-V03 — Solo se envían campos permitidos para el rol y que cambiaron. */
    fun guardar() {
        val u = original ?: return
        val e = buildMap {
            if (u.esViajero) {
                if (!Validaciones.nombreValido(nombre)) put("nombre", "Ingresa un nombre válido (solo letras).")
                if (!Validaciones.nombreValido(apellido)) put("apellido", "Ingresa un apellido válido (solo letras).")
            } else {
                if (empresa.trim().length < 2) put("empresa_nombre", "Entre 2 y 150 caracteres.")
                if (!Validaciones.telefonoValido(telefono)) put("telefono", "Formato esperado: +51 9XXXXXXXX")
            }
            if (avatar.isNotBlank() && !Validaciones.urlValida(avatar)) put("avatar_url", "Debe ser una URL http(s) válida.")
        }
        errores = e
        mensaje = null; mensajeError = null
        if (e.isNotEmpty()) return

        val cambios = mutableMapOf<String, String>()
        if (u.esViajero) {
            if (nombre.trim() != u.nombre.orEmpty()) cambios["nombre"] = nombre.trim()
            if (apellido.trim() != u.apellido.orEmpty()) cambios["apellido"] = apellido.trim()
        } else {
            if (empresa.trim() != u.empresaNombre.orEmpty()) cambios["empresa_nombre"] = empresa.trim()
            val tel = Validaciones.normalizarTelefono(telefono)
            if (tel != u.telefono.orEmpty()) cambios["telefono"] = tel
        }
        // "" se convierte en null en Laravel (quita la foto)
        if (avatar.trim() != u.avatarUrl.orEmpty()) cambios["avatar_url"] = avatar.trim()
        if (cambios.isEmpty()) { mensaje = "No hay cambios por guardar."; return }

        viewModelScope.launch {
            guardando = true
            when (val r = Repository.actualizarPerfil(cambios)) {
                is Result.Ok -> { r.value?.let { cargar(it) }; mensaje = "Perfil actualizado correctamente." }
                is Result.Err -> { errores = r.fields; mensajeError = r.message }
            }
            guardando = false
        }
    }

    fun cambiarPassword() {
        val e = buildMap {
            if (actual.isEmpty()) put("password_actual", "Ingresa tu contraseña actual.")
            if (nueva.length < 6) put("password", "Mínimo 6 caracteres.")
            if (confirmacion != nueva) put("password_confirmation", "Las contraseñas no coinciden.")
        }
        erroresPass = e; mensajePass = null; mensajePassError = null
        if (e.isNotEmpty()) return
        viewModelScope.launch {
            cambiandoPass = true
            when (val r = Repository.cambiarPassword(actual, nueva, confirmacion)) {
                is Result.Ok -> { mensajePass = r.value; actual = ""; nueva = ""; confirmacion = "" }
                is Result.Err -> { erroresPass = r.fields; mensajePassError = r.message }
            }
            cambiandoPass = false
        }
    }

    /** HU-V04 — 409 si tiene reservas activas; el historial se conserva. */
    fun eliminarCuenta(onEliminada: () -> Unit) {
        viewModelScope.launch {
            eliminando = true; errorEliminar = null
            when (val r = Repository.eliminarCuenta()) {
                is Result.Ok -> { confirmarEliminar = false; onEliminada() }
                is Result.Err -> errorEliminar = r.message
            }
            eliminando = false
        }
    }
}

@Composable
fun ProfileScreen(user: UserDto, onLogout: () -> Unit, onCuentaEliminada: () -> Unit, vm: ProfileViewModel = viewModel()) {
    LaunchedEffect(user.id) { vm.iniciar(user) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).tag("pantalla_perfil"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NetImage(vm.avatar.ifBlank { null }, Modifier.size(64.dp).clip(CircleShape), "Foto de perfil")
            Spacer(Modifier.width(12.dp))
            Column {
                Text(user.nombreVisible, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.tag("perfil_nombre_visible"))
                Text(user.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Rol: ${user.rol?.uppercase()} (no se puede cambiar)", style = MaterialTheme.typography.labelMedium, modifier = Modifier.tag("perfil_rol"))
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("Mis datos")
                if (user.esViajero) {
                    Campo(vm.nombre, { vm.nombre = it }, "Nombre", "perfil_nombre", error = vm.errores["nombre"])
                    Campo(vm.apellido, { vm.apellido = it }, "Apellido", "perfil_apellido", error = vm.errores["apellido"])
                } else {
                    Campo(vm.empresa, { vm.empresa = it }, "Nombre de la empresa", "perfil_empresa", error = vm.errores["empresa_nombre"])
                    Campo(vm.telefono, { vm.telefono = it }, "Celular", "perfil_telefono", error = vm.errores["telefono"], keyboardType = KeyboardType.Phone)
                    Campo(user.ruc.orEmpty(), {}, "RUC", "perfil_ruc", enabled = false, supporting = "No se puede modificar.")
                }
                Campo(vm.avatar, { vm.avatar = it }, "Foto de perfil (URL)", "perfil_avatar", error = vm.errores["avatar_url"], keyboardType = KeyboardType.Uri,
                    supporting = "Déjalo vacío para quitar la foto.")
                vm.mensaje?.let { Banner(it, error = false, tagId = "perfil_mensaje") }
                vm.mensajeError?.let { Banner(it, error = true, tagId = "perfil_mensaje_error") }
                Button(onClick = { vm.guardar() }, enabled = !vm.guardando, modifier = Modifier.fillMaxWidth().tag("perfil_guardar")) {
                    if (vm.guardando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Guardar cambios")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("Cambiar contraseña")
                PasswordField(vm.actual, { vm.actual = it }, "Contraseña actual", "password_actual", vm.erroresPass["password_actual"])
                PasswordField(vm.nueva, { vm.nueva = it }, "Nueva contraseña", "password_nueva", vm.erroresPass["password"])
                PasswordField(vm.confirmacion, { vm.confirmacion = it }, "Repite la nueva contraseña", "password_confirmacion",
                    vm.erroresPass["password_confirmation"] ?: if (vm.confirmacion.isNotEmpty() && vm.confirmacion != vm.nueva) "Las contraseñas no coinciden." else null)
                vm.mensajePass?.let { Banner(it, error = false, tagId = "password_mensaje") }
                vm.mensajePassError?.let { Banner(it, error = true, tagId = "password_mensaje_error") }
                Button(onClick = { vm.cambiarPassword() }, enabled = !vm.cambiandoPass && vm.confirmacion == vm.nueva, modifier = Modifier.fillMaxWidth().tag("password_guardar")) {
                    if (vm.cambiandoPass) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Actualizar contraseña")
                }
            }
        }

        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().tag("btn_logout")) { Text("Cerrar sesión") }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Eliminar cuenta", fontWeight = FontWeight.SemiBold)
        Text("Tu cuenta se desactivará y no podrás volver a iniciar sesión. No es posible si tienes reservas activas.",
            style = MaterialTheme.typography.bodySmall)
        Button(
            onClick = { vm.confirmarEliminar = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().tag("btn_eliminar_cuenta"),
        ) { Text("Eliminar mi cuenta") }
        Spacer(Modifier.height(24.dp))
    }

    if (vm.confirmarEliminar) {
        ConfirmDialog(
            title = "¿Eliminar tu cuenta?",
            message = "Esta acción no se puede deshacer.",
            confirmLabel = "Sí, eliminar",
            onConfirm = { vm.eliminarCuenta(onCuentaEliminada) },
            onDismiss = { vm.confirmarEliminar = false },
            tagId = "dialogo_eliminar_cuenta",
            working = vm.eliminando,
            error = vm.errorEliminar,
        )
    }
}
