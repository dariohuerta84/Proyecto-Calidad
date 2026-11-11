<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreTourRequest;
use App\Http\Requests\UpdateTourRequest;
use App\Models\Servicio;
use App\Models\Tour;
use App\Models\TourSalida;
use App\Services\EliminacionServicio;
use App\Support\Precio;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

/** TRV-301 (búsqueda), TRV-302 (detalle y cotización), TRV-504 (publicar/editar), TRV-508 (eliminar). */
class TourController extends Controller
{
    public const CATEGORIAS = ['Aventura', 'Gastronomía', 'Cultura', 'Relajación'];

    /** GET /api/tours?destino=&categoria=&checkIn=&checkOut=&cupos=&q=&sort=&page=&per_page= */
    public function index(Request $req): JsonResponse
    {
        $v = $req->validate([
            'destino'   => ['nullable', 'string', 'max:100'],
            'q'         => ['nullable', 'string', 'max:100'],
            'categoria' => ['nullable', 'in:' . implode(',', self::CATEGORIAS)],
            'checkIn'   => ['nullable', 'date_format:Y-m-d', 'after_or_equal:today'],
            'checkOut'  => ['nullable', 'date_format:Y-m-d', 'after_or_equal:checkIn'],
            'cupos'     => ['nullable', 'integer', 'between:1,50'],
            'sort'      => ['nullable', 'in:precio,-precio,nombre,-created_at'],
            'per_page'  => ['nullable', 'integer', 'between:1,100'],
            'page'      => ['nullable', 'integer', 'min:1'],
            'proveedor_id' => ['nullable', 'integer'],
        ], [
            'checkIn.after_or_equal'  => 'La fecha desde no puede ser anterior a hoy.',
            'checkOut.after_or_equal' => 'La fecha hasta debe ser igual o posterior a la fecha desde.',
            'categoria.in'            => 'La categoría debe ser Aventura, Gastronomía, Cultura o Relajación.',
        ]);

        $cupos = (int) ($v['cupos'] ?? 1);
        $desde = $v['checkIn'] ?? now()->toDateString();
        $hasta = $v['checkOut'] ?? null;

        $q = Servicio::query()
            ->where('tipo', 'tour')->where('activo', true)
            ->with('tour')
            ->withAvg('reviews', 'calificacion')->withCount('reviews')
            ->when($v['categoria'] ?? null, fn ($q, $c) => $q->whereHas('tour', fn ($t) => $t->where('categoria', $c)))
            ->when($v['proveedor_id'] ?? null, fn ($q, $p) => $q->where('proveedor_id', $p))
            // Solo tours con al menos una salida futura programada con cupo suficiente
            ->whereHas('salidas', function ($s) use ($desde, $hasta, $cupos) {
                $s->where('estado', 'programada')
                    ->whereDate('fecha', '>=', $desde)
                    ->when($hasta, fn ($x) => $x->whereDate('fecha', '<=', $hasta))
                    ->whereRaw('cupo_total - cupo_reservado >= ?', [$cupos]);
            });

        $todos = $q->get();

        // destino / texto libre sin distinguir mayúsculas ni tildes
        $norm = fn ($s) => Str::lower(Str::ascii((string) $s));
        if (!empty($v['destino'])) {
            $d = $norm($v['destino']);
            $todos = $todos->filter(fn ($s) => str_contains($norm($s->ciudad), $d) || str_contains($norm($s->pais), $d));
        }
        if (!empty($v['q'])) {
            $t = $norm($v['q']);
            $todos = $todos->filter(fn ($s) => str_contains($norm($s->nombre . ' ' . $s->descripcion . ' ' . $s->ciudad), $t));
        }

        $todos = match ($v['sort'] ?? null) {
            'precio'  => $todos->sortBy(fn ($s) => (float) $s->tour?->precio),
            '-precio' => $todos->sortByDesc(fn ($s) => (float) $s->tour?->precio),
            'nombre'  => $todos->sortBy('nombre'),
            default   => $todos->sortByDesc('created_at'),
        };

        $perPage = (int) ($v['per_page'] ?? 12);
        $page = (int) ($v['page'] ?? 1);
        $total = $todos->count();
        $data = $todos->values()->forPage($page, $perPage)->map(fn ($s) => $this->resumen($s))->values();

        // Formato de paginación de Laravel (compatible con el frontend existente)
        return response()->json([
            'current_page' => $page,
            'data'         => $data,
            'last_page'    => max(1, (int) ceil($total / $perPage)),
            'per_page'     => $perPage,
            'total'        => $total,
            'meta'         => [
                'current_page' => $page,
                'last_page'    => max(1, (int) ceil($total / $perPage)),
                'per_page'     => $perPage,
                'total'        => $total,
            ],
        ]);
    }

