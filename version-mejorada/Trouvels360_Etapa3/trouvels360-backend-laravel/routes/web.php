<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\HomeController;

// Página pública HTML (landing). Usa el controlador invocable.
Route::get('/', HomeController::class);

// (Opcional) Fallback para rutas web no encontradas
Route::fallback(function () {
    return response()->view('errors.404', [], 404);
});
