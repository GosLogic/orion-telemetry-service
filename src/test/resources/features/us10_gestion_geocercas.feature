# language: es
# Estas pruebas BDD garantizan el rendimiento bajo alta carga de telemetría
# y la precisión geoespacial mediante TimescaleDB.
Característica: US10 - Gestión de Geocercas
  Como gestor de operaciones Orion
  Quiero crear y consultar geocercas por tenant
  Para delimitar zonas operativas con aislamiento multi-tenant

  Escenario: Crear geocerca
    Dado que el gestor define un perímetro válido con latitud, longitud y radio
    Cuando lo guarda
    Entonces el sistema debe registrar la geocerca activa para su tenant

  Escenario: Consultar geocercas por tenant
    Dado un tenant con múltiples geocercas
    Cuando solicita su lista
    Entonces el sistema devuelve solo las pertenecientes a su empresa
