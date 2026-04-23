package com.jpsoftware.farmapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for updating the authenticated user's password.")
public class UpdatePasswordRequest {

    @NotBlank(message = "currentPassword must not be blank")
    @Schema(description = "Current user password.", example = "farmapp@123")
    private String currentPassword;

    @NotBlank(message = "newPassword must not be blank")
    @Schema(description = "New user password.", example = "farmapp@456")
    private String newPassword;

    public UpdatePasswordRequest() {
    }

    public UpdatePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
