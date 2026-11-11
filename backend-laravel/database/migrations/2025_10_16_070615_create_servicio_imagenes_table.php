<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('servicio_imagenes', function (Blueprint $table) {
            $table->id();

            // Relación con servicios (1:N)
            $table->foreignId('servicio_id')
                ->constrained('servicios')
                ->cascadeOnDelete();

            // Solo URL y texto alternativo
            $table->string('url', 500);
            $table->string('alt', 150)->nullable();

            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('servicio_imagenes');
    }
};
