# Catálogo de Comandos cURL y Configuración de Samplers en Apache JMeter

**Curso:** Calidad de Software (C8305 — UPCH)  
**Sistema:** Trouvels360 API (Laravel 11 + FastAPI + MySQL 8.0)  
**Objetivo:** Especificación de comandos cURL listos para importación en Apache JMeter vía el menú **`Tools → Import from cURL`**, con la parametrización de sus componentes de prueba (`HTTP Request Sampler`, `Header Manager`, `JSON Extractor`, `Response Assertion`).

---

## Flujo de Seguridad y Autenticación Previa (Handshake CSRF + JWT)

Debido a los middlewares `VerifyApiCsrf` y `AttachJwtFromCookie` del backend, toda petición que altere el estado (`POST`, `PUT`, `DELETE`) requiere la cookie `XSRF-TOKEN`, el encabezado `X-XSRF-TOKEN` y la cookie `access_token`.

### Paso 0A: Obtención de Token CSRF (GET)
```bash
curl -X GET "http://localhost:8000/api/auth/csrf" \
  -H "Accept: application/json" \
  -c cookies.txt
```

### Paso 0B: Login de Proveedor (POST)
```bash
curl -X POST "http://localhost:8000/api/auth/login" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${CSRF_TOKEN}" \
  -b cookies.txt -c cookies.txt \
  -d '{"email": "edwinproveedor@gmail.com", "password": "proveedor"}'
```

---

## Caso CP01: Método GET — Consultar Publicaciones del Proveedor
### Comando cURL
```bash
curl -X GET "http://localhost:8000/api/proveedor/servicios?tipo=hotel" \
  -H "Accept: application/json" \
  -b cookies.txt
```

### Configuración del Sampler en Apache JMeter
```text
1.  Elemento: HTTP Request Sampler
2.  Nombre: HTTP Request - GET Mis Publicaciones Hoteles
3.  Protocolo: http
4.  Server Name / IP: localhost
5.  Port Number: 8000
6.  Método HTTP: GET
7.  Path: /api/proveedor/servicios
8.  Parámetros Query:
    - tipo: hotel
9.  Header Manager:
    - Accept: application/json
10. HTTP Cookie Manager: Habilitado (manejo de access_token)
11. Aserciones (Response Assertion):
    - Response Field: Response Code
    - Matching Rule: Equals -> 200
```

### Respuesta Obtenida del Servidor
```json
{
  "data": [
    {
      "id": 1,
      "tipo": "hotel",
      "nombre": "JW Marriott Lima",
      "ciudad": "Lima",
      "pais": "Perú",
      "activo": true,
      "reservas_totales": {
        "confirmadas": 2,
        "canceladas": 0
      }
    }
  ],
  "meta": {
    "total": 10,
    "per_page": 15,
    "current_page": 1
  }
}
```

---

## Caso CP02: Método POST — Crear Nuevo Hotel (Payload JSON)
### Comando cURL
```bash
curl -X POST "http://localhost:8000/api/hoteles" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${CSRF_TOKEN}" \
  -b cookies.txt \
  -d '{
    "nombre": "Hotel JMeter Luxury Cusco",
    "descripcion": "Hotel de prueba para samplers de rendimiento y calidad",
    "ciudad": "Cusco",
    "pais": "Perú",
    "direccion": "Plaza Regocijo 120",
    "estrellas": 5,
    "activo": true
  }'
```

### Configuración del Sampler en Apache JMeter
```text
1.  Elemento: HTTP Request Sampler
2.  Nombre: HTTP Request - POST Crear Hotel
3.  Protocolo: http
4.  Server Name / IP: localhost
5.  Port Number: 8000
6.  Método HTTP: POST
7.  Path: /api/hoteles
8.  Content encoding: UTF-8
9.  Body Data:
    {
      "nombre": "Hotel JMeter Luxury Cusco",
      "descripcion": "Hotel de prueba para samplers de rendimiento y calidad",
      "ciudad": "Cusco",
      "pais": "Perú",
      "direccion": "Plaza Regocijo 120",
      "estrellas": 5,
      "activo": true
    }
10. Header Manager:
    - Accept: application/json
    - Content-Type: application/json
    - X-XSRF-TOKEN: ${csrf_token}
11. Post-Processor (JSON Extractor):
    - Variable Name: created_hotel_id
    - JSON Path Expression: $.data.servicio_id
    - Match No.: 1
12. Aserciones (Response Assertion):
    - Response Field: Response Code
    - Matching Rule: Equals -> 201
```

### Respuesta Obtenida del Servidor
```json
{
  "message": "Servicio y hotel creados correctamente.",
  "data": {
    "servicio_id": 11,
    "direccion": "Plaza Regocijo 120",
    "estrellas": 5,
    "servicio": {
      "id": 11,
      "nombre": "Hotel JMeter Luxury Cusco",
      "ciudad": "Cusco",
      "pais": "Perú"
    }
  }
}
```

