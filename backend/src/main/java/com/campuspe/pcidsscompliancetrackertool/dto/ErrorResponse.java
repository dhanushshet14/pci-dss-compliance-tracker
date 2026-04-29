package com.campuspe.pcidsscompliancetrackertool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error envelope returned by the API for all 4xx/5xx responses.
 * {@code details} is omitted from JSON when null (e.g. single-message errors).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response body")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "401")
    private int status;

    @Schema(description = "Short error category", example = "Unauthorized")
    private String error;

    @Schema(description = "Human-readable error message", example = "Invalid username or password.")
    private String message;

    @Schema(description = "ISO-8601 timestamp of when the error occurred", example = "2026-04-29T18:30:00Z")
    private String timestamp;

    @Schema(description = "Field-level validation errors (present only on 400 responses)")
    private List<String> details;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponse(int status, String error, String message, List<String> details) {
        this(status, error, message);
        this.details = details;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
}
