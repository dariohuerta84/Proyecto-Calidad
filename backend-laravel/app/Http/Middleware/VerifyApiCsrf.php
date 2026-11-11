<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;
use Illuminate\Support\Facades\Log; // <--- ¡AGREGA ESTO AQUÍ!

class VerifyApiCsrf
{
    /**
     * Double-submit CSRF:
     *  - El frontend recibe una cookie 'XSRF-TOKEN' (NO HttpOnly) desde /auth2/csrf
     *  - En cada petición que cambie estado, envía header 'X-XSRF-TOKEN' con el mismo valor
     *  - Aquí comparamos cookie vs header; si no coinciden => 403
     */
    public function handle(Request $request, Closure $next)
    {
        // Métodos que NO cambian estado ni deben validar CSRF
        $method = strtoupper($request->getMethod());
        if (in_array($method, ['GET','HEAD','OPTIONS'])) {
            return $next($request);
        }

        // Lee cookie y header
        $cookie = $request->cookie('XSRF-TOKEN');
        // Permitimos ambos headers por compatibilidad, priorizando X-XSRF-TOKEN
        $header = $request->header('X-XSRF-TOKEN') ?? $request->header('X-CSRF-TOKEN');

        // Validación básica
        if (!$cookie || !$header) {
            return response()->json([
                'message' => 'Invalid CSRF token (missing cookie or header).',
            ], Response::HTTP_FORBIDDEN);
        }

        // Comparación en tiempo constante para evitar timing attacks
        if (!hash_equals((string) $cookie, (string) $header)) {
            return response()->json([
                'message' => 'Invalid CSRF token (mismatch).',
            ], Response::HTTP_FORBIDDEN);
        }

        return $next($request);
    }
}
