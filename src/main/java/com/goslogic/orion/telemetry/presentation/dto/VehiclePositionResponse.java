package com.goslogic.orion.telemetry.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.telemetry.domain.model.VehiclePosition;

public record VehiclePositionResponse(
        String time,
        @JsonProperty("vehicle_id") String vehicleExternalId,
        Double latitude,
        Double longitude,
        @JsonProperty("speed_kmh") String speedKmh,
        Integer heading,
        @JsonProperty("route_sheet_id") String routeSheetExternalId,
        @JsonProperty("is_mocked") boolean mocked,
        @JsonProperty("driver_id") String driverExternalId
) {

    public static VehiclePositionResponse from(VehiclePosition position) {
        return new VehiclePositionResponse(
                position.getTime().toString(),
                position.getVehicleExternalId(),
                position.getLatitude(),
                position.getLongitude(),
                position.getSpeedKmh() != null ? position.getSpeedKmh().toPlainString() : null,
                position.getHeading(),
                position.getRouteSheetExternalId(),
                position.isMocked(),
                position.getDriverExternalId()
        );
    }
}
