import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';

import { AuthService } from '../../../../core/services/auth.service';
import { ReservasService } from '../../../hoteles/services/reservas.service';
import { CotizacionTour, PagoSimulado, ReservaTour } from '../../../../shared/models/reserva.model';
import { PagoSimuladoComponent } from '../../../../shared/components/pago-simulado/pago-simulado.component';
import { mensajeDeError } from '../../../../core/utils/api-error';
import { nuevaClaveIdempotencia } from '../../../../core/utils/idempotency';

/**
 * HU-V10 — Reservar una salida de tour con pago simulado.
 * La API confirma automáticamente y descuenta los cupos en la misma operación.
 */
@Component({
  selector: 'app-pagos-tours',
  standalone: true,
  templateUrl: './pagos-tours.component.html',
  styleUrls: ['./pagos-tours.component.css'],
  imports: [CommonModule, RouterModule, CurrencyPipe, DatePipe, PagoSimuladoComponent],
})
export class PagosToursComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private reservasService = inject(ReservasService);

  nombreTour = '';
  ubicacion = '';
  imagenTour?: string;
  fechaSalida: Date | null = null;
  horaSalida = '';
  salidaId = 0;
  tourId = 0;
  categoria = '';
  duracion = 0;
  adultos = 0;
  ninos = 0;
  totalPersonas = 0;

  cotizacion: CotizacionTour | null = null;
  cargandoCotizacion = true;
  errorCotizacion = '';

  mostrarModalPago = false;
  procesando = false;
  errorPago = '';
  private claveIdempotencia = nuevaClaveIdempotencia();

  reserva: ReservaTour | null = null;

  ngOnInit(): void {
    const p = this.route.snapshot.queryParamMap;
    this.nombreTour = p.get('tourNombre') || 'Tour';
    this.ubicacion = p.get('ubicacion') || '';
    this.imagenTour = p.get('imagen') || undefined;
    this.salidaId = +(p.get('salidaId') || 0);
    this.tourId = +(p.get('tourId') || 0);
    this.categoria = p.get('categoria') || '';
    this.duracion = +(p.get('duracion') || 0);
    this.horaSalida = p.get('horaSalida') || '';
    const f = p.get('fechaSalida') || '';
    if (/^\d{4}-\d{2}-\d{2}$/.test(f)) {
      const [y, m, d] = f.split('-').map(Number);
      this.fechaSalida = new Date(y, m - 1, d);
    }
    this.adultos = +(p.get('adultos') || 0);
    this.ninos = +(p.get('ninos') || 0);
    this.totalPersonas = +(p.get('totalPersonas') || this.adultos + this.ninos);
    this.cotizar();
  }

  get duracionFormateada(): string {
    if (!this.duracion) return '';
    const h = Math.floor(this.duracion / 60), m = this.duracion % 60;
    return m === 0 ? `${h} hora${h !== 1 ? 's' : ''}` : `${h}h ${m}m`;
  }

  cotizar(): void {
    if (!this.salidaId || this.totalPersonas < 1) {
      this.cargandoCotizacion = false;
      this.errorCotizacion = 'Faltan datos de la reserva. Vuelve al tour y elige una salida.';
      return;
    }
    this.cargandoCotizacion = true;
    this.errorCotizacion = '';
    this.reservasService.cotizarTour(this.salidaId, this.totalPersonas).subscribe({
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

  cerrarModal(): void {
    if (!this.procesando) this.mostrarModalPago = false;
  }

  confirmarPago(pago: PagoSimulado): void {
    this.procesando = true;
    this.errorPago = '';
    this.reservasService.crearReservaTour(this.salidaId, this.totalPersonas, pago, this.claveIdempotencia).subscribe({
      next: res => {
        this.procesando = false;
        this.mostrarModalPago = false;
        this.reserva = res.data;
        this.claveIdempotencia = nuevaClaveIdempotencia();
      },
      error: e => {
        this.procesando = false;
        this.errorPago = mensajeDeError(e, 'No se pudo registrar la reserva.');
        if (e?.status === 409 || e?.status === 422) {
          this.claveIdempotencia = nuevaClaveIdempotencia();
          if (e?.status === 409) this.cotizar();
        }
      },
    });
  }

  volverAtras(): void {
    if (this.tourId) this.router.navigate(['/tour/detalle', this.tourId]);
    else this.router.navigate(['/tour']);
  }
}
