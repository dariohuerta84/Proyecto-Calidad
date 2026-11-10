<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Review;
use App\Models\Usuario;
use App\Models\Servicio;
use App\Http\Requests\StoreReviewRequest;
use App\Http\Requests\UpdateReviewRequest;
use App\Http\Resources\ReviewResource;
use Illuminate\Http\JsonResponse;

class ReviewController extends Controller
{
    /**
     * GET /api/reviews
     * Lista reviews con paginación y filtros opcionales
     */
    public function index(Request $request): JsonResponse
    {   
        $query = Review::with(['usuario:id,nombre,apellido', 'servicio:id,nombre,tipo'])
                        ->latest(); // ORDER BY created_at DESC

        // Filtro por servicio_id
        if ($request->has('servicio_id')) {
            $query->where('servicio_id', $request->servicio_id);
        }

        // Filtro por usuario_id
        if ($request->has('usuario_id')) {
            $query->where('usuario_id', $request->usuario_id);
        }

        // Filtro por calificación
        if ($request->has('calificacion')) {
            $query->where('calificacion', $request->calificacion);
        }

        // Paginación (por defecto 15 reviews por página, máximo 50)
        $perPage = min((int) $request->query('per_page', 15), 50);
        $reviews = $query->paginate($perPage);

        return ReviewResource::collection($reviews)->response();
    }

    /**
     * POST /api/reviews
     * Crea una nueva review (usuario autenticado)
     */
    public function store(StoreReviewRequest $request): JsonResponse
    {
        $data = $request->validated();
        
        // Asignar el usuario autenticado automáticamente
        $data['usuario_id'] = auth()->id();

        $review = Review::create($data);
        
        // Cargar relaciones para el resource
        $review->load(['usuario:id,nombre,apellido', 'servicio:id,nombre,tipo']);

        return response()->json([
            'message' => 'Review creada correctamente',
            'data' => new ReviewResource($review)
        ], 201);
    }

    /**
     * PUT/PATCH /api/reviews/{review}
     * Actualiza una review existente (solo el autor)
     */
    public function update(UpdateReviewRequest $request, Review $review): JsonResponse
    {   
        $review->update($request->validated());
        
        // Cargar relaciones para el resource
        $review->load(['usuario:id,nombre,apellido', 'servicio:id,nombre,tipo']);
        
        return response()->json([
            'message' => 'Review actualizada correctamente',
            'data' => new ReviewResource($review)
        ]);
    }

    /**
     * DELETE /api/reviews/{review}
     * Elimina una review (solo el autor)
     */
    public function destroy(Review $review): JsonResponse
    {   
        // Validar que el usuario autenticado sea el autor
        if ((int) $review->usuario_id !== (int) auth()->id()) {
            return response()->json([
                'message' => 'No tienes permiso para eliminar esta reseña. Solo el autor puede eliminarla.'
            ], 403);
        }

        $review->delete();
        
        return response()->json(null, 204);
    }
}
