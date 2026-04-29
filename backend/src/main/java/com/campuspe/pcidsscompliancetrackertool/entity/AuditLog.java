package com.campuspe.pcidsscompliancetrackertool.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity mapped to the {@code audit_log} table (created by {@code V2__audit.sql}).
 *
 * <p>Captures an immutable record of every create, update, or delete operation
 * on tracked entities. {@code oldValue} and {@code newValue} are stored as
 * JSON-serialised strings.</p>
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    /** Auto-generated sequential identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** Simple class name of the audited entity (e.g. "ComplianceRecord"). */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    /** Primary key of the audited entity, stored as text. */
    @Column(name = "entity_id", nullable = false, length = 255)
    private String entityId;

    /** Mutation type: CREATE, UPDATE, or DELETE. */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /** Username of whoever triggered the change. */
    @Column(name = "changed_by", nullable = false, length = 150)
    private String changedBy;

    /** Server timestamp; auto-populated on persist. */
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    /** JSON snapshot of the entity state before the change (null on CREATE). */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /** JSON snapshot of the entity state after the change (null on DELETE). */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** Client IP address (IPv4 or IPv6). */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** HTTP User-Agent header. */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    // ── Constructors ──────────────────────────────────────────────────────────

    public AuditLog() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
