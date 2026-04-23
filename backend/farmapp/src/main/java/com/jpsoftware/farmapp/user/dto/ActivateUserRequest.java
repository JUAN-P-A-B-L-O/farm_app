package com.jpsoftware.farmapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for activating a user.")
public class ActivateUserRequest {

    @Schema(
            description = "Optional password to set when reactivating the user.",
            example = "farmapp@123")
    private String password;

    public ActivateUserRequest() {
    }

    public ActivateUserRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
