package com.trouvels360.app.data.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de la API Laravel. Las rutas son relativas a API_BASE_URL (termina en /api/).
 *
 * La sesión viaja en la cookie HttpOnly "access_token" (la guarda PersistentCookieJar) y las
 * peticiones que cambian estado llevan el header X-XSRF-TOKEN (lo agrega CsrfInterceptor),
 * exactamente como la web. Así el backend no necesita un modo especial para el móvil.
 *
 * Los DELETE devuelven Response<Unit> porque algunos responden 204 sin cuerpo.
 */
interface ApiService {

    // ---------------------------------------------------------------- Auth (TRV-101..103)
    @GET("auth/csrf") suspend fun csrf(): JsonObject
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): AuthResponse
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): AuthResponse
    @GET("auth/me") suspend fun me(): DataWrapper<UserDto>
    @POST("auth/refresh") suspend fun refresh(): MessageResponse
    @POST("auth/logout") suspend fun logout(): MessageResponse

    // ---------------------------------------------------------------- Mi cuenta (TRV-104, TRV-105)
    @GET("usuarios/me") suspend fun perfil(): DataWrapper<UserDto>
    @PATCH("usuarios/me") suspend fun actualizarPerfil(@Body cambios: Map<String, String>): DataWrapper<UserDto>
    @PUT("usuarios/me/password") suspend fun cambiarPassword(@Body body: PasswordRequest): MessageResponse
    @DELETE("usuarios/me") suspend fun eliminarCuenta(): Response<Unit>

    // ---------------------------------------------------------------- Hoteles (TRV-201..203)
    @GET("hoteles")
    suspend fun hoteles(
        @Query("ciudad") ciudad: String?,
        @Query("check_in") checkIn: String?,
        @Query("check_out") checkOut: String?,
        @Query("adultos") adultos: Int?,
        @Query("ninos") ninos: Int?,
        @Query("habitaciones") habitaciones: Int?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 10,
    ): Page<HotelItemDto>

    @GET("hoteles/{id}") suspend fun hotel(@Path("id") id: Int): HotelDetailResponse

    @GET("hoteles/{id}/disponibilidad")
    suspend fun disponibilidad(
        @Path("id") id: Int,
        @Query("check_in") checkIn: String,
        @Query("check_out") checkOut: String,
        @Query("adultos") adultos: Int,
        @Query("ninos") ninos: Int?,
        @Query("habitaciones") habitaciones: Int,
    ): DataWrapper<DisponibilidadData>

    @POST("reservas-habitaciones/cotizar")
    suspend fun cotizarHotel(@Body body: CotizarHotelRequest): DataWrapper<CotizacionHotelDto>

    @POST("reservas-habitaciones")
    suspend fun reservarHotel(
        @Body body: CrearReservaHotelRequest,
        @Header("Idempotency-Key") idempotencyKey: String,
    ): DataWrapper<SolicitudHotelDto>

    // ---------------------------------------------------------------- Tours (TRV-301..303)
    @GET("tours")
    suspend fun tours(
        @Query("destino") destino: String?,
        @Query("categoria") categoria: String?,
        @Query("checkIn") desde: String?,
        @Query("checkOut") hasta: String?,
        @Query("cupos") cupos: Int?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 10,
    ): Page<TourItemDto>

    @GET("tours/{id}") suspend fun tour(@Path("id") id: Int): TourDetailResponse

    @POST("tours/salidas/{salida}/cotizar")
    suspend fun cotizarTour(@Path("salida") salidaId: Int, @Body body: CotizarTourRequest): DataWrapper<CotizacionTourDto>

    @POST("tours/salidas/{salida}/reservas")
    suspend fun reservarTour(
        @Path("salida") salidaId: Int,
        @Body body: CrearReservaTourRequest,
        @Header("Idempotency-Key") idempotencyKey: String,
    ): DataWrapper<ReservaDto>

    // ---------------------------------------------------------------- Mis reservas (TRV-401, TRV-402)
    @GET("mis-reservas")
    suspend fun misReservasHotel(@Query("estado") estado: String?, @Query("page") page: Int, @Query("per_page") perPage: Int = 10): Page<ReservaDto>

    @POST("reservas-habitaciones/{id}/cancelar")
    suspend fun cancelarReservaHotel(@Path("id") id: Int): DataWrapper<ReservaDto>

    @GET("tours/mis-reservas")
    suspend fun misReservasTour(@Query("estado") estado: String?, @Query("page") page: Int, @Query("per_page") perPage: Int = 10): Page<ReservaDto>

    @POST("tours/reservas/{id}/cancelar")
    suspend fun cancelarReservaTour(@Path("id") id: Int): DataWrapper<ReservaDto>

    // ---------------------------------------------------------------- Reseñas (TRV-403)
    @GET("reviews")
    suspend fun reviews(@Query("servicio_id") servicioId: Int, @Query("page") page: Int, @Query("per_page") perPage: Int = 5): Page<ReviewDto>

    @GET("reviews/resumen") suspend fun resumenReviews(@Query("servicio_id") servicioId: Int): DataWrapper<ResumenResenas>
    @GET("reviews/puedo-opinar") suspend fun puedoOpinar(@Query("servicio_id") servicioId: Int): DataWrapper<PuedoOpinar>
    @POST("reviews") suspend fun crearReview(@Body body: ReviewRequest): DataWrapper<ReviewDto>
    @PUT("reviews/{id}") suspend fun editarReview(@Path("id") id: Int, @Body body: ReviewRequest): DataWrapper<ReviewDto>
    @DELETE("reviews/{id}") suspend fun eliminarReview(@Path("id") id: Int): Response<Unit>

    // ---------------------------------------------------------------- Itinerario (TRV-404)
    @POST("itineraries") suspend fun itinerario(@Body body: ItinerarioRequest): ItinerarioResponse

    // ---------------------------------------------------------------- Proveedor (TRV-204, 501..508)
    @GET("proveedor/indicadores") suspend fun indicadores(): DataWrapper<IndicadoresDto>

    @GET("proveedor/servicios")
    suspend fun misServicios(
        @Query("page") page: Int,
        @Query("tipo") tipo: String?,
        @Query("search") search: String?,
        @Query("per_page") perPage: Int = 15,
    ): Page<ServicioProveedorDto>

    @DELETE("servicios/{id}") suspend fun eliminarServicio(@Path("id") id: Int): Response<Unit>

    @GET("proveedor/reservas")
    suspend fun reservasRecibidas(
        @Query("tipo") tipo: String?,
        @Query("estado") estado: String?,
        @Query("servicio_id") servicioId: Int?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 15,
    ): Page<ReservaDto>

    @POST("proveedor/reservas-habitaciones/{id}/aceptar")
    suspend fun aceptarReserva(@Path("id") id: Int): DataWrapper<ReservaDto>

    @POST("proveedor/reservas-habitaciones/{id}/rechazar")
    suspend fun rechazarReserva(@Path("id") id: Int, @Body body: RechazoRequest): DataWrapper<ReservaDto>

    // Hoteles y tipos de habitación (TRV-502, TRV-503)
    @POST("hoteles") suspend fun crearHotel(@Body body: HotelUpsert): JsonObject
    @PUT("hoteles/{id}") suspend fun editarHotel(@Path("id") id: Int, @Body body: HotelUpsert): JsonObject
    @POST("hoteles/{id}/habitaciones") suspend fun crearHabitacion(@Path("id") hotelId: Int, @Body body: HabitacionUpsert): JsonObject
    @PUT("habitaciones/{id}") suspend fun editarHabitacion(@Path("id") id: Int, @Body body: HabitacionUpsert): JsonObject
    @DELETE("habitaciones/{id}") suspend fun eliminarHabitacion(@Path("id") id: Int): Response<Unit>

    // Tours, salidas y actividades (TRV-504..506)
    @POST("tours") suspend fun crearTour(@Body body: TourUpsert): JsonObject
    @PUT("tours/{id}") suspend fun editarTour(@Path("id") id: Int, @Body body: TourUpsert): JsonObject
    @GET("tours/{id}/salidas") suspend fun salidas(@Path("id") tourId: Int): List<SalidaDto>
    @POST("tours/{id}/salidas") suspend fun crearSalida(@Path("id") tourId: Int, @Body body: SalidaUpsert): JsonObject
    @PATCH("tours/{id}/salidas/{salida}")
    suspend fun editarSalida(@Path("id") tourId: Int, @Path("salida") salidaId: Int, @Body cambios: Map<String, @JvmSuppressWildcards Any>): JsonObject
    @DELETE("tours/{id}/salidas/{salida}") suspend fun eliminarSalida(@Path("id") tourId: Int, @Path("salida") salidaId: Int): Response<Unit>
    @GET("tours/{id}/actividades") suspend fun actividades(@Path("id") tourId: Int): List<ActividadDto>
    @POST("tours/{id}/actividades") suspend fun crearActividad(@Path("id") tourId: Int, @Body body: ActividadUpsert): JsonObject
    @PUT("tours/{id}/actividades/{act}")
    suspend fun editarActividad(@Path("id") tourId: Int, @Path("act") actividadId: Int, @Body body: ActividadUpsert): JsonObject
    @PATCH("tours/{id}/actividades/{act}")
    suspend fun moverActividad(@Path("id") tourId: Int, @Path("act") actividadId: Int, @Body cambios: Map<String, @JvmSuppressWildcards Any>): JsonObject
    @DELETE("tours/{id}/actividades/{act}") suspend fun eliminarActividad(@Path("id") tourId: Int, @Path("act") actividadId: Int): Response<Unit>
}
