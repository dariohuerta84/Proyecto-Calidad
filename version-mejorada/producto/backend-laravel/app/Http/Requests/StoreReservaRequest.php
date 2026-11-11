<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreReservaRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array<string, \Illuminate\Contracts\Validation\ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'usuario_id' => 'required|exists:usuarios,id',
            'servicio_id' => 'required|exists:servicios,id',
            'fecha_inicio' => 'required|date|after_or_equal:today',
            'fecha_fin' => 'required|date|after:fecha_inicio',
            'huespedes' => 'required|integer|min:1|max:20',
        ];
    }

    /**
     * Get custom error messages for validation rules.
     *
     * @return array<string, string>
     */
    public function messages(): array
    {
        return [
            'usuario_id.required' => 'El ID del usuario es obligatorio.',
            'usuario_id.exists' => 'El usuario especificado no existe.',
            'servicio_id.required' => 'El ID del servicio es obligatorio.',
            'servicio_id.exists' => 'El servicio especificado no existe.',
            'fecha_inicio.required' => 'La fecha de inicio es obligatoria.',
            'fecha_inicio.date' => 'La fecha de inicio debe ser una fecha válida.',
            'fecha_inicio.after_or_equal' => 'La fecha de inicio no puede ser anterior a hoy.',
            'fecha_fin.required' => 'La fecha de fin es obligatoria.',
            'fecha_fin.date' => 'La fecha de fin debe ser una fecha válida.',
            'fecha_fin.after' => 'La fecha de fin debe ser posterior a la fecha de inicio.',
            'huespedes.required' => 'El número de huéspedes es obligatorio.',
            'huespedes.integer' => 'El número de huéspedes debe ser un número entero.',
            'huespedes.min' => 'Debe haber al menos 1 huésped.',
            'huespedes.max' => 'No pueden ser más de 20 huéspedes.',
        ];
    }
}