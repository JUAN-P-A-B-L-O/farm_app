package com.jpsoftware.farmapp.production.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
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
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
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
public class ProductionService {

    private static final Double MILK_PRICE = 2.0;

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;
    private final ProductionMapper productionMapper;
    private final AuthenticationContextService authenticationContextService;

    public ProductionService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            UserRepository userRepository,
            ProductionMapper productionMapper,
            AuthenticationContextService authenticationContextService) {
        this.productionRepository = productionRepository;
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.userRepository = userRepository;
        this.productionMapper = productionMapper;
        this.authenticationContextService = authenticationContextService;
    }

    @Transactional
    public ProductionResponse create(CreateProductionRequest request) {
        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
        validateInput(request, createdBy);

        ProductionEntity productionEntity = toEntity(request);
        productionEntity.setCreatedBy(createdBy);
        ProductionEntity savedProduction = productionRepository.save(productionEntity);

        return toEnrichedResponse(savedProduction);
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll(String animalId, LocalDate date) {
        List<ProductionEntity> productions = findProductions(animalId, date);
        Map<String, AnimalSummaryResponse> animalsById = loadAnimalSummariesById(productions);
        return productions.stream()
                .map(production -> productionMapper.toResponse(production, animalsById.get(production.getAnimalId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductionResponse> findAllPaginated(String animalId, LocalDate date, int page, int size) {
        Page<ProductionEntity> productions = findProductions(animalId, date, PageRequest.of(page, size));
        Map<String, AnimalSummaryResponse> animalsById = loadAnimalSummariesById(productions.getContent());
        Page<ProductionResponse> responses = productions.map(production ->
                productionMapper.toResponse(production, animalsById.get(production.getAnimalId())));
        return toPaginatedResponse(responses);
    }

    @Transactional(readOnly = true)
    public ProductionResponse findById(String id) {
        ProductionEntity productionEntity = productionRepository.findById(validateId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Production not found"));

        return toEnrichedResponse(productionEntity);
    }

    @Transactional(readOnly = true)
    public ProductionSummaryResponse getSummaryByAnimal(String animalId) {
        String validatedAnimalId = validateAnimalId(animalId);

        validateAnimalExists(validatedAnimalId);

        Double totalQuantity = productionRepository.sumQuantityByAnimalId(validatedAnimalId);
        return new ProductionSummaryResponse(validatedAnimalId, totalQuantity != null ? totalQuantity : 0.0);
    }

    @Transactional(readOnly = true)
    public ProductionProfitResponse getProfitByAnimal(String animalId) {
        String validatedAnimalId = validateAnimalId(animalId);
        validateAnimalExists(validatedAnimalId);

        Double totalProduction = defaultToZero(productionRepository.sumProductionByAnimalId(validatedAnimalId));
        Double totalFeedingCost = defaultToZero(feedingRepository.sumFeedingCostByAnimalId(validatedAnimalId));
        Double revenue = totalProduction * MILK_PRICE;
        Double profit = revenue - totalFeedingCost;

        return new ProductionProfitResponse(
                validatedAnimalId,
                totalProduction,
                totalFeedingCost,
                MILK_PRICE,
                revenue,
                profit);
    }

    @Transactional
    public ProductionResponse update(String id, UpdateProductionRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }

        ProductionEntity productionEntity = productionRepository.findById(validateId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Production not found"));

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
            productionEntity.setQuantity(request.getQuantity());
        }

        ProductionEntity savedProduction = productionRepository.save(productionEntity);
        return toEnrichedResponse(savedProduction);
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

    private void validateInput(CreateProductionRequest request, String createdBy) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getAnimalId())) {
            throw new ValidationException("animalId must not be blank");
        }
        if (request.getDate() == null) {
            throw new ValidationException("date must not be null");
        }
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Date cannot be in the future");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("quantity must be greater than zero");
        }
        if (!StringUtils.hasText(createdBy)) {
            throw new ValidationException("userId must not be blank");
        }
        if (!animalRepository.existsById(request.getAnimalId())) {
            throw new ResourceNotFoundException("Animal not found");
        }
        if (!userRepository.existsById(parseUserId(createdBy))) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private ProductionEntity toEntity(CreateProductionRequest request) {
        return productionMapper.toEntity(request);
    }

    private List<ProductionEntity> findProductions(String animalId, LocalDate date) {
        if (StringUtils.hasText(animalId) && date != null) {
            return productionRepository.findByAnimalIdAndDate(animalId, date);
        }
        if (StringUtils.hasText(animalId)) {
            return productionRepository.findByAnimalId(animalId);
        }
        if (date != null) {
            return productionRepository.findByDate(date);
        }
        return productionRepository.findAll();
    }

    private Page<ProductionEntity> findProductions(String animalId, LocalDate date, org.springframework.data.domain.Pageable pageable) {
        if (StringUtils.hasText(animalId) && date != null) {
            return productionRepository.findByAnimalIdAndDate(animalId, date, pageable);
        }
        if (StringUtils.hasText(animalId)) {
            return productionRepository.findByAnimalId(animalId, pageable);
        }
        if (date != null) {
            return productionRepository.findByDate(date, pageable);
        }
        return productionRepository.findAll(pageable);
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

    private void validateAnimalExists(String animalId) {
        if (!animalRepository.existsById(animalId)) {
            throw new ResourceNotFoundException("Animal not found");
        }
    }

    private java.util.UUID parseUserId(String userId) {
        try {
            return java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("userId must be a valid UUID");
        }
    }

    private Double defaultToZero(Double value) {
        return value != null ? value : 0.0;
    }
}
