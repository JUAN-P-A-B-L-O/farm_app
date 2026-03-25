package com.jpsoftware.farmapp.unit.feed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.mapper.FeedTypeMapper;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feed.service.FeedTypeService;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeedTypeServiceTest {

    private InMemoryFeedTypeRepository repositoryHandler;
    private FeedTypeService feedTypeService;

    @BeforeEach
    void setUp() {
        repositoryHandler = new InMemoryFeedTypeRepository();
        FeedTypeRepository repository = repositoryHandler.createProxy();
        feedTypeService = new FeedTypeService(repository, new FeedTypeMapper());
    }

    @Test
    void shouldCreateFeedType() {
        FeedTypeResponse response = feedTypeService.create(new CreateFeedTypeRequest("Corn Silage", 1.75));

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Corn Silage", response.getName());
        assertEquals(1.75, response.getCostPerKg());
        assertEquals(true, response.getActive());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        CreateFeedTypeRequest invalidRequest = new CreateFeedTypeRequest(" ", 1.75);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> feedTypeService.create(invalidRequest));

        assertEquals("name must not be blank", exception.getMessage());
    }

    @Test
    void shouldFailWhenCostInvalid() {
        CreateFeedTypeRequest invalidRequest = new CreateFeedTypeRequest("Corn Silage", 0.0);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> feedTypeService.create(invalidRequest));

        assertEquals("costPerKg must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldReturnAllFeedTypes() {
        repositoryHandler.store(new FeedTypeEntity("feed-type-1", "Corn Silage", 1.75, true));

        List<FeedTypeResponse> responses = feedTypeService.findAll();

        assertEquals(1, responses.size());
        assertEquals("feed-type-1", responses.get(0).getId());
        assertEquals("Corn Silage", responses.get(0).getName());
        assertEquals(1.75, responses.get(0).getCostPerKg());
        assertEquals(true, responses.get(0).getActive());
    }

    @Test
    void shouldReturnFeedTypeById() {
        repositoryHandler.store(new FeedTypeEntity("feed-type-1", "Corn Silage", 1.75, true));

        FeedTypeResponse response = feedTypeService.findById("feed-type-1");

        assertNotNull(response);
        assertEquals("feed-type-1", response.getId());
        assertEquals("Corn Silage", response.getName());
        assertEquals(1.75, response.getCostPerKg());
        assertEquals(true, response.getActive());
    }

    @Test
    void shouldFailWhenFeedTypeNotFound() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedTypeService.findById("missing-id"));

        assertEquals("Feed type not found", exception.getMessage());
    }

    private static class InMemoryFeedTypeRepository {

        private final Map<String, FeedTypeEntity> data = new LinkedHashMap<>();
        private int sequence = 1;

        FeedTypeRepository createProxy() {
            return (FeedTypeRepository) Proxy.newProxyInstance(
                    FeedTypeRepository.class.getClassLoader(),
                    new Class<?>[]{FeedTypeRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("save".equals(methodName)) {
                            FeedTypeEntity entity = (FeedTypeEntity) args[0];
                            if (entity.getId() == null) {
                                entity.setId("feed-type-" + sequence++);
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
                            return "InMemoryFeedTypeRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }

        void store(FeedTypeEntity entity) {
            data.put(entity.getId(), entity);
        }
    }
}
