<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ReservaResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        $response = [
            'id' => $this->id,
            'codigo_reserva' => $this->codigo_reserva,
            'usuario' => $this->usuario->only('id', 'nombre', 'apellido', 'email'),
            'servicio' => [
                'id' => $this->servicio->id,
                'nombre' => $this->servicio->nombre,
                'tipo' => $this->servicio->tipo,
                'ciudad' => $this->servicio->ciudad,
                'precio_base' => $this->servicio->precio,
                'descripcion' => $this->servicio->descripcion ?? null,
                'imagen_url' => $this->servicio->imagen_url ?? null,
                'proveedor' => $this->when($this->servicio->relationLoaded('proveedor'), 
                    $this->servicio->proveedor?->only('id', 'nombre', 'apellido', 'email')
                ),
            ],
            'fecha_inicio' => $this->fecha_inicio,
            'fecha_fin' => $this->fecha_fin,
            'huespedes' => $this->huespedes,
            'estado' => $this->estado,
            'created_at' => $this->created_at,
            'updated_at' => $this->updated_at,
        ];

        // Agregar detalles específicos según el tipo de servicio
        if ($this->servicio->tipo === 'hotel' && $this->servicio->relationLoaded('hotel') && $this->servicio->hotel) {
            $response['servicio']['detalles_hotel'] = [
                'direccion' => $this->servicio->hotel->direccion,
                'estrellas' => $this->servicio->hotel->estrellas,
                'precio_por_noche' => $this->servicio->hotel->precio_por_noche,
            ];
        }

        if ($this->servicio->tipo === 'tour' && $this->servicio->relationLoaded('tour') && $this->servicio->tour) {
            $response['servicio']['detalles_tour'] = [
                'categoria' => $this->servicio->tour->categoria,
                'duracion' => $this->servicio->tour->duracion,
                'precio_por_persona' => $this->servicio->tour->precio_por_persona,
            ];
        }

        // Calcular precio total estimado usando el accessor del modelo
        $response['precio_total_estimado'] = $this->precio_total;
        $response['dias_reserva'] = $this->dias_reserva;

        return $response;
    }
}