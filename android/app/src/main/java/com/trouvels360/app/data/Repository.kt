package com.trouvels360.app.data

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.trouvels360.app.data.remote.ActividadUpsert
import com.trouvels360.app.data.remote.ApiClient
import com.trouvels360.app.data.remote.CotizarHotelRequest
import com.trouvels360.app.data.remote.CotizarTourRequest
import com.trouvels360.app.data.remote.CrearReservaHotelRequest
import com.trouvels360.app.data.remote.CrearReservaTourRequest
import com.trouvels360.app.data.remote.HabitacionUpsert
import com.trouvels360.app.data.remote.HotelUpsert
import com.trouvels360.app.data.remote.ItemHabitacion
import com.trouvels360.app.data.remote.ItinerarioRequest
import com.trouvels360.app.data.remote.LoginRequest
import com.trouvels360.app.data.remote.PagoDto
import com.trouvels360.app.data.remote.PasswordRequest
import com.trouvels360.app.data.remote.RechazoRequest
import com.trouvels360.app.data.remote.RegisterRequest
import com.trouvels360.app.data.remote.ReviewRequest
import com.trouvels360.app.data.remote.SalidaUpsert
import com.trouvels360.app.data.remote.TourUpsert
import com.trouvels360.app.data.remote.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.UUID

/** Resultado para la UI: valor o mensaje de error (con errores por campo si la API los envía). */
sealed interface Result<out T> {
    data class Ok<T>(val value: T) : Result<T>
    data class Err(
        val message: String,
        val code: Int? = null,
        val fields: Map<String, String> = emptyMap(),
    ) : Result<Nothing>
}

inline fun <T> Result<T>.onOk(block: (T) -> Unit): Result<T> { if (this is Result.Ok) block(value); return this }
inline fun <T> Result<T>.onErr(block: (Result.Err) -> Unit): Result<T> { if (this is Result.Err) block(this); return this }

/** Clave para el header Idempotency-Key: una por intento de pago (se conserva si falla la red). */
fun nuevaClaveIdempotencia(): String = UUID.randomUUID().toString()

/**
 * Usuario actual (equivalente al 'current_user' de la web). El JWT vive en la cookie HttpOnly.
 */
object Session {
    private lateinit var ctx: Context
    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    /** Se activa cuando la sesión expira (401 sin posibilidad de refrescar): la UI muestra el login. */
    private val _expirada = MutableStateFlow(false)
    val expirada: StateFlow<Boolean> = _expirada.asStateFlow()

    fun init(context: Context) {
        ctx = context.applicationContext
        val raw = prefs().getString("user", null)
        _user.value = raw?.let { runCatching { ApiClient.gson.fromJson(it, UserDto::class.java) }.getOrNull() }
    }

    private fun prefs() = ctx.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun set(user: UserDto?) {
        _user.value = user
        if (user != null) _expirada.value = false
        prefs().edit().apply {
            if (user == null) remove("user") else putString("user", ApiClient.gson.toJson(user))
        }.apply()
    }

    fun clear(porExpiracion: Boolean = false) {
        set(null)
        ApiClient.cookieJar.clear()
        _expirada.value = porExpiracion
    }

    fun avisoExpiracionVisto() { _expirada.value = false }
}

object Repository {
    private val api get() = ApiClient.api

    // ============================================================ Infraestructura

