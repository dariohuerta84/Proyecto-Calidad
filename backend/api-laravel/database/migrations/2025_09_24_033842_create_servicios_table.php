<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('servicios', function (Blueprint $table) {
            $table->id();

            $table->foreignId('proveedor_id')
                ->constrained('usuarios')
                ->cascadeOnDelete();

            $table->string('nombre', 150);
            $table->enum('tipo', ['hotel', 'tour']);
            $table->text('descripcion')->nullable();

            // Localización común
            $table->string('ciudad', 100);
            $table->string('pais', 100);

            // Coordenadas
            $table->decimal('latitud', 10, 6)->nullable();
            $table->decimal('longitud', 10, 6)->nullable();

            // Imagen principal opcional (portada/fallback)
            $table->string('imagen_url', 500)->nullable();

            $table->boolean('activo')->default(true);
            
            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();

            // Índices prácticos
            $table->index(['proveedor_id', 'tipo', 'activo']); // panel de proveedor
            $table->index(['pais', 'ciudad']);
            $table->index(['ciudad', 'tipo']);
            $table->index(['activo', 'tipo']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('servicios');
    }
};
