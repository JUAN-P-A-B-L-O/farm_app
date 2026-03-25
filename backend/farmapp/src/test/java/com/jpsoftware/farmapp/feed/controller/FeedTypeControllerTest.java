package com.jpsoftware.farmapp.feed.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.mapper.FeedTypeMapper;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feed.service.FeedTypeService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FeedTypeControllerTest {

    private MockMvc mockMvc;
    private TestFeedTypeService feedTypeService;

    @BeforeEach
    void setUp() {
        feedTypeService = new TestFeedTypeService();
        mockMvc = MockMvcBuilders.standaloneSetup(new FeedTypeController(feedTypeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateFeedType() throws Exception {
        String requestBody = """
                {
                  "name": "Corn Silage",
                  "costPerKg": 1.75
                }
                """;

        feedTypeService.createResponse = buildResponse();

        mockMvc.perform(post("/feed-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("feed-type-1"))
                .andExpect(jsonPath("$.name").value("Corn Silage"))
                .andExpect(jsonPath("$.costPerKg").value(1.75))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnAll() throws Exception {
        feedTypeService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/feed-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("feed-type-1"))
                .andExpect(jsonPath("$[0].name").value("Corn Silage"))
                .andExpect(jsonPath("$[0].costPerKg").value(1.75))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldReturnById() throws Exception {
        feedTypeService.findByIdResponse = buildResponse();

        mockMvc.perform(get("/feed-types/feed-type-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("feed-type-1"))
                .andExpect(jsonPath("$.name").value("Corn Silage"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        feedTypeService.findByIdException = new ResourceNotFoundException("Feed type not found");

        mockMvc.perform(get("/feed-types/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Feed type not found"))
                .andExpect(jsonPath("$.path").value("/feed-types/missing-id"));
    }

    @Test
    void shouldReturn400WhenInvalidInput() throws Exception {
        String requestBody = """
                {
                  "name": " ",
                  "costPerKg": 0
                }
                """;

        mockMvc.perform(post("/feed-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private FeedTypeResponse buildResponse() {
        return new FeedTypeResponse("feed-type-1", "Corn Silage", 1.75, true);
    }

    private static class TestFeedTypeService extends FeedTypeService {

        private FeedTypeResponse createResponse;
        private List<FeedTypeResponse> findAllResponse = List.of();
        private FeedTypeResponse findByIdResponse;
        private RuntimeException findByIdException;

        TestFeedTypeService() {
            super(dummyRepository(), new FeedTypeMapper());
        }

        @Override
        public FeedTypeResponse create(CreateFeedTypeRequest request) {
            return createResponse;
        }

        @Override
        public List<FeedTypeResponse> findAll() {
            return findAllResponse;
        }

        @Override
        public FeedTypeResponse findById(String id) {
            if (findByIdException != null) {
                throw findByIdException;
            }
            return findByIdResponse;
        }

        private static FeedTypeRepository dummyRepository() {
            return (FeedTypeRepository) Proxy.newProxyInstance(
                    FeedTypeRepository.class.getClassLoader(),
                    new Class<?>[]{FeedTypeRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }
    }
}
