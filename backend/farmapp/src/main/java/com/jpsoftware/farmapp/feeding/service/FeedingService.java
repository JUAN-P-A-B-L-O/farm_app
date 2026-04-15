package com.jpsoftware.farmapp.feeding.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.dto.FeedTypeSummaryResponse;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
import com.jpsoftware.farmapp.feeding.dto.UpdateFeedingRequest;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import com.jpsoftware.farmapp.feeding.mapper.FeedingMapper;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final AuthenticationContextService authenticationContextService;
    private final FarmAccessService farmAccessService;

    public FeedingService(
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FeedTypeRepository feedTypeRepository,
            UserRepository userRepository,
            FeedingMapper feedingMapper,
            AuthenticationContextService authenticationContextService,
            FarmAccessService farmAccessService) {
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.feedTypeRepository = feedTypeRepository;
        this.userRepository = userRepository;
        this.feedingMapper = feedingMapper;
        this.authenticationContextService = authenticationContextService;
        this.farmAccessService = farmAccessService;
    }

    @Transactional
    public FeedingResponse create(CreateFeedingRequest request, String farmId) {
        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
        validateInput(request, createdBy);
        String resolvedFarmId = resolveFarmId(request, farmId);
        validateRelations(request, createdBy, resolvedFarmId);

        FeedingEntity feedingEntity = feedingMapper.toEntity(request);
        feedingEntity.setQuantity(DecimalScaleUtils.normalize(feedingEntity.getQuantity()));
        feedingEntity.setCreatedBy(createdBy);
        feedingEntity.setFarmId(resolvedFarmId);
        feedingEntity.setStatus(FeedingEntity.STATUS_ACTIVE);
        FeedingEntity savedFeeding = feedingRepository.save(feedingEntity);

        return toEnrichedResponse(savedFeeding);
    }

    @Transactional(readOnly = true)
    public List<FeedingResponse> findAll(String animalId, LocalDate date, String farmId) {
        return getAllFeedings(animalId, date, farmId);
    }

    @Transactional(readOnly = true)
    public List<FeedingResponse> getAllFeedings(String animalId, LocalDate date, String farmId) {
        List<FeedingEntity> feedings = findFeedings(animalId, date, farmId);
        Map<String, AnimalSummaryResponse> animalsById = loadAnimalSummariesById(feedings);
        Map<String, FeedTypeSummaryResponse> feedTypesById = loadFeedTypeSummariesById(feedings);
        return feedings.stream()
                .map(feeding -> feedingMapper.toResponse(
                        feeding,
                        animalsById.get(feeding.getAnimalId()),
                        feedTypesById.get(feeding.getFeedTypeId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedingResponse> findAllPaginated(String animalId, LocalDate date, String farmId, int page, int size) {
        return getAllFeedingsPaginated(animalId, date, farmId, page, size);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedingResponse> getAllFeedingsPaginated(String animalId, LocalDate date, String farmId, int page, int size) {
        Page<FeedingEntity> feedings = findFeedings(animalId, date, farmId, PageRequest.of(page, size));
        Map<String, AnimalSummaryResponse> animalsById = loadAnimalSummariesById(feedings.getContent());
        Map<String, FeedTypeSummaryResponse> feedTypesById = loadFeedTypeSummariesById(feedings.getContent());
        Page<FeedingResponse> responses = feedings.map(feeding -> feedingMapper.toResponse(
                feeding,
                animalsById.get(feeding.getAnimalId()),
                feedTypesById.get(feeding.getFeedTypeId())));
        return toPaginatedResponse(responses);
    }

    @Transactional(readOnly = true)
    public FeedingResponse findById(String id, String farmId) {
        return getFeedingById(id, farmId);
    }

    @Transactional(readOnly = true)
    public FeedingResponse getFeedingById(String id, String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        FeedingEntity feedingEntity = (StringUtils.hasText(farmId)
                ? feedingRepository.findByIdAndFarmIdAndStatus(validateId(id), farmId, FeedingEntity.STATUS_ACTIVE)
                : feedingRepository.findById(validateId(id)))
                .orElseThrow(() -> new ResourceNotFoundException("Feeding not found"));

        return toEnrichedResponse(feedingEntity);
    }

    @Transactional
    public FeedingResponse updateFeeding(String id, UpdateFeedingRequest request, String farmId) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }

        FeedingEntity feedingEntity = getFeedingIncludingInactive(id, farmId);
        ensureFeedingIsActive(feedingEntity, "Inactive feeding cannot be updated");

        if (StringUtils.hasText(request.getAnimalId())) {
            validateAnimalExists(request.getAnimalId(), farmId);
            feedingEntity.setAnimalId(request.getAnimalId());
        }
        if (StringUtils.hasText(request.getFeedTypeId())) {
            validateFeedTypeExists(request.getFeedTypeId(), farmId);
            feedingEntity.setFeedTypeId(request.getFeedTypeId());
        }
        if (request.getDate() != null) {
            feedingEntity.setDate(request.getDate());
        }
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                throw new ValidationException("quantity must be greater than zero");
            }
            DecimalScaleUtils.requireMaxScale(request.getQuantity(), "quantity");
            feedingEntity.setQuantity(DecimalScaleUtils.normalize(request.getQuantity()));
        }

        FeedingEntity savedFeeding = feedingRepository.save(feedingEntity);
        return toEnrichedResponse(savedFeeding);
    }

    @Transactional
    public void deleteFeeding(String id, String farmId) {
        FeedingEntity feedingEntity = getFeedingIncludingInactive(id, farmId);
        if (FeedingEntity.STATUS_INACTIVE.equals(feedingEntity.getStatus())) {
            return;
        }

        feedingEntity.setStatus(FeedingEntity.STATUS_INACTIVE);
        feedingRepository.save(feedingEntity);
    }

    private FeedingResponse toEnrichedResponse(FeedingEntity feedingEntity) {
        AnimalSummaryResponse animal = animalRepository.findById(feedingEntity.getAnimalId())
                .map(this::toAnimalSummary)
                .orElse(null);
        FeedTypeSummaryResponse feedType = feedTypeRepository.findById(feedingEntity.getFeedTypeId())
                .map(this::toFeedTypeSummary)
                .orElse(null);
        return feedingMapper.toResponse(feedingEntity, animal, feedType);
    }

    private Map<String, AnimalSummaryResponse> loadAnimalSummariesById(Collection<FeedingEntity> feedings) {
        Set<String> animalIds = feedings.stream()
                .map(FeedingEntity::getAnimalId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        return animalRepository.findAllById(animalIds).stream()
                .collect(Collectors.toMap(AnimalEntity::getId, this::toAnimalSummary));
    }

    private Map<String, FeedTypeSummaryResponse> loadFeedTypeSummariesById(Collection<FeedingEntity> feedings) {
        Set<String> feedTypeIds = feedings.stream()
                .map(FeedingEntity::getFeedTypeId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        return feedTypeRepository.findAllById(feedTypeIds).stream()
                .collect(Collectors.toMap(FeedTypeEntity::getId, this::toFeedTypeSummary));
    }

    private AnimalSummaryResponse toAnimalSummary(AnimalEntity animalEntity) {
        return new AnimalSummaryResponse(animalEntity.getId(), animalEntity.getTag());
    }

    private FeedTypeSummaryResponse toFeedTypeSummary(FeedTypeEntity feedTypeEntity) {
        return new FeedTypeSummaryResponse(feedTypeEntity.getId(), feedTypeEntity.getName());
    }

    private void validateInput(CreateFeedingRequest request, String createdBy) {
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
        DecimalScaleUtils.requireMaxScale(request.getQuantity(), "quantity");
        if (!StringUtils.hasText(createdBy)) {
            throw new ValidationException("userId must not be blank");
        }
    }

    private void validateRelations(CreateFeedingRequest request, String createdBy, String farmId) {
        validateAnimalExists(request.getAnimalId(), farmId);
        validateFeedTypeExists(request.getFeedTypeId(), farmId);
        if (!userRepository.existsById(parseUserId(createdBy))) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private String validateId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }
        return id;
    }

    private List<FeedingEntity> findFeedings(String animalId, LocalDate date, String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        if (StringUtils.hasText(farmId) && StringUtils.hasText(animalId) && date != null) {
            return feedingRepository.findByFarmIdAndAnimalIdAndDateAndStatus(farmId, animalId, date, FeedingEntity.STATUS_ACTIVE);
        }
        if (StringUtils.hasText(farmId) && StringUtils.hasText(animalId)) {
            return feedingRepository.findByFarmIdAndAnimalIdAndStatus(farmId, animalId, FeedingEntity.STATUS_ACTIVE);
        }
        if (StringUtils.hasText(farmId) && date != null) {
            return feedingRepository.findByFarmIdAndDateAndStatus(farmId, date, FeedingEntity.STATUS_ACTIVE);
        }
        if (StringUtils.hasText(farmId)) {
            return feedingRepository.findByFarmIdAndStatus(farmId, FeedingEntity.STATUS_ACTIVE);
        }
        if (StringUtils.hasText(animalId) && date != null) {
            return feedingRepository.findByAnimalIdAndDateAndStatus(animalId, date, FeedingEntity.STATUS_ACTIVE);
        }
        if (StringUtils.hasText(animalId)) {
            return feedingRepository.findByAnimalIdAndStatus(animalId, FeedingEntity.STATUS_ACTIVE);
        }
        if (date != null) {
            return feedingRepository.findByDateAndStatus(date, FeedingEntity.STATUS_ACTIVE);
        }
        return feedingRepository.findAll();
    }

    private Page<FeedingEntity> findFeedings(String animalId, LocalDate date, String farmId, org.springframework.data.domain.Pageable pageable) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        if (StringUtils.hasText(farmId) && StringUtils.hasText(animalId) && date != null) {
            return feedingRepository.findByFarmIdAndAnimalIdAndDateAndStatus(farmId, animalId, date, FeedingEntity.STATUS_ACTIVE, pageable);
        }
        if (StringUtils.hasText(farmId) && StringUtils.hasText(animalId)) {
            return feedingRepository.findByFarmIdAndAnimalIdAndStatus(farmId, animalId, FeedingEntity.STATUS_ACTIVE, pageable);
        }
        if (StringUtils.hasText(farmId) && date != null) {
            return feedingRepository.findByFarmIdAndDateAndStatus(farmId, date, FeedingEntity.STATUS_ACTIVE, pageable);
        }
        if (StringUtils.hasText(farmId)) {
            return feedingRepository.findByFarmIdAndStatus(farmId, FeedingEntity.STATUS_ACTIVE, pageable);
        }
        if (StringUtils.hasText(animalId) && date != null) {
            return feedingRepository.findByAnimalIdAndDateAndStatus(animalId, date, FeedingEntity.STATUS_ACTIVE, pageable);
        }
        if (StringUtils.hasText(animalId)) {
            return feedingRepository.findByAnimalIdAndStatus(animalId, FeedingEntity.STATUS_ACTIVE, pageable);
        }
        if (date != null) {
            return feedingRepository.findByDateAndStatus(date, FeedingEntity.STATUS_ACTIVE, pageable);
        }
        return feedingRepository.findAll(pageable);
    }

    private FeedingEntity getFeedingIncludingInactive(String id, String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        return (StringUtils.hasText(farmId)
                ? feedingRepository.findAnyByIdAndFarmId(validateId(id), farmId)
                : feedingRepository.findAnyById(validateId(id)))
                .orElseThrow(() -> new ResourceNotFoundException("Feeding not found"));
    }

    private void ensureFeedingIsActive(FeedingEntity feedingEntity, String message) {
        if (FeedingEntity.STATUS_INACTIVE.equals(feedingEntity.getStatus())) {
            throw new ConflictException(message);
        }
    }

    private AnimalEntity validateAnimalExists(String animalId, String farmId) {
        AnimalEntity animal = StringUtils.hasText(farmId)
                ? animalRepository.findByIdAndFarmId(animalId, farmId).orElse(null)
                : animalRepository.findById(animalId).orElse(null);
        if (animal == null) {
            throw new ResourceNotFoundException("Animal not found");
        }
        if (!AnimalEntity.STATUS_ACTIVE.equals(animal.getStatus())) {
            throw new ValidationException("Animal must be ACTIVE for feeding operations");
        }
        return animal;
    }

    private void validateFeedTypeExists(String feedTypeId, String farmId) {
        if (!feedTypeRepository.existsById(feedTypeId)
                || (StringUtils.hasText(farmId) && !feedTypeRepository.existsByIdAndFarmId(feedTypeId, farmId))) {
            throw new ResourceNotFoundException("Feed type not found");
        }
    }

    private String resolveFarmId(CreateFeedingRequest request, String farmId) {
        if (StringUtils.hasText(farmId)) {
            return farmAccessService.validateAccessibleFarm(farmId);
        }

        AnimalEntity animalEntity = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));
        return farmAccessService.validateAccessibleFarm(animalEntity.getFarmId());
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
