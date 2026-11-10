<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class ImageSearchController extends Controller
{
    /**
     * Proxy interno hacia Unsplash API.
     */
    public function search(Request $request)
    {
        // 1. Obtener parámetro q
        $query = $request->query('q');

        if (empty($query)) {
            return response()->json([
                'error' => 'Query parameter "q" is required'
            ], 400);
        }

        // 2. Obtener Access Key desde config/unsplash.php
        $accessKey = config('unsplash.access_key');

        if (empty($accessKey)) {
            Log::error('Unsplash Access Key is not configured');
            return response()->json([
                'error' => 'Image search is not configured'
            ], 500);
        }

        // 3. Llamar a Unsplash con Http:: (Laravel)
        try {
            $response = Http::withHeaders([
                'Authorization' => 'Client-ID ' . $accessKey,
                'Accept-Version' => 'v1',
                'User-Agent' => 'Trouvels360-Backend'
            ])->get('https://api.unsplash.com/search/photos', [
                'query' => $query,
                'per_page' => 12,
                'orientation' => 'landscape'
            ]);
        } catch (\Exception $e) {
            Log::error('Error calling Unsplash API: ' . $e->getMessage());

            return response()->json([
                'error' => 'Failed to fetch images from unsplash'
            ], 502);
        }

        // 4. Validar errores HTTP
        if ($response->failed()) {
            Log::error("Unsplash API returned status " . $response->status());

            return response()->json([
                'error' => 'Image service returned an error'
            ], $response->status());
        }

        $data = $response->json();

        if (!isset($data['results'])) {
            Log::error('Invalid response from Unsplash API');

            return response()->json([
                'error' => 'Invalid response from image service'
            ], 502);
        }

        // 5. Limpiar el resultado
        $cleanedResults = array_map(function ($image) {
            return [
                'id'          => $image['id'],
                'alt'         => $image['alt_description'],
                'url_small'   => $image['urls']['small'],
                'url_regular' => $image['urls']['regular'],
                'user_name'   => $image['user']['name'],
            ];
        }, $data['results']);

        // 6. Devolver respuesta final
        return response()->json([
            'images' => $cleanedResults
        ]);
    }
}
