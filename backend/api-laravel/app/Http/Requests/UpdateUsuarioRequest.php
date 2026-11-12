<?php

namespace App\Http\Requests;

use App\Support\Telefono;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

/** HU-V03 — PATCH /api/usuarios/me. El rol y la contraseña no se cambian aquí. */
class UpdateUsuarioRequest extends FormRequest
{
    public function authorize(): bool
    {
        return (bool) $this->user();
    }

    protected function prepareForValidation(): void
    {
        if ($this->has('telefono') && $this->input('telefono') !== null) {
            $this->merge(['telefono' => Telefono::normalizar((string) $this->input('telefono'))]);
        }
        foreach (['nombre', 'apellido', 'empresa_nombre'] as $c) {
            if ($this->has($c) && is_string($this->input($c))) {
                $this->merge([$c => trim($this->input($c))]);
            }
        }
    }

    public function rules(): array
    {
        $usuario = $this->user();
        $rules = [
            'rol'        => ['prohibited'],
            'password'   => ['prohibited'],
            'email'      => ['sometimes', 'email', 'max:150', Rule::unique('usuarios', 'email')->ignore($usuario?->id)],
            'avatar_url' => ['sometimes', 'nullable', 'url:http,https', 'max:500'],
        ];

        if ($usuario?->rol === 'proveedor') {
            return $rules + [
                'empresa_nombre' => ['sometimes', 'string', 'min:2', 'max:150'],
                'telefono'       => ['sometimes', 'string', 'regex:' . Telefono::REGEX],
                'nombre'         => ['prohibited'],
                'apellido'       => ['prohibited'],
                'ruc'            => ['prohibited'],
            ];
        }

        return $rules + [
            'nombre'   => ['sometimes', 'string', 'min:1', 'max:100'],
            'apellido' => ['sometimes', 'string', 'min:1', 'max:100'],
        ];
    }

    public function messages(): array
    {
        return [
            'rol.prohibited'      => 'El rol no se puede modificar.',
            'password.prohibited' => 'Usa PUT /api/usuarios/me/password para cambiar la contraseña.',
            'ruc.prohibited'      => 'El RUC no se puede modificar.',
            'telefono.regex'      => Telefono::MENSAJE,
            'avatar_url.url'      => 'La imagen de perfil debe ser una URL http(s) válida.',
        ];
    }
}
