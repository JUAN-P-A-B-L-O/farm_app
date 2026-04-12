package com.jpsoftware.farmapp.feed.service;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.mapper.FeedTypeMapper;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
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
    private final FarmAccessService farmAccessService;

    public FeedTypeService(
            FeedTypeRepository feedTypeRepository,
            FeedTypeMapper feedTypeMapper,
            FarmAccessService farmAccessService) {
        this.feedTypeRepository = feedTypeRepository;
        this.feedTypeMapper = feedTypeMapper;
        this.farmAccessService = farmAccessService;
    }

    @Transactional
    public FeedTypeResponse create(CreateFeedTypeRequest request, String farmId) {
        validateInput(request);
        String validatedFarmId = farmAccessService.validateAccessibleFarm(farmId);

        FeedTypeEntity feedTypeEntity = feedTypeMapper.toEntity(request);
        feedTypeEntity.setFarmId(validatedFarmId);
        FeedTypeEntity savedFeedType = feedTypeRepository.save(feedTypeEntity);

        return feedTypeMapper.toResponse(savedFeedType);
    }

    @Transactional(readOnly = true)
    public List<FeedTypeResponse> findAll(String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        List<FeedTypeEntity> feedTypes = StringUtils.hasText(farmId)
                ? feedTypeRepository.findByFarmIdAndActiveTrue(farmId)
                : feedTypeRepository.findByActiveTrue();

        return feedTypes.stream()
                .map(feedTypeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedTypeResponse findById(String id, String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        FeedTypeEntity feedTypeEntity = (StringUtils.hasText(farmId)
                ? feedTypeRepository.findByIdAndFarmIdAndActiveTrue(validateId(id), farmId)
                : feedTypeRepository.findByIdAndActiveTrue(validateId(id)))
                .orElseThrow(() -> new ResourceNotFoundException("Feed type not found"));

        return feedTypeMapper.toResponse(feedTypeEntity);
    }

    @Transactional
    public FeedTypeResponse update(String id, CreateFeedTypeRequest request, String farmId) {
        validateInput(request);
        FeedTypeEntity feedTypeEntity = findActiveFeedType(validateId(id), farmId);
        feedTypeEntity.setName(request.getName());
        feedTypeEntity.setCostPerKg(request.getCostPerKg());

        return feedTypeMapper.toResponse(feedTypeRepository.save(feedTypeEntity));
    }

    @Transactional
    public void delete(String id, String farmId) {
        FeedTypeEntity feedTypeEntity = findAnyFeedType(validateId(id), farmId);

        if (!Boolean.TRUE.equals(feedTypeEntity.getActive())) {
            return;
        }

        feedTypeEntity.setActive(Boolean.FALSE);
        feedTypeRepository.save(feedTypeEntity);
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

    private FeedTypeEntity findActiveFeedType(String id, String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);

        return (StringUtils.hasText(farmId)
                ? feedTypeRepository.findByIdAndFarmIdAndActiveTrue(id, farmId)
                : feedTypeRepository.findByIdAndActiveTrue(id))
                .orElseThrow(() -> new ResourceNotFoundException("Feed type not found"));
    }

    private FeedTypeEntity findAnyFeedType(String id, String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);

        return (StringUtils.hasText(farmId)
                ? feedTypeRepository.findByIdAndFarmId(id, farmId)
                : feedTypeRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Feed type not found"));
    }
}
