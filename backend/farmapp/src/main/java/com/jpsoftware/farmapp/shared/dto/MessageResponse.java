package com.jpsoftware.farmapp.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic message returned by the API.")
public class MessageResponse {

    @Schema(description = "Human-readable message.", example = "Operação realizada com sucesso.")
    private String message;

    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
