<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StoreHotelRequest;
use App\Http\Requests\UpdateHotelRequest;
use App\Http\Requests\DisponibilidadHotelRequest;
use App\Http\Resources\HotelResource;
use App\Models\Habitacion;
use App\Models\Hotel;
use App\Models\Servicio;
use App\Services\Disponibilidad;
use App\Services\EliminacionServicio;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Pagination\LengthAwarePaginator;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

class HotelController extends Controller
{
    /**
     * GET /api/hoteles  (TRV-201)
     * Filtros: ciudad, check_in, check_out, adultos, ninos, habitaciones, sort (precio|-precio|estrellas|-estrellas), page, per_page.
     * Sin fechas: catálogo completo paginado. Con fechas: solo hoteles con disponibilidad real.
     */
    public function index(Request $request): JsonResponse
    {
        $v = $request->validate([
            'ciudad'       => ['nullable', 'string', 'max:100'],
            'pais'         => ['nullable', 'string', 'max:100'],
            'check_in'     => ['nullable', 'required_with:check_out', 'date_format:Y-m-d', 'after_or_equal:today'],
            'check_out'    => ['nullable', 'required_with:check_in', 'date_format:Y-m-d', 'after:check_in'],
            'adultos'      => ['nullable', 'integer', 'between:1,10'],
            'ninos'        => ['nullable', 'integer', 'between:0,10'],
            'habitaciones' => ['nullable', 'integer', 'between:1,10'],
            'sort'         => ['nullable', 'in:precio,-precio,estrellas,-estrellas,nombre'],
            'per_page'     => ['nullable', 'integer', 'between:1,100'],
            'page'         => ['nullable', 'integer', 'min:1'],
        ], [
            'check_in.after_or_equal' => 'La fecha de entrada no puede ser anterior a hoy.',
            'check_out.after'         => 'La fecha de salida debe ser posterior a la de entrada.',
        ]);

        $conFechas = !empty($v['check_in']);
        if ($conFechas && Carbon::parse($v['check_in'])->diffInDays(Carbon::parse($v['check_out'])) > 30) {
            throw ValidationException::withMessages(['check_out' => 'La estadía no puede superar 30 noches.']);
        }

        $hoteles = Hotel::query()
            ->whereHas('servicio', fn ($q) => $q->where('activo', true))
            ->with([
                'servicio' => fn ($q) => $q->withAvg('reviews', 'calificacion')->withCount('reviews'),
                'habitaciones',
            ])
            ->get();

        // Ciudad / país sin distinguir mayúsculas ni tildes
        $norm = fn ($s) => Str::lower(Str::ascii((string) $s));
        if (!empty($v['ciudad'])) {
            $hoteles = $hoteles->filter(fn ($h) => $norm($h->servicio->ciudad) === $norm($v['ciudad']));
        }
        if (!empty($v['pais'])) {
            $hoteles = $hoteles->filter(fn ($h) => $norm($h->servicio->pais) === $norm($v['pais']));
        }

        $adultos = isset($v['adultos']) ? (int) $v['adultos'] : null;
        $ninos   = isset($v['ninos']) ? (int) $v['ninos'] : null;
        $reqHab  = (int) ($v['habitaciones'] ?? 1);

        $items = collect();
        if ($conFechas) {
            $ids = $hoteles->flatMap(fn ($h) => $h->habitaciones->pluck('id'))->all();
            $ocup = Disponibilidad::ocupacion($ids, $v['check_in'], $v['check_out']);
            $filtros = ['adultos' => $adultos, 'ninos' => $ninos, 'habitaciones' => $reqHab];

            foreach ($hoteles as $hotel) {
                $disp = [];
                foreach ($hotel->habitaciones as $h) {
                    if (!Disponibilidad::cabe($h, $adultos, $ninos, $reqHab)) {
                        continue;
                    }
                    $libres = max(0, (int) $h->cantidad - ($ocup[$h->id] ?? 0));
                    if ($libres >= $reqHab) {
                        $disp[] = $this->habitacionArray($h, $libres);
                    }
                }
                if ($disp) {
                    $items->push((new HotelResource($hotel, $v['check_in'], $v['check_out'], $filtros, $disp))->toArray($request));
                }
            }
        } else {
            foreach ($hoteles as $hotel) {
                $items->push((new HotelResource($hotel))->toArray($request));
            }
        }

        $items = match ($v['sort'] ?? null) {
            'precio'     => $items->sortBy(fn ($x) => $x['precio_por_noche'] ?? PHP_INT_MAX),
            '-precio'    => $items->sortByDesc('precio_por_noche'),
            'estrellas'  => $items->sortBy('estrellas'),
            '-estrellas' => $items->sortByDesc('estrellas'),
            default      => $items->sortBy('nombre'),
        };

        $perPage = (int) ($v['per_page'] ?? 12);
        $page = (int) ($v['page'] ?? 1);
        $pag = new LengthAwarePaginator($items->values()->forPage($page, $perPage)->values(), $items->count(), $perPage, $page);

        return response()->json([
            'data' => $pag->items(),
            'meta' => [
                'current_page' => $pag->currentPage(),
                'last_page'    => $pag->lastPage(),
                'per_page'     => $pag->perPage(),
                'total'        => $pag->total(),
            ],
        ]);
    }

