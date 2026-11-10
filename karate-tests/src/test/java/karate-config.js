function fn() {
  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 15000);
  karate.configure('logPrettyRequest', true);
  karate.configure('logPrettyResponse', true);

  var env = karate.env; // e.g. 'local', 'docker'
  if (!env) {
    env = 'local';
  }

  var config = {
    env: env,
    baseUrl: 'http://localhost:8000/api',
    itineraryUrl: 'http://localhost:8001/api',
    defaultProveedorEmail: 'edwinproveedor@gmail.com',
    defaultProveedorPass: 'proveedor',
    defaultViajeroEmail: 'edwinviajero@gmail.com',
    defaultViajeroPass: 'viajero'
  };

  return config;
}
