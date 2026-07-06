package com.goslogic.orion.telemetry.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Habilita el modelo de mensajería JMS (broker ActiveMQ) y define el converter
 * que serializa/deserializa los payloads de telemetría como TextMessage JSON.
 * El type-id permite al listener reconstruir el record BatchIngest.
 */
@Configuration
@EnableJms
public class JmsConfig {

    public static final String TOPIC_TELEMETRY_POSITIONS = "orion.telemetry.positions";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
