package com.jpsoftware.farmapp.feed.controller;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.service.FeedTypeService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed-types")
@Tag(name = "Feed Types", description = "Operations for managing feed types.")
public class FeedTypeController {

    private final FeedTypeService feedTypeService;

    public FeedTypeController(FeedTypeService feedTypeService) {
        this.feedTypeService = feedTypeService;
    }

    @PostMapping
    @Operation(summary = "Create feed type", description = "Creates a new feed type.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Feed type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FeedTypeResponse> create(
            @Valid @RequestBody CreateFeedTypeRequest request,
            @RequestParam(required = false) String farmId) {
        FeedTypeResponse response = feedTypeService.create(request, farmId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List feed types", description = "Returns all feed types.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed types retrieved successfully")
    })
    public ResponseEntity<List<FeedTypeResponse>> findAll(@RequestParam(required = false) String farmId) {
        List<FeedTypeResponse> response = feedTypeService.findAll(farmId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @Operation(summary = "Export feed types", description = "Exports feed types as CSV using the current farm filter.")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String farmId) {
        return CsvResponseFactory.buildDownload("feed-types.csv", feedTypeService.exportAll(farmId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feed type by id", description = "Returns a feed type by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed type retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid feed type identifier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Feed type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FeedTypeResponse> findById(
            @PathVariable String id,
            @RequestParam(required = false) String farmId) {
        FeedTypeResponse response = feedTypeService.findById(id, farmId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update feed type", description = "Updates an existing feed type.")
    public ResponseEntity<FeedTypeResponse> update(
            @PathVariable String id,
            @Valid @RequestBody CreateFeedTypeRequest request,
            @RequestParam(required = false) String farmId) {
        FeedTypeResponse response = feedTypeService.update(id, request, farmId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete feed type", description = "Soft deletes a feed type by marking it inactive.")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestParam(required = false) String farmId) {
        feedTypeService.delete(id, farmId);
        return ResponseEntity.noContent().build();
    }
}
