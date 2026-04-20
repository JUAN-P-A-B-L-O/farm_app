package com.jpsoftware.farmapp.user.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserFarmAssignmentRepository userFarmAssignmentRepository;
    private final FarmRepository farmRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationContextService authenticationContextService;

    public UserService(
            UserRepository userRepository,
            UserFarmAssignmentRepository userFarmAssignmentRepository,
            FarmRepository farmRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationContextService authenticationContextService) {
        this.userRepository = userRepository;
        this.userFarmAssignmentRepository = userFarmAssignmentRepository;
        this.farmRepository = farmRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationContextService = authenticationContextService;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        UUID creatorId = authenticationContextService.getAuthenticatedUserId()
                .orElseThrow(() -> new ValidationException("Authenticated manager is required"));
        validateInput(request, creatorId);

        UserEntity userEntity = userMapper.toEntity(request);
        userEntity.setEmail(normalizeEmail(request.getEmail()));
        userEntity.setActive(Boolean.TRUE.equals(request.getActive()));
        userEntity.setPassword(passwordEncoder.encode(resolveRawPassword(request)));
        UserEntity savedUser = userRepository.save(userEntity);
        persistFarmAssignments(savedUser.getId(), request.getFarmIds());

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        UserEntity userEntity = userRepository.findById(validateId(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(userEntity);
    }

    private void validateInput(CreateUserRequest request, UUID creatorId) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new ValidationException("name must not be blank");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new ValidationException("email must not be blank");
        }
        if (!StringUtils.hasText(request.getRole())) {
            throw new ValidationException("role must not be blank");
        }
        if (request.getActive() == null) {
            throw new ValidationException("active must not be null");
        }
        if (Boolean.TRUE.equals(request.getActive()) && !StringUtils.hasText(request.getPassword())) {
            throw new ValidationException("password must not be blank when active is true");
        }

        LinkedHashSet<String> normalizedFarmIds = normalizeFarmIds(request.getFarmIds());
        if (normalizedFarmIds.isEmpty()) {
            throw new ValidationException("farmIds must not be empty");
        }
        boolean ownsAllFarms = normalizedFarmIds.stream()
                .allMatch(farmId -> farmRepository.existsByIdAndOwnerId(farmId, creatorId));
        if (!ownsAllFarms) {
            throw new ValidationException("farmIds must reference farms owned by the authenticated manager");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("User with this email already exists");
        }
    }

    private String resolveRawPassword(CreateUserRequest request) {
        if (StringUtils.hasText(request.getPassword())) {
            return request.getPassword();
        }
        return UUID.randomUUID().toString();
    }

    private void persistFarmAssignments(UUID userId, List<String> farmIds) {
        normalizeFarmIds(farmIds).forEach(farmId ->
                userFarmAssignmentRepository.save(new UserFarmAssignmentEntity(null, userId, farmId)));
    }

    private LinkedHashSet<String> normalizeFarmIds(List<String> farmIds) {
        LinkedHashSet<String> normalizedFarmIds = new LinkedHashSet<>();
        if (farmIds == null) {
            return normalizedFarmIds;
        }

        farmIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .forEach(normalizedFarmIds::add);
        return normalizedFarmIds;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UUID validateId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("id must be a valid UUID");
        }
    }
}
