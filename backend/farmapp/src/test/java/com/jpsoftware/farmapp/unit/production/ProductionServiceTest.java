package com.jpsoftware.farmapp.unit.production;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionProfitResponse;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.mapper.ProductionMapper;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.production.service.ProductionService;
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

class ProductionServiceTest {

    private InMemoryProductionRepository productionRepositoryHandler;
    private ExistsByIdRepository animalRepositoryHandler;
    private InMemoryFeedingRepository feedingRepositoryHandler;
    private UserExistsByIdRepository userRepositoryHandler;
    private ProductionService productionService;
    private CreateProductionRequest createProductionRequest;
    private ProductionEntity productionEntity;

    @BeforeEach
    void setUp() {
        productionRepositoryHandler = new InMemoryProductionRepository();
        animalRepositoryHandler = new ExistsByIdRepository();
        feedingRepositoryHandler = new InMemoryFeedingRepository();
        userRepositoryHandler = new UserExistsByIdRepository();

        productionService = new ProductionService(
                productionRepositoryHandler.createProxy(),
                feedingRepositoryHandler.createProxy(),
                animalRepositoryHandler.createProxy(),
                userRepositoryHandler.createProxy(),
                new ProductionMapper());

        createProductionRequest = new CreateProductionRequest(
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5,
                "11111111-1111-1111-1111-111111111111");

        productionEntity = new ProductionEntity();
        productionEntity.setId("production-1");
        productionEntity.setAnimalId("animal-1");
        productionEntity.setDate(LocalDate.of(2026, 3, 20));
        productionEntity.setQuantity(12.5);
        productionEntity.setCreatedBy("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void shouldCreateProduction() {
        animalRepositoryHandler.add("animal-1");
        userRepositoryHandler.add(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        ProductionResponse response = productionService.create(createProductionRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), response.getDate());
        assertEquals(12.5, response.getQuantity());
    }

    @Test
    void shouldFailWhenAnimalDoesNotExist() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.create(createProductionRequest));

