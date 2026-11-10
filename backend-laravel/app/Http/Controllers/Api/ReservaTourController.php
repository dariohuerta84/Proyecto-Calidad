<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ReservaTour;
use App\Models\Servicio;
use App\Models\TourSalida;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class ReservaTourController extends Controller
{
    /** POST /api/tours/salidas/{salida}/reservas  (viajero autenticado) */
    public function store(Request $req, $salida)
    {
        $user = Auth::user();
        if (!$user || $user->rol !== 'viajero') {
            abort(403, 'Solo viajeros pueden crear reservas de tour.');
        }

        $data = $req->validate([
            'personas' => ['required','integer','min:1','max:1000'],
        ]);

        $reserva = DB::transaction(function() use ($salida, $data, $user) {
            // Lock para evitar sobreventa
            $sal = TourSalida::where('id',$salida)->lockForUpdate()->firstOrFail();

            if ($sal->estado !== 'programada') {
                throw ValidationException::withMessages(['salida' => 'La salida no está disponible para reservar.']);
            }

            $disponible = $sal->cupo_total - $sal->cupo_reservado;
            if ($data['personas'] > $disponible) {
                throw ValidationException::withMessages([
                    'personas' => "Cupo insuficiente. Disponible: $disponible"
                ]);
            }

            // Precio snapshot desde tour (servicio->tour)
            $serv = Servicio::with('tour')->findOrFail($sal->servicio_id);
            $precioUnit = (float)$serv->tour->precio_persona;

            // Actualiza ocupación
            $sal->cupo_reservado += $data['personas'];
            $sal->save();

            // Código de reserva
            $codigo = strtoupper(bin2hex(random_bytes(5)));

            return ReservaTour::create([
                'codigo_reserva' => $codigo,
                'usuario_id'     => $user->id,
                'salida_id'      => $sal->id,
                'personas'       => $data['personas'],
                'estado'         => 'confirmada', // o 'pendiente' si integras pagos
                'precio_unitario'=> $precioUnit,
                'total'          => round($precioUnit * $data['personas'], 2),
            ]);
        });

        return response()->json([
            'reserva_id' => $reserva->id,
            'codigo'     => $reserva->codigo_reserva,
            'total'      => (float)$reserva->total,
            'estado'     => $reserva->estado,
        ], 201);
    }

    /** POST /api/tours/reservas/{reserva}/cancelar  (dueño de la reserva) */
    public function cancelar($reserva)
    {
        $user = Auth::user();
        if (!$user) abort(401);

        $res = ReservaTour::with('salida')->findOrFail($reserva);
        if ((int)$res->usuario_id !== (int)$user->id) {
            abort(403, 'No autorizado: no eres el dueño de esta reserva.');
        }

        if ($res->estado === 'cancelada') {
            return response()->json(['message' => 'La reserva ya está cancelada.']);
        }

        DB::transaction(function() use ($res) {
            // Liberar cupo sólo si estaba confirmada
            if ($res->estado === 'confirmada') {
                $sal = TourSalida::where('id',$res->salida_id)->lockForUpdate()->first();
                if ($sal && $sal->cupo_reservado >= $res->personas) {
                    $sal->cupo_reservado -= $res->personas;
                    $sal->save();
                }
            }
            $res->estado = 'cancelada';
            $res->save();
        });

        return response()->json(['message' => 'Reserva cancelada']);
    }

    /** GET /api/tours/mis-reservas  (viajero autenticado) */
    public function misReservas(Request $req)
    {
        $user = Auth::user();
        if (!$user || $user->rol !== 'viajero') abort(403);

        $q = ReservaTour::query()
            ->where('usuario_id',$user->id)
            ->with(['salida' => function($s){
                $s->select(['id','servicio_id','fecha','hora','cupo_total','cupo_reservado','estado']);
            }]);

        if ($req->filled('estado')) $q->where('estado', $req->query('estado'));

        return $q->orderByDesc('created_at')->paginate(20);
    }
}
