🌍 Trouvels360 – Backend PHP

Backend principal de Trouvels360, una plataforma integral de planificación de viajes que permite a los viajeros descubrir, reservar y calificar servicios turísticos (hoteles y tours), y a los proveedores gestionar sus publicaciones y visualizar reportes básicos.

Este backend constituye el núcleo transaccional del sistema y expone una API REST consumida por el frontend desarrollado en Angular y por el microservicio de optimización de itinerarios desarrollado en Python.

📌 Visión del Producto

Trouvels360 busca convertirse en una ventanilla única para el viajero moderno, combinando en una sola plataforma:

Un marketplace de servicios turísticos

Un sistema de reservas (simuladas en el MVP)

Un sistema de calificaciones y reseñas

Un motor de recomendación de itinerarios mediante microservicios

La plataforma conecta a viajeros con proveedores locales, fomentando un ecosistema turístico dinámico e interactivo.

🏗️ Arquitectura General
[ Angular Frontend ]
        |
        v
[ Trouvels360 - Backend PHP (API REST) ]
        |
        +--> Base de Datos
        |     - Usuarios
        |     - Servicios (Hoteles / Tours)
        |     - Reservas
        |     - Reseñas
        |
        +--> [ Microservicio Python ]
                /api/itinerary/optimize

👥 Roles del Sistema

El backend de Trouvels360 gestiona autenticación, autorización y control de acceso basado en roles:

✈️ Viajero

Buscar hoteles y tours

Ver detalles y reseñas

Simular reservas

Solicitar sugerencias de itinerario

Calificar y dejar reseñas

🏨 Proveedor

Registrar y administrar su perfil

Publicar y gestionar servicios turísticos

Visualizar reportes básicos de rendimiento

🚀 Funcionalidades del MVP
🔐 Autenticación y Gestión de Usuarios

Registro e inicio de sesión

Gestión de perfil

Asignación de roles (viajero, proveedor)

Protección de endpoints según rol

🏨 Gestión de Servicios Turísticos

Los proveedores pueden crear, editar y eliminar:

Hoteles

Tours

Información gestionada:

Nombre

Descripción

Ciudad y dirección

Categoría (para tours)

Precio base

Imágenes (1 a 5, clickeables desde el frontend)

🔍 Búsqueda y Descubrimiento

Búsqueda de servicios por ciudad

Filtros por tipo (hotel / tour) y categoría

Visualización de:

Precio

Estrellas promedio

Información principal del servicio

📄 Página de Detalle del Servicio

Información completa del servicio

Galería de imágenes

Listado de reseñas

Calificación promedio (1 a 5 estrellas)

📝 Reservas Simuladas (MVP)

Registro de reservas sin pasarela de pago

Generación de código de reserva

Visualización de reservas por proveedor

⭐ Sistema de Calificaciones y Reseñas

Los viajeros pueden:

Asignar una calificación en estrellas

Escribir una reseña

El sistema:

Recalcula automáticamente la calificación promedio

Muestra el resultado en búsquedas y detalles

📊 Reportes Básicos para Proveedores

Número de reservas simuladas por servicio

Calificación promedio

Métricas simples de rendimiento

🔗 Integración con el Microservicio de Itinerarios

Trouvels360 utiliza un microservicio en Python para la planificación de viajes.

Endpoint consumido
POST /api/itinerary/optimize

Ejemplo de Request
{
  "destination": "Cusco",
  "days": 2
}

Ejemplo de Response (MVP)
{
  "itinerary": [
    "Día 1: Plaza de Armas, Catedral",
    "Día 2: Valle Sagrado"
  ]
}


En el MVP, el itinerario es predefinido o fijo, validando la correcta comunicación entre servicios.

📦 Endpoints Principales (Referencia)
Método	Endpoint	Descripción
POST	/api/auth/register	Registro de usuario
POST	/api/auth/login	Inicio de sesión
GET	/api/services	Listado de hoteles y tours
GET	/api/services/{id}	Detalle de servicio
POST	/api/services	Crear servicio (Proveedor)
PUT	/api/services/{id}	Editar servicio
DELETE	/api/services/{id}	Eliminar servicio
POST	/api/reservations	Crear reserva simulada
POST	/api/reviews	Crear reseña
GET	/api/provider/reports	Reportes del proveedor
🛠️ Tecnologías Utilizadas

PHP – Backend principal

API REST

Base de Datos Relacional (MySQL / PostgreSQL)

JWT o Sesiones para autenticación

Arquitectura MVC o similar

ℹ️ El frontend en Angular y el microservicio en Python se mantienen en repositorios independientes.

📈 Roadmap (Post-MVP)

Gestión real de disponibilidad

Algoritmo de optimización de itinerarios basado en coordenadas

Integración con API de mapas (Google Maps / Mapbox)

Visualización de reservas por viajero

Reportes avanzados para proveedores

✅ Criterios de Éxito

Un viajero puede registrarse, reservar y dejar reseñas

Un proveedor puede publicar servicios y visualizar métricas

La API es clara, segura y desacoplada

Integración funcional con el microservicio de itinerarios

👨‍💻 Proyecto

Trouvels360
Plataforma de planificación de viajes basada en una arquitectura moderna, orientada a microservicios y enfocada en la experiencia del usuario.
