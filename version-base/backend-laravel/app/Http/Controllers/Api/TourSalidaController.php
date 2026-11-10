<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Servicio;
use App\Models\TourSalida;
use App\Models\ReservaTour;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\ValidationException;

class TourSalidaController extends Controller
{
    /** GET /api/tours/{tour}/salidas?desde=&hasta=&estado=  (público) */
    public function index(Request $req, $tour)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);

        $q = $serv->salidas()->newQuery();
        if ($req->filled('estado')) $q->where('estado', $req->query('estado'));
        if ($req->filled('desde'))  $q->whereDate('fecha','>=',$req->query('desde'));
        if ($req->filled('hasta'))  $q->whereDate('fecha','<=',$req->query('hasta'));

        return $q->orderBy('fecha')->orderBy('hora')->get()->map(function($s){
            return [
                'id' => $s->id,
                'servicio_id' => $s->servicio_id,
                'fecha' => $s->fecha?->format('Y-m-d'),
                'hora' => $s->hora?->format('H:i'),
                'cupo_total' => (int)$s->cupo_total,
                'cupo_reservado' => (int)$s->cupo_reservado,
                'cupo_disponible' => (int)max(0, $s->cupo_total - $s->cupo_reservado),
                'estado' => $s->estado,
            ];
        });
    }

    /** POST /api/tours/{tour}/salidas  (proveedor dueño) */
    public function store(Request $req, $tour)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);
        $this->assertOwnership($serv);

        $data = $req->validate([
            'fecha'      => ['required','date'],
            'hora'       => ['required','date_format:H:i'],
            'cupo_total' => ['nullable','integer','min:1'],
            'estado'     => ['sometimes','in:programada,cerrada,cancelada'],
        ]);

        $cupo = (int)($data['cupo_total'] ?? $serv->tour?->capacidad_por_salida ?? 0);
        if ($cupo < 1) {
            throw ValidationException::withMessages([
                'cupo_total' => 'Debe especificar cupo_total o configurar capacidad_por_salida en el tour.'
            ]);
        }

        try {
            $salida = $serv->salidas()->create([
                'fecha' => $data['fecha'],
                'hora'  => $data['hora'],
                'cupo_total' => $cupo,
                'estado' => $data['estado'] ?? 'programada',
            ]);
        } catch (\Illuminate\Database\QueryException $e) {
            throw ValidationException::withMessages([
                'fecha' => 'Ya existe una salida programada para esa fecha y hora.'
            ]);
        }

        return response()->json($salida, 201);
    }

    /** PUT/PATCH /api/tours/{tour}/salidas/{salida}  (proveedor dueño) */
    public function update(Request $req, $tour, $salida)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);
        $this->assertOwnership($serv);

        $sal = TourSalida::where('id',$salida)->where('servicio_id',$serv->id)->firstOrFail();

        $data = $req->validate([
            'fecha'      => ['sometimes','date'],
            'hora'       => ['sometimes','date_format:H:i'],
            'cupo_total' => ['sometimes','integer','min:1'],
            'estado'     => ['sometimes','in:programada,cerrada,cancelada'],
        ]);

        if (array_key_exists('cupo_total', $data) && $data['cupo_total'] < $sal->cupo_reservado) {
            throw ValidationException::withMessages([
                'cupo_total' => 'No puedes establecer un cupo_total menor a lo ya reservado.'
            ]);
        }

        $sal->fill($data)->save();
        return $sal;
    }

    /** DELETE /api/tours/{tour}/salidas/{salida}  (proveedor dueño) */
    public function destroy($tour, $salida)
    {
        $serv = Servicio::where('tipo','tour')->findOrFail($tour);
        $this->assertOwnership($serv);

        $sal = TourSalida::where('id',$salida)->where('servicio_id',$serv->id)->firstOrFail();

        // Bloquear si tiene reservas confirmadas
        if (ReservaTour::where('salida_id',$sal->id)->where('estado','confirmada')->exists()) {
            throw ValidationException::withMessages([
                'salida' => 'No se puede eliminar una salida con reservas confirmadas.'
            ]);
        }

        $sal->delete();
        return response()->json(['message' => 'Salida eliminada']);
    }

    private function assertOwnership(Servicio $serv): void
    {
        $user = Auth::user();
        if (!$user || $user->rol !== 'proveedor' || (int)$user->id !== (int)$serv->proveedor_id) {
            abort(403, 'No autorizado: no eres el proveedor dueño de este tour.');
        }
    }
}
