<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log; // <--- ¡AGREGA ESTO AQUÍ!

class AttachJwtFromCookie
{
    /**
     * Este middleware toma el token JWT de la cookie 'access_token'
     * y lo coloca en el encabezado Authorization como 'Bearer <token>'.
     * Así, el guard 'auth:api' puede autenticar correctamente.
     */
    public function handle(Request $request, Closure $next)
    {
        // --- DEBUG LOG ---
        $cookieToken = $request->cookie('access_token');
        Log::info('🕵️ Middleware Debug:', [
            'tiene_header_bearer' => $request->bearerToken() ? 'SI' : 'NO',
            'tiene_cookie' => $cookieToken ? 'SI (Longitud: ' . strlen($cookieToken) . ')' : 'NO',
            'ip_origen' => $request->ip()
        ]);
        // -----------------

        // Si la petición NO tiene un header Authorization válido...
        if (!$request->bearerToken()) {
            // ...buscamos la cookie 'access_token'
            $token = $request->cookie('access_token');
            if ($token) {
                $request->headers->set('Authorization', 'Bearer ' . $token);
            }
        }

        // Continuamos con la solicitud
        return $next($request);
    }
}
