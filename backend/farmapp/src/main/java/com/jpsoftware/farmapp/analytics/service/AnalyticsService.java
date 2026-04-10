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
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AnalyticsService {

    private static final Double MILK_PRICE = 2.0;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final FeedTypeRepository feedTypeRepository;

    public AnalyticsService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FeedTypeRepository feedTypeRepository) {
        this.productionRepository = productionRepository;
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.feedTypeRepository = feedTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsTimeSeriesPointResponse> getProductionSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam) {
        AnalyticsGroupBy groupBy = validateFilters(startDate, endDate, animalId, groupByParam);
        List<ProductionEntity> productions = filterProductions(startDate, endDate, animalId);

        return aggregateSeries(
                productions,
                groupBy,
                ProductionEntity::getDate,
                ProductionEntity::getQuantity);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsTimeSeriesPointResponse> getFeedingCostSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam) {
        AnalyticsGroupBy groupBy = validateFilters(startDate, endDate, animalId, groupByParam);
        List<FeedingEntity> feedings = filterFeedings(startDate, endDate, animalId);
        Map<String, Double> feedCostsById = loadFeedCostsById(feedings);

        return aggregateSeries(
                feedings,
                groupBy,
                FeedingEntity::getDate,
                feeding -> defaultToZero(feedCostsById.get(feeding.getFeedTypeId())) * defaultToZero(feeding.getQuantity()));
    }

    @Transactional(readOnly = true)
    public List<AnalyticsProfitPointResponse> getProfitSeries(
            LocalDate startDate,
            LocalDate endDate,
            String animalId,
            String groupByParam) {
        AnalyticsGroupBy groupBy = validateFilters(startDate, endDate, animalId, groupByParam);
        List<ProductionEntity> productions = filterProductions(startDate, endDate, animalId);
        List<FeedingEntity> feedings = filterFeedings(startDate, endDate, animalId);
        Map<String, Double> productionByPeriod = aggregateValues(
                productions,
                groupBy,
                ProductionEntity::getDate,
                ProductionEntity::getQuantity);
        Map<String, Double> feedCostsById = loadFeedCostsById(feedings);
        Map<String, Double> feedingCostByPeriod = aggregateValues(
                feedings,
                groupBy,
                FeedingEntity::getDate,
                feeding -> defaultToZero(feedCostsById.get(feeding.getFeedTypeId())) * defaultToZero(feeding.getQuantity()));

        Set<String> periods = new java.util.TreeSet<>();
        periods.addAll(productionByPeriod.keySet());
        periods.addAll(feedingCostByPeriod.keySet());

        return periods.stream()
                .map(period -> {
                    Double production = defaultToZero(productionByPeriod.get(period));
                    Double feedingCost = defaultToZero(feedingCostByPeriod.get(period));
                    Double revenue = production * MILK_PRICE;
                    Double profit = revenue - feedingCost;
                    return new AnalyticsProfitPointResponse(period, production, feedingCost, revenue, profit);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnalyticsAnimalProductionPointResponse> getProductionByAnimal(
            LocalDate startDate,
            LocalDate endDate,
            String animalId) {
        validateFilters(startDate, endDate, animalId, AnalyticsGroupBy.DAY.name());
        List<ProductionEntity> productions = filterProductions(startDate, endDate, animalId);
        Map<String, AnimalEntity> animalsById = loadAnimalsById(productions);

        return productions.stream()
                .collect(Collectors.groupingBy(
                        ProductionEntity::getAnimalId,
                        Collectors.summingDouble(production -> defaultToZero(production.getQuantity()))))
                .entrySet()
                .stream()
                .map(entry -> new AnalyticsAnimalProductionPointResponse(
                        entry.getKey(),
                        animalsById.containsKey(entry.getKey()) ? animalsById.get(entry.getKey()).getTag() : entry.getKey(),
                        entry.getValue()))
                .sorted(Comparator.comparing(AnalyticsAnimalProductionPointResponse::getAnimalTag))
                .toList();
    }

    private AnalyticsGroupBy validateFilters(LocalDate startDate, LocalDate endDate, String animalId, String groupByParam) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("startDate must be before or equal to endDate");
        }

        if (StringUtils.hasText(animalId) && !animalRepository.existsById(animalId)) {
            throw new ResourceNotFoundException("Animal not found");
        }

        try {
            return AnalyticsGroupBy.fromQueryParam(groupByParam);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("groupBy must be day or month");
        }
    }

    private List<ProductionEntity> filterProductions(LocalDate startDate, LocalDate endDate, String animalId) {
        return productionRepository.findAll().stream()
                .filter(production -> matchesAnimal(production.getAnimalId(), animalId))
                .filter(production -> matchesDateRange(production.getDate(), startDate, endDate))
                .toList();
    }

    private List<FeedingEntity> filterFeedings(LocalDate startDate, LocalDate endDate, String animalId) {
        return feedingRepository.findAll().stream()
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
                .collect(Collectors.toMap(FeedTypeEntity::getId, feedType -> defaultToZero(feedType.getCostPerKg())));
    }

    private Map<String, AnimalEntity> loadAnimalsById(Collection<ProductionEntity> productions) {
        Set<String> animalIds = productions.stream()
                .map(ProductionEntity::getAnimalId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        return animalRepository.findAllById(animalIds).stream()
                .collect(Collectors.toMap(AnimalEntity::getId, animal -> animal));
    }

    private <T> List<AnalyticsTimeSeriesPointResponse> aggregateSeries(
            Collection<T> items,
            AnalyticsGroupBy groupBy,
            java.util.function.Function<T, LocalDate> dateExtractor,
            java.util.function.Function<T, Double> valueExtractor) {
        return aggregateValues(items, groupBy, dateExtractor, valueExtractor).entrySet().stream()
                .map(entry -> new AnalyticsTimeSeriesPointResponse(entry.getKey(), entry.getValue()))
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
                        Collectors.summingDouble(item -> defaultToZero(valueExtractor.apply(item)))))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private String toPeriodLabel(LocalDate date, AnalyticsGroupBy groupBy) {
        if (groupBy == AnalyticsGroupBy.MONTH) {
            return YearMonth.from(date).format(MONTH_FORMATTER);
        }

        return date.toString();
    }

    private Double defaultToZero(Double value) {
        return value != null ? value : 0.0;
    }
}
