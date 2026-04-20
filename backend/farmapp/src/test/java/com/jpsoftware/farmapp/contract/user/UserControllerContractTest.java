package com.jpsoftware.farmapp.contract.user;

// CONTRACT TEST - DO NOT MODIFY BEHAVIOR

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.user.controller.UserController;
import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import com.jpsoftware.farmapp.user.service.UserService;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerContractTest {

    private MockMvc mockMvc;
    private TestUserService userService;

    @BeforeEach
    void setUp() {
        userService = new TestUserService();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        String requestBody = """
                {
                  "name": "Jane Doe",
                  "email": "jane@farm.com",
                  "role": "MANAGER",
                  "password": "farmapp@123",
                  "active": true,
                  "farmIds": ["farm-1"]
                }
                """;

        userService.createResponse = buildResponse();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@farm.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void shouldReturnAll() throws Exception {
        userService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].email").value("jane@farm.com"))
                .andExpect(jsonPath("$[0].role").value("MANAGER"));
    }

    @Test
    void shouldReturnById() throws Exception {
        userService.findByIdResponse = buildResponse();

        mockMvc.perform(get("/users/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@farm.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        userService.findByIdException = new ResourceNotFoundException("User not found");

        mockMvc.perform(get("/users/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User not found"))
                .andExpect(jsonPath("$.path").value("/users/11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void shouldReturn400WhenInvalidInput() throws Exception {
        String requestBody = """
                {
                  "name": " ",
                  "email": " ",
                  "role": "MANAGER",
                  "active": true,
                  "farmIds": ["farm-1"]
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private UserResponse buildResponse() {
        return new UserResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Jane Doe",
                "jane@farm.com",
                "MANAGER");
    }

    private static class TestUserService extends UserService {

        private UserResponse createResponse;
        private List<UserResponse> findAllResponse = List.of();
        private UserResponse findByIdResponse;
        private RuntimeException findByIdException;

        TestUserService() {
            super(
                    dummyRepository(),
                    dummyAssignmentRepository(),
                    dummyFarmRepository(),
                    new UserMapper(),
                    new BCryptPasswordEncoder(),
                    dummyAuthenticationContextService());
        }

        @Override
        public UserResponse create(CreateUserRequest request) {
            return createResponse;
        }

        @Override
        public List<UserResponse> findAll() {
            return findAllResponse;
        }

        @Override
        public UserResponse findById(String id) {
            if (findByIdException != null) {
                throw findByIdException;
            }
            return findByIdResponse;
        }

        private static UserRepository dummyRepository() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[]{UserRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static UserFarmAssignmentRepository dummyAssignmentRepository() {
            return (UserFarmAssignmentRepository) Proxy.newProxyInstance(
                    UserFarmAssignmentRepository.class.getClassLoader(),
                    new Class<?>[]{UserFarmAssignmentRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static FarmRepository dummyFarmRepository() {
            return (FarmRepository) Proxy.newProxyInstance(
                    FarmRepository.class.getClassLoader(),
                    new Class<?>[]{FarmRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static AuthenticationContextService dummyAuthenticationContextService() {
            return new AuthenticationContextService();
        }
    }
}
