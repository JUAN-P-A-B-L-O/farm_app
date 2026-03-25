package com.jpsoftware.farmapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "User data returned by the API.")
public class UserResponse {

    @Schema(description = "User identifier.", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "User full name.", example = "Maria Silva")
    private String name;

    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
    private String email;

    @Schema(description = "User role in the system.", example = "MANAGER")
    private String role;

    public UserResponse() {
    }

    public UserResponse(UUID id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
}
