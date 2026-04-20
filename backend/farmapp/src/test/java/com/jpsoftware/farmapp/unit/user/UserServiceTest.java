package com.jpsoftware.farmapp.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
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
    }

    @Test
    void shouldCreateActiveUserAndPersistFarmAssignments() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Doe",
                "Jane@Farm.com",
                "MANAGER",
                "farmapp@123",
                true,
                List.of("farm-1", "farm-2", "farm-1"));

        when(farmRepository.existsByIdAndOwnerId(eq("farm-1"), eq(managerId))).thenReturn(true);
        when(farmRepository.existsByIdAndOwnerId(eq("farm-2"), eq(managerId))).thenReturn(true);
        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            return entity;
        });

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@farm.com", response.getEmail());
        assertEquals("MANAGER", response.getRole());
        verify(userFarmAssignmentRepository, times(2)).save(any());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest(" ", "jane@farm.com", "MANAGER", "farmapp@123", true, List.of("farm-1"))));

        assertEquals("name must not be blank", exception.getMessage());
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest("Jane Doe", " ", "MANAGER", "farmapp@123", true, List.of("farm-1"))));

        assertEquals("email must not be blank", exception.getMessage());
    }

    @Test
    void shouldFailWhenActiveUserHasNoPassword() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest(
                        "Jane Doe",
                        "jane@farm.com",
                        "MANAGER",
                        null,
                        true,
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
                List.of("farm-1"));

        when(farmRepository.existsByIdAndOwnerId("farm-1", managerId)).thenReturn(true);
        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            return entity;
        });

        UserResponse response = userService.create(request);

        assertEquals("jane@farm.com", response.getEmail());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldReturnAllUsers() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userRepository.findAll()).thenReturn(List.of(new UserEntity(userId, "Jane Doe", "jane@farm.com", "MANAGER")));

        List<UserResponse> responses = userService.findAll();

        assertEquals(1, responses.size());
        assertEquals(userId, responses.get(0).getId());
        assertEquals("Jane Doe", responses.get(0).getName());
        assertEquals("jane@farm.com", responses.get(0).getEmail());
        assertEquals("MANAGER", responses.get(0).getRole());
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
