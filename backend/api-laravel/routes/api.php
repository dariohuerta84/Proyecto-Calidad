<?php

use Illuminate\Support\Facades\Route;

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\UsuarioController;
use App\Http\Controllers\Api\ServicioController;
use App\Http\Controllers\Api\HotelController;
use App\Http\Controllers\Api\HabitacionController;
use App\Http\Controllers\Api\ReservaHabitacionController;
use App\Http\Controllers\Api\ProveedorReservaController;
use App\Http\Controllers\Api\TourController;
use App\Http\Controllers\Api\TourSalidaController;
use App\Http\Controllers\Api\TourActividadController;
use App\Http\Controllers\Api\ReservaTourController;
use App\Http\Controllers\Api\ReviewController;
use App\Http\Controllers\Api\ImageSearchController;
use App\Http\Controllers\Api\ItineraryController;

/*
|--------------------------------------------------------------------------
| API Trouvels360  (prefijo /api)
|--------------------------------------------------------------------------
| Autenticación: JWT en cookie HttpOnly 'access_token' (o header Authorization: Bearer).
| CSRF: toda petición que modifica datos exige X-XSRF-TOKEN = cookie XSRF-TOKEN (GET /api/auth/csrf).
| Las claves HU-xxx indican la historia de usuario que cubre cada ruta.
*/

Route::get('ping', fn () => response()->json(['pong' => true]));

// ------------------------------------------------------------------ AUTH (HU-V01, HU-P01, HU-V02, HU-P02)
Route::prefix('auth')->group(function () {
    Route::get('/csrf', [AuthController::class, 'csrf']);
    Route::post('/register', [AuthController::class, 'register'])->middleware(['csrf.api', 'throttle:registro']);
    Route::post('/login', [AuthController::class, 'login'])->middleware(['csrf.api', 'throttle:login']);
    Route::post('/refresh', [AuthController::class, 'refresh'])->middleware(['jwt.cookie', 'csrf.api']);

    Route::middleware(['jwt.cookie', 'jwt.auth'])->group(function () {
        Route::get('/me', [AuthController::class, 'me']);
        Route::post('/logout', [AuthController::class, 'logout'])->middleware('csrf.api');
    });
});

// ------------------------------------------------------------------ PÚBLICO (catálogo)
Route::apiResource('servicios', ServicioController::class)->only(['index', 'show']);

Route::get('hoteles', [HotelController::class, 'index']);                                                     // HU-V05
Route::get('hoteles/{servicio_id}', [HotelController::class, 'show'])->whereNumber('servicio_id');             // HU-V06
Route::get('hoteles/{servicio_id}/disponibilidad', [HotelController::class, 'disponibilidad'])->whereNumber('servicio_id');
Route::post('reservas-habitaciones/cotizar', [ReservaHabitacionController::class, 'cotizar']);               // HU-V06

Route::get('tours', [TourController::class, 'index']);                                                        // HU-V08
Route::get('tours/{tour}', [TourController::class, 'show'])->whereNumber('tour');                              // HU-V09
Route::get('tours/{tour}/salidas', [TourSalidaController::class, 'index'])->whereNumber('tour');
Route::get('tours/{tour}/actividades', [TourActividadController::class, 'index'])->whereNumber('tour');
Route::post('tours/salidas/{salida}/cotizar', [TourController::class, 'cotizar'])->whereNumber('salida');      // HU-V09

Route::get('reviews', [ReviewController::class, 'index']);                                                    // HU-V14
Route::get('reviews/resumen', [ReviewController::class, 'resumen']);

