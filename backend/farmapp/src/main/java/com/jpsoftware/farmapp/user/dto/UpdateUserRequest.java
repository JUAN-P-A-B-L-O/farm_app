package com.jpsoftware.farmapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request payload for updating a user.")
public class UpdateUserRequest {

    @NotBlank(message = "name must not be blank")
    @Schema(description = "User full name.", example = "Maria Silva")
    private String name;

    @NotBlank(message = "email must not be blank")
    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
    private String email;

    @NotBlank(message = "role must not be blank")
    @Schema(description = "User role in the system.", example = "MANAGER")
    private String role;

    @NotEmpty(message = "farmIds must not be empty")
    @Schema(description = "Farm identifiers assigned to the user.", example = "[\"farm-001\"]")
    private List<String> farmIds;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String name, String email, String role, List<String> farmIds) {
        this.name = name;
        this.email = email;
        this.role = role;
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

    public List<String> getFarmIds() {
        return farmIds;
    }

    public void setFarmIds(List<String> farmIds) {
        this.farmIds = farmIds;
    }
}
