<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Predeterminados de Autenticación
    |--------------------------------------------------------------------------
    |
    | Definimos el "guard" por defecto y el broker de reset de contraseñas.
    | Como ahora migramos a JWT, ponemos 'api' como guard por defecto.
    | Si tu app usa vistas con sesión (Blade), podrías dejar 'web'; para API puro: 'api'.
    |
    */

    'defaults' => [
        'guard' => env('AUTH_GUARD', 'api'),   // <-- AHORA JWT (api) es el guard por defecto
        'passwords' => env('AUTH_PASSWORD_BROKER', 'users'),
    ],

    /*
    |--------------------------------------------------------------------------
    | Guards de Autenticación
    |--------------------------------------------------------------------------
    |
    | Aquí definimos cada "guard". Mantenemos 'web' (sesiones) por si existiera
    | alguna vista, pero todo lo protegido por API usará 'api' con driver 'jwt'.
    |
    | Drivers soportados aquí: "session" (para web) y "jwt" (paquete tymon/jwt-auth).
    |
    */

    'guards' => [
        'web' => [
            'driver' => 'session',
            'provider' => 'users',
        ],

        // Guard principal para la API usando JWT
        'api' => [
            'driver' => 'jwt',
            'provider' => 'users',
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Providers de Usuarios
    |--------------------------------------------------------------------------
    |
    | Definen cómo se obtienen los usuarios. Usamos Eloquent con el modelo
    | App\Models\Usuario (tu modelo ya implementa JWTSubject).
    |
    */

    'providers' => [
        'users' => [
            'driver' => 'eloquent',
            'model' => env('AUTH_MODEL', App\Models\Usuario::class),
        ],

        // Alternativa con base de datos plana (no la usamos):
        // 'users' => [
        //     'driver' => 'database',
        //     'table' => 'users',
        // ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Reset de Contraseñas
    |--------------------------------------------------------------------------
    |
    | Config por defecto de reset (tokens, expiración, etc.). No cambia por JWT.
    |
    */

    'passwords' => [
        'users' => [
            'provider' => 'users',
            'table' => env('AUTH_PASSWORD_RESET_TOKEN_TABLE', 'password_reset_tokens'),
            'expire' => 60,   // minutos
            'throttle' => 60, // segundos
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Tiempo de espera para Confirmación de Password
    |--------------------------------------------------------------------------
    |
    | Cuántos segundos antes de volver a pedir la contraseña en pantallas
    | protegidas (útil si tienes secciones con reconfirmación).
    |
    */

    'password_timeout' => env('AUTH_PASSWORD_TIMEOUT', 10800),

];
