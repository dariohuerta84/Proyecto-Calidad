<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreReservaRequest;
use App\Http\Requests\UpdateReservaRequest;
use App\Http\Resources\ReservaResource;
use App\Services\DisponibilidadService;
use Illuminate\Http\Request;
use App\Models\Reserva;
use App\Models\Usuario;
use App\Models\Servicio;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\DB;

class ReservaController extends Controller
{
    /**
     * Listar todas las reservas (con filtros opcionales)
     * GET /api/reservas
     */
    public function index(Request $request): JsonResponse
    {
        $query = Reserva::with([
            'usuario:id,nombre,apellido,email',
            'servicio:id,nombre,tipo,ciudad,precio,descripcion,imagen_url',
            'servicio.proveedor:id,nombre,apellido,email',
        ]);

        // Cargar relaciones específicas según el tipo de servicio de manera optimizada
        $query->with(['servicio.hotel' => function ($q) {
            $q->whereHas('servicio', function ($servicio) {
                $servicio->where('tipo', 'hotel');
            })->select('servicio_id', 'direccion', 'estrellas', 'precio_por_noche');
        }]);

        $query->with(['servicio.tour' => function ($q) {
            $q->whereHas('servicio', function ($servicio) {
                $servicio->where('tipo', 'tour');
            })->select('servicio_id', 'categoria', 'duracion', 'precio_por_persona');
        }]);

        // Filtros opcionales
        if ($request->filled('usuario_id')) {
            $query->where('usuario_id', $request->usuario_id);
        }

        if ($request->filled('estado')) {
            $query->where('estado', $request->estado);
        }

        if ($request->filled('tipo_servicio')) {
            $query->whereHas('servicio', function ($q) use ($request) {
                $q->where('tipo', $request->tipo_servicio);
            });
        }

        if ($request->filled('ciudad')) {
            $query->whereHas('servicio', function ($q) use ($request) {
                $q->where('ciudad', 'like', '%' . $request->ciudad . '%');
            });
        }

        if ($request->filled('fecha_desde')) {
            $query->where('fecha_inicio', '>=', $request->fecha_desde);
        }

        if ($request->filled('fecha_hasta')) {
            $query->where('fecha_fin', '<=', $request->fecha_hasta);
        }

        $reservas = $query->orderBy('created_at', 'desc')->get();

        return response()->json([
            'data' => ReservaResource::collection($reservas),
            'total' => $reservas->count(),
        ]);
    }

