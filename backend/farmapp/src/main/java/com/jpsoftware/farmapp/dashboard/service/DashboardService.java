package com.jpsoftware.farmapp.dashboard.service;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.milkprice.service.MilkPriceService;
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DashboardService {

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final FarmAccessService farmAccessService;
    private final MilkPriceService milkPriceService;

    @Autowired
    public DashboardService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FarmAccessService farmAccessService,
            MilkPriceService milkPriceService) {
        this.productionRepository = productionRepository;
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.farmAccessService = farmAccessService;
        this.milkPriceService = milkPriceService;
    }

    public DashboardService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FarmAccessService farmAccessService) {
        this(productionRepository, feedingRepository, animalRepository, farmAccessService, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String farmId, boolean includeAcquisitionCost) {
        if (farmAccessService != null) {
            farmAccessService.validateAccessibleFarmIfPresent(farmId);
        }

        Double totalProduction = DecimalScaleUtils.zeroIfNull(StringUtils.hasText(farmId)
                ? productionRepository.sumTotalProductionByFarmId(farmId)
                : productionRepository.sumTotalProduction());
        Double totalFeedingCost = DecimalScaleUtils.zeroIfNull(StringUtils.hasText(farmId)
                ? feedingRepository.sumTotalFeedingCostByFarmId(farmId)
                : feedingRepository.sumTotalFeedingCost());
        Double totalAcquisitionCost = includeAcquisitionCost
                ? DecimalScaleUtils.zeroIfNull(sumAcquisitionCost(farmId))
                : 0.0;
        Double totalSaleRevenue = DecimalScaleUtils.zeroIfNull(sumSaleRevenue(farmId));
        Double totalRevenue = DecimalScaleUtils.normalize(sumMilkRevenue(farmId, totalProduction) + totalSaleRevenue);
        Double totalProfit = DecimalScaleUtils.subtract(
                totalRevenue,
                DecimalScaleUtils.normalize(totalFeedingCost + totalAcquisitionCost));
        Long animalCount = StringUtils.hasText(farmId)
                ? animalRepository.countByFarmId(farmId)
                : animalRepository.count();

        return new DashboardResponse(
                totalProduction,
                totalFeedingCost,
                totalRevenue,
                totalProfit,
                animalCount);
    }

    @Transactional(readOnly = true)
    public String exportDashboard(String farmId, boolean includeAcquisitionCost) {
        DashboardResponse dashboard = getDashboard(farmId, includeAcquisitionCost);
        return CsvExportUtils.write(List.of(dashboard), List.of(
                new CsvColumn<>("totalProduction", DashboardResponse::getTotalProduction),
                new CsvColumn<>("totalFeedingCost", DashboardResponse::getTotalFeedingCost),
                new CsvColumn<>("totalRevenue", DashboardResponse::getTotalRevenue),
                new CsvColumn<>("totalProfit", DashboardResponse::getTotalProfit),
                new CsvColumn<>("animalCount", DashboardResponse::getAnimalCount)));
    }

    private Double sumMilkRevenue(String farmId, Double totalProduction) {
        if (StringUtils.hasText(farmId)) {
            Double milkPrice = milkPriceService != null
                    ? milkPriceService.resolveCurrentPriceValue(farmId)
                    : MilkPriceService.DEFAULT_MILK_PRICE;
            return DecimalScaleUtils.multiply(totalProduction, milkPrice);
        }

        return productionRepository.findAll()
                .stream()
                .map(production -> DecimalScaleUtils.multiply(
                        DecimalScaleUtils.zeroIfNull(production.getQuantity()),
                        milkPriceService != null
                                ? milkPriceService.resolveCurrentPriceValue(production.getFarmId())
                                : MilkPriceService.DEFAULT_MILK_PRICE))
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
    }

    private Double sumAcquisitionCost(String farmId) {
        List<AnimalEntity> animals = StringUtils.hasText(farmId)
                ? animalRepository.findByFarmId(farmId)
                : animalRepository.findAll();

        return animals.stream()
                .map(AnimalEntity::getAcquisitionCost)
                .map(DecimalScaleUtils::zeroIfNull)
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
    }

    private Double sumSaleRevenue(String farmId) {
        List<AnimalEntity> animals = StringUtils.hasText(farmId)
                ? animalRepository.findByFarmId(farmId)
                : animalRepository.findAll();

        return animals.stream()
                .map(AnimalEntity::getSalePrice)
                .map(DecimalScaleUtils::zeroIfNull)
                .reduce(0.0, (left, right) -> DecimalScaleUtils.normalize(left + right));
    }
}
