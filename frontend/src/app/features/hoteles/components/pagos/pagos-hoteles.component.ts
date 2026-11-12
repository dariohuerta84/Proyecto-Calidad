import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';

import { AuthService } from '../../../../core/services/auth.service';
import { ReservasService } from '../../services/reservas.service';
import { CotizacionHotel, ItemHabitacion, PagoSimulado, SolicitudHotelCreada } from '../../../../shared/models/reserva.model';
import { PagoSimuladoComponent } from '../../../../shared/components/pago-simulado/pago-simulado.component';
import { mensajeDeError } from '../../../../core/utils/api-error';
import { nuevaClaveIdempotencia } from '../../../../core/utils/idempotency';

/**
 * HU-V07 — Confirmar y pagar una solicitud de hotel.
 * - Los importes (subtotal, IGV 18%, total) los calcula la API con /cotizar: coinciden con lo guardado.
 * - El pago simulado se valida en el formulario y en la API.
 * - La reserva queda PENDIENTE hasta que el proveedor la acepte o rechace (HU-P13/HU-P14).
 * - Una misma clave de idempotencia por intento evita reservas duplicadas si se reintenta.
 */
@Component({
  selector: 'app-pagos-hoteles',
  standalone: true,
  templateUrl: './pagos-hoteles.component.html',
  styleUrls: ['./pagos-hoteles.component.css'],
  imports: [CommonModule, RouterModule, CurrencyPipe, DatePipe, PagoSimuladoComponent],
})
export class PagosHotelesComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private reservasService = inject(ReservasService);

  // Datos de la selección (vienen del detalle del hotel)
  hotelId = 0;
  nombreHotel = '';
  ubicacion = '';
  imagenHotel?: string;
  checkInStr = '';
  checkOutStr = '';
  checkIn: Date | null = null;
  checkOut: Date | null = null;
  adultosReservados = 0;
  ninosReservados = 0;
  items: ItemHabitacion[] = [];

  // Cotización oficial
  cotizacion: CotizacionHotel | null = null;
  cargandoCotizacion = true;
  errorCotizacion = '';

  // Itinerario sugerido (solo informativo)
  itinerario: any = null;

  // Pago
  mostrarModalPago = false;
  procesando = false;
  errorPago = '';
  private claveIdempotencia = nuevaClaveIdempotencia();

  // Resultado
  solicitud: SolicitudHotelCreada | null = null;

  get cantidadTotalCuartos(): number { return this.items.reduce((a, i) => a + i.cantidad, 0); }
  get totalNoches(): number { return this.cotizacion?.noches ?? 0; }

  ngOnInit(): void {
    const p = this.route.snapshot.queryParamMap;
    this.hotelId = +(p.get('hotelId') || 0);
    this.nombreHotel = p.get('hotelNombre') || 'Hotel';
    this.ubicacion = p.get('ubicacion') || '';
    this.imagenHotel = p.get('imagen') || undefined;
    this.checkInStr = p.get('checkIn') || '';
    this.checkOutStr = p.get('checkOut') || '';
    this.checkIn = this.parseFecha(this.checkInStr);
    this.checkOut = this.parseFecha(this.checkOutStr);
    this.adultosReservados = +(p.get('adultos') || 0);
    this.ninosReservados = +(p.get('ninos') || 0);

    const n = +(p.get('numTiposReservados') || 0);
    for (let i = 0; i < n; i++) {
      const id = +(p.get(`reserva_${i}_habitacion_id`) || 0);
      const cant = +(p.get(`reserva_${i}_cant`) || 0);
      if (id > 0 && cant > 0) this.items.push({ habitacion_id: id, cantidad: cant });
    }

    try {
      const raw = this.hotelId ? sessionStorage.getItem('itinerario_hotel_' + this.hotelId) : null;
      if (raw && p.get('itinerarioBasico') === 'true') this.itinerario = JSON.parse(raw);
    } catch { /* sin almacenamiento */ }

    this.cotizar();
  }

  /** Pide a la API el importe oficial de la selección. */
  cotizar(): void {
    if (!this.checkInStr || !this.checkOutStr || this.items.length === 0) {
      this.cargandoCotizacion = false;
      this.errorCotizacion = 'Faltan datos de la reserva. Vuelve al hotel y selecciona fechas y habitaciones.';
      return;
    }
    this.cargandoCotizacion = true;
    this.errorCotizacion = '';
    this.reservasService.cotizarHotel(this.checkInStr, this.checkOutStr, this.items).subscribe({
      next: c => { this.cotizacion = c; this.cargandoCotizacion = false; },
      error: e => { this.cargandoCotizacion = false; this.errorCotizacion = mensajeDeError(e, 'No pudimos calcular el importe.'); },
    });
  }

  procesarPago(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    if (this.authService.getRole() !== 'viajero') {
      this.errorCotizacion = 'Solo las cuentas de viajero pueden reservar.';
      return;
    }
    this.errorPago = '';
    this.mostrarModalPago = true;
  }

  /** Cerrar el formulario antes de enviar conserva la selección y no crea nada. */
  cerrarModal(): void {
    if (!this.procesando) this.mostrarModalPago = false;
  }

  confirmarPago(pago: PagoSimulado): void {
    this.procesando = true;
    this.errorPago = '';
    this.reservasService.crearReservaHotel(this.checkInStr, this.checkOutStr, this.items, pago, this.claveIdempotencia).subscribe({
      next: res => {
        this.procesando = false;
        this.mostrarModalPago = false;
        this.solicitud = res.data;
        this.claveIdempotencia = nuevaClaveIdempotencia();
        try { sessionStorage.removeItem('itinerario_hotel_' + this.hotelId); } catch { /* */ }
      },
      error: e => {
        this.procesando = false;
        this.errorPago = mensajeDeError(e, 'No se pudo registrar la reserva.');
        // 409 (sin unidades) o 422 (datos): la operación no se registró → nueva clave para el siguiente intento.
        if (e?.status === 409 || e?.status === 422) {
          this.claveIdempotencia = nuevaClaveIdempotencia();
          if (e?.status === 409) this.cotizar();
        }
        // Error de red / 5xx: se conserva la clave; reintentar no duplicará la reserva.
      },
    });
  }

  volverAtras(): void {
    if (this.hotelId) {
      this.router.navigate(['/hoteles/detalle', this.hotelId], {
        queryParams: { checkIn: this.checkInStr, checkOut: this.checkOutStr, adultos: this.adultosReservados || 1, ninos: this.ninosReservados || 0 },
      });
    } else {
      this.router.navigate(['/hoteles']);
    }
  }

  private parseFecha(s: string): Date | null {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(s)) return null;
    const [y, m, d] = s.split('-').map(Number);
    return new Date(y, m - 1, d);
  }
}