    private function resumen(Servicio $s): array
    {
        return [
            'id'          => $s->id,
            'proveedor_id'=> $s->proveedor_id,
            'nombre'      => $s->nombre,
            'tipo'        => 'tour',
            'descripcion' => $s->descripcion,
            'ciudad'      => $s->ciudad,
            'pais'        => $s->pais,
            'imagen_url'  => $s->imagen_url,
            'activo'      => (bool) $s->activo,
            'created_at'  => $s->created_at,
            'tour'        => $s->tour ? [
                'servicio_id' => $s->tour->servicio_id,
                'categoria'   => $s->tour->categoria,
                'duracion'    => (int) $s->tour->duracion,
                'precio'      => (float) $s->tour->precio,
            ] : null,
            'calificacion' => [
                'promedio' => $s->reviews_avg_calificacion !== null ? round((float) $s->reviews_avg_calificacion, 1) : 0,
                'cantidad' => (int) ($s->reviews_count ?? 0),
            ],
        ];
    }

    /** GET /api/tours/{tour}  ({tour} = servicio_id). Solo salidas futuras programadas. */
    public function show(Request $req, $tour): JsonResponse
    {
        $serv = Servicio::with([
            'tour',
            'imagenes:id,servicio_id,url,alt',
            'actividades',
            'tour.items:id,servicio_id,nombre,icono',
        ])->where('tipo', 'tour')->where('activo', true)->find($tour);

        if (!$serv) {
            return response()->json(['message' => 'Tour no encontrado'], 404);
        }

        $cupos = max(1, (int) $req->query('cupos', 1));
        $desde = $req->query('checkIn');
        $hasta = $req->query('checkOut', $desde);

        $salidas = TourSalida::where('servicio_id', $serv->id)
            ->where('estado', 'programada')
            ->whereDate('fecha', '>=', now()->toDateString())
            ->when($desde, fn ($q) => $q->whereDate('fecha', '>=', $desde)->whereDate('fecha', '<=', $hasta))
            ->orderBy('fecha')->orderBy('hora')
            ->get()
            ->filter(fn ($s) => !$s->yaPaso() && $s->disponibilidad >= $cupos)
            ->map(fn ($s) => [
                'id'               => $s->id,
                'servicio_id'      => $s->servicio_id,
                'fecha'            => $s->fechaIso(),
                'hora'             => $s->horaCorta(),
                'cupo_total'       => (int) $s->cupo_total,
                'cupo_reservado'   => (int) $s->cupo_reservado,
                'cupos_disponibles'=> $s->disponibilidad,
                'estado'           => $s->estado,
            ])->values();

        $reviews = $serv->reviews()->with('usuario:id,nombre,apellido')->latest()->limit(10)->get();

        $data = $serv->toArray();
        $data['tour']['precio'] = (float) $serv->tour?->precio;
        $data['salidas'] = $salidas;
        $data['calificacion'] = $serv->resumenResenas();
        $data['reviews'] = $reviews->map(fn ($r) => [
            'id' => $r->id,
            'usuario' => [
                'id' => $r->usuario_id,
                'nombre' => $r->usuario?->nombre,
                'apellido' => $r->usuario?->apellido,
                'nombre_completo' => trim(($r->usuario->nombre ?? '') . ' ' . ($r->usuario->apellido ?? '')) ?: 'Usuario Anónimo',
            ],
            'comentario' => $r->comentario,
            'calificacion' => (int) $r->calificacion,
            'created_at' => $r->created_at?->toISOString(),
            'fecha_formateada' => $r->created_at?->locale('es')->diffForHumans(),
        ])->values();

        return response()->json(['servicio' => $data]);
    }

    /** POST /api/tours/salidas/{salida}/cotizar {personas}  (TRV-302) */
    public function cotizar(Request $req, int $salida): JsonResponse
    {
        $v = $req->validate(['personas' => ['required', 'integer', 'min:1', 'max:50']]);
        $sal = TourSalida::with('servicio.tour')->find($salida);
        if (!$sal || !$sal->servicio || !$sal->servicio->activo) {
            return response()->json(['message' => 'Salida no encontrada.'], 404);
        }
        if ($sal->estado !== 'programada' || $sal->yaPaso()) {
            return response()->json(['message' => 'La salida no está disponible para reservar.'], 409);
        }
        if ($v['personas'] > $sal->disponibilidad) {
            return response()->json([
                'message' => "Solo quedan {$sal->disponibilidad} cupo(s) en esta salida.",
                'errors'  => ['personas' => ["Máximo {$sal->disponibilidad} personas."]],
            ], 422);
        }

        $precio = (float) $sal->servicio->tour->precio;
        $t = Precio::desdeSubtotalCentimos(Precio::lineaCentimos($precio, (int) $v['personas']));

        return response()->json(['data' => [
            'tour'            => ['id' => $sal->servicio->id, 'nombre' => $sal->servicio->nombre],
            'salida'          => ['id' => $sal->id, 'fecha' => $sal->fechaIso(), 'hora' => $sal->horaCorta(), 'cupos_disponibles' => $sal->disponibilidad],
            'personas'        => (int) $v['personas'],
            'precio_unitario' => $precio,
            'igv_porcentaje'  => Precio::IGV,
        ] + $t]);
    }

