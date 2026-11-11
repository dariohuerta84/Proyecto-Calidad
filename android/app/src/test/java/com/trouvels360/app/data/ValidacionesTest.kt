package com.trouvels360.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/** Pruebas unitarias de las reglas que la app valida antes de llamar a la API. */
class ValidacionesTest {

    @Test fun luhnAceptaTarjetaDePrueba() {
        assertTrue(ValidacionPago.luhnValido("4242 4242 4242 4242"))
        assertTrue(ValidacionPago.luhnValido("5555555555554444"))
    }

    @Test fun luhnRechazaNumeroAlterado() {
        assertFalse(ValidacionPago.luhnValido("4242424242424241"))
        assertFalse(ValidacionPago.luhnValido("1234"))
        assertFalse(ValidacionPago.luhnValido(""))
    }

    @Test fun vencimientoDebeSerFuturoOMesActual() {
        val hoy = YearMonth.of(2026, 11)
        assertTrue(ValidacionPago.vencimientoValido("11/26", hoy))
        assertTrue(ValidacionPago.vencimientoValido("12/30", hoy))
        assertFalse(ValidacionPago.vencimientoValido("10/26", hoy))
        assertFalse(ValidacionPago.vencimientoValido("13/30", hoy))
        assertFalse(ValidacionPago.vencimientoValido("1230", hoy))
    }

    @Test fun cvcDeTresOCuatroDigitos() {
        assertTrue(ValidacionPago.cvcValido("123"))
        assertTrue(ValidacionPago.cvcValido("1234"))
        assertFalse(ValidacionPago.cvcValido("12"))
        assertFalse(ValidacionPago.cvcValido("12a"))
    }

    @Test fun erroresDePagoPorCampo() {
        val e = ValidacionPago.errores("Ed", "4242424242424241", "01/20", "1")
        assertEquals(setOf("titular", "numero", "vencimiento", "cvc"), e.keys)
        assertTrue(ValidacionPago.errores("Edwin Jara", "4242424242424242", "12/99", "123").isEmpty())
    }

    @Test fun telefonoSeNormalizaAlFormatoDeLaApi() {
        assertEquals("+51 987654321", Validaciones.normalizarTelefono("987654321"))
        assertEquals("+51 987654321", Validaciones.normalizarTelefono("+51987654321"))
        assertEquals("+51 987654321", Validaciones.normalizarTelefono("+51 987 654 321"))
        assertTrue(Validaciones.telefonoValido("987654321"))
        assertFalse(Validaciones.telefonoValido("887654321"))
    }

    @Test fun rucEmailYNombre() {
        assertTrue(Validaciones.rucValido("20123456789"))
        assertFalse(Validaciones.rucValido("2012345678"))
        assertTrue(Validaciones.emailValido("qa.viajero1@trouvels360.test"))
        assertFalse(Validaciones.emailValido("sin-arroba"))
        assertTrue(Validaciones.nombreValido("José Ñúñez"))
        assertFalse(Validaciones.nombreValido("R2D2"))
    }

    @Test fun comentarioYMotivoEntre10Y300() {
        assertFalse(Validaciones.comentarioValido("corto"))
        assertTrue(Validaciones.comentarioValido("Muy buena atención."))
        assertFalse(Validaciones.comentarioValido("x".repeat(301)))
        assertTrue(Validaciones.motivoValido("Mantenimiento de la habitación."))
        assertFalse(Validaciones.motivoValido("   no   "))
    }

    @Test fun horaYUrl() {
        assertTrue(Validaciones.horaValida("07:30"))
        assertFalse(Validaciones.horaValida("24:00"))
        assertTrue(Validaciones.urlValida("https://example.com/h.jpg"))
        assertFalse(Validaciones.urlValida("ftp://x"))
    }
}
