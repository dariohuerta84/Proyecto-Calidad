<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreHotelRequest;
use App\Http\Requests\UpdateHotelRequest;
use App\Http\Resources\HotelResource;
use App\Http\Requests\DisponibilidadHotelRequest;
use App\Models\Hotel;
use App\Models\Servicio;
use App\Models\Habitacion;
use App\Models\ReservaHabitacion;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use App\Models\ServicioImagen;
use Illuminate\Support\Facades\DB;

class HotelController extends Controller
{
    // GET /api/hoteles
    public function index(Request $request): JsonResponse
    {
        // --- MODO DISPONIBILIDAD GLOBAL (con check_in + check_out) ---
        if ($request->filled('check_in') && $request->filled('check_out')) {
            $checkIn  = Carbon::parse($request->query('check_in'))->toDateString();
            $checkOut = Carbon::parse($request->query('check_out'))->toDateString();

            $adultos = $request->query('adultos'); // opcional
            $ninos   = $request->query('ninos');   // opcional
            $reqHab  = (int) ($request->query('habitaciones', 1));

            // Filtros opcionales de destino (útiles para buscar por ciudad/pais)
            $pais   = $request->query('pais');
            $ciudad = $request->query('ciudad');

            // 1) Traer hoteles activos con servicio (y habitaciones) aplicando filtros de destino
            $hoteles = Hotel::with(['servicio:id,nombre,tipo,descripcion,ciudad,pais,imagen_url,activo', 'habitaciones'])
                ->whereHas('servicio', function ($q) use ($pais, $ciudad) {
                    $q->where('activo', true)
                      ->when($pais,   fn($qq) => $qq->where('pais', $pais))
                      ->when($ciudad, fn($qq) => $qq->where('ciudad', $ciudad));
                })
                ->get();

            if ($hoteles->isEmpty()) {
                return response()->json(['data' => []], 200);
            }

            // 2) Filtrar por capacidad y recolectar IDs de habitaciones candidatas
            $habitacionIds = [];
            $hoteles->each(function ($hotel) use (&$habitacionIds, $adultos, $ninos) {
                $cands = $hotel->habitaciones
                    ->when($adultos !== null, fn($c) => $c->where('capacidad_adultos', '>=', (int)$adultos))
                    ->when($ninos   !== null, fn($c) => $c->where('capacidad_ninos',   '>=', (int)$ninos));
                $habitacionIds = array_merge($habitacionIds, $cands->pluck('id')->all());
            });

            if (empty($habitacionIds)) {
                return response()->json(['data' => []], 200);
            }

            // 3) Cargar ocupación (reservas) para TODO el conjunto en el rango dado
            $ocupacion = ReservaHabitacion::query()
                ->selectRaw('habitacion_id, COALESCE(SUM(cantidad),0) as unidades_ocupadas')
                ->whereIn('estado', ['pendiente','confirmada'])
                ->whereDate('fecha_inicio', '<',  $checkOut)
                ->whereDate('fecha_fin',    '>',  $checkIn)
                ->whereIn('habitacion_id', $habitacionIds)
                ->groupBy('habitacion_id')
                ->pluck('unidades_ocupadas', 'habitacion_id');

            // 4) Construir respuesta por hotel
            $filtros = [
                'adultos'      => $adultos !== null ? (int)$adultos : null,
                'ninos'        => $ninos   !== null ? (int)$ninos   : null,
                'habitaciones' => $reqHab,
            ];

            $items = [];
            foreach ($hoteles as $hotel) {
                $cands = $hotel->habitaciones
                    ->when($adultos !== null, fn($c) => $c->where('capacidad_adultos', '>=', (int)$adultos))
                    ->when($ninos   !== null, fn($c) => $c->where('capacidad_ninos',   '>=', (int)$ninos));

                $habitacionesDisponibles = [];
                foreach ($cands as $h) {
                    $ocupadas     = (int) ($ocupacion[$h->id] ?? 0);
                    $disponibles  = max(0, (int)$h->cantidad - $ocupadas);

                    if ($disponibles >= $reqHab) {
                        $habitacionesDisponibles[] = [
                            'id'                   => $h->id,
                            'nombre'               => $h->nombre,
                            'capacidad_adultos'    => (int)$h->capacidad_adultos,
                            'capacidad_ninos'      => (int)$h->capacidad_ninos,
                            'precio_por_noche'     => (float)$h->precio_por_noche,
                            'unidades_totales'     => (int)$h->cantidad,
                            'unidades_disponibles' => $disponibles,
                            'descripcion'          => $h->descripcion,
                        ];
                    }
                }

                if (!empty($habitacionesDisponibles)) {
                    $items[] = (new HotelResource(
                        $hotel,
                        $checkIn,
                        $checkOut,
                        $filtros,
                        $habitacionesDisponibles
                    ))->toArray($request);
                }
            }

            return response()->json(['data' => $items], 200);
        }

        // --- MODO LISTA NORMAL (sin check_in/check_out) ---
        $hoteles = Hotel::with([
            'servicio:id,nombre,tipo,descripcion,ciudad,pais,imagen_url,activo',
            'habitaciones:id,servicio_id,precio_por_noche,cantidad'
        ])->get();

        return HotelResource::collection($hoteles)->response();
    }

