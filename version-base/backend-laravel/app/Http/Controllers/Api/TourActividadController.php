<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Servicio;
use App\Models\TourActividad;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class TourActividadController extends Controller
{
    /** GET /api/tours/{tour}/actividades  (público) */
    public function index($tour)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);
        return $serv->actividades()->orderBy('orden')->get();
    }

    /** POST /api/tours/{tour}/actividades  (proveedor dueño) */
    public function store(Request $req, $tour)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);
        $this->assertOwnership($serv);

        $data = $req->validate([
            'titulo'        => ['required','string','max:150'],
            'descripcion'   => ['nullable','string'],
            'orden'         => ['nullable','integer','min:1'],
            'duracion_min'  => ['nullable','integer','min:0'],
            'direccion'     => ['nullable','string','max:255'],
            'imagen_url'    => ['nullable','url'],
        ]);

        $act = $serv->actividades()->create([
            'titulo'       => $data['titulo'],
            'descripcion'  => $data['descripcion'] ?? null,
            'orden'        => $data['orden'] ?? 1,
            'duracion_min' => $data['duracion_min'] ?? null,
            'direccion'    => $data['direccion'] ?? null,
            'imagen_url'   => $data['imagen_url'] ?? null,
        ]);

        return response()->json($act, 201);
    }

    /** PUT/PATCH /api/tours/{tour}/actividades/{actividad}  (proveedor dueño) */
    public function update(Request $req, $tour, $actividad)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);
        $this->assertOwnership($serv);

        $act = TourActividad::where('id',$actividad)->where('servicio_id',$serv->id)->firstOrFail();

        $data = $req->validate([
            'titulo'        => ['sometimes','string','max:150'],
            'descripcion'   => ['sometimes','nullable','string'],
            'orden'         => ['sometimes','integer','min:1'],
            'duracion_min'  => ['sometimes','nullable','integer','min:0'],
            'direccion'     => ['sometimes','nullable','string','max:255'],
            'imagen_url'    => ['sometimes','nullable','url'],
        ]);

        $act->fill($data)->save();
        return $act;
    }

    /** DELETE /api/tours/{tour}/actividades/{actividad}  (proveedor dueño) */
    public function destroy($tour, $actividad)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);
        $this->assertOwnership($serv);

        $act = TourActividad::where('id',$actividad)->where('servicio_id',$serv->id)->firstOrFail();
        $act->delete();

        return response()->json(['message' => 'Actividad eliminada']);
    }

    private function assertOwnership(Servicio $serv): void
    {
        $user = Auth::user();
        if (!$user || $user->rol !== 'proveedor' || (int)$user->id !== (int)$serv->proveedor_id) {
            abort(403, 'No autorizado: no eres el proveedor dueño de este tour.');
        }
    }
}
