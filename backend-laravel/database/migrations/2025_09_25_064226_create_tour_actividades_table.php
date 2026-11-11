<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    /** Lista de actividades/etapas del tour (ordenadas). */
    public function up(): void
    {
        Schema::create('tour_actividades', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('servicio_id'); // FK al servicio (tipo='tour')

            $table->string('titulo', 150);
            $table->text('descripcion')->nullable();
            $table->unsignedSmallInteger('orden')->default(1);      // 1 = primero
            $table->unsignedSmallInteger('duracion_min')->nullable(); // estimado
            $table->string('direccion', 255)->nullable();
            $table->string('imagen_url', 500)->nullable();

            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();

            $table->foreign('servicio_id')
                  ->references('id')->on('servicios')
                  ->onDelete('cascade');

            $table->unique(['servicio_id','orden']); // no repetir orden en el mismo tour
            $table->index(['servicio_id','orden']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('tour_actividades');
    }
};
