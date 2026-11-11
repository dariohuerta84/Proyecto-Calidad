<?php

namespace App\Support;

/**
 * Cálculo monetario con IGV del 18%.
 * Se trabaja en céntimos (enteros) para evitar errores de coma flotante:
 * web, móvil y lo guardado coinciden al céntimo.
 */
final class Precio
{
    public const IGV = 18; // %

    /** Convierte un monto decimal a céntimos (half-up). */
    public static function aCentimos(float|int|string $monto): int
    {
        return (int) round(((float) $monto) * 100, 0, PHP_ROUND_HALF_UP);
    }

    /** IGV en céntimos de un subtotal en céntimos (half-up). */
    public static function igvCentimos(int $subtotalCentimos): int
    {
        return intdiv($subtotalCentimos * self::IGV + 50, 100);
    }

    /**
     * @return array{subtotal: float, impuesto: float, total: float}
     */
    public static function desdeSubtotalCentimos(int $subtotalCentimos): array
    {
        $igv = self::igvCentimos($subtotalCentimos);

        return [
            'subtotal' => $subtotalCentimos / 100,
            'impuesto' => $igv / 100,
            'total'    => ($subtotalCentimos + $igv) / 100,
        ];
    }

    /** Subtotal de una línea: tarifa × cantidad × multiplicador (noches o 1), en céntimos. */
    public static function lineaCentimos(float|string $tarifa, int $cantidad, int $multiplicador = 1): int
    {
        return self::aCentimos($tarifa) * $cantidad * $multiplicador;
    }
}
