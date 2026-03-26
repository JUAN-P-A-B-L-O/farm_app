package com.jpsoftware.farmapp.unit.feeding;

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
import com.jpsoftware.farmapp.feeding.service.FeedingService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

        List<FeedingResponse> responses = feedingService.findAll(null, null);

        assertEquals(1, responses.size());
        assertEquals("feeding-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        assertEquals("feed-type-1", responses.get(0).getFeedTypeId());
        assertEquals(LocalDate.of(2026, 3, 24), responses.get(0).getDate());
        assertEquals(8.5, responses.get(0).getQuantity());
    }

    @Test
    void shouldReturnPaginatedFeedings() {
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5,
                "11111111-1111-1111-1111-111111111111"));

        PaginatedResponse<FeedingResponse> response = feedingService.findAllPaginated(null, null, 0, 10);

        assertEquals(1, response.getContent().size());
        assertEquals("feeding-1", response.getContent().get(0).getId());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    void shouldReturnEmptyFeedingPageWhenOutOfBounds() {
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5,
                "11111111-1111-1111-1111-111111111111"));

        PaginatedResponse<FeedingResponse> response = feedingService.findAllPaginated(null, null, 2, 10);

        assertEquals(0, response.getContent().size());
        assertEquals(2, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    void shouldFilterFeedingsByAnimalId() {
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5,
                "11111111-1111-1111-1111-111111111111"));
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-2",
                "animal-2",
                "feed-type-2",
                LocalDate.of(2026, 3, 24),
                9.0,
                "11111111-1111-1111-1111-111111111111"));

        List<FeedingResponse> responses = feedingService.findAll("animal-1", null);

        assertEquals(1, responses.size());
        assertEquals("feeding-1", responses.get(0).getId());
    }

    @Test
    void shouldFilterFeedingsByDate() {
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5,
                "11111111-1111-1111-1111-111111111111"));
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-2",
                "animal-2",
                "feed-type-2",
                LocalDate.of(2026, 3, 25),
                9.0,
                "11111111-1111-1111-1111-111111111111"));

        List<FeedingResponse> responses = feedingService.findAll(null, LocalDate.of(2026, 3, 24));

        assertEquals(1, responses.size());
        assertEquals("feeding-1", responses.get(0).getId());
    }

    @Test
    void shouldFilterFeedingsByAnimalIdAndDate() {
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5,
                "11111111-1111-1111-1111-111111111111"));
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-2",
                "animal-1",
                "feed-type-2",
                LocalDate.of(2026, 3, 25),
                9.0,
                "11111111-1111-1111-1111-111111111111"));
        feedingRepositoryHandler.store(new FeedingEntity(
                "feeding-3",
                "animal-2",
                "feed-type-3",
                LocalDate.of(2026, 3, 24),
                7.0,
                "11111111-1111-1111-1111-111111111111"));

        List<FeedingResponse> responses = feedingService.findAll("animal-1", LocalDate.of(2026, 3, 24));

        assertEquals(1, responses.size());
        assertEquals("feeding-1", responses.get(0).getId());
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
                            if (args != null && args.length == 1 && args[0] instanceof org.springframework.data.domain.Pageable pageable) {
                                List<FeedingEntity> all = new ArrayList<>(data.values());
                                return paginate(all, pageable);
                            }
                            return new ArrayList<>(data.values());
                        }
                        if ("findByAnimalId".equals(methodName)) {
                            List<FeedingEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getAnimalId().equals(args[0]))
                                    .toList();
                            if (args.length == 2) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[1]);
                            }
                            return filtered;
                        }
                        if ("findByDate".equals(methodName)) {
                            List<FeedingEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getDate().equals(args[0]))
                                    .toList();
                            if (args.length == 2) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[1]);
                            }
                            return filtered;
                        }
                        if ("findByAnimalIdAndDate".equals(methodName)) {
                            List<FeedingEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getAnimalId().equals(args[0]))
                                    .filter(entity -> entity.getDate().equals(args[1]))
                                    .toList();
                            if (args.length == 3) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[2]);
                            }
                            return filtered;
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

        private PageImpl<FeedingEntity> paginate(List<FeedingEntity> source, org.springframework.data.domain.Pageable pageable) {
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), source.size());
            List<FeedingEntity> content = start >= source.size() ? List.of() : source.subList(start, end);
            return new PageImpl<>(content, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), source.size());
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
