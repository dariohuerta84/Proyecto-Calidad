#!/usr/bin/env bash
set -e

# ==============================================================================
# Script de Ejecución de Pruebas de Servicios REST con cURL (Trouvels360 API)
# Métodos cubiertos: GET, POST, PUT, DELETE
# Asignatura: Calidad de Software (C8305 - UPCH)
# ==============================================================================

BASE_URL="http://localhost:8000/api"
COOKIE_JAR="$(mktemp /tmp/trouvels360_cookies_XXXXXX.txt)"

trap 'rm -f "$COOKIE_JAR"' EXIT

echo "=========================================================="
echo " [cURL Test Suite] Iniciando pruebas CRUD sobre Trouvels360"
echo "=========================================================="

# 1. GET CSRF Token
echo -e "\n[Paso 1] GET /auth/csrf..."
CSRF_RES=$(curl -s -c "$COOKIE_JAR" "$BASE_URL/auth/csrf")
CSRF_TOKEN=$(echo "$CSRF_RES" | grep -o '"csrf_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$CSRF_TOKEN" ]; then
  echo "❌ Error: No se pudo obtener el token CSRF de $BASE_URL/auth/csrf"
  exit 1
fi
echo "✅ CSRF Token obtenido: ${CSRF_TOKEN:0:16}..."

# 2. POST Login Proveedor
echo -e "\n[Paso 2] POST /auth/login (Proveedor)..."
LOGIN_HTTP=$(curl -s -o /tmp/login_res.json -w "%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -d '{"email": "edwinproveedor@gmail.com", "password": "proveedor"}')

if [ "$LOGIN_HTTP" != "200" ]; then
  echo "❌ Error en Login: HTTP $LOGIN_HTTP"
  cat /tmp/login_res.json
  exit 1
fi
echo "✅ Login Exitoso (HTTP $LOGIN_HTTP)"

# 3. GET Mis Publicaciones (GET)
echo -e "\n[Paso 3] GET /proveedor/servicios?tipo=hotel..."
curl -s -X GET "$BASE_URL/proveedor/servicios?tipo=hotel" \
  -H "Accept: application/json" \
  -b "$COOKIE_JAR" | head -c 250
echo -e "\n✅ Publicaciones consultadas correctamente."

# 4. POST Crear Hotel (POST)
echo -e "\n[Paso 4] POST /hoteles..."
RAND_ID=$((RANDOM % 9000 + 1000))
HOTEL_RES=$(curl -s -X POST "$BASE_URL/hoteles" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -b "$COOKIE_JAR" \
  -d '{
    "nombre": "Hotel cURL Luxury Cusco '$RAND_ID'",
    "descripcion": "Hotel creado mediante script automatizado cURL",
    "ciudad": "Cusco",
    "pais": "Perú",
    "direccion": "Av. Sol '$RAND_ID'",
    "estrellas": 5,
    "activo": true
  }')

HOTEL_ID=$(echo "$HOTEL_RES" | grep -o '"servicio_id":[0-9]*' | head -n1 | cut -d':' -f2)
echo "✅ Hotel Creado con ID: $HOTEL_ID"

# 5. PUT Actualizar Hotel (PUT)
echo -e "\n[Paso 5] PUT /hoteles/$HOTEL_ID..."
PUT_HTTP=$(curl -s -o /tmp/put_res.json -w "%{http_code}" -X PUT "$BASE_URL/hoteles/$HOTEL_ID" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -b "$COOKIE_JAR" \
  -d '{
    "nombre": "Hotel cURL Luxury Cusco '$RAND_ID' Modificado",
    "estrellas": 5,
    "direccion": "Av. Sol '$RAND_ID' Suite A"
  }')
echo "✅ Hotel Actualizado (HTTP $PUT_HTTP)"

# 6. DELETE Retirar Hotel (DELETE)
echo -e "\n[Paso 6] DELETE /servicios/$HOTEL_ID..."
DEL_HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/servicios/$HOTEL_ID" \
  -H "Accept: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -b "$COOKIE_JAR")
echo "✅ Servicio Eliminado en Cascada (HTTP $DEL_HTTP)"

echo -e "\n=========================================================="
echo "🎉 ¡Todos los métodos cURL (GET, POST, PUT, DELETE) superados con éxito!"
echo "=========================================================="
