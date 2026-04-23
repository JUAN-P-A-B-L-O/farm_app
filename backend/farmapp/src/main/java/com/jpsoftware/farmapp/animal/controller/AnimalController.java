package com.jpsoftware.farmapp.animal.controller;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.SellAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.service.AnimalService;
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
@RequestMapping("/animals")
@Tag(name = "Animals", description = "Operations for managing farm animals.")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping
    @Operation(summary = "Create animal", description = "Creates a new animal record.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Animal created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AnimalResponse> create(@Valid @RequestBody CreateAnimalRequest request) {
        AnimalResponse response = animalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List animals", description = "Returns all animals or filters them by farm identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animals retrieved successfully")
    })
    public ResponseEntity<List<AnimalResponse>> findAll(@RequestParam(required = false) String farmId) {
        List<AnimalResponse> response = animalService.findAll(farmId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    @Operation(summary = "Export animals", description = "Exports animals as CSV using the current farm filter.")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String farmId) {
        return CsvResponseFactory.buildDownload("animals.csv", animalService.exportAll(farmId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get animal by id", description = "Returns an animal by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animal retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Animal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AnimalResponse> findById(
            @PathVariable String id,
            @RequestParam(required = false) String farmId) {
        AnimalResponse response = animalService.findById(id, farmId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update animal", description = "Updates the mutable fields of an existing animal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animal updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Animal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AnimalResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateAnimalRequest request,
            @RequestParam(required = false) String farmId) {
        AnimalResponse response = animalService.update(id, request, farmId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/sell")
    @Operation(summary = "Sell animal", description = "Marks an animal as sold and stores its sale information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Animal sold successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Animal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AnimalResponse> sell(
            @PathVariable String id,
            @Valid @RequestBody SellAnimalRequest request,
            @RequestParam(required = false) String farmId) {
        AnimalResponse response = animalService.sell(id, request, farmId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete animal", description = "Deletes an animal by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Animal deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Animal not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestParam(required = false) String farmId) {
        animalService.delete(id, farmId);
        return ResponseEntity.noContent().build();
    }
}
