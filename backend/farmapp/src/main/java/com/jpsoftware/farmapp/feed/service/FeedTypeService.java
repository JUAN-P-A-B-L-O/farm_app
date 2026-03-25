package com.jpsoftware.farmapp.feed.service;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.mapper.FeedTypeMapper;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FeedTypeService {

    private final FeedTypeRepository feedTypeRepository;
    private final FeedTypeMapper feedTypeMapper;

    public FeedTypeService(FeedTypeRepository feedTypeRepository, FeedTypeMapper feedTypeMapper) {
        this.feedTypeRepository = feedTypeRepository;
        this.feedTypeMapper = feedTypeMapper;
    }

    @Transactional
    public FeedTypeResponse create(CreateFeedTypeRequest request) {
        validateInput(request);

        FeedTypeEntity feedTypeEntity = feedTypeMapper.toEntity(request);
        FeedTypeEntity savedFeedType = feedTypeRepository.save(feedTypeEntity);

        return feedTypeMapper.toResponse(savedFeedType);
    }

    @Transactional(readOnly = true)
    public List<FeedTypeResponse> findAll() {
        return feedTypeRepository.findAll().stream()
                .map(feedTypeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedTypeResponse findById(String id) {
        FeedTypeEntity feedTypeEntity = feedTypeRepository.findById(validateId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Feed type not found"));

        return feedTypeMapper.toResponse(feedTypeEntity);
    }

    private void validateInput(CreateFeedTypeRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (request.getCostPerKg() == null || request.getCostPerKg() <= 0) {
            throw new ValidationException("costPerKg must be greater than zero");
        }
    }

    private String validateId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }
        return id;
    }
}
