package com.jpsoftware.farmapp.feeding.service;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import com.jpsoftware.farmapp.feeding.mapper.FeedingMapper;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FeedingService {

    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final FeedTypeRepository feedTypeRepository;
    private final UserRepository userRepository;
    private final FeedingMapper feedingMapper;

    public FeedingService(
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FeedTypeRepository feedTypeRepository,
            UserRepository userRepository,
            FeedingMapper feedingMapper) {
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.feedTypeRepository = feedTypeRepository;
        this.userRepository = userRepository;
        this.feedingMapper = feedingMapper;
    }

    @Transactional
    public FeedingResponse create(CreateFeedingRequest request) {
        validateInput(request);
        validateRelations(request);

        FeedingEntity feedingEntity = feedingMapper.toEntity(request);
        FeedingEntity savedFeeding = feedingRepository.save(feedingEntity);

        return feedingMapper.toResponse(savedFeeding);
    }

    @Transactional(readOnly = true)
    public List<FeedingResponse> findAll(String animalId, LocalDate date) {
        return findFeedings(animalId, date).stream()
                .map(feedingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedingResponse> findAllPaginated(String animalId, LocalDate date, int page, int size) {
        Page<FeedingResponse> responses = findFeedings(animalId, date, PageRequest.of(page, size))
                .map(feedingMapper::toResponse);
        return toPaginatedResponse(responses);
    }

    @Transactional(readOnly = true)
    public FeedingResponse findById(String id) {
        FeedingEntity feedingEntity = feedingRepository.findById(validateId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Feeding not found"));

        return feedingMapper.toResponse(feedingEntity);
    }

    private void validateInput(CreateFeedingRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getAnimalId())) {
            throw new ValidationException("animalId must not be blank");
        }
        if (!StringUtils.hasText(request.getFeedTypeId())) {
            throw new ValidationException("feedTypeId must not be blank");
        }
        if (request.getDate() == null) {
            throw new ValidationException("date must not be null");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("quantity must be greater than zero");
        }
        if (!StringUtils.hasText(request.getUserId())) {
            throw new ValidationException("userId must not be blank");
        }
    }

    private void validateRelations(CreateFeedingRequest request) {
        if (!animalRepository.existsById(request.getAnimalId())) {
            throw new ResourceNotFoundException("Animal not found");
        }
        if (!feedTypeRepository.existsById(request.getFeedTypeId())) {
            throw new ResourceNotFoundException("Feed type not found");
        }
        if (!userRepository.existsById(parseUserId(request.getUserId()))) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private String validateId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }
        return id;
    }

    private List<FeedingEntity> findFeedings(String animalId, LocalDate date) {
        if (StringUtils.hasText(animalId) && date != null) {
            return feedingRepository.findByAnimalIdAndDate(animalId, date);
        }
        if (StringUtils.hasText(animalId)) {
            return feedingRepository.findByAnimalId(animalId);
        }
        if (date != null) {
            return feedingRepository.findByDate(date);
        }
        return feedingRepository.findAll();
    }

    private Page<FeedingEntity> findFeedings(String animalId, LocalDate date, org.springframework.data.domain.Pageable pageable) {
        if (StringUtils.hasText(animalId) && date != null) {
            return feedingRepository.findByAnimalIdAndDate(animalId, date, pageable);
        }
        if (StringUtils.hasText(animalId)) {
            return feedingRepository.findByAnimalId(animalId, pageable);
        }
        if (date != null) {
            return feedingRepository.findByDate(date, pageable);
        }
        return feedingRepository.findAll(pageable);
    }

    private PaginatedResponse<FeedingResponse> toPaginatedResponse(Page<FeedingResponse> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private java.util.UUID parseUserId(String userId) {
        try {
            return java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("userId must be a valid UUID");
        }
    }
}
