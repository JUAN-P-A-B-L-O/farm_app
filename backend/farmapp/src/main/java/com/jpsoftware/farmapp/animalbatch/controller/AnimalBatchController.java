package com.jpsoftware.farmapp.animalbatch.controller;

import com.jpsoftware.farmapp.animalbatch.dto.AnimalBatchResponse;
import com.jpsoftware.farmapp.animalbatch.dto.CreateAnimalBatchRequest;
import com.jpsoftware.farmapp.animalbatch.dto.UpdateAnimalBatchRequest;
import com.jpsoftware.farmapp.animalbatch.service.AnimalBatchService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/animal-batches")
@Tag(name = "Animal Batches", description = "Operations for managing farm-scoped animal batches.")
public class AnimalBatchController {

    private final AnimalBatchService animalBatchService;

    public AnimalBatchController(AnimalBatchService animalBatchService) {
        this.animalBatchService = animalBatchService;
    }

    @PostMapping
    @Operation(summary = "Create animal batch", description = "Creates a new animal batch for the selected farm.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Animal batch created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Farm or animal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AnimalBatchResponse> create(
            @Valid @RequestBody CreateAnimalBatchRequest request,
            @RequestParam String farmId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalBatchService.create(request, farmId));
    }

    @GetMapping
    @Operation(summary = "List animal batches", description = "Returns animal batches for the selected farm.")
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String search,
            @RequestParam String farmId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PaginatedResponse<AnimalBatchResponse> response = animalBatchService.findAllPaginated(search, farmId, page, size);
            return ResponseEntity.ok(response);
        }

        List<AnimalBatchResponse> response = animalBatchService.findAll(search, farmId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get animal batch by id", description = "Returns a farm-scoped animal batch by its identifier.")
    public ResponseEntity<AnimalBatchResponse> findById(
            @PathVariable String id,
            @RequestParam String farmId) {
        return ResponseEntity.ok(animalBatchService.findById(id, farmId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update animal batch", description = "Updates a farm-scoped animal batch.")
    public ResponseEntity<AnimalBatchResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateAnimalBatchRequest request,
            @RequestParam String farmId) {
        return ResponseEntity.ok(animalBatchService.update(id, request, farmId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete animal batch", description = "Deletes a farm-scoped animal batch.")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestParam String farmId) {
        animalBatchService.delete(id, farmId);
        return ResponseEntity.noContent().build();
    }
}
