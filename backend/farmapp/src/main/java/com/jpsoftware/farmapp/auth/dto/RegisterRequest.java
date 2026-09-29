package com.jpsoftware.farmapp.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating a new account.")
public class RegisterRequest {

    @NotBlank(message = "name must not be blank")
    @Schema(description = "User full name.", example = "Maria Silva")
    private String name;

    @NotBlank(message = "email must not be blank")
    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
    private String email;

    @NotBlank(message = "password must not be blank")
    @Schema(description = "User password for authentication.", example = "farmapp@123")
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
