<?php

namespace App\Support;

/**
 * Normaliza celulares peruanos al formato "+51 9XXXXXXXX".
 * Acepta: 987654321, +51987654321, +51 987 654 321, 51-987-654-321.
 */
final class Telefono
{
    public static function normalizar(?string $valor): ?string
    {
        if ($valor === null) {
            return null;
        }
        $digitos = preg_replace('/\D/', '', $valor);
        if (strlen($digitos) === 11 && str_starts_with($digitos, '51')) {
            $digitos = substr($digitos, 2);
        }
        if (preg_match('/^9\d{8}$/', $digitos)) {
            return '+51 ' . $digitos;
        }

        return trim($valor); // inválido: lo devuelve tal cual para que la validación lo rechace
    }

    public const REGEX = '/^\+51 9\d{8}$/';
    public const MENSAJE = 'El teléfono debe tener el formato +51 9########.';
}
