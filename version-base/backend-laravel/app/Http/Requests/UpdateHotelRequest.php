<?php

namespace App\Http\Requests;

use App\Models\Servicio;
use Illuminate\Foundation\Http\FormRequest;

class UpdateHotelRequest extends FormRequest
{
    public function authorize(): bool
    {
        $user = $this->user();
        if (!$user || $user->rol !== 'proveedor') {
            return false;
        }

        $servicioId = (int) $this->route('servicio_id');
        $servicio = Servicio::find($servicioId);

        return $servicio
            && $servicio->tipo === 'hotel'
            && (int) $servicio->proveedor_id === (int) $user->id;
    }

    public function rules(): array
    {
        return [
            // SERVICIO
            'nombre'      => ['sometimes','string','max:150'],
            'descripcion' => ['sometimes','nullable','string'],
            'ciudad'      => ['sometimes','string','max:100'],
            'pais'        => ['sometimes','string','max:100'],
            'imagen_url'  => ['sometimes','nullable','url','max:500'],
            'activo'      => ['sometimes','boolean'],

            // HOTEL
            'direccion'   => ['sometimes','string','max:255'],
            'estrellas'   => ['sometimes','nullable','integer','between:1,5'],

            // Galería (reemplazo total si viene)
            'imagenes'       => ['sometimes','array','max:5'],
            'imagenes.*'     => ['nullable'],
            'imagenes.*.url' => ['sometimes','required','url','max:500'],
            'imagenes.*.alt' => ['sometimes','nullable','string','max:150'],

            // HABITACIONES
            'habitaciones'                 => ['sometimes', 'array'],
            'habitaciones.*.id'            => ['nullable', 'integer', 'exists:habitaciones,id'], // ID para UPDATE/DELETE
            'habitaciones.*.nombre'        => ['sometimes', 'string', 'max:100'],
            'habitaciones.*.capacidad_adultos' => ['sometimes', 'integer', 'min:1'],
            'habitaciones.*.capacidad_ninos'   => ['sometimes', 'integer', 'min:0'],
            'habitaciones.*.precio_por_noche'  => ['sometimes', 'numeric', 'min:0'],
            'habitaciones.*.cantidad'      => ['sometimes', 'integer', 'min:1'],
            'habitaciones.*.descripcion'   => ['nullable', 'string'],
        ];
    }

}