    /**
     * Ejecuta la llamada y traduce errores HTTP de Laravel a mensajes legibles.
     * Ante un 401 con sesión iniciada intenta refrescar el JWT una vez y reintenta.
     */
    private suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.Ok(block())
    } catch (e: HttpException) {
        if (e.code() == 401 && Session.user.value != null && intentarRefrescar()) {
            try { Result.Ok(block()) } catch (e2: HttpException) { error(e2) } catch (e2: IOException) { sinRed() }
        } else error(e)
    } catch (e: IOException) {
        sinRed()
    } catch (e: Exception) {
        Result.Err(e.message ?: "Ocurrió un error inesperado.")
    }

    /** Antes de cada petición que cambia estado se obtiene la cookie XSRF-TOKEN (doble envío). */
    private suspend fun <T> mutate(block: suspend () -> T): Result<T> = call {
        api.csrf()
        block()
    }

    /** Para endpoints que devuelven Response<Unit> (DELETE, posibles 204). */
    private fun <T> Response<T>.orThrow() { if (!isSuccessful) throw HttpException(this) }

    private suspend fun intentarRefrescar(): Boolean = try {
        api.csrf(); api.refresh(); true
    } catch (e: Exception) {
        Session.clear(porExpiracion = true); false
    }

    private fun sinRed() = Result.Err("No se puede conectar con el servidor. Revisa tu conexión.")

    private fun error(e: HttpException): Result.Err {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val json = runCatching { JsonParser.parseString(body).asJsonObject }.getOrNull()
        val fields = mutableMapOf<String, String>()
        json?.getAsJsonObject("errors")?.entrySet()?.forEach { (k, v) ->
            val msg = runCatching { v.asJsonArray.firstOrNull()?.asString }.getOrNull()
            if (!msg.isNullOrBlank()) fields[k] = msg
        }
        val message = json?.get("message")?.takeIf { it.isJsonPrimitive }?.asString
            ?.replace(Regex("""\s*\(and \d+ more errors?\)"""), "")
        val texto = when {
            fields.isNotEmpty() -> fields.values.first()
            !message.isNullOrBlank() -> message
            e.code() == 401 -> "Tu sesión expiró. Inicia sesión nuevamente."
            e.code() == 403 -> "No tienes permiso para realizar esta acción."
            e.code() == 404 -> "No encontrado."
            e.code() == 429 -> "Demasiados intentos. Espera un momento."
            else -> "Error del servidor (${e.code()})."
        }
        return Result.Err(texto, e.code(), fields)
    }

    private fun mensaje(json: JsonObject?, porDefecto: String): String =
        json?.get("message")?.takeIf { it.isJsonPrimitive }?.asString ?: porDefecto

    // ============================================================ Auth (HU-V01..103)

    suspend fun login(email: String, password: String) = mutate {
        val user = api.login(LoginRequest(email.trim().lowercase(), password)).data?.user
        Session.set(user)
        user
    }

    suspend fun register(req: RegisterRequest) = mutate {
        val user = api.register(req).data?.user
        Session.set(user)
        user
    }

    /** Valida la cookie guardada al abrir la app. */
    suspend fun restoreSession() {
        if (Session.user.value == null) return
        when (val r = call { api.me() }) {
            is Result.Ok -> r.value.data?.let { Session.set(it) }
            is Result.Err -> if (r.code == 401) Session.clear()
        }
    }

    suspend fun logout() {
        mutate { api.logout() }
        Session.clear()
    }

    // ============================================================ Mi cuenta (HU-V03, HU-V04)

    suspend fun perfil() = call { api.perfil().data }

    /** Solo envía los campos permitidos para el rol (nunca rol ni contraseña). */
    suspend fun actualizarPerfil(cambios: Map<String, String>) = mutate {
        val u = api.actualizarPerfil(cambios).data
        if (u != null) Session.set(u)
        u
    }

    suspend fun cambiarPassword(actual: String, nueva: String, confirmacion: String) =
        mutate { api.cambiarPassword(PasswordRequest(actual, nueva, confirmacion)).message ?: "Contraseña actualizada." }

    suspend fun eliminarCuenta() = mutate {
        api.eliminarCuenta().orThrow()
        Session.clear()
    }

    // ============================================================ Hoteles (HU-V05..203)

    suspend fun buscarHoteles(ciudad: String?, checkIn: String?, checkOut: String?, adultos: Int, ninos: Int, habitaciones: Int, page: Int) = call {
        val conFechas = !checkIn.isNullOrBlank() && !checkOut.isNullOrBlank()
        api.hoteles(
            ciudad = ciudad?.trim()?.ifBlank { null },
            checkIn = checkIn.takeIf { conFechas },
            checkOut = checkOut.takeIf { conFechas },
            adultos = adultos.takeIf { conFechas },
            ninos = ninos.takeIf { conFechas && it > 0 },
            habitaciones = habitaciones.takeIf { conFechas },
            page = page,
        )
    }

    suspend fun hotel(id: Int) = call { api.hotel(id) }

    suspend fun disponibilidad(id: Int, checkIn: String, checkOut: String, adultos: Int, ninos: Int, habitaciones: Int) = call {
        api.disponibilidad(id, checkIn, checkOut, adultos, ninos.takeIf { it > 0 }, habitaciones).data?.habitaciones.orEmpty()
    }

    suspend fun cotizarHotel(checkIn: String, checkOut: String, items: List<ItemHabitacion>) =
        mutate { requireNotNull(api.cotizarHotel(CotizarHotelRequest(checkIn, checkOut, items)).data) }

    suspend fun reservarHotel(checkIn: String, checkOut: String, items: List<ItemHabitacion>, pago: PagoDto, clave: String) =
        mutate { requireNotNull(api.reservarHotel(CrearReservaHotelRequest(checkIn, checkOut, items, pago), clave).data) }

    // ============================================================ Tours (HU-V08..303)

    suspend fun buscarTours(destino: String?, categoria: String?, desde: String?, hasta: String?, personas: Int, page: Int) = call {
        api.tours(destino?.trim()?.ifBlank { null }, categoria, desde, hasta, personas.takeIf { it > 1 }, page)
    }

    suspend fun tour(id: Int) = call { requireNotNull(api.tour(id).servicio) }

    suspend fun cotizarTour(salidaId: Int, personas: Int) =
        mutate { requireNotNull(api.cotizarTour(salidaId, CotizarTourRequest(personas)).data) }

    suspend fun reservarTour(salidaId: Int, personas: Int, pago: PagoDto, clave: String) =
        mutate { requireNotNull(api.reservarTour(salidaId, CrearReservaTourRequest(personas, pago), clave).data) }

    // ============================================================ Mis reservas (HU-V11, HU-V13)

    suspend fun misReservasHotel(estado: String?, page: Int) = call { api.misReservasHotel(estado, page) }
    suspend fun misReservasTour(estado: String?, page: Int) = call { api.misReservasTour(estado, page) }
    suspend fun cancelarReservaHotel(id: Int) = mutate { api.cancelarReservaHotel(id) }
    suspend fun cancelarReservaTour(id: Int) = mutate { api.cancelarReservaTour(id) }

    // ============================================================ Reseñas (HU-V14)

    suspend fun reviews(servicioId: Int, page: Int) = call { api.reviews(servicioId, page) }
    suspend fun resumenReviews(servicioId: Int) = call { api.resumenReviews(servicioId).data }
    suspend fun puedoOpinar(servicioId: Int) = call { api.puedoOpinar(servicioId).data }
    suspend fun crearReview(servicioId: Int, calificacion: Int, comentario: String) =
        mutate { api.crearReview(ReviewRequest(servicioId, calificacion, comentario.trim())) }
    suspend fun editarReview(id: Int, calificacion: Int, comentario: String) =
        mutate { api.editarReview(id, ReviewRequest(null, calificacion, comentario.trim())) }
    suspend fun eliminarReview(id: Int) = mutate { api.eliminarReview(id).orThrow() }

    // ============================================================ Itinerario (HU-V15)

    suspend fun itinerario(destino: String, hotelId: Int, checkIn: String, checkOut: String, interes: String) =
        mutate { api.itinerario(ItinerarioRequest(destino, hotelId, checkIn, checkOut, interes)) }

    // ============================================================ Proveedor (HU-P13/HU-P14, HU-P03/HU-P04..508)

    suspend fun indicadores() = call { api.indicadores().data }
    suspend fun misServicios(page: Int, tipo: String?, busqueda: String?) =
        call { api.misServicios(page, tipo, busqueda?.trim()?.ifBlank { null }) }
    suspend fun eliminarServicio(id: Int) = mutate { api.eliminarServicio(id).orThrow() }

    suspend fun reservasRecibidas(tipo: String?, estado: String?, servicioId: Int?, page: Int) =
        call { api.reservasRecibidas(tipo, estado, servicioId, page) }
    suspend fun aceptarReserva(id: Int) = mutate { api.aceptarReserva(id) }
    suspend fun rechazarReserva(id: Int, motivo: String) = mutate { api.rechazarReserva(id, RechazoRequest(motivo.trim())) }

    suspend fun crearHotel(body: HotelUpsert) = mutate { mensaje(api.crearHotel(body), "Hotel publicado.") }
    suspend fun editarHotel(id: Int, body: HotelUpsert) = mutate { mensaje(api.editarHotel(id, body), "Hotel actualizado.") }
    suspend fun crearHabitacion(hotelId: Int, body: HabitacionUpsert) = mutate { api.crearHabitacion(hotelId, body); Unit }
    suspend fun editarHabitacion(id: Int, body: HabitacionUpsert) = mutate { api.editarHabitacion(id, body); Unit }
    suspend fun eliminarHabitacion(id: Int) = mutate { api.eliminarHabitacion(id).orThrow() }

    suspend fun crearTour(body: TourUpsert) = mutate { mensaje(api.crearTour(body), "Tour publicado.") }
    suspend fun editarTour(id: Int, body: TourUpsert) = mutate { mensaje(api.editarTour(id, body), "Tour actualizado.") }

    suspend fun salidas(tourId: Int) = call { api.salidas(tourId) }
    suspend fun crearSalida(tourId: Int, body: SalidaUpsert) = mutate { api.crearSalida(tourId, body); Unit }
    suspend fun editarSalida(tourId: Int, salidaId: Int, cambios: Map<String, Any>) = mutate { api.editarSalida(tourId, salidaId, cambios); Unit }
    suspend fun eliminarSalida(tourId: Int, salidaId: Int) = mutate { api.eliminarSalida(tourId, salidaId).orThrow() }

    suspend fun actividades(tourId: Int) = call { api.actividades(tourId) }
    suspend fun crearActividad(tourId: Int, body: ActividadUpsert) = mutate { api.crearActividad(tourId, body); Unit }
    suspend fun editarActividad(tourId: Int, id: Int, body: ActividadUpsert) = mutate { api.editarActividad(tourId, id, body); Unit }
    suspend fun moverActividad(tourId: Int, id: Int, orden: Int) = mutate { api.moverActividad(tourId, id, mapOf("orden" to orden)); Unit }
    suspend fun eliminarActividad(tourId: Int, id: Int) = mutate { api.eliminarActividad(tourId, id).orThrow() }
}
