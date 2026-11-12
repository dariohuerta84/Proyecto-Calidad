<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreReviewRequest;
use App\Http\Requests\UpdateReviewRequest;
use App\Http\Resources\ReviewResource;
use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\Review;
use App\Models\Servicio;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

/** HU-V14 — Reseñas (solo de quien usó el servicio). */
class ReviewController extends Controller
{
    /** GET /api/reviews?servicio_id=&usuario_id=&calificacion=&page=&per_page= */
    public function index(Request $request): JsonResponse
    {
        $v = $request->validate([
            'servicio_id'  => ['nullable', 'integer'],
            'usuario_id'   => ['nullable', 'integer'],
            'calificacion' => ['nullable', 'integer', 'between:1,5'],
            'per_page'     => ['nullable', 'integer', 'between:1,50'],
        ]);

        $reviews = Review::with(['usuario:id,nombre,apellido', 'servicio:id,nombre,tipo'])
            ->when($v['servicio_id'] ?? null, fn ($q, $x) => $q->where('servicio_id', $x))
            ->when($v['usuario_id'] ?? null, fn ($q, $x) => $q->where('usuario_id', $x))
            ->when($v['calificacion'] ?? null, fn ($q, $x) => $q->where('calificacion', $x))
            ->latest()->orderByDesc('id')
            ->paginate((int) ($v['per_page'] ?? 10));

        return ReviewResource::collection($reviews)->response();
    }

    /** GET /api/reviews/resumen?servicio_id= — promedio y cantidad del conjunto completo. */
    public function resumen(Request $request): JsonResponse
    {
        $v = $request->validate(['servicio_id' => ['required', 'integer', 'exists:servicios,id']]);
        $q = Review::where('servicio_id', $v['servicio_id']);
        $dist = (clone $q)->selectRaw('calificacion, COUNT(*) as n')->groupBy('calificacion')->pluck('n', 'calificacion');
        $total = (clone $q)->count();

        return response()->json(['data' => [
            'servicio_id'  => (int) $v['servicio_id'],
            'promedio'     => $total ? round((float) (clone $q)->avg('calificacion'), 1) : 0,
            'total'        => $total,
            'distribucion' => collect(range(1, 5))->mapWithKeys(fn ($i) => [$i => (int) ($dist[$i] ?? 0)]),
        ]]);
    }

    /** GET /api/reviews/puedo-opinar?servicio_id= — para mostrar u ocultar el formulario. */
    public function puedoOpinar(Request $request): JsonResponse
    {
        $v = $request->validate(['servicio_id' => ['required', 'integer', 'exists:servicios,id']]);
        [$ok, $motivo] = $this->elegible($request->user()->id, (int) $v['servicio_id']);

        return response()->json(['data' => ['puede' => $ok, 'motivo' => $motivo,
            'mi_resena' => Review::where('servicio_id', $v['servicio_id'])->where('usuario_id', $request->user()->id)->first()]]);
    }

    /** @return array{0: bool, 1: ?string} */
    private function elegible(int $usuarioId, int $servicioId): array
    {
        $servicio = Servicio::find($servicioId);
        if (!$servicio) {
            return [false, 'El servicio no existe.'];
        }
        if (Review::where('servicio_id', $servicioId)->where('usuario_id', $usuarioId)->exists()) {
            return [false, 'Ya publicaste una reseña para este servicio.'];
        }
        $hoy = now()->toDateString();
        $ok = $servicio->tipo === 'hotel'
            ? ReservaHabitacion::where('usuario_id', $usuarioId)->where('estado', 'confirmada')
                ->whereDate('fecha_fin', '<=', $hoy)
                ->whereHas('habitacion', fn ($q) => $q->where('servicio_id', $servicioId))->exists()
            : ReservaTour::where('usuario_id', $usuarioId)->where('estado', 'confirmada')
                ->whereHas('salida', fn ($q) => $q->where('servicio_id', $servicioId)->whereDate('fecha', '<', $hoy))->exists();

        return $ok ? [true, null] : [false, 'Debes haber completado una reserva confirmada de este servicio para opinar.'];
    }

    /** POST /api/reviews */
    public function store(StoreReviewRequest $request): JsonResponse
    {
        $data = $request->validated();
        $usuarioId = $request->user()->id;

        if (Review::where('servicio_id', $data['servicio_id'])->where('usuario_id', $usuarioId)->exists()) {
            return response()->json(['message' => 'Ya publicaste una reseña para este servicio. Puedes editarla.'], 409);
        }
        [$ok, $motivo] = $this->elegible($usuarioId, (int) $data['servicio_id']);
        if (!$ok) {
            return response()->json(['message' => $motivo], 403);
        }

        $review = Review::create($data + ['usuario_id' => $usuarioId]);
        $review->load(['usuario:id,nombre,apellido', 'servicio:id,nombre,tipo']);

        return response()->json(['message' => 'Reseña publicada', 'data' => new ReviewResource($review)], 201);
    }

    /** PUT/PATCH /api/reviews/{review} — solo el autor (UpdateReviewRequest::authorize). */
    public function update(UpdateReviewRequest $request, Review $review): JsonResponse
    {
        $review->update($request->validated());
        $review->load(['usuario:id,nombre,apellido', 'servicio:id,nombre,tipo']);

        return response()->json(['message' => 'Reseña actualizada', 'data' => new ReviewResource($review)]);
    }

    /** DELETE /api/reviews/{review} — solo el autor. */
    public function destroy(Request $request, Review $review): JsonResponse
    {
        if ((int) $review->usuario_id !== (int) $request->user()->id) {
            return response()->json(['message' => 'Solo el autor puede eliminar esta reseña.'], 403);
        }
        $review->forceDelete(); // permite volver a opinar si quiere

        return response()->json(null, 204);
    }
}
