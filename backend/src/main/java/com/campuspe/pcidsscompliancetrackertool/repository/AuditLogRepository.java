package com.campuspe.pcidsscompliancetrackertool.repository;

import com.campuspe.pcidsscompliancetrackertool.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AuditLog} entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Returns the full audit history for a specific entity, most recent first.
     *
     * @param entityType simple class name (e.g. "ComplianceRecord")
     * @param entityId   primary key of the audited entity (as string)
     * @return ordered list of audit entries
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(
            String entityType, String entityId);

    /**
     * Returns paginated audit logs for actions performed by a given user.
     *
     * @param changedBy username
     * @param pageable  pagination/sort information
     * @return paged audit entries
     */
    Page<AuditLog> findByChangedByOrderByChangedAtDesc(
            String changedBy, Pageable pageable);
}
