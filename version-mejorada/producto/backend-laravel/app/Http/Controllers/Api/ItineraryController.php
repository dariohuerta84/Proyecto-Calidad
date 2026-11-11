<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Firebase\JWT\JWT;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

/**
 * TRV-404 — API Gateway hacia el microservicio Python (FastAPI) de itinerarios.
 * Laravel valida, firma un JWT de servicio (HS256, aud=fastapi-itinerarios, 5 min) y reenvía la petición.
 */
class ItineraryController extends Controller
{
    public const INTERESES = ['Aventura', 'Relajación', 'Cultura', 'Gastronomía'];

    public function store(Request $request): JsonResponse
    {
        $data = $request->validate([
            'destino'        => ['required', 'string', 'min:2', 'max:100'],
            'hotel_id'       => ['required', 'integer', 'min:1'],
            'fecha_checkin'  => ['required', 'date_format:Y-m-d', 'after_or_equal:today'],
            'fecha_checkout' => ['required', 'date_format:Y-m-d', 'after:fecha_checkin'],
            'interes'        => ['required', 'string', 'in:' . implode(',', self::INTERESES)],
        ], [
            'interes.in' => 'El interés debe ser Aventura, Relajación, Cultura o Gastronomía.',
            'fecha_checkin.after_or_equal' => 'La fecha de entrada no puede ser anterior a hoy.',
        ]);

        $dias = (new \DateTime($data['fecha_checkin']))->diff(new \DateTime($data['fecha_checkout']))->days;
        if ($dias > 14) {
            return response()->json([
                'message' => 'El itinerario puede cubrir como máximo 14 días.',
                'errors'  => ['fecha_checkout' => ['Máximo 14 días.']],
            ], 422);
        }

        $url = rtrim((string) config('services.itinerarios.url'), '/');
        $secret = (string) config('services.itinerarios.secret');
        if (!$url || !$secret) {
            Log::warning('Microservicio de itinerarios sin configurar (PYTHON_MICROSERVICE_URL / JWT_MS_SECRET).');
            return $this->noDisponible();
        }

        $user = $request->user();
        $now = time();
        $token = JWT::encode([
            'sub'   => (string) $user->id,
            'email' => $user->email,
            'rol'   => $user->rol,
            'iss'   => config('services.itinerarios.issuer'),
            'aud'   => 'fastapi-itinerarios',
            'iat'   => $now,
            'exp'   => $now + 300,
        ], $secret, 'HS256');

        try {
            $response = Http::acceptJson()->withToken($token)->timeout(10)->connectTimeout(3)
                ->post("{$url}/api/itinerary", $data);
        } catch (\Throwable $e) {
            Log::error('Microservicio de itinerarios no responde: ' . $e->getMessage());
            return $this->noDisponible();
        }

        if ($response->successful()) {
            $body = $response->json();
            $body['sugerencia'] = true; // se rotula como sugerencia: nada se reserva
            return response()->json($body, 200);
        }

        // Errores de negocio del microservicio (404 sin actividades, 400 validación) se trasladan con su mensaje
        $detalle = $response->json('detail') ?? $response->json();
        $mensaje = is_array($detalle) ? ($detalle['error'] ?? $detalle['message'] ?? 'No pudimos generar el itinerario.') : (string) $detalle;
        $status = in_array($response->status(), [400, 404, 422], true) ? $response->status() : 503;
        if ($response->status() === 401) {
            Log::error('El microservicio rechazó el JWT de servicio: revisa JWT_MS_SECRET / JWT_ISSUER.');
            return $this->noDisponible();
        }

        return response()->json(['success' => false, 'message' => $mensaje, 'error' => $mensaje], $status);
    }

    private function noDisponible(): JsonResponse
    {
        return response()->json([
            'success' => false,
            'message' => 'No pudimos generar el itinerario en este momento. Puedes reintentar o continuar con tu reserva.',
            'error'   => 'El servicio de generación de itinerarios no está disponible en este momento.',
        ], 503);
    }
}
