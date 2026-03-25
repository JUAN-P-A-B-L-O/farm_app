package com.jpsoftware.farmapp.dashboard.service;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final Double MILK_PRICE = 2.0;

    private final ProductionRepository productionRepository;
    private final FeedingRepository feedingRepository;
    private final AnimalRepository animalRepository;

    public DashboardService(
            ProductionRepository productionRepository,
            FeedingRepository feedingRepository,
            AnimalRepository animalRepository) {
        this.productionRepository = productionRepository;
        this.feedingRepository = feedingRepository;
        this.animalRepository = animalRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Double totalProduction = defaultToZero(productionRepository.sumTotalProduction());
        Double totalFeedingCost = defaultToZero(feedingRepository.sumTotalFeedingCost());
        Double totalRevenue = totalProduction * MILK_PRICE;
        Double totalProfit = totalRevenue - totalFeedingCost;
        Long animalCount = animalRepository.count();

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