---

## Caso CP03: Método PUT — Actualizar Datos del Hotel
### Comando cURL
```bash
curl -X PUT "http://localhost:8000/api/hoteles/${created_hotel_id}" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${CSRF_TOKEN}" \
  -b cookies.txt \
  -d '{
    "nombre": "Hotel JMeter Luxury Cusco Resort & Spa",
    "direccion": "Plaza Regocijo 120 Int. 2",
    "estrellas": 5
  }'
```

### Configuración del Sampler en Apache JMeter
```text
1.  Elemento: HTTP Request Sampler
2.  Nombre: HTTP Request - PUT Modificar Hotel
3.  Protocolo: http
4.  Server Name / IP: localhost
5.  Port Number: 8000
6.  Método HTTP: PUT
7.  Path: /api/hoteles/${created_hotel_id}
8.  Content encoding: UTF-8
9.  Body Data:
    {
      "nombre": "Hotel JMeter Luxury Cusco Resort & Spa",
      "direccion": "Plaza Regocijo 120 Int. 2",
      "estrellas": 5
    }
10. Header Manager:
    - Accept: application/json
    - Content-Type: application/json
    - X-XSRF-TOKEN: ${csrf_token}
11. Aserciones (Response Assertion):
    - Response Field: Response Code
    - Matching Rule: Equals -> 200
    - Text Response Contains: "Hotel actualizado correctamente."
```

### Respuesta Obtenida del Servidor
```json
{
  "message": "Hotel actualizado correctamente.",
  "data": {
    "hotel": {
      "servicio_id": 11,
      "direccion": "Plaza Regocijo 120 Int. 2",
      "estrellas": 5
    },
    "servicio": {
      "id": 11,
      "nombre": "Hotel JMeter Luxury Cusco Resort & Spa"
    }
  }
}
```

---

## Caso CP04: Método DELETE — Eliminar Publicación de Servicio en Cascada
### Comando cURL
```bash
curl -X DELETE "http://localhost:8000/api/servicios/${created_hotel_id}" \
  -H "Accept: application/json" \
  -H "X-XSRF-TOKEN: ${CSRF_TOKEN}" \
  -b cookies.txt
```

### Configuración del Sampler en Apache JMeter
```text
1.  Elemento: HTTP Request Sampler
2.  Nombre: HTTP Request - DELETE Retirar Servicio Hotel
3.  Protocolo: http
4.  Server Name / IP: localhost
5.  Port Number: 8000
6.  Método HTTP: DELETE
7.  Path: /api/servicios/${created_hotel_id}
8.  Header Manager:
    - Accept: application/json
    - X-XSRF-TOKEN: ${csrf_token}
9.  Aserciones (Response Assertion):
    - Response Field: Response Code
    - Matching Rule: Equals -> 204
```

### Respuesta Obtenida del Servidor
```text
HTTP/1.1 204 No Content
Content-Type: application/json
```

---

## Caso CP05: Método POST — Generación de Itinerario con Microservicio Python FastAPI
### Comando cURL
```bash
curl -X POST "http://localhost:8000/api/itineraries" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: ${CSRF_TOKEN}" \
  -b cookies.txt \
  -d '{
    "destino": "Cusco",
    "hotel_id": 4,
    "fecha_checkin": "2026-12-01",
    "fecha_checkout": "2026-12-04",
    "interes": "Cultura"
  }'
```

### Configuración del Sampler en Apache JMeter
```text
1.  Elemento: HTTP Request Sampler
2.  Nombre: HTTP Request - POST Generar Itinerario Óptimo
3.  Protocolo: http
4.  Server Name / IP: localhost
5.  Port Number: 8000
6.  Método HTTP: POST
7.  Path: /api/itineraries
8.  Body Data:
    {
      "destino": "Cusco",
      "hotel_id": 4,
      "fecha_checkin": "2026-12-01",
      "fecha_checkout": "2026-12-04",
      "interes": "Cultura"
    }
9.  Header Manager:
    - Content-Type: application/json
    - X-XSRF-TOKEN: ${csrf_token}
10. Aserciones (Response Assertion):
    - Response Code: 200
    - Text Response Contains: "success":true
```

### Respuesta Obtenida del Servidor
```json
{
  "success": true,
  "data": {
    "dias_totales": 3,
    "destino": "Cusco",
    "hotel": "Palacio del Inka",
    "itinerario_diario": [
      {
        "dia": 1,
        "actividades": [
          "Catedral del Cusco y Qorikancha"
        ]
      }
    ]
  },
  "mensaje": "Itinerario generado exitosamente para 3 días en Cusco"
}
```
