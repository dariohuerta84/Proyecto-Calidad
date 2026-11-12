<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ReservaTour;
use App\Models\TourSalida;
use App\Services\ReservaPresenter;
use App\Support\PagoSimulado;
use App\Support\Precio;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

/** HU-V10 (reservar con confirmación automática), HU-V11 (mis reservas), HU-V13 (cancelar). */
class ReservaTourController extends Controller
{
    /** POST /api/tours/salidas/{salida}/reservas  (Idempotency-Key) */
    public function store(Request $req, int $salida): JsonResponse
    {
        $user = $req->user();
        if (!$user->esViajero()) {
            return response()->json(['message' => 'Solo las cuentas de viajero pueden reservar tours.'], 403);
        }

        $data = $req->validate(
            ['personas' => ['required', 'integer', 'min:1', 'max:50']] + PagoSimulado::reglas(),
            PagoSimulado::mensajes()
        );

        return DB::transaction(function () use ($salida, $data, $user) {
            // Bloqueo de fila: evita sobreventa con reservas simultáneas
            $sal = TourSalida::where('id', $salida)->lockForUpdate()->first();
            if (!$sal) {
                return response()->json(['message' => 'Salida no encontrada.'], 404);
            }
            $serv = $sal->servicio()->with('tour')->first();
            if (!$serv || !$serv->activo) {
                return response()->json(['message' => 'El tour ya no está disponible.'], 409);
            }
            if ($sal->estado !== 'programada') {
                return response()->json(['message' => "La salida no admite reservas (estado: {$sal->estado}).", 'estado' => $sal->estado], 409);
            }
            if ($sal->yaPaso()) {
                return response()->json(['message' => 'La salida ya ocurrió.'], 409);
            }
            $disponible = $sal->cupo_total - $sal->cupo_reservado;
            if ($data['personas'] > $disponible) {
                return response()->json([
                    'message' => "Cupo insuficiente. Disponible: {$disponible}.",
                    'cupos_disponibles' => $disponible,
                ], 409);
            }

            $precio = (float) $serv->tour->precio;
            $t = Precio::desdeSubtotalCentimos(Precio::lineaCentimos($precio, (int) $data['personas']));

            $sal->cupo_reservado += $data['personas'];
            $sal->save();

            $reserva = ReservaTour::create([
                'codigo_reserva'   => $this->codigoUnico(),
                'usuario_id'       => $user->id,
                'salida_id'        => $sal->id,
                'personas'         => $data['personas'],
                'estado'           => 'confirmada', // confirmación automática
                'precio_unitario'  => $precio,
                'subtotal'         => $t['subtotal'],
                'impuesto'         => $t['impuesto'],
                'total'            => $t['total'],
                'tarjeta_ultimos4' => PagoSimulado::ultimos4($data['pago']),
            ]);

            return response()->json([
                'message'    => 'Reserva confirmada.',
                // campos planos por compatibilidad con clientes anteriores
                'reserva_id' => $reserva->id,
                'codigo'     => $reserva->codigo_reserva,
                'estado'     => $reserva->estado,
                'subtotal'   => $t['subtotal'],
                'impuesto'   => $t['impuesto'],
                'total'      => $t['total'],
                'data'       => ReservaPresenter::tour($reserva->fresh()),
            ], 201);
        });
    }

    /** POST /api/tours/reservas/{reserva}/cancelar */
    public function cancelar(Request $req, int $reserva): JsonResponse
    {
        $user = $req->user();

        return DB::transaction(function () use ($user, $reserva) {
            $res = ReservaTour::where('id', $reserva)->where('usuario_id', $user->id)->lockForUpdate()->first();
            if (!$res) {
                return response()->json(['message' => 'Reserva no encontrada.'], 404);
            }
            if ($res->estado === 'cancelada') {
                return response()->json(['message' => 'La reserva ya está cancelada.', 'estado' => $res->estado], 409);
            }
            $sal = TourSalida::withTrashed()->where('id', $res->salida_id)->lockForUpdate()->first();
            if ($sal && $sal->yaPaso()) {
                return response()->json(['message' => 'La salida ya ocurrió; no se puede cancelar.', 'estado' => $res->estado], 409);
            }

            // Devuelve exactamente sus personas al cupo, una sola vez.
            if ($res->estado === 'confirmada' && $sal) {
                $sal->cupo_reservado = max(0, $sal->cupo_reservado - $res->personas);
                $sal->save();
            }
            $res->update(['estado' => 'cancelada', 'cancelado_en' => now()]);

            return response()->json(['message' => 'Reserva cancelada', 'data' => ReservaPresenter::tour($res->fresh())]);
        });
    }

    /** GET /api/tours/mis-reservas?estado=&page=&per_page= */
    public function misReservas(Request $req): JsonResponse
    {
        $user = $req->user();
        if (!$user->esViajero()) {
            return response()->json(['message' => 'Solo para cuentas de viajero.'], 403);
        }
        $v = $req->validate([
            'estado'   => ['nullable', 'in:pendiente,confirmada,cancelada'],
            'per_page' => ['nullable', 'integer', 'between:1,50'],
        ]);

        $pag = ReservaTour::with('salida')
            ->where('usuario_id', $user->id)
            ->when($v['estado'] ?? null, fn ($q, $e) => $q->where('estado', $e))
            ->orderByDesc('created_at')->orderByDesc('id')
            ->paginate((int) ($v['per_page'] ?? 10));

        return response()->json(ReservaPresenter::paginado($pag, collect($pag->items())->map(fn ($r) => ReservaPresenter::tour($r))->all()));
    }

    private function codigoUnico(): string
    {
        do {
            $c = 'T' . strtoupper(Str::random(9));
        } while (ReservaTour::withTrashed()->where('codigo_reserva', $c)->exists());

        return $c;
    }
}
