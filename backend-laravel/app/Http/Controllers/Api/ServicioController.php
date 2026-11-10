<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreServicioRequest;
use App\Http\Requests\UpdateServicioRequest;
use App\Models\Servicio;
use App\Models\Habitacion;
use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\TourSalida;
use App\Models\ServicioImagen; // 👈 NUEVO
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB; // 👈 NUEVO

class ServicioController extends Controller
{
    // GET /api/servicios (público)
    public function index(Request $request): JsonResponse
    {
        $q = Servicio::query()
            ->select('id','proveedor_id','nombre','tipo','ciudad','pais','descripcion','imagen_url','activo','created_at');

        // Filtros básicos
        if ($request->filled('tipo'))   $q->where('tipo', $request->query('tipo'));      // 'hotel' | 'tour'
        if ($request->filled('ciudad')) $q->where('ciudad', $request->query('ciudad'));
        if ($request->filled('pais'))   $q->where('pais', $request->query('pais'));
        if ($request->filled('activo')) $q->where('activo', filter_var($request->query('activo'), FILTER_VALIDATE_BOOLEAN));

        if ($request->filled('q')) {
            $term = trim($request->query('q'));
            $q->where(function ($w) use ($term) {
                $w->where('nombre','like',"%{$term}%")
                  ->orWhere('descripcion','like',"%{$term}%");
            });
        }

        // Orden
        $orden = $request->query('orden', 'recientes');
        $orden === 'recientes'
            ? $q->orderByDesc('id')
            : $q->orderBy('nombre');

        // Paginación
        $perPage = max(1, min((int) $request->query('per_page', 12), 50));
        $paginator = $q->paginate($perPage);

        return response()->json($paginator, 200);
    }

