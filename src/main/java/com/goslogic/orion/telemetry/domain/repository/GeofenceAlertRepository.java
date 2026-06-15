package com.goslogic.orion.telemetry.domain.repository;

import com.goslogic.orion.telemetry.domain.model.GeofenceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeofenceAlertRepository extends JpaRepository<GeofenceAlert, Long> {
}
