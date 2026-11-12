<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('tour_items', function (Blueprint $table) {
            $table->id();
            $table->foreignId('servicio_id')
                  ->constrained('servicios')
                  ->cascadeOnDelete(); // borrar servicio elimina sus items
            $table->string('nombre'); // nombre del item
            $table->string('icono')->nullable(); // emoji o icono asociado al item

            // Agregar soft deletes
            $table->softDeletes();
            
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('tour_items');
    }
};
