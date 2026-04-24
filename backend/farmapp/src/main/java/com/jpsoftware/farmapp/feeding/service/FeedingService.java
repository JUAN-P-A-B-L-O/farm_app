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
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FeedingService {
    private static final String WORKER_ROLE = "WORKER";

    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final FeedTypeRepository feedTypeRepository;
    private final UserRepository userRepository;
    private final FeedingMapper feedingMapper;
    private final AuthenticationContextService authenticationContextService;
    private final FarmAccessService farmAccessService;

    @Autowired
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

    public FeedingService(
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FeedTypeRepository feedTypeRepository,
            UserRepository userRepository,
            FeedingMapper feedingMapper,
            AuthenticationContextService authenticationContextService) {
        this(
                feedingRepository,
                animalRepository,
                feedTypeRepository,
                userRepository,
                feedingMapper,
                authenticationContextService,
                null);
    }

    @Transactional
    public FeedingResponse create(CreateFeedingRequest request, String farmId) {
        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
        LocalDate effectiveDate = resolveCreationDate(request);
        validateInput(request, createdBy, effectiveDate);
        String resolvedFarmId = resolveFarmId(request, farmId);
        validateRelations(request, createdBy, resolvedFarmId);

        FeedingEntity feedingEntity = feedingMapper.toEntity(request);
        feedingEntity.setDate(effectiveDate);
        feedingEntity.setQuantity(DecimalScaleUtils.normalize(feedingEntity.getQuantity()));
        feedingEntity.setCreatedBy(createdBy);
        feedingEntity.setFarmId(resolvedFarmId);
        feedingEntity.setStatus(FeedingEntity.STATUS_ACTIVE);
        FeedingEntity savedFeeding = feedingRepository.save(feedingEntity);

        return toEnrichedResponse(savedFeeding);
    }

    @Transactional
    public FeedingResponse create(CreateFeedingRequest request) {
        return create(request, null);
    }

    @Transactional(readOnly = true)
    public List<FeedingResponse> findAll(String search, String animalId, String feedTypeId, LocalDate date, String farmId) {
        return getAllFeedings(search, animalId, feedTypeId, date, farmId);
    }

    @Transactional(readOnly = true)
    public String exportAll(String search, String animalId, String feedTypeId, LocalDate date, String farmId) {
        return CsvExportUtils.write(findAll(search, animalId, feedTypeId, date, farmId), List.of(
                new CsvColumn<>("id", FeedingResponse::getId),
                new CsvColumn<>("animalId", FeedingResponse::getAnimalId),
                new CsvColumn<>("animalTag", feeding -> feeding.getAnimal() != null ? feeding.getAnimal().getTag() : null),
                new CsvColumn<>("feedTypeId", FeedingResponse::getFeedTypeId),
                new CsvColumn<>("feedTypeName", feeding -> feeding.getFeedType() != null ? feeding.getFeedType().getName() : null),
                new CsvColumn<>("date", FeedingResponse::getDate),
                new CsvColumn<>("quantity", FeedingResponse::getQuantity)));
    }

    @Transactional(readOnly = true)
    public List<FeedingResponse> findAll(String animalId, LocalDate date) {
        return findAll(null, animalId, null, date, null);
    }

    @Transactional(readOnly = true)
    public List<FeedingResponse> getAllFeedings(String search, String animalId, String feedTypeId, LocalDate date, String farmId) {
        List<FeedingEntity> feedings = findFeedings(search, animalId, feedTypeId, date, farmId);
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
    public PaginatedResponse<FeedingResponse> findAllPaginated(
            String search,
            String animalId,
            String feedTypeId,
            LocalDate date,
            String farmId,
            int page,
            int size) {
        return getAllFeedingsPaginated(search, animalId, feedTypeId, date, farmId, page, size);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedingResponse> findAllPaginated(String animalId, LocalDate date, int page, int size) {
        return findAllPaginated(null, animalId, null, date, null, page, size);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedingResponse> getAllFeedingsPaginated(
            String search,
            String animalId,
            String feedTypeId,
            LocalDate date,
            String farmId,
            int page,
            int size) {
        Page<FeedingEntity> feedings = findFeedings(search, animalId, feedTypeId, date, farmId, PageRequest.of(page, size));
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
    public FeedingResponse findById(String id) {
        return findById(id, null);
    }

    @Transactional(readOnly = true)
    public FeedingResponse getFeedingById(String id, String farmId) {
        validateAccessibleFarmIfPresent(farmId);
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
    public FeedingResponse updateFeeding(String id, UpdateFeedingRequest request) {
        return updateFeeding(id, request, null);
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

    @Transactional
    public void deleteFeeding(String id) {
        deleteFeeding(id, null);
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

    private void validateInput(CreateFeedingRequest request, String createdBy, LocalDate effectiveDate) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getAnimalId())) {
            throw new ValidationException("animalId must not be blank");
        }
        if (!StringUtils.hasText(request.getFeedTypeId())) {
            throw new ValidationException("feedTypeId must not be blank");
        }
        if (effectiveDate == null) {
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

    private List<FeedingEntity> findFeedings(String search, String animalId, String feedTypeId, LocalDate date, String farmId) {
        validateAccessibleFarmIfPresent(farmId);
        return feedingRepository.findAll(buildFeedingSpecification(search, animalId, feedTypeId, date, farmId));
    }

    private Page<FeedingEntity> findFeedings(
            String search,
            String animalId,
            String feedTypeId,
            LocalDate date,
            String farmId,
            org.springframework.data.domain.Pageable pageable) {
        validateAccessibleFarmIfPresent(farmId);
        return feedingRepository.findAll(buildFeedingSpecification(search, animalId, feedTypeId, date, farmId), pageable);
    }

    private Specification<FeedingEntity> buildFeedingSpecification(
            String search,
            String animalId,
            String feedTypeId,
            LocalDate date,
            String farmId) {
        String normalizedSearch = normalizeFilter(search);
        Specification<FeedingEntity> specification = Specification.where((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), FeedingEntity.STATUS_ACTIVE));

        if (StringUtils.hasText(farmId)) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("farmId"), farmId));
        }
        if (StringUtils.hasText(animalId)) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("animalId"), animalId));
        }
        if (StringUtils.hasText(feedTypeId)) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("feedTypeId"), feedTypeId));
        }
        if (date != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("date"), date));
        }
        if (normalizedSearch != null) {
            String searchPattern = "%" + normalizedSearch + "%";
            specification = specification.and((root, query, criteriaBuilder) -> {
                var animalQuery = query.subquery(Integer.class);
                var animalRoot = animalQuery.from(AnimalEntity.class);
                animalQuery.select(criteriaBuilder.literal(1));
                animalQuery.where(
                        criteriaBuilder.equal(animalRoot.get("id"), root.get("animalId")),
                        criteriaBuilder.like(criteriaBuilder.lower(animalRoot.get("tag")), searchPattern));

                var feedTypeQuery = query.subquery(Integer.class);
                var feedTypeRoot = feedTypeQuery.from(FeedTypeEntity.class);
                feedTypeQuery.select(criteriaBuilder.literal(1));
                feedTypeQuery.where(
                        criteriaBuilder.equal(feedTypeRoot.get("id"), root.get("feedTypeId")),
                        criteriaBuilder.like(criteriaBuilder.lower(feedTypeRoot.get("name")), searchPattern));

                return criteriaBuilder.or(criteriaBuilder.exists(animalQuery), criteriaBuilder.exists(feedTypeQuery));
            });
        }

        return specification;
    }

    private FeedingEntity getFeedingIncludingInactive(String id, String farmId) {
        validateAccessibleFarmIfPresent(farmId);
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
            return farmAccessService != null ? farmAccessService.validateAccessibleFarm(farmId) : farmId;
        }

        AnimalEntity animalEntity = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));
        return farmAccessService != null
                ? farmAccessService.validateAccessibleFarm(animalEntity.getFarmId())
                : animalEntity.getFarmId();
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

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDate resolveCreationDate(CreateFeedingRequest request) {
        if (authenticationContextService.hasRole(WORKER_ROLE)) {
            return LocalDate.now();
        }

        return request != null ? request.getDate() : null;
    }

    private void validateAccessibleFarmIfPresent(String farmId) {
        if (farmAccessService != null) {
            farmAccessService.validateAccessibleFarmIfPresent(farmId);
        }
    }
}
