package com.trouvels360.app.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.trouvels360.app.data.remote.AuthResponse
import com.trouvels360.app.data.remote.CotizacionHotelDto
import com.trouvels360.app.data.remote.CotizacionTourDto
import com.trouvels360.app.data.remote.DataWrapper
import com.trouvels360.app.data.remote.HotelDetailResponse
import com.trouvels360.app.data.remote.HotelItemDto
import com.trouvels360.app.data.remote.IndicadoresDto
import com.trouvels360.app.data.remote.ItinerarioResponse
import com.trouvels360.app.data.remote.LenientListAdapterFactory
import com.trouvels360.app.data.remote.Page
import com.trouvels360.app.data.remote.PuedoOpinar
import com.trouvels360.app.data.remote.ReservaDto
import com.trouvels360.app.data.remote.ReviewDto
import com.trouvels360.app.data.remote.SalidaDto
import com.trouvels360.app.data.remote.ServicioProveedorDto
import com.trouvels360.app.data.remote.SolicitudHotelDto
import com.trouvels360.app.data.remote.TourDetailResponse
import com.trouvels360.app.data.remote.TourItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba de contrato: respuestas REALES de la API (capturadas del backend, en src/test/resources/api)
 * deben convertirse a los modelos de la app sin perder los campos que usan las pantallas.
 * Si el backend cambia un nombre de campo, esta prueba falla antes que la app.
 */
class ContratoApiTest {
    private val gson: Gson = GsonBuilder().registerTypeAdapterFactory(LenientListAdapterFactory()).create()

    private inline fun <reified T> leer(nombre: String): T {
        val texto = requireNotNull(javaClass.classLoader?.getResource("api/$nombre.json")) { "Falta api/$nombre.json" }.readText()
        return gson.fromJson(texto, object : TypeToken<T>() {}.type)
    }

    @Test fun login() {
        val r = leer<AuthResponse>("login")
        assertTrue(r.data?.user?.esViajero == true)
        assertNotNull(r.data?.user?.email)
    }

    @Test fun busquedaDeHotelesPaginada() {
        val p = leer<Page<HotelItemDto>>("hoteles")
        assertTrue(p.items.isNotEmpty())
        assertTrue(p.lastPage >= 1)
        val h = p.items.first()
        assertNotNull(h.precioPorNoche)
        assertNotNull(h.calificacion)
    }

    @Test fun detalleDeHotelConHabitaciones() {
        val d = leer<HotelDetailResponse>("hotel")
        assertNotNull(d.hotel?.nombre)
        assertTrue(d.habitaciones.orEmpty().all { it.id != null && it.precioPorNoche != null })
        val disp = leer<DataWrapper<com.trouvels360.app.data.remote.DisponibilidadData>>("disponibilidad")
        assertTrue(disp.data?.habitaciones.orEmpty().all { it.unidadesDisponibles != null })
    }

    @Test fun cotizacionHotelSumaIgv() {
        val c = requireNotNull(leer<DataWrapper<CotizacionHotelDto>>("cotizar_hotel").data)
        assertEquals(18, c.igvPorcentaje)
        assertEquals(c.total!!, c.subtotal!! + c.impuesto!!, 0.01)
    }

    @Test fun solicitudHotelQuedaPendiente() {
        val s = requireNotNull(leer<DataWrapper<SolicitudHotelDto>>("reservar_hotel").data)
        assertEquals("pendiente", s.estado)
        assertTrue(s.reservas.orEmpty().isNotEmpty())
    }

    @Test fun toursYDetalleConSalidas() {
        val p = leer<Page<TourItemDto>>("tours")
        assertNotNull(p.items.first().tour?.precio)
        val t = requireNotNull(leer<TourDetailResponse>("tour").servicio)
        assertTrue(t.salidas.orEmpty().isNotEmpty())
        assertTrue(t.salidas.orEmpty().all { it.libres >= 0 && it.hora != null })
    }

    @Test fun cotizacionYReservaDeTourConfirmada() {
        val c = requireNotNull(leer<DataWrapper<CotizacionTourDto>>("cotizar_tour").data)
        assertEquals(c.total!!, c.subtotal!! + c.impuesto!!, 0.01)
        val r = requireNotNull(leer<DataWrapper<ReservaDto>>("reservar_tour").data)
        assertEquals("confirmada", r.estado)
        assertNotNull(r.salida?.fecha)
    }

    @Test fun misReservas() {
        val h = leer<Page<ReservaDto>>("mis_hotel")
        assertTrue(h.items.all { it.esHotel && it.cancelable != null })
        val t = leer<Page<ReservaDto>>("mis_tour")
        assertTrue(t.items.all { !it.esHotel })
    }

    @Test fun resenas() {
        val p = leer<Page<ReviewDto>>("reviews")
        assertNotNull(p.items.first().usuario?.nombreCompleto)
        val e = requireNotNull(leer<DataWrapper<PuedoOpinar>>("puedo").data)
        assertNotNull(e.puede)
    }

    @Test fun itinerarioConSalidaSugerida() {
        val it = leer<ItinerarioResponse>("itinerario")
        assertTrue(it.success == true)
        val act = it.data?.itinerario.orEmpty().flatMap { d -> d.actividades.orEmpty() }
        assertTrue(act.all { a -> a.salida?.hora != null })
    }

    @Test fun proveedor() {
        val ind = requireNotNull(leer<DataWrapper<IndicadoresDto>>("indicadores").data)
        assertNotNull(ind.solicitudesPendientes)
        assertNotNull(ind.reservasHotel?.get("pendiente"))
        val s = leer<Page<ServicioProveedorDto>>("servicios")
        assertTrue(s.items.all { it.reservasTotales != null && it.metaTipo != null })
        val rr = leer<Page<ReservaDto>>("recibidas")
        assertTrue(rr.items.all { it.cliente?.email != null })
        val sal = leer<List<SalidaDto>>("salidas")
        assertTrue(sal.all { it.estado != null && it.cupoTotal != null })
    }
}
