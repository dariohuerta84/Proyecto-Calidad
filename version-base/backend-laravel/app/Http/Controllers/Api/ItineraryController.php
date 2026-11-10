<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Validator;
use Firebase\JWT\JWT;
use Illuminate\Support\Facades\Log;

class ItineraryController extends Controller
{
    /**
     * Genera un itinerario enviando la petición al microservicio de Python.
     * Actúa como un API Gateway autenticado.
     */
    public function store(Request $request)
    {
        // 1. Validar los datos de entrada (Deben coincidir con el esquema de Python)
        $validator = Validator::make($request->all(), [
            'destino'        => 'required|string|min:2',
            'hotel_id'       => 'required|integer',
            'fecha_checkin'  => 'required|date',
            'fecha_checkout' => 'required|date|after:fecha_checkin',
            'interes'        => 'required|string|in:Aventura,Relajación,Cultura,Gastronomía',
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        // 2. Obtener el usuario autenticado (ya validado por el middleware jwt.auth)
        $user = auth('api')->user();

        // 3. Generar Token interno para el Microservicio (JWT_MS)
        // Este token le dice a Python: "La petición viene de Laravel y este es el usuario"
        $msToken = null;

        if ($user) {
            try {
                $now = time();
                $ttlMs = (int) env('JWT_MS_TTL', 20); // Tiempo de vida corto para seguridad interna
                
                $payload = [
                    'sub'   => (string) $user->id,
                    'email' => $user->email,
                    'rol'   => $user->rol ?? 'viajero',
                    
                    // Claims estándar requeridos por tu configuración de Python
                    'iss'   => config('app.url'),    // http://localhost:8000
                    'aud'   => 'fastapi-itinerarios', // Debe coincidir con JWT_AUDIENCE en Python
                    'iat'   => $now,
                    'exp'   => $now + ($ttlMs * 60),
                ];

                // Usamos el secreto compartido (JWT_MS_SECRET)
                $msSecret = env('JWT_MS_SECRET');
                
                if ($msSecret) {
                    $msToken = JWT::encode($payload, $msSecret, 'HS256');
                } else {
                    Log::warning('JWT_MS_SECRET no está configurado en el .env');
                }
            } catch (\Exception $e) {
                Log::error('Error generando token para microservicio: ' . $e->getMessage());
                // Continuamos sin token, Python lo tratará como usuario anónimo (sin guardar)
            }
        }

        // 4. Enviar la petición al Microservicio de Python
        // Usamos la URL interna de Docker configurada en el .env
        $pythonUrl = env('PYTHON_MICROSERVICE_URL'); 

        try {
            $http = Http::acceptJson();

            // Si logramos generar el token, lo adjuntamos
            if ($msToken) {
                $http->withToken($msToken);
            }

            // Hacemos el POST al endpoint de Python
            // La URL final será: http://trouvels360-itinerarios:8001/api/itinerary
            $response = $http->post("{$pythonUrl}/api/itinerary", $validator->validated());

            // 5. Retornar la respuesta de Python directamente al Frontend (Angular)
            // Esto incluye el JSON del itinerario y el código de estado (200, 400, etc.)
            return response()->json($response->json(), $response->status());

        } catch (\Exception $e) {
            Log::error("Error conectando con microservicio Python: " . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => 'El servicio de generación de itinerarios no está disponible en este momento.',
                'debug' => env('APP_DEBUG') ? $e->getMessage() : null
            ], 503);
        }
    }
}