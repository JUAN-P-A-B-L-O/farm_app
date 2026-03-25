package com.jpsoftware.farmapp.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private InMemoryUserRepository repositoryHandler;
    private UserService userService;

    @BeforeEach
    void setUp() {
        repositoryHandler = new InMemoryUserRepository();
        userService = new UserService(repositoryHandler.createProxy(), new UserMapper());
    }

    @Test
    void shouldCreateUser() {
        UserResponse response = userService.create(new CreateUserRequest("Jane Doe", "jane@farm.com", "ADMIN"));

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@farm.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest(" ", "jane@farm.com", "ADMIN")));

        assertEquals("name must not be blank", exception.getMessage());
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(new CreateUserRequest("Jane Doe", " ", "ADMIN")));

        assertEquals("email must not be blank", exception.getMessage());
    }

    @Test
    void shouldReturnAllUsers() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        repositoryHandler.store(new UserEntity(userId, "Jane Doe", "jane@farm.com", "ADMIN"));

        List<UserResponse> responses = userService.findAll();

        assertEquals(1, responses.size());
        assertEquals(userId, responses.get(0).getId());
        assertEquals("Jane Doe", responses.get(0).getName());
        assertEquals("jane@farm.com", responses.get(0).getEmail());
        assertEquals("ADMIN", responses.get(0).getRole());
    }

    @Test
    void shouldReturnUserById() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        repositoryHandler.store(new UserEntity(userId, "Jane Doe", "jane@farm.com", "ADMIN"));

        UserResponse response = userService.findById("11111111-1111-1111-1111-111111111111");

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@farm.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void shouldFailWhenUserNotFound() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findById("11111111-1111-1111-1111-111111111111"));

        assertEquals("User not found", exception.getMessage());
    }

    private static class InMemoryUserRepository {

        private final Map<UUID, UserEntity> data = new LinkedHashMap<>();

        UserRepository createProxy() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[]{UserRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("save".equals(methodName)) {
                            UserEntity entity = (UserEntity) args[0];
                            if (entity.getId() == null) {
                                entity.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                            }
                            data.put(entity.getId(), entity);
                            return entity;
                        }
                        if ("findAll".equals(methodName)) {
                            return new ArrayList<>(data.values());
                        }
                        if ("findById".equals(methodName)) {
                            return Optional.ofNullable(data.get(args[0]));
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "InMemoryUserRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }

        void store(UserEntity entity) {
            data.put(entity.getId(), entity);
        }
    }
}
