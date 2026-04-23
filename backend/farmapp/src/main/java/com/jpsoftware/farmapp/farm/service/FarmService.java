package com.jpsoftware.farmapp.farm.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.dto.CreateFarmRequest;
import com.jpsoftware.farmapp.farm.dto.FarmResponse;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserFarmAssignmentRepository userFarmAssignmentRepository;
    private final AuthenticationContextService authenticationContextService;

    public FarmService(
            FarmRepository farmRepository,
            UserFarmAssignmentRepository userFarmAssignmentRepository,
            AuthenticationContextService authenticationContextService) {
        this.farmRepository = farmRepository;
        this.userFarmAssignmentRepository = userFarmAssignmentRepository;
        this.authenticationContextService = authenticationContextService;
    }

    @Transactional(readOnly = true)
    public List<FarmResponse> findAccessibleFarms(boolean ownedOnly) {
        return authenticationContextService.getAuthenticatedUserId()
                .map(userId -> ownedOnly ? farmRepository.findByOwnerId(userId) : findAccessibleFarmEntities(userId))
                .orElseGet(farmRepository::findAll)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FarmResponse create(CreateFarmRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new ValidationException("name must not be blank");
        }

        FarmEntity farmEntity = new FarmEntity();
        farmEntity.setName(request.getName());
        farmEntity.setOwnerId(authenticationContextService.getAuthenticatedUserId()
                .orElseThrow(() -> new ValidationException("Authenticated user is required")));

        return toResponse(farmRepository.save(farmEntity));
    }

    private FarmResponse toResponse(FarmEntity farmEntity) {
        return new FarmResponse(farmEntity.getId(), farmEntity.getName());
    }

    private List<FarmEntity> findAccessibleFarmEntities(UUID userId) {
        LinkedHashMap<String, FarmEntity> accessibleFarms = new LinkedHashMap<>();

        farmRepository.findByOwnerId(userId).forEach(farm -> accessibleFarms.put(farm.getId(), farm));
        userFarmAssignmentRepository.findByUserId(userId).stream()
                .map(assignment -> assignment.getFarmId())
                .distinct()
                .forEach(farmId -> farmRepository.findById(farmId)
                        .ifPresent(farm -> accessibleFarms.putIfAbsent(farm.getId(), farm)));

        return List.copyOf(accessibleFarms.values());
    }
}
