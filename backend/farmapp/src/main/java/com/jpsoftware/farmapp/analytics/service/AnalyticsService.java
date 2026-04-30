package com.jpsoftware.farmapp.analytics.service;

import com.jpsoftware.farmapp.analytics.dto.AnalyticsAnimalProductionPointResponse;
import com.jpsoftware.farmapp.analytics.dto.AnalyticsGroupBy;
import com.jpsoftware.farmapp.analytics.dto.AnalyticsProfitPointResponse;
import com.jpsoftware.farmapp.analytics.dto.AnalyticsTimeSeriesPointResponse;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.milkprice.service.MilkPriceService;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.currency.CurrencyConversionUtils;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.shared.measurement.MeasurementUnit;
import com.jpsoftware.farmapp.shared.measurement.MeasurementUnitConverter;
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
public class AnalyticsService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final FeedTypeRepository feedTypeRepository;
    private final FarmAccessService farmAccessService;
    private final MilkPriceService milkPriceService;

    @Autowired
    public AnalyticsService(
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

    public AnalyticsService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FeedTypeRepository feedTypeRepository,
            FarmAccessService farmAccessService) {
        this(
                productionRepository,
                feedingRepository,
                animalRepository,
                feedTypeRepository,
                farmAccessService,
                null);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsTimeSeriesPointResponse> getProductionSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId) {
        AnalyticsGroupBy groupBy = validateFilters(startDate, endDate, animalId, groupByParam, farmId);
        List<ProductionEntity> productions = filterProductions(startDate, endDate, animalId, farmId);

        return aggregateSeries(
                productions,
                groupBy,
                ProductionEntity::getDate,
                ProductionEntity::getQuantity);
    }

    @Transactional(readOnly = true)
    public String exportProductionSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId) {
        return exportProductionSeries(startDate, endDate, animalId, groupByParam, farmId, null);
    }

    @Transactional(readOnly = true)
    public String exportProductionSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            String productionUnitParam) {
        MeasurementUnit productionUnit = MeasurementUnit.fromProductionParam(productionUnitParam, "productionUnit");
        return CsvExportUtils.write(getProductionSeries(startDate, endDate, animalId, groupByParam, farmId), List.of(
                new CsvColumn<>("period", AnalyticsTimeSeriesPointResponse::getPeriod),
                new CsvColumn<>("value", point -> MeasurementUnitConverter.convertFromBase(point.getValue(), productionUnit)),
                new CsvColumn<>("valueUnit", row -> productionUnit.getSymbol())));
    }

    @Transactional(readOnly = true)
    public List<AnalyticsTimeSeriesPointResponse> getFeedingCostSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId) {
        return getFeedingCostSeries(startDate, endDate, animalId, groupByParam, farmId, null);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsTimeSeriesPointResponse> getFeedingCostSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            String currency) {
        AnalyticsGroupBy groupBy = validateFilters(startDate, endDate, animalId, groupByParam, farmId);
        List<FeedingEntity> feedings = filterFeedings(startDate, endDate, animalId, farmId);
        Map<String, Double> feedCostsById = loadFeedCostsById(feedings);

        return convertSeriesValues(
                aggregateSeries(
                feedings,
                groupBy,
                FeedingEntity::getDate,
                feeding -> DecimalScaleUtils.multiply(
                        DecimalScaleUtils.zeroIfNull(feedCostsById.get(feeding.getFeedTypeId())),
                        DecimalScaleUtils.zeroIfNull(feeding.getQuantity()))),
                currency);
    }

    @Transactional(readOnly = true)
    public String exportFeedingCostSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId) {
        return exportFeedingCostSeries(startDate, endDate, animalId, groupByParam, farmId, null);
    }

    @Transactional(readOnly = true)
    public String exportFeedingCostSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            String currency) {
        return CsvExportUtils.write(getFeedingCostSeries(startDate, endDate, animalId, groupByParam, farmId, currency), List.of(
                new CsvColumn<>("period", AnalyticsTimeSeriesPointResponse::getPeriod),
                new CsvColumn<>("value", AnalyticsTimeSeriesPointResponse::getValue)));
    }

    @Transactional(readOnly = true)
    public List<AnalyticsProfitPointResponse> getProfitSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            boolean includeAcquisitionCost) {
        return getProfitSeries(startDate, endDate, animalId, groupByParam, farmId, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsProfitPointResponse> getProfitSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            boolean includeAcquisitionCost,
            String currency) {
        AnalyticsGroupBy groupBy = validateFilters(startDate, endDate, animalId, groupByParam, farmId);
        List<ProductionEntity> productions = filterProductions(startDate, endDate, animalId, farmId);
        List<FeedingEntity> feedings = filterFeedings(startDate, endDate, animalId, farmId);
        Map<String, Double> productionByPeriod = aggregateValues(
                productions,
                groupBy,
                ProductionEntity::getDate,
                ProductionEntity::getQuantity);
        Map<String, Double> milkRevenueByPeriod = aggregateValues(
                productions,
                groupBy,
                ProductionEntity::getDate,
                production -> DecimalScaleUtils.multiply(
                        DecimalScaleUtils.zeroIfNull(production.getQuantity()),
                        milkPriceService != null
                                ? milkPriceService.resolveCurrentPriceValue(production.getFarmId())
                                : MilkPriceService.DEFAULT_MILK_PRICE));
        Map<String, Double> saleRevenueByPeriod = aggregateSaleRevenue(startDate, endDate, animalId, farmId, groupBy);
        Map<String, Double> feedCostsById = loadFeedCostsById(feedings);
        Map<String, Double> feedingCostByPeriod = aggregateValues(
                feedings,
                groupBy,
                FeedingEntity::getDate,
                feeding -> DecimalScaleUtils.multiply(
                        DecimalScaleUtils.zeroIfNull(feedCostsById.get(feeding.getFeedTypeId())),
                        DecimalScaleUtils.zeroIfNull(feeding.getQuantity())));

        Set<String> periods = new java.util.TreeSet<>();
        periods.addAll(productionByPeriod.keySet());
        periods.addAll(milkRevenueByPeriod.keySet());
        periods.addAll(saleRevenueByPeriod.keySet());
        periods.addAll(feedingCostByPeriod.keySet());
        String acquisitionPeriod = periods.stream().findFirst().orElse(null);
        Double acquisitionCost = includeAcquisitionCost
                ? DecimalScaleUtils.zeroIfNull(sumAcquisitionCost(animalId, farmId))
                : 0.0;

        return periods.stream()
                .map(period -> {
                    Double production = DecimalScaleUtils.zeroIfNull(productionByPeriod.get(period));
                    Double milkRevenue = DecimalScaleUtils.zeroIfNull(milkRevenueByPeriod.get(period));
                    Double saleRevenue = DecimalScaleUtils.zeroIfNull(saleRevenueByPeriod.get(period));
                    Double feedingCost = DecimalScaleUtils.zeroIfNull(feedingCostByPeriod.get(period));
                    if (includeAcquisitionCost && period.equals(acquisitionPeriod)) {
                        feedingCost = DecimalScaleUtils.normalize(feedingCost + acquisitionCost);
                    }
                    Double revenue = DecimalScaleUtils.normalize(milkRevenue + saleRevenue);
                    Double profit = DecimalScaleUtils.subtract(revenue, feedingCost);
                    return new AnalyticsProfitPointResponse(
                            period,
                            production,
                            CurrencyConversionUtils.convertMonetaryValue(feedingCost, currency),
                            CurrencyConversionUtils.convertMonetaryValue(revenue, currency),
                            CurrencyConversionUtils.convertMonetaryValue(profit, currency));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public String exportProfitSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            boolean includeAcquisitionCost) {
        return exportProfitSeries(startDate, endDate, animalId, groupByParam, farmId, includeAcquisitionCost, null);
    }

    @Transactional(readOnly = true)
    public String exportProfitSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            boolean includeAcquisitionCost,
            String currency) {
        return exportProfitSeries(
                startDate,
                endDate,
                animalId,
                groupByParam,
                farmId,
                includeAcquisitionCost,
                currency,
                null);
    }

    @Transactional(readOnly = true)
    public String exportProfitSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId,
            boolean includeAcquisitionCost,
            String currency,
            String productionUnitParam) {
        MeasurementUnit productionUnit = MeasurementUnit.fromProductionParam(productionUnitParam, "productionUnit");
        return CsvExportUtils.write(
                getProfitSeries(startDate, endDate, animalId, groupByParam, farmId, includeAcquisitionCost, currency),
                List.of(
                        new CsvColumn<>("period", AnalyticsProfitPointResponse::getPeriod),
                        new CsvColumn<>("production", point -> MeasurementUnitConverter.convertFromBase(
                                point.getProduction(),
                                productionUnit)),
                        new CsvColumn<>("productionUnit", row -> productionUnit.getSymbol()),
                        new CsvColumn<>("feedingCost", AnalyticsProfitPointResponse::getFeedingCost),
                        new CsvColumn<>("revenue", AnalyticsProfitPointResponse::getRevenue),
                        new CsvColumn<>("profit", AnalyticsProfitPointResponse::getProfit)));
    }

    @Transactional(readOnly = true)
    public List<AnalyticsAnimalProductionPointResponse> getProductionByAnimal(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String farmId) {
        validateFilters(startDate, endDate, animalId, AnalyticsGroupBy.DAY.name(), farmId);
        List<ProductionEntity> productions = filterProductions(startDate, endDate, animalId, farmId);
        Map<String, AnimalEntity> animalsById = loadAnimalsById(productions);

        return productions.stream()
                .collect(Collectors.groupingBy(
                        ProductionEntity::getAnimalId,
                        Collectors.summingDouble(production -> DecimalScaleUtils.zeroIfNull(production.getQuantity()))))
                .entrySet()
                .stream()
                .map(entry -> new AnalyticsAnimalProductionPointResponse(
                        entry.getKey(),
                        animalsById.containsKey(entry.getKey()) ? animalsById.get(entry.getKey()).getTag() : entry.getKey(),
                        DecimalScaleUtils.normalize(entry.getValue())))
                .sorted(Comparator.comparing(AnalyticsAnimalProductionPointResponse::getAnimalTag))
                .toList();
    }

    @Transactional(readOnly = true)
    public String exportProductionByAnimal(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String farmId) {
        return exportProductionByAnimal(startDate, endDate, animalId, farmId, null);
    }

    @Transactional(readOnly = true)
    public String exportProductionByAnimal(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String farmId,
            String productionUnitParam) {
        MeasurementUnit productionUnit = MeasurementUnit.fromProductionParam(productionUnitParam, "productionUnit");
        return CsvExportUtils.write(getProductionByAnimal(startDate, endDate, animalId, farmId), List.of(
                new CsvColumn<>("animalId", AnalyticsAnimalProductionPointResponse::getAnimalId),
                new CsvColumn<>("animalTag", AnalyticsAnimalProductionPointResponse::getAnimalTag),
                new CsvColumn<>("quantity", point -> MeasurementUnitConverter.convertFromBase(
                        point.getQuantity(),
                        productionUnit)),
                new CsvColumn<>("quantityUnit", row -> productionUnit.getSymbol())));
    }

    private AnalyticsGroupBy validateFilters(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam,
            String farmId) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("startDate must be before or equal to endDate");
        }

        if (farmAccessService != null) {
            farmAccessService.validateAccessibleFarmIfPresent(farmId);
        }

        if (StringUtils.hasText(animalId) && !animalRepository.existsById(animalId)) {
            throw new ResourceNotFoundException("Animal not found");
        }
        if (StringUtils.hasText(animalId) && StringUtils.hasText(farmId) && !animalRepository.existsByIdAndFarmId(animalId, farmId)) {
            throw new ResourceNotFoundException("Animal not found");
        }

        try {
            return AnalyticsGroupBy.fromQueryParam(groupByParam);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("groupBy must be day or month");
        }
    }

    private List<ProductionEntity> filterProductions(LocalDate startDate, LocalDate endDate, String animalId, String farmId) {
        List<ProductionEntity> productions = StringUtils.hasText(farmId)
                ? productionRepository.findByFarmIdAndStatus(farmId, ProductionEntity.STATUS_ACTIVE)
                : productionRepository.findAll();

        return productions.stream()
                .filter(production -> matchesAnimal(production.getAnimalId(), animalId))
                .filter(production -> matchesDateRange(production.getDate(), startDate, endDate))
                .toList();
    }

    private List<FeedingEntity> filterFeedings(LocalDate startDate, LocalDate endDate, String animalId, String farmId) {
        List<FeedingEntity> feedings = StringUtils.hasText(farmId)
                ? feedingRepository.findByFarmIdAndStatus(farmId, FeedingEntity.STATUS_ACTIVE)
                : feedingRepository.findAll();

        return feedings.stream()
                .filter(feeding -> matchesAnimal(feeding.getAnimalId(), animalId))
                .filter(feeding -> matchesDateRange(feeding.getDate(), startDate, endDate))
                .toList();
    }

    private boolean matchesAnimal(String entityAnimalId, String filterAnimalId) {
        return !StringUtils.hasText(filterAnimalId) || Objects.equals(entityAnimalId, filterAnimalId);
    }

    private boolean matchesDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null) {
            return false;
        }

        boolean matchesStart = startDate == null || !date.isBefore(startDate);
        boolean matchesEnd = endDate == null || !date.isAfter(endDate);
        return matchesStart && matchesEnd;
    }

    private Map<String, Double> loadFeedCostsById(Collection<FeedingEntity> feedings) {
        Set<String> feedTypeIds = feedings.stream()
                .map(FeedingEntity::getFeedTypeId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        return feedTypeRepository.findAllById(feedTypeIds).stream()
                .collect(Collectors.toMap(FeedTypeEntity::getId, feedType -> DecimalScaleUtils.zeroIfNull(feedType.getCostPerKg())));
    }

    private Map<String, AnimalEntity> loadAnimalsById(Collection<ProductionEntity> productions) {
        Set<String> animalIds = productions.stream()
                .map(ProductionEntity::getAnimalId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        return animalRepository.findAllById(animalIds).stream()
                .collect(Collectors.toMap(AnimalEntity::getId, animal -> animal));
    }

    private Double sumAcquisitionCost(String animalId, String farmId) {
        return filterAnimalsForFinancials(animalId, farmId).stream()
                .map(AnimalEntity::getAcquisitionCost)
                .map(DecimalScaleUtils::zeroIfNull)
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
    }

    private Map<String, Double> aggregateSaleRevenue(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String farmId,
            AnalyticsGroupBy groupBy) {
        List<AnimalEntity> soldAnimals = filterAnimalsForFinancials(animalId, farmId).stream()
                .filter(animal -> animal.getSaleDate() != null)
                .filter(animal -> matchesDateRange(animal.getSaleDate(), startDate, endDate))
                .filter(animal -> animal.getSalePrice() != null)
                .toList();

        return aggregateValues(
                soldAnimals,
                groupBy,
                AnimalEntity::getSaleDate,
                AnimalEntity::getSalePrice);
    }

    private List<AnimalEntity> filterAnimalsForFinancials(String animalId, String farmId) {
        List<AnimalEntity> animals;
        if (StringUtils.hasText(animalId)) {
            AnimalEntity animal = StringUtils.hasText(farmId)
                    ? animalRepository.findByIdAndFarmId(animalId, farmId).orElse(null)
                    : animalRepository.findById(animalId).orElse(null);
            animals = animal != null ? List.of(animal) : List.of();
        } else if (StringUtils.hasText(farmId)) {
            animals = animalRepository.findByFarmId(farmId);
        } else {
            animals = animalRepository.findAll();
        }

        return animals;
    }

    private <T> List<AnalyticsTimeSeriesPointResponse> aggregateSeries(
            Collection<T> items,
            AnalyticsGroupBy groupBy,
            java.util.function.Function<T, LocalDate> dateExtractor,
            java.util.function.Function<T, Double> valueExtractor) {
        return aggregateValues(items, groupBy, dateExtractor, valueExtractor).entrySet().stream()
                .map(entry -> new AnalyticsTimeSeriesPointResponse(entry.getKey(), DecimalScaleUtils.normalize(entry.getValue())))
                .toList();
    }

    private <T> Map<String, Double> aggregateValues(
            Collection<T> items,
            AnalyticsGroupBy groupBy,
            java.util.function.Function<T, LocalDate> dateExtractor,
            java.util.function.Function<T, Double> valueExtractor) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> toPeriodLabel(dateExtractor.apply(item), groupBy),
                        LinkedHashMap::new,
                        Collectors.summingDouble(item -> DecimalScaleUtils.zeroIfNull(valueExtractor.apply(item)))))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> DecimalScaleUtils.normalize(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private List<AnalyticsTimeSeriesPointResponse> convertSeriesValues(
            List<AnalyticsTimeSeriesPointResponse> series,
            String currency) {
        return series.stream()
                .map(point -> new AnalyticsTimeSeriesPointResponse(
                        point.getPeriod(),
                        CurrencyConversionUtils.convertMonetaryValue(point.getValue(), currency)))
                .toList();
    }

    private String toPeriodLabel(LocalDate date, AnalyticsGroupBy groupBy) {
        if (groupBy == AnalyticsGroupBy.MONTH) {
            return YearMonth.from(date).format(MONTH_FORMATTER);
        }

        return date.toString();
    }
}
