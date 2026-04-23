package com.jpsoftware.farmapp.user.service;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.shared.util.CsvColumn;
import com.jpsoftware.farmapp.shared.util.CsvExportUtils;
import com.jpsoftware.farmapp.user.dto.ActivateUserRequest;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UpdatePasswordRequest;
import com.jpsoftware.farmapp.user.dto.UpdateUserRequest;
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
import org.springframework.data.domain.Sort;
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
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        UUID creatorId = authenticationContextService.getAuthenticatedUserId()
                .orElseThrow(() -> new ValidationException("Authenticated manager is required"));
        LinkedHashSet<String> normalizedFarmIds = validateManagedUserInput(
                request.getName(),
                request.getEmail(),
                request.getRole(),
                request.getFarmIds(),
                creatorId,
                null);
        validateCreatePasswordRules(request);

        UserEntity userEntity = userMapper.toEntity(request);
        userEntity.setName(request.getName().trim());
        userEntity.setEmail(normalizeEmail(request.getEmail()));
        userEntity.setRole(normalizeRole(request.getRole()));
        userEntity.setActive(Boolean.TRUE.equals(request.getActive()));
        userEntity.setAvatarUrl(normalizeAvatarUrl(request.getAvatarUrl()));
        userEntity.setPassword(passwordEncoder.encode(resolveRawPassword(request)));
        UserEntity savedUser = userRepository.save(userEntity);
        persistFarmAssignments(savedUser.getId(), normalizedFarmIds);

        return buildUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(String search, Boolean active, String role) {
        String normalizedSearch = normalizeFilter(search);
        String normalizedRole = normalizeFilterRole(role);

        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "name", "email")).stream()
                .filter(userEntity -> matchesSearch(userEntity, normalizedSearch))
                .filter(userEntity -> matchesActive(userEntity, active))
                .filter(userEntity -> matchesRole(userEntity, normalizedRole))
                .map(this::buildUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public String exportAll(String search, Boolean active, String role) {
        return CsvExportUtils.write(findAll(search, active, role), List.of(
                new CsvColumn<>("id", user -> user.getId() != null ? user.getId().toString() : null),
                new CsvColumn<>("name", UserResponse::getName),
                new CsvColumn<>("email", UserResponse::getEmail),
                new CsvColumn<>("role", UserResponse::getRole),
                new CsvColumn<>("active", UserResponse::getActive),
                new CsvColumn<>("avatarUrl", UserResponse::getAvatarUrl),
                new CsvColumn<>("farmIds", user -> user.getFarmIds() != null ? String.join(";", user.getFarmIds()) : "")));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        UserEntity userEntity = findUserEntity(validateId(id));

        return buildUserResponse(userEntity);
    }

    @Transactional
    public UserResponse update(String id, UpdateUserRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        UUID creatorId = authenticationContextService.getAuthenticatedUserId()
                .orElseThrow(() -> new ValidationException("Authenticated manager is required"));
        UUID userId = validateId(id);
        UserEntity userEntity = findUserEntity(userId);
        LinkedHashSet<String> normalizedFarmIds = validateManagedUserInput(
                request.getName(),
                request.getEmail(),
                request.getRole(),
                request.getFarmIds(),
                creatorId,
                userId);
        ensureFarmOwnerRemainsActiveManager(userEntity, normalizeRole(request.getRole()));

        userEntity.setName(request.getName().trim());
        userEntity.setEmail(normalizeEmail(request.getEmail()));
        userEntity.setRole(normalizeRole(request.getRole()));
        userEntity.setAvatarUrl(normalizeAvatarUrl(request.getAvatarUrl()));
        UserEntity savedUser = userRepository.save(userEntity);
        replaceFarmAssignments(savedUser.getId(), normalizedFarmIds);

        return buildUserResponse(savedUser);
    }

    @Transactional
    public UserResponse inactivate(String id) {
        UserEntity userEntity = findUserEntity(validateId(id));
        ensureUserDoesNotOwnFarms(userEntity.getId(), "Cannot inactivate user who owns farms");

        userEntity.setActive(false);
        return buildUserResponse(userRepository.save(userEntity));
    }

    @Transactional
    public UserResponse activate(String id, ActivateUserRequest request) {
        UserEntity userEntity = findUserEntity(validateId(id));

        userEntity.setActive(true);
        if (request != null && StringUtils.hasText(request.getPassword())) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        return buildUserResponse(userRepository.save(userEntity));
    }

    @Transactional
    public void delete(String id) {
        UserEntity userEntity = findUserEntity(validateId(id));
        ensureUserDoesNotOwnFarms(userEntity.getId(), "Cannot delete user who owns farms");

        userFarmAssignmentRepository.deleteByUserId(userEntity.getId());
        userRepository.delete(userEntity);
    }

    @Transactional
    public void updateOwnPassword(UpdatePasswordRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getCurrentPassword())) {
            throw new ValidationException("currentPassword must not be blank");
        }
        if (!StringUtils.hasText(request.getNewPassword())) {
            throw new ValidationException("newPassword must not be blank");
        }

        UUID authenticatedUserId = authenticationContextService.getAuthenticatedUserId()
                .orElseThrow(() -> new ValidationException("Authenticated user is required"));
        UserEntity userEntity = findUserEntity(authenticatedUserId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), userEntity.getPassword())) {
            throw new ValidationException("currentPassword is incorrect");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new ValidationException("newPassword must be different from currentPassword");
        }

        userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(userEntity);
    }

    private String resolveRawPassword(CreateUserRequest request) {
        if (StringUtils.hasText(request.getPassword())) {
            return request.getPassword();
        }
        return UUID.randomUUID().toString();
    }

    private void persistFarmAssignments(UUID userId, LinkedHashSet<String> farmIds) {
        farmIds.forEach(farmId ->
                userFarmAssignmentRepository.save(new UserFarmAssignmentEntity(null, userId, farmId)));
    }

    private void replaceFarmAssignments(UUID userId, LinkedHashSet<String> farmIds) {
        userFarmAssignmentRepository.deleteByUserId(userId);
        userFarmAssignmentRepository.flush();
        persistFarmAssignments(userId, farmIds);
    }

    private UserResponse buildUserResponse(UserEntity userEntity) {
        List<String> farmIds = userFarmAssignmentRepository.findByUserId(userEntity.getId()).stream()
                .map(UserFarmAssignmentEntity::getFarmId)
                .toList();
        return userMapper.toResponse(userEntity, farmIds);
    }

    private LinkedHashSet<String> validateManagedUserInput(
            String name,
            String email,
            String role,
            List<String> farmIds,
            UUID creatorId,
            UUID existingUserId) {
        validateRequiredText(name, "name must not be blank");
        validateRequiredText(email, "email must not be blank");
        validateRequiredText(role, "role must not be blank");

        LinkedHashSet<String> normalizedFarmIds = normalizeFarmIds(farmIds);
        if (normalizedFarmIds.isEmpty()) {
            throw new ValidationException("farmIds must not be empty");
        }
        boolean ownsAllFarms = normalizedFarmIds.stream()
                .allMatch(farmId -> farmRepository.existsByIdAndOwnerId(farmId, creatorId));
        if (!ownsAllFarms) {
            throw new ValidationException("farmIds must reference farms owned by the authenticated manager");
        }

        String normalizedEmail = normalizeEmail(email);
        userRepository.findByEmail(normalizedEmail)
                .filter(user -> existingUserId == null || !user.getId().equals(existingUserId))
                .ifPresent(existingUser -> {
                    throw new ConflictException("User with this email already exists");
                });

        return normalizedFarmIds;
    }

    private void validateCreatePasswordRules(CreateUserRequest request) {
        if (request.getActive() == null) {
            throw new ValidationException("active must not be null");
        }
        if (Boolean.TRUE.equals(request.getActive()) && !StringUtils.hasText(request.getPassword())) {
            throw new ValidationException("password must not be blank when active is true");
        }
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

    private void ensureFarmOwnerRemainsActiveManager(UserEntity userEntity, String nextRole) {
        if (!ownsAnyFarm(userEntity.getId())) {
            return;
        }
        if (!"MANAGER".equals(nextRole)) {
            throw new ConflictException("User who owns farms must remain a manager");
        }
        if (!userEntity.isActive()) {
            throw new ConflictException("Inactive user who owns farms cannot be updated");
        }
    }

    private void ensureUserDoesNotOwnFarms(UUID userId, String message) {
        if (ownsAnyFarm(userId)) {
            throw new ConflictException(message);
        }
    }

    private boolean ownsAnyFarm(UUID userId) {
        return !farmRepository.findByOwnerId(userId).isEmpty();
    }

    private UserEntity findUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateRequiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException(message);
        }
    }

    private boolean matchesSearch(UserEntity userEntity, String normalizedSearch) {
        if (!StringUtils.hasText(normalizedSearch)) {
            return true;
        }

        return userEntity.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                || userEntity.getEmail().toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    private boolean matchesActive(UserEntity userEntity, Boolean active) {
        return active == null || userEntity.isActive() == active;
    }

    private boolean matchesRole(UserEntity userEntity, String normalizedRole) {
        return !StringUtils.hasText(normalizedRole)
                || normalizedRole.equalsIgnoreCase(userEntity.getRole());
    }

    private String normalizeFilter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeFilterRole(String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        return normalizeRole(role);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeAvatarUrl(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return null;
        }
        return avatarUrl.trim();
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