    // GET /api/proveedor/servicios (privado - requiere auth)
    public function indexMine(Request $request): JsonResponse
    {
        $user = $request->user();

        $tipo   = $request->query('tipo');   // 'hotel' | 'tour' | null
        $ciudad = $request->query('ciudad');
        $pais   = $request->query('pais');

        // Solo filtrar por 'activo' si el parámetro viene en la URL
        $activo = $request->has('activo')
            ? filter_var($request->query('activo'), FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE)
            : null;

        $search = $request->query('search');

        $perPage   = min((int) $request->query('per_page', 15), 100);
        $sort      = $request->query('sort', '-created_at'); // ej: 'nombre' o '-nombre'
        $dir       = str_starts_with($sort, '-') ? 'desc' : 'asc';
        $sortField = ltrim($sort, '-');

        $sortable = ['nombre', 'tipo', 'ciudad', 'pais', 'activo', 'created_at'];
        if (!in_array($sortField, $sortable, true)) {
            $sortField = 'created_at';
        }

        $query = Servicio::query()
            ->where('proveedor_id', $user->id)
            ->when($tipo,   fn($q) => $q->where('tipo', $tipo))
            ->when($ciudad, fn($q) => $q->where('ciudad', $ciudad))
            ->when($pais,   fn($q) => $q->where('pais', $pais))
            ->when($activo !== null, fn($q) => $q->where('activo', $activo))
            ->when($search, fn($q) => $q->where('nombre', 'like', "%{$search}%"));

        // Relaciones + métricas
        $query->with([
                // OJO: hoteles no tiene columna 'id' → NO la pidas
                'hotel:servicio_id,direccion,estrellas',
                // tours también usa PK = servicio_id; su precio es 'precio'
                'tour:servicio_id,categoria,duracion,precio',
            ])
            ->withCount([
                'habitaciones as habitaciones_total',
                'salidas as salidas_total',
            ])
            ->select('servicios.*')
            ->selectSub(
                Habitacion::selectRaw('MIN(precio_por_noche)')
                    ->whereColumn('servicio_id', 'servicios.id'),
                'tarifa_min_hotel'
            )
            ->selectSub(
                TourSalida::selectRaw('COUNT(*)')
                    ->whereColumn('servicio_id', 'servicios.id')
                    ->where('fecha', '>=', now()->toDateString()),
                'proximas_salidas_count'
            )
            ->selectSub(
                ReservaHabitacion::selectRaw('COUNT(*)')
                    // 2. Solo contar reservas cuyas Habitaciones servicio_id=servicios.id
                    ->whereHas('habitacion', function ($q) {
                        $q->whereColumn('servicio_id', 'servicios.id');
                    })
                    // 3. Filtrar por estado 'confirmada'
                    ->where('estado', 'confirmada'),
                'reservas_confirmadas_hotel' // Alias del campo
            )
            ->selectSub(
                ReservaHabitacion::selectRaw('COUNT(*)')
                    // 2. Solo contar reservas cuyas Habitaciones servicio_id=servicios.id
                    ->whereHas('habitacion', function ($q) {
                        $q->whereColumn('servicio_id', 'servicios.id');
                    })
                    // 3. Filtrar por estado 'confirmada'
                    ->where('estado', 'cancelada'),
                'reservas_canceladas_hotel' // Alias del campo
            )
            ->selectSub(
                ReservaTour::selectRaw('COUNT(*)')
                    // 2. Solo contar reservas cuyas Habitaciones servicio_id=servicios.id
                    ->whereHas('salida', function ($q) {
                        $q->whereColumn('servicio_id', 'servicios.id');
                    })
                    // 3. Filtrar por estado 'confirmada'
                    ->where('estado', 'confirmada'),
                'reservas_confirmadas_tour' // Alias del campo
            )
            ->selectSub(
                ReservaTour::selectRaw('COUNT(*)')
                    // 2. Solo contar reservas cuyas Habitaciones servicio_id=servicios.id
                    ->whereHas('salida', function ($q) {
                        $q->whereColumn('servicio_id', 'servicios.id');
                    })
                    // 3. Filtrar por estado 'confirmada'
                    ->where('estado', 'cancelada'),
                'reservas_canceladas_tour' // Alias del campo
            )
            ->orderBy($sortField, $dir);

        $servicios = $query->paginate($perPage)->appends($request->query());

        $data = $servicios->getCollection()->map(function ($s) {
            $base = [
                'id'          => $s->id,
                'tipo'        => $s->tipo,
                'nombre'      => $s->nombre,
                'descripcion' => $s->descripcion,
                'ciudad'      => $s->ciudad,
                'pais'        => $s->pais,
                'imagen_url'  => $s->imagen_url,
                'activo'      => (bool) $s->activo,
                'created_at'  => $s->created_at,
            ];

            if ($s->tipo === 'hotel') {
                return $base + [
                    'meta_tipo' => [
                        'direccion'           => $s->hotel->direccion ?? null,
                        'estrellas'           => $s->hotel->estrellas ?? null,
                        'habitaciones_count'  => $s->habitaciones_total ?? 0,
                        'tarifa_min_desde'    => $s->tarifa_min_hotel !== null ? (float) $s->tarifa_min_hotel : null,
                    ],
                    'reservas_totales' => [
                        'confirmadas' => (int) ($s->reservas_confirmadas_hotel ?? 0),
                        'canceladas'  => (int) ($s->reservas_canceladas_hotel ?? 0),
                    ],
                ];
            }

            // tipo === 'tour'
            return $base + [
                'meta_tipo' => [
                    'categoria'         => $s->tour->categoria ?? null,
                    'duracion'          => $s->tour->duracion ?? null,
                    'precio'            => isset($s->tour->precio) ? (float) $s->tour->precio : null,
                    'salidas_count'     => $s->salidas_total ?? 0,
                    'proximas_salidas'  => (int) $s->proximas_salidas_count,
                ],
                'reservas_totales' => [
                    'confirmadas' => (int) ($s->reservas_confirmadas_tour ?? 0),
                    'canceladas'  => (int) ($s->reservas_canceladas_tour ?? 0),
                ],
            ];
        });

        return response()->json([
            'data'  => $data,
            'meta'  => [
                'total'        => $servicios->total(),
                'per_page'     => $servicios->perPage(),
                'current_page' => $servicios->currentPage(),
                'last_page'    => $servicios->lastPage(),
            ],
            'links' => [
                'first' => $servicios->url(1),
                'prev'  => $servicios->previousPageUrl(),
                'next'  => $servicios->nextPageUrl(),
                'last'  => $servicios->url($servicios->lastPage()),
            ],
        ]);
    }
    //GET /api/proveedor/servicios/{id}/reservas
    public function reservasPorServicio(Request $request, int $id): JsonResponse
    {
        $user = $request->user();

        // 🔒 Verifica que el servicio pertenezca al proveedor autenticado
        $servicio = Servicio::where('id', $id)
            ->where('proveedor_id', $user->id)
            ->firstOrFail();

        // Contenedores de respuesta
        $totales = [
            'confirmadas' => 0,
            'canceladas'  => 0,
        ];
        $detalle = [];

        if ($servicio->tipo === 'hotel') {
            // 🏨 --- HOTEL ---
            $reservas = ReservaHabitacion::whereHas('habitacion', function ($q) use ($servicio) {
                    $q->where('servicio_id', $servicio->id);
                })
                ->with(['habitacion:id,nombre,servicio_id'])
                ->select('habitacion_id', 'estado', 'cantidad', 'total')
                ->get();

            $totales['confirmadas'] = $reservas->where('estado', 'confirmada')->count();
            $totales['canceladas']  = $reservas->where('estado', 'cancelada')->count();

            // Agrupamos por habitación
            $detalle = $reservas->groupBy('habitacion_id')->map(function ($items, $habitacionId) {
                $habitacion = $items->first()->habitacion;
                return [
                    'habitacion_id' => $habitacionId,
                    'habitacion_nombre' => $habitacion->nombre ?? null,
                    'reservas' => $items->count(),
                    'total_personas' => $items->sum('cantidad'),
                    'total_monto' => (float) $items->sum('total'),
                ];
            })->values()->all();
        }

        elseif ($servicio->tipo === 'tour') {
            // 🏞 --- TOUR ---
            $reservas = ReservaTour::whereHas('salida', function ($q) use ($servicio) {
                    $q->where('servicio_id', $servicio->id);
                })
                ->with(['salida:id,servicio_id,fecha'])
                ->select('salida_id', 'estado', 'personas', 'total')
                ->get();

            $totales['confirmadas'] = $reservas->where('estado', 'confirmada')->count();
            $totales['canceladas']  = $reservas->where('estado', 'cancelada')->count();

            // Agrupamos por salida
            $detalle = $reservas->groupBy('salida_id')->map(function ($items, $salidaId) {
                $salida = $items->first()->salida;
                return [
                    'salida_id' => $salidaId,
                    'fecha' => $salida->fecha ?? null,
                    'reservas' => $items->count(),
                    'total_personas' => $items->sum('personas'),
                    'total_monto' => (float) $items->sum('total'),
                ];
            })->values()->all();
        }

        else {
            return response()->json(['error' => 'Tipo de servicio no soportado'], 400);
        }

        return response()->json([
            'servicio_id' => $servicio->id,
            'tipo'        => $servicio->tipo,
            'nombre'      => $servicio->nombre,
            'totales'     => $totales,
            'detalle'     => $detalle,
        ]);
    }

