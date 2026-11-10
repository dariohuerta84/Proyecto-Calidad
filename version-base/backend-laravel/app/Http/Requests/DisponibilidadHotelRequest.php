<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class DisponibilidadHotelRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; // protege luego si quieres
    }

    public function rules(): array
    {
        return [
            'check_in'  => ['required','date','date_format:Y-m-d'],
            'check_out' => ['required','date','date_format:Y-m-d','after:check_in'],
            'adultos'   => ['nullable','integer','min:1'],
            'ninos'     => ['nullable','integer','min:0'],
            'habitaciones' => ['nullable','integer','min:1'],
        ];
    }

    protected function prepareForValidation(): void
    {
        // Normaliza valores opcionales
        $this->merge([
            'adultos'      => $this->adultos !== null ? (int) $this->adultos : null,
            'ninos'        => $this->ninos   !== null ? (int) $this->ninos   : null,
            'habitaciones' => $this->habitaciones !== null ? (int) $this->habitaciones : 1,
        ]);
    }
}