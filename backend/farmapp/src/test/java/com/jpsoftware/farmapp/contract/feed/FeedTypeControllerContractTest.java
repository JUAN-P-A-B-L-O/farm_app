package com.jpsoftware.farmapp.contract.feed;

// CONTRACT TEST - DO NOT MODIFY BEHAVIOR

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.feed.controller.FeedTypeController;
import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.mapper.FeedTypeMapper;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feed.service.FeedTypeService;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.lang.reflect.Proxy;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FeedTypeControllerContractTest {

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
                        .queryParam("farmId", "farm-1")
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

        mockMvc.perform(get("/feed-types").queryParam("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("feed-type-1"))
                .andExpect(jsonPath("$[0].name").value("Corn Silage"))
                .andExpect(jsonPath("$[0].costPerKg").value(1.75))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldReturnPaginatedFeedTypes() throws Exception {
        feedTypeService.paginatedResponse = new PaginatedResponse<>(
                List.of(buildResponse()),
                0,
                10,
                1,
                1);

        mockMvc.perform(get("/feed-types")
                        .queryParam("farmId", "farm-1")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("feed-type-1"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldExportFeedTypes() throws Exception {
        feedTypeService.exportResponse = "id,name\nfeed-type-1,Corn Silage\n";

        mockMvc.perform(get("/feed-types/export").queryParam("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("feed-types.csv")))
                .andExpect(content().string("id,name\nfeed-type-1,Corn Silage\n"));
    }

    @Test
    void shouldReturnById() throws Exception {
        feedTypeService.findByIdResponse = buildResponse();

        mockMvc.perform(get("/feed-types/feed-type-1").queryParam("farmId", "farm-1"))
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
                .andExpect(jsonPath("$.error").value("Tipo de ração não encontrado."))
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
        private PaginatedResponse<FeedTypeResponse> paginatedResponse = new PaginatedResponse<>(List.of(), 0, 10, 0, 0);
        private FeedTypeResponse findByIdResponse;
        private String exportResponse = "";
        private RuntimeException findByIdException;

        TestFeedTypeService() {
            super(dummyRepository(), new FeedTypeMapper(), dummyFarmAccessService());
        }

        @Override
        public FeedTypeResponse create(CreateFeedTypeRequest request, String farmId) {
            return createResponse;
        }

        @Override
        public List<FeedTypeResponse> findAll(String farmId, String search) {
            return findAllResponse;
        }

        @Override
        public PaginatedResponse<FeedTypeResponse> findAllPaginated(String farmId, String search, int page, int size) {
            return paginatedResponse;
        }

        @Override
        public String exportAll(String farmId, String search, String currency) {
            return exportResponse;
        }

        @Override
        public FeedTypeResponse findById(String id, String farmId) {
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

        private static FarmAccessService dummyFarmAccessService() {
            return org.mockito.Mockito.mock(FarmAccessService.class);
        }
    }
}
