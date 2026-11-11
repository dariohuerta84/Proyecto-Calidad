<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

/** La regla de "reserva finalizada" se verifica en el controlador para devolver la causa exacta. */
class StoreReviewRequest extends FormRequest
{
    public function authorize(): bool
    {
        return auth()->check();
    }

    public function rules(): array
    {
        return [
            'servicio_id'  => ['required', 'integer', 'exists:servicios,id'],
            'comentario'   => ['required', 'string', 'min:10', 'max:300'],
            'calificacion' => ['required', 'integer', 'min:1', 'max:5'],
        ];
    }

    public function messages(): array
    {
        return [
            'comentario.min' => 'El comentario debe tener al menos 10 caracteres.',
            'comentario.max' => 'El comentario no puede superar los 300 caracteres.',
            'calificacion.min' => 'La calificación va de 1 a 5.',
            'calificacion.max' => 'La calificación va de 1 a 5.',
        ];
    }
}
