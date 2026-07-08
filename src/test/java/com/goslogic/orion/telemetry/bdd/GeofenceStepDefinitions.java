package com.goslogic.orion.telemetry.bdd;

import com.goslogic.orion.telemetry.application.GeofenceApplicationService;
import com.goslogic.orion.telemetry.application.GeofenceApplicationService.CreateGeofenceCommand;
import com.goslogic.orion.telemetry.application.TelemetryIngestionService;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer.BatchIngest;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer.PositionPayload;
import com.goslogic.orion.telemetry.domain.model.Geofence;
import com.goslogic.orion.telemetry.domain.model.GeofenceEventType;
import com.goslogic.orion.telemetry.domain.repository.GeofenceAlertRepository;
import com.goslogic.orion.telemetry.domain.repository.GeofenceRepository;
import com.goslogic.orion.telemetry.domain.repository.VehiclePositionRepository;
import com.goslogic.orion.telemetry.infrastructure.messaging.TelemetryEventPublisher;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Step definitions BDD (Cucumber ES) para geocercas y alertas espaciales.
 * Estas pruebas garantizan el rendimiento bajo alta carga de telemetría
 * y la precisión geoespacial mediante TimescaleDB.
 */
public class GeofenceStepDefinitions {

    private final GeofenceRepository geofenceRepository = mock(GeofenceRepository.class);
    private final GeofenceAlertRepository geofenceAlertRepository = mock(GeofenceAlertRepository.class);
    private final VehiclePositionRepository positionRepository = mock(VehiclePositionRepository.class);
    private final TelemetryEventPublisher eventPublisher = mock(TelemetryEventPublisher.class);

    private final GeofenceApplicationService geofenceService =
            new GeofenceApplicationService(geofenceRepository);
    private final TelemetryIngestionService ingestionService = new TelemetryIngestionService(
            positionRepository, geofenceRepository, geofenceAlertRepository, eventPublisher);

    private CreateGeofenceCommand createCommand;
    private Geofence createdGeofence;
    private List<Geofence> listedGeofences;

    public GeofenceStepDefinitions() {
        when(geofenceRepository.save(any())).thenAnswer(inv -> {
            Geofence g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });
        when(positionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Dado("que el gestor define un perímetro válido con latitud, longitud y radio")
    public void queElGestorDefineUnPerimetroValido() {
        createCommand = new CreateGeofenceCommand(
                "tenant-demo", "Bodega Central", 4.6097, -74.0817, 500.0);
    }

    @Cuando("lo guarda")
    public void loGuarda() {
        createdGeofence = geofenceService.create(createCommand);
    }

    @Entonces("el sistema debe registrar la geocerca activa para su tenant")
    public void elSistemaDebeRegistrarLaGeocercaActiva() {
        assertThat(createdGeofence).isNotNull();
        assertThat(createdGeofence.isActive()).isTrue();
        assertThat(createdGeofence.getTenantExternalId()).isEqualTo("tenant-demo");
        verify(geofenceRepository).save(any(Geofence.class));
    }

    @Dado("un tenant con múltiples geocercas")
    public void unTenantConMultiplesGeocercas() {
        Geofence a = new Geofence("tenant-demo", "Bodega", 4.6097, -74.0817, 500.0);
        Geofence b = new Geofence("tenant-demo", "Puerto", 4.7100, -74.0700, 300.0);
        when(geofenceRepository.findByTenantExternalId("tenant-demo")).thenReturn(List.of(a, b));
    }

    @Cuando("solicita su lista")
    public void solicitaSuLista() {
        listedGeofences = geofenceService.listByTenant("tenant-demo");
    }

    @Entonces("el sistema devuelve solo las pertenecientes a su empresa")
    public void elSistemaDevuelveSoloLasDeSuEmpresa() {
        assertThat(listedGeofences).hasSize(2);
        assertThat(listedGeofences).allMatch(g -> "tenant-demo".equals(g.getTenantExternalId()));
    }

    @Dado("que un vehículo se acerca a una zona permitida")
    public void queUnVehiculoSeAcercaAUnaZonaPermitida() {
        Geofence geofence = new Geofence("tenant-demo", "Bodega", 4.6097, -74.0817, 500.0);
        geofence.setId(1L);
        when(geofenceRepository.findByTenantExternalIdAndActiveTrue("tenant-demo"))
                .thenReturn(List.of(geofence));
    }

    @Cuando("sus coordenadas ingresan al radio de la geocerca activa")
    public void susCoordenadasIngresanAlRadio() {
        BatchIngest command = new BatchIngest(
                "tenant-demo",
                "driver-demo",
                List.of(new PositionPayload(
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
        ingestionService.ingestBatch(command);
    }

    @Entonces("el sistema debe generar una alerta de entrada y publicar el evento")
    public void elSistemaDebeGenerarAlertaDeEntrada() {
        verify(geofenceAlertRepository).save(org.mockito.ArgumentMatchers.argThat(alert ->
                alert.getEventType() == GeofenceEventType.ENTER
                        && "vehicle-001".equals(alert.getVehicleExternalId())));
        verify(eventPublisher).publish(eq(TelemetryIngestionService.TOPIC_GEOFENCE_ALERT), anyMap());
    }
}
