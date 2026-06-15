package com.goslogic.orion.telemetry.domain.repository;

import com.goslogic.orion.telemetry.domain.model.VehiclePosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiclePositionRepository extends JpaRepository<VehiclePosition, Long> {

    Optional<VehiclePosition> findTopByVehicleExternalIdAndTenantExternalIdOrderByTimeDesc(
            String vehicleExternalId, String tenantExternalId);

    List<VehiclePosition> findByTenantExternalIdOrderByTimeDesc(String tenantExternalId);

    long countByTenantExternalId(String tenantExternalId);
}
