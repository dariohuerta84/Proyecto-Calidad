import { Component, OnInit, inject, Pipe, PipeTransform } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ProveedorService, Indicadores } from '../../services/proveedor.service';
import { ServiceData } from '../../../../shared/models';
import { Paginado } from '../../../../shared/models/reserva.model';
import { mensajeDeError } from '../../../../core/utils/api-error';

/** Convierte una calificación en estrellas llenas / medias / vacías. */
@Pipe({ name: 'starArray', standalone: true })
export class StarArrayPipe implements PipeTransform {
  transform(rating: number | null | undefined): ('full' | 'half' | 'empty')[] {
    if (!rating || rating <= 0) return [];
    const stars: ('full' | 'half' | 'empty')[] = [];
    const entero = Math.floor(rating);
    for (let i = 0; i < entero; i++) stars.push('full');
    if (rating - entero >= 0.25 && rating - entero < 0.75) stars.push('half');
    while (stars.length < 5) stars.push('empty');
    return stars;
  }
}

/**
 * TRV-501 — Panel del proveedor: indicadores (desde la API, por estado) y publicaciones paginadas.
 * TRV-508 — Eliminar publicación con confirmación (409 si tiene reservas activas).
 */
@Component({
  selector: 'app-proveedor-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, StarArrayPipe],
  templateUrl: './proveedor.component.html',
  styleUrls: ['./proveedor.component.css'],
})
export class ProveedorComponent implements OnInit {
  private router = inject(Router);
  private prov = inject(ProveedorService);

  indicadores: Indicadores | null = null;
  pagina: Paginado<ServiceData> | null = null;
  page = 1;
  tipo = '';
  busqueda = '';

  cargando = true;
  error = '';
  mensaje = '';
  mensajeError = '';

  aEliminar: ServiceData | null = null;
  eliminando = false;

  get publications(): ServiceData[] { return this.pagina?.data ?? []; }

  ngOnInit(): void {
    this.cargarIndicadores();
    this.cargar();
  }

  cargarIndicadores(): void {
    this.prov.indicadores().subscribe({ next: i => this.indicadores = i, error: () => this.indicadores = null });
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.prov.servicios(this.page, this.tipo, this.busqueda).subscribe({
      next: p => { this.pagina = p; this.cargando = false; },
      error: e => { this.cargando = false; this.error = mensajeDeError(e, 'No pudimos cargar tus publicaciones.'); },
    });
  }

  filtrar(): void { this.page = 1; this.cargar(); }
  irPagina(p: number): void { this.page = p; this.cargar(); window.scrollTo({ top: 0 }); }
  paginas(): number[] {
    const last = this.pagina?.meta.last_page ?? 1;
    return last > 1 ? Array.from({ length: last }, (_, i) => i + 1) : [];
  }

  editPublication(id: number, tipo: string): void {
    this.router.navigate([tipo === 'hotel' ? '/proveedor/editar-hotel' : '/proveedor/editar-tour', id]);
  }

  pedirEliminar(pub: ServiceData): void {
    this.mensaje = '';
    this.mensajeError = '';
    this.aEliminar = pub;
  }

  confirmarEliminar(): void {
    const pub = this.aEliminar;
    if (!pub) return;
    this.eliminando = true;
    this.prov.eliminarServicio(pub.id).subscribe({
      next: () => {
        this.eliminando = false;
        this.aEliminar = null;
        this.mensaje = `«${pub.nombre}» se eliminó. Su historial de reservas se conserva.`;
        if (this.publications.length === 1 && this.page > 1) this.page--;
        this.cargar();
        this.cargarIndicadores();
      },
      error: e => {
        this.eliminando = false;
        this.aEliminar = null;
        this.mensajeError = mensajeDeError(e, 'No se pudo eliminar la publicación.');
      },
    });
  }
}
