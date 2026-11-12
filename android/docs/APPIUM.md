# Guía para Appium (QA-04)

## Capabilities (UiAutomator2)

```json
{
  "platformName": "Android",
  "appium:automationName": "UiAutomator2",
  "appium:deviceName": "emulator-5554",
  "appium:app": "<ruta>/app/build/outputs/apk/debug/app-debug.apk",
  "appium:appPackage": "com.trouvels360.app",
  "appium:appActivity": "com.trouvels360.app.MainActivity",
  "appium:noReset": false,
  "appium:newCommandTimeout": 120
}
```

APK: `./gradlew assembleDebug`. El emulador ve el backend en `http://10.0.2.2:8000/api/`.

## Localizadores

Cada `testTag` es un resource-id: `driver.findElement(AppiumBy.id("login_email"))`.
Los diálogos (pago, rechazo, confirmaciones, selector de fechas) también exponen sus ids.
Para listas: `//*[starts-with(@resource-id,'hotel_card_')]` (primer hotel), `rr_card_<código>`, `reserva_card_<código>`.

## Flujo extremo a extremo de la rúbrica

| Paso | Acción | Selectores |
|---|---|---|
| 1 | Viajero inicia sesión | `nav_cuenta` → `btn_ir_login` → `login_email`, `login_password`, `login_submit` |
| 2 | Busca hotel con fechas | `nav_hoteles` → `buscar_ciudad` → `buscar_fechas` (diálogo `buscar_fechas_dialogo`, `_aceptar`) → `btn_buscar_hoteles` |
| 3 | Elige hotel y habitación | `hotel_card_{id}` → `habitacion_mas_{id}` → `btn_reservar_hotel` → `btn_sin_itinerario` |
| 4 | Paga | `resumen_total` → `btn_confirmar_pagar` → `pago_titular`, `pago_numero`, `pago_vencimiento`, `pago_cvc`, `pago_confirmar` |
| 5 | Verifica PENDIENTE | `comprobante_estado` (texto `PENDIENTE`), `comprobante_codigo` |
| 6 | Proveedor acepta | (logout `btn_logout`) login proveedor → `nav_recibidas` → `rr_estado_pendiente` → `btn_aceptar_{codigo}` → `rr_mensaje_ok` |
| 7 | Viajero ve CONFIRMADA | login viajero → `nav_reservas` → `reserva_estado_{codigo}` |

Variantes negativas útiles: tarjeta `4242424242424241` → `pago_numero_error`; motivo corto en
`rechazo_motivo` → `rechazo_motivo_error`; eliminar publicación con reservas → `panel_mensaje_error`.

## Gestos

- Scroll en listas: `new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().resourceId("btn_reservar_hotel"))`.
- Fechas: en el diálogo, el ícono de lápiz del DatePicker cambia a modo texto para escribir la fecha.
