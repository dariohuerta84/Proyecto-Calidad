package com.trouvels360.app.data.remote

import com.google.gson.annotations.SerializedName

/*
 * Modelos de la API Laravel (etapa 2/3). Todos los campos son anulables para que un cambio menor
 * en la respuesta no haga caer la app. Los nombres siguen el JSON real (snake_case → @SerializedName).
 * Verificados contra respuestas reales del backend (ver docs/CONTRATO_API.md).
 */

// ------------------------------------------------------------------ Genéricos

data class DataWrapper<T>(val data: T? = null, val message: String? = null)

data class MessageResponse(val message: String? = null)

data class PageMeta(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    val total: Int? = null,
)

/** Respuesta paginada {data, meta}. */
data class Page<T>(val data: List<T>? = null, val meta: PageMeta? = null) {
    val items: List<T> get() = data.orEmpty()
    val page: Int get() = meta?.currentPage ?: 1
    val lastPage: Int get() = meta?.lastPage ?: 1
    val total: Int get() = meta?.total ?: items.size
}

data class Calificacion(val promedio: Double? = null, val cantidad: Int? = null)

// ------------------------------------------------------------------ Usuario / Auth

data class UserDto(
    val id: Int? = null,
    val rol: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    @SerializedName("empresa_nombre") val empresaNombre: String? = null,
    val telefono: String? = null,
    val ruc: String? = null,
    val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
) {
    val esProveedor: Boolean get() = rol == "proveedor"
    val esViajero: Boolean get() = rol == "viajero"
    val nombreVisible: String
        get() = if (esProveedor) empresaNombre ?: email.orEmpty()
        else listOfNotNull(nombre, apellido).joinToString(" ").ifBlank { email.orEmpty() }
}

data class AuthData(val user: UserDto? = null)
data class AuthResponse(val message: String? = null, val data: AuthData? = null)

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val rol: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val nombre: String? = null,
    val apellido: String? = null,
    @SerializedName("empresa_nombre") val empresaNombre: String? = null,
    val ruc: String? = null,
    val telefono: String? = null,
)

data class PasswordRequest(
    @SerializedName("password_actual") val passwordActual: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
)

// ------------------------------------------------------------------ Hoteles

data class ImagenDto(val id: Int? = null, val url: String? = null, val alt: String? = null)

data class HabitacionDto(
    val id: Int? = null,
    val nombre: String? = null,
    @SerializedName("capacidad_adultos") val capacidadAdultos: Int? = null,
    @SerializedName("capacidad_ninos") val capacidadNinos: Int? = null,
    val cantidad: Int? = null,
    @SerializedName("precio_por_noche") val precioPorNoche: Double? = null,
    @SerializedName("unidades_totales") val unidadesTotales: Int? = null,
    @SerializedName("unidades_disponibles") val unidadesDisponibles: Int? = null,
    val descripcion: String? = null,
) {
    /** Unidades que se pueden seleccionar (libres en las fechas, o el total si no hay fechas). */
    val maxSeleccionable: Int get() = unidadesDisponibles ?: unidadesTotales ?: cantidad ?: 0
}

/** Ítem del listado GET /hoteles. */
data class HotelItemDto(
    val id: Int? = null,
    val nombre: String? = null,
    val ciudad: String? = null,
    val pais: String? = null,
    val direccion: String? = null,
    val estrellas: Int? = null,
    @SerializedName("precio_por_noche") val precioPorNoche: Double? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    val descripcion: String? = null,
    val calificacion: Calificacion? = null,
    val habitaciones: List<HabitacionDto>? = null,
)

/** GET /hoteles/{id} → {hotel, habitaciones}. */
data class HotelInfoDto(
    @SerializedName("servicio_id") val servicioId: Int? = null,
    @SerializedName("proveedor_id") val proveedorId: Int? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val ciudad: String? = null,
    val pais: String? = null,
    val direccion: String? = null,
    val estrellas: Int? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    val imagenes: List<ImagenDto>? = null,
    val calificacion: Calificacion? = null,
)

data class HotelDetailResponse(val hotel: HotelInfoDto? = null, val habitaciones: List<HabitacionDto>? = null)

