package com.campuspe.pcidsscompliancetrackertool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for capturing new user registration details.
 * Validated before processing; password is never stored in plain text.
 */
@Schema(description = "Request payload for user registration")
public class RegisterRequestDto {

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Desired username (unique)", example = "jane_doe")
    private String username;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Must be a valid email address")
    @Schema(description = "User's email address", example = "jane.doe@company.com")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "Plain-text password (will be BCrypt-hashed before storage)", example = "Secret@123")
    private String password;

    // ── Constructors ──────────────────────────────────────────────────────────

    public RegisterRequestDto() {}

    public RegisterRequestDto(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
