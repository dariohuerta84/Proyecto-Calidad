<?php

namespace App\Rules;

use Closure;
use Illuminate\Contracts\Validation\ValidationRule;

/** Número de tarjeta de 16 dígitos que pasa el algoritmo de Luhn (pago simulado). */
class TarjetaLuhn implements ValidationRule
{
    public function validate(string $attribute, mixed $value, Closure $fail): void
    {
        $num = preg_replace('/\s+/', '', (string) $value);
        if (!preg_match('/^\d{16}$/', $num)) {
            $fail('El número de tarjeta debe tener 16 dígitos.');
            return;
        }
        $suma = 0;
        $alternar = false;
        for ($i = strlen($num) - 1; $i >= 0; $i--) {
            $d = (int) $num[$i];
            if ($alternar) {
                $d *= 2;
                if ($d > 9) {
                    $d -= 9;
                }
            }
            $suma += $d;
            $alternar = !$alternar;
        }
        if ($suma % 10 !== 0) {
            $fail('El número de tarjeta no es válido.');
        }
    }
}