    /**
     * POST /api/hoteles
     * Crea Servicio(tipo=hotel) + Hotel en una transacción.
     * 🔹 Ahora acepta JSON plano (sin objeto "servicio").
     */
    public function store(StoreHotelRequest $request): JsonResponse
    {
        $user = $request->user();
        $data = $request->validated();

        $hotel = DB::transaction(function () use ($data, $user) {
            // 1) Crear SERVICIO
            $servicio = Servicio::create([
                'proveedor_id' => $user->id,
                'nombre'       => $data['nombre'],
                'tipo'         => 'hotel',
                'descripcion'  => $data['descripcion'] ?? null,
                'ciudad'       => $data['ciudad'],
                'pais'         => $data['pais'],
                'imagen_url'   => $data['imagen_url'] ?? null,
                'activo'       => $data['activo'] ?? true,
            ]);

            // 1.1) Galería simple (si viene) – máx 5
            if (!empty($data['imagenes'])) {
                $imgs = collect($data['imagenes'])->take(5)->map(function ($item) {
                    if (is_string($item)) return ['url' => $item, 'alt' => null];
                    return ['url' => $item['url'] ?? null, 'alt' => $item['alt'] ?? null];
                })->filter(fn($x) => !empty($x['url']))->values()->all();

                if (!empty($imgs)) {
                    $servicio->imagenes()->createMany($imgs);
                }
            }

            // 2) Crear HOTEL (detalle)
            return Hotel::create([
                'servicio_id' => $servicio->id,
                'direccion'   => $data['direccion'],
                'estrellas'   => $data['estrellas'] ?? null,
            ]);
        });

        return response()->json([
            'message' => 'Servicio y hotel creados correctamente.',
            'data'    => $hotel->load('servicio:id,nombre,ciudad,pais'),
        ], 201);
    }


