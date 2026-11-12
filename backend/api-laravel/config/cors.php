<?php

/*
| Orígenes (la web) que pueden llamar a la API enviando cookies.
| Se configuran en .env con CORS_ALLOWED_ORIGINS, separados por coma y sin barra final:
|   CORS_ALLOWED_ORIGINS=http://localhost:4200,https://app.midominio.com
| No se puede usar '*' porque la sesión viaja en cookies (supports_credentials).
*/

return [

    'paths' => ['api/*'],

    'allowed_methods' => ['*'],

    'allowed_origins' => array_values(array_filter(array_map('trim',
        explode(',', (string) env('CORS_ALLOWED_ORIGINS', 'http://localhost:4200'))
    ))),

    'allowed_origins_patterns' => [],

    'allowed_headers' => ['*'],

    'exposed_headers' => [],

    'max_age' => 0,

    'supports_credentials' => true,

];
