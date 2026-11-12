/** Modelos de reservas alineados a la API (etapa 2). */

export type EstadoReservaHotel = 'pendiente' | 'confirmada' | 'rechazada' | 'cancelada';
export type EstadoReservaTour = 'confirmada' | 'cancelada';

/** Datos del pago simulado (no se guardan: la API solo conserva los 4 últimos dígitos). */
export interface PagoSimulado {
  titular: string;
  numero: string;       // 16 dígitos
  vencimiento: string;  // MM/AA
  cvc: string;          // 3 dígitos
}

export interface ItemHabitacion {
  habitacion_id: number;
  cantidad: number;
}

export interface CotizacionHotel {
  hotel: { id: number; nombre: string };
  fecha_inicio: string;
  fecha_fin: string;
  noches: number;
  items: Array<{
    habitacion_id: number; nombre: string; cantidad: number; precio_por_noche: number;
    unidades_disponibles: number; subtotal: number; impuesto: number; total: number;
  }>;
  subtotal: number;
  impuesto: number;
  igv_porcentaje: number;
  total: number;
}

export interface CotizacionTour {
  tour: { id: number; nombre: string };
  salida: { id: number; fecha: string; hora: string; cupos_disponibles: number };
  personas: number;
  precio_unitario: number;
  igv_porcentaje: number;
  subtotal: number;
  impuesto: number;
  total: number;
}

export interface ServicioResumen {
  id: number | null;
  nombre: string | null;
  ciudad: string | null;
  imagen_url: string | null;
  eliminado: boolean;
}

export interface ReservaHotel {
  id: number;
  tipo: 'hotel';
  codigo: string;
  solicitud_codigo: string | null;
  estado: EstadoReservaHotel;
  motivo_rechazo: string | null;
  fecha_inicio: string;
  fecha_fin: string;
  noches: number;
  cantidad: number;
  precio_noche: number;
  subtotal: number;
  impuesto: number;
  total: number;
  tarjeta_ultimos4: string | null;
  decidido_en: string | null;
  cancelado_en: string | null;
  creado_en: string | null;
  cancelable: boolean;
  servicio: ServicioResumen;
  habitacion: { id: number | null; nombre: string | null };
  cliente?: { id: number; nombre: string; email: string } | null;
}

export interface ReservaTour {
  id: number;
  tipo: 'tour';
  codigo: string;
  estado: EstadoReservaTour;
  personas: number;
  precio_unitario: number;
  subtotal: number;
  impuesto: number;
  total: number;
  tarjeta_ultimos4: string | null;
  cancelado_en: string | null;
  creado_en: string | null;
  cancelable: boolean;
  aviso: string | null;
  servicio: ServicioResumen;
  salida: { id: number | null; fecha: string | null; hora: string | null; estado: string | null };
  cliente?: { id: number; nombre: string; email: string } | null;
}

export type ReservaCualquiera = ReservaHotel | ReservaTour;

export interface Paginado<T> {
  data: T[];
  meta: { current_page: number; last_page: number; per_page: number; total: number };
}

export interface SolicitudHotelCreada {
  solicitud_codigo: string;
  estado: 'pendiente';
  hotel: { id: number; nombre: string };
  fecha_inicio: string;
  fecha_fin: string;
  noches: number;
  subtotal: number;
  impuesto: number;
  total: number;
  tarjeta_ultimos4: string;
  codigo_reserva: string;
  reservas: ReservaHotel[];
}

// ---- Compatibilidad con código anterior
export interface ReservaHabitacionPayload {
  habitacion_id: number;
  fecha_inicio: string;
  fecha_fin: string;
  cantidad: number;
}
export interface ReservaApiRespuesta { message?: string; data?: any; }
export type MisReservasHotelItem = ReservaHotel;
