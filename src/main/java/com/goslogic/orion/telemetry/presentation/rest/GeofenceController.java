package com.goslogic.orion.telemetry.presentation.rest;

import com.goslogic.orion.telemetry.application.GeofenceApplicationService;
import com.goslogic.orion.telemetry.application.GeofenceApplicationService.CreateGeofenceCommand;
import com.goslogic.orion.telemetry.domain.model.Geofence;
import com.goslogic.orion.telemetry.presentation.dto.GeofenceRequest;
import com.goslogic.orion.telemetry.presentation.dto.GeofenceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/telemetry/geofences")
@Tag(name = "Geofences", description = "Gestión de geocercas circulares")
public class GeofenceController {

    private final GeofenceApplicationService geofenceService;

    public GeofenceController(GeofenceApplicationService geofenceService) {
        this.geofenceService = geofenceService;
    }

    @GetMapping
    @Operation(summary = "Listar geocercas del tenant")
    public ResponseEntity<List<GeofenceResponse>> listByTenant(
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        List<GeofenceResponse> responses = geofenceService.listByTenant(tenantExternalId)
                .stream()
                .map(GeofenceResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener geocerca por ID")
    public ResponseEntity<GeofenceResponse> findById(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        return ResponseEntity.ok(GeofenceResponse.from(
                geofenceService.findById(id, tenantExternalId)));
    }

    @PostMapping
    @Operation(summary = "Crear geocerca")
    public ResponseEntity<GeofenceResponse> create(
            @Valid @RequestBody GeofenceRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        CreateGeofenceCommand cmd = new CreateGeofenceCommand(
                tenantExternalId,
                req.name(),
                req.centerLatitude(),
                req.centerLongitude(),
                req.radiusMeters()
        );
        Geofence geofence = geofenceService.create(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(GeofenceResponse.from(geofence));
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Activar o desactivar geocerca")
    public ResponseEntity<GeofenceResponse> updateActive(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ResponseEntity.ok(GeofenceResponse.from(
                geofenceService.updateActive(id, tenantExternalId, active)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar geocerca")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        geofenceService.delete(id, tenantExternalId);
        return ResponseEntity.noContent().build();
    }
}
