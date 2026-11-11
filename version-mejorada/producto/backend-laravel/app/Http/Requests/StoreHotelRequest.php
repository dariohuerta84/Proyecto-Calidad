<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreHotelRequest extends FormRequest
{
    /**
     * Solo proveedores autenticados pueden crear hoteles.
     * El proveedor dueño se tomará del usuario autenticado (no del body).
     */
    public function authorize(): bool
    {
        return auth()->check() && auth()->user()->rol === 'proveedor';
    }

    /**
     * Ahora recibimos JSON plano (sin objeto "servicio").
     * Validamos campos del Servicio + Hotel directamente.
     */
    public function rules(): array
    {
        return [
            // SERVICIO
            'nombre'      => ['required','string','max:150'],
            'descripcion' => ['sometimes','nullable','string'],
            'ciudad'      => ['required','string','max:100'],
            'pais'        => ['required','string','max:100'],
            'imagen_url'  => ['required','url','max:500'],
            'activo'      => ['sometimes','boolean'],
            'latitud'     => ['nullable','numeric','between:-90,90'],
            'longitud'    => ['nullable','numeric','between:-180,180'],

            // HOTEL
            'direccion'   => ['required','string','max:255'],
            'estrellas'   => ['required','integer','between:1,5'],

            // HABITACIONES INICIALES (al menos una)
            'habitaciones'                     => ['required','array','min:1','max:20'],
            'habitaciones.*.nombre'            => ['required','string','max:100','distinct'],
            'habitaciones.*.capacidad_adultos' => ['required','integer','min:1','max:20'],
            'habitaciones.*.capacidad_ninos'   => ['nullable','integer','min:0','max:20'],
            'habitaciones.*.cantidad'          => ['required','integer','min:1','max:500'],
            'habitaciones.*.precio_por_noche'  => ['required','numeric','gt:0','max:100000'],
            'habitaciones.*.descripcion'       => ['nullable','string','max:1000'],

            // Galería (servicio_imagenes)
            'imagenes'       => ['sometimes','array','max:5'],
            'imagenes.*'     => ['nullable'],
            'imagenes.*.url' => ['sometimes','required','url','max:500'],
            'imagenes.*.alt' => ['sometimes','nullable','string','max:150'],
        ];
    }

    public function messages(): array
    {
        return [
            'imagenes.max'          => 'Máximo 5 imágenes en la galería.',
            'habitaciones.required' => 'Agrega al menos un tipo de habitación.',
            'habitaciones.min'      => 'Agrega al menos un tipo de habitación.',
            'imagen_url.required'   => 'La imagen principal es obligatoria.',
            'habitaciones.*.nombre.distinct' => 'Los nombres de habitación no se pueden repetir.',
        ];
    }
}
