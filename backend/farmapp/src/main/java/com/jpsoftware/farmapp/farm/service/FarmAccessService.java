package com.jpsoftware.farmapp.farm.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FarmAccessService {

    private final FarmRepository farmRepository;
    private final AuthenticationContextService authenticationContextService;

    public FarmAccessService(FarmRepository farmRepository, AuthenticationContextService authenticationContextService) {
        this.farmRepository = farmRepository;
        this.authenticationContextService = authenticationContextService;
    }

    public Optional<String> validateAccessibleFarmIfPresent(String farmId) {
        if (!StringUtils.hasText(farmId)) {
            return Optional.empty();
        }

        return Optional.of(validateAccessibleFarm(farmId));
    }

    public String validateAccessibleFarm(String farmId) {
        if (!StringUtils.hasText(farmId)) {
            throw new ValidationException("farmId must not be blank");
        }

        Optional<UUID> authenticatedUserId = authenticationContextService.getAuthenticatedUserId();
        boolean hasAccess = authenticatedUserId
                .map(userId -> farmRepository.existsByIdAndOwnerId(farmId, userId))
                .orElseGet(() -> farmRepository.existsById(farmId));

        if (!hasAccess) {
            throw new ResourceNotFoundException("Farm not found");
        }

        return farmId;
    }

    public void ensureBelongsToFarm(String entityFarmId, String farmId, String notFoundMessage) {
        if (StringUtils.hasText(farmId) && !farmId.equals(entityFarmId)) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
    }
}