    // POST /api/servicios
    public function store(StoreServicioRequest $request): JsonResponse
    {
        $data = $request->validated();

        $servicio = DB::transaction(function () use ($data) {
            // 1) Crear servicio
            $servicio = Servicio::create($data);

            // 2) Guardar imágenes (si llegan)
            if (!empty($data['imagenes'])) {
                $imagenes = collect($data['imagenes'])->take(5)->map(function ($item) {
                    // item puede ser string (url) o array ['url' => ..., 'alt' => ...]
                    if (is_string($item)) {
                        return ['url' => $item, 'alt' => null];
                    }
                    return [
                        'url' => $item['url'] ?? null,
                        'alt' => $item['alt'] ?? null,
                    ];
                })->filter(fn($x) => !empty($x['url']))->values()->all();

                if (!empty($imagenes)) {
                    $servicio->imagenes()->createMany($imagenes);
                }
            }

            return $servicio;
        });

        return response()->json([
            'message' => 'Servicio creado exitosamente.',
            'data'    => $servicio->only('id','proveedor_id','nombre','tipo','ciudad','pais','activo','created_at'),
        ], 201);
    }

    // GET /api/servicios/{servicio}
    public function show(Servicio $servicio): JsonResponse
    {
        // 👇 Opcional: incluye galería en la respuesta pública de detalle
        $servicio->load('imagenes:id,servicio_id,url,alt');

        return response()->json([
            'id'          => $servicio->id,
            'proveedor_id'=> $servicio->proveedor_id,
            'nombre'      => $servicio->nombre,
            'tipo'        => $servicio->tipo,
            'ciudad'      => $servicio->ciudad,
            'pais'        => $servicio->pais,
            'descripcion' => $servicio->descripcion,
            'imagen_url'  => $servicio->imagen_url,
            'activo'      => $servicio->activo,
            'created_at'  => $servicio->created_at,
            'updated_at'  => $servicio->updated_at,
            'imagenes'    => $servicio->imagenes->map(fn($img) => [
                'url' => $img->url,
                'alt' => $img->alt,
            ]),
        ], 200);
    }

