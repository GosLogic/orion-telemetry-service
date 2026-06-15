package com.goslogic.orion.telemetry.infrastructure.messaging;

import java.util.Map;

public interface TelemetryEventPublisher {

    void publish(String topic, Map<String, Object> payload);
}
