package com.campuspe.pcidsscompliancetrackertool.controller;

import com.campuspe.pcidsscompliancetrackertool.BaseControllerTest;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceRecordResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceStatsResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceUpdateRequestDTO;
import com.campuspe.pcidsscompliancetrackertool.entity.ComplianceRecord;
import com.campuspe.pcidsscompliancetrackertool.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code ComplianceRecordController} and
 * {@code ComplianceExportController}.
 */
class ComplianceRecordControllerTest extends BaseControllerTest {

    private static final String BASE_URL    = "/api/v1/compliance-records";
    private static final UUID   RECORD_ID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID   MISSING_ID  = UUID.fromString("00000000-0000-0000-0000-999999999999");

    private ComplianceRecordResponseDTO sampleRecord;
    private ComplianceUpdateRequestDTO  updateDto;

    @BeforeEach
    void setUp() {
        sampleRecord = ComplianceRecordResponseDTO.builder()
                .id(RECORD_ID)
                .requirementId("REQ-001")
                .title("Encrypt stored cardholder data")
                .status("IN_PROGRESS")
                .complianceScore(BigDecimal.valueOf(75.50))
                .assignedTo("john.doe")
                .dueDate(LocalDate.now().plusDays(30))
                .reviewDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        updateDto = ComplianceUpdateRequestDTO.builder()
                .title("Updated title")
                .status("COMPLIANT")
                .complianceScore(BigDecimal.valueOf(95.00))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET /api/v1/compliance-records  (paginated list)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/v1/compliance-records")
    class GetAllRecords {

        @Test
        @DisplayName("200 — ADMIN can list records")
        void adminCanListRecords() throws Exception {
            Page<ComplianceRecordResponseDTO> page =
                    new PageImpl<>(List.of(sampleRecord));
            when(complianceRecordService.getAllRecords(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", buildAuthHeader(generateAdminToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(RECORD_ID.toString()))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("200 — VIEWER can list records")
        void viewerCanListRecords() throws Exception {
            Page<ComplianceRecordResponseDTO> page =
                    new PageImpl<>(Collections.emptyList());
            when(complianceRecordService.getAllRecords(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", buildAuthHeader(generateViewerToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("403 — unauthenticated request is rejected")
        void unauthenticatedIsRejected() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PUT /api/v1/compliance-records/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/v1/compliance-records/{id}")
    class UpdateRecord {

        @Test
        @DisplayName("200 — MANAGER can update a record")
        void managerCanUpdate() throws Exception {
            when(complianceRecordService.updateRecord(eq(RECORD_ID), any()))
                    .thenReturn(sampleRecord);

            mockMvc.perform(put(BASE_URL + "/{id}", RECORD_ID)
                            .header("Authorization", buildAuthHeader(generateManagerToken()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECORD_ID.toString()));
        }

        @Test
        @DisplayName("403 — VIEWER cannot update a record")
        void viewerCannotUpdate() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", RECORD_ID)
                            .header("Authorization", buildAuthHeader(generateViewerToken()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("404 — updating non-existent record returns 404")
        void updateNonExistent() throws Exception {
            when(complianceRecordService.updateRecord(eq(MISSING_ID), any()))
                    .thenThrow(new ResourceNotFoundException("ComplianceRecord", "id", MISSING_ID));

            mockMvc.perform(put(BASE_URL + "/{id}", MISSING_ID)
                            .header("Authorization", buildAuthHeader(generateAdminToken()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DELETE /api/v1/compliance-records/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/v1/compliance-records/{id}")
    class DeleteRecord {

        @Test
        @DisplayName("204 — ADMIN can soft-delete a record")
        void adminCanDelete() throws Exception {
            doNothing().when(complianceRecordService).softDeleteRecord(RECORD_ID);

            mockMvc.perform(delete(BASE_URL + "/{id}", RECORD_ID)
                            .header("Authorization", buildAuthHeader(generateAdminToken())))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("403 — MANAGER cannot delete a record")
        void managerCannotDelete() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", RECORD_ID)
                            .header("Authorization", buildAuthHeader(generateManagerToken())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 — VIEWER cannot delete a record")
        void viewerCannotDelete() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", RECORD_ID)
                            .header("Authorization", buildAuthHeader(generateViewerToken())))
                    .andExpect(status().isForbidden());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET /api/v1/compliance-records/search
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/v1/compliance-records/search")
    class SearchRecords {

        @Test
        @DisplayName("200 — authenticated user can search records")
        void authenticatedCanSearch() throws Exception {
            Page<ComplianceRecordResponseDTO> page =
                    new PageImpl<>(List.of(sampleRecord));
            when(complianceRecordService.searchRecords(eq("test"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/search")
                            .param("q", "test")
                            .header("Authorization", buildAuthHeader(generateViewerToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").exists());
        }

        @Test
        @DisplayName("403 — unauthenticated search is rejected")
        void unauthenticatedIsRejected() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search").param("q", "test"))
                    .andExpect(status().isForbidden());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET /api/v1/compliance-records/stats
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/v1/compliance-records/stats")
    class GetStatistics {

        @Test
        @DisplayName("200 — ADMIN can view statistics")
        void adminCanViewStats() throws Exception {
            ComplianceStatsResponseDTO stats = ComplianceStatsResponseDTO.builder()
                    .totalRecords(10)
                    .countByStatus(Map.of("COMPLIANT", 5L, "IN_PROGRESS", 5L))
                    .averageComplianceScore(BigDecimal.valueOf(82.50))
                    .overdueCount(2)
                    .build();
            when(complianceRecordService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get(BASE_URL + "/stats")
                            .header("Authorization", buildAuthHeader(generateAdminToken())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRecords").value(10))
                    .andExpect(jsonPath("$.overdueCount").value(2));
        }

        @Test
        @DisplayName("403 — VIEWER cannot view statistics")
        void viewerCannotViewStats() throws Exception {
            mockMvc.perform(get(BASE_URL + "/stats")
                            .header("Authorization", buildAuthHeader(generateViewerToken())))
                    .andExpect(status().isForbidden());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET /api/v1/compliance-records/export/csv
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/v1/compliance-records/export/csv")
    class ExportCsv {

        @Test
        @DisplayName("200 — ADMIN receives CSV file with correct Content-Type")
        void adminCanExportCsv() throws Exception {
            ComplianceRecord entity = new ComplianceRecord();
            entity.setId(RECORD_ID);
            entity.setRequirementId("REQ-001");
            entity.setTitle("Test Record");
            entity.setStatus("COMPLIANT");
            entity.setComplianceScore(BigDecimal.valueOf(100));
            entity.setAssignedTo("admin");
            entity.setDueDate(LocalDate.now());
            entity.setReviewDate(LocalDate.now());
            entity.setCreatedAt(LocalDateTime.now());

            when(complianceRecordService.getAllForExport()).thenReturn(List.of(entity));

            mockMvc.perform(get(BASE_URL + "/export/csv")
                            .header("Authorization", buildAuthHeader(generateAdminToken())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/csv"))
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("compliance_records_")));
        }

        @Test
        @DisplayName("403 — VIEWER cannot export CSV")
        void viewerCannotExportCsv() throws Exception {
            mockMvc.perform(get(BASE_URL + "/export/csv")
                            .header("Authorization", buildAuthHeader(generateViewerToken())))
                    .andExpect(status().isForbidden());
        }
    }
}
