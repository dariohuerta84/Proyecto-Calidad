<?php

namespace App\Http\Middleware;

use App\Models\IdempotencyKey;
use Closure;
use Illuminate\Database\QueryException;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * Reintentos seguros para creación de reservas (TRV-203 / TRV-303).
 *
 * - Exige el header Idempotency-Key (8–100 caracteres).
 * - Primer envío: se registra la clave, se procesa y se guarda la respuesta exitosa.
 * - Reenvío con la misma clave y el mismo cuerpo: devuelve la respuesta guardada con 200
 *   y el header Idempotent-Replayed: true, sin volver a ejecutar la operación.
 * - Misma clave con otro cuerpo: 422. Misma clave aún en proceso: 409.
 * - Si la operación falla (4xx/5xx) la clave se libera para poder reintentar.
 */
class Idempotency
{
    public function handle(Request $request, Closure $next): Response
    {
        $clave = (string) $request->header('Idempotency-Key', '');
        if (strlen($clave) < 8 || strlen($clave) > 100) {
            return response()->json([
                'message' => 'El header Idempotency-Key es obligatorio (8 a 100 caracteres).',
            ], 400);
        }

        $usuarioId = $request->user()?->id;
        $huella = hash('sha256', $request->method() . '|' . $request->path() . '|' . json_encode($request->all()));

        $existente = IdempotencyKey::where('usuario_id', $usuarioId)->where('clave', $clave)->first();
        if ($existente) {
            return $this->repetido($existente, $huella);
        }

        try {
            $registro = IdempotencyKey::create([
                'usuario_id' => $usuarioId,
                'clave'      => $clave,
                'ruta'       => substr($request->path(), 0, 150),
                'huella'     => $huella,
            ]);
        } catch (QueryException $e) {
            // Otra petición con la misma clave ganó la carrera.
            $existente = IdempotencyKey::where('usuario_id', $usuarioId)->where('clave', $clave)->first();
            return $existente ? $this->repetido($existente, $huella) : $this->enProceso();
        }

        try {
            $response = $next($request);
        } catch (\Throwable $e) {
            $registro->delete();
            throw $e;
        }

        if ($response->isSuccessful()) {
            $registro->update(['status' => $response->getStatusCode(), 'respuesta' => $response->getContent()]);
        } else {
            $registro->delete(); // permite reintentar tras un error
        }

        return $response;
    }

    private function repetido(IdempotencyKey $registro, string $huella): Response
    {
        if ($registro->huella !== $huella) {
            return response()->json([
                'message' => 'La Idempotency-Key ya se usó con otros datos. Genera una nueva para otra operación.',
            ], 422);
        }
        if ($registro->respuesta === null) {
            return $this->enProceso();
        }

        return response($registro->respuesta, 200)
            ->header('Content-Type', 'application/json')
            ->header('Idempotent-Replayed', 'true');
    }

    private function enProceso(): Response
    {
        return response()->json(['message' => 'Una solicitud con esta Idempotency-Key se está procesando.'], 409);
    }
}
