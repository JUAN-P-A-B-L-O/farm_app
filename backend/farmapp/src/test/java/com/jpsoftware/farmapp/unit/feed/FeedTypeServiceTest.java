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
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class FeedTypeServiceTest {

    private InMemoryFeedTypeRepository repositoryHandler;
    private FeedTypeService feedTypeService;
    private FarmAccessService farmAccessService;

    @BeforeEach
    void setUp() {
        repositoryHandler = new InMemoryFeedTypeRepository();
        FeedTypeRepository repository = repositoryHandler.createProxy();
        farmAccessService = org.mockito.Mockito.mock(FarmAccessService.class);
        org.mockito.Mockito.when(farmAccessService.validateAccessibleFarm(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.when(farmAccessService.validateAccessibleFarmIfPresent(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(invocation.getArgument(0)));
        feedTypeService = new FeedTypeService(repository, new FeedTypeMapper(), farmAccessService);
    }

    @Test
    void shouldCreateFeedType() {
        FeedTypeResponse response = feedTypeService.create(new CreateFeedTypeRequest("Corn Silage", 1.75), "farm-1");

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
                () -> feedTypeService.create(invalidRequest, "farm-1"));

        assertEquals("name must not be blank", exception.getMessage());
    }

    @Test
    void shouldFailWhenCostInvalid() {
        CreateFeedTypeRequest invalidRequest = new CreateFeedTypeRequest("Corn Silage", 0.0);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> feedTypeService.create(invalidRequest, "farm-1"));

        assertEquals("costPerKg must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldReturnAllFeedTypes() {
        repositoryHandler.store(new FeedTypeEntity("feed-type-1", "Corn Silage", 1.75, true, "farm-1"));

        List<FeedTypeResponse> responses = feedTypeService.findAll("farm-1");

        assertEquals(1, responses.size());
        assertEquals("feed-type-1", responses.get(0).getId());
        assertEquals("Corn Silage", responses.get(0).getName());
        assertEquals(1.75, responses.get(0).getCostPerKg());
        assertEquals(true, responses.get(0).getActive());
    }

    @Test
    void shouldReturnPaginatedFeedTypes() {
        repositoryHandler.store(new FeedTypeEntity("feed-type-1", "Corn Silage", 1.75, true, "farm-1"));

        PaginatedResponse<FeedTypeResponse> response = feedTypeService.findAllPaginated("farm-1", 0, 10);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    void shouldReturnFeedTypeById() {
        repositoryHandler.store(new FeedTypeEntity("feed-type-1", "Corn Silage", 1.75, true, "farm-1"));

        FeedTypeResponse response = feedTypeService.findById("feed-type-1", "farm-1");

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
                () -> feedTypeService.findById("missing-id", "farm-1"));

        assertEquals("Feed type not found", exception.getMessage());
    }

    @Test
    void shouldExportFeedTypesWithConvertedMeasurementUnit() {
        repositoryHandler.store(new FeedTypeEntity("feed-type-1", "Corn Silage", 1.75, true, "farm-1"));

        String csv = feedTypeService.exportAll("farm-1", null, null, "GRAM");

        assertEquals(
                "id,name,costPerUnit,costUnit,active\nfeed-type-1,Corn Silage,0.00175,g,true\n",
                csv);
    }

    @Test
    void shouldExportFeedTypesWithConvertedMeasurementUnitAndCurrencyPrecision() {
        repositoryHandler.store(new FeedTypeEntity("feed-type-1", "Corn Silage", 1.75, true, "farm-1"));

        String csv = feedTypeService.exportAll("farm-1", null, "USD", "GRAM");

        assertEquals(
                "id,name,costPerUnit,costUnit,active\nfeed-type-1,Corn Silage,0.00035,g,true\n",
                csv);
    }

    @Test
    void shouldRejectInvalidMeasurementUnitWhenExportingFeedTypes() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> feedTypeService.exportAll("farm-1", null, null, "LITER"));

        assertEquals("measurementUnit must be KILOGRAM or GRAM", exception.getMessage());
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
                            List<FeedTypeEntity> activeFeedTypes = data.values().stream()
                                    .filter(entity -> Boolean.TRUE.equals(entity.getActive()))
                                    .toList();
                            if (args != null && args.length == 2 && args[1] instanceof org.springframework.data.domain.Pageable pageable) {
                                return paginate(activeFeedTypes, pageable);
                            }
                            return new ArrayList<>(activeFeedTypes);
                        }
                        if ("findByFarmIdAndActiveTrue".equals(methodName)) {
                            if (args.length == 2) {
                                List<FeedTypeEntity> filtered = data.values().stream()
                                        .filter(entity -> Boolean.TRUE.equals(entity.getActive()))
                                        .filter(entity -> args[0].equals(entity.getFarmId()))
                                        .toList();
                                org.springframework.data.domain.Pageable pageable =
                                        (org.springframework.data.domain.Pageable) args[1];
                                return paginate(filtered, pageable);
                            }
                            return data.values().stream()
                                    .filter(entity -> Boolean.TRUE.equals(entity.getActive()))
                                    .filter(entity -> args[0].equals(entity.getFarmId()))
                                    .toList();
                        }
                        if ("findByActiveTrue".equals(methodName)) {
                            if (args != null && args.length == 1) {
                                org.springframework.data.domain.Pageable pageable =
                                        (org.springframework.data.domain.Pageable) args[0];
                                return paginate(
                                        data.values().stream()
                                                .filter(entity -> Boolean.TRUE.equals(entity.getActive()))
                                                .toList(),
                                        pageable);
                            }
                            return data.values().stream()
                                    .filter(entity -> Boolean.TRUE.equals(entity.getActive()))
                                    .toList();
                        }
                        if ("findById".equals(methodName)) {
                            return Optional.ofNullable(data.get(args[0]));
                        }
                        if ("findByIdAndActiveTrue".equals(methodName)) {
                            FeedTypeEntity entity = data.get(args[0]);
                            return Optional.ofNullable(entity).filter(item -> Boolean.TRUE.equals(item.getActive()));
                        }
                        if ("findByIdAndFarmIdAndActiveTrue".equals(methodName)) {
                            FeedTypeEntity entity = data.get(args[0]);
                            return Optional.ofNullable(entity)
                                    .filter(item -> Boolean.TRUE.equals(item.getActive()))
                                    .filter(item -> args[1].equals(item.getFarmId()));
                        }
                        if ("findByIdAndFarmId".equals(methodName)) {
                            FeedTypeEntity entity = data.get(args[0]);
                            return Optional.ofNullable(entity)
                                    .filter(item -> args[1].equals(item.getFarmId()));
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

        private PageImpl<FeedTypeEntity> paginate(List<FeedTypeEntity> source, org.springframework.data.domain.Pageable pageable) {
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), source.size());
            List<FeedTypeEntity> content = start >= source.size() ? List.of() : source.subList(start, end);
            return new PageImpl<>(content, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), source.size());
        }
    }
}
