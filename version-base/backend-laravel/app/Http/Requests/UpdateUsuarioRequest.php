<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateUsuarioRequest extends FormRequest
{
    public function authorize(): bool
    {
        return $this->user() !== null;
    }

    protected function prepareForValidation(): void
    {
        $data = $this->all();

        // 🔧 Soporta /usuarios/{usuario} y /usuarios/me
        $usuario = $this->route('usuario') ?? $this->user();
        $currentRole = $usuario?->rol;

        // Normaliza y elimina strings vacíos
        $normalize = function($key, $val) {
            if (is_string($val)) $val = trim($val);
            if ($val === '' || $val === null) return [false, null];

            if ($key === 'email')      return [true, mb_strtolower($val)];
            if ($key === 'ruc')        return [true, preg_replace('/\D/', '', (string)$val)];
            if ($key === 'telefono')   return [true, $val];
            if (in_array($key, ['empresa_nombre','nombre','apellido'], true)) return [true, $val];

            return [true, $val];
        };

        foreach (array_keys($data) as $k) {
            if ($k === 'rol') { // Nunca permitir cambiar rol
                unset($data[$k]);
                continue;
            }
            [$keep, $v] = $normalize($k, $data[$k]);
            if (!$keep) {
                unset($data[$k]);
            } else {
                $data[$k] = $v;
            }
        }

        // Si es viajero, ignora silenciosamente campos de proveedor
        if ($currentRole === 'viajero') {
            unset($data['empresa_nombre'], $data['telefono'], $data['ruc']);
        }

        $this->replace($data);
    }

    public function rules(): array
    {
        // 🔧 Soporta /usuarios/{usuario} y /usuarios/me
        $usuario = $this->route('usuario') ?? $this->user();
        $id      = $usuario?->id;
        $role    = $usuario?->rol;

        $rules = [
            'rol'       => ['prohibited'],

            'nombre'    => ['sometimes','string','max:100'],
            'apellido'  => ['sometimes','string','max:100'],
            'email'     => ['sometimes','email','max:150', Rule::unique('usuarios','email')->ignore($id)],
            'password'  => ['sometimes','string','min:6'],
        ];

        if ($role === 'proveedor') {
            $rules = array_merge($rules, [
                'empresa_nombre' => ['sometimes','string','max:150'],
                // mismo patrón que usas en el front: +51 9########
                'telefono'       => ['sometimes','string','regex:/^\+51\s?9\d{8}$/','max:15'],
                'ruc'            => ['sometimes','string','size:11','regex:/^\d{11}$/', Rule::unique('usuarios','ruc')->ignore($id)],
            ]);
        }

        return $rules;
    }

    public function messages(): array
    {
        return [
            'rol.prohibited'  => 'No está permitido cambiar el rol del usuario.',
            'telefono.regex'  => 'El teléfono debe tener el formato +51 9########.',
            'ruc.size'        => 'El RUC debe tener exactamente 11 dígitos.',
            'ruc.regex'       => 'El RUC solo debe contener dígitos.',
            'ruc.unique'      => 'El RUC ya está registrado.',
        ];
    }
}