    // GET /api/hoteles/{servicio_id}
    public function show(int $servicio_id): JsonResponse
    {
        $hotel = Hotel::with([
            'servicio:id,nombre,ciudad,pais,imagen_url,descripcion',
            'servicio.imagenes:id,servicio_id,url,alt',
            'servicio.reviews' => function ($q) {
                $q->with('usuario:id,nombre,apellido')
                  ->latest()
                  ->limit(10); // Últimas 10 reviews
            },
            'habitaciones'
        ])->find($servicio_id);

        if (!$hotel) return response()->json(['message' => 'Hotel no encontrado'], 404);

        // Calcular estadísticas de reviews
        $servicio = $hotel->servicio;
        $promedioCalificacion = $servicio->promedio_calificacion ?? 0;
        $cantidadReviews = $servicio->cantidad_reviews ?? 0;

        return response()->json([
            'hotel' => [
                'servicio_id' => $hotel->servicio_id,
                'direccion'   => $hotel->direccion,
                'estrellas'   => $hotel->estrellas,
                'created_at'  => $hotel->created_at,
                'updated_at'  => $hotel->updated_at,
                'nombre'      => $servicio->nombre ?? null,
                'descripcion' => $servicio->descripcion ?? null,
                'ciudad'      => $servicio->ciudad ?? null,
                'pais'        => $servicio->pais ?? null,
                'imagen_url'  => $servicio->imagen_url,
                'imagenes'    => $servicio->imagenes->map(fn($img) => [
                    'url' => $img->url,
                    'alt' => $img->alt,
                ]),
                'calificacion' => [
                    'promedio' => $promedioCalificacion,
                    'cantidad' => $cantidadReviews,
                ],
            ],
            'habitaciones'=> $hotel->habitaciones->map(fn($h) => [
                'id'                 => $h->id,
                'nombre'             => $h->nombre,
                'capacidad_adultos'  => (int) $h->capacidad_adultos,
                'capacidad_ninos'    => (int) $h->capacidad_ninos,
                'cantidad'           => (int) $h->cantidad,
                'precio_por_noche'   => (float) $h->precio_por_noche,
            ]),
            'reviews' => $servicio->reviews->map(fn($r) => [
                'id' => $r->id,
                'usuario' => [
                    'id' => $r->usuario_id,
                    'nombre' => $r->usuario?->nombre,
                    'apellido' => $r->usuario?->apellido,
                    'nombre_completo' => $r->usuario 
                        ? trim(($r->usuario->nombre ?? '') . ' ' . ($r->usuario->apellido ?? '')) ?: 'Usuario Anónimo'
                        : 'Usuario Anónimo',
                ],
                'comentario' => $r->comentario,
                'calificacion' => (int) $r->calificacion,
                'created_at' => $r->created_at?->toISOString(),
                'fecha_formateada' => $r->created_at?->locale('es')->diffForHumans(),
            ]),
        ], 200);
    }


    // PUT /api/hoteles/{servicio_id}
    public function update(UpdateHotelRequest $request, int $servicio_id): JsonResponse
    {
        $hotel = Hotel::with(['servicio','habitaciones'])->find($servicio_id);
        if (!$hotel) {
            return response()->json(['message' => 'Hotel no encontrado'], 404);
        }

        $data = $request->validated();

        DB::transaction(function () use ($hotel, $data, $servicio_id) {
            // 1) Actualizar HOTEL
            $hotelData = array_intersect_key($data, array_flip(['direccion', 'estrellas']));
            if (!empty($hotelData)) {
                $hotel->update($hotelData);
            }

            // 2) Actualizar SERVICIO
            $servicioData = array_intersect_key($data, array_flip([
                'nombre', 'descripcion', 'ciudad', 'pais', 'imagen_url', 'activo'
            ]));
            if (!empty($servicioData)) {
                $hotel->servicio->update($servicioData);
            }

            // 3) Reemplazar galería si viene 'imagenes'
            if (array_key_exists('imagenes', $data)) {
                $hotel->servicio->imagenes()->delete();

                $imgs = collect($data['imagenes'] ?? [])->take(5)->map(function ($item) {
                    if (is_string($item)) return ['url' => $item, 'alt' => null];
                    return ['url' => $item['url'] ?? null, 'alt' => $item['alt'] ?? null];
                })->filter(fn($x) => !empty($x['url']))->values()->all();

                if (!empty($imgs)) {
                    $hotel->servicio->imagenes()->createMany($imgs);
                }
            }
            if(array_key_exists('habitaciones', $data)){
                $this->syncHabitaciones($hotel, $data['habitaciones']);
            }
        });

        $hotel->refresh()->load(['servicio','habitaciones']);

        return response()->json([
            'message' => 'Hotel actualizado correctamente.',
            'data'    => [
                'hotel'    => $hotel,
                'servicio' => $hotel->servicio,
                'habitaciones' => $hotel->habitaciones,
            ],
        ], 200);
    }

