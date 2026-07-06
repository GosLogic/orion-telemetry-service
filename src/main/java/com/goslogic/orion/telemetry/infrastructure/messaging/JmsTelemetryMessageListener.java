package com.goslogic.orion.telemetry.infrastructure.messaging;

import com.goslogic.orion.telemetry.application.TelemetryIngestionService;
import com.goslogic.orion.telemetry.application.TelemetryIngestionService.BatchResult;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer.BatchIngest;
import com.goslogic.orion.telemetry.infrastructure.config.JmsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor JMS del topic {@code orion.telemetry.positions}. Deserializa el lote y delega
 * en {@link TelemetryIngestionService#ingestBatch} para validar, persistir y disparar alertas.
 * Aquí ocurre la persistencia real (asíncrona respecto a la respuesta HTTP del gateway).
 */
@Component
public class JmsTelemetryMessageListener {

    private static final Logger log = LoggerFactory.getLogger(JmsTelemetryMessageListener.class);

    private final TelemetryIngestionService ingestionService;

    public JmsTelemetryMessageListener(TelemetryIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @JmsListener(destination = JmsConfig.TOPIC_TELEMETRY_POSITIONS)
    public void onBatch(BatchIngest command) {
        BatchResult result = ingestionService.ingestBatch(command);
        log.info("[JMS] Lote de telemetría procesado tenant={} accepted={} rejected={}",
                command.tenantExternalId(), result.accepted(), result.rejected());
    }
}
