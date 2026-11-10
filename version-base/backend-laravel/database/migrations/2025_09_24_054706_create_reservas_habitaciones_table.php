<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('reservas_habitaciones', function (Blueprint $table) {
            $table->id();

            $table->string('codigo_reserva', 20)->unique();

            // Quién reserva
            $table->foreignId('usuario_id')
                ->constrained('usuarios')
                ->cascadeOnDelete();

            // Qué habitación reserva
            $table->foreignId('habitacion_id')
                ->constrained('habitaciones')
                ->cascadeOnDelete();

            // Rango de estadía
            $table->date('fecha_inicio');
            $table->date('fecha_fin');

            // Cuántas unidades de esa habitación
            $table->unsignedSmallInteger('cantidad')->default(1);

            // Estados
            $table->enum('estado', ['pendiente', 'confirmada', 'cancelada'])
                  ->default('pendiente');

            // Snapshot de precio y total
            $table->decimal('precio_por_noche', 10, 2);
            $table->decimal('total', 12, 2);

            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();

            // Índices
            $table->index(['habitacion_id', 'fecha_inicio', 'fecha_fin']);
            $table->index(['usuario_id', 'estado']);
            $table->index(['estado', 'fecha_inicio', 'fecha_fin']);

            // (Opcional) Evitar reservas duplicadas exactas del mismo usuario
            // $table->unique(['usuario_id','habitacion_id','fecha_inicio','fecha_fin']);
        });

        // (Opcional) CHECKs si tu MySQL los aplica
        // Schema::table('reservas_habitaciones', function (Blueprint $table) {
        //     $table->check('fecha_fin > fecha_inicio');
        //     $table->check('cantidad > 0');
        //     $table->check('precio_por_noche >= 0');
        //     $table->check('total >= 0');
        // });
    }

    public function down(): void
    {
        Schema::dropIfExists('reservas_habitaciones');
    }
};
