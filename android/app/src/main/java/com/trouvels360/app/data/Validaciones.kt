package com.trouvels360.app.data

import java.time.YearMonth

/** Validaciones del pago simulado: las mismas reglas que la API (Luhn, vencimiento futuro, CVC). */
object ValidacionPago {
    fun soloDigitos(s: String) = s.filter { it.isDigit() }

    fun luhnValido(numero: String): Boolean {
        val d = soloDigitos(numero)
        if (d.length !in 13..19) return false
        var suma = 0
        d.reversed().forEachIndexed { i, c ->
            var n = c - '0'
            if (i % 2 == 1) { n *= 2; if (n > 9) n -= 9 }
            suma += n
        }
        return suma % 10 == 0
    }

    /** "MM/AA" con fecha igual o posterior al mes actual. */
    fun vencimientoValido(v: String, hoy: YearMonth = YearMonth.now()): Boolean {
        val m = Regex("""^(0[1-9]|1[0-2])/(\d{2})$""").matchEntire(v.trim()) ?: return false
        val ym = YearMonth.of(2000 + m.groupValues[2].toInt(), m.groupValues[1].toInt())
        return !ym.isBefore(hoy)
    }

    fun cvcValido(c: String) = Regex("""^\d{3,4}$""").matches(c.trim())

    fun errores(titular: String, numero: String, venc: String, cvc: String): Map<String, String> = buildMap {
        if (titular.trim().length < 3) put("titular", "Escribe el nombre como figura en la tarjeta.")
        if (!luhnValido(numero)) put("numero", "El número de tarjeta no es válido.")
        if (!vencimientoValido(venc)) put("vencimiento", "Usa MM/AA y una fecha no vencida.")
        if (!cvcValido(cvc)) put("cvc", "El CVC tiene 3 o 4 dígitos.")
    }
}


/** Validaciones de formularios con las mismas reglas que la API (mensajes en español). */
object Validaciones {
    private val email = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")
    private val telefono = Regex("""^\+51 9\d{8}$""")
    private val soloLetras = Regex("""^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]+$""")

    fun emailValido(s: String) = email.matches(s.trim())

    /** Acepta "987654321", "+51987654321" o "+51 987654321" y lo deja como "+51 987654321". */
    fun normalizarTelefono(s: String): String {
        val d = s.filter { it.isDigit() }.removePrefix("51").takeLast(9)
        return if (d.length == 9) "+51 $d" else s.trim()
    }

    fun telefonoValido(s: String) = telefono.matches(normalizarTelefono(s))
    fun rucValido(s: String) = Regex("""^\d{11}$""").matches(s.trim())
    fun nombreValido(s: String) = s.trim().isNotEmpty() && s.trim().length <= 100 && soloLetras.matches(s.trim())
    fun urlValida(s: String) = Regex("""^https?://\S+$""", RegexOption.IGNORE_CASE).matches(s.trim())

    /** Comentario de reseña: 10 a 300 caracteres (TRV-403). */
    fun comentarioValido(s: String) = s.trim().length in 10..300

    /** Motivo de rechazo: 10 a 300 caracteres (TRV-204). */
    fun motivoValido(s: String) = s.trim().length in 10..300

    /** "HH:MM" 00:00–23:59. */
    fun horaValida(s: String) = Regex("""^([01]\d|2[0-3]):[0-5]\d$""").matches(s.trim())
}
