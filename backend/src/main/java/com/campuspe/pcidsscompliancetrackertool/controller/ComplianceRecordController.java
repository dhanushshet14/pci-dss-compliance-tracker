package com.campuspe.pcidsscompliancetrackertool.controller;

import com.campuspe.pcidsscompliancetrackertool.config.RoleConstants;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceRecordResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceStatsResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceUpdateRequestDTO;
import com.campuspe.pcidsscompliancetrackertool.service.ComplianceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance-records")
@Tag(name = "Compliance Records", description = "Endpoints for PCI-DSS compliance record management")
public class ComplianceRecordController {

    private final ComplianceRecordService complianceRecordService;

    public ComplianceRecordController(ComplianceRecordService complianceRecordService) {
        this.complianceRecordService = complianceRecordService;
    }

    @Operation(
            summary = "Update a compliance record",
            description = "Partially updates an existing compliance record. " +
                          "Only non-null fields in the request body are applied."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Record updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ComplianceRecordResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error in request body",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compliance record not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN_OR_MANAGER)
    public ResponseEntity<ComplianceRecordResponseDTO> updateRecord(
            @Parameter(description = "UUID of the compliance record", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ComplianceUpdateRequestDTO dto) {

        ComplianceRecordResponseDTO updated = complianceRecordService.updateRecord(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Soft-delete a compliance record",
            description = "Marks the record as deleted (isDeleted = true). " +
                          "The record is never physically removed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Record soft-deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compliance record not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> deleteRecord(
            @Parameter(description = "UUID of the compliance record", required = true)
            @PathVariable UUID id) {

        complianceRecordService.softDeleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Search compliance records",
            description = "Case-insensitive keyword search across title, description, " +
                          "and requirementId. Returns paginated results."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results returned successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search or pagination parameters",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/search")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<Page<ComplianceRecordResponseDTO>> searchRecords(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam("q") String q,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {

        Page<ComplianceRecordResponseDTO> results = complianceRecordService.searchRecords(q, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(
            summary = "Get compliance statistics",
            description = "Returns a summary with total records, count per status, " +
                          "average compliance score, and count of overdue items " +
                          "(dueDate before today and status ≠ COMPLIANT)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ComplianceStatsResponseDTO.class)
                    )
            )
    })
    @GetMapping("/stats")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ComplianceStatsResponseDTO> getStatistics() {

        ComplianceStatsResponseDTO stats = complianceRecordService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
