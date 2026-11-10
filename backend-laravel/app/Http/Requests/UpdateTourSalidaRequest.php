<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateTourSalidaRequest extends FormRequest
{
    public function authorize(): bool { return true; }

    public function rules(): array
    {
        return [
            'fecha'      => ['sometimes','date','after_or_equal:today'],
            'hora'       => ['sometimes','nullable','date_format:H:i'],
            'cupo_total' => ['sometimes','integer','min:1'],
            'estado'     => ['sometimes','in:abierta,cerrada,finalizada'],
        ];
    }
}
