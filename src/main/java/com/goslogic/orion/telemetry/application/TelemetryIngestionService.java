package com.goslogic.orion.telemetry.application;

import com.goslogic.orion.telemetry.application.exception.ResourceNotFoundException;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer;
import com.goslogic.orion.telemetry.domain.model.Geofence;
import com.goslogic.orion.telemetry.domain.model.GeofenceAlert;
import com.goslogic.orion.telemetry.domain.model.GeofenceEventType;
import com.goslogic.orion.telemetry.domain.model.VehiclePosition;
import com.goslogic.orion.telemetry.domain.repository.GeofenceAlertRepository;
import com.goslogic.orion.telemetry.domain.repository.GeofenceRepository;
import com.goslogic.orion.telemetry.domain.repository.VehiclePositionRepository;
import com.goslogic.orion.telemetry.infrastructure.geo.HaversineCalculator;
import com.goslogic.orion.telemetry.infrastructure.messaging.TelemetryEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TelemetryIngestionService {

    public static final String TOPIC_FRAUD_ALERT = "orion.telemetry.fraud-alert";
    public static final String TOPIC_GEOFENCE_ALERT = "orion.telemetry.geofence-alert";

    private final VehiclePositionRepository positionRepository;
    private final GeofenceRepository geofenceRepository;
    private final GeofenceAlertRepository geofenceAlertRepository;
    private final TelemetryEventPublisher eventPublisher;

    public TelemetryIngestionService(VehiclePositionRepository positionRepository,
                                     GeofenceRepository geofenceRepository,
                                     GeofenceAlertRepository geofenceAlertRepository,
                                     TelemetryEventPublisher eventPublisher) {
        this.positionRepository = positionRepository;
        this.geofenceRepository = geofenceRepository;
        this.geofenceAlertRepository = geofenceAlertRepository;
        this.eventPublisher = eventPublisher;
    }

    public BatchResult ingestBatch(TelemetryPositionConsumer.BatchIngest command) {
        int accepted = 0;
        int rejected = 0;

        List<Geofence> activeGeofences = geofenceRepository
                .findByTenantExternalIdAndActiveTrue(command.tenantExternalId());

        for (TelemetryPositionConsumer.PositionPayload payload : command.positions()) {
            Optional<VehiclePosition> positionOpt = buildPosition(payload, command);
            if (positionOpt.isEmpty()) {
                rejected++;
                continue;
            }

            VehiclePosition position = positionOpt.get();
            positionRepository.save(position);
            accepted++;

            if (position.isMocked()) {
                publishFraudAlert(position);
            } else {
                checkGeofences(position, activeGeofences);
            }
        }

        return new BatchResult(accepted, rejected);
    }

    @Transactional(readOnly = true)
    public VehiclePosition findLatest(String vehicleExternalId, String tenantExternalId) {
        return positionRepository
                .findTopByVehicleExternalIdAndTenantExternalIdOrderByTimeDesc(vehicleExternalId, tenantExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay posiciones para el vehículo: " + vehicleExternalId));
    }

    private Optional<VehiclePosition> buildPosition(TelemetryPositionConsumer.PositionPayload payload,
                                                    TelemetryPositionConsumer.BatchIngest command) {
        if (payload.latitude() == null || payload.longitude() == null) {
            return Optional.empty();
        }
        if (payload.latitude() < -90 || payload.latitude() > 90
                || payload.longitude() < -180 || payload.longitude() > 180) {
            return Optional.empty();
        }

        Integer heading = payload.heading();
        if (heading != null && (heading < 0 || heading > 359)) {
            return Optional.empty();
        }

        BigDecimal speed = null;
        if (payload.speedKmh() != null) {
            double clamped = Math.max(0.0, Math.min(300.0, payload.speedKmh()));
            speed = BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP);
        }

        OffsetDateTime time = payload.time() != null && !payload.time().isBlank()
                ? OffsetDateTime.parse(payload.time())
                : OffsetDateTime.now();

        String vehicleId = payload.vehicleExternalId() != null
                ? payload.vehicleExternalId()
                : "vehicle-unknown";

        boolean mocked = Boolean.TRUE.equals(payload.mocked());

        return Optional.of(new VehiclePosition(
                time,
                vehicleId,
                payload.latitude(),
                payload.longitude(),
                speed,
                heading,
                payload.routeSheetExternalId(),
                mocked,
                command.tenantExternalId(),
                command.driverExternalId()
        ));
    }

    private void publishFraudAlert(VehiclePosition position) {
        eventPublisher.publish(TOPIC_FRAUD_ALERT, Map.of(
                "vehicle_external_id", position.getVehicleExternalId(),
                "tenant_id", position.getTenantExternalId(),
                "driver_external_id", position.getDriverExternalId(),
                "latitude", position.getLatitude(),
                "longitude", position.getLongitude(),
                "time", position.getTime().toString()
        ));
    }

    private void checkGeofences(VehiclePosition position, List<Geofence> geofences) {
        for (Geofence geofence : geofences) {
            double distance = HaversineCalculator.distanceMeters(
                    position.getLatitude(), position.getLongitude(),
                    geofence.getCenterLatitude(), geofence.getCenterLongitude());

            if (distance <= geofence.getRadiusMeters()) {
                GeofenceAlert alert = new GeofenceAlert(
                        position.getVehicleExternalId(),
                        geofence,
                        GeofenceEventType.ENTER,
                        position.getTime()
                );
                geofenceAlertRepository.save(alert);

                eventPublisher.publish(TOPIC_GEOFENCE_ALERT, Map.of(
                        "vehicle_external_id", position.getVehicleExternalId(),
                        "geofence_id", geofence.getId(),
                        "geofence_name", geofence.getName(),
                        "event_type", GeofenceEventType.ENTER.name(),
                        "tenant_id", position.getTenantExternalId(),
                        "time", position.getTime().toString()
                ));
            }
        }
    }

    public record BatchResult(int accepted, int rejected) {}
}