data class DisponibilidadData(val habitaciones: List<HabitacionDto>? = null)

// ------------------------------------------------------------------ Tours

data class TourItemNecesario(val nombre: String? = null, val icono: String? = null)

data class TourInfoDto(
    val categoria: String? = null,
    val duracion: Int? = null, // minutos
    val precio: Double? = null,
    val items: List<TourItemNecesario>? = null,
)

data class SalidaDto(
    val id: Int? = null,
    val fecha: String? = null,
    val hora: String? = null,
    @SerializedName("cupo_total") val cupoTotal: Int? = null,
    @SerializedName("cupo_reservado") val cupoReservado: Int? = null,
    @SerializedName("cupos_disponibles") val cuposDisponibles: Int? = null,
    @SerializedName("cupo_disponible") val cupoDisponible: Int? = null,
    val estado: String? = null,
    @SerializedName("reservas_confirmadas") val reservasConfirmadas: Int? = null,
) {
    val libres: Int get() = cuposDisponibles ?: cupoDisponible ?: ((cupoTotal ?: 0) - (cupoReservado ?: 0)).coerceAtLeast(0)
}

data class ActividadDto(
    val id: Int? = null,
    val titulo: String? = null,
    val descripcion: String? = null,
    val orden: Int? = null,
    @SerializedName("duracion_min") val duracionMin: Int? = null,
)

/** Ítem del listado GET /tours. */
data class TourItemDto(
    val id: Int? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val ciudad: String? = null,
    val pais: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    val tour: TourInfoDto? = null,
    val calificacion: Calificacion? = null,
)

data class TourServicioDto(
    val id: Int? = null,
    @SerializedName("proveedor_id") val proveedorId: Int? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val ciudad: String? = null,
    val pais: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    val tour: TourInfoDto? = null,
    val imagenes: List<ImagenDto>? = null,
    val actividades: List<ActividadDto>? = null,
    val salidas: List<SalidaDto>? = null,
)

/** GET /tours/{id} → {servicio}. */
data class TourDetailResponse(val servicio: TourServicioDto? = null)

// ------------------------------------------------------------------ Cotización y pago

data class PagoDto(val titular: String, val numero: String, val vencimiento: String, val cvc: String)

data class ItemHabitacion(@SerializedName("habitacion_id") val habitacionId: Int, val cantidad: Int)

data class CotizarHotelRequest(
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    val items: List<ItemHabitacion>,
)

data class CrearReservaHotelRequest(
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    val items: List<ItemHabitacion>,
    val pago: PagoDto,
)

data class IdNombre(val id: Int? = null, val nombre: String? = null)

data class CotizacionItemDto(
    @SerializedName("habitacion_id") val habitacionId: Int? = null,
    val nombre: String? = null,
    val cantidad: Int? = null,
    @SerializedName("precio_por_noche") val precioPorNoche: Double? = null,
    val subtotal: Double? = null,
)

data class CotizacionHotelDto(
    val hotel: IdNombre? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String? = null,
    @SerializedName("fecha_fin") val fechaFin: String? = null,
    val noches: Int? = null,
    val items: List<CotizacionItemDto>? = null,
    val subtotal: Double? = null,
    val impuesto: Double? = null,
    @SerializedName("igv_porcentaje") val igvPorcentaje: Int? = null,
    val total: Double? = null,
)

data class CotizarTourRequest(val personas: Int)
data class CrearReservaTourRequest(val personas: Int, val pago: PagoDto)

data class SalidaMini(
    val id: Int? = null,
    val fecha: String? = null,
    val hora: String? = null,
    val estado: String? = null,
    @SerializedName("cupos_disponibles") val cuposDisponibles: Int? = null,
)

data class CotizacionTourDto(
    val tour: IdNombre? = null,
    val salida: SalidaMini? = null,
    val personas: Int? = null,
    @SerializedName("precio_unitario") val precioUnitario: Double? = null,
    @SerializedName("igv_porcentaje") val igvPorcentaje: Int? = null,
    val subtotal: Double? = null,
    val impuesto: Double? = null,
    val total: Double? = null,
)

// ------------------------------------------------------------------ Reservas (hotel y tour)

