package com.jpsoftware.farmapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Request payload for creating a user.")
public class CreateUserRequest {

    @NotBlank(message = "name must not be blank")
    @Schema(description = "User full name.", example = "Maria Silva")
    private String name;

    @NotBlank(message = "email must not be blank")
    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
    private String email;

    @NotBlank(message = "role must not be blank")
    @Schema(description = "User role in the system.", example = "MANAGER")
    private String role;

    @Schema(description = "User password for authentication.", example = "farmapp@123")
    private String password;

    @NotNull(message = "active must not be null")
    @Schema(description = "Whether the user can authenticate.", example = "true")
    private Boolean active;

    @NotEmpty(message = "farmIds must not be empty")
    @Schema(description = "Farm identifiers assigned to the user.", example = "[\"farm-001\"]")
    private List<String> farmIds;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String name, String email, String role) {
        this(name, email, role, null, true, List.of());
    }

    public CreateUserRequest(String name, String email, String role, String password) {
        this(name, email, role, password, true, List.of());
    }

    public CreateUserRequest(
            String name,
            String email,
            String role,
            String password,
            Boolean active,
            List<String> farmIds) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
        this.active = active;
        this.farmIds = farmIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getFarmIds() {
        return farmIds;
    }

    public void setFarmIds(List<String> farmIds) {
        this.farmIds = farmIds;
    }
}
