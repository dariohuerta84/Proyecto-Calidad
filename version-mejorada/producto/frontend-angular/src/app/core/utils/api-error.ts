import { HttpErrorResponse } from '@angular/common/http';

/**
 * Convierte un error HTTP de la API Laravel en un mensaje legible en español.
 * - 422: primer mensaje de validación por campo.
 * - 401/403/404/409/429/503: mensaje del backend o uno por defecto.
 * - 0: sin conexión.
 */
export function mensajeDeError(err: unknown, porDefecto = 'Ocurrió un error inesperado. Intenta nuevamente.'): string {
  if (!(err instanceof HttpErrorResponse)) {
    return (err as any)?.message || porDefecto;
  }
  if (err.status === 0) return 'No se puede conectar con el servidor. Revisa tu conexión.';

  const body: any = err.error || {};
  if (err.status === 422 && body.errors) {
    const primero = Object.values(body.errors)[0] as any;
    if (Array.isArray(primero) && primero.length) return String(primero[0]);
  }
  if (typeof body.message === 'string' && body.message) return body.message;
  if (typeof body.error === 'string' && body.error) return body.error;

  switch (err.status) {
    case 401: return 'Tu sesión expiró. Inicia sesión nuevamente.';
    case 403: return 'No tienes permiso para realizar esta acción.';
    case 404: return 'No encontramos lo que buscas.';
    case 409: return 'La operación no se pudo completar por el estado actual del recurso.';
    case 429: return 'Demasiados intentos. Espera un minuto.';
    case 503: return 'El servicio no está disponible en este momento.';
    default:  return porDefecto;
  }
}

/** Errores de validación por campo: { campo: 'mensaje' } */
export function erroresPorCampo(err: unknown): Record<string, string> {
  if (!(err instanceof HttpErrorResponse) || err.status !== 422) return {};
  const errors = (err.error?.errors || {}) as Record<string, string[]>;
  return Object.fromEntries(Object.entries(errors).map(([k, v]) => [k, Array.isArray(v) ? v[0] : String(v)]));
}
