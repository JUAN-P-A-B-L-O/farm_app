package com.jpsoftware.farmapp.dashboard.service;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
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
    public DashboardResponse getDashboard(String farmId) {
        farmAccessService.validateAccessibleFarmIfPresent(farmId);

        Double totalProduction = defaultToZero(StringUtils.hasText(farmId)
                ? productionRepository.sumTotalProductionByFarmId(farmId)
                : productionRepository.sumTotalProduction());
        Double totalFeedingCost = defaultToZero(StringUtils.hasText(farmId)
                ? feedingRepository.sumTotalFeedingCostByFarmId(farmId)
                : feedingRepository.sumTotalFeedingCost());
        Double totalRevenue = totalProduction * MILK_PRICE;
        Double totalProfit = totalRevenue - totalFeedingCost;
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

    private Double defaultToZero(Double value) {
        return value != null ? value : 0.0;
    }
}
