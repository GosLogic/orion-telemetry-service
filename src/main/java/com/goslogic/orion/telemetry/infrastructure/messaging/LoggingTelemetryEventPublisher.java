package com.goslogic.orion.telemetry.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implementación stub de TelemetryEventPublisher que registra los eventos en el log.
 * Sustituir por AmqpTelemetryEventPublisher cuando el broker esté disponible (decisión D11).
 */
@Component
public class LoggingTelemetryEventPublisher implements TelemetryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingTelemetryEventPublisher.class);

    @Override
    public void publish(String topic, Map<String, Object> payload) {
        log.info("[EVENT] topic={} payload={}", topic, payload);
    }
}
