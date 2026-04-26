package com.jpsoftware.farmapp.dashboard.service;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.milkprice.service.MilkPriceService;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.shared.currency.CurrencyConversionUtils;
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DashboardService {

    private static final Set<String> SUPPORTED_ANIMAL_STATUSES = Set.of(
            AnimalEntity.STATUS_ACTIVE,
            AnimalEntity.STATUS_INACTIVE,
            AnimalEntity.STATUS_SOLD,
            AnimalEntity.STATUS_DEAD);

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final FeedTypeRepository feedTypeRepository;
    private final FarmAccessService farmAccessService;
    private final MilkPriceService milkPriceService;

    @Autowired
    public DashboardService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FeedTypeRepository feedTypeRepository,
            FarmAccessService farmAccessService,
            MilkPriceService milkPriceService) {
        this.productionRepository = productionRepository;
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.feedTypeRepository = feedTypeRepository;
        this.farmAccessService = farmAccessService;
        this.milkPriceService = milkPriceService;
    }

    public DashboardService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FeedTypeRepository feedTypeRepository,
            FarmAccessService farmAccessService) {
        this(productionRepository, feedingRepository, animalRepository, feedTypeRepository, farmAccessService, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String farmId, boolean includeAcquisitionCost) {
        return getDashboard(farmId, null, null, null, null, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String farmId, boolean includeAcquisitionCost, String currency) {
        return getDashboard(farmId, null, null, null, null, includeAcquisitionCost, currency);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String status,
            boolean includeAcquisitionCost) {
        return getDashboard(farmId, startDate, endDate, animalId, status, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String status,
            boolean includeAcquisitionCost,
            String currency) {
        return getDashboardByAnimals(
                farmId,
                startDate,
                endDate,
                toAnimalFilterList(animalId),
                status,
                includeAcquisitionCost,
                currency);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardByAnimals(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            Collection<String> animalIds,
            String status,
            boolean includeAcquisitionCost) {
        return getDashboardByAnimals(farmId, startDate, endDate, animalIds, status, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardByAnimals(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            Collection<String> animalIds,
            String status,
            boolean includeAcquisitionCost,
            String currency) {
        List<String> normalizedAnimalIds = normalizeAnimalIds(animalIds);
        validateFilters(farmId, startDate, endDate, normalizedAnimalIds, status);

        List<AnimalEntity> animals = filterAnimals(normalizedAnimalIds, farmId, status);
        Set<String> scopedAnimalIds = animals.stream()
                .map(AnimalEntity::getId)
                .collect(Collectors.toSet());
        List<ProductionEntity> productions = filterProductions(startDate, endDate, farmId, scopedAnimalIds);
        List<FeedingEntity> feedings = filterFeedings(startDate, endDate, farmId, scopedAnimalIds);
        Map<String, Double> feedCostsById = loadFeedCostsById(feedings);

        Double totalProduction = productions.stream()
                .map(ProductionEntity::getQuantity)
                .map(DecimalScaleUtils::zeroIfNull)
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
        Double totalFeedingCost = feedings.stream()
                .map(feeding -> DecimalScaleUtils.multiply(
                        DecimalScaleUtils.zeroIfNull(feedCostsById.get(feeding.getFeedTypeId())),
                        DecimalScaleUtils.zeroIfNull(feeding.getQuantity())))
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
        Double totalAcquisitionCost = includeAcquisitionCost
                ? DecimalScaleUtils.zeroIfNull(sumAcquisitionCost(animals))
                : 0.0;
        Double totalSaleRevenue = DecimalScaleUtils.zeroIfNull(sumSaleRevenue(animals, startDate, endDate));
        Double totalRevenue = DecimalScaleUtils.normalize(sumMilkRevenue(productions) + totalSaleRevenue);
        Double totalProfit = DecimalScaleUtils.subtract(
                totalRevenue,
                DecimalScaleUtils.normalize(totalFeedingCost + totalAcquisitionCost));
        Long animalCount = (long) animals.size();

        return new DashboardResponse(
                totalProduction,
                CurrencyConversionUtils.convertMonetaryValue(totalFeedingCost, currency),
                CurrencyConversionUtils.convertMonetaryValue(totalRevenue, currency),
                CurrencyConversionUtils.convertMonetaryValue(totalProfit, currency),
                animalCount);
    }

    @Transactional(readOnly = true)
    public String exportDashboard(String farmId, boolean includeAcquisitionCost) {
        return exportDashboard(farmId, null, null, null, null, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public String exportDashboard(String farmId, boolean includeAcquisitionCost, String currency) {
        return exportDashboard(farmId, null, null, null, null, includeAcquisitionCost, currency);
    }

    @Transactional(readOnly = true)
    public String exportDashboard(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String status,
            boolean includeAcquisitionCost) {
        return exportDashboard(farmId, startDate, endDate, animalId, status, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public String exportDashboardByAnimals(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            Collection<String> animalIds,
            String status,
            boolean includeAcquisitionCost) {
        return exportDashboardByAnimals(farmId, startDate, endDate, animalIds, status, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public String exportDashboard(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String status,
            boolean includeAcquisitionCost,
            String currency) {
        DashboardResponse dashboard = getDashboard(
                farmId,
                startDate,
                endDate,
                animalId,
                status,
                includeAcquisitionCost,
                currency);
        return CsvExportUtils.write(List.of(dashboard), List.of(
                new CsvColumn<>("totalProduction", DashboardResponse::getTotalProduction),
                new CsvColumn<>("totalFeedingCost", DashboardResponse::getTotalFeedingCost),
                new CsvColumn<>("totalRevenue", DashboardResponse::getTotalRevenue),
                new CsvColumn<>("totalProfit", DashboardResponse::getTotalProfit),
                new CsvColumn<>("animalCount", DashboardResponse::getAnimalCount)));
    }

    @Transactional(readOnly = true)
    public String exportDashboardByAnimals(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            Collection<String> animalIds,
            String status,
            boolean includeAcquisitionCost,
            String currency) {
        DashboardResponse dashboard = getDashboardByAnimals(
                farmId,
                startDate,
                endDate,
                animalIds,
                status,
                includeAcquisitionCost,
                currency);
        return CsvExportUtils.write(List.of(dashboard), List.of(
                new CsvColumn<>("totalProduction", DashboardResponse::getTotalProduction),
                new CsvColumn<>("totalFeedingCost", DashboardResponse::getTotalFeedingCost),
                new CsvColumn<>("totalRevenue", DashboardResponse::getTotalRevenue),
                new CsvColumn<>("totalProfit", DashboardResponse::getTotalProfit),
                new CsvColumn<>("animalCount", DashboardResponse::getAnimalCount)));
    }

    private void validateFilters(
            String farmId,
            LocalDate startDate,
            LocalDate endDate,
            Collection<String> animalIds,
            String status) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("startDate must be before or equal to endDate");
        }

        if (farmAccessService != null) {
            farmAccessService.validateAccessibleFarmIfPresent(farmId);
        }

        for (String animalId : animalIds) {
            if (!animalRepository.existsById(animalId)) {
                throw new ResourceNotFoundException("Animal not found");
            }

            if (StringUtils.hasText(farmId) && !animalRepository.existsByIdAndFarmId(animalId, farmId)) {
                throw new ResourceNotFoundException("Animal not found");
            }
        }

        if (StringUtils.hasText(status) && !SUPPORTED_ANIMAL_STATUSES.contains(status)) {
            throw new ValidationException("status must be ACTIVE, INACTIVE, SOLD, or DEAD");
        }
    }

    private List<AnimalEntity> filterAnimals(Collection<String> animalIds, String farmId, String status) {
        List<AnimalEntity> animals;
        if (!animalIds.isEmpty()) {
            LinkedHashSet<String> requestedAnimalIds = new LinkedHashSet<>(animalIds);
            animals = animalRepository.findAllById(requestedAnimalIds).stream()
                    .filter(animal -> !StringUtils.hasText(farmId) || Objects.equals(animal.getFarmId(), farmId))
                    .toList();
        } else if (StringUtils.hasText(farmId)) {
            animals = animalRepository.findByFarmId(farmId);
        } else {
            animals = animalRepository.findAll();
        }

        return animals.stream()
                .filter(animal -> matchesStatus(animal.getStatus(), status))
                .toList();
    }

    private List<ProductionEntity> filterProductions(
            LocalDate startDate,
            LocalDate endDate,
            String farmId,
            Set<String> animalIds) {
        if (animalIds.isEmpty()) {
            return List.of();
        }

        List<ProductionEntity> productions = StringUtils.hasText(farmId)
                ? productionRepository.findByFarmIdAndStatus(farmId, ProductionEntity.STATUS_ACTIVE)
                : productionRepository.findAll();

        return productions.stream()
                .filter(production -> animalIds.contains(production.getAnimalId()))
                .filter(production -> matchesDateRange(production.getDate(), startDate, endDate))
                .toList();
    }

    private List<FeedingEntity> filterFeedings(
            LocalDate startDate,
            LocalDate endDate,
            String farmId,
            Set<String> animalIds) {
        if (animalIds.isEmpty()) {
            return List.of();
        }

        List<FeedingEntity> feedings = StringUtils.hasText(farmId)
                ? feedingRepository.findByFarmIdAndStatus(farmId, FeedingEntity.STATUS_ACTIVE)
                : feedingRepository.findAll();

        return feedings.stream()
                .filter(feeding -> animalIds.contains(feeding.getAnimalId()))
                .filter(feeding -> matchesDateRange(feeding.getDate(), startDate, endDate))
                .toList();
    }

    private Map<String, Double> loadFeedCostsById(Collection<FeedingEntity> feedings) {
        Set<String> feedTypeIds = feedings.stream()
                .map(FeedingEntity::getFeedTypeId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        if (feedTypeIds.isEmpty()) {
            return Map.of();
        }

        return feedTypeRepository.findAllById(feedTypeIds).stream()
                .collect(Collectors.toMap(FeedTypeEntity::getId, feedType -> DecimalScaleUtils.zeroIfNull(feedType.getCostPerKg())));
    }

    private Double sumMilkRevenue(List<ProductionEntity> productions) {
        return productions.stream()
                .map(production -> DecimalScaleUtils.multiply(
                        DecimalScaleUtils.zeroIfNull(production.getQuantity()),
                        milkPriceService != null
                                ? milkPriceService.resolveCurrentPriceValue(production.getFarmId())
                                : MilkPriceService.DEFAULT_MILK_PRICE))
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
    }

    private Double sumAcquisitionCost(List<AnimalEntity> animals) {
        return animals.stream()
                .map(AnimalEntity::getAcquisitionCost)
                .map(DecimalScaleUtils::zeroIfNull)
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
    }

    private Double sumSaleRevenue(List<AnimalEntity> animals, LocalDate startDate, LocalDate endDate) {
        return animals.stream()
                .filter(animal -> animal.getSaleDate() != null)
                .filter(animal -> matchesDateRange(animal.getSaleDate(), startDate, endDate))
                .map(AnimalEntity::getSalePrice)
                .map(DecimalScaleUtils::zeroIfNull)
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
    }

    private boolean matchesDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null) {
            return false;
        }

        boolean matchesStart = startDate == null || !date.isBefore(startDate);
        boolean matchesEnd = endDate == null || !date.isAfter(endDate);
        return matchesStart && matchesEnd;
    }

    private boolean matchesStatus(String animalStatus, String requestedStatus) {
        return !StringUtils.hasText(requestedStatus) || Objects.equals(animalStatus, requestedStatus);
    }

    private List<String> toAnimalFilterList(String animalId) {
        if (!StringUtils.hasText(animalId)) {
            return List.of();
        }
        return List.of(animalId.trim());
    }

    private List<String> normalizeAnimalIds(Collection<String> animalIds) {
        if (animalIds == null || animalIds.isEmpty()) {
            return List.of();
        }

        return animalIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }
}
