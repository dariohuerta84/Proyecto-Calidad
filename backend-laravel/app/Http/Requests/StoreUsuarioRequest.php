<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreUsuarioRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    protected function prepareForValidation(): void
    {
        // Normaliza email si viene
        if ($this->has('email')) {
            $email = trim((string) $this->input('email'));
            $this->merge(['email' => $email === '' ? null : mb_strtolower($email)]);
        }

        // Normaliza rol a minúsculas
        if ($this->has('rol')) {
            $this->merge(['rol' => strtolower(trim((string) $this->input('rol')))]);
        }

        // Limpieza básica de proveedor
        if ($this->input('rol') === 'proveedor') {
            // RUC: sólo dígitos
            if ($this->has('ruc')) {
                $this->merge(['ruc' => preg_replace('/\D/', '', (string) $this->input('ruc'))]);
            }
            // Teléfono: se normaliza a "+51 9XXXXXXXX"
            if ($this->has('telefono')) {
                $this->merge(['telefono' => \App\Support\Telefono::normalizar((string) $this->input('telefono'))]);
            }
            if ($this->has('empresa_nombre')) {
                $this->merge(['empresa_nombre' => trim((string) $this->input('empresa_nombre'))]);
            }
        } else {
            // Viajero: limpia nombre/apellido
            if ($this->has('nombre')) {
                $this->merge(['nombre' => trim((string) $this->input('nombre'))]);
            }
            if ($this->has('apellido')) {
                $this->merge(['apellido' => trim((string) $this->input('apellido'))]);
            }
        }
    }

    public function rules(): array
    {
        $rol = $this->input('rol');

        // Reglas base comunes
        $base = [
            'rol'      => ['required', Rule::in(['viajero','proveedor'])],
            'email'    => ['required','email','max:150', Rule::unique('usuarios','email')],
            'password' => ['required','string','min:6','max:100','confirmed'],
        ];

        // Para viajero: nombre y apellido requeridos; campos de proveedor opcionales
        if ($rol === 'proveedor') {
            // Proveedor: empresa, teléfono (+51 9########), ruc (11 dígitos único)
            $proveedor = [
                'nombre'         => ['nullable','string','max:100'],
                'apellido'       => ['nullable','string','max:100'],

                'empresa_nombre' => ['required','string','max:150'],

                // Acepta exactamente: +51 (espacio opcional) + 9 + 8 dígitos
                'telefono'       => ['required','string','regex:/^\+51 9\d{8}$/'],

                'ruc'            => ['required','string','size:11','regex:/^\d{11}$/', Rule::unique('usuarios','ruc')],
            ];

            return array_merge($base, $proveedor);
        }

        // Viajero (rol por defecto si no es 'proveedor')
        $viajero = [
            'nombre'         => ['required','string','max:100'],
            'apellido'       => ['required','string','max:100'],

            'empresa_nombre' => ['nullable','string','max:150'],
            'telefono'       => ['nullable','string','max:15'],
            'ruc'            => ['nullable','string','size:11','regex:/^\d{11}$/', Rule::unique('usuarios','ruc')],
        ];

        return array_merge($base, $viajero);
    }

    public function messages(): array
    {
        return [
            'telefono.regex' => 'El teléfono debe tener el formato +51 9########.',
            'ruc.size'       => 'El RUC debe tener exactamente 11 dígitos.',
            'ruc.regex'      => 'El RUC solo debe contener dígitos.',
            'ruc.unique'     => 'El RUC ya está registrado.',
            'email.unique'   => 'El correo ya está registrado.',
            'password.confirmed' => 'Las contraseñas no coinciden.',
            'password.min'   => 'La contraseña debe tener al menos 6 caracteres.',
            'rol.in'         => 'El rol debe ser viajero o proveedor.',
        ];
    }
}
