package com.goslogic.orion.telemetry.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "geofences")
@Getter
@Setter
@NoArgsConstructor
public class Geofence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_external_id", nullable = false, length = 100)
    private String tenantExternalId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "center_latitude", nullable = false)
    private Double centerLatitude;

    @Column(name = "center_longitude", nullable = false)
    private Double centerLongitude;

    @Column(name = "radius_meters", nullable = false)
    private Double radiusMeters;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Geofence(String tenantExternalId,
                    String name,
                    Double centerLatitude,
                    Double centerLongitude,
                    Double radiusMeters) {
        this.tenantExternalId = tenantExternalId;
        this.name = name;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.radiusMeters = radiusMeters;
    }
}