    /** POST /api/tours  (TRV-504) */
    public function store(StoreTourRequest $req): JsonResponse
    {
        $user = Auth::user();
        $data = $req->validated();

        $serv = DB::transaction(function () use ($data, $user) {
            $serv = Servicio::create([
                'proveedor_id' => $user->id,
                'nombre'       => $data['nombre'],
                'tipo'         => 'tour',
                'descripcion'  => $data['descripcion'],
                'ciudad'       => $data['ciudad'],
                'pais'         => $data['pais'],
                'imagen_url'   => $data['imagen_url'],
                'latitud'      => $data['latitud'] ?? null,
                'longitud'     => $data['longitud'] ?? null,
                'activo'       => $data['activo'] ?? true,
            ]);

            Tour::create([
                'servicio_id' => $serv->id,
                'categoria'   => $data['categoria'],
                'duracion'    => $data['duracion'],
                'precio'      => $data['precio'],
            ]);

            $this->guardarGaleria($serv, $data['imagenes'] ?? []);
            $this->guardarItems($serv, $data['items'] ?? []);

            foreach ($data['salidas'] as $s) {
                $serv->salidas()->create([
                    'fecha'          => $s['fecha'],
                    'hora'           => $s['hora'],
                    'cupo_total'     => (int) $s['cupo_total'],
                    'cupo_reservado' => 0,              // solo el sistema lo modifica
                    'estado'         => $s['estado'] ?? 'programada',
                ]);
            }

            return $serv->load('tour', 'tour.items', 'imagenes', 'salidas');
        });

        return response()->json(['message' => 'Tour creado correctamente', 'data' => $serv], 201)
            ->header('Location', url("/api/tours/{$serv->id}"));
    }

    /**
     * PUT/PATCH /api/tours/{tour}  (TRV-504)
     * Actualiza datos generales, galería e items. NO toca salidas (se gestionan en /tours/{id}/salidas),
     * así se conservan sus ids, cupos reservados y las reservas existentes.
     */
    public function update(UpdateTourRequest $req, $tour): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $this->assertOwnership($serv);
        $data = $req->validated();

        DB::transaction(function () use ($serv, $data) {
            $serv->fill(array_intersect_key($data, array_flip(['nombre', 'descripcion', 'ciudad', 'pais', 'imagen_url', 'activo', 'latitud', 'longitud'])))->save();

            if ($serv->tour) {
                $serv->tour->fill(array_intersect_key($data, array_flip(['categoria', 'duracion', 'precio'])))->save();
            }
            if (array_key_exists('imagenes', $data)) {
                $serv->imagenes()->delete();
                $this->guardarGaleria($serv, $data['imagenes'] ?? []);
            }
            if (array_key_exists('items', $data)) {
                $serv->tour?->items()->forceDelete();
                $this->guardarItems($serv, $data['items'] ?? []);
            }
        });

        return response()->json([
            'message' => 'Tour actualizado correctamente',
            'data'    => $serv->fresh()->load(['tour', 'tour.items', 'imagenes:id,servicio_id,url,alt', 'salidas']),
        ]);
    }

    /** DELETE /api/tours/{tour}  (TRV-508) */
    public function destroy(Request $req, $tour): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->find($tour);
        if (!$serv) {
            return response()->json(['message' => 'Tour no encontrado'], 404);
        }
        [$status, $body] = EliminacionServicio::eliminar($serv, $req->user());

        return response()->json($body, $status);
    }

    // ------------------------------------------------------------------ helpers

    private function guardarGaleria(Servicio $serv, array $imagenes): void
    {
        $imgs = collect($imagenes)->map(fn ($i) => is_string($i) ? ['url' => $i, 'alt' => null] : ['url' => $i['url'] ?? null, 'alt' => $i['alt'] ?? null])
            ->filter(fn ($x) => !empty($x['url']))->values()->all();
        if ($imgs) {
            $serv->imagenes()->createMany($imgs);
        }
    }

    private function guardarItems(Servicio $serv, array $items): void
    {
        foreach ($items as $item) {
            \App\Models\TourItem::create([
                'servicio_id' => $serv->id,
                'nombre'      => $item['nombre'],
                'icono'       => $item['icono'] ?? null,
            ]);
        }
    }

    private function assertOwnership(Servicio $serv): void
    {
        $user = Auth::user();
        if (!$user || $user->rol !== 'proveedor' || (int) $user->id !== (int) $serv->proveedor_id) {
            abort(response()->json(['message' => 'No autorizado: no eres el proveedor dueño de este tour.'], 403));
        }
    }
}
