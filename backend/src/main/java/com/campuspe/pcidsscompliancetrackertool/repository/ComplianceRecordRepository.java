package com.campuspe.pcidsscompliancetrackertool.repository;

import com.campuspe.pcidsscompliancetrackertool.entity.ComplianceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ComplianceRecord} entities.
 * Provides CRUD operations and custom query methods for compliance tracking.
 */
@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, UUID> {

    /**
     * Searches across title, description, and requirementId using a
     * case-insensitive LIKE match. Useful for the global search bar.
     *
     * @param keyword the search term (wrapped with % wildcards by the caller)
     * @return list of matching compliance records
     */
    @Query("SELECT c FROM ComplianceRecord c " +
           "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.requirementId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ComplianceRecord> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Finds all records with the given compliance status.
     * Uses Spring Data derived query naming convention.
     *
     * @param status the compliance status to filter by
     * @return list of records matching the status
     */
    List<ComplianceRecord> findByStatus(String status);

    /**
     * Finds all records whose due date falls within the specified range (inclusive).
     *
     * @param startDate the start of the date range
     * @param endDate   the end of the date range
     * @return list of records with due dates in the range
     */
    List<ComplianceRecord> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Returns a paginated list of all non-deleted (active) records.
     * Supports sorting and pagination via the Pageable parameter.
     *
     * @param pageable pagination and sorting information
     * @return page of active compliance records
     */
    @Query("SELECT c FROM ComplianceRecord c WHERE c.isDeleted = false")
    Page<ComplianceRecord> findAllActiveRecords(Pageable pageable);

    /**
     * Counts the number of compliance records grouped by status.
     * Returns each status and its count as an Object[] pair.
     * Uses native SQL because GROUP BY aggregation on an ENUM
     * column is more straightforward in native PostgreSQL.
     *
     * @return list of [status, count] pairs
     */
    @Query(value = "SELECT status, COUNT(*) FROM compliance_records " +
                   "WHERE is_deleted = false GROUP BY status",
           nativeQuery = true)
    List<Object[]> countByStatus();

    /**
     * Finds all records assigned to a specific person or team.
     *
     * @param assignedTo the assignee name to filter by
     * @return list of records assigned to the given person
     */
    List<ComplianceRecord> findByAssignedTo(String assignedTo);

    /**
     * Finds all non-deleted records with the given status (combined filter).
     *
     * @param status    the compliance status
     * @param isDeleted the soft-delete flag
     * @return list of matching records
     */
    List<ComplianceRecord> findByStatusAndIsDeleted(String status, Boolean isDeleted);
}
