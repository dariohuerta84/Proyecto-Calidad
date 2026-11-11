<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateServicioRequest extends FormRequest
{
    public function authorize(): bool
    {
        $user = $this->user();
        $servicio = $this->route('servicio'); // route-model binding

        return $user
            && $user->rol === 'proveedor'
            && $servicio
            && (int) $servicio->proveedor_id === (int) $user->id;
    }

    protected function prepareForValidation(): void
    {
        if ($this->has('nombre') && is_string($this->nombre)) {
            $this->merge(['nombre' => trim($this->nombre)]);
        }
        if ($this->has('ciudad') && is_string($this->ciudad)) {
            $this->merge(['ciudad' => trim($this->ciudad)]);
        }
    }

    public function rules(): array
    {
        return [
            'proveedor_id' => ['prohibited'],
            'nombre'       => ['sometimes','string','max:150'],
            'tipo'         => ['sometimes', Rule::in(['hotel','tour'])],
            'descripcion'  => ['sometimes','nullable','string'],
            'ciudad'       => ['sometimes','string','max:100'],
            'pais'         => ['sometimes','string','max:100'],
            'imagen_url'   => ['sometimes','nullable','url','max:500'],
            'activo'       => ['sometimes','boolean'],

            // 👇 NUEVO: reemplazo total de la galería si viene
            'imagenes'     => ['sometimes','array','max:5'],
            'imagenes.*'   => ['nullable'],
            'imagenes.*.url' => ['sometimes','required','url','max:500'],
            'imagenes.*.alt' => ['sometimes','nullable','string','max:150'],
        ];
    }
}
