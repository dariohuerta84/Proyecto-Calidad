<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('habitaciones', function (Blueprint $table) {
            $table->id();

            // Pertenece a un hotel (hoteles.servicio_id es la PK)
            $table->unsignedBigInteger('servicio_id');
            $table->foreign('servicio_id')
                ->references('servicio_id')->on('hoteles')
                ->onDelete('cascade');

            $table->string('nombre', 100);                 // p.ej. "Doble", "Familiar"
            $table->unsignedSmallInteger('capacidad_adultos');
            $table->unsignedSmallInteger('capacidad_ninos')->default(0);
            $table->unsignedInteger('cantidad');           // unidades de este tipo de habitación
            $table->decimal('precio_por_noche', 10, 2);    // tarifa base por noche
            $table->text('descripcion')->nullable();

            // Agregar soft deletes
            $table->softDeletes();

            $table->timestamps();

            // Evitar duplicados del mismo tipo de habitación en el mismo hotel
            $table->unique(['servicio_id', 'nombre']);

            // Índices útiles
            $table->index(['servicio_id', 'capacidad_adultos', 'capacidad_ninos']);
            $table->index(['servicio_id', 'precio_por_noche']);
        });

        // (Opcional) CHECKs si tu MySQL los aplica
        // Schema::table('habitaciones', function (Blueprint $table) {
        //     $table->check('capacidad_adultos >= 1');
        //     $table->check('capacidad_ninos >= 0');
        //     $table->check('cantidad > 0');
        //     $table->check('precio_por_noche >= 0');
        // });
    }

    public function down(): void
    {
        Schema::dropIfExists('habitaciones');
    }
};
