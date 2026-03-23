package com.juan.farmapp.animal.controller;

import com.juan.farmapp.animal.dto.AnimalResponse;
import com.juan.farmapp.animal.dto.CreateAnimalRequest;
import com.juan.farmapp.animal.dto.UpdateAnimalRequest;
import com.juan.farmapp.animal.service.AnimalService;
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
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping
    public ResponseEntity<AnimalResponse> create(@Valid @RequestBody CreateAnimalRequest request) {
        AnimalResponse response = animalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AnimalResponse>> findAll(@RequestParam(required = false) String farmId) {
        List<AnimalResponse> response = animalService.findAll(farmId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponse> findById(@PathVariable String id) {
        AnimalResponse response = animalService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateAnimalRequest request) {
        AnimalResponse response = animalService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
