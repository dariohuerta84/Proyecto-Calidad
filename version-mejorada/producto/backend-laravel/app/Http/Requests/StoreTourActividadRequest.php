<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreTourActividadRequest extends FormRequest
{
    public function authorize(): bool { return true; }

    public function prepareForValidation(): void
    {
        if ($this->has('titulo') && is_string($this->titulo)) {
            $this->merge(['titulo' => trim($this->titulo)]);
        }
    }

    public function rules(): array
    {
        return [
            'titulo'       => ['required','string','max:150'],
            'descripcion'  => ['nullable','string'],
            'orden'        => ['nullable','integer','min:1'],
            'duracion_min' => ['nullable','integer','min:1'],
            'direccion'    => ['nullable','string','max:255'],
            'imagen_url'   => ['nullable','url','max:500'],
        ];
    }
}
