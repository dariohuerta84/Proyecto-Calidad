import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Review } from '../../models/review.model';
import { ReviewsService } from '../../services/reviews.service';
import { mensajeDeError, erroresPorCampo } from '../../../core/utils/api-error';

/**
 * TRV-401 / TRV-402 — Crear o editar la reseña propia.
 * Calificación 1–5 y comentario de 10 a 300 caracteres (mismas reglas que la API).
 */
@Component({
  selector: 'app-review-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-form.component.html',
  styleUrls: ['./review-form.component.css'],
})
export class ReviewFormComponent implements OnInit {
  private reviews = inject(ReviewsService);

  @Input() servicioId!: number;
  @Input() servicioTipo: 'hotel' | 'tour' = 'hotel';
  /** Si se pasa, el formulario edita esa reseña. */
  @Input() resena: { id: number; calificacion: number; comentario: string } | null = null;
  @Output() onSubmit = new EventEmitter<Review>();
  @Output() onCancel = new EventEmitter<void>();

  calificacion = 0;
  comentario = '';
  hoverCalificacion = 0;
  submitting = false;
  error = false;
  errorMessage = '';

  readonly MAX_CARACTERES = 300;
  readonly MIN_CARACTERES = 10;
  readonly estrellasArray = [1, 2, 3, 4, 5];

  get editando(): boolean { return !!this.resena; }
  get estrellasAMostrar(): number { return this.hoverCalificacion || this.calificacion; }
  get largo(): number { return this.comentario.trim().length; }

  ngOnInit(): void {
    if (this.resena) {
      this.calificacion = this.resena.calificacion;
      this.comentario = this.resena.comentario;
    }
  }

  seleccionarCalificacion(n: number): void { this.calificacion = n; this.error = false; }
  onMouseEnter(n: number): void { this.hoverCalificacion = n; }
  onMouseLeave(): void { this.hoverCalificacion = 0; }

  get validationMessage(): string {
    if (this.calificacion < 1 || this.calificacion > 5) return 'Selecciona una calificación de 1 a 5 estrellas.';
    if (this.largo === 0) return 'Escribe un comentario.';
    if (this.largo < this.MIN_CARACTERES) return `El comentario debe tener al menos ${this.MIN_CARACTERES} caracteres.`;
    if (this.largo > this.MAX_CARACTERES) return `El comentario no puede superar los ${this.MAX_CARACTERES} caracteres.`;
    return '';
  }

  get contadorColor(): string {
    if (this.largo > this.MAX_CARACTERES) return '#b71c1c';
    if (this.largo > 0 && this.largo < this.MIN_CARACTERES) return '#8a5300';
    return '#5e6c84';
  }

  enviarReview(): void {
    if (this.submitting) return;
    const v = this.validationMessage;
    if (v) { this.error = true; this.errorMessage = v; return; }

    this.submitting = true;
    this.error = false;
    const datos = { calificacion: this.calificacion, comentario: this.comentario.trim() };
    const obs = this.resena
      ? this.reviews.actualizar(this.resena.id, datos)
      : this.reviews.crear({ servicio_id: this.servicioId, ...datos });

    obs.subscribe({
      next: r => { this.submitting = false; this.onSubmit.emit(r); },
      error: e => {
        this.submitting = false;
        this.error = true;
        const campos = erroresPorCampo(e);
        this.errorMessage = Object.values(campos)[0] || mensajeDeError(e, 'No se pudo guardar la reseña.');
      },
    });
  }

  cancelar(): void { if (!this.submitting) this.onCancel.emit(); }
}
