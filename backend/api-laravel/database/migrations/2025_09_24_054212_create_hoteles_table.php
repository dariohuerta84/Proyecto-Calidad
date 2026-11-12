<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('hoteles', function (Blueprint $table) {
            // 1:1 con servicios (aplica cuando servicios.tipo='hotel')
            $table->unsignedBigInteger('servicio_id')->primary();

            $table->string('direccion', 255);
            $table->unsignedTinyInteger('estrellas')->nullable(); // 1..5

            $table->foreign('servicio_id')
                ->references('id')->on('servicios')
                ->onDelete('cascade');

            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();
        });

        // (Opcional) CHECKs vía DB::statement si tu versión MySQL los soporta
        // DB::statement('ALTER TABLE hoteles ADD CONSTRAINT chk_estrellas CHECK (estrellas IS NULL OR (estrellas BETWEEN 1 AND 5))');
    }

    public function down(): void
    {
        Schema::dropIfExists('hoteles');
    }
};
