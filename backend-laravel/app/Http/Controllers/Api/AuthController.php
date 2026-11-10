<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreUsuarioRequest;
use App\Models\Usuario;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Illuminate\Support\Facades\Log; // <--- AGREGA ESTA LÍNEA

class AuthController extends Controller
{
    /**
     * Devuelve un token CSRF y setea cookie 'XSRF-TOKEN' (NO HttpOnly).
     * El frontend debe enviar ese valor en el header 'X-XSRF-TOKEN'
     * en toda petición que cambie estado (POST/PUT/PATCH/DELETE).
     */
    public function csrf()
    {
        $token = bin2hex(random_bytes(32));

        return response()->json(['csrf_token' => $token])->cookie(
            'XSRF-TOKEN',                // name
            $token,                      // value
            60,                          // minutes
            '/',                         // path
            $this->cookieDomain(),       // domain
            $this->cookieSecure(),       // secure
            false,                       // httpOnly (debe ser legible por JS)
            false,                       // raw
            $this->cookieSameSite()      // sameSite
        );
    }

    /**
     * Registro de usuario (opcional: auto-login emitiendo cookie HttpOnly).
     * Requiere CSRF en la ruta.
     */
    public function register(StoreUsuarioRequest $request)
    {
        $data = $request->validated();

        // Limpiar campos según rol (una sola tabla)
        if (($data['rol'] ?? null) === 'viajero') {
            $data['empresa_nombre'] = null;
            $data['telefono']       = null;
            $data['ruc']            = null;
        } else { // proveedor
            $data['nombre']   = null;
            $data['apellido'] = null;
        }

        // El modelo 'Usuario' tiene cast 'password' => 'hashed'
        $user = Usuario::create($data);

        // Auto-login inmediato (si no deseas, comenta estas 6 líneas)
        $token = auth('api')->login($user);
        $ttl   = auth('api')->factory()->getTTL();

        $cookie = $this->makeAccessCookie($token, $ttl);

        return response()->json([
            'message'    => 'Registro exitoso.',
            'expires_in' => $ttl * 60,
            'data'       => [
                'user' => [
                    'id'             => $user->id,
                    'rol'            => $user->rol,
                    'nombre'         => $user->nombre,
                    'apellido'       => $user->apellido,
                    'empresa_nombre' => $user->empresa_nombre,
                    'telefono'       => $user->telefono,
                    'ruc'            => $user->ruc,
                    'email'          => $user->email,
                    'created_at'     => $user->created_at,
                ],
            ],
        ])->withCookie($cookie);
    }

    /**
     * Login: valida credenciales, genera JWT y setea cookie HttpOnly 'access_token'.
     * Requiere CSRF (agregado por el middleware 'csrf.api' en la ruta).
     */
    public function login(Request $request)
    {
        $validator = Validator::make($request->only('email', 'password'), [
            'email'    => ['required','email'],
            'password' => ['required','string'],
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Datos inválidos',
                'errors'  => $validator->errors(),
            ], 422);
        }

        // Intenta autenticar con el guard 'api' (driver jwt)
        if (!$token = auth('api')->attempt($validator->validated())) {
            return response()->json(['message' => 'Credenciales inválidas'], 401);
        }

        $ttl = auth('api')->factory()->getTTL();
        $cookie = $this->makeAccessCookie($token, $ttl);

        return response()->json([
            'message'    => 'Login OK',
            'expires_in' => $ttl * 60, // segundos
            'data'       => [
                'user' => auth('api')->user(),
            ],
        ])->withCookie($cookie);
    }

    /**
     * Usuario autenticado (requiere jwt.cookie + jwt.auth).
     */
    public function me()
    {
        return response()->json(['data' => auth('api')->user()]);
    }

    /**
     * Refresca el JWT y setea nueva cookie HttpOnly 'access_token'.
     * Requiere CSRF.
     */
    public function refresh()
    {
        $newToken = auth('api')->refresh();
        $ttl      = auth('api')->factory()->getTTL();

        $cookie = $this->makeAccessCookie($newToken, $ttl);

        return response()->json([
            'message'    => 'Token refrescado',
            'expires_in' => $ttl * 60,
        ])->withCookie($cookie);
    }

    /**
     * Invalida el token actual (blacklist) y borra la cookie 'access_token'.
     * Requiere CSRF.
     */
    public function logout()
    {
        try {
            auth('api')->invalidate(true); // blacklist si está habilitada
        } catch (\Throwable $e) {
            // si ya expiró/no válido, igual borramos cookie
        }

        $forget = cookie(
            'access_token', '', -1, '/', $this->cookieDomain(),
            $this->cookieSecure(), true, false, $this->cookieSameSite()
        );

        return response()->json(['message' => 'Logout OK'])->withCookie($forget);
    }

    // ===================== Helpers de cookies =====================

    private function makeAccessCookie(string $token, int $ttlMinutes)
    {
        return cookie(
            'access_token',               // name
            $token,                       // value
            $ttlMinutes,                  // minutes
            '/',                          // path
            $this->cookieDomain(),        // domain
            $this->cookieSecure(),        // secure (true en prod/https)
            true,                         // httpOnly
            false,                        // raw
            $this->cookieSameSite()       // sameSite
        );
    }

    private function cookieSameSite(): string
    {
        return env('COOKIE_SAMESITE', 'Strict'); // 'Strict' | 'Lax' | 'None'
    }

    private function cookieDomain(): ?string
    {
        $domain = env('COOKIE_DOMAIN');
        return $domain !== null && $domain !== '' ? $domain : null;
    }

    private function cookieSecure(): bool
    {
        return filter_var(env('COOKIE_SECURE', app()->environment('production')), FILTER_VALIDATE_BOOL);
    }
        
}
