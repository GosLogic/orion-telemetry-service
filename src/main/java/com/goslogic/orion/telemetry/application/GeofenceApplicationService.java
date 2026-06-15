package com.goslogic.orion.telemetry.application;

import com.goslogic.orion.telemetry.application.exception.ResourceNotFoundException;
import com.goslogic.orion.telemetry.domain.model.Geofence;
import com.goslogic.orion.telemetry.domain.repository.GeofenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GeofenceApplicationService {

    private final GeofenceRepository geofenceRepository;

    public GeofenceApplicationService(GeofenceRepository geofenceRepository) {
        this.geofenceRepository = geofenceRepository;
    }

    public record CreateGeofenceCommand(
            String tenantExternalId,
            String name,
            Double centerLatitude,
            Double centerLongitude,
            Double radiusMeters
    ) {}

    public Geofence create(CreateGeofenceCommand cmd) {
        Geofence geofence = new Geofence(
                cmd.tenantExternalId(),
                cmd.name(),
                cmd.centerLatitude(),
                cmd.centerLongitude(),
                cmd.radiusMeters()
        );
        return geofenceRepository.save(geofence);
    }

    @Transactional(readOnly = true)
    public List<Geofence> listByTenant(String tenantExternalId) {
        return geofenceRepository.findByTenantExternalId(tenantExternalId);
    }

    @Transactional(readOnly = true)
    public Geofence findById(Long id, String tenantExternalId) {
        return geofenceRepository.findByIdAndTenantExternalId(id, tenantExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Geocerca no encontrada: " + id));
    }

    public Geofence updateActive(Long id, String tenantExternalId, boolean active) {
        Geofence geofence = findById(id, tenantExternalId);
        geofence.setActive(active);
        return geofenceRepository.save(geofence);
    }

    public void delete(Long id, String tenantExternalId) {
        Geofence geofence = findById(id, tenantExternalId);
        geofenceRepository.delete(geofence);
    }
}