    private function syncHabitaciones(Hotel $hotel, array $incomingHabitacionesData):void
    {   
        $incomingHabitaciones=collect($incomingHabitacionesData);
        $existingHabitaciones = $hotel->habitaciones;
        $servicio_id=$hotel->servicio_id;

        $incomingIds=$incomingHabitaciones->pluck('id')->filter()->toArray();
        $existingIds=$existingHabitaciones->pluck('id')->toArray();

        $idsToDelete=array_diff($existingIds, $incomingIds);
        if(!empty($idsToDelete)){
            Habitacion::whereIn('id',$idsToDelete)
                ->where('servicio_id', $servicio_id)
                ->delete();
        }
        $incomingHabitaciones->each(function($habData) use ($servicio_id){
            $habPayload=array_intersect_key($habData, array_flip([
                'nombre', 'capacidad_adultos', 'capacidad_ninos', 'precio_por_noche','cantidad', 'descripcion'
            ]));
            if(isset($habData['id']) && $habData['id']){
                $habitacion=Habitacion::find($habData['id']);
                if ($habitacion && $habitacion->servicio_id===$servicio_id){
                    $habitacion->update($habPayload);
                }
            }
            else{
                Habitacion::create(array_merge($habPayload,['servicio_id' => $servicio_id]));
            }
        });
    }

    // DELETE /api/hoteles/{servicio_id}
    public function destroy(UpdateHotelRequest $request, int $servicio_id): JsonResponse
    {
        $hotel = Hotel::find($servicio_id);
        if (!$hotel) return response()->json(['message' => 'Hotel no encontrado'], 404);

        $hotel->delete();
        return response()->json(null, 204);
    }

    // GET /api/hoteles/{servicio_id}/disponibilidad  (modo por un solo hotel)
    public function disponibilidad(DisponibilidadHotelRequest $request, int $servicio_id): JsonResponse
    {
        $hotel = Hotel::with([
            'servicio:id,nombre,tipo,descripcion,ciudad,pais,imagen_url,activo',
            'habitaciones:id,servicio_id,precio_por_noche'
        ])->where('servicio_id', $servicio_id)->first();

        if (!$hotel || !$hotel->servicio || !$hotel->servicio->activo) {
            return response()->json(['message' => 'Hotel no disponible o no encontrado.'], 404);
        }

        $checkIn  = Carbon::parse($request->input('check_in'))->toDateString();
        $checkOut = Carbon::parse($request->input('check_out'))->toDateString();

        $adultos  = $request->input('adultos');
        $ninos    = $request->input('ninos');
        $reqHab   = (int) ($request->input('habitaciones') ?? 1);

        $habitaciones = Habitacion::query()
            ->where('servicio_id', $servicio_id)
            ->when($adultos !== null, fn($q) => $q->where('capacidad_adultos', '>=', $adultos))
            ->when($ninos !== null, fn($q)   => $q->where('capacidad_ninos',   '>=', $ninos))
            ->get(['id','servicio_id','nombre','capacidad_adultos','capacidad_ninos','cantidad','precio_por_noche','descripcion']);

        $reservas = ReservaHabitacion::query()
            ->selectRaw('habitacion_id, COALESCE(SUM(cantidad),0) as unidades_ocupadas')
            ->whereIn('estado', ['pendiente','confirmada'])
            ->whereDate('fecha_inicio', '<',  $checkOut)
            ->whereDate('fecha_fin',    '>',  $checkIn)
            ->whereIn('habitacion_id', $habitaciones->pluck('id'))
            ->groupBy('habitacion_id')
            ->pluck('unidades_ocupadas', 'habitacion_id');

        $resultado = [];
        foreach ($habitaciones as $h) {
            $ocupadas = (int) ($reservas[$h->id] ?? 0);
            $disponibles = max(0, (int)$h->cantidad - $ocupadas);
            if ($disponibles < $reqHab) continue;

            $resultado[] = [
                'id'                   => $h->id,
                'nombre'               => $h->nombre,
                'capacidad_adultos'    => (int) $h->capacidad_adultos,
                'capacidad_ninos'      => (int) $h->capacidad_ninos,
                'precio_por_noche'     => (float) $h->precio_por_noche,
                'unidades_totales'     => (int) $h->cantidad,
                'unidades_disponibles' => $disponibles,
                'descripcion'          => $h->descripcion,
            ];
        }

        $filtros = [
            'adultos' => $adultos,
            'ninos' => $ninos,
            'habitaciones' => $reqHab,
        ];

        return (new HotelResource(
            $hotel,
            $checkIn,
            $checkOut,
            $filtros,
            array_values($resultado)
        ))->response();
    }
}
