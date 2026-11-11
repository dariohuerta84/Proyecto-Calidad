import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProveedorService, Salida, Actividad } from '../../services/proveedor.service';
import { mensajeDeError, erroresPorCampo } from '../../../../core/utils/api-error';
import { environment } from '../../../../../environments/environment';

/**
 * TRV-506 — Programar salidas (fecha, hora, cupo), cerrarlas, cancelarlas o eliminarlas.
 * TRV-505 — Actividades del tour con orden.
 */
@Component({
  selector: 'app-gestionar-tour',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './gestionar-tour.component.html',
  styleUrls: ['./gestionar-tour.component.css'],
})
export class GestionarTourComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private prov = inject(ProveedorService);
  private http = inject(HttpClient);

  tourId = 0;
  nombre = '';
  salidas: Salida[] = [];
  actividades: Actividad[] = [];
  cargando = true;
  error = '';
  mensaje = '';
  mensajeError = '';

  readonly hoy = new Date().toISOString().slice(0, 10);

  // Formulario de salida (crear / editar)
  salidaForm = { fecha: '', hora: '08:00', cupo_total: 10 };
  editandoSalida: Salida | null = null;
  erroresSalida: Record<string, string> = {};
  guardandoSalida = false;

  // Formulario de actividad
  actForm: { titulo: string; descripcion: string; duracion_min: number | null; orden: number | null } = { titulo: '', descripcion: '', duracion_min: null, orden: null };
  editandoAct: Actividad | null = null;
  erroresAct: Record<string, string> = {};
  guardandoAct = false;

  ngOnInit(): void {
    this.tourId = +(this.route.snapshot.paramMap.get('id') || 0);
    this.http.get<any>(`${environment.apiUrl}/tours/${this.tourId}`).subscribe({
      next: r => this.nombre = r?.servicio?.nombre ?? r?.nombre ?? '',
      error: () => {},
    });
    this.cargarTodo();
  }

  cargarTodo(): void {
    this.cargando = true;
    this.error = '';
    let pendientes = 2;
    const fin = () => { if (--pendientes === 0) this.cargando = false; };
    this.prov.salidas(this.tourId).subscribe({ next: s => { this.salidas = s; fin(); }, error: e => { this.error = mensajeDeError(e, 'No pudimos cargar las salidas.'); fin(); } });
    this.prov.actividades(this.tourId).subscribe({ next: a => { this.actividades = a; fin(); }, error: e => { this.error = mensajeDeError(e, 'No pudimos cargar las actividades.'); fin(); } });
  }

  private ok(m: string): void { this.mensaje = m; this.mensajeError = ''; }
  private ko(e: any, m: string): void { this.mensajeError = mensajeDeError(e, m); this.mensaje = ''; }

  // ---------------- Salidas
  editarSalida(s: Salida): void {
    this.editandoSalida = s;
    this.salidaForm = { fecha: s.fecha, hora: s.hora, cupo_total: s.cupo_total };
    this.erroresSalida = {};
  }

  cancelarEdicionSalida(): void {
    this.editandoSalida = null;
    this.salidaForm = { fecha: '', hora: '08:00', cupo_total: 10 };
    this.erroresSalida = {};
  }

  guardarSalida(): void {
    this.erroresSalida = {};
    const f = this.salidaForm;
    if (!f.fecha) this.erroresSalida['fecha'] = 'Elige una fecha.';
    else if (f.fecha < this.hoy) this.erroresSalida['fecha'] = 'La fecha no puede ser pasada.';
    if (!/^\d{2}:\d{2}$/.test(f.hora || '')) this.erroresSalida['hora'] = 'Indica la hora (HH:MM).';
    if (!f.cupo_total || f.cupo_total < 1 || f.cupo_total > 1000) this.erroresSalida['cupo_total'] = 'El cupo debe estar entre 1 y 1000.';
    if (this.editandoSalida && f.cupo_total < this.editandoSalida.cupo_reservado) this.erroresSalida['cupo_total'] = `Mínimo ${this.editandoSalida.cupo_reservado} (cupos ya reservados).`;
    if (Object.keys(this.erroresSalida).length) return;

    this.guardandoSalida = true;
    const cambios: any = { cupo_total: +f.cupo_total };
    if (!this.editandoSalida || this.editandoSalida.fecha !== f.fecha) cambios.fecha = f.fecha;
    if (!this.editandoSalida || this.editandoSalida.hora !== f.hora) cambios.hora = f.hora;
    const obs = this.editandoSalida
      ? this.prov.actualizarSalida(this.tourId, this.editandoSalida.id, cambios)
      : this.prov.crearSalida(this.tourId, { fecha: f.fecha, hora: f.hora, cupo_total: +f.cupo_total });
    const editando = !!this.editandoSalida;
    obs.subscribe({
      next: () => { this.guardandoSalida = false; this.ok(editando ? 'Salida actualizada.' : 'Salida programada.'); this.cancelarEdicionSalida(); this.recargarSalidas(); },
      error: e => {
        this.guardandoSalida = false;
        const campos = erroresPorCampo(e);
        if (Object.keys(campos).length) this.erroresSalida = campos;
        this.ko(e, 'No se pudo guardar la salida.');
      },
    });
  }

  cambiarEstado(s: Salida, estado: 'programada' | 'cerrada' | 'cancelada'): void {
    this.prov.actualizarSalida(this.tourId, s.id, { estado }).subscribe({
      next: () => { this.ok(`Salida del ${s.fecha} ${estado === 'programada' ? 'reabierta' : estado}.`); this.recargarSalidas(); },
      error: e => this.ko(e, 'No se pudo cambiar el estado.'),
    });
  }

  eliminarSalida(s: Salida): void {
    this.prov.eliminarSalida(this.tourId, s.id).subscribe({
      next: () => { this.ok('Salida eliminada.'); this.recargarSalidas(); },
      error: e => this.ko(e, 'No se pudo eliminar la salida.'),
    });
  }

  private recargarSalidas(): void { this.prov.salidas(this.tourId).subscribe(s => this.salidas = s); }

  // ---------------- Actividades
  editarAct(a: Actividad): void {
    this.editandoAct = a;
    this.actForm = { titulo: a.titulo, descripcion: a.descripcion ?? '', duracion_min: a.duracion_min, orden: a.orden };
    this.erroresAct = {};
  }

  cancelarEdicionAct(): void {
    this.editandoAct = null;
    this.actForm = { titulo: '', descripcion: '', duracion_min: null, orden: null };
    this.erroresAct = {};
  }

  guardarAct(): void {
    this.erroresAct = {};
    const f = this.actForm;
    if (!f.titulo.trim()) this.erroresAct['titulo'] = 'El título es obligatorio.';
    if (f.duracion_min !== null && (f.duracion_min < 0 || f.duracion_min > 1440)) this.erroresAct['duracion_min'] = 'Entre 0 y 1440 minutos.';
    if (f.orden !== null && f.orden < 1) this.erroresAct['orden'] = 'El orden empieza en 1.';
    if (Object.keys(this.erroresAct).length) return;

    const body: any = { titulo: f.titulo.trim(), descripcion: f.descripcion.trim() || null, duracion_min: f.duracion_min };
    if (f.orden) body.orden = +f.orden;
    this.guardandoAct = true;
    const editando = !!this.editandoAct;
    const obs = this.editandoAct ? this.prov.actualizarActividad(this.tourId, this.editandoAct.id, body) : this.prov.crearActividad(this.tourId, body);
    obs.subscribe({
      next: () => { this.guardandoAct = false; this.ok(editando ? 'Actividad actualizada.' : 'Actividad agregada.'); this.cancelarEdicionAct(); this.recargarActs(); },
      error: e => { this.guardandoAct = false; this.erroresAct = erroresPorCampo(e); this.ko(e, 'No se pudo guardar la actividad.'); },
    });
  }

  mover(a: Actividad, delta: number): void {
    const nuevo = a.orden + delta;
    if (nuevo < 1 || nuevo > this.actividades.length) return;
    this.prov.actualizarActividad(this.tourId, a.id, { orden: nuevo }).subscribe({ next: () => this.recargarActs(), error: e => this.ko(e, 'No se pudo reordenar.') });
  }

  eliminarAct(a: Actividad): void {
    this.prov.eliminarActividad(this.tourId, a.id).subscribe({
      next: () => { this.ok('Actividad eliminada.'); this.recargarActs(); },
      error: e => this.ko(e, 'No se pudo eliminar la actividad.'),
    });
  }

  private recargarActs(): void { this.prov.actividades(this.tourId).subscribe(a => this.actividades = a); }
}
