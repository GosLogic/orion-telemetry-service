package com.goslogic.orion.telemetry.domain.repository;

import com.goslogic.orion.telemetry.domain.model.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {

    List<Geofence> findByTenantExternalIdAndActiveTrue(String tenantExternalId);

    List<Geofence> findByTenantExternalId(String tenantExternalId);

    Optional<Geofence> findByIdAndTenantExternalId(Long id, String tenantExternalId);
}
