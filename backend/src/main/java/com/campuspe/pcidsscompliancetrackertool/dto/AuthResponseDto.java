package com.campuspe.pcidsscompliancetrackertool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard authentication response returned after a successful login or token refresh.
 * Contains the access token, an optional refresh token, token lifetime and the user's role.
 * {@code refreshToken} and {@code username}/{@code email} are omitted from the JSON when null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response with tokens and user metadata")
public class AuthResponseDto {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "JWT refresh token (returned only on login)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Token lifetime in seconds", example = "3600")
    private long expiresIn;

    @Schema(description = "Role assigned to the authenticated user", example = "VIEWER")
    private String role;

    /** Returned only on /auth/register (never contains the password). */
    @Schema(description = "Username of the newly registered user", example = "jane_doe")
    private String username;

    @Schema(description = "Email of the newly registered user", example = "jane.doe@company.com")
    private String email;

    // ── Constructors ──────────────────────────────────────────────────────────

    public AuthResponseDto() {}

    /** Used for login / refresh responses. */
    public AuthResponseDto(String token, String refreshToken, long expiresIn, String role) {
        this.token        = token;
        this.refreshToken = refreshToken;
        this.expiresIn    = expiresIn;
        this.role         = role;
    }

    /** Used for the registration response (no refresh token needed). */
    public AuthResponseDto(String username, String email, String role) {
        this.username = username;
        this.email    = email;
        this.role     = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
