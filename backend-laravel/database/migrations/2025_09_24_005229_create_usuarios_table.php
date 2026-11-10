<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /** Crea la tabla 'usuarios' lista para autenticación y roles viajero/proveedor. */
    public function up(): void
    {
        Schema::create('usuarios', function (Blueprint $table) {
            $table->id();

            // Campos para VIAJERO (personales)
            $table->string('nombre', 100)->nullable();    // <- nullable para proveedores
            $table->string('apellido', 100)->nullable();  // <- nullable para proveedores

            // Campos comunes
            $table->string('email', 150)->unique();
            $table->timestamp('email_verified_at')->nullable();
            $table->string('password');

            // Rol
            $table->enum('rol', ['viajero', 'proveedor'])->default('viajero');

            // Campos para PROVEEDOR
            $table->string('empresa_nombre', 150)->nullable();
            $table->string('telefono', 20)->nullable();  // +51 9########
            $table->string('ruc', 11)->nullable();

            // Índices específicos
            $table->unique('ruc');
            $table->index('telefono');

            // Autenticación
            $table->rememberToken();
            // Agregar soft deletes
            $table->softDeletes();
            $table->timestamps();
        });
    }

    /** Elimina la tabla 'usuarios' si existe. */
    public function down(): void
    {
        Schema::dropIfExists('usuarios');
    }
};