    // PUT/PATCH /api/servicios/{servicio}
    public function update(UpdateServicioRequest $request, Servicio $servicio): JsonResponse
    {
        // impedir cambiar 'tipo' tras crear (opcional y ya validado)
        if ($request->filled('tipo') && $request->input('tipo') !== $servicio->tipo) {
            return response()->json(['message' => 'No se permite cambiar el tipo del servicio.'], 422);
        }

        $data = $request->validated();

        DB::transaction(function () use ($servicio, $data) {
            // 1) Actualizar campos del servicio
            $servicio->update($data);

            // 2) Si viene 'imagenes', reemplazar la galería completa (estrategia simple)
            if (array_key_exists('imagenes', $data)) {
                // borrar actuales y volver a crear
                $servicio->imagenes()->delete();

                $imagenes = collect($data['imagenes'] ?? [])->take(5)->map(function ($item) {
                    if (is_string($item)) {
                        return ['url' => $item, 'alt' => null];
                    }
                    return [
                        'url' => $item['url'] ?? null,
                        'alt' => $item['alt'] ?? null,
                    ];
                })->filter(fn($x) => !empty($x['url']))->values()->all();

                if (!empty($imagenes)) {
                    $servicio->imagenes()->createMany($imagenes);
                }
            }
        });

        $servicio->refresh();

        return response()->json([
            'message' => 'Servicio modificado exitosamente.',
            'data'    => $servicio->only('id','proveedor_id','nombre','tipo','ciudad','pais','activo','updated_at'),
        ], 200);
    }

    // DELETE /api/servicios/{servicio}
    public function destroy(Servicio $servicio): JsonResponse
    {   
        // Si tiene imágenes o reviews asociadas al servicio
        $servicio->imagenes()->delete();
        $servicio->reviews()->delete();

        // Si el servicio tiene un Tour asociado
        if($servicio->tour) {
            $servicio->tour->items()->delete();
            $servicio->tour->salidas()->delete();
            $servicio->tour->actividades()->delete();
            $servicio->tour->delete();
        }
        // Si el servicio tiene un Hotel asociado
        if($servicio->hotel) {
            $servicio->hotel->habitaciones()->delete();
            $servicio->hotel->delete();
        }
        //Soft delete del servicio
        $servicio->delete();

        return response()->json(null, 204);
    }
    public function eliminados(): JsonResponse
    {
        // Trae solo los servicios que tienen deleted_at != null
        $serviciosEliminados = Servicio::onlyTrashed()
            ->with([
                // Relaciones directas
                'imagenes'=>fn($q)=>$q->withTrashed(), 
                'reviews'=>fn($q)=>$q->withTrashed(),

                // Relaciones tipo Tour
                'tour'=>fn($q)=>$q->withTrashed()->with([
                    'items'=>fn($q2)=>$q2->withTrashed(),
                    'salidas'=>fn($q2)=>$q2->withTrashed(),
                    'actividades'=>fn($q2)=>$q2->withTrashed(),
                ]),

                // Relaciones tipo Hotel
                'hotel'=>fn($q)=>$q->withTrashed()->with([
                    'habitaciones'=>fn($q2)=>$q2->withTrashed(),
                ]),
            ])
            ->get();

        return response()->json([
            'count' => $serviciosEliminados->count(),
            'data' => $serviciosEliminados,
        ], 200);
    }
}