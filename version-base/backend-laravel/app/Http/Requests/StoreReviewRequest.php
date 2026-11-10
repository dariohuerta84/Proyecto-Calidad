<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use App\Models\ReservaHabitacion;
use App\Models\ReservaTour;
use App\Models\Servicio;
use Carbon\Carbon;

class StoreReviewRequest extends FormRequest
{
    /**
     * Determina si el usuario está autorizado para hacer esta solicitud.
     * Valida que:
     * 1. El usuario esté autenticado
     * 2. El servicio exista
     * 3. El usuario haya reservado el servicio
     * 4. La reserva haya finalizado (fecha_fin < hoy)
     */
    public function authorize(): bool
    {
        // Debe estar autenticado
        if (!auth()->check()) {
            return false;
        }

        $userId = auth()->id();
        $servicioId = $this->input('servicio_id');

        // Validar que el servicio existe
        $servicio = Servicio::find($servicioId);
        if (!$servicio) {
            return false;
        }

        $hoy = Carbon::now()->startOfDay();

        // Si es hotel, buscar en reservas de habitaciones
        if ($servicio->tipo === 'hotel') {
            $tieneReservaCompletada = ReservaHabitacion::where('usuario_id', $userId)
                ->whereHas('habitacion', function ($q) use ($servicioId) {
                    $q->where('servicio_id', $servicioId);
                })
                ->whereIn('estado', ['confirmada'])
                ->whereDate('fecha_fin', '<', $hoy)
                ->exists();

            if (!$tieneReservaCompletada) {
                return false;
            }
        }
        // Si es tour, buscar en reservas de tours
        elseif ($servicio->tipo === 'tour') {
            $tieneReservaCompletada = ReservaTour::where('usuario_id', $userId)
                ->whereHas('salida', function ($q) use ($servicioId, $hoy) {
                    $q->where('servicio_id', $servicioId)
                      ->whereDate('fecha', '<', $hoy);
                })
                ->whereIn('estado', ['confirmada'])
                ->exists();

            if (!$tieneReservaCompletada) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array<string, \Illuminate\Contracts\Validation\ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'servicio_id' => 'required|exists:servicios,id',
            'comentario'  => 'required|string|max:300',
            'calificacion'=> 'required|integer|min:1|max:5',
        ];
    }

    /**
     * Mensajes de error personalizados
     */
    public function messages(): array
    {
        return [
            'servicio_id.required' => 'El servicio es obligatorio.',
            'servicio_id.exists' => 'El servicio seleccionado no existe.',
            'comentario.required' => 'El comentario es obligatorio.',
            'comentario.max' => 'El comentario no puede exceder los 300 caracteres.',
            'calificacion.required' => 'La calificación es obligatoria.',
            'calificacion.integer' => 'La calificación debe ser un número entero.',
            'calificacion.min' => 'La calificación mínima es 1.',
            'calificacion.max' => 'La calificación máxima es 5.',
        ];
    }

    /**
     * Mensajes de error de autorización
     */
    protected function failedAuthorization()
    {
        abort(403, 'No puedes crear una reseña para este servicio. Debes tener una reserva confirmada y completada.');
    }
}
