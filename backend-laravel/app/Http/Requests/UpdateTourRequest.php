<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateTourRequest extends FormRequest
{
    public function authorize(): bool
    {
        return auth()->check() && auth()->user()->rol === 'proveedor';
    }

    public function rules(): array
    {
        return [
            // Servicio (opcionales)
            'nombre'      => ['sometimes','string','max:150'],
            'descripcion' => ['sometimes','nullable','string'],
            'ciudad'      => ['sometimes','string','max:100'],
            'pais'        => ['sometimes','string','max:100'],
            'imagen_url'  => ['sometimes','nullable','url','max:500'],
            'activo'      => ['sometimes','boolean'],

            // Tour (opcionales)
            'categoria'           => ['sometimes','in:Gastronomía,Aventura,Cultura,Relajación'],
            'duracion'            => ['sometimes','integer','min:1','max:10080'],
            'latitud'             => ['sometimes','nullable','numeric','between:-90,90'],
            'longitud'            => ['sometimes','nullable','numeric','between:-180,180'],
            'precio'              => ['sometimes','numeric','min:0'],

            // Galería (reemplazo total si viene)
            'imagenes'       => ['sometimes','array','max:5'],
            'imagenes.*.url' => ['sometimes','url','max:500'],
            'imagenes.*.alt' => ['sometimes', 'nullable','string','max:150'],

            // Items (cosas para llevar)
            'items' => ['sometimes','array','max:10'], // opcional y limitado a 10
            'items.*.nombre' => ['required_with:items','string','max:100'],
            'items.*.icono'  => ['nullable','string','max:50'], // puede ser emoji o ícono corto

        ];
    }
}
