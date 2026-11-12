import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  CotizacionHotel, CotizacionTour, ItemHabitacion, PagoSimulado, Paginado,
  ReservaHotel, ReservaTour, SolicitudHotelCreada,
} from '../../../shared/models/reserva.model';

export type { ReservaHabitacionPayload, ReservaApiRespuesta, MisReservasHotelItem } from '../../../shared/models/reserva.model';

/**
 * Reservas del viajero (hotel y tour) — HU-V06, 203, 302, 303, 401, 402.
 * Los importes (subtotal, IGV 18%, total) siempre los calcula la API.
 */
@Injectable({ providedIn: 'root' })
export class ReservasService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  private params(obj: Record<string, any>): HttpParams {
    let p = new HttpParams();
    Object.entries(obj).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') p = p.set(k, String(v));
    });
    return p;
  }

  // ---------------------------------------------------------------- Hotel
  cotizarHotel(fechaInicio: string, fechaFin: string, items: ItemHabitacion[]): Observable<CotizacionHotel> {
    return this.http.post<{ data: CotizacionHotel }>(`${this.api}/reservas-habitaciones/cotizar`, {
      fecha_inicio: fechaInicio, fecha_fin: fechaFin, items,
    }).pipe(map(r => r.data));
  }

  /** Crea la solicitud PENDIENTE. La misma clave de idempotencia en un reintento no duplica la reserva. */
  crearReservaHotel(fechaInicio: string, fechaFin: string, items: ItemHabitacion[], pago: PagoSimulado, claveIdempotencia: string)
    : Observable<{ message: string; data: SolicitudHotelCreada }> {
    return this.http.post<{ message: string; data: SolicitudHotelCreada }>(`${this.api}/reservas-habitaciones`,
      { fecha_inicio: fechaInicio, fecha_fin: fechaFin, items, pago },
      { headers: { 'Idempotency-Key': claveIdempotencia } });
  }

  misReservasHotel(page = 1, estado?: string, perPage = 10): Observable<Paginado<ReservaHotel>> {
    return this.http.get<Paginado<ReservaHotel>>(`${this.api}/mis-reservas`, { params: this.params({ page, estado, per_page: perPage }) });
  }

  reservaHotel(id: number): Observable<ReservaHotel> {
    return this.http.get<{ data: ReservaHotel }>(`${this.api}/mis-reservas/${id}`).pipe(map(r => r.data));
  }

  cancelarReservaHotel(id: number): Observable<{ message: string; data: ReservaHotel }> {
    return this.http.post<{ message: string; data: ReservaHotel }>(`${this.api}/reservas-habitaciones/${id}/cancelar`, {});
  }

  // ---------------------------------------------------------------- Tour
  cotizarTour(salidaId: number, personas: number): Observable<CotizacionTour> {
    return this.http.post<{ data: CotizacionTour }>(`${this.api}/tours/salidas/${salidaId}/cotizar`, { personas })
      .pipe(map(r => r.data));
  }

  crearReservaTour(salidaId: number, personas: number, pago: PagoSimulado, claveIdempotencia: string)
    : Observable<{ message: string; codigo: string; estado: string; total: number; data: ReservaTour }> {
    return this.http.post<any>(`${this.api}/tours/salidas/${salidaId}/reservas`, { personas, pago },
      { headers: { 'Idempotency-Key': claveIdempotencia } });
  }

  misReservasTour(page = 1, estado?: string, perPage = 10): Observable<Paginado<ReservaTour>> {
    return this.http.get<Paginado<ReservaTour>>(`${this.api}/tours/mis-reservas`, { params: this.params({ page, estado, per_page: perPage }) });
  }

  cancelarReservaTour(id: number): Observable<{ message: string; data: ReservaTour }> {
    return this.http.post<{ message: string; data: ReservaTour }>(`${this.api}/tours/reservas/${id}/cancelar`, {});
  }
}
