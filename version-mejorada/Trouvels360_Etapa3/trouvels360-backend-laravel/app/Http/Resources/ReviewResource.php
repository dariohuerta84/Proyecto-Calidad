<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ReviewResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'servicio_id' => $this->servicio_id,
            'servicio_nombre' => $this->servicio?->nombre,
            'servicio_tipo' => $this->servicio?->tipo,
            'usuario' => [
                'id' => $this->usuario_id,
                'nombre' => $this->usuario?->nombre,
                'apellido' => $this->usuario?->apellido,
                'nombre_completo' => $this->usuario 
                    ? trim(($this->usuario->nombre ?? '') . ' ' . ($this->usuario->apellido ?? '')) ?: 'Usuario Anónimo'
                    : 'Usuario Anónimo',
            ],
            'comentario' => $this->comentario,
            'calificacion' => (int) $this->calificacion,
            'created_at' => $this->created_at?->toISOString(),
            'updated_at' => $this->updated_at?->toISOString(),
            'fecha_formateada' => $this->created_at?->locale('es')->diffForHumans(),
        ];
    }
}