data class ServicioMini(
    val id: Int? = null,
    val nombre: String? = null,
    val ciudad: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    val eliminado: Boolean? = null,
)

data class ClienteMini(val id: Int? = null, val nombre: String? = null, val email: String? = null)

/** Reserva de hotel o de tour tal como la devuelve ReservaPresenter. */
data class ReservaDto(
    val id: Int? = null,
    val tipo: String? = null,
    val codigo: String? = null,
    @SerializedName("solicitud_codigo") val solicitudCodigo: String? = null,
    val estado: String? = null,
    @SerializedName("motivo_rechazo") val motivoRechazo: String? = null,
    // hotel
    @SerializedName("fecha_inicio") val fechaInicio: String? = null,
    @SerializedName("fecha_fin") val fechaFin: String? = null,
    val noches: Int? = null,
    val cantidad: Int? = null,
    @SerializedName("precio_noche") val precioNoche: Double? = null,
    val habitacion: IdNombre? = null,
    // tour
    val personas: Int? = null,
    @SerializedName("precio_unitario") val precioUnitario: Double? = null,
    val salida: SalidaMini? = null,
    val aviso: String? = null,
    // comunes
    val subtotal: Double? = null,
    val impuesto: Double? = null,
    val total: Double? = null,
    @SerializedName("tarjeta_ultimos4") val tarjetaUltimos4: String? = null,
    @SerializedName("decidido_en") val decididoEn: String? = null,
    @SerializedName("cancelado_en") val canceladoEn: String? = null,
    @SerializedName("creado_en") val creadoEn: String? = null,
    val cancelable: Boolean? = null,
    val servicio: ServicioMini? = null,
    val cliente: ClienteMini? = null,
) {
    val esHotel: Boolean get() = tipo == "hotel"
}

/** 201 de POST /reservas-habitaciones. */
data class SolicitudHotelDto(
    @SerializedName("solicitud_codigo") val solicitudCodigo: String? = null,
    val estado: String? = null,
    val hotel: IdNombre? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String? = null,
    @SerializedName("fecha_fin") val fechaFin: String? = null,
    val noches: Int? = null,
    val subtotal: Double? = null,
    val impuesto: Double? = null,
    val total: Double? = null,
    @SerializedName("tarjeta_ultimos4") val tarjetaUltimos4: String? = null,
    val reservas: List<ReservaDto>? = null,
)

// ------------------------------------------------------------------ Reseñas

data class ReviewUsuario(
    val id: Int? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    @SerializedName("nombre_completo") val nombreCompleto: String? = null,
)

data class ReviewDto(
    val id: Int? = null,
    @SerializedName("servicio_id") val servicioId: Int? = null,
    val usuario: ReviewUsuario? = null,
    val comentario: String? = null,
    val calificacion: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("fecha_formateada") val fechaFormateada: String? = null,
)

data class ResumenResenas(val promedio: Double? = null, val total: Int? = null, val distribucion: Map<String, Int>? = null)

data class MiResena(val id: Int? = null, val calificacion: Int? = null, val comentario: String? = null)

data class PuedoOpinar(val puede: Boolean? = null, val motivo: String? = null, @SerializedName("mi_resena") val miResena: MiResena? = null)

data class ReviewRequest(
    @SerializedName("servicio_id") val servicioId: Int? = null,
    val calificacion: Int,
    val comentario: String,
)

// ------------------------------------------------------------------ Itinerario (microservicio Python vía Laravel)

data class ItinerarioRequest(
    val destino: String,
    @SerializedName("hotel_id") val hotelId: Int,
    @SerializedName("fecha_checkin") val fechaCheckin: String,
    @SerializedName("fecha_checkout") val fechaCheckout: String,
    val interes: String,
)

data class SalidaSugerida(@SerializedName("salida_id") val salidaId: Int? = null, val hora: String? = null, @SerializedName("cupos_disponibles") val cupos: Int? = null)

data class ActividadSugerida(
    @SerializedName("tour_id") val tourId: Int? = null,
    val nombre: String? = null,
    @SerializedName("duracion_horas") val duracionHoras: Double? = null,
    val precio: Double? = null,
    @SerializedName("distancia_km") val distanciaKm: Double? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    val salida: SalidaSugerida? = null,
)

