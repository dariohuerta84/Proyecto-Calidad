<?php

return [

    'paths' => ['api/*', 'sanctum/csrf-cookie'],

    'allowed_methods' => ['*'],

    // ❌ ERROR: 'allowed_origins' => ['*'],
    // ✅ CORRECTO: Pon la URL exacta de tu Angular (sin barra al final)
    'allowed_origins' => ['http://localhost:4200'],

    'allowed_origins_patterns' => [],

    'allowed_headers' => ['*'],

    'exposed_headers' => [],

    'max_age' => 0,

    // ✅ ESTO ES OBLIGATORIO EN TRUE
    'supports_credentials' => true,

];