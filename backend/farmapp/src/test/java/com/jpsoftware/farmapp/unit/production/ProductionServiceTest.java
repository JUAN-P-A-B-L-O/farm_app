package com.jpsoftware.farmapp.unit.production;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.auth.model.AuthenticatedUser;
import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
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
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
        SecurityContextHolder.clearContext();
        productionRepositoryHandler = new InMemoryProductionRepository();
        animalRepositoryHandler = new ExistsByIdRepository();
        feedingRepositoryHandler = new InMemoryFeedingRepository();
        userRepositoryHandler = new UserExistsByIdRepository();

        productionService = new ProductionService(
                productionRepositoryHandler.createProxy(),
                feedingRepositoryHandler.createProxy(),
                animalRepositoryHandler.createProxy(),
                userRepositoryHandler.createProxy(),
                new ProductionMapper(),
                new AuthenticationContextService());

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
        productionEntity.setStatus(ProductionEntity.STATUS_ACTIVE);
    }

    @Test
    void shouldCreateProduction() {
        animalRepositoryHandler.add("animal-1", "TAG-001");
        userRepositoryHandler.add(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        ProductionResponse response = productionService.create(createProductionRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("animal-1", response.getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), response.getDate());
        assertEquals(12.5, response.getQuantity());
        assertNotNull(response.getAnimal());
        assertEquals("animal-1", response.getAnimal().getId());
        assertEquals("TAG-001", response.getAnimal().getTag());
    }

    @Test
    void shouldUseCurrentDateWhenAuthenticatedUserIsWorker() {
        UUID workerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        animalRepositoryHandler.add("animal-1", "TAG-001");
        userRepositoryHandler.add(workerId);
        authenticate(workerId, "WORKER");

        ProductionResponse response = productionService.create(
                new CreateProductionRequest(
                        "animal-1",
                        LocalDate.of(2026, 3, 20),
                        12.5,
                        "11111111-1111-1111-1111-111111111111"));

        assertEquals(LocalDate.now(), response.getDate());
    }

    @Test
    void shouldFailWhenAnimalDoesNotExist() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productionService.create(createProductionRequest));

        assertEquals("Animal not found", exception.getMessage());
    }

    private void authenticate(UUID userId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(
                        new AuthenticatedUser(userId, List.of(role)),
                        null,
                        role));
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
        animalRepositoryHandler.add("animal-1", "TAG-001");
        productionRepositoryHandler.store(productionEntity);

        List<ProductionResponse> responses = productionService.findAll(null, null);

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
        assertEquals("animal-1", responses.get(0).getAnimalId());
        assertEquals(LocalDate.of(2026, 3, 20), responses.get(0).getDate());
        assertEquals(12.5, responses.get(0).getQuantity());
        assertNotNull(responses.get(0).getAnimal());
        assertEquals("TAG-001", responses.get(0).getAnimal().getTag());
    }

    @Test
    void shouldHideInactiveProductionsFromDefaultQueries() {
        productionRepositoryHandler.store(new ProductionEntity(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5,
                "11111111-1111-1111-1111-111111111111",
                ProductionEntity.STATUS_ACTIVE));
        productionRepositoryHandler.store(new ProductionEntity(
                "production-2",
                "animal-1",
                LocalDate.of(2026, 3, 21),
                10.0,
                "11111111-1111-1111-1111-111111111111",
                ProductionEntity.STATUS_INACTIVE));

        List<ProductionResponse> responses = productionService.findAll(null, null);

        assertEquals(1, responses.size());
        assertEquals("production-1", responses.get(0).getId());
    }

    @Test
    void shouldReturnProductionWithAnimalSummary() {
        animalRepositoryHandler.add("animal-1", "TAG-001");
        productionRepositoryHandler.store(productionEntity);

        ProductionResponse response = productionService.findById("production-1");

        assertNotNull(response);
        assertEquals("production-1", response.getId());
        assertEquals("animal-1", response.getAnimalId());
        assertNotNull(response.getAnimal());
        assertEquals("animal-1", response.getAnimal().getId());
        assertEquals("TAG-001", response.getAnimal().getTag());
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
        animalRepositoryHandler.add("animal-1");
        animalRepositoryHandler.add("animal-2");
        productionRepositoryHandler.store(productionEntity);

        ProductionResponse response = productionService.update(
                "production-1",
                new UpdateProductionRequest("animal-2", LocalDate.of(2026, 3, 21), 15.0));

        assertNotNull(response);
        assertEquals("production-1", response.getId());
        assertEquals("animal-2", response.getAnimalId());
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

    @Test
    void shouldFailWhenUpdatingInactiveProduction() {
        productionRepositoryHandler.store(new ProductionEntity(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5,
                "11111111-1111-1111-1111-111111111111",
                ProductionEntity.STATUS_INACTIVE));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> productionService.update(
                        "production-1",
                        new UpdateProductionRequest(LocalDate.of(2026, 3, 21), 15.0)));

        assertEquals("Inactive production cannot be updated", exception.getMessage());
    }

    @Test
    void shouldSoftDeleteProduction() {
        productionRepositoryHandler.store(productionEntity);

        productionService.deleteProduction("production-1");

        assertEquals(0, productionService.findAll(null, null).size());
        assertEquals(
                ProductionEntity.STATUS_INACTIVE,
                productionRepositoryHandler.getRequired("production-1").getStatus());
    }

    @Test
    void shouldIgnoreDeleteWhenProductionAlreadyInactive() {
        productionRepositoryHandler.store(new ProductionEntity(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5,
                "11111111-1111-1111-1111-111111111111",
                ProductionEntity.STATUS_INACTIVE));

        productionService.deleteProduction("production-1");

        assertEquals(
                ProductionEntity.STATUS_INACTIVE,
                productionRepositoryHandler.getRequired("production-1").getStatus());
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
                                List<ProductionEntity> all = data.values().stream()
                                        .filter(entity -> ProductionEntity.STATUS_ACTIVE.equals(entity.getStatus()))
                                        .toList();
                                return paginate(all, pageable);
                            }
                            return data.values().stream()
                                    .filter(entity -> ProductionEntity.STATUS_ACTIVE.equals(entity.getStatus()))
                                    .toList();
                        }
                        if ("findById".equals(methodName)) {
                            return Optional.ofNullable(data.get(args[0]))
                                    .filter(entity -> ProductionEntity.STATUS_ACTIVE.equals(entity.getStatus()));
                        }
                        if ("findAnyById".equals(methodName)) {
                            return Optional.ofNullable(data.get(args[0]));
                        }
                        if ("findByAnimalIdAndStatus".equals(methodName)) {
                            List<ProductionEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getAnimalId().equals(args[0]))
                                    .filter(entity -> entity.getStatus().equals(args[1]))
                                    .toList();
                            if (args.length == 3) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[2]);
                            }
                            return filtered;
                        }
                        if ("findByDateAndStatus".equals(methodName)) {
                            List<ProductionEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getDate().equals(args[0]))
                                    .filter(entity -> entity.getStatus().equals(args[1]))
                                    .toList();
                            if (args.length == 3) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[2]);
                            }
                            return filtered;
                        }
                        if ("findByAnimalIdAndDateAndStatus".equals(methodName)) {
                            List<ProductionEntity> filtered = data.values().stream()
                                    .filter(entity -> entity.getAnimalId().equals(args[0]))
                                    .filter(entity -> entity.getDate().equals(args[1]))
                                    .filter(entity -> entity.getStatus().equals(args[2]))
                                    .toList();
                            if (args.length == 4) {
                                return paginate(filtered, (org.springframework.data.domain.Pageable) args[3]);
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
            if (entity.getStatus() == null) {
                entity.setStatus(ProductionEntity.STATUS_ACTIVE);
            }
            data.put(entity.getId(), entity);
        }

        ProductionEntity getRequired(String id) {
            return data.get(id);
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

        private final Map<String, com.jpsoftware.farmapp.animal.entity.AnimalEntity> animalsById = new LinkedHashMap<>();

        void add(String id, String tag) {
            animalsById.put(id, com.jpsoftware.farmapp.animal.entity.AnimalEntity.builder()
                    .id(id)
                    .tag(tag)
                    .breed("Holstein")
                    .birthDate(LocalDate.of(2024, 1, 1))
                    .status("ACTIVE")
                    .farmId("farm-1")
                    .build());
        }

        void add(String id) {
            add(id, "TAG-" + id);
        }

        AnimalRepository createProxy() {
            return (AnimalRepository) Proxy.newProxyInstance(
                    AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{AnimalRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("existsById".equals(methodName)) {
                            return animalsById.containsKey(args[0]);
                        }
                        if ("findById".equals(methodName)) {
                            return Optional.ofNullable(animalsById.get(args[0]));
                        }
                        if ("findAllById".equals(methodName)) {
                            Collection<?> requestedIds = (Collection<?>) args[0];
                            return requestedIds.stream()
                                    .map(id -> animalsById.get(id))
                                    .filter(java.util.Objects::nonNull)
                                    .toList();
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