data class DiaItinerario(
    val dia: Int? = null,
    @SerializedName("fecha_sugerida") val fechaSugerida: String? = null,
    val actividades: List<ActividadSugerida>? = null,
)

data class ResumenItinerario(
    @SerializedName("total_dias") val totalDias: Int? = null,
    @SerializedName("total_tours") val totalTours: Int? = null,
    @SerializedName("costo_total") val costoTotal: Double? = null,
)

data class ItinerarioData(val itinerario: List<DiaItinerario>? = null, val resumen: ResumenItinerario? = null)
data class ItinerarioResponse(val success: Boolean? = null, val data: ItinerarioData? = null, val mensaje: String? = null)

// ------------------------------------------------------------------ Proveedor

data class MetaTipo(
    // hotel
    val direccion: String? = null,
    val estrellas: Int? = null,
    @SerializedName("habitaciones_count") val habitacionesCount: Int? = null,
    @SerializedName("tarifa_min_desde") val tarifaMinDesde: Double? = null,
    // tour
    val categoria: String? = null,
    val duracion: Int? = null,
    val precio: Double? = null,
    @SerializedName("salidas_count") val salidasCount: Int? = null,
    @SerializedName("proximas_salidas") val proximasSalidas: Int? = null,
)

data class ReservasTotales(
    val pendientes: Int? = null,
    val confirmadas: Int? = null,
    val rechazadas: Int? = null,
    val canceladas: Int? = null,
)

data class ServicioProveedorDto(
    val id: Int? = null,
    val tipo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val ciudad: String? = null,
    val pais: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    val activo: Boolean? = null,
    @SerializedName("meta_tipo") val metaTipo: MetaTipo? = null,
    @SerializedName("reservas_totales") val reservasTotales: ReservasTotales? = null,
    val resenas: Calificacion? = null,
) {
    val esHotel: Boolean get() = tipo == "hotel"
}

data class IndicadoresDto(
    val publicaciones: Int? = null,
    val hoteles: Int? = null,
    val tours: Int? = null,
    @SerializedName("solicitudes_pendientes") val solicitudesPendientes: Int? = null,
    @SerializedName("reservas_hotel") val reservasHotel: Map<String, Int>? = null,
    @SerializedName("reservas_tour") val reservasTour: Map<String, Int>? = null,
    @SerializedName("ingresos_confirmados_mes") val ingresosMes: Double? = null,
    @SerializedName("ingresos_confirmados_total") val ingresosTotal: Double? = null,
    val resenas: Calificacion? = null,
)

data class RechazoRequest(val motivo: String)

data class HabitacionUpsert(
    val nombre: String,
    @SerializedName("capacidad_adultos") val capacidadAdultos: Int,
    @SerializedName("capacidad_ninos") val capacidadNinos: Int,
    val cantidad: Int,
    @SerializedName("precio_por_noche") val precioPorNoche: Double,
    val descripcion: String? = null,
)

data class HotelUpsert(
    val nombre: String,
    val descripcion: String?,
    val ciudad: String,
    val pais: String,
    @SerializedName("imagen_url") val imagenUrl: String,
    val direccion: String,
    val estrellas: Int,
    /** Solo al crear (TRV-502). Al editar, las habitaciones se gestionan aparte (TRV-503). */
    val habitaciones: List<HabitacionUpsert>? = null,
)

data class SalidaUpsert(val fecha: String, val hora: String, @SerializedName("cupo_total") val cupoTotal: Int)

data class TourUpsert(
    val nombre: String,
    val descripcion: String,
    val ciudad: String,
    val pais: String,
    @SerializedName("imagen_url") val imagenUrl: String,
    val categoria: String,
    val duracion: Int,
    val precio: Double,
    /** Solo al crear (TRV-504). Luego se gestionan en "Salidas y actividades" (TRV-506). */
    val salidas: List<SalidaUpsert>? = null,
)

data class ActividadUpsert(
    val titulo: String,
    val descripcion: String? = null,
    @SerializedName("duracion_min") val duracionMin: Int? = null,
    val orden: Int? = null,
)
