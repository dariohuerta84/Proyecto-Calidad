import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProveedorService } from '../../services/proveedor.service';
import { Paginado, ReservaCualquiera, ReservaHotel } from '../../../../shared/models/reserva.model';
import { mensajeDeError } from '../../../../core/utils/api-error';

/**
 * TRV-507 — Reservas recibidas (filtros por tipo, estado y servicio; pendientes primero).
 * TRV-204 — Aceptar o rechazar (con motivo de 10 a 300 caracteres) solicitudes de hotel.
 */
@Component({
  selector: 'app-reservas-recibidas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reservas-recibidas.component.html',
  styleUrls: ['./reservas-recibidas.component.css'],
})
export class ReservasRecibidasComponent implements OnInit {
  private prov = inject(ProveedorService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  tipo = '';
  estado = '';
  servicioId = '';
  page = 1;

  pagina: Paginado<ReservaCualquiera> | null = null;
  cargando = true;
  error = '';
  mensaje = '';
  mensajeError = '';

  procesandoId: number | null = null;
  aRechazar: ReservaHotel | null = null;
  motivo = '';
  errorMotivo = '';

  readonly estados = ['', 'pendiente', 'confirmada', 'rechazada', 'cancelada'];

  get reservas(): ReservaCualquiera[] { return this.pagina?.data ?? []; }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(q => {
      this.tipo = q.get('tipo') || '';
      this.estado = q.get('estado') || '';
      this.servicioId = q.get('servicio_id') || '';
      this.page = +(q.get('page') || 1);
      this.cargar();
    });
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.prov.reservas({ tipo: this.tipo, estado: this.estado, servicio_id: this.servicioId, page: this.page }).subscribe({
      next: p => { this.pagina = p; this.cargando = false; },
      error: e => { this.cargando = false; this.error = mensajeDeError(e, 'No pudimos cargar las reservas.'); },
    });
  }

  aplicarFiltros(cambios: Record<string, string | number | null>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tipo: this.tipo || null, estado: this.estado || null, servicio_id: this.servicioId || null, page: null, ...cambios },
    });
  }

  irPagina(p: number): void { this.aplicarFiltros({ page: p > 1 ? p : null }); window.scrollTo({ top: 0 }); }

  paginas(): number[] {
    const last = this.pagina?.meta.last_page ?? 1;
    return last > 1 ? Array.from({ length: last }, (_, i) => i + 1) : [];
  }

  esHotel(r: ReservaCualquiera): r is ReservaHotel { return r.tipo === 'hotel'; }
  asHotel(r: ReservaCualquiera): ReservaHotel { return r as ReservaHotel; }
  asTour(r: ReservaCualquiera): any { return r as any; }

  aceptar(r: ReservaHotel): void {
    this.limpiarAvisos();
    this.procesandoId = r.id;
    this.prov.aceptar(r.id).subscribe({
      next: res => { this.procesandoId = null; this.mensaje = res.message || `Reserva ${r.codigo} confirmada.`; this.cargar(); },
      error: e => { this.procesandoId = null; this.mensajeError = mensajeDeError(e, 'No se pudo aceptar la reserva.'); this.cargar(); },
    });
  }

  pedirRechazo(r: ReservaHotel): void {
    this.limpiarAvisos();
    this.aRechazar = r;
    this.motivo = '';
    this.errorMotivo = '';
  }

  confirmarRechazo(): void {
    const r = this.aRechazar;
    if (!r) return;
    const m = this.motivo.trim();
    if (m.length < 10 || m.length > 300) {
      this.errorMotivo = 'El motivo debe tener entre 10 y 300 caracteres.';
      return;
    }
    this.procesandoId = r.id;
    this.prov.rechazar(r.id, m).subscribe({
      next: res => {
        this.procesandoId = null; this.aRechazar = null;
        this.mensaje = res.message || `Reserva ${r.codigo} rechazada.`;
        this.cargar();
      },
      error: e => {
        this.procesandoId = null;
        if (e?.status === 422) { this.errorMotivo = mensajeDeError(e, 'Revisa el motivo.'); return; }
        this.aRechazar = null;
        this.mensajeError = mensajeDeError(e, 'No se pudo rechazar la reserva.');
        this.cargar();
      },
    });
  }

  private limpiarAvisos(): void { this.mensaje = ''; this.mensajeError = ''; }
}
