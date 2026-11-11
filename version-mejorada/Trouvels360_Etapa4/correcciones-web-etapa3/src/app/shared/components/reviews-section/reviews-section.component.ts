import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Review, ResumenResenas, PuedoOpinar } from '../../models/review.model';
import { ReviewsService } from '../../services/reviews.service';
import { AuthService } from '../../../core/services/auth.service';
import { ReviewFormComponent } from '../review-form/review-form.component';
import { mensajeDeError } from '../../../core/utils/api-error';

/**
 * TRV-403 — Sección de reseñas del detalle de hotel o tour.
 * - Promedio y total desde /reviews/resumen (todas las reseñas, no solo la página).
 * - Lista paginada (5 por página), más recientes primero.
 * - "Deja tu opinión" solo si /reviews/puedo-opinar lo permite; si no, se muestra el motivo.
 * - El autor puede editar o eliminar su reseña.
 */
@Component({
  selector: 'app-reviews-section',
  standalone: true,
  imports: [CommonModule, RouterModule, ReviewFormComponent],
  templateUrl: './reviews-section.component.html',
  styleUrls: ['./reviews-section.component.css'],
})
export class ReviewsSectionComponent implements OnInit {
  private reviews$ = inject(ReviewsService);
  private auth = inject(AuthService);
  private router = inject(Router);

  @Input() servicioId!: number;
  @Input() servicioTipo: 'hotel' | 'tour' = 'hotel';

  reviews: Review[] = [];
  page = 1;
  lastPage = 1;
  loading = true;
  error = false;
  errorMessage = '';

  resumen: ResumenResenas | null = null;
  textoCalificacion = '';

  logueado = false;
  esViajero = false;
  elegibilidad: PuedoOpinar | null = null;
  currentUserId: number | null = null;

  mostrarFormulario = false;
  editando: { id: number; calificacion: number; comentario: string } | null = null;
  aEliminar: Review | null = null;
  eliminando = false;
  aviso = '';
  avisoError = '';

  get totalReviews(): number { return this.resumen?.total ?? 0; }
  get promedioCalificacion(): number { return this.resumen?.promedio ?? 0; }

  ngOnInit(): void {
    this.currentUserId = this.auth.getCurrentUserId();
    this.logueado = this.auth.isAuthenticated();
    this.esViajero = this.auth.getRole() === 'viajero';
    this.cargarResumen();
    this.loadReviews();
    this.cargarElegibilidad();
  }

  cargarResumen(): void {
    this.reviews$.resumen(this.servicioId).subscribe({
      next: r => { this.resumen = r; this.textoCalificacion = this.reviews$.textoCalificacion(r.promedio); },
      error: () => this.resumen = null,
    });
  }

  cargarElegibilidad(): void {
    if (!this.logueado || !this.esViajero) { this.elegibilidad = null; return; }
    this.reviews$.puedoOpinar(this.servicioId).subscribe({ next: e => this.elegibilidad = e, error: () => this.elegibilidad = null });
  }

  loadReviews(page = this.page): void {
    this.loading = true;
    this.error = false;
    this.reviews$.listar(this.servicioId, page).subscribe({
      next: r => {
        this.reviews = r.data;
        this.page = r.meta.current_page;
        this.lastPage = r.meta.last_page;
        this.loading = false;
      },
      error: e => {
        this.error = true;
        this.errorMessage = mensajeDeError(e, 'No se pudieron cargar las reseñas.');
        this.loading = false;
      },
    });
  }

  irPagina(p: number): void { if (p >= 1 && p <= this.lastPage) this.loadReviews(p); }

  abrirFormulario(): void {
    this.aviso = ''; this.avisoError = '';
    if (!this.logueado) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    if (this.elegibilidad?.mi_resena) { this.editarPropia(); return; }
    this.editando = null;
    this.mostrarFormulario = true;
  }

  editarPropia(): void {
    const mia = this.elegibilidad?.mi_resena;
    if (!mia) return;
    this.editando = { id: mia.id, calificacion: mia.calificacion, comentario: mia.comentario };
    this.mostrarFormulario = true;
  }

  editar(r: Review): void {
    this.editando = { id: r.id, calificacion: r.calificacion, comentario: r.comentario };
    this.mostrarFormulario = true;
  }

  cerrarFormulario(): void { this.mostrarFormulario = false; this.editando = null; }

  onGuardada(): void {
    const eraEdicion = !!this.editando;
    this.cerrarFormulario();
    this.aviso = eraEdicion ? 'Tu reseña se actualizó.' : '¡Gracias! Tu reseña se publicó.';
    this.refrescar(eraEdicion ? this.page : 1);
  }

  isAuthor(r: Review): boolean { return this.currentUserId !== null && r.usuario.id === this.currentUserId; }

  pedirEliminar(r: Review): void { this.aviso = ''; this.avisoError = ''; this.aEliminar = r; }

  confirmarEliminar(): void {
    const r = this.aEliminar;
    if (!r) return;
    this.eliminando = true;
    this.reviews$.eliminar(r.id).subscribe({
      next: () => {
        this.eliminando = false; this.aEliminar = null; this.aviso = 'Reseña eliminada.';
        this.refrescar(this.reviews.length === 1 && this.page > 1 ? this.page - 1 : this.page);
      },
      error: e => { this.eliminando = false; this.aEliminar = null; this.avisoError = mensajeDeError(e, 'No se pudo eliminar la reseña.'); },
    });
  }

  private refrescar(page: number): void {
    this.cargarResumen();
    this.loadReviews(page);
    this.cargarElegibilidad();
  }

  getEstrellas(n: number): { llena: boolean }[] { return Array.from({ length: 5 }, (_, i) => ({ llena: i < n })); }
  paginas(): number[] { return this.lastPage > 1 ? Array.from({ length: this.lastPage }, (_, i) => i + 1) : []; }
}
