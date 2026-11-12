/**
 * Utilidades para manejo de imágenes
 * Centraliza la lógica de obtención de imágenes con fallbacks
 */

// Interfaz para el nuevo formato de objeto de imagen
export interface ImageObject {
  url: string;
  alt?: string | null;
}

export class ImageUtils {
  static readonly SIN_IMAGEN = '/public/img/sin-imagen.svg';
  static readonly SIN_IMAGEN_HOTEL = '/public/img/sin-imagen-hotel.svg';
  static readonly SIN_IMAGEN_TOUR = '/public/img/sin-imagen-tour.svg';

  /**
   * Si una imagen no carga (enlace roto o sitio que bloquea mostrarla en otra página), la reemplaza por la
   * imagen local "no disponible". Se registra una sola vez para toda la app (AppComponent).
   */
  static activarImagenDeReemplazo(doc: Document): void {
    doc.addEventListener('error', (evento) => {
      const img = evento.target;
      if (!(img instanceof HTMLImageElement) || img.dataset['sinImagen']) return;
      img.dataset['sinImagen'] = '1';
      const texto = `${img.alt} ${img.className}`.toLowerCase();
      img.src = texto.includes('hotel') ? ImageUtils.SIN_IMAGEN_HOTEL
              : texto.includes('tour') ? ImageUtils.SIN_IMAGEN_TOUR
              : ImageUtils.SIN_IMAGEN;
    }, true);
  }
  /**
   * Obtiene la URL de imagen principal con fallback
   * @param primary Imagen principal
   * @param gallery Array de imágenes de galería
   * @param tipo Tipo de servicio para placeholder específico
   */
  static getImageUrl(
    primary: string | undefined | null,
    gallery: ImageObject[] | undefined | null,
    tipo: 'hotel' | 'tour' | 'generic' = 'generic'
  ): string {
    // 1. Intentar usar imagen principal
    if (this.isValidImageUrl(primary)) {
      return primary;
    }

    // 2. Intentar usar primera imagen de galería
    if (gallery && Array.isArray(gallery) && gallery.length > 0) {
      // Filtrar el primer objeto con una URL válida
      const firstImageObject = gallery.find(
        img => img && this.isValidImageUrl(img.url)
      );
      if (firstImageObject && firstImageObject.url) {
        return firstImageObject.url;
      }
    }

    // 3. Retornar placeholder según tipo
    return this.getPlaceholder(tipo);
  }

  /**
   * Obtiene todas las imágenes disponibles (principal + galería)
   * @param primary Imagen principal
   * @param gallery Array de imágenes de galería
   */
  static getAllImages(
    primary: string | undefined | null,
    gallery: ImageObject[] | undefined | null
  ): string[] {
    const images: string[] = [];

    // 1. Agregar imagen principal si existe
    if (this.isValidImageUrl(primary)) {
      images.push(primary);
    }

    // 2. Agregar URLs de galería si existen
    if (gallery && Array.isArray(gallery)) {
      const galleryUrls = gallery
        .map(img => img.url) // Extraer solo la URL
        .filter(this.isValidImageUrl); // Filtrar URLs válidas

      images.push(...galleryUrls);
    }

    // 3. Remover duplicados manteniendo orden
    return [...new Set(images)];
  }

  /**
   * Completa una galería hasta un número específico de imágenes
   * @param images Array de imágenes existentes
   * @param targetCount Número objetivo de imágenes
   * @param tipo Tipo de servicio para placeholders
   */
  static fillGallery(
    images: string[],
    targetCount: number,
    tipo: 'hotel' | 'tour' | 'generic' = 'generic'
  ): string[] {
    const result = [...images];

    // Determinar la URL de relleno a usar: la primera imagen real si existe, o null.
    const fillerUrl = images.length > 0 ? images[0] : null;

    // Completar hasta el objetivo
    while (result.length < targetCount) {
      if(fillerUrl){
        result.push(fillerUrl);
      }else{
        // Agregar placeholder con índice
        const index = result.length + 1;
        result.push(`${this.getPlaceholder(tipo)}?index=${index}`);
      }
    }

    // Limitar al número objetivo
    return result.slice(0, targetCount);
  }

  /**
   * Obtiene el placeholder apropiado según el tipo
   * @param tipo Tipo de servicio
   */
  static getPlaceholder(tipo: 'hotel' | 'tour' | 'generic'): string {
    // Imágenes locales (src/public/img): siempre existen, aunque falle internet o el sitio de origen
    const placeholders = {
      hotel: ImageUtils.SIN_IMAGEN_HOTEL,
      tour: ImageUtils.SIN_IMAGEN_TOUR,
      generic: ImageUtils.SIN_IMAGEN
    };

    return placeholders[tipo] || placeholders.generic;
  }

  /**
   * Valida si una URL de imagen es válida
   * @param url URL a validar
   */
  static isValidImageUrl(url: string | undefined | null): url is string {
    if (!url || typeof url !== 'string') {
      return false;
    }

    const trimmed = url.trim();
    if (trimmed.length === 0) {
      return false;
    }

    // Validar que sea una URL válida o ruta relativa
    return trimmed.startsWith('http') || 
           trimmed.startsWith('/') || 
           trimmed.startsWith('assets/') ||
           trimmed.startsWith('./');
  }

  /**
   * Maneja el error de carga de imagen
   * @param event Evento de error
   * @param tipo Tipo de servicio para placeholder
   */
  static handleImageError(
    event: Event,
    tipo: 'hotel' | 'tour' | 'generic' = 'generic'
  ): void {
    const target = event.target as HTMLImageElement;
    if (target && target.src !== this.getPlaceholder(tipo)) {
      target.src = this.getPlaceholder(tipo);
    }
  }
}
