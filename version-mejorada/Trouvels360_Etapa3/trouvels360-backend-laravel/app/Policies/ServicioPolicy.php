<?php

namespace App\Policies;

use App\Models\Servicio;
use App\Models\Usuario;

class ServicioPolicy
{
    /**
     * Determina si el usuario puede ver la lista de servicios.
     * (En este proyecto, las rutas de index/show son públicas, así que no aplicamos restricción aquí.)
     */
    public function viewAny(Usuario $usuario): bool
    {
        return false; // no usamos auth para index
    }

    /**
     * Determina si el usuario puede ver un servicio específico.
     * (También lo dejamos público, por eso devolvemos false para no usar policy.)
     */
    public function view(Usuario $usuario, Servicio $servicio): bool
    {
        return false; // no usamos auth para show
    }

    /**
     * Determina si el usuario puede crear un servicio.
     * Solo los usuarios con rol "proveedor" pueden crear servicios.
     */
    public function create(Usuario $user): bool
    {
        return $user->rol === 'proveedor';
    }

    /**
     * Determina si el usuario puede actualizar un servicio.
     * Solo el proveedor que lo creó (dueño) puede modificarlo.
     */
    public function update(Usuario $user, Servicio $servicio): bool
    {
        return $user->rol === 'proveedor' && $servicio->proveedor_id === $user->id;
    }

    /**
     * Determina si el usuario puede eliminar un servicio.
     * Solo el proveedor dueño puede eliminarlo.
     */
    public function delete(Usuario $user, Servicio $servicio): bool
    {
        return $user->rol === 'proveedor' && $servicio->proveedor_id === $user->id;
    }

    /**
     * Determina si el usuario puede restaurar un servicio eliminado.
     * (No usamos esta funcionalidad en el proyecto.)
     */
    public function restore(Usuario $usuario, Servicio $servicio): bool
    {
        return false;
    }

    /**
     * Determina si el usuario puede eliminar permanentemente un servicio.
     * (No usamos esta funcionalidad en el proyecto.)
     */
    public function forceDelete(Usuario $usuario, Servicio $servicio): bool
    {
        return false;
    }
}
