package com.jpsoftware.farmapp.production.controller;

import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionProfitResponse;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
import com.jpsoftware.farmapp.production.service.ProductionService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/productions")
@Tag(name = "Productions", description = "Operations for managing milk production records and summaries.")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @GetMapping
    @Operation(summary = "List productions", description = "Returns production records, optionally filtered by animal or date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Production records retrieved successfully")
    })
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PaginatedResponse<ProductionResponse> response = productionService.findAllPaginated(animalId, date, page, size);
            return ResponseEntity.ok(response);
        }

        List<ProductionResponse> response = productionService.findAll(animalId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get production by id", description = "Returns a production record by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Production record retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid production identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Production not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductionResponse> findById(@PathVariable String id) {
        ProductionResponse response = productionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/by-animal")
    @Operation(summary = "Get production summary by animal", description = "Returns the total produced quantity for a specific animal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Production summary retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid animal identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Animal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductionSummaryResponse> getSummaryByAnimal(
            @RequestParam @NotBlank(message = "animalId must not be blank") String animalId) {
        ProductionSummaryResponse response = productionService.getSummaryByAnimal(animalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/profit/by-animal")
    @Operation(summary = "Get production profit by animal", description = "Returns production, cost, revenue, and profit metrics for a specific animal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Production profit summary retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid animal identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Animal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductionProfitResponse> getProfit(
            @RequestParam @NotBlank(message = "animalId must not be blank") String animalId) {
        ProductionProfitResponse response = productionService.getProfitByAnimal(animalId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create production", description = "Creates a new production record.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Production record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Related resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductionResponse> create(@Valid @RequestBody CreateProductionRequest request) {
        ProductionResponse response = productionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update production", description = "Updates mutable fields of an existing production record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Production record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Production not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductionResponse> update(
            @PathVariable String id,
            @RequestBody UpdateProductionRequest request) {
        ProductionResponse response = productionService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete production", description = "Soft deletes a production record by marking it inactive.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Production deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Production not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productionService.deleteProduction(id);
        return ResponseEntity.noContent().build();
    }
}
