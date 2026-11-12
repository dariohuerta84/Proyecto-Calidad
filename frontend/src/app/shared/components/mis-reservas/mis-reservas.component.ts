import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ReservasService } from '../../../features/hoteles/services/reservas.service';
import { Paginado, ReservaHotel, ReservaTour } from '../../models/reserva.model';
import { mensajeDeError } from '../../../core/utils/api-error';
import { ImageUtils } from '../../utils/image.utils';

type Tab = 'hoteles' | 'tours';

/**
 * HU-V11 / HU-V13 — Mis reservas.
 * Fuente de verdad: la API (no se guarda nada en el navegador). Muestra los mismos códigos,
 * estados e importes que la app móvil. Permite filtrar, paginar y cancelar con confirmación.
 */
@Component({
  selector: 'app-mis-reservas',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyPipe, DatePipe],
  templateUrl: './mis-reservas.component.html',
  styleUrls: ['./mis-reservas.component.css'],
})
export class MisReservasComponent implements OnInit {
  private reservas = inject(ReservasService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  tab: Tab = 'hoteles';
  estado = '';
  page = 1;

  cargando = false;
  error = '';
  hoteles: Paginado<ReservaHotel> | null = null;
  tours: Paginado<ReservaTour> | null = null;

  // Cancelación
  aCancelar: ReservaHotel | ReservaTour | null = null;
  cancelando = false;
  mensaje = '';
  mensajeError = '';

  readonly estadosHotel = [
    { v: '', t: 'Todas' }, { v: 'pendiente', t: 'Pendientes' }, { v: 'confirmada', t: 'Confirmadas' },
    { v: 'rechazada', t: 'Rechazadas' }, { v: 'cancelada', t: 'Canceladas' },
  ];
  readonly estadosTour = [{ v: '', t: 'Todas' }, { v: 'confirmada', t: 'Confirmadas' }, { v: 'cancelada', t: 'Canceladas' }];

  get estados() { return this.tab === 'hoteles' ? this.estadosHotel : this.estadosTour; }
  get meta() { return this.tab === 'hoteles' ? this.hoteles?.meta : this.tours?.meta; }
  get lista(): Array<ReservaHotel | ReservaTour> {
    return (this.tab === 'hoteles' ? this.hoteles?.data : this.tours?.data) ?? [];
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(q => {
      this.tab = q.get('tab') === 'tours' ? 'tours' : 'hoteles';
      this.estado = q.get('estado') || '';
      this.page = Math.max(1, +(q.get('page') || 1));
      this.cargar();
    });
  }

  private navegar(params: Record<string, any>): void {
    this.router.navigate([], { relativeTo: this.route, queryParams: params, queryParamsHandling: 'merge' });
  }

  cambiarTab(t: Tab): void { this.navegar({ tab: t, estado: null, page: null }); }
  cambiarEstado(e: string): void { this.navegar({ estado: e || null, page: null }); }
  irPagina(p: number): void { this.navegar({ page: p > 1 ? p : null }); }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    const obs = this.tab === 'hoteles'
      ? this.reservas.misReservasHotel(this.page, this.estado)
      : this.reservas.misReservasTour(this.page, this.estado);
    (obs as any).subscribe({
      next: (r: any) => {
        if (this.tab === 'hoteles') this.hoteles = r; else this.tours = r;
        this.cargando = false;
      },
      error: (e: any) => {
        this.cargando = false;
        this.error = mensajeDeError(e, 'No pudimos cargar tus reservas.');
      },
    });
  }

  imagen(r: ReservaHotel | ReservaTour): string {
    return r.servicio?.imagen_url || ImageUtils.getPlaceholder(r.tipo === 'hotel' ? 'hotel' : 'tour');
  }

  etiquetaEstado(e: string): string {
    return ({ pendiente: 'Pendiente', confirmada: 'Confirmada', rechazada: 'Rechazada', cancelada: 'Cancelada' } as any)[e] || e;
  }

  pedirCancelacion(r: ReservaHotel | ReservaTour): void {
    this.mensaje = '';
    this.mensajeError = '';
    this.aCancelar = r;
  }

  confirmarCancelacion(): void {
    const r = this.aCancelar;
    if (!r) return;
    this.cancelando = true;
    const obs: any = r.tipo === 'hotel' ? this.reservas.cancelarReservaHotel(r.id) : this.reservas.cancelarReservaTour(r.id);
    obs.subscribe({
      next: () => {
        this.cancelando = false;
        this.aCancelar = null;
        this.mensaje = `La reserva ${r.codigo} fue cancelada.`;
        this.cargar();
      },
      error: (e: any) => {
        this.cancelando = false;
        this.aCancelar = null;
        // Ante cualquier respuesta se recarga el estado real guardado (no se anuncia un éxito falso).
        this.mensajeError = mensajeDeError(e, 'No se pudo cancelar la reserva.');
        this.cargar();
      },
    });
  }

  paginas(): number[] {
    const m = this.meta;
    if (!m || m.last_page <= 1) return [];
    return Array.from({ length: m.last_page }, (_, i) => i + 1);
  }

  esHotel(r: ReservaHotel | ReservaTour): r is ReservaHotel { return r.tipo === 'hotel'; }
}
