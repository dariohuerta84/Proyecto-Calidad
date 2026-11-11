<?php

namespace App\Providers;

use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        //
    }

    public function boot(): void
    {
        // TRV-103: máximo N intentos de login por minuto por correo + IP (429 al exceder).
        RateLimiter::for('login', function (Request $request) {
            $max = (int) env('LOGIN_RATE_LIMIT', 6);
            return Limit::perMinute($max)->by(mb_strtolower((string) $request->input('email')) . '|' . $request->ip());
        });
    }
}
