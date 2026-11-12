<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

/**
 * Etapa 2 — Flujo de aprobación, IGV 18% e idempotencia.
 *
 * - usuarios: avatar_url (HU-V03).
 * - reservas_habitaciones: estado 'rechazada', solicitud agrupada, subtotal/impuesto, decisión del proveedor (HU-V07, HU-P14).
 * - reservas_tour: subtotal/impuesto, últimos 4 dígitos de tarjeta (HU-V10).
 * - idempotency_keys: respuestas guardadas para reintentos seguros (HU-V07, HU-V10).
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('usuarios', function (Blueprint $table) {
            $table->string('avatar_url', 500)->nullable()->after('ruc');
        });

        // Bases de datos existentes (MySQL): agregar 'rechazada' al enum.
        // Las instalaciones nuevas ya lo traen desde la migración original.
        if (DB::getDriverName() === 'mysql') {
            DB::statement("ALTER TABLE reservas_habitaciones MODIFY estado ENUM('pendiente','confirmada','rechazada','cancelada') NOT NULL DEFAULT 'pendiente'");
        }

        Schema::table('reservas_habitaciones', function (Blueprint $table) {
            $table->string('solicitud_codigo', 20)->nullable()->after('codigo_reserva')->index();
            $table->decimal('subtotal', 12, 2)->default(0)->after('precio_por_noche');
            $table->decimal('impuesto', 12, 2)->default(0)->after('subtotal');
            $table->string('tarjeta_ultimos4', 4)->nullable()->after('total');
            $table->string('motivo_rechazo', 300)->nullable()->after('estado');
            $table->foreignId('decidido_por')->nullable()->after('motivo_rechazo')->constrained('usuarios')->nullOnDelete();
            $table->timestamp('decidido_en')->nullable()->after('decidido_por');
            $table->timestamp('cancelado_en')->nullable()->after('decidido_en');
        });

        Schema::table('reservas_tour', function (Blueprint $table) {
            $table->decimal('subtotal', 10, 2)->default(0)->after('precio_unitario');
            $table->decimal('impuesto', 10, 2)->default(0)->after('subtotal');
            $table->string('tarjeta_ultimos4', 4)->nullable()->after('total');
            $table->timestamp('cancelado_en')->nullable()->after('estado');
        });

        Schema::create('idempotency_keys', function (Blueprint $table) {
            $table->id();
            $table->foreignId('usuario_id')->constrained('usuarios')->cascadeOnDelete();
            $table->string('clave', 100);
            $table->string('ruta', 150);
            $table->string('huella', 64);               // hash del cuerpo enviado
            $table->unsignedSmallInteger('status')->nullable();
            $table->longText('respuesta')->nullable();  // null = en proceso
            $table->timestamps();
            $table->unique(['usuario_id', 'clave']);
        });

        // Recalcular subtotal / impuesto de reservas previas (total histórico = subtotal sin IGV)
        DB::table('reservas_habitaciones')->where('subtotal', 0)->update([
            'subtotal' => DB::raw('total'),
            'impuesto' => DB::raw('ROUND(total * 0.18, 2)'),
            'total'    => DB::raw('ROUND(total * 1.18, 2)'),
        ]);
        DB::table('reservas_tour')->where('subtotal', 0)->update([
            'subtotal' => DB::raw('total'),
            'impuesto' => DB::raw('ROUND(total * 0.18, 2)'),
            'total'    => DB::raw('ROUND(total * 1.18, 2)'),
        ]);
        // Los tours se confirman al pagar (HU-V09): datos antiguos 'pendiente' pasan a 'confirmada'.
        DB::table('reservas_tour')->where('estado', 'pendiente')->update(['estado' => 'confirmada']);
    }

    public function down(): void
    {
        Schema::dropIfExists('idempotency_keys');

        Schema::table('reservas_tour', function (Blueprint $table) {
            $table->dropColumn(['subtotal', 'impuesto', 'tarjeta_ultimos4', 'cancelado_en']);
        });

        Schema::table('reservas_habitaciones', function (Blueprint $table) {
            $table->dropConstrainedForeignId('decidido_por');
            $table->dropColumn(['solicitud_codigo', 'subtotal', 'impuesto', 'tarjeta_ultimos4',
                'motivo_rechazo', 'decidido_en', 'cancelado_en']);
        });

        Schema::table('usuarios', function (Blueprint $table) {
            $table->dropColumn('avatar_url');
        });
    }
};
