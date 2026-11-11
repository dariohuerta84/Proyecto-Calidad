<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

class ServicioImagen extends Model
{
    use HasFactory;
    use SoftDeletes;

    protected $table = 'servicio_imagenes';

    protected $fillable = [
        'servicio_id',
        'url',
        'alt',
    ];
    protected $dates = ['deleted_at'];

    public function servicio()
    {
        return $this->belongsTo(Servicio::class);
    }

    public function scopeRecientes($query)
    {
        return $query->orderByDesc('created_at');
    }
}
