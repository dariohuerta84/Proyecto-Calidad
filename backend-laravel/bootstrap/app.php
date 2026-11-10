<?php

use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__ . '/../routes/web.php',
        api: __DIR__ . '/../routes/api.php',
        commands: __DIR__ . '/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware) {
        /*
        |--------------------------------------------------------------------------
        | Alias de Middlewares (API)
        |--------------------------------------------------------------------------
        | - jwt.cookie  : Toma la cookie 'access_token' y la inyecta como
        |                 Authorization: Bearer <token> para que el guard JWT
        |                 pueda autenticar.
        | - csrf.api    : Double-submit CSRF (compara cookie XSRF-TOKEN vs header
        |                 X-XSRF-TOKEN) solo para métodos que mutan estado.
        | - jwt.auth    : Middleware oficial de tymon/jwt-auth que valida el token
        |                 y autentica al usuario (debe ir DESPUÉS de jwt.cookie).
        | - jwt.refresh : (Opcional) Middleware del paquete para refrescar token
        |                 automáticamente; normalmente no lo usamos en API REST,
        |                 preferimos un endpoint /auth/refresh explícito.
        */

        $middleware->alias([
            'jwt.cookie'  => \App\Http\Middleware\AttachJwtFromCookie::class,
            'csrf.api'    => \App\Http\Middleware\VerifyApiCsrf::class,

            // Middlewares del paquete tymon/jwt-auth
            'jwt.auth'    => \Tymon\JWTAuth\Http\Middleware\Authenticate::class,
            'jwt.refresh' => \Tymon\JWTAuth\Http\Middleware\RefreshToken::class,
        ]);

        // Nota: No añadimos EncryptCookies al stack API para NO cifrar 'access_token'.
        // Si lo tuvieras en algún momento, asegúrate de excluir 'access_token' y 'XSRF-TOKEN'.
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        // Personalización global de excepciones (opcional)
    })
    ->create();
