package com.goslogic.orion.telemetry.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GeofenceRequest(
        @NotBlank String name,
        @NotNull Double centerLatitude,
        @NotNull Double centerLongitude,
        @NotNull @Positive Double radiusMeters
) {}
