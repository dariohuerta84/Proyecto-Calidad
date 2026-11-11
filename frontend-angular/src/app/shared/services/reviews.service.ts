import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Review, ReviewCreate, ReviewUpdate, ReviewListResponse, ResumenResenas, PuedoOpinar } from '../models/review.model';

/**
 * TRV-403 — Reseñas.
 * Los errores HTTP se propagan sin transformar para que el componente muestre el mensaje de la API
 * (403 no elegible, 409 duplicada, 422 validación).
 */
@Injectable({ providedIn: 'root' })
export class ReviewsService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  listar(servicioId: number, page = 1, perPage = 5): Observable<ReviewListResponse> {
    const params = new HttpParams().set('servicio_id', servicioId).set('page', page).set('per_page', perPage);
    return this.http.get<ReviewListResponse>(`${this.api}/reviews`, { params });
  }

  /** Promedio y total de TODAS las reseñas (no solo la página visible). */
  resumen(servicioId: number): Observable<ResumenResenas> {
    return this.http.get<{ data: ResumenResenas }>(`${this.api}/reviews/resumen`, { params: { servicio_id: servicioId } }).pipe(map(r => r.data));
  }

  /** Requiere sesión: indica si el usuario puede opinar (reserva confirmada y estadía/salida pasada). */
  puedoOpinar(servicioId: number): Observable<PuedoOpinar> {
    return this.http.get<{ data: PuedoOpinar }>(`${this.api}/reviews/puedo-opinar`, { params: { servicio_id: servicioId } }).pipe(map(r => r.data));
  }

  crear(review: ReviewCreate): Observable<Review> {
    return this.http.post<{ data: Review }>(`${this.api}/reviews`, review).pipe(map(r => r.data));
  }

  actualizar(id: number, cambios: ReviewUpdate): Observable<Review> {
    return this.http.put<{ data: Review }>(`${this.api}/reviews/${id}`, cambios).pipe(map(r => r.data));
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/reviews/${id}`);
  }

  textoCalificacion(promedio: number): string {
    if (promedio >= 4.5) return 'Excelente';
    if (promedio >= 4) return 'Muy bueno';
    if (promedio >= 3) return 'Bueno';
    if (promedio >= 2) return 'Regular';
    if (promedio > 0) return 'Malo';
    return 'Sin calificación';
  }
}
