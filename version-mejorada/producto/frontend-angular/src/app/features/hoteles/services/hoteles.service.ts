import { Injectable, inject } from '@angular/core';
// 🚨 CAMBIO: HttpHeaders ya no es necesario para la autorización, solo para 'Accept'
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http'; 
import { Observable, forkJoin, map, catchError, throwError, switchMap } from 'rxjs'; 
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service'; 

// ==========================================================
// MODELOS IMPORTADOS DESDE SHARED (Se mantienen)
// ==========================================================
import {
  Habitacion,
  HabitacionCreatePayload,
  HotelData,
  ServiceData,
  HotelDetalles,
  HotelListApiRespuesta,
  SupplierHotelListApiRespuesta,
  HotelCreatePayload,
  HotelCreateResponse,
  HabitacionUpdatePayload
} from '../../../shared/models';

// Re-exportamos las interfaces para mantener backward compatibility
export type {
  Habitacion,
  HabitacionCreatePayload,
  HotelData,
  ServiceData,
  HotelDetalles,
  HotelListApiRespuesta,
  SupplierHotelListApiRespuesta,
  HotelCreatePayload,
  HotelCreateResponse,
  HabitacionUpdatePayload
};

// ==========================================================
// 2. SERVICIO PRINCIPAL
// ==========================================================
@Injectable({
  providedIn: 'root'
})
export class HotelService {
  private http = inject(HttpClient);
  // Mantemos la inyección de AuthService por si se usa isLoggedIn() o datos de usuario
  private auth = inject(AuthService); 
  private readonly API_URL = environment.apiUrl;

