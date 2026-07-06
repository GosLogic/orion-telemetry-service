package com.goslogic.orion.telemetry.infrastructure.messaging;

import com.goslogic.orion.telemetry.application.TelemetryIngestionService.BatchResult;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer;
import com.goslogic.orion.telemetry.infrastructure.config.JmsConfig;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementación asíncrona (broker JMS/ActiveMQ) del puerto TelemetryPositionConsumer.
 * Publica el lote al topic {@code orion.telemetry.positions} y devuelve una aceptación
 * optimista: la validación y persistencia reales ocurren en {@link JmsTelemetryMessageListener}.
 * Es el bean @Primary, por lo que el controller lo inyecta por defecto.
 */
@Component
@Primary
public class JmsTelemetryPositionConsumer implements TelemetryPositionConsumer {

    private final JmsTemplate jmsTemplate;

    public JmsTelemetryPositionConsumer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public BatchResult consume(BatchIngest command) {
        jmsTemplate.convertAndSend(JmsConfig.TOPIC_TELEMETRY_POSITIONS, command);
        // Aceptación optimista: {accepted, rejected} refleja lo recibido, no lo persistido.
        return new BatchResult(command.positions().size(), 0);
    }
}
