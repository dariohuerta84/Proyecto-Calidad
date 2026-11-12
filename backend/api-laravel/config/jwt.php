<?php

/*
 * This file is part of jwt-auth.
 * (c) Sean Tymon <tymon148@gmail.com>
 */

return [

    /*
    |--------------------------------------------------------------------------
    | Clave secreta (HMAC)
    |--------------------------------------------------------------------------
    | Define JWT_SECRET en .env (php artisan jwt:secret).
    */
    'secret' => env('JWT_SECRET'),

    /*
    |--------------------------------------------------------------------------
    | Claves para algoritmos asimétricos (si los usaras)
    |--------------------------------------------------------------------------
    */
    'keys' => [
        'public'     => env('JWT_PUBLIC_KEY'),
        'private'    => env('JWT_PRIVATE_KEY'),
        'passphrase' => env('JWT_PASSPHRASE'),
    ],

    /*
    |--------------------------------------------------------------------------
    | TTL del token (minutos)
    |--------------------------------------------------------------------------
    | Tiempo de vida del access token. Recomendado: corto (ej. 20–30 min).
    */
    'ttl' => (int) env('JWT_TTL', 60),

    /*
    |--------------------------------------------------------------------------
    | TTL de refresco (minutos)
    |--------------------------------------------------------------------------
    | Ventana en la que se permite refrescar un token expirado (p. ej. 14 días).
    */
    'refresh_ttl' => (int) env('JWT_REFRESH_TTL', 20160),

    /*
    |--------------------------------------------------------------------------
    | Algoritmo de firmado
    |--------------------------------------------------------------------------
    */
    'algo' => env('JWT_ALGO', Tymon\JWTAuth\Providers\JWT\Provider::ALGO_HS256),

    /*
    |--------------------------------------------------------------------------
    | Claims requeridos
    |--------------------------------------------------------------------------
    */
    'required_claims' => ['iss','iat','exp','nbf','sub','jti'],

    /*
    |--------------------------------------------------------------------------
    | Claims persistentes al refrescar
    |--------------------------------------------------------------------------
    */
    'persistent_claims' => [
        // Ej.: 'rol', 'email' si los añades en getJWTCustomClaims()
    ],

    /*
    |--------------------------------------------------------------------------
    | Lock Subject
    |--------------------------------------------------------------------------
    | Añade 'prv' para evitar colisiones entre múltiples modelos de usuario.
    */
    'lock_subject' => true,

    /*
    |--------------------------------------------------------------------------
    | Leeway (segundos)
    |--------------------------------------------------------------------------
    | Margen para desfases de reloj en iat/nbf/exp.
    */
    'leeway' => (int) env('JWT_LEEWAY', 0),

    /*
   |--------------------------------------------------------------------------
    | Blacklist
    |--------------------------------------------------------------------------
    | Habilitar blacklist para invalidar tokens (logout).
    */
    'blacklist_enabled' => (bool) env('JWT_BLACKLIST_ENABLED', true),

    /*
    |--------------------------------------------------------------------------
    | Blacklist Grace Period (segundos)
    |--------------------------------------------------------------------------
    | Evita fallas en requests paralelas al regenerar tokens.
    */
    'blacklist_grace_period' => (int) env('JWT_BLACKLIST_GRACE_PERIOD', 0),

    /*
    |--------------------------------------------------------------------------
    | Cookies encryption
    |--------------------------------------------------------------------------
    | Mantenlo en false: no necesitamos que el paquete descifre cookies.
    | (Estamos gestionando la cookie 'access_token' sin cifrar en API.)
    */
    'decrypt_cookies' => false,

    /*
    |--------------------------------------------------------------------------
    | Providers
    |--------------------------------------------------------------------------
    */
    'providers' => [
        'jwt'     => Tymon\JWTAuth\Providers\JWT\Lcobucci::class,
        'auth'    => Tymon\JWTAuth\Providers\Auth\Illuminate::class,
        'storage' => Tymon\JWTAuth\Providers\Storage\Illuminate::class,
    ],

];
