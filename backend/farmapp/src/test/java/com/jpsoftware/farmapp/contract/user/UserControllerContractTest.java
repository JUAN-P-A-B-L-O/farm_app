package com.jpsoftware.farmapp.contract.user;

// CONTRACT TEST - DO NOT MODIFY BEHAVIOR

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.user.controller.UserController;
import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.repository.FarmRepository;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.user.dto.ActivateUserRequest;
import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UpdatePasswordRequest;
import com.jpsoftware.farmapp.user.dto.UpdateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import com.jpsoftware.farmapp.user.service.UserService;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
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
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.farmIds[0]").value("farm-1"));
    }

    @Test
    void shouldReturnAll() throws Exception {
        userService.findAllResponse = List.of(buildResponse());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].email").value("jane@farm.com"))
                .andExpect(jsonPath("$[0].role").value("MANAGER"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].avatarUrl").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$[0].farmIds[0]").value("farm-1"));
    }

    @Test
    void shouldReturnPaginatedUsers() throws Exception {
        userService.paginatedResponse = new PaginatedResponse<>(
                List.of(buildResponse()),
                0,
                10,
                1,
                1);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldExportUsers() throws Exception {
        userService.exportResponse = "id,name\n11111111-1111-1111-1111-111111111111,Jane Doe\n";

        mockMvc.perform(get("/users/export")
                        .param("search", "Jane")
                        .param("active", "true")
                        .param("role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("users.csv")))
                .andExpect(content().string("id,name\n11111111-1111-1111-1111-111111111111,Jane Doe\n"));
    }

    @Test
    void shouldReturnById() throws Exception {
        userService.findByIdResponse = buildResponse();

        mockMvc.perform(get("/users/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@farm.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.farmIds[0]").value("farm-1"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        String requestBody = """
                {
                  "name": "Updated Jane Doe",
                  "email": "updated@farm.com",
                  "role": "WORKER",
                  "avatarUrl": "https://example.com/avatar-updated.png",
                  "farmIds": ["farm-1"]
                }
                """;

        userService.updateResponse = new UserResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Updated Jane Doe",
                "updated@farm.com",
                "WORKER",
                true,
                "https://example.com/avatar-updated.png",
                List.of("farm-1"));

        mockMvc.perform(put("/users/11111111-1111-1111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Jane Doe"))
                .andExpect(jsonPath("$.email").value("updated@farm.com"))
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar-updated.png"));
    }

    @Test
    void shouldInactivateUser() throws Exception {
        userService.inactivateResponse = new UserResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                false,
                "https://example.com/avatar.png",
                List.of("farm-1"));

        mockMvc.perform(patch("/users/11111111-1111-1111-1111-111111111111/inactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldActivateUser() throws Exception {
        String requestBody = """
                {
                  "password": "farmapp@456"
                }
                """;

        userService.activateResponse = new UserResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                true,
                "https://example.com/avatar.png",
                List.of("farm-1"));

        mockMvc.perform(patch("/users/11111111-1111-1111-1111-111111111111/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateOwnPassword() throws Exception {
        String requestBody = """
                {
                  "currentPassword": "farmapp@123",
                  "newPassword": "farmapp@456"
                }
                """;

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        userService.findByIdException = new ResourceNotFoundException("User not found");

        mockMvc.perform(get("/users/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Usuário não encontrado."))
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
                "MANAGER",
                true,
                "https://example.com/avatar.png",
                List.of("farm-1"));
    }

    private static class TestUserService extends UserService {

        private UserResponse createResponse;
        private List<UserResponse> findAllResponse = List.of();
        private PaginatedResponse<UserResponse> paginatedResponse = new PaginatedResponse<>(List.of(), 0, 10, 0, 0);
        private UserResponse findByIdResponse;
        private UserResponse updateResponse;
        private UserResponse inactivateResponse;
        private UserResponse activateResponse;
        private String exportResponse = "";
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
        public List<UserResponse> findAll(String search, Boolean active, String role) {
            return findAllResponse;
        }

        @Override
        public PaginatedResponse<UserResponse> findAllPaginated(String search, Boolean active, String role, int page, int size) {
            return paginatedResponse;
        }

        @Override
        public String exportAll(String search, Boolean active, String role) {
            return exportResponse;
        }

        @Override
        public UserResponse findById(String id) {
            if (findByIdException != null) {
                throw findByIdException;
            }
            return findByIdResponse;
        }

        @Override
        public UserResponse update(String id, UpdateUserRequest request) {
            return updateResponse;
        }

        @Override
        public UserResponse inactivate(String id) {
            return inactivateResponse;
        }

        @Override
        public UserResponse activate(String id, ActivateUserRequest request) {
            return activateResponse;
        }

        @Override
        public void delete(String id) {
        }

        @Override
        public void updateOwnPassword(UpdatePasswordRequest request) {
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
