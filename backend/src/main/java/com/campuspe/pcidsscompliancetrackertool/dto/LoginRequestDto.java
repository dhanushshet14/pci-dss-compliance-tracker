package com.campuspe.pcidsscompliancetrackertool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for capturing login credentials from the client.
 * Validated before processing to ensure required fields are present.
 */
@Schema(description = "Request payload for user login")
public class LoginRequestDto {

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Registered username", example = "john_doe")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "Account password", example = "Secret@123")
    private String password;

    // ── Constructors ──────────────────────────────────────────────────────────

    public LoginRequestDto() {}

    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
