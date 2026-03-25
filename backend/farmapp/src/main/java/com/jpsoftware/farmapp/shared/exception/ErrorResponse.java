package com.jpsoftware.farmapp.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "ErrorResponse", description = "Standard error payload returned by the API.")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred.", example = "2026-03-25T14:30:00Z")
    private final Instant timestamp;

    @Schema(description = "HTTP status code.", example = "400")
    private final int status;

    @Schema(description = "Error message describing the failure.", example = "animalId must not be blank")
    private final String error;

    @Schema(description = "Request path that produced the error.", example = "/productions/summary/by-animal")
    private final String path;
}