    /** GET /api/hoteles/{servicio_id}  (TRV-202) */
    public function show(int $servicio_id): JsonResponse
    {
        $hotel = Hotel::with([
            'servicio.imagenes:id,servicio_id,url,alt',
            'habitaciones',
        ])->whereHas('servicio', fn ($q) => $q->where('activo', true))->find($servicio_id);

        if (!$hotel) {
            return response()->json(['message' => 'Hotel no encontrado'], 404);
        }

        $servicio = $hotel->servicio;
        $resumen = $servicio->resumenResenas();
        $reviews = $servicio->reviews()->with('usuario:id,nombre,apellido')->latest()->limit(10)->get();

        return response()->json([
            'hotel' => [
                'servicio_id'  => $hotel->servicio_id,
                'proveedor_id' => $servicio->proveedor_id,
                'direccion'    => $hotel->direccion,
                'estrellas'    => $hotel->estrellas,
                'created_at'   => $hotel->created_at,
                'updated_at'   => $hotel->updated_at,
                'nombre'       => $servicio->nombre,
                'descripcion'  => $servicio->descripcion,
                'ciudad'       => $servicio->ciudad,
                'pais'         => $servicio->pais,
                'latitud'      => $servicio->latitud !== null ? (float) $servicio->latitud : null,
                'longitud'     => $servicio->longitud !== null ? (float) $servicio->longitud : null,
                'imagen_url'   => $servicio->imagen_url,
                'imagenes'     => $servicio->imagenes->map(fn ($img) => ['id' => $img->id, 'url' => $img->url, 'alt' => $img->alt])->values(),
                'calificacion' => $resumen,
            ],
            'habitaciones' => $hotel->habitaciones->map(fn ($h) => $this->habitacionArray($h, (int) $h->cantidad))->values(),
            'reviews' => $reviews->map(fn ($r) => [
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
            ])->values(),
        ]);
    }

    /** GET /api/hoteles/{servicio_id}/disponibilidad  (TRV-202) */
    public function disponibilidad(DisponibilidadHotelRequest $request, int $servicio_id): JsonResponse
    {
        $hotel = Hotel::with(['servicio', 'habitaciones'])
            ->whereHas('servicio', fn ($q) => $q->where('activo', true))
            ->find($servicio_id);
        if (!$hotel) {
            return response()->json(['message' => 'Hotel no disponible o no encontrado.'], 404);
        }

        $in = $request->input('check_in');
        $out = $request->input('check_out');
        $adultos = $request->filled('adultos') ? (int) $request->input('adultos') : null;
        $ninos = $request->filled('ninos') ? (int) $request->input('ninos') : null;
        $reqHab = (int) ($request->input('habitaciones') ?: 1);

        $ocup = Disponibilidad::ocupacion($hotel->habitaciones->pluck('id')->all(), $in, $out);
        $resultado = [];
        foreach ($hotel->habitaciones as $h) {
            if (!Disponibilidad::cabe($h, $adultos, $ninos, $reqHab)) {
                continue;
            }
            $libres = max(0, (int) $h->cantidad - ($ocup[$h->id] ?? 0));
            if ($libres >= $reqHab) {
                $resultado[] = $this->habitacionArray($h, $libres);
            }
        }

        return (new HotelResource($hotel, $in, $out, ['adultos' => $adultos, 'ninos' => $ninos, 'habitaciones' => $reqHab], $resultado))->response();
    }

