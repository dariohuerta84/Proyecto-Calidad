@ignore
Feature: Helper - sesiones compartidas de la ejecución (karate.callSingle)

  Scenario:
    * def viajero = call read('this:sesion.feature') { email: 'edwinviajero@gmail.com', password: 'viajero' }
    * def viajero1 = call read('this:sesion.feature') { email: 'qa.viajero1@trouvels360.test', password: 'Viajero123' }
    * def viajero2 = call read('this:sesion.feature') { email: 'qa.viajero2@trouvels360.test', password: 'Viajero123' }
    * def proveedor = call read('this:sesion.feature') { email: 'edwinproveedor@gmail.com', password: 'proveedor' }
    * def proveedor2 = call read('this:sesion.feature') { email: 'qa.proveedor2@trouvels360.test', password: 'Proveedor123' }
    * assert viajero.loginStatus == 200 && viajero1.loginStatus == 200 && viajero2.loginStatus == 200
    * assert proveedor.loginStatus == 200 && proveedor2.loginStatus == 200
