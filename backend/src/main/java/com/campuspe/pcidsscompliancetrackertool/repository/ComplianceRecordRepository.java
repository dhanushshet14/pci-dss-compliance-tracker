package com.campuspe.pcidsscompliancetrackertool.repository;

import com.campuspe.pcidsscompliancetrackertool.entity.ComplianceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, UUID> {

    @Query("SELECT c FROM ComplianceRecord c " +
           "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.requirementId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ComplianceRecord> searchByKeyword(@Param("keyword") String keyword);

    List<ComplianceRecord> findByStatus(String status);

    List<ComplianceRecord> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    // BUG-4 FIX: @EntityGraph fetches all associations in a single LEFT JOIN query,
    // eliminating the N+1 problem (one extra SELECT per record) visible with show-sql=true.
    @EntityGraph(attributePaths = {})
    @Query("SELECT c FROM ComplianceRecord c WHERE c.isDeleted = false")
    Page<ComplianceRecord> findAllActiveRecords(Pageable pageable);

    @Query(value = "SELECT status, COUNT(*) FROM compliance_records " +
                   "WHERE is_deleted = false GROUP BY status",
           nativeQuery = true)
    List<Object[]> countByStatus();

    List<ComplianceRecord> findByAssignedTo(String assignedTo);

    List<ComplianceRecord> findByStatusAndIsDeleted(String status, Boolean isDeleted);

    @Query("SELECT c FROM ComplianceRecord c " +
           "WHERE c.isDeleted = false " +
           "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.requirementId) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    @EntityGraph(attributePaths = {})
    Page<ComplianceRecord> searchByKeywordPaginated(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(c) FROM ComplianceRecord c " +
           "WHERE c.isDeleted = false " +
           "AND c.dueDate < :today " +
           "AND c.status <> :excludedStatus")
    long countOverdueRecords(@Param("today") LocalDate today, @Param("excludedStatus") String excludedStatus);

    @Query("SELECT AVG(c.complianceScore) FROM ComplianceRecord c WHERE c.isDeleted = false")
    java.math.BigDecimal findAverageComplianceScore();

    @Query("SELECT COUNT(c) FROM ComplianceRecord c WHERE c.isDeleted = false")
    long countActiveRecords();

    /**
     * Returns all non-deleted compliance records without pagination.
     * Used exclusively by the CSV export service.
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.isDeleted = false ORDER BY c.createdAt DESC")
    List<ComplianceRecord> findAllActiveForExport();

    /**
     * Finds all non-deleted, non-compliant records whose due date has passed.
     * Used by the daily overdue-reminder scheduler job.
     */
    @Query("SELECT c FROM ComplianceRecord c " +
           "WHERE c.isDeleted = false " +
           "AND c.dueDate < :today " +
           "AND c.status <> :excludedStatus")
    List<ComplianceRecord> findOverdueRecords(
            @Param("today") LocalDate today,
            @Param("excludedStatus") String excludedStatus);

    /**
     * Finds all non-deleted, non-compliant records whose due date falls
     * between {@code start} (inclusive) and {@code end} (inclusive).
     * Used by the 7-day advance deadline-alert scheduler job.
     */
    @Query("SELECT c FROM ComplianceRecord c " +
           "WHERE c.isDeleted = false " +
           "AND c.dueDate BETWEEN :start AND :end " +
           "AND c.status <> :excludedStatus")
    List<ComplianceRecord> findUpcomingDeadlineRecords(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludedStatus") String excludedStatus);
}
