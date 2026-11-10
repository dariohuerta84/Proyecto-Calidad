<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    /** Crea salidas con fecha/hora y cupo efectivo. */
    public function up(): void
    {
        Schema::create('tour_salidas', function (Blueprint $table) {
            $table->id();
            $table->foreignId('servicio_id')
                  ->constrained('servicios')
                  ->cascadeOnDelete(); // borrar servicio elimina sus salidas

            $table->date('fecha');
            $table->time('hora');

            $table->unsignedSmallInteger('cupo_total');              // capacidad efectiva
            $table->unsignedSmallInteger('cupo_reservado')->default(0); // ocupación acumulada
            $table->enum('estado', ['programada','cerrada','cancelada'])->default('programada');

            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();

            $table->unique(['servicio_id','fecha','hora']); // evita duplicar misma salida
            $table->index(['servicio_id','fecha','estado']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('tour_salidas');
    }
};
