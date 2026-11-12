<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreTourSalidaRequest extends FormRequest
{
    public function authorize(): bool { return true; }

    public function rules(): array
    {
        return [
            'fecha'      => ['required','date','after_or_equal:today'],
            'hora'       => ['nullable','date_format:H:i'],
            'cupo_total' => ['required','integer','min:1'],
            'estado'     => ['nullable','in:abierta,cerrada,finalizada'],
        ];
    }
}
