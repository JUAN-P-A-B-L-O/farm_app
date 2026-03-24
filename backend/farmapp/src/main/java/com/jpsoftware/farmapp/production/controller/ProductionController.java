package com.jpsoftware.farmapp.production.controller;

import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
import com.jpsoftware.farmapp.production.service.ProductionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/productions")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @GetMapping
    public ResponseEntity<List<ProductionResponse>> findAll(
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) LocalDate date) {
        List<ProductionResponse> response = productionService.findAll(animalId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionResponse> findById(@PathVariable String id) {
        ProductionResponse response = productionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/by-animal")
    public ResponseEntity<ProductionSummaryResponse> getSummaryByAnimal(
            @RequestParam @NotBlank(message = "animalId must not be blank") String animalId) {
        ProductionSummaryResponse response = productionService.getSummaryByAnimal(animalId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductionResponse> create(@Valid @RequestBody CreateProductionRequest request) {
        ProductionResponse response = productionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionResponse> update(
            @PathVariable String id,
            @RequestBody UpdateProductionRequest request) {
        ProductionResponse response = productionService.update(id, request);
        return ResponseEntity.ok(response);
    }
}
