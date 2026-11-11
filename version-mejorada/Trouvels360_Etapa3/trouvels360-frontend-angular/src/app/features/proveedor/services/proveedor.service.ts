import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Paginado, ReservaCualquiera, ReservaHotel } from '../../../shared/models/reserva.model';
import { ServiceData } from '../../../shared/models';

export interface Indicadores {
  publicaciones: number;
  hoteles: number;
  tours: number;
  solicitudes_pendientes: number;
  reservas_hotel: Record<'pendiente' | 'confirmada' | 'rechazada' | 'cancelada', number>;
  reservas_tour: Record<'pendiente' | 'confirmada' | 'rechazada' | 'cancelada', number>;
  ingresos_confirmados_mes: number;
  ingresos_confirmados_total: number;
  resenas: { cantidad: number; promedio: number };
}

export interface Salida {
  id: number; servicio_id: number; fecha: string; hora: string;
  cupo_total: number; cupo_reservado: number; cupo_disponible: number;
  estado: 'programada' | 'cerrada' | 'cancelada'; reservas_confirmadas: number;
}

export interface Actividad {
  id: number; servicio_id: number; titulo: string; descripcion: string | null;
  orden: number; duracion_min: number | null;
}

/** Portal del proveedor — TRV-204, 501, 503, 505, 506, 507, 508. */
@Injectable({ providedIn: 'root' })
export class ProveedorService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  private params(obj: Record<string, any>): HttpParams {
    let p = new HttpParams();
    Object.entries(obj).forEach(([k, v]) => { if (v !== null && v !== undefined && v !== '') p = p.set(k, String(v)); });
    return p;
  }

  // ---------------- Panel (TRV-501)
  servicios(page = 1, tipo = '', search = '', perPage = 15): Observable<Paginado<ServiceData>> {
    return this.http.get<Paginado<ServiceData>>(`${this.api}/proveedor/servicios`, {
      params: this.params({ page, tipo, search, per_page: perPage }),
    });
  }

  indicadores(): Observable<Indicadores> {
    return this.http.get<{ data: Indicadores }>(`${this.api}/proveedor/indicadores`).pipe(map(r => r.data));
  }

  eliminarServicio(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.api}/servicios/${id}`);
  }

  // ---------------- Reservas recibidas y aprobación (TRV-507 / TRV-204)
  reservas(filtros: { tipo?: string; estado?: string; servicio_id?: number | string; page?: number; per_page?: number }): Observable<Paginado<ReservaCualquiera>> {
    return this.http.get<Paginado<ReservaCualquiera>>(`${this.api}/proveedor/reservas`, { params: this.params(filtros) });
  }

  aceptar(id: number): Observable<{ message: string; data: ReservaHotel }> {
    return this.http.post<{ message: string; data: ReservaHotel }>(`${this.api}/proveedor/reservas-habitaciones/${id}/aceptar`, {});
  }

  rechazar(id: number, motivo: string): Observable<{ message: string; data: ReservaHotel }> {
    return this.http.post<{ message: string; data: ReservaHotel }>(`${this.api}/proveedor/reservas-habitaciones/${id}/rechazar`, { motivo });
  }

  // ---------------- Salidas (TRV-506)
  salidas(tourId: number): Observable<Salida[]> {
    return this.http.get<Salida[]>(`${this.api}/tours/${tourId}/salidas`);
  }
  crearSalida(tourId: number, s: { fecha: string; hora: string; cupo_total: number }): Observable<Salida> {
    return this.http.post<Salida>(`${this.api}/tours/${tourId}/salidas`, s);
  }
  actualizarSalida(tourId: number, id: number, cambios: Partial<Pick<Salida, 'fecha' | 'hora' | 'cupo_total' | 'estado'>>): Observable<Salida> {
    return this.http.put<Salida>(`${this.api}/tours/${tourId}/salidas/${id}`, cambios);
  }
  eliminarSalida(tourId: number, id: number): Observable<any> {
    return this.http.delete(`${this.api}/tours/${tourId}/salidas/${id}`);
  }

  // ---------------- Actividades (TRV-505)
  actividades(tourId: number): Observable<Actividad[]> {
    return this.http.get<Actividad[]>(`${this.api}/tours/${tourId}/actividades`);
  }
  crearActividad(tourId: number, a: Partial<Actividad>): Observable<Actividad> {
    return this.http.post<Actividad>(`${this.api}/tours/${tourId}/actividades`, a);
  }
  actualizarActividad(tourId: number, id: number, a: Partial<Actividad>): Observable<Actividad> {
    return this.http.put<Actividad>(`${this.api}/tours/${tourId}/actividades/${id}`, a);
  }
  eliminarActividad(tourId: number, id: number): Observable<any> {
    return this.http.delete(`${this.api}/tours/${tourId}/actividades/${id}`);
  }
}
