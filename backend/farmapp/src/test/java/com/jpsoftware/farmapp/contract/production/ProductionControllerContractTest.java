package com.jpsoftware.farmapp.contract.production;

// CONTRACT TEST - DO NOT MODIFY BEHAVIOR

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.production.controller.ProductionController;
import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionProfitResponse;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
import com.jpsoftware.farmapp.production.mapper.ProductionMapper;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.production.service.ProductionService;
import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductionControllerContractTest {

    private MockMvc mockMvc;
    private TestProductionService productionService;

    @BeforeEach
    void setUp() {
        productionService = new TestProductionService();
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductionController(productionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnAllProductions() throws Exception {
        productionService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/productions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("production-1"))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].quantity").value(12.5));
    }

    @Test
    void shouldReturnFilteredByAnimalId() throws Exception {
        productionService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/productions").param("animalId", "animal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("production-1"))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"));
    }

    @Test
    void shouldReturnFilteredByDate() throws Exception {
        productionService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/productions").param("date", "2026-03-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("production-1"))
                .andExpect(jsonPath("$[0].date[0]").value(2026))
                .andExpect(jsonPath("$[0].date[1]").value(3))
                .andExpect(jsonPath("$[0].date[2]").value(20));
    }

    @Test
    void shouldExportProductions() throws Exception {
        productionService.exportResponse = "id,animalId\nproduction-1,animal-1\n";

        mockMvc.perform(get("/productions/export")
                        .param("animalId", "animal-1")
                        .param("date", "2026-03-20")
                        .param("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("productions.csv")))
                .andExpect(content().string("id,animalId\nproduction-1,animal-1\n"));
    }

    @Test
    void shouldReturnSummary() throws Exception {
        productionService.summaryResponse = new ProductionSummaryResponse("123", 35.5);

        mockMvc.perform(get("/productions/summary/by-animal").param("animalId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalId").value("123"))
                .andExpect(jsonPath("$.totalQuantity").value(35.5));
    }

    @Test
    void shouldReturnProfit() throws Exception {
        productionService.profitResponse = new ProductionProfitResponse("123", 35.5, 20.0, 2.0, 71.0, 51.0);

        mockMvc.perform(get("/productions/summary/profit/by-animal").param("animalId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalId").value("123"))
                .andExpect(jsonPath("$.totalProduction").value(35.5))
                .andExpect(jsonPath("$.totalFeedingCost").value(20.0))
                .andExpect(jsonPath("$.milkPrice").value(2.0))
                .andExpect(jsonPath("$.revenue").value(71.0))
                .andExpect(jsonPath("$.profit").value(51.0));
    }

    @Test
    void shouldCreateProductionSuccessfully() throws Exception {
        String requestBody = """
                {
                  "animalId": "animal-1",
                  "date": "2026-03-20",
                  "quantity": 12.5,
                  "userId": "11111111-1111-1111-1111-111111111111"
                }
                """;

        productionService.createResponse = buildResponse();

        mockMvc.perform(post("/productions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("production-1"))
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.quantity").value(12.5));
    }

    @Test
    void shouldFailWhenQuantityIsInvalid() throws Exception {
        String requestBody = """
                {
                  "animalId": "animal-1",
                  "date": "2026-03-20",
                  "quantity": 0,
                  "userId": "11111111-1111-1111-1111-111111111111"
                }
                """;

        mockMvc.perform(post("/productions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("must be greater than 0"))
                .andExpect(jsonPath("$.path").value("/productions"));
    }

    @Test
    void shouldFailWhenAnimalNotFoundOnCreate() throws Exception {
        String requestBody = """
                {
                  "animalId": "missing-animal",
                  "date": "2026-03-20",
                  "quantity": 12.5,
                  "userId": "11111111-1111-1111-1111-111111111111"
                }
                """;

        productionService.createException = new ResourceNotFoundException("Animal not found");

        mockMvc.perform(post("/productions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Animal not found"))
                .andExpect(jsonPath("$.path").value("/productions"));
    }

    @Test
    void shouldUpdateProductionSuccessfully() throws Exception {
        String requestBody = """
                {
                  "date": "2026-03-21",
                  "quantity": 15.0
                }
                """;

        productionService.updateResponse = new ProductionResponse(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 21),
                15.0);

        mockMvc.perform(put("/productions/production-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("production-1"))
                .andExpect(jsonPath("$.date[0]").value(2026))
                .andExpect(jsonPath("$.date[1]").value(3))
                .andExpect(jsonPath("$.date[2]").value(21))
                .andExpect(jsonPath("$.quantity").value(15.0));
    }

    @Test
    void shouldFailWhenProductionIsNotFound() throws Exception {
        String requestBody = """
                {
                  "quantity": 15.0
                }
                """;

        productionService.updateException = new ResourceNotFoundException("Production not found");

        mockMvc.perform(put("/productions/missing-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Production not found"))
                .andExpect(jsonPath("$.path").value("/productions/missing-id"));
    }

    @Test
    void shouldFailWhenAnimalNotFound() throws Exception {
        productionService.summaryException = new ResourceNotFoundException("Animal not found");

        mockMvc.perform(get("/productions/summary/by-animal").param("animalId", "missing-animal"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Animal not found"))
                .andExpect(jsonPath("$.path").value("/productions/summary/by-animal"));
    }

    @Test
    void shouldFailWhenAnimalNotFoundForProfitSummary() throws Exception {
        productionService.profitException = new ResourceNotFoundException("Animal not found");

        mockMvc.perform(get("/productions/summary/profit/by-animal").param("animalId", "missing-animal"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Animal not found"))
                .andExpect(jsonPath("$.path").value("/productions/summary/profit/by-animal"));
    }

    @Test
    void shouldFailWhenAnimalIdIsMissingFromSummaryRequest() throws Exception {
        mockMvc.perform(get("/productions/summary/by-animal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.path").value("/productions/summary/by-animal"));
    }

    private ProductionResponse buildResponse() {
        return new ProductionResponse(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5);
    }

    private static class TestProductionService extends ProductionService {

        private ProductionResponse createResponse;
        private RuntimeException createException;
        private List<ProductionResponse> findAllResponse = List.of();
        private PaginatedResponse<ProductionResponse> paginatedResponse;
        private ProductionSummaryResponse summaryResponse;
        private ProductionProfitResponse profitResponse;
        private String exportResponse = "";
        private ProductionResponse updateResponse;
        private RuntimeException summaryException;
        private RuntimeException profitException;
        private RuntimeException updateException;
        private RuntimeException deleteException;

        TestProductionService() {
            super(
                    dummyProductionRepository(),
                    dummyFeedingRepository(),
                    dummyAnimalRepository(),
                    dummyUserRepository(),
                    new ProductionMapper(),
                    new AuthenticationContextService());
        }

        @Override
        public List<ProductionResponse> findAll(String animalId, LocalDate date, String farmId) {
            return findAllResponse;
        }

        @Override
        public String exportAll(String animalId, LocalDate date, String farmId) {
            return exportResponse;
        }

        @Override
        public PaginatedResponse<ProductionResponse> findAllPaginated(String animalId, LocalDate date, String farmId, int page, int size) {
            return paginatedResponse;
        }

        @Override
        public ProductionSummaryResponse getSummaryByAnimal(String animalId, String farmId) {
            if (summaryException != null) {
                throw summaryException;
            }
            return summaryResponse;
        }

        @Override
        public ProductionProfitResponse getProfitByAnimal(String animalId, String farmId, boolean includeAcquisitionCost) {
            if (profitException != null) {
                throw profitException;
            }
            return profitResponse;
        }

        @Override
        public ProductionResponse create(CreateProductionRequest request, String farmId) {
            if (createException != null) {
                throw createException;
            }
            return createResponse;
        }

        @Override
        public ProductionResponse update(String id, UpdateProductionRequest request, String farmId) {
            if (updateException != null) {
                throw updateException;
            }
            return updateResponse;
        }

        @Override
        public void deleteProduction(String id, String farmId) {
            if (deleteException != null) {
                throw deleteException;
            }
        }

        private static ProductionRepository dummyProductionRepository() {
            return (ProductionRepository) Proxy.newProxyInstance(
                    ProductionRepository.class.getClassLoader(),
                    new Class<?>[]{ProductionRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static com.jpsoftware.farmapp.feeding.repository.FeedingRepository dummyFeedingRepository() {
            return (com.jpsoftware.farmapp.feeding.repository.FeedingRepository) Proxy.newProxyInstance(
                    com.jpsoftware.farmapp.feeding.repository.FeedingRepository.class.getClassLoader(),
                    new Class<?>[]{com.jpsoftware.farmapp.feeding.repository.FeedingRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static com.jpsoftware.farmapp.animal.repository.AnimalRepository dummyAnimalRepository() {
            return (com.jpsoftware.farmapp.animal.repository.AnimalRepository) Proxy.newProxyInstance(
                    com.jpsoftware.farmapp.animal.repository.AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{com.jpsoftware.farmapp.animal.repository.AnimalRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static UserRepository dummyUserRepository() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[]{UserRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }
    }

    @Test
    void shouldReturnPaginatedProductions() throws Exception {
        productionService.paginatedResponse = new PaginatedResponse<>(
                List.of(buildResponse()),
                0,
                10,
                1,
                1);

        mockMvc.perform(get("/productions").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("production-1"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldRespectPageAndSize() throws Exception {
        productionService.paginatedResponse = new PaginatedResponse<>(
                List.of(
                        new ProductionResponse("production-2", "animal-1", LocalDate.of(2026, 3, 21), 15.0),
                        new ProductionResponse("production-3", "animal-1", LocalDate.of(2026, 3, 22), 16.0)),
                1,
                2,
                5,
                3);

        mockMvc.perform(get("/productions").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("production-2"))
                .andExpect(jsonPath("$.content[1].id").value("production-3"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldReturnEmptyPageWhenOutOfBounds() throws Exception {
        productionService.paginatedResponse = new PaginatedResponse<>(
                List.of(),
                5,
                10,
                2,
                1);

        mockMvc.perform(get("/productions").param("page", "5").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(5))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldFailWhenUpdatingInactiveProduction() throws Exception {
        productionService.updateException = new ConflictException("Inactive production cannot be updated");

        mockMvc.perform(put("/productions/production-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 15.0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Inactive production cannot be updated"))
                .andExpect(jsonPath("$.path").value("/productions/production-1"));
    }

    @Test
    void shouldDeleteProductionSuccessfully() throws Exception {
        mockMvc.perform(delete("/productions/production-1"))
                .andExpect(status().isNoContent());
    }
}
