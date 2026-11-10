<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateTourActividadRequest extends FormRequest
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
            'titulo'       => ['sometimes','string','max:150'],
            'descripcion'  => ['sometimes','nullable','string'],
            'orden'        => ['sometimes','integer','min:1'],
            'duracion_min' => ['sometimes','integer','min:1'],
            'direccion'    => ['sometimes','string','max:255'],
            'imagen_url'   => ['sometimes','nullable','url','max:500'],
        ];
    }
}
