<?php

header('Content-Type: application/json');

$status = 'OK';
$checks = [];

try {
    // Verificar conexión a base de datos
    $pdo = new PDO(
        "mysql:host=" . $_ENV['DB_HOST'] . ";port=" . $_ENV['DB_PORT'] . ";dbname=" . $_ENV['DB_DATABASE'],
        $_ENV['DB_USERNAME'],
        $_ENV['DB_PASSWORD']
    );
    $checks['database'] = 'connected';
} catch (Exception $e) {
    $status = 'ERROR';
    $checks['database'] = 'failed: ' . $e->getMessage();
}

// Verificar directorios de escritura
$writableDirs = ['storage/logs', 'storage/framework/cache', 'storage/framework/sessions'];
foreach ($writableDirs as $dir) {
    $fullPath = dirname(__DIR__) . '/' . $dir;
    if (is_writable($fullPath)) {
        $checks['writable_' . str_replace(['/', '\\'], '_', $dir)] = 'OK';
    } else {
        $status = 'ERROR';
        $checks['writable_' . str_replace(['/', '\\'], '_', $dir)] = 'NOT_WRITABLE';
    }
}

// Verificar variables de entorno críticas
$requiredEnvVars = ['APP_KEY', 'DB_HOST', 'DB_DATABASE', 'DB_USERNAME'];
foreach ($requiredEnvVars as $var) {
    if (isset($_ENV[$var]) && !empty($_ENV[$var])) {
        $checks['env_' . strtolower($var)] = 'set';
    } else {
        $status = 'ERROR';
        $checks['env_' . strtolower($var)] = 'missing';
    }
}

$response = [
    'status' => $status,
    'timestamp' => date('c'),
    'checks' => $checks
];

http_response_code($status === 'OK' ? 200 : 500);
echo json_encode($response, JSON_PRETTY_PRINT);
?>