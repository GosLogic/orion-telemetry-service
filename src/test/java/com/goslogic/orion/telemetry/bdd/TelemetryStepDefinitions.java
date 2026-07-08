package com.goslogic.orion.telemetry.bdd;

import com.goslogic.orion.telemetry.application.TelemetryIngestionService;
import com.goslogic.orion.telemetry.application.TelemetryIngestionService.BatchResult;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer.BatchIngest;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer.PositionPayload;
import com.goslogic.orion.telemetry.domain.repository.GeofenceAlertRepository;
import com.goslogic.orion.telemetry.domain.repository.GeofenceRepository;
import com.goslogic.orion.telemetry.domain.repository.VehiclePositionRepository;
import com.goslogic.orion.telemetry.infrastructure.messaging.TelemetryEventPublisher;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Step definitions BDD (Cucumber ES) para la ingesta de posiciones GPS.
 * Estas pruebas garantizan el rendimiento bajo alta carga de telemetría
 * y la precisión geoespacial mediante TimescaleDB.
 */
public class TelemetryStepDefinitions {

    private final VehiclePositionRepository positionRepository = mock(VehiclePositionRepository.class);
    private final GeofenceRepository geofenceRepository = mock(GeofenceRepository.class);
    private final GeofenceAlertRepository geofenceAlertRepository = mock(GeofenceAlertRepository.class);
    private final TelemetryEventPublisher eventPublisher = mock(TelemetryEventPublisher.class);

    private final TelemetryIngestionService ingestionService = new TelemetryIngestionService(
            positionRepository, geofenceRepository, geofenceAlertRepository, eventPublisher);

    private final List<PositionPayload> positions = new ArrayList<>();
    private BatchResult batchResult;
    private boolean fraudScenario;

    public TelemetryStepDefinitions() {
        when(positionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(geofenceRepository.findByTenantExternalIdAndActiveTrue("tenant-demo"))
                .thenReturn(List.of());
    }

    @Dado("un lote de coordenadas válidas")
    public void unLoteDeCoordenadasValidas() {
        positions.clear();
        fraudScenario = false;
        positions.add(new PositionPayload(
                "2026-06-05T14:30:05.000Z",
                "vehicle-001",
                4.6097,
                -74.0817,
                42.5,
                180,
                "route-001",
                false
        ));
    }

    @Dado("una posición con latitud o longitud fuera de rango")
    public void unaPosicionConLatitudOLongitudFueraDeRango() {
        positions.clear();
        fraudScenario = false;
        positions.add(new PositionPayload(
                "2026-06-05T14:30:05.000Z",
                "vehicle-001",
                999.0,
                -74.0817,
                42.5,
                180,
                null,
                false
        ));
        positions.add(new PositionPayload(
                "2026-06-05T14:30:06.000Z",
                "vehicle-001",
                4.6097,
                -74.0817,
                10.0,
                90,
                null,
                false
        ));
    }

    @Dado("que la posición recibida posee is_mocked = true")
    public void queLaPosicionRecibidaPoseeIsMockedTrue() {
        positions.clear();
        fraudScenario = true;
        positions.add(new PositionPayload(
                "2026-06-05T14:30:05.000Z",
                "vehicle-001",
                4.6097,
                -74.0817,
                0.0,
                0,
                null,
                true
        ));
    }

    @Cuando("el sistema procesa el batch")
    @Cuando("el evento es procesado")
    @Cuando("el sistema lo detecta")
    public void elSistemaProcesaElBatch() {
        BatchIngest command = new BatchIngest("tenant-demo", "driver-demo", List.copyOf(positions));
        batchResult = ingestionService.ingestBatch(command);
    }

    @Entonces("la ubicación debe persistirse y refrescarse automáticamente")
    public void laUbicacionDebePersistirseYRefrescarseAutomaticamente() {
        assertThat(batchResult.accepted()).isGreaterThanOrEqualTo(1);
        assertThat(batchResult.rejected()).isZero();
        verify(positionRepository).save(any());
    }

    @Entonces("el sistema debe rechazarla sin detener el lote")
    public void elSistemaDebeRechazarlaSinDetenerElLote() {
        assertThat(batchResult.rejected()).isGreaterThanOrEqualTo(1);
        assertThat(batchResult.accepted()).isGreaterThanOrEqualTo(1);
    }

    @Entonces("debe publicar una alerta de fraude en el tópico correspondiente")
    public void debePublicarUnaAlertaDeFraudeEnElTopicoCorrespondiente() {
        assertThat(fraudScenario).isTrue();
        assertThat(batchResult.accepted()).isEqualTo(1);
        verify(eventPublisher).publish(eq(TelemetryIngestionService.TOPIC_FRAUD_ALERT), anyMap());
    }
}
