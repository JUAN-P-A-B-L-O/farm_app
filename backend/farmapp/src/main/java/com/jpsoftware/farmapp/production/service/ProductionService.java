package com.jpsoftware.farmapp.production.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.milkprice.service.MilkPriceService;
import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionProfitResponse;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.mapper.ProductionMapper;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.BusinessException;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.measurement.MeasurementUnit;
import com.jpsoftware.farmapp.shared.measurement.MeasurementUnitConverter;
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
public class ProductionService {
    private static final String WORKER_ROLE = "WORKER";

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;
    private final ProductionMapper productionMapper;
    private final AuthenticationContextService authenticationContextService;
    private final FarmAccessService farmAccessService;
    private final MilkPriceService milkPriceService;

    @Autowired
    public ProductionService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            UserRepository userRepository,
            ProductionMapper productionMapper,
            AuthenticationContextService authenticationContextService,
            FarmAccessService farmAccessService,
            MilkPriceService milkPriceService) {
        this.productionRepository = productionRepository;
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.userRepository = userRepository;
        this.productionMapper = productionMapper;
        this.authenticationContextService = authenticationContextService;
        this.farmAccessService = farmAccessService;
        this.milkPriceService = milkPriceService;
    }

    public ProductionService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            UserRepository userRepository,
            ProductionMapper productionMapper,
            AuthenticationContextService authenticationContextService) {
        this(
                productionRepository,
                feedingRepository,
                animalRepository,
                userRepository,
                productionMapper,
                authenticationContextService,
                null,
                null);
    }

    @Transactional
    public ProductionResponse create(CreateProductionRequest request, String farmId) {
        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
        LocalDate effectiveDate = resolveCreationDate(request);
        validateInput(request, createdBy, effectiveDate);
        String resolvedFarmId = resolveFarmId(request, farmId);
        validateRelations(request, createdBy, resolvedFarmId);

        ProductionEntity productionEntity = toEntity(request);
        productionEntity.setDate(effectiveDate);
        productionEntity.setQuantity(DecimalScaleUtils.normalize(productionEntity.getQuantity()));
        productionEntity.setCreatedBy(createdBy);
        productionEntity.setFarmId(resolvedFarmId);
        productionEntity.setStatus(ProductionEntity.STATUS_ACTIVE);
        ProductionEntity savedProduction = productionRepository.save(productionEntity);

        return toEnrichedResponse(savedProduction);
    }

    @Transactional
    public ProductionResponse create(CreateProductionRequest request) {
        return create(request, null);
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll(String search, String animalId, LocalDate date, String farmId) {
        return getAllProductions(search, animalId, date, farmId);
    }

    @Transactional(readOnly = true)
    public String exportAll(String search, String animalId, LocalDate date, String farmId) {
        return exportAll(search, animalId, date, farmId, null);
    }

    @Transactional(readOnly = true)
    public String exportAll(String search, String animalId, LocalDate date, String farmId, String measurementUnitParam) {
        MeasurementUnit measurementUnit = MeasurementUnit.fromProductionParam(measurementUnitParam, "measurementUnit");
        return CsvExportUtils.write(findAll(search, animalId, date, farmId), List.of(
                new CsvColumn<>("id", ProductionResponse::getId),
                new CsvColumn<>("animalId", ProductionResponse::getAnimalId),
                new CsvColumn<>("animalTag", production -> production.getAnimal() != null ? production.getAnimal().getTag() : null),
                new CsvColumn<>("date", ProductionResponse::getDate),
                new CsvColumn<>("quantity", production -> MeasurementUnitConverter.convertFromBase(
                        production.getQuantity(),
                        measurementUnit)),
                new CsvColumn<>("quantityUnit", row -> measurementUnit.getSymbol())));
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll(String animalId, LocalDate date) {
        return findAll(null, animalId, date, null);
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> getAllProductions(String search, String animalId, LocalDate date, String farmId) {
        List<ProductionEntity> productions = findProductions(search, animalId, date, farmId);
        Map<String, AnimalSummaryResponse> animalsById = loadAnimalSummariesById(productions);
        return productions.stream()
                .map(production -> productionMapper.toResponse(production, animalsById.get(production.getAnimalId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductionResponse> findAllPaginated(String search, String animalId, LocalDate date, String farmId, int page, int size) {
        return getAllProductionsPaginated(search, animalId, date, farmId, page, size);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductionResponse> findAllPaginated(String animalId, LocalDate date, int page, int size) {
        return findAllPaginated(null, animalId, date, null, page, size);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductionResponse> getAllProductionsPaginated(
            String search,
            String animalId,
            LocalDate date,
            String farmId,
            int page,
            int size) {
        Page<ProductionEntity> productions = findProductions(search, animalId, date, farmId, PageRequest.of(page, size));
        Map<String, AnimalSummaryResponse> animalsById = loadAnimalSummariesById(productions.getContent());
        Page<ProductionResponse> responses = productions.map(production ->
                productionMapper.toResponse(production, animalsById.get(production.getAnimalId())));
        return toPaginatedResponse(responses);
    }

    @Transactional(readOnly = true)
    public ProductionResponse findById(String id, String farmId) {
        return getProductionById(id, farmId);
    }

    @Transactional(readOnly = true)
    public ProductionResponse findById(String id) {
        return findById(id, null);
    }

    @Transactional(readOnly = true)
    public ProductionResponse getProductionById(String id, String farmId) {
        validateAccessibleFarmIfPresent(farmId);
        ProductionEntity productionEntity = (StringUtils.hasText(farmId)
                ? productionRepository.findByIdAndFarmIdAndStatus(validateId(id), farmId, ProductionEntity.STATUS_ACTIVE)
                : productionRepository.findById(validateId(id)))
                .orElseThrow(() -> new ResourceNotFoundException("Production not found"));

        return toEnrichedResponse(productionEntity);
    }

    @Transactional(readOnly = true)
    public ProductionSummaryResponse getSummaryByAnimal(String animalId, String farmId) {
        String validatedAnimalId = validateAnimalId(animalId);
        validateAnimal(validatedAnimalId, farmId);

        Double totalQuantity = productionRepository.sumQuantityByAnimalId(validatedAnimalId);
        return new ProductionSummaryResponse(validatedAnimalId, DecimalScaleUtils.zeroIfNull(totalQuantity));
    }

    @Transactional(readOnly = true)
    public ProductionSummaryResponse getSummaryByAnimal(String animalId) {
        return getSummaryByAnimal(animalId, null);
    }

    @Transactional(readOnly = true)
    public ProductionProfitResponse getProfitByAnimal(String animalId, String farmId, boolean includeAcquisitionCost) {
        String validatedAnimalId = validateAnimalId(animalId);
        AnimalEntity animal = validateAnimal(validatedAnimalId, farmId);

        Double totalProduction = DecimalScaleUtils.zeroIfNull(productionRepository.sumProductionByAnimalId(validatedAnimalId));
        Double totalFeedingCost = DecimalScaleUtils.zeroIfNull(feedingRepository.sumFeedingCostByAnimalId(validatedAnimalId));
        Double acquisitionCost = includeAcquisitionCost
                ? DecimalScaleUtils.zeroIfNull(animal.getAcquisitionCost())
                : 0.0;
        Double milkPrice = milkPriceService != null
                ? milkPriceService.resolveCurrentPriceValue(animal.getFarmId())
                : MilkPriceService.DEFAULT_MILK_PRICE;
        Double revenue = DecimalScaleUtils.multiply(totalProduction, milkPrice);
        Double totalCost = DecimalScaleUtils.normalize(totalFeedingCost + acquisitionCost);
        Double profit = DecimalScaleUtils.subtract(revenue, totalCost);

        return new ProductionProfitResponse(
                validatedAnimalId,
                totalProduction,
                totalCost,
                milkPrice,
                revenue,
                profit);
    }

    @Transactional(readOnly = true)
    public ProductionProfitResponse getProfitByAnimal(String animalId, String farmId) {
        return getProfitByAnimal(animalId, farmId, true);
    }

    @Transactional(readOnly = true)
    public ProductionProfitResponse getProfitByAnimal(String animalId) {
        return getProfitByAnimal(animalId, null, true);
    }

    @Transactional
    public ProductionResponse update(String id, UpdateProductionRequest request, String farmId) {
        return updateProduction(id, request, farmId);
    }

    @Transactional
    public ProductionResponse update(String id, UpdateProductionRequest request) {
        return update(id, request, null);
    }

    @Transactional
    public ProductionResponse updateProduction(String id, UpdateProductionRequest request, String farmId) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }

        ProductionEntity productionEntity = getProductionIncludingInactive(id, farmId);
        ensureProductionIsActive(productionEntity, "Inactive production cannot be updated");

        if (StringUtils.hasText(request.getAnimalId())) {
            validateAnimalIsActive(request.getAnimalId(), farmId);
            productionEntity.setAnimalId(request.getAnimalId());
        }
        if (request.getDate() != null) {
            if (request.getDate().isAfter(LocalDate.now())) {
                throw new BusinessException("Date cannot be in the future");
            }
            productionEntity.setDate(request.getDate());
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                throw new ValidationException("quantity must be greater than zero");
            }
            DecimalScaleUtils.requireMaxScale(request.getQuantity(), "quantity");
            productionEntity.setQuantity(DecimalScaleUtils.normalize(request.getQuantity()));
        }

        ProductionEntity savedProduction = productionRepository.save(productionEntity);
        return toEnrichedResponse(savedProduction);
    }

    @Transactional
    public void deleteProduction(String id, String farmId) {
        ProductionEntity productionEntity = getProductionIncludingInactive(id, farmId);
        if (ProductionEntity.STATUS_INACTIVE.equals(productionEntity.getStatus())) {
            return;
        }

        productionEntity.setStatus(ProductionEntity.STATUS_INACTIVE);
        productionRepository.save(productionEntity);
    }

    @Transactional
    public void deleteProduction(String id) {
        deleteProduction(id, null);
    }

    private ProductionResponse toEnrichedResponse(ProductionEntity productionEntity) {
        AnimalSummaryResponse animal = animalRepository.findById(productionEntity.getAnimalId())
                .map(this::toAnimalSummary)
                .orElse(null);
        return productionMapper.toResponse(productionEntity, animal);
    }

    private Map<String, AnimalSummaryResponse> loadAnimalSummariesById(Collection<ProductionEntity> productions) {
        Set<String> animalIds = productions.stream()
                .map(ProductionEntity::getAnimalId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        return animalRepository.findAllById(animalIds).stream()
                .collect(Collectors.toMap(AnimalEntity::getId, this::toAnimalSummary));
    }

    private AnimalSummaryResponse toAnimalSummary(AnimalEntity animalEntity) {
        return new AnimalSummaryResponse(animalEntity.getId(), animalEntity.getTag());
    }

    private void validateInput(CreateProductionRequest request, String createdBy, LocalDate effectiveDate) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getAnimalId())) {
            throw new ValidationException("animalId must not be blank");
        }
        if (effectiveDate == null) {
            throw new ValidationException("date must not be null");
        }
        if (effectiveDate.isAfter(LocalDate.now())) {
            throw new BusinessException("Date cannot be in the future");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("quantity must be greater than zero");
        }
        DecimalScaleUtils.requireMaxScale(request.getQuantity(), "quantity");
        if (!StringUtils.hasText(createdBy)) {
            throw new ValidationException("userId must not be blank");
        }
    }

    private void validateRelations(CreateProductionRequest request, String createdBy, String farmId) {
        validateAnimalIsActive(request.getAnimalId(), farmId);
        if (!userRepository.existsById(parseUserId(createdBy))) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private ProductionEntity toEntity(CreateProductionRequest request) {
        return productionMapper.toEntity(request);
    }

    private LocalDate resolveCreationDate(CreateProductionRequest request) {
        if (authenticationContextService.hasRole(WORKER_ROLE)) {
            return LocalDate.now();
        }

        return request != null ? request.getDate() : null;
    }

    private List<ProductionEntity> findProductions(String search, String animalId, LocalDate date, String farmId) {
        validateAccessibleFarmIfPresent(farmId);
        return productionRepository.findAll(buildProductionSpecification(search, animalId, date, farmId));
    }

    private Page<ProductionEntity> findProductions(
            String search,
            String animalId,
            LocalDate date,
            String farmId,
            org.springframework.data.domain.Pageable pageable) {
        validateAccessibleFarmIfPresent(farmId);
        return productionRepository.findAll(buildProductionSpecification(search, animalId, date, farmId), pageable);
    }

    private Specification<ProductionEntity> buildProductionSpecification(String search, String animalId, LocalDate date, String farmId) {
        String normalizedSearch = normalizeFilter(search);
        Specification<ProductionEntity> specification = Specification.where((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), ProductionEntity.STATUS_ACTIVE));

        if (StringUtils.hasText(farmId)) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("farmId"), farmId));
        }
        if (StringUtils.hasText(animalId)) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("animalId"), animalId));
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
                return criteriaBuilder.exists(animalQuery);
            });
        }

        return specification;
    }

    private ProductionEntity getProductionIncludingInactive(String id, String farmId) {
        validateAccessibleFarmIfPresent(farmId);
        return (StringUtils.hasText(farmId)
                ? productionRepository.findAnyByIdAndFarmId(validateId(id), farmId)
                : productionRepository.findAnyById(validateId(id)))
                .orElseThrow(() -> new ResourceNotFoundException("Production not found"));
    }

    private void ensureProductionIsActive(ProductionEntity productionEntity, String message) {
        if (ProductionEntity.STATUS_INACTIVE.equals(productionEntity.getStatus())) {
            throw new ConflictException(message);
        }
    }

    private PaginatedResponse<ProductionResponse> toPaginatedResponse(Page<ProductionResponse> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private String validateId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }
        return id;
    }

    private String validateAnimalId(String animalId) {
        if (!StringUtils.hasText(animalId)) {
            throw new ValidationException("animalId must not be blank");
        }
        return animalId;
    }

    private AnimalEntity validateAnimal(String animalId, String farmId) {
        AnimalEntity animal = StringUtils.hasText(farmId)
                ? animalRepository.findByIdAndFarmId(animalId, farmId).orElse(null)
                : animalRepository.findById(animalId).orElse(null);
        if (animal == null) {
            throw new ResourceNotFoundException("Animal not found");
        }
        return animal;
    }

    private AnimalEntity validateAnimalIsActive(String animalId, String farmId) {
        AnimalEntity animal = validateAnimal(animalId, farmId);
        if (!AnimalEntity.STATUS_ACTIVE.equals(animal.getStatus())) {
            throw new ValidationException("Animal must be ACTIVE for production operations");
        }
        return animal;
    }

    private String resolveFarmId(CreateProductionRequest request, String farmId) {
        if (StringUtils.hasText(farmId)) {
            return farmAccessService != null ? farmAccessService.validateAccessibleFarm(farmId) : farmId;
        }

        AnimalEntity animalEntity = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));
        return farmAccessService != null ? farmAccessService.validateAccessibleFarm(animalEntity.getFarmId()) : animalEntity.getFarmId();
    }

    private java.util.UUID parseUserId(String userId) {
        try {
            return java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("userId must be a valid UUID");
        }
    }

    private void validateAccessibleFarmIfPresent(String farmId) {
        if (farmAccessService != null) {
            farmAccessService.validateAccessibleFarmIfPresent(farmId);
        }
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

}
