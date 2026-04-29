package com.campuspe.pcidsscompliancetrackertool.controller;

import com.campuspe.pcidsscompliancetrackertool.config.RoleConstants;
import com.campuspe.pcidsscompliancetrackertool.entity.AuditLog;
import com.campuspe.pcidsscompliancetrackertool.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for querying the immutable audit trail.
 * All endpoints are restricted to {@code ADMIN} role.
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Trail", description = "Endpoints for viewing entity change history")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Returns the full audit history for a specific entity.
     *
     * @param entityType simple class name (e.g. "ComplianceRecord")
     * @param entityId   primary key of the entity (as string)
     * @return list of audit entries, most recent first
     */
    @Operation(
            summary     = "Get entity audit history",
            description = "Returns all audit log entries for the given entity type and ID, " +
                          "ordered by changedAt descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit history returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{entityType}/{entityId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<List<AuditLog>> getAuditHistory(
            @Parameter(description = "Entity type (e.g. ComplianceRecord)", required = true)
            @PathVariable String entityType,
            @Parameter(description = "Entity primary key", required = true)
            @PathVariable String entityId) {

        List<AuditLog> history = auditService.getAuditHistory(entityType, entityId);
        return ResponseEntity.ok(history);
    }

    /**
     * Returns paginated audit logs for all actions performed by a given user.
     *
     * @param username the changedBy username to filter on
     * @param pageable pagination parameters
     * @return page of audit entries
     */
    @Operation(
            summary     = "Get user activity log",
            description = "Returns paginated audit entries for a specific user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User activity returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/user/{username}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Page<AuditLog>> getUserActivity(
            @Parameter(description = "Username to look up", required = true)
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<AuditLog> activity = auditService.getUserActivity(username, pageable);
        return ResponseEntity.ok(activity);
    }
}
