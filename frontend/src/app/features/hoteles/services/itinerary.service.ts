import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/** HU-V15 — Itinerario sugerido (Laravel firma un JWT y consulta al microservicio Python). */
@Injectable({ providedIn: 'root' })
export class ItineraryService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/itineraries`;

  generarItinerario(datos: any): Observable<any> {
    return this.http.post(this.apiUrl, datos);
  }
}
