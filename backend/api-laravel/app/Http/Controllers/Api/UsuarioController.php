<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\UpdateUsuarioRequest;
use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\Servicio;
use App\Services\EliminacionServicio;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

/** HU-V03 (perfil y contraseña) y HU-V04 (eliminar cuenta). Siempre sobre la cuenta propia (/me). */
class UsuarioController extends Controller
{
    /** GET /api/usuarios/me */
    public function me(Request $request): JsonResponse
    {
        return response()->json(['data' => $request->user()->perfil()]);
    }

    /** PATCH|PUT /api/usuarios/me */
    public function updateMe(UpdateUsuarioRequest $request): JsonResponse
    {
        $usuario = $request->user();
        $data = $request->validated();

        if (empty($data)) {
            return response()->json(['message' => 'No se recibieron campos para actualizar.'], 422);
        }

        $usuario->update($data);

        return response()->json([
            'message' => 'Perfil actualizado correctamente.',
            'data'    => $usuario->refresh()->perfil(),
        ]);
    }

    /** PUT /api/usuarios/me/password {password_actual, password, password_confirmation} */
    public function cambiarPassword(Request $request): JsonResponse
    {
        $data = $request->validate([
            'password_actual' => ['required', 'string'],
            'password'        => ['required', 'string', 'min:6', 'max:100', 'confirmed', 'different:password_actual'],
        ], [
            'password.confirmed' => 'Las contraseñas no coinciden.',
            'password.min'       => 'La nueva contraseña debe tener al menos 6 caracteres.',
            'password.different' => 'La nueva contraseña debe ser distinta de la actual.',
        ]);

        $usuario = $request->user();
        if (!Hash::check($data['password_actual'], $usuario->password)) {
            return response()->json([
                'message' => 'La contraseña actual no es correcta.',
                'errors'  => ['password_actual' => ['La contraseña actual no es correcta.']],
            ], 422);
        }

        $usuario->password = $data['password']; // cast 'hashed'
        $usuario->save();

        return response()->json(['message' => 'Contraseña actualizada.']);
    }

    /** DELETE /api/usuarios/me — borrado lógico, bloqueado con reservas activas (409). */
    public function destroyMe(Request $request): JsonResponse
    {
        $usuario = $request->user();
        $hoy = now()->toDateString();

        if ($usuario->esViajero()) {
            $activas = ReservaHabitacion::where('usuario_id', $usuario->id)
                ->whereIn('estado', ReservaHabitacion::OCUPAN)->whereDate('fecha_fin', '>=', $hoy)->count()
                + ReservaTour::where('usuario_id', $usuario->id)->where('estado', 'confirmada')
                    ->whereHas('salida', fn ($q) => $q->whereDate('fecha', '>=', $hoy))->count();
        } else {
            $activas = Servicio::where('proveedor_id', $usuario->id)->get()
                ->sum(fn ($s) => EliminacionServicio::reservasActivas($s));
        }

        if ($activas > 0) {
            return response()->json([
                'message' => "Tienes {$activas} reserva(s) activa(s). Cancélalas o espera a que terminen para eliminar tu cuenta.",
                'reservas_activas' => $activas,
            ], 409);
        }

        try {
            auth('api')->invalidate(true);
        } catch (\Throwable $e) {
            // el token ya no era válido
        }

        if ($usuario->esProveedor()) {
            Servicio::where('proveedor_id', $usuario->id)->get()->each(function ($s) {
                $s->update(['activo' => false]);
                $s->delete();
            });
        }
        $usuario->delete(); // soft delete: el historial de reservas se conserva

        return response()->json(['message' => 'Cuenta eliminada exitosamente.'])
            ->withCookie(cookie()->forget('access_token'));
    }
}
