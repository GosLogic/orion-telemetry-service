package com.goslogic.orion.telemetry.application;

import com.goslogic.orion.telemetry.application.exception.ResourceNotFoundException;
import com.goslogic.orion.telemetry.domain.model.Geofence;
import com.goslogic.orion.telemetry.domain.repository.GeofenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeofenceApplicationServiceTest {

    @Mock GeofenceRepository geofenceRepository;

    GeofenceApplicationService service;

    @BeforeEach
    void setUp() {
        service = new GeofenceApplicationService(geofenceRepository);
        when(geofenceRepository.save(any())).thenAnswer(inv -> {
            Geofence g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });
    }

    @Test
    void create_registra_geocerca() {
        var cmd = new GeofenceApplicationService.CreateGeofenceCommand(
                "tenant-demo", "Bodega Central", 4.6097, -74.0817, 500.0);

        Geofence result = service.create(cmd);

        assertThat(result.getName()).isEqualTo("Bodega Central");
        assertThat(result.getRadiusMeters()).isEqualTo(500.0);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void findById_lanza_404_si_no_existe() {
        when(geofenceRepository.findByIdAndTenantExternalId(99L, "tenant-demo"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L, "tenant-demo"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByTenant_devuelve_geocercas() {
        Geofence g = new Geofence("tenant-demo", "Bodega", 4.6097, -74.0817, 500.0);
        when(geofenceRepository.findByTenantExternalId("tenant-demo")).thenReturn(List.of(g));

        assertThat(service.listByTenant("tenant-demo")).hasSize(1);
    }
}
