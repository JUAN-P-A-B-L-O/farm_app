package com.jpsoftware.farmapp.contract.feeding;

// CONTRACT TEST - DO NOT MODIFY BEHAVIOR

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.controller.FeedingController;
import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
import com.jpsoftware.farmapp.feeding.dto.UpdateFeedingRequest;
import com.jpsoftware.farmapp.feeding.mapper.FeedingMapper;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.feeding.service.FeedingService;
import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
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

class FeedingControllerContractTest {

    private MockMvc mockMvc;
    private TestFeedingService feedingService;

    @BeforeEach
    void setUp() {
        feedingService = new TestFeedingService();
        mockMvc = MockMvcBuilders.standaloneSetup(new FeedingController(feedingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateFeedingSuccessfully() throws Exception {
        String requestBody = """
                {
                  "animalId": "animal-1",
                  "feedTypeId": "feed-type-1",
                  "date": "2026-03-24",
                  "quantity": 8.5,
                  "userId": "11111111-1111-1111-1111-111111111111"
                }
                """;

        feedingService.createResponse = buildResponse();

        mockMvc.perform(post("/feedings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("feeding-1"))
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value("feed-type-1"))
                .andExpect(jsonPath("$.date[0]").value(2026))
                .andExpect(jsonPath("$.date[1]").value(3))
                .andExpect(jsonPath("$.date[2]").value(24))
                .andExpect(jsonPath("$.quantity").value(8.5));
    }

    @Test
    void shouldReturnAllFeedings() throws Exception {
        feedingService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/feedings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("feeding-1"))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].feedTypeId").value("feed-type-1"))
                .andExpect(jsonPath("$[0].quantity").value(8.5));
    }

    @Test
    void shouldReturnAllFeedingsWhenNoFilter() throws Exception {
        feedingService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/feedings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("feeding-1"))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].feedTypeId").value("feed-type-1"))
                .andExpect(jsonPath("$[0].quantity").value(8.5));
    }

