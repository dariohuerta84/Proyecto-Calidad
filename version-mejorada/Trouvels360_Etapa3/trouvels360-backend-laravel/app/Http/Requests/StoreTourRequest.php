<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreTourRequest extends FormRequest
{
    public function authorize(): bool
    {
        return auth()->check() && auth()->user()->rol === 'proveedor';
    }

    public function rules(): array
    {
        return [
            // Servicio
            'nombre'      => ['required', 'string', 'max:150'],
            'descripcion' => ['required', 'string', 'max:5000'],
            'ciudad'      => ['required', 'string', 'max:100'],
            'pais'        => ['required', 'string', 'max:100'],
            'imagen_url'  => ['required', 'url', 'max:500'],
            'activo'      => ['boolean'],
            'latitud'     => ['nullable', 'numeric', 'between:-90,90'],
            'longitud'    => ['nullable', 'numeric', 'between:-180,180'],

            // Tour
            'categoria'           => ['required','in:Gastronomía,Aventura,Cultura,Relajación'],
            'duracion'            => ['required','integer','min:1','max:10080'],
            'precio'              => ['required','numeric','min:0'],

            // Galería (servicio_imagenes)
            'imagenes'       => ['sometimes','array','max:5'],
            'imagenes.*'     => ['nullable'],
            'imagenes.*.url' => ['sometimes','required','url','max:500'],
            'imagenes.*.alt' => ['sometimes','nullable','string','max:150'],

            // Items (cosas para llevar)
            'items' => ['sometimes','array','max:10'], // opcional y limitado a 10
            'items.*.nombre' => ['required_with:items','string','max:100'],
            'items.*.icono'  => ['nullable','string','max:50'], // puede ser emoji o ícono corto

            // Salidas (array de salidas)
            'salidas' => ['required', 'array', 'min:1', 'max:60'],
            'salidas.*.fecha' => ['required', 'date', 'after_or_equal:today'],
            'salidas.*.hora' => ['required', 'date_format:H:i'],
            'salidas.*.cupo_total' => ['required', 'integer', 'min:1'],
            'salidas.*.estado' => ['nullable', 'in:programada,cerrada,cancelada'],

        ];
    }

    public function messages(): array
    {
        return [
            'imagenes.max'    => 'Máximo 5 imágenes en la galería.',
            'items.max'       => 'Máximo 10 elementos en "qué llevar".',
            'categoria.in'    => 'La categoría debe ser Aventura, Gastronomía, Cultura o Relajación.',
            'salidas.required'=> 'Agrega al menos una salida.',
            'salidas.*.fecha.after_or_equal' => 'Las salidas deben tener fecha de hoy en adelante.',
        ];
    }

    /** Fecha + hora no se repiten dentro del mismo tour. */
    public function withValidator($validator): void
    {
        $validator->after(function ($v) {
            $vistos = [];
            foreach ((array) $this->input('salidas', []) as $i => $s) {
                $k = ($s['fecha'] ?? '') . ' ' . ($s['hora'] ?? '');
                if (isset($vistos[$k])) {
                    $v->errors()->add("salidas.$i.fecha", 'Hay dos salidas con la misma fecha y hora.');
                }
                $vistos[$k] = true;
            }
        });
    }
}
