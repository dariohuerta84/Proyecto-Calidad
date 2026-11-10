<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

/**
 * Resource para exponer un Hotel con datos del Servicio y (opcionalmente) disponibilidad.
 * - Toma nombre/ciudad/pais desde $hotel->servicio
 * - Si se pasan $desde/$hasta, incluye bloque de disponibilidad + habitaciones filtradas
 */
class HotelResource extends JsonResource
{
    /**
     * Fechas de disponibilidad (opcionales). Si no se pasan, se devuelven todas las habitaciones.
     */
    protected ?string $desde;
    protected ?string $hasta;

    /**
     * Filtros usados para calcular disponibilidad (adultos, niños, habitaciones).
     */
    protected array $filtros;

    /**
     * Habitaciones resultantes del cálculo de disponibilidad.
     * Si está vacío, se usarán las habitaciones cargadas en la relación (si existen).
     */
    protected array $habitacionesDisponibles;

    /**
     * @param  mixed        $resource    Modelo Hotel (con relaciones)
     * @param  string|null  $desde       Fecha inicio (YYYY-MM-DD)
     * @param  string|null  $hasta       Fecha fin (YYYY-MM-DD)
     * @param  array        $filtros     Filtros de búsqueda (adultos, niños, habitaciones)
     * @param  array        $habitacionesDisponibles  Habitaciones ya filtradas/cálculo disponibilidad
     */
    public function __construct($resource, ?string $desde = null, ?string $hasta = null, array $filtros = [], array $habitacionesDisponibles = [])
    {
        parent::__construct($resource);
        $this->desde = $desde;
        $this->hasta = $hasta;
        $this->filtros = $filtros;
        $this->habitacionesDisponibles = $habitacionesDisponibles;
    }

    /**
     * Transforma el recurso en un arreglo JSON listo para la API.
     */
    public function toArray(Request $request): array
    {
        // Relación servicio: se espera eager-loaded en el controller.
        $servicio = $this->servicio;

        // Calcular precio mínimo:
        // - Si tenemos habitacionesDisponibles (modo disponibilidad), usar ese array.
        // - Si no, y la relación 'habitaciones' está cargada, usar la relación.
        $precioMinimo = null;
        if (!empty($this->habitacionesDisponibles)) {
            // habitacionesDisponibles es un array simple; sacamos el min de 'precio_por_noche'
            $precios = array_column($this->habitacionesDisponibles, 'precio_por_noche');
            if (!empty($precios)) {
                $precioMinimo = min($precios);
            }
        } elseif ($this->relationLoaded('habitaciones')) {
            $precioMinimo = optional($this->habitaciones)->min('precio_por_noche');
        }

        // Normalizamos imagenUrl como array (aunque sea una sola imagen)
        $imagenes = [];
        if (!empty($servicio?->imagen_url)) {
            $imagenes[] = $servicio->imagen_url;
        }

        $data = [
            'id'                => $this->servicio_id,                    // PK de hotel = id del servicio
            'tipo'              => $servicio?->tipo ?? 'hotel',
            'nombre'            => $servicio?->nombre,                    // nombre comercial desde servicios
            'ciudad'            => $servicio?->ciudad,
            'pais'              => $servicio?->pais,
            'direccion'         => $this->direccion,
            'estrellas'         => $this->estrellas,
            'precio_por_noche'  => $precioMinimo !== null ? (float) $precioMinimo : null,
            'imagenUrl'         => $imagenes,
            'descripcion'       => $servicio?->descripcion,
        ];

        // Si tenemos información de disponibilidad (desde/hasta), agregamos bloque y la lista prefiltrada
        if ($this->desde && $this->hasta) {
            $data['disponibilidad'] = [
                'desde' => $this->desde,
                'hasta' => $this->hasta,
            ];
            $data['filtros'] = $this->filtros ?: null;
            $data['habitaciones'] = $this->habitacionesDisponibles;
            return $data;
        }

        // Si NO hay disponibilidad calculada, devolvemos las habitaciones cargadas (si existen)
        if ($this->relationLoaded('habitaciones')) {
            $data['habitaciones'] = $this->habitaciones->map(function ($h) {
                return [
                    'id'                   => $h->id,
                    'nombre'               => $h->nombre,
                    'capacidad_adultos'    => (int) $h->capacidad_adultos,
                    'capacidad_ninos'      => (int) $h->capacidad_ninos,
                    'precio_por_noche'     => (float) $h->precio_por_noche,
                    'unidades_totales'     => (int) $h->cantidad,
                    'unidades_disponibles' => (int) $h->cantidad, // sin cálculo = igual al stock
                    'descripcion'          => $h->descripcion,
                ];
            })->values();
        }

        return $data;
    }
}
