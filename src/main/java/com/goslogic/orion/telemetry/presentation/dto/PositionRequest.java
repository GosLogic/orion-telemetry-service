package com.goslogic.orion.telemetry.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record PositionRequest(
        String time,
        @JsonProperty("vehicle_id") Integer vehicleId,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @JsonProperty("speed_kmh") Double speedKmh,
        Integer heading,
        @JsonProperty("route_sheet_id") Integer routeSheetId,
        @JsonProperty("is_mocked") Boolean mocked
) {}
