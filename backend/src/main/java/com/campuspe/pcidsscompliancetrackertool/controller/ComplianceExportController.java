package com.campuspe.pcidsscompliancetrackertool.controller;

import com.campuspe.pcidsscompliancetrackertool.config.RoleConstants;
import com.campuspe.pcidsscompliancetrackertool.entity.ComplianceRecord;
import com.campuspe.pcidsscompliancetrackertool.service.ComplianceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST controller for exporting compliance data in CSV format.
 *
 * <p>The export streams records directly to the HTTP response output stream
 * so that large datasets do not need to be buffered entirely in memory.</p>
 */
@RestController
@RequestMapping("/api/v1/compliance-records")
@Tag(name = "Compliance Export", description = "Endpoints for exporting compliance data")
public class ComplianceExportController {

    private static final Logger log = LoggerFactory.getLogger(ComplianceExportController.class);

    /** Column headers written as the first row of every CSV export. */
    private static final String[] CSV_HEADERS = {
            "ID",
            "Requirement ID",
            "Title",
            "Status",
            "Compliance Score",
            "Assigned To",
            "Due Date",
            "Review Date",
            "Created At"
    };

    private final ComplianceRecordService complianceRecordService;

    public ComplianceExportController(ComplianceRecordService complianceRecordService) {
        this.complianceRecordService = complianceRecordService;
    }

    @Operation(
            summary = "Export compliance records as CSV",
            description = "Streams all active (non-deleted) compliance records as a downloadable " +
                          "CSV file. Accessible by ADMIN and MANAGER roles only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file streamed successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "CSV generation failed")
    })
    @GetMapping("/export/csv")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN_OR_MANAGER)
    public void exportCsv(HttpServletResponse response) {
        String dateStamp = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String filename = "compliance_records_" + dateStamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try {
            // BUG-6 FIX: Write a UTF-8 BOM (EF BB BF) so that Microsoft Excel
            // detects the encoding automatically. Without this, non-ASCII
            // characters (accented names, currency symbols, etc.) display
            // as garbled text when the file is opened in Excel on Windows.
            response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            response.getOutputStream().flush();

            CSVPrinter csvPrinter = new CSVPrinter(
                response.getWriter(),
                CSVFormat.DEFAULT.builder().setHeader(CSV_HEADERS).build());

            List<ComplianceRecord> records = complianceRecordService.getAllForExport();

            for (ComplianceRecord record : records) {
                csvPrinter.printRecord(
                        record.getId(),
                        record.getRequirementId(),
                        record.getTitle(),
                        record.getStatus(),
                        record.getComplianceScore(),
                        record.getAssignedTo(),
                        record.getDueDate(),
                        record.getReviewDate(),
                        record.getCreatedAt()
                );
            }

            csvPrinter.flush();

        } catch (IOException ex) {
            log.error("Failed to export compliance records to CSV", ex);
            try {
                response.resetBuffer();
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"CSV export failed\",\"message\":\"" +
                        ex.getMessage().replace("\"", "'") + "\"}");
                response.getWriter().flush();
            } catch (IOException ioEx) {
                log.error("Failed to write error response after CSV export failure", ioEx);
            }
        }
    }
}
