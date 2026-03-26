package com.jpsoftware.farmapp.contract.feeding;

// CONTRACT TEST - DO NOT MODIFY BEHAVIOR

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.controller.FeedingController;
import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
import com.jpsoftware.farmapp.feeding.mapper.FeedingMapper;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.feeding.service.FeedingService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;
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
        private FeedingResponse findByIdResponse;
        private RuntimeException findByIdException;

        TestFeedingService() {
            super(
                    dummyFeedingRepository(),
                    dummyAnimalRepository(),
                    dummyFeedTypeRepository(),
                    dummyUserRepository(),
                    new FeedingMapper());
        }

        @Override
        public FeedingResponse create(CreateFeedingRequest request) {
            return createResponse;
        }

        @Override
        public List<FeedingResponse> findAll(String animalId, LocalDate date) {
            return findAllResponse;
        }

        @Override
        public FeedingResponse findById(String id) {
            if (findByIdException != null) {
                throw findByIdException;
            }
            return findByIdResponse;
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
}
