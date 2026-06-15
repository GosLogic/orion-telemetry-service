package com.goslogic.orion.telemetry.presentation.dto;

public record BatchResultResponse(
        int accepted,
        int rejected
) {}