// ------------------------------------------------------------------ PRIVADO (requiere sesión)
Route::middleware(['jwt.cookie', 'jwt.auth'])->group(function () {

    // Perfil y cuenta (HU-V03 / HU-V04)
    Route::get('usuarios/me', [UsuarioController::class, 'me']);
    Route::match(['put', 'patch'], 'usuarios/me', [UsuarioController::class, 'updateMe'])->middleware('csrf.api');
    Route::put('usuarios/me/password', [UsuarioController::class, 'cambiarPassword'])->middleware('csrf.api');
    Route::delete('usuarios/me', [UsuarioController::class, 'destroyMe'])->middleware('csrf.api');

    // Viajero: reservas de hotel (HU-V07 / HU-V11 / HU-V13)
    Route::post('reservas-habitaciones', [ReservaHabitacionController::class, 'store'])->middleware(['csrf.api', 'idempotency']);
    Route::post('reservas-habitaciones/{reserva}/cancelar', [ReservaHabitacionController::class, 'cancelar'])->whereNumber('reserva')->middleware('csrf.api');
    Route::get('mis-reservas', [ReservaHabitacionController::class, 'misReservas']);
    Route::get('mis-reservas/{reserva}', [ReservaHabitacionController::class, 'show'])->whereNumber('reserva');

    // Viajero: reservas de tour (HU-V10 / HU-V11 / HU-V13)
    Route::post('tours/salidas/{salida}/reservas', [ReservaTourController::class, 'store'])->whereNumber('salida')->middleware(['csrf.api', 'idempotency']);
    Route::post('tours/reservas/{reserva}/cancelar', [ReservaTourController::class, 'cancelar'])->whereNumber('reserva')->middleware('csrf.api');
    Route::get('tours/mis-reservas', [ReservaTourController::class, 'misReservas']);

    // Reseñas (HU-V14)
    Route::get('reviews/puedo-opinar', [ReviewController::class, 'puedoOpinar']);
    Route::post('reviews', [ReviewController::class, 'store'])->middleware('csrf.api');
    Route::match(['put', 'patch'], 'reviews/{review}', [ReviewController::class, 'update'])->middleware('csrf.api');
    Route::delete('reviews/{review}', [ReviewController::class, 'destroy'])->middleware('csrf.api');

    // Itinerario sugerido vía microservicio Python (HU-V15)
    Route::post('itineraries', [ItineraryController::class, 'store'])->middleware(['csrf.api', 'throttle:20,1']);

    // ---------------- Proveedor
    Route::get('proveedor/servicios', [ServicioController::class, 'indexMine']);                                   // HU-P03
    Route::get('proveedor/servicios/{id}/reservas', [ServicioController::class, 'reservasPorServicio'])->whereNumber('id');
    Route::get('proveedor/indicadores', [ProveedorReservaController::class, 'indicadores']);                       // HU-P04
    Route::get('proveedor/reservas', [ProveedorReservaController::class, 'index']);                                // HU-P12
    Route::get('proveedor/reservas/hotel/{id}', [ProveedorReservaController::class, 'showHotel'])->whereNumber('id');
    Route::post('proveedor/reservas-habitaciones/{id}/aceptar', [ProveedorReservaController::class, 'aceptar'])->whereNumber('id')->middleware('csrf.api');   // HU-P13
    Route::post('proveedor/reservas-habitaciones/{id}/rechazar', [ProveedorReservaController::class, 'rechazar'])->whereNumber('id')->middleware('csrf.api'); // HU-P14

    Route::middleware('csrf.api')->group(function () {
        Route::apiResource('servicios', ServicioController::class)->only(['store', 'update', 'destroy']);         // HU-P15

        // Hoteles y habitaciones (HU-P05/HU-P06 / HU-P07)
        Route::post('hoteles', [HotelController::class, 'store']);
        Route::put('hoteles/{servicio_id}', [HotelController::class, 'update'])->whereNumber('servicio_id');
        Route::delete('hoteles/{servicio_id}', [HotelController::class, 'destroy'])->whereNumber('servicio_id');
        Route::post('hoteles/{servicio_id}/habitaciones', [HabitacionController::class, 'store'])->whereNumber('servicio_id');
        Route::put('habitaciones/{habitacion}', [HabitacionController::class, 'update'])->whereNumber('habitacion');
        Route::delete('habitaciones/{habitacion}', [HabitacionController::class, 'destroy'])->whereNumber('habitacion');

        // Tours, salidas y actividades (HU-P08/HU-P09 / HU-P10 / HU-P11)
        Route::post('tours', [TourController::class, 'store']);
        Route::match(['put', 'patch'], 'tours/{tour}', [TourController::class, 'update'])->whereNumber('tour');
        Route::delete('tours/{tour}', [TourController::class, 'destroy'])->whereNumber('tour');
        Route::post('tours/{tour}/salidas', [TourSalidaController::class, 'store'])->whereNumber('tour');
        Route::match(['put', 'patch'], 'tours/{tour}/salidas/{salida}', [TourSalidaController::class, 'update'])->whereNumber(['tour', 'salida']);
        Route::delete('tours/{tour}/salidas/{salida}', [TourSalidaController::class, 'destroy'])->whereNumber(['tour', 'salida']);
        Route::post('tours/{tour}/actividades', [TourActividadController::class, 'store'])->whereNumber('tour');
        Route::match(['put', 'patch'], 'tours/{tour}/actividades/{actividad}', [TourActividadController::class, 'update'])->whereNumber(['tour', 'actividad']);
        Route::delete('tours/{tour}/actividades/{actividad}', [TourActividadController::class, 'destroy'])->whereNumber(['tour', 'actividad']);
    });

    // Búsqueda de imágenes (Unsplash) para el formulario del proveedor
    Route::get('images/search', [ImageSearchController::class, 'search'])->name('images.search');
});
