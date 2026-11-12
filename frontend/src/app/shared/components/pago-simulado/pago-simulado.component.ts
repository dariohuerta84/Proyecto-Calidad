import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PagoSimulado } from '../../models/reserva.model';

/** Valida un número de tarjeta con el algoritmo de Luhn. */
export function luhnValido(numero: string): boolean {
  const n = (numero || '').replace(/\D/g, '');
  if (n.length !== 16) return false;
  let suma = 0;
  let alternar = false;
  for (let i = n.length - 1; i >= 0; i--) {
    let d = +n[i];
    if (alternar) { d *= 2; if (d > 9) d -= 9; }
    suma += d;
    alternar = !alternar;
  }
  return suma % 10 === 0;
}

/** true si MM/AA es un mes real y no ha vencido (vale hasta el último día del mes). */
export function vencimientoValido(v: string): boolean {
  const m = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(v || '');
  if (!m) return false;
  const finDeMes = new Date(2000 + +m[2], +m[1], 0, 23, 59, 59);
  return finDeMes.getTime() >= Date.now();
}

/**
 * Formulario de pago simulado (HU-V07 / HU-V10).
 * Reglas: titular (letras, 2–100), 16 dígitos con Luhn, vencimiento MM/AA vigente, CVC de 3 dígitos.
 * Emite el pago validado; el componente padre llama a la API y muestra el error que devuelva.
 */
@Component({
  selector: 'app-pago-simulado',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe],
  templateUrl: './pago-simulado.component.html',
  styleUrls: ['./pago-simulado.component.css'],
})
export class PagoSimuladoComponent {
  @Input() total = 0;
  @Input() procesando = false;
  @Input() errorServidor = '';
  @Output() confirmar = new EventEmitter<PagoSimulado>();
  @Output() cancelar = new EventEmitter<void>();

  titular = '';
  numero = '';
  vencimiento = '';
  cvc = '';
  intentado = false;

  get errores(): Record<string, string> {
    const e: Record<string, string> = {};
    const t = this.titular.trim();
    if (t.length < 2) e['titular'] = 'Ingresa el nombre del titular.';
    else if (!/^[\p{L}\s'.-]+$/u.test(t)) e['titular'] = 'El titular solo puede contener letras.';
    const n = this.numero.replace(/\s/g, '');
    if (n.length !== 16) e['numero'] = 'El número debe tener 16 dígitos.';
    else if (!luhnValido(n)) e['numero'] = 'El número de tarjeta no es válido.';
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(this.vencimiento)) e['vencimiento'] = 'Usa el formato MM/AA.';
    else if (!vencimientoValido(this.vencimiento)) e['vencimiento'] = 'La tarjeta está vencida.';
    if (!/^\d{3}$/.test(this.cvc)) e['cvc'] = 'El CVC debe tener 3 dígitos.';
    return e;
  }

  get valido(): boolean { return Object.keys(this.errores).length === 0; }

  formatearNumero(v: string): void {
    const d = v.replace(/\D/g, '').slice(0, 16);
    this.numero = d.replace(/(.{4})/g, '$1 ').trim();
  }

  formatearVencimiento(v: string): void {
    let d = v.replace(/\D/g, '').slice(0, 4);
    if (d.length >= 2) d = d.slice(0, 2) + '/' + d.slice(2);
    this.vencimiento = d;
  }

  soloDigitos(v: string): void { this.cvc = v.replace(/\D/g, '').slice(0, 3); }

  enviar(): void {
    this.intentado = true;
    if (!this.valido || this.procesando) return;
    this.confirmar.emit({
      titular: this.titular.trim(),
      numero: this.numero.replace(/\s/g, ''),
      vencimiento: this.vencimiento,
      cvc: this.cvc,
    });
  }
}
