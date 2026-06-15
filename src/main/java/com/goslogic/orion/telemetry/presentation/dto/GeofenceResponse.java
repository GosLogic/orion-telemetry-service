package com.goslogic.orion.telemetry.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.telemetry.domain.model.Geofence;

public record GeofenceResponse(
        Long id,
        String name,
        @JsonProperty("center_latitude") Double centerLatitude,
        @JsonProperty("center_longitude") Double centerLongitude,
        @JsonProperty("radius_meters") Double radiusMeters,
        @JsonProperty("is_active") boolean active
) {

    public static GeofenceResponse from(Geofence geofence) {
        return new GeofenceResponse(
                geofence.getId(),
                geofence.getName(),
                geofence.getCenterLatitude(),
                geofence.getCenterLongitude(),
                geofence.getRadiusMeters(),
                geofence.isActive()
        );
    }
}
