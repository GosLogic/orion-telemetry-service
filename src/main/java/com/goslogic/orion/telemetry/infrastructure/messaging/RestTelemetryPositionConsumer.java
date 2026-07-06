package com.goslogic.orion.telemetry.infrastructure.messaging;

import com.goslogic.orion.telemetry.application.TelemetryIngestionService;
import com.goslogic.orion.telemetry.application.TelemetryIngestionService.BatchResult;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer;
import org.springframework.stereotype.Component;

/**
 * Implementación REST (síncrona) del puerto TelemetryPositionConsumer: persiste directamente.
 * Queda como bean NO-primary; en runtime el controller inyecta {@link JmsTelemetryPositionConsumer}
 * (@Primary). Se mantiene como fallback e ingesta directa para los tests de {@link TelemetryIngestionService}.
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
