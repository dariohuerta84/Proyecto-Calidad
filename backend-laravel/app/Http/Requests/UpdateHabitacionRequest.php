<?php

namespace App\Http\Requests;

use App\Models\Habitacion;
use App\Models\Servicio;
use Illuminate\Foundation\Http\FormRequest;

class UpdateHabitacionRequest extends FormRequest
{
    public function authorize(): bool
    {
        $user = $this->user();
        if (!$user || $user->rol !== 'proveedor') return false;

        $habitacion = $this->route('habitacion');
        // Verifica propiedad del proveedor a través del servicio -> proveedor_id
        return $habitacion
            && $habitacion->hotel?->servicio?->proveedor_id === $user->id;
    }

    public function rules(): array
    {
        return [
            'nombre'            => ['sometimes','string','max:100'],
            'capacidad_adultos' => ['sometimes','integer','min:1'],
            'capacidad_ninos'   => ['sometimes','integer','min:0'],
            'cantidad'          => ['sometimes','integer','min:1'],
            'precio_por_noche'  => ['sometimes','numeric','min:0'],
            'descripcion'       => ['sometimes','nullable','string'],
        ];
    }
}