        assertEquals("Animal not found", exception.getMessage());
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        animalRepositoryHandler.add("animal-1");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.create(createProductionRequest));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldReturnTotalProduction() {
        animalRepositoryHandler.add("animal-1");
        productionRepositoryHandler.setTotalQuantity("animal-1", 35.5);

        ProductionSummaryResponse response = productionService.getSummaryByAnimal("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(35.5, response.getTotalQuantity());
    }

    @Test
    void shouldReturnZeroWhenNoProduction() {
        animalRepositoryHandler.add("animal-1");

        ProductionSummaryResponse response = productionService.getSummaryByAnimal("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(0.0, response.getTotalQuantity());
    }

    @Test
    void shouldFailWhenAnimalNotFound() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.getSummaryByAnimal("animal-1"));

        assertEquals("Animal not found", exception.getMessage());
    }

    @Test
    void shouldFailWhenAnimalIdIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productionService.getSummaryByAnimal(""));

        assertEquals("animalId must not be blank", exception.getMessage());
    }

    @Test
    void shouldCalculateProfitCorrectly() {
        animalRepositoryHandler.add("animal-1");
        productionRepositoryHandler.setTotalProduction("animal-1", 35.5);
        feedingRepositoryHandler.setTotalFeedingCost("animal-1", 20.0);

        ProductionProfitResponse response = productionService.getProfitByAnimal("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(35.5, response.getTotalProduction());
        assertEquals(20.0, response.getTotalFeedingCost());
        assertEquals(2.0, response.getMilkPrice());
        assertEquals(71.0, response.getRevenue());
        assertEquals(51.0, response.getProfit());
    }

    @Test
    void shouldReturnZeroWhenNoData() {
        animalRepositoryHandler.add("animal-1");
        productionRepositoryHandler.setTotalProduction("animal-1", 0.0);
        feedingRepositoryHandler.setTotalFeedingCost("animal-1", 0.0);

        ProductionProfitResponse response = productionService.getProfitByAnimal("animal-1");

        assertNotNull(response);
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(0.0, response.getTotalProduction());
        assertEquals(0.0, response.getTotalFeedingCost());
        assertEquals(0.0, response.getRevenue());
        assertEquals(0.0, response.getProfit());
    }

    @Test
    void shouldFailWhenAnimalNotFoundForProfit() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.getProfitByAnimal("animal-1"));

        assertEquals("Animal not found", exception.getMessage());
    }

    @Test
    void shouldFailWhenInvalidAnimalIdForProfit() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productionService.getProfitByAnimal(""));

        assertEquals("animalId must not be blank", exception.getMessage());
    }

    @Test
    void shouldFilterByAnimalId() {
        productionRepositoryHandler.store(productionEntity);

        List<ProductionResponse> responses = productionService.findAll("animal-1", null);

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
    }

    @Test
    void shouldFilterByDate() {
        productionRepositoryHandler.store(productionEntity);

        List<ProductionResponse> responses = productionService.findAll(null, LocalDate.of(2026, 3, 20));

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals(LocalDate.of(2026, 3, 20), responses.get(0).getDate());
    }

    @Test
    void shouldFilterByAnimalIdAndDate() {
        productionRepositoryHandler.store(productionEntity);

        List<ProductionResponse> responses = productionService.findAll("animal-1", LocalDate.of(2026, 3, 20));

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), responses.get(0).getDate());
    }

    @Test
    void shouldReturnAllWhenNoFilters() {
        productionRepositoryHandler.store(productionEntity);

        List<ProductionResponse> responses = productionService.findAll(null, null);

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), responses.get(0).getDate());
        assertEquals(12.5, responses.get(0).getQuantity());
    }

    @Test
    void shouldReturnPaginatedProductions() {
        productionRepositoryHandler.store(productionEntity);

        PaginatedResponse<ProductionResponse> response = productionService.findAllPaginated(null, null, 0, 10);

        assertEquals(1, response.getContent().size());
        assertEquals("production-1", response.getContent().get(0).getId());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    void shouldReturnEmptyProductionPageWhenOutOfBounds() {
        productionRepositoryHandler.store(productionEntity);

        PaginatedResponse<ProductionResponse> response = productionService.findAllPaginated(null, null, 2, 10);

        assertEquals(0, response.getContent().size());
        assertEquals(2, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    void shouldFailWhenQuantityIsInvalid() {
        CreateProductionRequest invalidRequest = new CreateProductionRequest(
                "animal-1",
                LocalDate.of(2026, 3, 20),
                0.0,
                "11111111-1111-1111-1111-111111111111");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productionService.create(invalidRequest));

        assertEquals("quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldUpdateProduction() {
        productionRepositoryHandler.store(productionEntity);

        ProductionResponse response = productionService.update(
                "production-1",
                new UpdateProductionRequest(LocalDate.of(2026, 3, 21), 15.0));

        assertNotNull(response);
        assertEquals("production-1", response.getId());
        assertEquals(LocalDate.of(2026, 3, 21), response.getDate());
        assertEquals(15.0, response.getQuantity());
    }

    @Test
    void shouldFailWhenQuantityInvalid() {
        productionRepositoryHandler.store(productionEntity);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productionService.update("production-1", new UpdateProductionRequest(null, 0.0)));

        assertEquals("quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldFailWhenProductionNotFound() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.update(
                        "missing-id",
                        new UpdateProductionRequest(LocalDate.of(2026, 3, 21), 15.0)));

        assertEquals("Production not found", exception.getMessage());
    }

    private static class InMemoryProductionRepository {

        private final Map<String, ProductionEntity> data = new LinkedHashMap<>();
        private final Map<String, Double> totalQuantityByAnimalId = new LinkedHashMap<>();
        private final Map<String, Double> totalProductionByAnimalId = new LinkedHashMap<>();
        private int sequence = 1;

        ProductionRepository createProxy() {
            return (ProductionRepository) Proxy.newProxyInstance(
                    ProductionRepository.class.getClassLoader(),
                    new Class<?>[]{ProductionRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("save".equals(methodName)) {
                            ProductionEntity entity = (ProductionEntity) args[0];
                            if (entity.getId() == null) {
                                entity.setId("production-" + sequence++);
                            }
                            data.put(entity.getId(), entity);
                            return entity;
                        }
                        if ("findAll".equals(methodName)) {
                            if (args != null && args.length == 1 && args[0] instanceof org.springframework.data.domain.Pageable pageable) {
                                List<ProductionEntity> all = new ArrayList<>(data.values());
                                return paginate(all, pageable);
                            }
                            return new ArrayList<>(data.values());
                        }
                        if ("findById".equals(methodName)) {
                            return Optional.ofNullable(data.get(args[0]));
                        }
                        if ("findByAnimalId".equals(methodName)) {
                            List<ProductionEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getAnimalId().equals(args[0]))
                                    .toList();
                            if (args.length == 2) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[1]);
                            }
                            return filtered;
                        }
                        if ("findByDate".equals(methodName)) {
                            List<ProductionEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getDate().equals(args[0]))
                                    .toList();
                            if (args.length == 2) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[1]);
                            }
                            return filtered;
                        }
                        if ("findByAnimalIdAndDate".equals(methodName)) {
                            List<ProductionEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getAnimalId().equals(args[0]))
                                    .filter(entity -> entity.getDate().equals(args[1]))
                                    .toList();
                            if (args.length == 3) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[2]);
                            }
                            return filtered;
                        }
                        if ("sumQuantityByAnimalId".equals(methodName)) {
                            return totalQuantityByAnimalId.get(args[0]);
                        }
                        if ("sumProductionByAnimalId".equals(methodName)) {
                            return totalProductionByAnimalId.get(args[0]);
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "InMemoryProductionRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }

        void store(ProductionEntity entity) {
            data.put(entity.getId(), entity);
        }

        void setTotalQuantity(String animalId, Double totalQuantity) {
            totalQuantityByAnimalId.put(animalId, totalQuantity);
        }

        void setTotalProduction(String animalId, Double totalProduction) {
            totalProductionByAnimalId.put(animalId, totalProduction);
        }

        private PageImpl<ProductionEntity> paginate(List<ProductionEntity> source, org.springframework.data.domain.Pageable pageable) {
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), source.size());
            List<ProductionEntity> content = start >= source.size() ? List.of() : source.subList(start, end);
            return new PageImpl<>(content, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), source.size());
        }
    }

    private static class InMemoryFeedingRepository {

        private final Map<String, Double> totalFeedingCostByAnimalId = new LinkedHashMap<>();

        FeedingRepository createProxy() {
            return (FeedingRepository) Proxy.newProxyInstance(
                    FeedingRepository.class.getClassLoader(),
                    new Class<?>[]{FeedingRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("sumFeedingCostByAnimalId".equals(methodName)) {
                            return totalFeedingCostByAnimalId.get(args[0]);
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

        void setTotalFeedingCost(String animalId, Double totalFeedingCost) {
            totalFeedingCostByAnimalId.put(animalId, totalFeedingCost);
        }
    }

    private static class ExistsByIdRepository {

        private final List<String> ids = new ArrayList<>();

        void add(String id) {
            ids.add(id);
        }

        AnimalRepository createProxy() {
            return (AnimalRepository) Proxy.newProxyInstance(
                    AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{AnimalRepository.class},
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
                            return "AnimalRepositoryProxy";
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

        UserRepository createProxy() {
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
