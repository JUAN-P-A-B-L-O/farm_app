package com.jpsoftware.farmapp.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.dto.ActivateUserRequest;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UpdatePasswordRequest;
import com.jpsoftware.farmapp.user.dto.UpdateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import com.jpsoftware.farmapp.user.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserFarmAssignmentRepository userFarmAssignmentRepository =
            org.mockito.Mockito.mock(UserFarmAssignmentRepository.class);
    private final FarmRepository farmRepository = org.mockito.Mockito.mock(FarmRepository.class);
    private final AuthenticationContextService authenticationContextService =
            org.mockito.Mockito.mock(AuthenticationContextService.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserService userService;
    private UUID managerId;

    @BeforeEach
    void setUp() {
        managerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        userService = new UserService(
                userRepository,
                userFarmAssignmentRepository,
                farmRepository,
                new UserMapper(),
                passwordEncoder,
                authenticationContextService);
        when(authenticationContextService.getAuthenticatedUserId()).thenReturn(Optional.of(managerId));
        when(userFarmAssignmentRepository.findByUserId(any())).thenReturn(List.of());
        when(farmRepository.findByOwnerId(any())).thenReturn(List.of());
    }

    @Test
    void shouldCreateActiveUserAndPersistFarmAssignments() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Doe",
                "Jane@Farm.com",
                "MANAGER",
                "farmapp@123",
                true,
                "https://example.com/avatar.png",
                List.of("farm-1", "farm-2", "farm-1"));

        when(farmRepository.existsByIdAndOwnerId(eq("farm-1"), eq(managerId))).thenReturn(true);
        when(farmRepository.existsByIdAndOwnerId(eq("farm-2"), eq(managerId))).thenReturn(true);
        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            return entity;
        });
        when(userFarmAssignmentRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(List.of(
                        new com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity(
                                UUID.randomUUID(),
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "farm-1"),
                        new com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity(
                                UUID.randomUUID(),
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "farm-2")));

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@farm.com", response.getEmail());
        assertEquals("MANAGER", response.getRole());
        assertEquals("https://example.com/avatar.png", response.getAvatarUrl());
        assertEquals(List.of("farm-1", "farm-2"), response.getFarmIds());
        assertTrue(Boolean.TRUE.equals(response.getActive()));
        verify(userFarmAssignmentRepository, times(2)).save(any());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest(" ", "jane@farm.com", "MANAGER", "farmapp@123", true, null, List.of("farm-1"))));

        assertEquals("name must not be blank", exception.getMessage());
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest("Jane Doe", " ", "MANAGER", "farmapp@123", true, null, List.of("farm-1"))));

        assertEquals("email must not be blank", exception.getMessage());
    }

    @Test
    void shouldFailWhenActiveUserHasNoPassword() {
        when(farmRepository.existsByIdAndOwnerId(eq("farm-1"), eq(managerId))).thenReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest(
                        "Jane Doe",
                        "jane@farm.com",
                        "MANAGER",
                        null,
                        true,
                        null,
                        List.of("farm-1"))));

        assertEquals("password must not be blank when active is true", exception.getMessage());
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                "farmapp@123",
                true,
                null,
                List.of("farm-1"));

        when(farmRepository.existsByIdAndOwnerId("farm-1", managerId)).thenReturn(true);
        when(userRepository.findByEmail("jane@farm.com"))
                .thenReturn(Optional.of(new UserEntity(UUID.randomUUID(), "Existing", "jane@farm.com", "MANAGER")));

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.create(request));

        assertEquals("User with this email already exists", exception.getMessage());
    }

    @Test
    void shouldFailWhenFarmDoesNotBelongToCreator() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                "farmapp@123",
                true,
                null,
                List.of("farm-1"));

        when(farmRepository.existsByIdAndOwnerId("farm-1", managerId)).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.create(request));

        assertEquals("farmIds must reference farms owned by the authenticated manager", exception.getMessage());
    }

    @Test
    void shouldCreateInactiveUserWithoutPassword() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Doe",
                "jane@farm.com",
                "WORKER",
                null,
                false,
                null,
                List.of("farm-1"));

        when(farmRepository.existsByIdAndOwnerId("farm-1", managerId)).thenReturn(true);
        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            return entity;
        });
        when(userFarmAssignmentRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(List.of(
                        new com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity(
                                UUID.randomUUID(),
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "farm-1")));

        UserResponse response = userService.create(request);

        assertEquals("jane@farm.com", response.getEmail());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldUpdateUserAndReplaceFarmAssignments() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UserEntity existingUser = new UserEntity(userId, "Jane Doe", "jane@farm.com", "WORKER", "encoded-password", true);
        UpdateUserRequest request = new UpdateUserRequest(
                "Updated Jane",
                "Updated@Farm.com",
                "MANAGER",
                "https://example.com/updated-avatar.png",
                List.of("farm-1", "farm-2"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(farmRepository.existsByIdAndOwnerId("farm-1", managerId)).thenReturn(true);
        when(farmRepository.existsByIdAndOwnerId("farm-2", managerId)).thenReturn(true);
        when(userRepository.findByEmail("updated@farm.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userFarmAssignmentRepository.findByUserId(userId)).thenReturn(List.of());

        UserResponse response = userService.update(userId.toString(), request);

        assertEquals("Updated Jane", response.getName());
        assertEquals("updated@farm.com", response.getEmail());
        assertEquals("MANAGER", response.getRole());
        assertEquals("https://example.com/updated-avatar.png", response.getAvatarUrl());
        verify(userFarmAssignmentRepository).deleteByUserId(userId);
        verify(userFarmAssignmentRepository, times(2)).save(any());
    }

    @Test
    void shouldInactivateUser() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        UserEntity existingUser = new UserEntity(userId, "Jane Doe", "jane@farm.com", "WORKER", "encoded-password", true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.inactivate(userId.toString());

        assertTrue(Boolean.FALSE.equals(response.getActive()));
        verify(userRepository).save(argThat(user -> !user.isActive()));
    }

    @Test
    void shouldActivateUserAndUpdatePassword() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000016");
        UserEntity existingUser = new UserEntity(userId, "Jane Doe", "jane@farm.com", "WORKER", passwordEncoder.encode("old-password"), false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.activate(userId.toString(), new ActivateUserRequest("farmapp@456"));

        assertTrue(Boolean.TRUE.equals(response.getActive()));
        verify(userRepository).save(argThat(user -> user.isActive() && passwordEncoder.matches("farmapp@456", user.getPassword())));
    }

    @Test
    void shouldFailToInactivateUserWhoOwnsFarms() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000012");
        UserEntity existingUser = new UserEntity(userId, "Manager Owner", "owner@farm.com", "MANAGER", "encoded-password", true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(farmRepository.findByOwnerId(userId)).thenReturn(List.of(new FarmEntity("farm-1", "North Dairy", userId)));

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.inactivate(userId.toString()));

        assertEquals("Cannot inactivate user who owns farms", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserAndAssignments() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000013");
        UserEntity existingUser = new UserEntity(userId, "Jane Doe", "jane@farm.com", "WORKER", "encoded-password", true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.delete(userId.toString());

        verify(userFarmAssignmentRepository).deleteByUserId(userId);
        verify(userRepository).delete(existingUser);
    }

    @Test
    void shouldUpdateOwnPassword() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000014");
        UserEntity existingUser = new UserEntity(
                userId,
                "Jane Doe",
                "jane@farm.com",
                "WORKER",
                passwordEncoder.encode("farmapp@123"),
                true);

        when(authenticationContextService.getAuthenticatedUserId()).thenReturn(Optional.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateOwnPassword(new UpdatePasswordRequest("farmapp@123", "farmapp@456"));

        verify(userRepository).save(argThat(user -> passwordEncoder.matches("farmapp@456", user.getPassword())));
    }

    @Test
    void shouldFailToUpdateOwnPasswordWhenCurrentPasswordIsIncorrect() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000015");
        UserEntity existingUser = new UserEntity(
                userId,
                "Jane Doe",
                "jane@farm.com",
                "WORKER",
                passwordEncoder.encode("farmapp@123"),
                true);

        when(authenticationContextService.getAuthenticatedUserId()).thenReturn(Optional.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.updateOwnPassword(new UpdatePasswordRequest("wrong-password", "farmapp@456")));

        assertEquals("currentPassword is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllUsers() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(new UserEntity(userId, "Jane Doe", "jane@farm.com", "MANAGER")));

        List<UserResponse> responses = userService.findAll(null, null, null);

        assertEquals(1, responses.size());
        assertEquals(userId, responses.get(0).getId());
        assertEquals("Jane Doe", responses.get(0).getName());
        assertEquals("jane@farm.com", responses.get(0).getEmail());
        assertEquals("MANAGER", responses.get(0).getRole());
        assertTrue(Boolean.TRUE.equals(responses.get(0).getActive()));
    }

    @Test
    void shouldFilterUsersBySearchRoleAndStatus() {
        UserEntity manager = new UserEntity(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Jane Manager",
                "jane@farm.com",
                "MANAGER",
                "encoded-password",
                true);
        UserEntity worker = new UserEntity(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Pedro Worker",
                "pedro@farm.com",
                "WORKER",
                "encoded-password",
                false);

        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(manager, worker));

        List<UserResponse> responses = userService.findAll("pedro", false, "worker");

        assertEquals(1, responses.size());
        assertEquals(worker.getId(), responses.get(0).getId());
        assertEquals("WORKER", responses.get(0).getRole());
        assertTrue(Boolean.FALSE.equals(responses.get(0).getActive()));
    }

    @Test
    void shouldReturnUserById() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new UserEntity(userId, "Jane Doe", "jane@farm.com", "MANAGER")));

        UserResponse response = userService.findById("11111111-1111-1111-1111-111111111111");

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@farm.com", response.getEmail());
        assertEquals("MANAGER", response.getRole());
        assertTrue(Boolean.TRUE.equals(response.getActive()));
    }

    @Test
    void shouldFailWhenUserNotFound() {
        when(userRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findById("11111111-1111-1111-1111-111111111111"));

        assertEquals("User not found", exception.getMessage());
    }
}
