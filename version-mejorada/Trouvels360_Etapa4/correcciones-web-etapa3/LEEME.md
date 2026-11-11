# Corrección de trazabilidad en la web (etapa 3)

Al cruzar la app móvil con las HU detecté que en la web algunos comentarios y documentos citaban
códigos de historia equivocados. **No cambia ningún comportamiento**, solo la trazabilidad para Jira:

| Tema | Decía | Correcto |
|---|---|---|
| Reseñas | TRV-401..404 | TRV-403 |
| Itinerario sugerido | TRV-205 | TRV-404 |
| Mis reservas / cancelar | TRV-206, TRV-304 | TRV-401, TRV-402 |
| Eliminar cuenta | (dentro de TRV-104) | TRV-105 |

Copia estos 6 archivos encima de `trouvels360-frontend-angular/` (mismas rutas).