    @Test
    void shouldFilterByAnimalId() throws Exception {
        feedingService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/feedings").param("animalId", "animal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("feeding-1"))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].feedTypeId").value("feed-type-1"))
                .andExpect(jsonPath("$[0].quantity").value(8.5));
    }

    @Test
    void shouldFilterByDate() throws Exception {
        feedingService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/feedings").param("date", "2026-03-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("feeding-1"))
                .andExpect(jsonPath("$[0].date[0]").value(2026))
                .andExpect(jsonPath("$[0].date[1]").value(3))
                .andExpect(jsonPath("$[0].date[2]").value(24));
    }

    @Test
    void shouldFilterByAnimalIdAndDate() throws Exception {
        feedingService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/feedings")
                        .param("animalId", "animal-1")
                        .param("date", "2026-03-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("feeding-1"))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].date[0]").value(2026))
                .andExpect(jsonPath("$[0].date[1]").value(3))
                .andExpect(jsonPath("$[0].date[2]").value(24));
    }

    @Test
    void shouldExportFeedings() throws Exception {
        feedingService.exportResponse = "id,animalId\nfeeding-1,animal-1\n";

        mockMvc.perform(get("/feedings/export")
                        .param("animalId", "animal-1")
                        .param("date", "2026-03-24")
                        .param("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("feedings.csv")))
                .andExpect(content().string("id,animalId\nfeeding-1,animal-1\n"));
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() throws Exception {
        feedingService.findAllResponse = List.of();

        mockMvc.perform(get("/feedings")
                        .param("animalId", "missing-animal")
                        .param("date", "2026-03-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnFeedingById() throws Exception {
        feedingService.findByIdResponse = buildResponse();

        mockMvc.perform(get("/feedings/feeding-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("feeding-1"))
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value("feed-type-1"));
    }

    @Test
    void shouldFailWhenFeedingNotFound() throws Exception {
        feedingService.findByIdException = new ResourceNotFoundException("Feeding not found");

        mockMvc.perform(get("/feedings/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Feeding not found"))
                .andExpect(jsonPath("$.path").value("/feedings/missing-id"));
    }

    @Test
    void shouldFailWhenUserIdIsMissing() throws Exception {
        String requestBody = """
                {
                  "animalId": "animal-1",
                  "feedTypeId": "feed-type-1",
                  "date": "2026-03-24",
                  "quantity": 8.5,
                  "userId": " "
                }
                """;

        mockMvc.perform(post("/feedings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("userId must not be blank"))
                .andExpect(jsonPath("$.path").value("/feedings"));
    }

    private FeedingResponse buildResponse() {
        return new FeedingResponse(
                "feeding-1",
                "animal-1",
                "feed-type-1",
                LocalDate.of(2026, 3, 24),
                8.5);
    }

    private static class TestFeedingService extends FeedingService {

        private FeedingResponse createResponse;
        private List<FeedingResponse> findAllResponse = List.of();
        private PaginatedResponse<FeedingResponse> paginatedResponse;
        private FeedingResponse findByIdResponse;
        private String exportResponse = "";
        private RuntimeException findByIdException;
        private FeedingResponse updateResponse;
        private RuntimeException updateException;
        private RuntimeException deleteException;

        TestFeedingService() {
            super(
                    dummyFeedingRepository(),
                    dummyAnimalRepository(),
                    dummyFeedTypeRepository(),
                    dummyUserRepository(),
                    new FeedingMapper(),
                    new AuthenticationContextService());
        }

        @Override
        public FeedingResponse create(CreateFeedingRequest request, String farmId) {
            return createResponse;
        }

        @Override
        public List<FeedingResponse> findAll(String search, String animalId, String feedTypeId, LocalDate date, String farmId) {
            return findAllResponse;
        }

        @Override
        public String exportAll(String search, String animalId, String feedTypeId, LocalDate date, String farmId) {
            return exportResponse;
        }

        @Override
        public PaginatedResponse<FeedingResponse> findAllPaginated(
                String search,
                String animalId,
                String feedTypeId,
                LocalDate date,
                String farmId,
                int page,
                int size) {
            return paginatedResponse;
        }

        @Override
        public FeedingResponse findById(String id, String farmId) {
            if (findByIdException != null) {
                throw findByIdException;
            }
            return findByIdResponse;
        }

        @Override
        public FeedingResponse updateFeeding(String id, UpdateFeedingRequest request, String farmId) {
            if (updateException != null) {
                throw updateException;
            }
            return updateResponse;
        }

        @Override
        public void deleteFeeding(String id, String farmId) {
            if (deleteException != null) {
                throw deleteException;
            }
        }

        private static FeedingRepository dummyFeedingRepository() {
            return (FeedingRepository) Proxy.newProxyInstance(
                    FeedingRepository.class.getClassLoader(),
                    new Class<?>[]{FeedingRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static AnimalRepository dummyAnimalRepository() {
            return (AnimalRepository) Proxy.newProxyInstance(
                    AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{AnimalRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static FeedTypeRepository dummyFeedTypeRepository() {
            return (FeedTypeRepository) Proxy.newProxyInstance(
                    FeedTypeRepository.class.getClassLoader(),
                    new Class<?>[]{FeedTypeRepository.class},
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
    void shouldReturnPaginatedFeedings() throws Exception {
        feedingService.paginatedResponse = new PaginatedResponse<>(
                List.of(buildResponse()),
                0,
                10,
                1,
                1);

        mockMvc.perform(get("/feedings").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("feeding-1"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldRespectPageAndSize() throws Exception {
        feedingService.paginatedResponse = new PaginatedResponse<>(
                List.of(
                        new FeedingResponse("feeding-2", "animal-1", "feed-type-1", LocalDate.of(2026, 3, 25), 9.0),
                        new FeedingResponse("feeding-3", "animal-1", "feed-type-1", LocalDate.of(2026, 3, 26), 10.0)),
                1,
                2,
                5,
                3);

        mockMvc.perform(get("/feedings").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("feeding-2"))
                .andExpect(jsonPath("$.content[1].id").value("feeding-3"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldReturnEmptyPageWhenOutOfBounds() throws Exception {
        feedingService.paginatedResponse = new PaginatedResponse<>(
                List.of(),
                5,
                10,
                2,
                1);

        mockMvc.perform(get("/feedings").param("page", "5").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(5))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldUpdateFeedingSuccessfully() throws Exception {
        feedingService.updateResponse = buildResponse();

        mockMvc.perform(put("/feedings/feeding-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "animalId": "animal-1",
                                  "feedTypeId": "feed-type-1",
                                  "date": "2026-03-24",
                                  "quantity": 8.5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("feeding-1"))
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value("feed-type-1"));
    }

    @Test
    void shouldFailToUpdateInactiveFeeding() throws Exception {
        feedingService.updateException = new ConflictException("Inactive feeding cannot be updated");

        mockMvc.perform(put("/feedings/feeding-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 9.0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Inactive feeding cannot be updated"))
                .andExpect(jsonPath("$.path").value("/feedings/feeding-1"));
    }

    @Test
    void shouldDeleteFeedingSuccessfully() throws Exception {
        mockMvc.perform(delete("/feedings/feeding-1"))
                .andExpect(status().isNoContent());
    }
}
