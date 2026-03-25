package com.jpsoftware.farmapp.feeding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import com.jpsoftware.farmapp.feeding.mapper.FeedingMapper;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeedingServiceTest {

    private InMemoryFeedingRepository feedingRepositoryHandler;
    private ExistsByIdRepository animalRepositoryHandler;
    private ExistsByIdRepository feedTypeRepositoryHandler;
    private UserExistsByIdRepository userRepositoryHandler;
    private FeedingService feedingService;

    @BeforeEach
    void setUp() {
        feedingRepositoryHandler = new InMemoryFeedingRepository();
        animalRepositoryHandler = new ExistsByIdRepository();
        feedTypeRepositoryHandler = new ExistsByIdRepository();
        userRepositoryHandler = new UserExistsByIdRepository();

        feedingService = new FeedingService(
                feedingRepositoryHandler.createProxy(),
                animalRepositoryHandler.createAnimalProxy(),
                feedTypeRepositoryHandler.createFeedTypeProxy(),
                userRepositoryHandler.createUserProxy(),
                new FeedingMapper());
    }

    @Test
    void shouldCreateFeeding() {
        animalRepositoryHandler.add("animal-1");
        feedTypeRepositoryHandler.add("feed-type-1");
        userRepositoryHandler.add(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        FeedingResponse response = feedingService.create(
                new CreateFeedingRequest(
                        "animal-1",
                        "feed-type-1",
                        LocalDate.of(2026, 3, 24),
                        8.5,
                        "11111111-1111-1111-1111-111111111111"));

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("animal-1", response.getAnimalId());
        assertEquals("feed-type-1", response.getFeedTypeId());
        assertEquals(LocalDate.of(2026, 3, 24), response.getDate());
        assertEquals(8.5, response.getQuantity());
    }

    @Test
    void shouldFailWhenAnimalNotFound() {
        feedTypeRepositoryHandler.add("feed-type-1");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedingService.create(
                        new CreateFeedingRequest(
                                "animal-1",
                                "feed-type-1",
                                LocalDate.of(2026, 3, 24),
                                8.5,
                                "11111111-1111-1111-1111-111111111111")));

        assertEquals("Animal not found", exception.getMessage());
    }

    @Test
    void shouldFailWhenFeedTypeNotFound() {
        animalRepositoryHandler.add("animal-1");
        userRepositoryHandler.add(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedingService.create(
                        new CreateFeedingRequest(
                                "animal-1",
                                "feed-type-1",
                                LocalDate.of(2026, 3, 24),
                                8.5,
                                "11111111-1111-1111-1111-111111111111")));

        assertEquals("Feed type not found", exception.getMessage());
    }

    @Test
    void shouldFailWhenUserNotFound() {
        animalRepositoryHandler.add("animal-1");
        feedTypeRepositoryHandler.add("feed-type-1");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedingService.create(
                        new CreateFeedingRequest(
                                "animal-1",
                                "feed-type-1",
                                LocalDate.of(2026, 3, 24),
                                8.5,
                                "11111111-1111-1111-1111-111111111111")));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldFailWhenQuantityInvalid() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> feedingService.create(
                        new CreateFeedingRequest(
                                "animal-1",
                                "feed-type-1",
                                LocalDate.of(2026, 3, 24),
                                0.0,
                                "11111111-1111-1111-1111-111111111111")));

        assertEquals("quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldReturnAllFeedings() {
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5,
                "11111111-1111-1111-1111-111111111111"));

        List<FeedingResponse> responses = feedingService.findAll();

        assertEquals(1, responses.size());
        assertEquals("feeding-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        assertEquals("feed-type-1", responses.get(0).getFeedTypeId());
        assertEquals(LocalDate.of(2026, 3, 24), responses.get(0).getDate());
        assertEquals(8.5, responses.get(0).getQuantity());
    }

    @Test
    void shouldReturnFeedingById() {
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5,
                "11111111-1111-1111-1111-111111111111"));

        FeedingResponse response = feedingService.findById("feeding-1");

        assertNotNull(response);
        assertEquals("feeding-1", response.getId());
        assertEquals("animal-1", response.getAnimalId());
        assertEquals("feed-type-1", response.getFeedTypeId());
        assertEquals(LocalDate.of(2026, 3, 24), response.getDate());
        assertEquals(8.5, response.getQuantity());
    }

    @Test
    void shouldFailWhenFeedingNotFound() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedingService.findById("missing-id"));

        assertEquals("Feeding not found", exception.getMessage());
    }

    private static class InMemoryFeedingRepository {

        private final Map<String, FeedingEntity> data = new LinkedHashMap<>();
        private int sequence = 1;

        FeedingRepository createProxy() {
            return (FeedingRepository) Proxy.newProxyInstance(
                    FeedingRepository.class.getClassLoader(),
                    new Class<?>[]{FeedingRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("save".equals(methodName)) {
                            FeedingEntity entity = (FeedingEntity) args[0];
                            if (entity.getId() == null) {
                                entity.setId("feeding-" + sequence++);
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
                            return "InMemoryFeedingRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }

        void store(FeedingEntity entity) {
            data.put(entity.getId(), entity);
        }
    }

    private static class ExistsByIdRepository {

        private final List<String> ids = new ArrayList<>();

        void add(String id) {
            ids.add(id);
        }

        AnimalRepository createAnimalProxy() {
            return (AnimalRepository) createProxy(AnimalRepository.class);
        }

        FeedTypeRepository createFeedTypeProxy() {
            return (FeedTypeRepository) createProxy(FeedTypeRepository.class);
        }

        private Object createProxy(Class<?> repositoryType) {
            return Proxy.newProxyInstance(
                    repositoryType.getClassLoader(),
                    new Class<?>[]{repositoryType},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("existsById".equals(methodName)) {
                            return ids.contains(args[0]);
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return repositoryType.getSimpleName() + "Proxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }
    }

    private static class UserExistsByIdRepository {

        private final List<UUID> ids = new ArrayList<>();

        void add(UUID id) {
            ids.add(id);
        }

        UserRepository createUserProxy() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[]{UserRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("existsById".equals(methodName)) {
                            return ids.contains(args[0]);
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "UserRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }
    }
}