  /**
   * 🚨 CAMBIO CRÍTICO: Simplificamos getHeaders(). 
   * Ya NO incluye 'Authorization: Bearer'. 
   * El Interceptor se encarga de la autenticación vía Cookies/CSRF.
   */
  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Accept': 'application/json' 
      // Se ELIMINA: (token ? { 'Authorization': `Bearer ${token}` } : {})
    });
  }
  
  // ==========================================================
  // 3. MÉTODOS
  // ==========================================================
  
  /**
   * NUEVO: Crea una o varias habitaciones para un servicio de hotel dado.
   */
  createHabitaciones(servicioId: number, habitaciones: HabitacionCreatePayload[]): Observable<any> {
    
    const creationRequests = habitaciones.map(habitacion => {
      const endpoint = `${this.API_URL}/hoteles/${servicioId}/habitaciones`; 
      
      // Asegurar que `descripcion` siempre sea un string (evitar undefined/null)
      const body = {
        ...habitacion,
        descripcion: (habitacion.descripcion || '').toString()
      };
      console.log('[SERVICE] Creando habitación:', endpoint, body);
      // 🚨 CAMBIO: getHeaders() ya no tiene token, pero lo enviamos por consistencia
      return this.http.post(endpoint, body, { headers: this.getHeaders() });
    });

    return forkJoin(creationRequests).pipe(
      catchError(error => {
        console.error('Error al crear una o más habitaciones (petición individual falló):', error);
        return throwError(() => new Error('Error al registrar las habitaciones. Revise la consola.'));
      })
    );
  }

  /**
   * Crea el hotel con sus habitaciones en UNA petición (TRV-502).
   * La API lo hace en una transacción: o se crea todo o nada.
   */
  createHotelWithHabitaciones(payload: {
    hotel: HotelCreatePayload,
    habitaciones: Array<Omit<HabitacionCreatePayload, 'servicio_id'>>
  }): Observable<any> {
    const body = {
      ...payload.hotel,
      habitaciones: payload.habitaciones.map(h => ({ ...h, descripcion: (h.descripcion || '').toString() })),
    };
    return this.http.post<HotelCreateResponse>(`${this.API_URL}/hoteles`, body, { headers: this.getHeaders() });
  }

  updateHotelWithHabitaciones(servicioId: number, payload: {hotel: HotelCreatePayload, habitaciones: HabitacionUpdatePayload[] }): Observable<any> {  
        // 1. Combinar el payload de hotel y habitaciones en un solo objeto para el PUT
        // El backend de Laravel espera que las habitaciones vengan anidadas en el payload principal
    const fullPayload = {
      ...payload.hotel,
      habitaciones: payload.habitaciones
    };
    console.log(`[SERVICE] Enviando PUT a /hoteles/${servicioId} con payload completo.`, fullPayload);
        
    // 2. Llamar al endpoint PUT /api/hoteles/{servicio_id}
    // El backend ahora maneja: 
    // a) Actualización de Servicio/Hotel
    // b) Reemplazo de Galería
    // c) Sincronización (CUD) de Habitaciones
    return this.http.put<any>(
      `${this.API_URL}/hoteles/${servicioId}`, 
      fullPayload, 
      { headers: this.getHeaders() }
    ).pipe(
      catchError(error => {
        console.error('Error en el flujo de actualización Hotel + Habitaciones:', error);
        // Propagamos el error original
        return throwError(() => error);
      })
    );
  }

  /**
   * NUEVO: Obtiene la lista de hoteles Pertenecientes al proveedor autenticado.
   */
  getSupplierHotels(): Observable<HotelData[]> {
    const endpoint = `${this.API_URL}/proveedor/servicios`; 

    return this.http
      .get<SupplierHotelListApiRespuesta>(endpoint, {
        // 🚨 CAMBIO: getHeaders() ya no tiene token, pero lo enviamos
        headers: this.getHeaders(), 
      })
      .pipe(
        map(res =>
          res.data.map(apiHotel => ({
            id: apiHotel.id, 
            nombre: apiHotel.nombre,
            ciudad: apiHotel.ciudad,
            pais: apiHotel.pais,
            direccion: apiHotel.direccion,
            estrellas: apiHotel.estrellas,
            imagen_url: apiHotel.imagen_url || 'assets/images/placeholder-hotel.jpg',
            imagenes: apiHotel.imagenes ?? [],
            precio_por_noche: apiHotel.precio_por_noche ?? null,
            descripcion: apiHotel.descripcion ?? null,
            reservations: apiHotel.reservas_pendientes ?? 0, 
          }) as HotelData)
        ),
        catchError((error) => {
            console.error('Error al cargar hoteles del proveedor:', error);
            return throwError(() => new Error('No se pudieron cargar sus hoteles. Verifique su autenticación.'));
        })
      );
  }
  /**
   * NUEVO: Obtiene la lista de servicios Pertenecientes al proveedor autenticado.
   */
  getSupplierServices(): Observable<ServiceData[]> {
    const endpoint = `${this.API_URL}/proveedor/servicios`;

    return this.http.get<{ data: any[] }>(endpoint, {
      headers: this.getHeaders(),
    }).pipe(
      map(res => res.data.map(item => ({
        id: item.id,
        tipo: item.tipo,
        nombre: item.nombre,
        descripcion: item.descripcion,
        ciudad: item.ciudad,
        pais: item.pais,
        imagen_url: item.imagen_url || 'assets/images/placeholder.jpg',
        activo: item.activo,
        created_at: item.created_at,
        meta_tipo: item.meta_tipo,
        reservas_totales: item.reservas_totales,
      }) as ServiceData)),
      catchError((error) => {
        console.error('Error al cargar servicios del proveedor:', error);
        return throwError(() => new Error('No se pudieron cargar los servicios.'));
      })
    );
  }

  /**
   * NUEVO: Método para eliminar un hotel por su ID de servicio.
   */
  deleteHotel(servicioId: number): Observable<any> {
    // El error se propaga tal cual: 409 (reservas activas) o 403 (no es tuyo) traen su mensaje.
    return this.http.delete(`${this.API_URL}/servicios/${servicioId}`, { headers: this.getHeaders() });
  }

  /** Mapea un hotel del listado de la API al modelo de la web. */
  private mapHotel(apiHotel: any): HotelData {
    return {
      id: apiHotel.id,
      nombre: apiHotel.nombre,
      ciudad: apiHotel.ciudad,
      pais: apiHotel.pais,
      direccion: apiHotel.direccion,
      estrellas: apiHotel.estrellas,
      imagen_url: apiHotel.imagen_url || apiHotel.imagenUrl?.[0] || 'https://img.freepik.com/premium-photo/abstract-blur-hotel-interior_1124848-65384.jpg?semt=ais_hybrid&w=740&q=80',
      precio_por_noche: apiHotel.precio_por_noche ?? null,
      descripcion: apiHotel.descripcion ?? null,
      reservations: 0,
      calificacion: apiHotel.calificacion,
      habitaciones_disponibles: apiHotel.habitaciones,
    } as HotelData;
  }

  /**
   * Búsqueda de hoteles (TRV-201). Parámetros: ciudad, check_in, check_out, adultos, ninos, habitaciones,
   * sort, page, per_page. Con fechas, la API devuelve solo hoteles con unidades libres en todo el rango.
   */
  buscarHoteles(params?: Record<string, any>): Observable<{ items: HotelData[]; meta: { current_page: number; last_page: number; total: number; per_page: number } }> {
    let httpParams = new HttpParams();
    Object.entries(params || {}).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') httpParams = httpParams.set(k, String(v));
    });
    return this.http.get<any>(`${this.API_URL}/hoteles`, { headers: this.getHeaders(), params: httpParams }).pipe(
      map(res => ({
        items: (res.data || []).map((h: any) => this.mapHotel(h)),
        meta: res.meta || { current_page: 1, last_page: 1, total: (res.data || []).length, per_page: (res.data || []).length },
      }))
    );
  }

  /** Compatibilidad: lista de hoteles (primera página, hasta 100). */
  getHoteles(params?: Record<string, any>): Observable<HotelData[]> {
    return this.buscarHoteles({ per_page: 100, ...(params || {}) }).pipe(map(r => r.items));
  }

  /** Disponibilidad real de un hotel en un rango (TRV-202). */
  getDisponibilidad(id: number, checkIn: string, checkOut: string, adultos?: number, ninos?: number, habitaciones?: number): Observable<Habitacion[]> {
    let p = new HttpParams().set('check_in', checkIn).set('check_out', checkOut);
    if (adultos) p = p.set('adultos', String(adultos));
    if (ninos) p = p.set('ninos', String(ninos));
    if (habitaciones) p = p.set('habitaciones', String(habitaciones));
    return this.http.get<any>(`${this.API_URL}/hoteles/${id}/disponibilidad`, { headers: this.getHeaders(), params: p }).pipe(
      map(res => (res.data?.habitaciones || []).map((r: any) => ({
        id: r.id,
        nombre: r.nombre,
        capacidad_adultos: r.capacidad_adultos,
        capacidad_ninos: r.capacidad_ninos,
        cantidad: r.unidades_totales ?? r.cantidad,
        unidades_disponibles: r.unidades_disponibles,
        precio_por_noche: r.precio_por_noche,
        descripcion: r.descripcion ?? '',
      } as Habitacion)))
    );
  }

  /**
   * Obtener los detalles de un hotel específico (habitaciones)
   */
  getHotelDetalles(id: number): Observable<HotelDetalles> {
    return this.http
      .get<any>(`${this.API_URL}/hoteles/${id}`, {
        // 🚨 CAMBIO: getHeaders() ya no tiene token, pero lo enviamos
        headers: this.getHeaders()
      })
      .pipe(
        map(res => {
          const h = res.hotel ?? res.data ?? res;
          if (!h) {
            throw new Error('Formato de respuesta inesperado al obtener detalles del hotel.');
          }

          const hotel: HotelData = {
            id: h.servicio_id ?? h.id,
            nombre: h.nombre,
            ciudad: h.ciudad,
            pais: h.pais,
            direccion: h.direccion,
            estrellas: h.estrellas,
            imagen_url: h.imagen_url || 'https://img.freepik.com/premium-photo/abstract-blur-hotel-interior_1124848-65384.jpg?semt=ais_hybrid&w=740&q=80',
            imagenes:  (h.imagenes ?? [])
              .filter((img: any) => img && img.url)
              .map((img: any) => ({
                url: img.url,
                alt: img.alt ?? null
              })),
            precio_por_noche: h.precio_por_noche ?? null,
            descripcion: h.descripcion ?? null,
            reservations: 0,
            calificacion: h.calificacion,
          } as HotelData;

          const habitaciones: Habitacion[] = (h.habitaciones ?? res.habitaciones ?? []).map((r: any) => ({
            id: r.id,
            nombre: r.nombre ?? '',
            capacidad_adultos: r.capacidad_adultos,
            capacidad_ninos: r.capacidad_ninos,
            cantidad: r.cantidad ?? r.unidades_totales ?? 0,
            unidades_disponibles: r.unidades_disponibles ?? r.cantidad,
            precio_por_noche: r.precio_por_noche,
            descripcion: r.descripcion ?? ''
          }));

          return { hotel, habitaciones };
        })
      );
  }

  /**
   * Combina la información general y los detalles del hotel
   */
  getHotelCompleto(id: number): Observable<HotelDetalles> {
    return forkJoin({
      lista: this.getHoteles(),
      detalle: this.getHotelDetalles(id)
    }).pipe(
      map(({ lista, detalle }) => {
        const hotelLista = lista.find(h => h.id === id);

        if (hotelLista) {
          detalle.hotel = {
            ...detalle.hotel,
            ...hotelLista 
          };
        }

        return detalle;
      })
    );
  }
}