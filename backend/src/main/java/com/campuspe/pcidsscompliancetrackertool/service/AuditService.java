package com.campuspe.pcidsscompliancetrackertool.service;

import com.campuspe.pcidsscompliancetrackertool.entity.AuditLog;
import com.campuspe.pcidsscompliancetrackertool.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for querying the audit trail.
 */
@Service
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Returns the complete audit history for a given entity, sorted by
     * {@code changedAt} descending (most recent first).
     *
     * @param entityType simple class name (e.g. "ComplianceRecord")
     * @param entityId   primary key of the entity (as string)
     * @return list of audit log entries
     */
    public List<AuditLog> getAuditHistory(String entityType, String entityId) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
    }

    /**
     * Returns paginated audit logs for all actions performed by a specific user.
     *
     * @param username the {@code changedBy} value to filter on
     * @param pageable pagination and sort parameters
     * @return page of audit log entries
     */
    public Page<AuditLog> getUserActivity(String username, Pageable pageable) {
        return auditLogRepository
                .findByChangedByOrderByChangedAtDesc(username, pageable);
    }
}