    /**
     * Crear una nueva reserva
     * POST /api/reservas
     */
    public function store(StoreReservaRequest $request): JsonResponse
    {
        $validated = $request->validated();

        // Verificar que el usuario existe y es viajero
        $usuario = Usuario::find($validated['usuario_id']);
        if ($usuario->rol !== 'viajero') {
            return response()->json([
                'message' => 'Solo los usuarios con rol viajero pueden hacer reservas.',
            ], 403);
        }

        // Verificar que el servicio existe
        $servicio = Servicio::with(['hotel', 'tour'])->find($validated['servicio_id']);
        if (!$servicio) {
            return response()->json([
                'message' => 'El servicio especificado no existe.',
            ], 404);
        }

        // Verificar disponibilidad del servicio en las fechas solicitadas
        if (DisponibilidadService::verificarConflictos(
            $validated['servicio_id'], 
            $validated['fecha_inicio'], 
            $validated['fecha_fin']
        )) {
            return response()->json([
                'message' => 'El servicio no está disponible en las fechas seleccionadas.',
                'error' => 'SERVICIO_NO_DISPONIBLE',
            ], 409);
        }

        try {
            DB::beginTransaction();

            // Generar código único de reserva con información semántica
            $prefijo = $servicio->tipo === 'hotel' ? 'H' : 'T'; // Hotel o Tour
            $anio = date('y'); // Año en 2 dígitos
            
            do {
                $codigoReserva = $prefijo . $anio . '-' . strtoupper(Str::random(6));
            } while (Reserva::where('codigo_reserva', $codigoReserva)->exists());

            // Crear la reserva
            $reserva = Reserva::create([
                'codigo_reserva' => $codigoReserva,
                'usuario_id' => $validated['usuario_id'],
                'servicio_id' => $validated['servicio_id'],
                'fecha_inicio' => $validated['fecha_inicio'],
                'fecha_fin' => $validated['fecha_fin'],
                'huespedes' => $validated['huespedes'],
                'estado' => 'pendiente',
            ]);

            DB::commit();

            return $this->respuestaReservaConRelaciones(
                $reserva,
                'Reserva creada correctamente.',
                201
            );

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'message' => 'Error al crear la reserva.',
            ], 500);
        }
    }

    /**
     * Mostrar una reserva específica
     * GET /api/reservas/{reserva}
     */
    public function show(Reserva $reserva): JsonResponse
    {
        return $this->respuestaReservaConRelaciones($reserva, 'Reserva encontrada.');
    }

    /**
     * Actualizar el estado de una reserva
     * PATCH /api/reservas/{reserva}/estado
     */
    public function actualizarEstado(UpdateReservaRequest $request, Reserva $reserva): JsonResponse
    {
        $validated = $request->validated();

        $estadoAnterior = $reserva->estado;

        // Validaciones de transiciones de estado
        if ($estadoAnterior === 'cancelada') {
            return response()->json([
                'message' => 'No se puede modificar una reserva cancelada.',
            ], 400);
        }

        if ($estadoAnterior === 'confirmada' && $validated['estado'] === 'pendiente') {
            return response()->json([
                'message' => 'No se puede cambiar una reserva confirmada a pendiente.',
            ], 400);
        }

        try {
            DB::beginTransaction();

            // Actualizar la reserva
            $reserva->update([
                'estado' => $validated['estado'],
            ]);

            DB::commit();

            return response()->json([
                'message' => "Reserva {$validated['estado']} correctamente.",
                'data' => [
                    'codigo_reserva' => $reserva->codigo_reserva,
                    'estado_anterior' => $estadoAnterior,
                    'estado_actual' => $reserva->estado,
                    'updated_at' => $reserva->updated_at,
                ],
            ]);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'message' => 'Error al actualizar el estado de la reserva.',
            ], 500);
        }
    }

    /**
     * Cancelar una reserva
     * POST /api/reservas/{reserva}/cancelar
     */
    public function cancelar(Request $request, Reserva $reserva): JsonResponse
    {
        $validatedData = $request->validate([
            'motivo' => 'nullable|string|max:255',
        ]);

        if ($reserva->estado === 'cancelada') {
            return response()->json([
                'message' => 'La reserva ya está cancelada.',
            ], 400);
        }

        $reserva->update([
            'estado' => 'cancelada',
        ]);

        return response()->json([
            'message' => 'Reserva cancelada correctamente.',
            'data' => [
                'codigo_reserva' => $reserva->codigo_reserva,
                'estado' => $reserva->estado,
                'motivo' => $validatedData['motivo'] ?? 'Sin motivo especificado',
                'updated_at' => $reserva->updated_at,
            ],
        ]);
    }

    /**
     * Obtener reservas de un usuario específico
     * GET /api/usuarios/{usuario_id}/reservas
     */
    public function porUsuario($usuario_id): JsonResponse
    {
        $usuario = Usuario::find($usuario_id);
        if (!$usuario) {
            return response()->json([
                'message' => 'Usuario no encontrado.',
            ], 404);
        }

        $reservas = Reserva::with([
            'usuario:id,nombre,apellido,email',
            'servicio:id,nombre,tipo,ciudad,precio,descripcion,imagen_url',
            'servicio.proveedor:id,nombre,apellido,email',
        ])
        // Optimización: cargar relaciones específicas solo cuando aplican
        ->with(['servicio.hotel' => function ($q) {
            $q->whereHas('servicio', function ($servicio) {
                $servicio->where('tipo', 'hotel');
            })->select('servicio_id', 'direccion', 'estrellas', 'precio_por_noche');
        }])
        ->with(['servicio.tour' => function ($q) {
            $q->whereHas('servicio', function ($servicio) {
                $servicio->where('tipo', 'tour');
            })->select('servicio_id', 'categoria', 'duracion', 'precio_por_persona');
        }])
        ->where('usuario_id', $usuario_id)
        ->orderBy('created_at', 'desc')
        ->get();

        return response()->json([
            'usuario' => [
                'id' => $usuario->id,
                'nombre' => $usuario->nombre,
                'apellido' => $usuario->apellido,
                'email' => $usuario->email,
            ],
            'reservas' => ReservaResource::collection($reservas),
            'total_reservas' => $reservas->count(),
            'estadisticas' => [
                'pendientes' => $reservas->where('estado', 'pendiente')->count(),
                'confirmadas' => $reservas->where('estado', 'confirmada')->count(),
                'canceladas' => $reservas->where('estado', 'cancelada')->count(),
            ],
        ]);
    }

    /**
     * Obtener reservas de un servicio específico
     * GET /api/servicios/{servicio_id}/reservas
     */
    public function porServicio($servicio_id): JsonResponse
    {
        $servicio = Servicio::with(['hotel', 'tour'])->find($servicio_id);
        if (!$servicio) {
            return response()->json([
                'message' => 'Servicio no encontrado.',
            ], 404);
        }

        $reservas = Reserva::with([
            'usuario:id,nombre,apellido,email',
            'servicio:id,nombre,tipo,ciudad,precio,descripcion,imagen_url',
            'servicio.proveedor:id,nombre,apellido,email',
            'servicio.hotel:servicio_id,direccion,estrellas,precio_por_noche',
            'servicio.tour:servicio_id,categoria,duracion,precio_por_persona'
        ])
            ->where('servicio_id', $servicio_id)
            ->orderBy('fecha_inicio', 'asc')
            ->get();

        return response()->json([
            'servicio' => [
                'id' => $servicio->id,
                'nombre' => $servicio->nombre,
                'tipo' => $servicio->tipo,
                'ciudad' => $servicio->ciudad,
            ],
            'reservas' => ReservaResource::collection($reservas),
            'total_reservas' => $reservas->count(),
            'estadisticas' => [
                'pendientes' => $reservas->where('estado', 'pendiente')->count(),
                'confirmadas' => $reservas->where('estado', 'confirmada')->count(),
                'canceladas' => $reservas->where('estado', 'cancelada')->count(),
            ],
        ]);
    }

    /**
     * Buscar reservas por código
     * GET /api/reservas/buscar/{codigo}
     */
    public function buscarPorCodigo($codigo): JsonResponse
    {
        $reserva = Reserva::with([
            'usuario:id,nombre,apellido,email',
            'servicio:id,nombre,tipo,descripcion,ciudad,precio,imagen_url',
            'servicio.proveedor:id,nombre,apellido,email',
            'servicio.hotel:servicio_id,direccion,estrellas,precio_por_noche',
            'servicio.tour:servicio_id,categoria,duracion,precio_por_persona'
        ])
        ->where('codigo_reserva', strtoupper($codigo))
        ->first();

        if (!$reserva) {
            return response()->json([
                'message' => 'Reserva no encontrada con el código especificado.',
            ], 404);
        }

        return response()->json([
            'message' => 'Reserva encontrada.',
            'data' => new ReservaResource($reserva),
        ]);
    }

    /**
     * Método helper para generar respuestas consistentes con ReservaResource
     */
    private function respuestaReservaConRelaciones(Reserva $reserva, string $mensaje, int $codigoEstado = 200): JsonResponse
    {
        // Cargar las relaciones necesarias para la presentación completa
        $reserva->load([
            'usuario:id,nombre,apellido,email',
            'servicio:id,nombre,tipo,descripcion,ciudad,precio,imagen_url',
            'servicio.proveedor:id,nombre,apellido,email',
            'servicio.hotel:servicio_id,direccion,estrellas,precio_por_noche',
            'servicio.tour:servicio_id,categoria,duracion,precio_por_persona'
        ]);
        
        return response()->json([
            'message' => $mensaje,
            'data' => new ReservaResource($reserva),
        ], $codigoEstado);
    }
}
