package com.jpsoftware.farmapp.feeding.controller;

import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
import com.jpsoftware.farmapp.feeding.dto.UpdateFeedingRequest;
import com.jpsoftware.farmapp.feeding.service.FeedingService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/feedings")
@Tag(name = "Feedings", description = "Operations for recording animal feeding events.")
public class FeedingController {

    private final FeedingService feedingService;

    public FeedingController(FeedingService feedingService) {
        this.feedingService = feedingService;
    }

    @PostMapping
    @Operation(summary = "Create feeding", description = "Registers a new feeding event.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Feeding created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Related resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FeedingResponse> create(@Valid @RequestBody CreateFeedingRequest request) {
        FeedingResponse response = feedingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List feedings", description = "Returns feeding records, optionally filtered by animal or date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feedings retrieved successfully")
    })
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            PaginatedResponse<FeedingResponse> response = feedingService.findAllPaginated(animalId, date, page, size);
            return ResponseEntity.ok(response);
        }

        List<FeedingResponse> response = feedingService.findAll(animalId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feeding by id", description = "Returns a feeding record by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feeding retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid feeding identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feeding not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FeedingResponse> findById(@PathVariable String id) {
        FeedingResponse response = feedingService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update feeding", description = "Updates mutable fields of an existing feeding record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feeding updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feeding or related resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Feeding is inactive",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FeedingResponse> update(
            @PathVariable String id,
            @RequestBody UpdateFeedingRequest request) {
        FeedingResponse response = feedingService.updateFeeding(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete feeding", description = "Soft deletes a feeding record by marking it inactive.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Feeding deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Feeding not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        feedingService.deleteFeeding(id);
        return ResponseEntity.noContent().build();
    }
}
