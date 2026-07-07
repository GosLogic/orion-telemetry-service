package com.goslogic.orion.telemetry.infrastructure.config;

import com.goslogic.orion.telemetry.domain.model.Geofence;
import com.goslogic.orion.telemetry.domain.model.VehiclePosition;
import com.goslogic.orion.telemetry.domain.repository.GeofenceRepository;
import com.goslogic.orion.telemetry.domain.repository.VehiclePositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Datos demo alineados con dispatch (Lima) y tenant-demo / vehicle-001.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final GeofenceRepository geofenceRepository;
    private final VehiclePositionRepository positionRepository;

    public DataSeeder(GeofenceRepository geofenceRepository,
                      VehiclePositionRepository positionRepository) {
        this.geofenceRepository = geofenceRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (positionRepository.countByTenantExternalId("tenant-demo") > 0) {
            log.info("[DataSeeder] Datos demo ya existentes — omitiendo seed");
            return;
        }

        Geofence bodega = geofenceRepository.save(new Geofence(
                "tenant-demo",
                "Bodega Central",
                -12.0464,
                -77.0428,
                500.0
        ));

        OffsetDateTime baseTime = OffsetDateTime.now().minusMinutes(10);

        positionRepository.save(new VehiclePosition(
                baseTime,
                "vehicle-001",
                -12.0464,
                -77.0428,
                BigDecimal.valueOf(0.0),
                0,
                "route-demo-001",
                false,
                "tenant-demo",
                "driver-demo"
        ));

        positionRepository.save(new VehiclePosition(
                baseTime.plusMinutes(5),
                "vehicle-001",
                -12.0534,
                -77.0500,
                BigDecimal.valueOf(42.5),
                180,
                "route-demo-001",
                false,
                "tenant-demo",
                "driver-demo"
        ));

        log.info("[DataSeeder] Geocerca '{}' (id={}) y 2 posiciones Lima para vehicle-001",
                bodega.getName(), bodega.getId());
    }
}
