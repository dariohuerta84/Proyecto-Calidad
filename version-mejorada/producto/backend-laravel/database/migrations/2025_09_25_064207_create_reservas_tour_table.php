<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    /** Registra reservas por salida, con estado y snapshot de precio. */
    public function up(): void
    {
        Schema::create('reservas_tour', function (Blueprint $table) {
            $table->id();
            $table->string('codigo_reserva', 20)->unique();

            $table->foreignId('usuario_id')
                  ->constrained('usuarios')
                  ->cascadeOnDelete(); // al borrar viajero se van sus reservas

            $table->foreignId('salida_id')
                  ->constrained('tour_salidas')
                  ->cascadeOnDelete(); // al borrar salida se van sus reservas

            $table->unsignedSmallInteger('personas'); // total de personas
            $table->enum('estado', ['pendiente','confirmada','cancelada'])->default('pendiente');

            // snapshot de precio para evitar cambios retroactivos
            $table->decimal('precio_unitario', 10, 2);
            $table->decimal('total', 10, 2);

            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();

            $table->index(['usuario_id','estado']);
            $table->index(['salida_id','estado']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('reservas_tour');
    }
};