    /** POST /api/hoteles  (TRV-502) — servicio + hotel + galería + habitaciones en una transacción */
    public function store(StoreHotelRequest $request): JsonResponse
    {
        $user = $request->user();
        $data = $request->validated();

        $hotel = DB::transaction(function () use ($data, $user) {
            $servicio = Servicio::create([
                'proveedor_id' => $user->id,
                'nombre'       => $data['nombre'],
                'tipo'         => 'hotel',
                'descripcion'  => $data['descripcion'] ?? null,
                'ciudad'       => $data['ciudad'],
                'pais'         => $data['pais'],
                'imagen_url'   => $data['imagen_url'],
                'latitud'      => $data['latitud'] ?? null,
                'longitud'     => $data['longitud'] ?? null,
                'activo'       => $data['activo'] ?? true,
            ]);

            $this->guardarGaleria($servicio, $data['imagenes'] ?? []);

            $hotel = Hotel::create([
                'servicio_id' => $servicio->id,
                'direccion'   => $data['direccion'],
                'estrellas'   => $data['estrellas'],
            ]);

            foreach ($data['habitaciones'] as $hab) {
                Habitacion::create($this->soloCamposHabitacion($hab) + ['servicio_id' => $servicio->id]);
            }

            return $hotel;
        });

        $hotel->load('servicio:id,nombre,ciudad,pais,imagen_url', 'habitaciones');

        return response()->json([
            'message' => 'Hotel publicado correctamente.',
            'data'    => $hotel,
        ], 201);
    }

    /** PUT /api/hoteles/{servicio_id}  (TRV-502 / TRV-503) */
    public function update(UpdateHotelRequest $request, int $servicio_id): JsonResponse
    {
        $hotel = Hotel::with(['servicio', 'habitaciones'])->find($servicio_id);
        if (!$hotel) {
            return response()->json(['message' => 'Hotel no encontrado'], 404);
        }

        $data = $request->validated();

        if (array_key_exists('habitaciones', $data)) {
            $this->validarSincronizacion($hotel, $data['habitaciones']);
        }

        DB::transaction(function () use ($hotel, $data) {
            $hotelData = array_intersect_key($data, array_flip(['direccion', 'estrellas']));
            if ($hotelData) {
                $hotel->update($hotelData);
            }

            $servicioData = array_intersect_key($data, array_flip(['nombre', 'descripcion', 'ciudad', 'pais', 'imagen_url', 'activo', 'latitud', 'longitud']));
            if ($servicioData) {
                $hotel->servicio->update($servicioData);
            }

            if (array_key_exists('imagenes', $data)) {
                $hotel->servicio->imagenes()->delete();
                $this->guardarGaleria($hotel->servicio, $data['imagenes'] ?? []);
            }

            if (array_key_exists('habitaciones', $data)) {
                $this->sincronizarHabitaciones($hotel, $data['habitaciones']);
            }
        });

        $hotel->refresh()->load(['servicio.imagenes', 'habitaciones']);

        return response()->json([
            'message' => 'Hotel actualizado correctamente.',
            'data'    => [
                'hotel'        => $hotel,
                'servicio'     => $hotel->servicio,
                'habitaciones' => $hotel->habitaciones,
            ],
        ]);
    }

