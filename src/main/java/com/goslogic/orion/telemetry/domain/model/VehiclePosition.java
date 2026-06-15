package com.goslogic.orion.telemetry.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicle_positions", indexes = {
        @Index(name = "idx_vp_vehicle_time", columnList = "vehicle_external_id, time"),
        @Index(name = "idx_vp_tenant", columnList = "tenant_external_id"),
        @Index(name = "idx_vp_mocked", columnList = "is_mocked")
})
@Getter
@Setter
@NoArgsConstructor
public class VehiclePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime time;

    @Column(name = "vehicle_external_id", nullable = false, length = 100)
    private String vehicleExternalId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "speed_kmh", precision = 5, scale = 2)
    private BigDecimal speedKmh;

    private Integer heading;

    @Column(name = "route_sheet_external_id", length = 100)
    private String routeSheetExternalId;

    @Column(name = "is_mocked", nullable = false)
    private boolean mocked;

    @Column(name = "tenant_external_id", nullable = false, length = 100)
    private String tenantExternalId;

    @Column(name = "driver_external_id", nullable = false, length = 100)
    private String driverExternalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public VehiclePosition(OffsetDateTime time,
                           String vehicleExternalId,
                           Double latitude,
                           Double longitude,
                           BigDecimal speedKmh,
                           Integer heading,
                           String routeSheetExternalId,
                           boolean mocked,
                           String tenantExternalId,
                           String driverExternalId) {
        this.time = time;
        this.vehicleExternalId = vehicleExternalId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.heading = heading;
        this.routeSheetExternalId = routeSheetExternalId;
        this.mocked = mocked;
        this.tenantExternalId = tenantExternalId;
        this.driverExternalId = driverExternalId;
    }
}
