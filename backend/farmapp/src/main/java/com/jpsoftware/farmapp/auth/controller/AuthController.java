package com.jpsoftware.farmapp.auth.controller;

import com.jpsoftware.farmapp.auth.dto.LoginRequest;
import com.jpsoftware.farmapp.auth.dto.LoginResponse;
import com.jpsoftware.farmapp.auth.dto.RegisterRequest;
import com.jpsoftware.farmapp.auth.dto.ResendConfirmationRequest;
import com.jpsoftware.farmapp.auth.service.AuthService;
import com.jpsoftware.farmapp.shared.dto.MessageResponse;
import com.jpsoftware.farmapp.shared.exception.ErrorResponse;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication operations.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Email not confirmed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/register")
    @Operation(summary = "Register account", description = "Creates a new manager account pending email confirmation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/confirm-email")
    @Operation(summary = "Confirm email", description = "Validates the email confirmation token and activates email access.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already confirmed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> confirmEmail(
            @RequestParam @NotBlank(message = "token must not be blank") String token) {
        return ResponseEntity.ok(authService.confirmEmail(token));
    }

    @PostMapping("/confirm-email/resend")
    @Operation(summary = "Resend confirmation email", description = "Generates a new confirmation token and resends the confirmation email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Confirmation email resent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already confirmed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> resendConfirmation(@Valid @RequestBody ResendConfirmationRequest request) {
        return ResponseEntity.ok(authService.resendConfirmation(request.getEmail()));
    }
}
