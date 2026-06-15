package com.goslogic.orion.telemetry.application.port;

import com.goslogic.orion.telemetry.application.TelemetryIngestionService.BatchResult;

import java.util.List;

/**
 * Puerto de entrada para ingesta de posiciones GPS.
 * Implementaciones: RestTelemetryPositionConsumer (REST actual), AmqpTelemetryMessageListener (futuro broker).
 */
public interface TelemetryPositionConsumer {

    BatchResult consume(BatchIngest command);

    record BatchIngest(
            String tenantExternalId,
            String driverExternalId,
            List<PositionPayload> positions
    ) {}

    record PositionPayload(
            String time,
            String vehicleExternalId,
            Double latitude,
            Double longitude,
            Double speedKmh,
            Integer heading,
            String routeSheetExternalId,
            Boolean mocked
    ) {}
}