    /** DELETE /api/hoteles/{servicio_id}  (TRV-508) */
    public function destroy(Request $request, int $servicio_id): JsonResponse
    {
        $servicio = Servicio::where('tipo', 'hotel')->find($servicio_id);
        if (!$servicio) {
            return response()->json(['message' => 'Hotel no encontrado'], 404);
        }
        [$status, $body] = EliminacionServicio::eliminar($servicio, $request->user());

        return response()->json($body, $status);
    }

    // ------------------------------------------------------------------ helpers

    private function habitacionArray(Habitacion $h, int $libres): array
    {
        return [
            'id'                   => $h->id,
            'nombre'               => $h->nombre,
            'capacidad_adultos'    => (int) $h->capacidad_adultos,
            'capacidad_ninos'      => (int) $h->capacidad_ninos,
            'cantidad'             => (int) $h->cantidad,
            'precio_por_noche'     => (float) $h->precio_por_noche,
            'unidades_totales'     => (int) $h->cantidad,
            'unidades_disponibles' => $libres,
            'descripcion'          => $h->descripcion,
        ];
    }

    private function guardarGaleria(Servicio $servicio, array $imagenes): void
    {
        $imgs = collect($imagenes)->map(function ($item) {
            if (is_string($item)) {
                return ['url' => $item, 'alt' => null];
            }
            return ['url' => $item['url'] ?? null, 'alt' => $item['alt'] ?? null];
        })->filter(fn ($x) => !empty($x['url']))->values()->all();

        if ($imgs) {
            $servicio->imagenes()->createMany($imgs);
        }
    }

    private function soloCamposHabitacion(array $hab): array
    {
        return array_intersect_key($hab, array_flip([
            'nombre', 'capacidad_adultos', 'capacidad_ninos', 'precio_por_noche', 'cantidad', 'descripcion',
        ]));
    }

    /** Antes de tocar nada: no borrar tipos con reservas activas ni reducir por debajo de la ocupación. */
    private function validarSincronizacion(Hotel $hotel, array $entrantes): void
    {
        $idsEntrantes = collect($entrantes)->pluck('id')->filter()->map(fn ($x) => (int) $x)->all();
        $errores = [];

        foreach ($hotel->habitaciones as $h) {
            if (!in_array($h->id, $idsEntrantes, true) && Disponibilidad::tieneReservasActivas($h->id)) {
                $errores[] = "La habitación '{$h->nombre}' tiene reservas activas y no se puede eliminar.";
            }
        }
        foreach ($entrantes as $hab) {
            if (!empty($hab['id']) && isset($hab['cantidad'])) {
                $min = Disponibilidad::ocupacionFutura((int) $hab['id']);
                if ((int) $hab['cantidad'] < $min) {
                    $errores[] = "La habitación #{$hab['id']} tiene {$min} unidades comprometidas. Mínimo permitido: {$min}.";
                }
            }
            if (!empty($hab['id']) && !$hotel->habitaciones->contains('id', (int) $hab['id'])) {
                $errores[] = "La habitación #{$hab['id']} no pertenece a este hotel.";
            }
        }

        if ($errores) {
            abort(response()->json(['message' => $errores[0], 'errores' => $errores], 409));
        }
    }

    private function sincronizarHabitaciones(Hotel $hotel, array $entrantes): void
    {
        $sid = $hotel->servicio_id;
        $idsEntrantes = collect($entrantes)->pluck('id')->filter()->map(fn ($x) => (int) $x)->all();

        Habitacion::where('servicio_id', $sid)->whereNotIn('id', $idsEntrantes ?: [0])->get()->each->delete();

        foreach ($entrantes as $hab) {
            $payload = $this->soloCamposHabitacion($hab);
            if (!empty($hab['id'])) {
                Habitacion::where('id', $hab['id'])->where('servicio_id', $sid)->first()?->update($payload);
            } else {
                Habitacion::create($payload + ['servicio_id' => $sid]);
            }
        }
    }
}
