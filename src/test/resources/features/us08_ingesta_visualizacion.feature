# language: es
# Estas pruebas BDD garantizan el rendimiento bajo alta carga de telemetría
# y la precisión geoespacial mediante TimescaleDB.
Característica: US08 - Ingesta y Visualización (Procesamiento Batch)
  Como plataforma Orion Telemetry
  Quiero ingerir lotes de posiciones GPS
  Para persistir ubicaciones válidas, rechazar inválidas y detectar fraude sin interrumpir el flujo

  Escenario: Aceptar posición válida
    Dado un lote de coordenadas válidas
    Cuando el sistema procesa el batch
    Entonces la ubicación debe persistirse y refrescarse automáticamente

  Escenario: Rechazar coordenadas inválidas
    Dado una posición con latitud o longitud fuera de rango
    Cuando el evento es procesado
    Entonces el sistema debe rechazarla sin detener el lote

  Escenario: Identificar fraude GPS
    Dado que la posición recibida posee is_mocked = true
    Cuando el sistema lo detecta
    Entonces debe publicar una alerta de fraude en el tópico correspondiente
