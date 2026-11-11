<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class DisponibilidadHotelRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; // consulta pública
    }

    public function rules(): array
    {
        return [
            'check_in'     => ['required', 'date_format:Y-m-d', 'after_or_equal:today'],
            'check_out'    => ['required', 'date_format:Y-m-d', 'after:check_in'],
            'adultos'      => ['nullable', 'integer', 'between:1,10'],
            'ninos'        => ['nullable', 'integer', 'between:0,10'],
            'habitaciones' => ['nullable', 'integer', 'between:1,10'],
        ];
    }

    public function messages(): array
    {
        return [
            'check_in.after_or_equal' => 'La fecha de entrada no puede ser anterior a hoy.',
            'check_out.after'         => 'La fecha de salida debe ser posterior a la de entrada.',
        ];
    }
}
