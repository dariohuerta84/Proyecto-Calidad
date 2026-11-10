<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreReservaHabitacionRequest extends FormRequest
{
    public function authorize(): bool
    {
        $user = $this->user();
        return $user && $user->rol === 'viajero';
    }

    public function rules(): array
    {
        return [
            'habitacion_id'   => ['required','integer','exists:habitaciones,id'],
            'fecha_inicio'    => ['required','date_format:Y-m-d'],
            'fecha_fin'       => ['required','date_format:Y-m-d','after:fecha_inicio'],
            'cantidad'        => ['required','integer','min:1'],
        ];
    }
}
