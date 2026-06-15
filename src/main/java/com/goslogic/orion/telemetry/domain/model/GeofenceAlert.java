package com.goslogic.orion.telemetry.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "geofence_alerts")
@Getter
@Setter
@NoArgsConstructor
public class GeofenceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_external_id", nullable = false, length = 100)
    private String vehicleExternalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "geofence_id", nullable = false)
    private Geofence geofence;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 10)
    private GeofenceEventType eventType;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    public GeofenceAlert(String vehicleExternalId,
                         Geofence geofence,
                         GeofenceEventType eventType,
                         OffsetDateTime timestamp) {
        this.vehicleExternalId = vehicleExternalId;
        this.geofence = geofence;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }
}
