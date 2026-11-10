<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    /** Define configuración base del tour (precio único y capacidad por salida por defecto). */
    public function up(): void
    {
        Schema::create('tours', function (Blueprint $table) {
            $table->unsignedBigInteger('servicio_id')->primary(); // PK = FK a servicios.id
            $table->enum('categoria', ['Gastronomía','Aventura','Cultura','Relajación']);
            $table->unsignedSmallInteger('duracion')->nullable(); // ej. 240 = 4h
            $table->decimal('precio', 10, 2);                     // precio por persona
            
            // Agregar soft deletes
            $table->softDeletes();
            $table->timestamps();

            $table->foreign('servicio_id')
                ->references('id')->on('servicios')
                ->onDelete('cascade');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('tours');
    }
};
