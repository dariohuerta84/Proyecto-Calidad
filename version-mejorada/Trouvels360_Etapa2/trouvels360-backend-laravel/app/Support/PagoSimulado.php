<?php

namespace App\Support;

use App\Rules\TarjetaLuhn;
use App\Rules\VencimientoTarjeta;

/**
 * Reglas del pago simulado (TRV-203 / TRV-303).
 * No se guarda ningún dato de tarjeta salvo los 4 últimos dígitos.
 */
final class PagoSimulado
{
    public static function reglas(): array
    {
        return [
            'pago'             => ['required', 'array'],
            'pago.titular'     => ['required', 'string', 'min:2', 'max:100', "regex:/^[\\pL\\s'.-]+$/u"],
            'pago.numero'      => ['required', 'string', new TarjetaLuhn()],
            'pago.vencimiento' => ['required', 'string', new VencimientoTarjeta()],
            'pago.cvc'         => ['required', 'regex:/^\d{3}$/'],
        ];
    }

    public static function mensajes(): array
    {
        return [
            'pago.required'         => 'Los datos de pago son obligatorios.',
            'pago.titular.required' => 'Ingresa el titular de la tarjeta.',
            'pago.titular.regex'    => 'El titular solo puede contener letras.',
            'pago.cvc.regex'        => 'El CVC debe tener 3 dígitos.',
            'pago.cvc.required'     => 'Ingresa el CVC.',
        ];
    }

    public static function ultimos4(array $pago): string
    {
        return substr(preg_replace('/\D/', '', $pago['numero'] ?? ''), -4);
    }
}
