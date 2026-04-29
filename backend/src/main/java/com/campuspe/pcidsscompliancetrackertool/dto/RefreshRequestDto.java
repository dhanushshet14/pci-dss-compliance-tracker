package com.campuspe.pcidsscompliancetrackertool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for the token-refresh endpoint.
 * Carries the previously issued refresh token sent back by the client.
 */
@Schema(description = "Request payload for refreshing an access token")
public class RefreshRequestDto {

    @NotBlank(message = "Refresh token must not be blank")
    @Schema(description = "The refresh token issued during login", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    // ── Constructors ──────────────────────────────────────────────────────────

    public RefreshRequestDto() {}

    public RefreshRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
