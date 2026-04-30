package com.jpsoftware.farmapp.feed.service;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.mapper.FeedTypeMapper;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.shared.currency.CurrencyConversionUtils;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.shared.measurement.MeasurementUnit;
import com.jpsoftware.farmapp.shared.measurement.MeasurementUnitConverter;
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FeedTypeService {
    private static final int DEFAULT_MONEY_SCALE = 2;
    private static final int FEED_RATE_EXPORT_SCALE = 5;

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
        feedTypeEntity.setCostPerKg(DecimalScaleUtils.normalize(feedTypeEntity.getCostPerKg()));
        feedTypeEntity.setFarmId(validatedFarmId);
        FeedTypeEntity savedFeedType = feedTypeRepository.save(feedTypeEntity);

        return feedTypeMapper.toResponse(savedFeedType);
    }

    @Transactional(readOnly = true)
    public List<FeedTypeResponse> findAll(String farmId, String search) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        List<FeedTypeEntity> feedTypes = feedTypeRepository.findAll(
                buildFeedTypeSpecification(farmId, search),
                Sort.by(Sort.Direction.ASC, "name"));

        return feedTypes.stream()
                .map(feedTypeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedTypeResponse> findAll(String farmId) {
        return findAll(farmId, null);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedTypeResponse> findAllPaginated(String farmId, String search, int page, int size) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        Page<FeedTypeEntity> feedTypes = feedTypeRepository.findAll(
                buildFeedTypeSpecification(farmId, search),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        Page<FeedTypeResponse> responses = feedTypes.map(feedTypeMapper::toResponse);
        return new PaginatedResponse<>(
                responses.getContent(),
                responses.getNumber(),
                responses.getSize(),
                responses.getTotalElements(),
                responses.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<FeedTypeResponse> findAllPaginated(String farmId, int page, int size) {
        return findAllPaginated(farmId, null, page, size);
    }

    @Transactional(readOnly = true)
    public String exportAll(String farmId, String search, String currency) {
        return exportAll(farmId, search, currency, null);
    }

    @Transactional(readOnly = true)
    public String exportAll(String farmId, String search, String currency, String measurementUnitParam) {
        MeasurementUnit measurementUnit = MeasurementUnit.fromFeedingParam(measurementUnitParam, "measurementUnit");
        int monetaryScale = measurementUnit == MeasurementUnit.GRAM ? FEED_RATE_EXPORT_SCALE : DEFAULT_MONEY_SCALE;
        List<FeedTypeResponse> rows = findAll(farmId, search).stream()
                .map(feedType -> new FeedTypeResponse(
                        feedType.getId(),
                        feedType.getName(),
                        CurrencyConversionUtils.convertMonetaryValue(
                                MeasurementUnitConverter.convertRateFromBase(
                                        feedType.getCostPerKg(),
                                        measurementUnit,
                                        monetaryScale),
                                currency,
                                monetaryScale),
                        feedType.getActive()))
                .toList();

        if (!StringUtils.hasText(measurementUnitParam)) {
            return CsvExportUtils.write(rows, List.of(
                new CsvColumn<>("id", FeedTypeResponse::getId),
                new CsvColumn<>("name", FeedTypeResponse::getName),
                new CsvColumn<>("costPerKg", FeedTypeResponse::getCostPerKg),
                new CsvColumn<>("active", FeedTypeResponse::getActive)));
        }

        return CsvExportUtils.write(rows, List.of(
                new CsvColumn<>("id", FeedTypeResponse::getId),
                new CsvColumn<>("name", FeedTypeResponse::getName),
                new CsvColumn<>("costPerUnit", FeedTypeResponse::getCostPerKg),
                new CsvColumn<>("costUnit", row -> measurementUnit.getSymbol()),
                new CsvColumn<>("active", FeedTypeResponse::getActive)));
    }

    @Transactional(readOnly = true)
    public String exportAll(String farmId) {
        return exportAll(farmId, null, null);
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
        feedTypeEntity.setCostPerKg(DecimalScaleUtils.normalize(request.getCostPerKg()));

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
        DecimalScaleUtils.requireMaxScale(request.getCostPerKg(), "costPerKg");
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

    private Specification<FeedTypeEntity> buildFeedTypeSpecification(String farmId, String search) {
        String normalizedSearch = normalizeFilter(search);
        Specification<FeedTypeEntity> specification = Specification.where((root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("active")));

        if (StringUtils.hasText(farmId)) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("farmId"), farmId));
        }
        if (normalizedSearch != null) {
            String searchPattern = "%" + normalizedSearch + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern));
        }

        return specification;
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
