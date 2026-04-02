package com.jpsoftware.farmapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

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

    public CreateUserRequest() {
    }

    public CreateUserRequest(String name, String email, String role) {
        this(name, email, role, null);
    }

    public CreateUserRequest(String name, String email, String role, String password) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
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
}
