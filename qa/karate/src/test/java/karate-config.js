function fn() {
  var env = karate.env || 'local';
  var config = {
    env: env,
    baseUrl: java.lang.System.getenv('API_URL') || 'http://localhost:8000/api',
    // Tiempo máximo aceptado por respuesta (QA-01). En CI con contenedores lentos se relaja.
    maxMs: 1000
  };
  if (env == 'ci') {
    config.maxMs = 2000;
  }
  if (karate.properties['baseUrl']) config.baseUrl = karate.properties['baseUrl'];

  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 20000);
  karate.configure('headers', { Accept: 'application/json' });

  // ---------- utilidades de datos dinámicos
  config.uuid = function () { return java.util.UUID.randomUUID() + '' };
  config.sufijo = function () { return java.lang.System.currentTimeMillis() + '' + Math.floor(Math.random() * 1000) };
  config.correo = function (prefijo) { return (prefijo || 'qa.karate') + '.' + java.lang.System.nanoTime() + '@trouvels360.test' };
  config.ruc = function () { var s = '20'; for (var i = 0; i < 9; i++) s += Math.floor(Math.random() * 10); return s };
  config.celular = function () { var s = '9'; for (var i = 0; i < 8; i++) s += Math.floor(Math.random() * 10); return s };
  // Fecha relativa a hoy (yyyy-MM-dd). Las reservas de prueba usan fechas lejanas y aleatorias para no chocar entre ejecuciones.
  config.fecha = function (dias) { return java.time.LocalDate.now().plusDays(dias).toString() };
  config.diaLibre = function () { return 400 + Math.floor(Math.random() * 3000) };
  config.pago = { titular: 'QA Karate', numero: '4242424242424242', vencimiento: '12/39', cvc: '123' };

  // ---------- sesiones: "actuar como" un usuario (cookie JWT + CSRF de doble envío)
  config.como = function (s) {
    karate.configure('cookies', { access_token: s.token, 'XSRF-TOKEN': s.xsrf });
    karate.configure('headers', { Accept: 'application/json', 'X-XSRF-TOKEN': s.xsrf });
  };
  config.anonimo = function () {
    karate.configure('cookies', null);
    karate.configure('headers', { Accept: 'application/json' });
  };

  // Sesiones compartidas, una sola vez por ejecución (evita el límite de logins por minuto)
  config.S = karate.callSingle('classpath:trouvels360/helpers/sesiones.feature', config);
  return config;
}
