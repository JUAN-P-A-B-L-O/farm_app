package com.jpsoftware.farmapp.feed.controller;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.service.FeedTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed-types")
public class FeedTypeController {

    private final FeedTypeService feedTypeService;

    public FeedTypeController(FeedTypeService feedTypeService) {
        this.feedTypeService = feedTypeService;
    }

    @PostMapping
    public ResponseEntity<FeedTypeResponse> create(@Valid @RequestBody CreateFeedTypeRequest request) {
        FeedTypeResponse response = feedTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FeedTypeResponse>> findAll() {
        List<FeedTypeResponse> response = feedTypeService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedTypeResponse> findById(@PathVariable String id) {
        FeedTypeResponse response = feedTypeService.findById(id);
        return ResponseEntity.ok(response);
    }
}
