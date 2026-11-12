@ignore
Feature: Helper - registrar un proveedor nuevo (sin historial) y devolver su sesión

  # Salida: sesion (token, xsrf, user)
  Scenario:
    * def r = call read('this:registrar.feature') { datos: { rol: 'proveedor', email: '#(correo("qa.prov"))', password: 'Clave123', empresa_nombre: 'QA Proveedor Nuevo SAC', ruc: '#(ruc())', telefono: '#(celular())' } }
    * match r.status == 201
    * def sesion = { token: '#(r.token)', xsrf: '#(r.xsrf)', user: '#(r.user)' }
