package com.jpsoftware.farmapp.farm.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FarmAccessService {

    private final FarmRepository farmRepository;
    private final UserFarmAssignmentRepository userFarmAssignmentRepository;
    private final AuthenticationContextService authenticationContextService;

    public FarmAccessService(
            FarmRepository farmRepository,
            UserFarmAssignmentRepository userFarmAssignmentRepository,
            AuthenticationContextService authenticationContextService) {
        this.farmRepository = farmRepository;
        this.userFarmAssignmentRepository = userFarmAssignmentRepository;
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

        boolean hasAccess = authenticationContextService.getAuthenticatedUserId()
                .map(userId -> hasAccessToFarm(userId, farmId))
                .orElseGet(() -> farmRepository.existsById(farmId));

        if (!hasAccess) {
            throw new ResourceNotFoundException("Farm not found");
        }

        return farmId;
    }

    public Optional<Set<String>> getAccessibleFarmIds() {
        return authenticationContextService.getAuthenticatedUserId()
                .map(this::collectAccessibleFarmIds);
    }

    public void validateEntityFarmAccess(String entityFarmId, String notFoundMessage) {
        if (!StringUtils.hasText(entityFarmId)) {
            throw new ResourceNotFoundException(notFoundMessage);
        }

        Optional<UUID> authenticatedUserId = authenticationContextService.getAuthenticatedUserId();
        if (authenticatedUserId.isPresent() && !hasAccessToFarm(authenticatedUserId.get(), entityFarmId)) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
    }

    public void ensureBelongsToFarm(String entityFarmId, String farmId, String notFoundMessage) {
        if (StringUtils.hasText(farmId) && !farmId.equals(entityFarmId)) {
            throw new ResourceNotFoundException(notFoundMessage);
        }
    }

    private boolean hasAccessToFarm(UUID userId, String farmId) {
        return farmRepository.existsByIdAndOwnerId(farmId, userId)
                || userFarmAssignmentRepository.existsByUserIdAndFarmId(userId, farmId);
    }

    private Set<String> collectAccessibleFarmIds(UUID userId) {
        LinkedHashSet<String> farmIds = new LinkedHashSet<>();
        farmRepository.findByOwnerId(userId).forEach(farm -> farmIds.add(farm.getId()));
        userFarmAssignmentRepository.findByUserId(userId).forEach(assignment -> farmIds.add(assignment.getFarmId()));
        return Set.copyOf(farmIds);
    }
}
