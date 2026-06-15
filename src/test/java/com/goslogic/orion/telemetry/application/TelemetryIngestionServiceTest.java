package com.goslogic.orion.telemetry.application;

import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer;
import com.goslogic.orion.telemetry.domain.model.Geofence;
import com.goslogic.orion.telemetry.domain.model.GeofenceEventType;
import com.goslogic.orion.telemetry.domain.model.VehiclePosition;
import com.goslogic.orion.telemetry.domain.repository.GeofenceAlertRepository;
import com.goslogic.orion.telemetry.domain.repository.GeofenceRepository;
import com.goslogic.orion.telemetry.domain.repository.VehiclePositionRepository;
import com.goslogic.orion.telemetry.infrastructure.messaging.TelemetryEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TelemetryIngestionServiceTest {

    @Mock VehiclePositionRepository positionRepository;
    @Mock GeofenceRepository geofenceRepository;
    @Mock GeofenceAlertRepository geofenceAlertRepository;
    @Mock TelemetryEventPublisher eventPublisher;

    TelemetryIngestionService service;

    @BeforeEach
    void setUp() {
        service = new TelemetryIngestionService(
                positionRepository, geofenceRepository, geofenceAlertRepository, eventPublisher);
        when(positionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(geofenceRepository.findByTenantExternalIdAndActiveTrue("tenant-demo"))
                .thenReturn(List.of());
    }

    @Test
    void ingestBatch_acepta_posicion_valida() {
        var command = new TelemetryPositionConsumer.BatchIngest(
                "tenant-demo",
                "driver-demo",
                List.of(new TelemetryPositionConsumer.PositionPayload(
                        "2026-06-05T14:30:05.000Z",
                        "vehicle-001",
                        4.6097,
                        -74.0817,
                        42.5,
                        180,
                        "route-001",
                        false
                ))
        );

        TelemetryIngestionService.BatchResult result = service.ingestBatch(command);

        assertThat(result.accepted()).isEqualTo(1);
        assertThat(result.rejected()).isZero();
        verify(positionRepository).save(any(VehiclePosition.class));
    }

    @Test
    void ingestBatch_rechaza_coordenadas_invalidas() {
        var command = new TelemetryPositionConsumer.BatchIngest(
                "tenant-demo",
                "driver-demo",
                List.of(new TelemetryPositionConsumer.PositionPayload(
                        "2026-06-05T14:30:05.000Z",
                        "vehicle-001",
                        999.0,
                        -74.0817,
                        42.5,
                        180,
                        null,
                        false
                ))
        );

        TelemetryIngestionService.BatchResult result = service.ingestBatch(command);

        assertThat(result.accepted()).isZero();
        assertThat(result.rejected()).isEqualTo(1);
        verify(positionRepository, never()).save(any());
    }

    @Test
    void ingestBatch_mocked_publica_fraud_alert() {
        var command = new TelemetryPositionConsumer.BatchIngest(
                "tenant-demo",
                "driver-demo",
                List.of(new TelemetryPositionConsumer.PositionPayload(
                        "2026-06-05T14:30:05.000Z",
                        "vehicle-001",
                        4.6097,
                        -74.0817,
                        0.0,
                        0,
                        null,
                        true
                ))
        );

        service.ingestBatch(command);

        verify(eventPublisher).publish(eq(TelemetryIngestionService.TOPIC_FRAUD_ALERT), anyMap());
    }

    @Test
    void ingestBatch_dentro_geocerca_crea_alerta_y_publica_evento() {
        Geofence geofence = new Geofence("tenant-demo", "Bodega", 4.6097, -74.0817, 500.0);
        geofence.setId(1L);
        when(geofenceRepository.findByTenantExternalIdAndActiveTrue("tenant-demo"))
                .thenReturn(List.of(geofence));

        var command = new TelemetryPositionConsumer.BatchIngest(
                "tenant-demo",
                "driver-demo",
                List.of(new TelemetryPositionConsumer.PositionPayload(
                        "2026-06-05T14:30:05.000Z",
                        "vehicle-001",
                        4.6097,
                        -74.0817,
                        10.0,
                        90,
                        null,
                        false
                ))
        );

        service.ingestBatch(command);

        verify(geofenceAlertRepository).save(argThat(alert ->
                alert.getEventType() == GeofenceEventType.ENTER
                        && alert.getVehicleExternalId().equals("vehicle-001")));
        verify(eventPublisher).publish(eq(TelemetryIngestionService.TOPIC_GEOFENCE_ALERT), anyMap());
    }
}
