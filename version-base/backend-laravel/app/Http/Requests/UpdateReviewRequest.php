<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateReviewRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     * Solo el autor de la review puede actualizarla.
     */
    public function authorize(): bool
    {
        // Debe estar autenticado
        if (!auth()->check()) {
            return false;
        }

        // Obtener la review desde la ruta
        $review = $this->route('review');

        // Validar que el usuario autenticado sea el autor
        return $review && (int) $review->usuario_id === (int) auth()->id();
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array<string, \Illuminate\Contracts\Validation\ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'comentario'  => 'sometimes|required|string|max:300',
            'calificacion'=> 'sometimes|required|integer|min:1|max:5',
        ];
    }

    /**
     * Mensajes de error personalizados
     */
    public function messages(): array
    {
        return [
            'comentario.required' => 'El comentario es obligatorio.',
            'comentario.max' => 'El comentario no puede exceder los 300 caracteres.',
            'calificacion.required' => 'La calificación es obligatoria.',
            'calificacion.integer' => 'La calificación debe ser un número entero.',
            'calificacion.min' => 'La calificación mínima es 1.',
            'calificacion.max' => 'La calificación máxima es 5.',
        ];
    }

    /**
     * Mensajes de error de autorización
     */
    protected function failedAuthorization()
    {
        abort(403, 'No tienes permiso para actualizar esta reseña. Solo el autor puede modificarla.');
    }
}
