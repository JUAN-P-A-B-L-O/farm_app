package com.jpsoftware.farmapp.farm.controller;

import com.jpsoftware.farmapp.farm.dto.CreateFarmRequest;
import com.jpsoftware.farmapp.farm.dto.FarmResponse;
import com.jpsoftware.farmapp.farm.service.FarmService;
import com.jpsoftware.farmapp.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/farms")
@Tag(name = "Farms", description = "Operations for managing accessible farms.")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping
    @Operation(summary = "List farms", description = "Returns the farms accessible to the authenticated user.")
    public ResponseEntity<List<FarmResponse>> findAll(
            @RequestParam(defaultValue = "false") boolean ownedOnly) {
        return ResponseEntity.ok(farmService.findAccessibleFarms(ownedOnly));
    }

    @PostMapping
    @Operation(summary = "Create farm", description = "Creates a new farm for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Farm created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FarmResponse> create(@Valid @RequestBody CreateFarmRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.create(request));
    }
}
