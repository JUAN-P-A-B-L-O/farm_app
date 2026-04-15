package com.jpsoftware.farmapp.dashboard.service;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DashboardService {

    private static final Double MILK_PRICE = 2.0;

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;
    private final FarmAccessService farmAccessService;

    public DashboardService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository,
            FarmAccessService farmAccessService) {
        this.productionRepository = productionRepository;
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
        this.farmAccessService = farmAccessService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String farmId, boolean includeAcquisitionCost) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);

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
        Double totalRevenue = DecimalScaleUtils.normalize(
                DecimalScaleUtils.multiply(totalProduction, MILK_PRICE) + totalSaleRevenue);
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
