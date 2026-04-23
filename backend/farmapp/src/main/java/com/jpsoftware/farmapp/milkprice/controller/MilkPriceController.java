package com.jpsoftware.farmapp.milkprice.controller;

import com.jpsoftware.farmapp.milkprice.dto.CreateMilkPriceRequest;
import com.jpsoftware.farmapp.milkprice.dto.MilkPriceResponse;
import com.jpsoftware.farmapp.milkprice.service.MilkPriceService;
import com.jpsoftware.farmapp.shared.exception.ErrorResponse;
import com.jpsoftware.farmapp.shared.util.CsvResponseFactory;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/milk-prices")
@Tag(name = "Milk Prices", description = "Operations for managing milk price history.")
public class MilkPriceController {

    private final MilkPriceService milkPriceService;

    public MilkPriceController(MilkPriceService milkPriceService) {
        this.milkPriceService = milkPriceService;
    }

    @PostMapping
    @Operation(summary = "Register milk price", description = "Stores a new milk price record without overwriting previous values.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Milk price registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Farm not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MilkPriceResponse> create(
            @Valid @RequestBody CreateMilkPriceRequest request,
            @RequestParam String farmId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(milkPriceService.create(request, farmId));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current milk price", description = "Returns the active milk price for the farm based on the latest effective date up to today.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current milk price retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Farm not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MilkPriceResponse> getCurrent(@RequestParam String farmId) {
        return ResponseEntity.ok(milkPriceService.getCurrent(farmId));
    }

    @GetMapping
    @Operation(summary = "List milk price history", description = "Returns all registered milk prices for the farm ordered from newest to oldest.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Milk price history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Farm not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<MilkPriceResponse>> getHistory(@RequestParam String farmId) {
        return ResponseEntity.ok(milkPriceService.getHistory(farmId));
    }

    @GetMapping("/export")
    @Operation(summary = "Export milk price history", description = "Exports milk price history as CSV for the selected farm.")
    public ResponseEntity<byte[]> export(@RequestParam String farmId) {
        return CsvResponseFactory.buildDownload("milk-prices.csv", milkPriceService.exportHistory(farmId));
    }
}
