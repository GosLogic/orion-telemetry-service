# language: es
# Estas pruebas BDD garantizan el rendimiento bajo alta carga de telemetría
# y la precisión geoespacial mediante TimescaleDB.
Característica: US11 - Alertas por Geocerca (Evaluación Espacial)
  Como plataforma Orion Telemetry
  Quiero evaluar espacialmente cada posición ingerida
  Para generar alertas de ingreso cuando un vehículo entra al radio de una geocerca activa

  Escenario: Detectar ingreso a geocerca
    Dado que un vehículo se acerca a una zona permitida
    Cuando sus coordenadas ingresan al radio de la geocerca activa
    Entonces el sistema debe generar una alerta de entrada y publicar el evento
