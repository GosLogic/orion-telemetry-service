package com.goslogic.orion.telemetry.presentation.rest;

import com.goslogic.orion.telemetry.application.TelemetryIngestionService;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer.BatchIngest;
import com.goslogic.orion.telemetry.application.port.TelemetryPositionConsumer.PositionPayload;
import com.goslogic.orion.telemetry.domain.model.VehiclePosition;
import com.goslogic.orion.telemetry.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/telemetry/vehicle-positions")
@Tag(name = "Vehicle Positions", description = "Ingesta y consulta de posiciones GPS")
public class VehiclePositionController {

    private final TelemetryPositionConsumer positionConsumer;
    private final TelemetryIngestionService ingestionService;

    public VehiclePositionController(TelemetryPositionConsumer positionConsumer,
                                       TelemetryIngestionService ingestionService) {
        this.positionConsumer = positionConsumer;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/batch")
    @Operation(summary = "Ingesta batch de posiciones GPS (contrato móvil Fase 2)")
    public ResponseEntity<BatchResultResponse> ingestBatch(
            @Valid @RequestBody BatchPositionRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader("X-Driver-Id") String driverExternalId) {
        BatchIngest command = new BatchIngest(
                tenantExternalId,
                driverExternalId,
                req.positions().stream().map(this::toPayload).toList()
        );
        var result = positionConsumer.consume(command);
        return ResponseEntity.ok(new BatchResultResponse(result.accepted(), result.rejected()));
    }

    @PostMapping
    @Operation(summary = "Ingesta posición individual (emergencia)")
    public ResponseEntity<BatchResultResponse> ingestSingle(
            @Valid @RequestBody PositionRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId,
            @RequestHeader("X-Driver-Id") String driverExternalId) {
        BatchIngest command = new BatchIngest(
                tenantExternalId,
                driverExternalId,
                List.of(toPayload(req))
        );
        var result = positionConsumer.consume(command);
        return ResponseEntity.ok(new BatchResultResponse(result.accepted(), result.rejected()));
    }

    @GetMapping("/latest")
    @Operation(summary = "Última posición conocida de un vehículo")
    public ResponseEntity<VehiclePositionResponse> findLatest(
            @RequestParam("vehicle_id") String vehicleExternalId,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        VehiclePosition position = ingestionService.findLatest(vehicleExternalId, tenantExternalId);
        return ResponseEntity.ok(VehiclePositionResponse.from(position));
    }

    private PositionPayload toPayload(PositionRequest req) {
        return new PositionPayload(
                req.time(),
                resolveVehicleExternalId(req.vehicleId()),
                req.latitude(),
                req.longitude(),
                req.speedKmh(),
                req.heading(),
                resolveRouteSheetExternalId(req.routeSheetId()),
                req.mocked()
        );
    }

    /** ACL: el móvil envía INT; iter 1 almacena como string. Q4: Gateway resolverá al external_id real. */
    private String resolveVehicleExternalId(Integer vehicleId) {
        if (vehicleId == null) {
            return "vehicle-unknown";
        }
        return "vehicle-" + String.format("%03d", vehicleId);
    }

    private String resolveRouteSheetExternalId(Integer routeSheetId) {
        if (routeSheetId == null) {
            return null;
        }
        return "route-" + String.format("%03d", routeSheetId);
    }
}
