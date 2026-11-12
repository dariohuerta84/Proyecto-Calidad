<?php

namespace App\Rules;

use Closure;
use Illuminate\Contracts\Validation\ValidationRule;

/** Vencimiento MM/AA no vencido (la tarjeta es válida hasta el último día del mes). */
class VencimientoTarjeta implements ValidationRule
{
    public function validate(string $attribute, mixed $value, Closure $fail): void
    {
        if (!preg_match('/^(0[1-9]|1[0-2])\/(\d{2})$/', (string) $value, $m)) {
            $fail('El vencimiento debe tener el formato MM/AA.');
            return;
        }
        $finDeMes = \Carbon\Carbon::createFromDate(2000 + (int) $m[2], (int) $m[1], 1)->endOfMonth();
        if ($finDeMes->isPast()) {
            $fail('La tarjeta está vencida.');
        }
    }
}
