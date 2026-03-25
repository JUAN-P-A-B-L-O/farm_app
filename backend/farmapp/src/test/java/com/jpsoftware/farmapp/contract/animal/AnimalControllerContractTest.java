package com.jpsoftware.farmapp.contract.animal;

// CONTRACT TEST - DO NOT MODIFY BEHAVIOR

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.controller.AnimalController;
import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.mapper.AnimalMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.animal.service.AnimalService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AnimalControllerContractTest {

    private MockMvc mockMvc;
    private TestAnimalService animalService;

    @BeforeEach
    void setUp() {
        animalService = new TestAnimalService();
        mockMvc = MockMvcBuilders.standaloneSetup(new AnimalController(animalService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateAnimalSuccessfully() throws Exception {
        animalService.createResponse = buildResponse();
        String requestBody = """
                {
                  "tag": "TAG-001",
                  "breed": "Angus",
                  "birthDate": "2022-01-10",
                  "farmId": "FARM-001"
                }
                """;

        mockMvc.perform(post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("animal-1"))
                .andExpect(jsonPath("$.tag").value("TAG-001"));

        assertEquals("TAG-001", animalService.lastCreateRequest.getTag());
    }

    @Test
    void shouldFailWhenCreateAnimalPayloadIsInvalid() throws Exception {
        String requestBody = """
                {
                  "tag": " ",
                  "breed": "Angus",
                  "birthDate": "2022-01-10",
                  "farmId": "FARM-001"
                }
                """;

        mockMvc.perform(post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.path").value("/animals"));
    }

    @Test
    void shouldReturnAllAnimals() throws Exception {
        animalService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("animal-1"))
                .andExpect(jsonPath("$[0].breed").value("Angus"));

        assertEquals(1, animalService.findAllCalls);
    }

    @Test
    void shouldReturnAnimalById() throws Exception {
        animalService.findByIdResponse = buildResponse();

        mockMvc.perform(get("/animals/animal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("animal-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertEquals("animal-1", animalService.lastFindById);
    }

    @Test
    void shouldFailWhenAnimalNotFound() throws Exception {
        animalService.findByIdException = new ResourceNotFoundException("Animal not found");

        mockMvc.perform(get("/animals/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Animal not found"))
                .andExpect(jsonPath("$.path").value("/animals/missing-id"));

        assertEquals("missing-id", animalService.lastFindById);
    }

    @Test
    void shouldUpdateAnimalSuccessfully() throws Exception {
        String requestBody = """
                {
                  "tag": "TAG-002",
                  "breed": "Nelore",
                  "birthDate": "2021-05-20",
                  "status": "INACTIVE",
                  "farmId": "FARM-002"
                }
                """;
        animalService.updateResponse = AnimalResponse.builder()
                .id("animal-1")
                .tag("TAG-002")
                .breed("Nelore")
                .birthDate(LocalDate.of(2021, 5, 20))
                .status("INACTIVE")
                .farmId("FARM-002")
                .build();

        mockMvc.perform(put("/animals/animal-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tag").value("TAG-002"))
                .andExpect(jsonPath("$.farmId").value("FARM-002"));

        assertEquals("animal-1", animalService.lastUpdatedId);
        assertEquals("TAG-002", animalService.lastUpdateRequest.getTag());
    }

    @Test
    void shouldDeleteAnimalSuccessfully() throws Exception {
        mockMvc.perform(delete("/animals/animal-1"))
                .andExpect(status().isNoContent());

        assertEquals("animal-1", animalService.lastDeletedId);
    }

    private AnimalResponse buildResponse() {
        return AnimalResponse.builder()
                .id("animal-1")
                .tag("TAG-001")
                .breed("Angus")
                .birthDate(LocalDate.of(2022, 1, 10))
                .status("ACTIVE")
                .farmId("FARM-001")
                .build();
    }

    private static class TestAnimalService extends AnimalService {

        private AnimalResponse createResponse;
        private List<AnimalResponse> findAllResponse = List.of();
        private AnimalResponse findByIdResponse;
        private RuntimeException findByIdException;
        private AnimalResponse updateResponse;
        private String lastFindById;
        private String lastUpdatedId;
        private String lastDeletedId;
        private int findAllCalls;
        private CreateAnimalRequest lastCreateRequest;
        private UpdateAnimalRequest lastUpdateRequest;

        TestAnimalService() {
            super(dummyRepository(), new AnimalMapper());
        }

        @Override
        public AnimalResponse create(CreateAnimalRequest request) {
            lastCreateRequest = request;
            return createResponse;
        }

        @Override
        public List<AnimalResponse> findAll(String farmId) {
            findAllCalls++;
            return findAllResponse;
        }

        @Override
        public AnimalResponse findById(String id) {
            lastFindById = id;
            if (findByIdException != null) {
                throw findByIdException;
            }
            return findByIdResponse;
        }

        @Override
        public AnimalResponse update(String id, UpdateAnimalRequest request) {
            lastUpdatedId = id;
            lastUpdateRequest = request;
            return updateResponse;
        }

        @Override
        public void delete(String id) {
            lastDeletedId = id;
        }

        private static AnimalRepository dummyRepository() {
            return (AnimalRepository) Proxy.newProxyInstance(
                    AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{AnimalRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }
    }
}
