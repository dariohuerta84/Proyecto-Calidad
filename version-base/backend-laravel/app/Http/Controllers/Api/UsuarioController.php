<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\UpdateUsuarioRequest;
use App\Models\Usuario;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class UsuarioController extends Controller
{
    // GET /api/usuarios  (público)
    public function index(): JsonResponse
    {
        $usuarios = Usuario::select(
            'id',
            'nombre',
            'apellido',
            'empresa_nombre',
            'telefono',
            'ruc',
            'email',
            'rol',
            'created_at'
        )->get();

        return response()->json($usuarios, 200);
    }

    // GET /api/usuarios/{usuario}  (público)
    public function show(Usuario $usuario): JsonResponse
    {
        return response()->json(
            $usuario->only(
                'id',
                'nombre',
                'apellido',
                'empresa_nombre',
                'telefono',
                'ruc',
                'email',
                'rol',
                'created_at',
                'updated_at'
            ),
            200
        );
    }

    // PATCH/PUT /api/usuarios/me  (protegido)
    public function updateMe(UpdateUsuarioRequest $request): JsonResponse
    {
        $usuario = $request->user();
        $data = $request->validated();

        if (empty($data)) {
            return response()->json(['message' => 'No se recibieron campos para actualizar.'], 422);
        }

        // Defensa extra: nunca permitir cambiar rol desde aquí
        unset($data['rol']);

        $usuario->update($data);
        $usuario->refresh();

        return response()->json([
            'message' => 'Usuario actualizado correctamente.',
            'data'    => $usuario->only(
                'id',
                'nombre',
                'apellido',
                'empresa_nombre',
                'telefono',
                'ruc',
                'email',
                'rol',
                'updated_at'
            ),
        ], 200);
    }

    // DELETE /api/usuarios/me  (protegido)
    public function destroyMe(Request $request): JsonResponse
    {
        $usuario = $request->user();
        $usuario->delete();

        return response()->json(['message' => 'Cuenta eliminada exitosamente.'], 200);
    }
}
