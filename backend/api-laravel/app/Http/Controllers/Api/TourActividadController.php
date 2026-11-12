<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Servicio;
use App\Models\TourActividad;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;

/** HU-P10 — Programa (actividades ordenadas) del tour. */
class TourActividadController extends Controller
{
    /** GET /api/tours/{tour}/actividades  (público) */
    public function index($tour): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);

        return response()->json(TourActividad::where('servicio_id', $serv->id)->orderBy('orden')->get());
    }

    private function reglas(bool $crear): array
    {
        $req = $crear ? 'required' : 'sometimes';

        return [
            'titulo'       => [$req, 'string', 'max:150'],
            'descripcion'  => ['sometimes', 'nullable', 'string', 'max:2000'],
            'orden'        => ['sometimes', 'nullable', 'integer', 'min:1', 'max:500'],
            'duracion_min' => ['sometimes', 'nullable', 'integer', 'min:0', 'max:1440'],
            'direccion'    => ['sometimes', 'nullable', 'string', 'max:255'],
            'imagen_url'   => ['sometimes', 'nullable', 'url', 'max:500'],
        ];
    }

    /** Abre un hueco en $orden desplazando las siguientes (+1). */
    private function abrirHueco(int $servicioId, int $orden, ?int $excluirId = null): void
    {
        $siguientes = TourActividad::where('servicio_id', $servicioId)
            ->where('orden', '>=', $orden)
            ->when($excluirId, fn ($q) => $q->where('id', '!=', $excluirId))
            ->orderByDesc('orden')->get();
        foreach ($siguientes as $a) { // de mayor a menor para no chocar con el índice único
            $a->update(['orden' => $a->orden + 1]);
        }
    }

    /** POST /api/tours/{tour}/actividades — sin 'orden' va al final. */
    public function store(Request $req, $tour): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $this->assertOwnership($serv);
        $data = $req->validate($this->reglas(true));

        $act = DB::transaction(function () use ($serv, $data) {
            $max = (int) TourActividad::where('servicio_id', $serv->id)->max('orden');
            $orden = isset($data['orden']) ? min((int) $data['orden'], $max + 1) : $max + 1;
            if ($orden <= $max) {
                $this->abrirHueco($serv->id, $orden);
            }

            return TourActividad::create([
                'servicio_id'  => $serv->id,
                'titulo'       => $data['titulo'],
                'descripcion'  => $data['descripcion'] ?? null,
                'orden'        => $orden,
                'duracion_min' => $data['duracion_min'] ?? null,
                'direccion'    => $data['direccion'] ?? null,
                'imagen_url'   => $data['imagen_url'] ?? null,
            ]);
        });

        return response()->json($act, 201);
    }

    /** PUT/PATCH /api/tours/{tour}/actividades/{actividad} */
    public function update(Request $req, $tour, $actividad): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $this->assertOwnership($serv);
        $act = TourActividad::where('id', $actividad)->where('servicio_id', $serv->id)->firstOrFail();
        $data = $req->validate($this->reglas(false));

        DB::transaction(function () use ($serv, $act, $data) {
            if (isset($data['orden']) && (int) $data['orden'] !== (int) $act->orden) {
                // Mover = sacar de su posición (cerrar hueco) y abrir hueco en la nueva.
                $anterior = (int) $act->orden;
                $act->update(['orden' => 0]); // aparta temporalmente (índice único)
                TourActividad::where('servicio_id', $serv->id)->where('id', '!=', $act->id)
                    ->where('orden', '>', $anterior)->orderBy('orden')->get()
                    ->each(fn ($a) => $a->update(['orden' => $a->orden - 1]));
                $max = (int) TourActividad::where('servicio_id', $serv->id)->where('id', '!=', $act->id)->max('orden');
                $data['orden'] = max(1, min((int) $data['orden'], $max + 1));
                $this->abrirHueco($serv->id, $data['orden'], $act->id);
            }
            $act->fill($data)->save();
        });

        return response()->json($act->fresh());
    }

    /** DELETE /api/tours/{tour}/actividades/{actividad} — no afecta salidas ni reservas. */
    public function destroy($tour, $actividad): JsonResponse
    {
        $serv = Servicio::where('tipo', 'tour')->findOrFail($tour);
        $this->assertOwnership($serv);
        $act = TourActividad::where('id', $actividad)->where('servicio_id', $serv->id)->firstOrFail();

        DB::transaction(function () use ($serv, $act) {
            $orden = $act->orden;
            $act->forceDelete(); // libera el orden (índice único)
            TourActividad::where('servicio_id', $serv->id)->where('orden', '>', $orden)->orderBy('orden')->get()
                ->each(fn ($a) => $a->update(['orden' => $a->orden - 1]));
        });

        return response()->json(['message' => 'Actividad eliminada']);
    }

    private function assertOwnership(Servicio $serv): void
    {
        $user = Auth::user();
        if (!$user || $user->rol !== 'proveedor' || (int) $user->id !== (int) $serv->proveedor_id) {
            abort(response()->json(['message' => 'No autorizado: no eres el proveedor dueño de este tour.'], 403));
        }
    }
}
