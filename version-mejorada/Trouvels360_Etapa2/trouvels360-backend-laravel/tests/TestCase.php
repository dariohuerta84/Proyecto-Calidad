<?php

namespace Tests;

use App\Models\Usuario;
use Illuminate\Foundation\Testing\TestCase as BaseTestCase;
use Illuminate\Testing\TestResponse;

abstract class TestCase extends BaseTestCase
{
    protected ?string $token = null;

    /** Autentica como el usuario dado usando un JWT real (header Bearer). */
    public function como(Usuario|string $usuario): static
    {
        $u = is_string($usuario) ? Usuario::where('email', $usuario)->firstOrFail() : $usuario;
        app('auth')->forgetGuards();
        $this->token = auth('api')->login($u);
        app('auth')->forgetGuards();

        return $this;
    }

    public function anonimo(): static
    {
        $this->token = null;
        app('auth')->forgetGuards();

        return $this;
    }

    /** Petición JSON con CSRF de doble envío válido y el token actual. */
    public function api(string $method, string $uri, array $data = [], array $headers = []): TestResponse
    {
        app('auth')->forgetGuards();
        $h = ['Accept' => 'application/json', 'X-XSRF-TOKEN' => 'csrf-test'] + $headers;
        if ($this->token) {
            $h['Authorization'] = 'Bearer ' . $this->token;
        }

        return $this->withCredentials()->withUnencryptedCookie('XSRF-TOKEN', 'csrf-test')->json($method, "/api/$uri", $data, $h);
    }

    public function pagoValido(): array
    {
        return ['titular' => 'Ana Torres', 'numero' => '4242 4242 4242 4242', 'vencimiento' => now()->addYear()->format('m/y'), 'cvc' => '123'];
    }

    public function idem(): array
    {
        return ['Idempotency-Key' => (string) \Illuminate\Support\Str::uuid()];
    }
}
