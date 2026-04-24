package com.jpsoftware.farmapp.milkprice.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.milkprice.dto.CreateMilkPriceRequest;
import com.jpsoftware.farmapp.milkprice.dto.MilkPriceResponse;
import com.jpsoftware.farmapp.milkprice.entity.MilkPriceEntity;
import com.jpsoftware.farmapp.milkprice.repository.MilkPriceRepository;
import com.jpsoftware.farmapp.shared.currency.CurrencyConversionUtils;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MilkPriceService {

    public static final Double DEFAULT_MILK_PRICE = 2.0;

    private final MilkPriceRepository milkPriceRepository;
    private final FarmRepository farmRepository;
    private final FarmAccessService farmAccessService;
    private final AuthenticationContextService authenticationContextService;

    public MilkPriceService(
            MilkPriceRepository milkPriceRepository,
            FarmRepository farmRepository,
            FarmAccessService farmAccessService,
            AuthenticationContextService authenticationContextService) {
        this.milkPriceRepository = milkPriceRepository;
        this.farmRepository = farmRepository;
        this.farmAccessService = farmAccessService;
        this.authenticationContextService = authenticationContextService;
    }

    @Transactional
    public MilkPriceResponse create(CreateMilkPriceRequest request, String farmId) {
        String resolvedFarmId = validateAndResolveFarmId(farmId);
        validateRequest(request);

        String createdBy = authenticationContextService.resolveUserId(null);
        if (!StringUtils.hasText(createdBy)) {
            throw new ValidationException("Authenticated user is required to register milk price");
        }

        MilkPriceEntity entity = new MilkPriceEntity();
        entity.setFarmId(resolvedFarmId);
        entity.setPrice(DecimalScaleUtils.normalize(request.getPrice()));
        entity.setEffectiveDate(request.getEffectiveDate());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);

        return toResponse(milkPriceRepository.save(entity), false);
    }

    @Transactional(readOnly = true)
    public MilkPriceResponse getCurrent(String farmId) {
        String resolvedFarmId = validateAndResolveFarmId(farmId);
        return findCurrentEntity(resolvedFarmId)
                .map(entity -> toResponse(entity, false))
                .orElseGet(() -> new MilkPriceResponse(
                        null,
                        resolvedFarmId,
                        DEFAULT_MILK_PRICE,
                        null,
                        null,
                        null,
                        true));
    }

    @Transactional(readOnly = true)
    public List<MilkPriceResponse> getHistory(String farmId) {
        String resolvedFarmId = validateAndResolveFarmId(farmId);
        return milkPriceRepository.findByFarmIdOrderByEffectiveDateDescCreatedAtDesc(resolvedFarmId).stream()
                .map(entity -> toResponse(entity, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public String exportHistory(String farmId) {
        return exportHistory(farmId, null);
    }

    @Transactional(readOnly = true)
    public String exportHistory(String farmId, String currency) {
        return CsvExportUtils.write(getHistory(farmId).stream()
                .map(price -> new MilkPriceResponse(
                        price.getId(),
                        price.getFarmId(),
                        CurrencyConversionUtils.convertMonetaryValue(price.getPrice(), currency),
                        price.getEffectiveDate(),
                        price.getCreatedAt(),
                        price.getCreatedBy(),
                        price.isFallbackDefault()))
                .toList(), List.of(
                new CsvColumn<>("id", MilkPriceResponse::getId),
                new CsvColumn<>("farmId", MilkPriceResponse::getFarmId),
                new CsvColumn<>("price", MilkPriceResponse::getPrice),
                new CsvColumn<>("effectiveDate", MilkPriceResponse::getEffectiveDate),
                new CsvColumn<>("createdAt", MilkPriceResponse::getCreatedAt),
                new CsvColumn<>("createdBy", MilkPriceResponse::getCreatedBy),
                new CsvColumn<>("fallbackDefault", MilkPriceResponse::isFallbackDefault)));
    }

    @Transactional(readOnly = true)
    public Double resolveCurrentPriceValue(String farmId) {
        if (!StringUtils.hasText(farmId)) {
            return DEFAULT_MILK_PRICE;
        }
        return getCurrent(farmId).getPrice();
    }

    private java.util.Optional<MilkPriceEntity> findCurrentEntity(String farmId) {
        return milkPriceRepository.findTopByFarmIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                farmId,
                LocalDate.now());
    }

    private String validateAndResolveFarmId(String farmId) {
        if (!StringUtils.hasText(farmId)) {
            throw new ValidationException("farmId must not be blank");
        }

        farmAccessService.validateAccessibleFarmIfPresent(farmId);
        if (!farmRepository.existsById(farmId)) {
            throw new ResourceNotFoundException("Farm not found");
        }
        return farmId;
    }

    private void validateRequest(CreateMilkPriceRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new ValidationException("price must be greater than zero");
        }
        DecimalScaleUtils.requireMaxScale(request.getPrice(), "price");
        if (request.getEffectiveDate() == null) {
            throw new ValidationException("effectiveDate must not be null");
        }
    }

    private MilkPriceResponse toResponse(MilkPriceEntity entity, boolean fallbackDefault) {
        return new MilkPriceResponse(
                entity.getId(),
                entity.getFarmId(),
                DecimalScaleUtils.normalize(entity.getPrice()),
                entity.getEffectiveDate(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                fallbackDefault);
    }
}
