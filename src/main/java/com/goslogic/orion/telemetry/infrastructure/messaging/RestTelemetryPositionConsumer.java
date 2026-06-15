package com.goslogic.orion.telemetry.infrastructure.messaging;

import com.goslogic.orion.telemetry.application.TelemetryIngestionService;
import com.goslogic.orion.telemetry.application.TelemetryIngestionService.BatchResult;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer;
import org.springframework.stereotype.Component;

/**
 * Implementación REST del puerto TelemetryPositionConsumer.
 * El futuro AmqpTelemetryMessageListener delegará en el mismo TelemetryIngestionService.
 */
@Component
public class RestTelemetryPositionConsumer implements TelemetryPositionConsumer {

    private final TelemetryIngestionService ingestionService;

    public RestTelemetryPositionConsumer(TelemetryIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public BatchResult consume(BatchIngest command) {
        return ingestionService.ingestBatch(command);
    }
}
