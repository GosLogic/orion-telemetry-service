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
 * Datos demo alineados con el ecosistema Orion (tenant-demo, vehicle-001, Bodega Central).
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
                4.6097,
                -74.0817,
                500.0
        ));

        OffsetDateTime baseTime = OffsetDateTime.parse("2026-06-05T14:30:00.000Z");

        // Dentro de la geocerca (centro exacto)
        positionRepository.save(new VehiclePosition(
                baseTime,
                "vehicle-001",
                4.6097,
                -74.0817,
                BigDecimal.valueOf(0.0),
                0,
                "route-001",
                false,
                "tenant-demo",
                "driver-demo"
        ));

        // Fuera de la geocerca (~2 km al norte)
        positionRepository.save(new VehiclePosition(
                baseTime.plusMinutes(5),
                "vehicle-001",
                4.6277,
                -74.0817,
                BigDecimal.valueOf(42.5),
                180,
                "route-001",
                false,
                "tenant-demo",
                "driver-demo"
        ));

        log.info("[DataSeeder] Geocerca demo '{}' (id={}) y 2 posiciones para vehicle-001",
                bodega.getName(), bodega.getId());
    }
}
