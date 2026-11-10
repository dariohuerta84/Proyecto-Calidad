<?php

namespace App\Http\Requests;

use App\Models\Servicio;
use Illuminate\Foundation\Http\FormRequest;

class StoreHabitacionRequest extends FormRequest
{
    public function authorize(): bool
    {
        $user = $this->user();
        if (!$user || $user->rol !== 'proveedor') return false;

        $servicioId = (int) $this->route('servicio_id');
        $servicio = Servicio::find($servicioId);
        return $servicio
            && $servicio->tipo === 'hotel'
            && $servicio->proveedor_id === $user->id;
    }

    public function rules(): array
    {
        return [
            'nombre'            => ['required','string','max:100'],
            'capacidad_adultos' => ['required','integer','min:1'],
            'capacidad_ninos'   => ['nullable','integer','min:0'],
            'cantidad'          => ['required','integer','min:1'],
            'precio_por_noche'  => ['required','numeric','min:0'],
            'descripcion'       => ['nullable','string'],
        ];
    }
}
