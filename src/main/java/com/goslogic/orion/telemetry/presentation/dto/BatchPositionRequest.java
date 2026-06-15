package com.goslogic.orion.telemetry.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchPositionRequest(
        @NotEmpty @Valid List<PositionRequest> positions
) {}
