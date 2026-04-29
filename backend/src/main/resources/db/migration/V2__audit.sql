-- ============================================================================
-- Flyway Migration: V2__audit.sql
-- Purpose: Create the audit_log table to track all entity changes
-- ============================================================================

-- ============================================================================
-- SECTION 1: Audit Log Table
-- ============================================================================
CREATE TABLE audit_log (
    id           BIGSERIAL    PRIMARY KEY,                          
    entity_type  VARCHAR(100) NOT NULL,                             
    entity_id    VARCHAR(255) NOT NULL,                             
    action       VARCHAR(50)  NOT NULL,                             
    changed_by   VARCHAR(150) NOT NULL,                             
    changed_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,   
    old_value    TEXT,                                               
    new_value    TEXT,                                               
    ip_address   VARCHAR(45),                                       
    user_agent   VARCHAR(512)                                      
);

-- ============================================================================
-- SECTION 2: Table and Column Comments
-- ============================================================================
COMMENT ON TABLE  audit_log                IS 'Immutable audit trail capturing every create, update, and delete operation on tracked entities';
COMMENT ON COLUMN audit_log.id             IS 'Auto-generated sequential identifier for the audit entry';
COMMENT ON COLUMN audit_log.entity_type    IS 'Fully-qualified or short name of the entity being audited (e.g., ComplianceRecord)';
COMMENT ON COLUMN audit_log.entity_id      IS 'Primary key of the audited entity, stored as text for flexibility across ID types';
COMMENT ON COLUMN audit_log.action         IS 'The type of mutation performed: CREATE, UPDATE, or DELETE';
COMMENT ON COLUMN audit_log.changed_by     IS 'Username or system identifier of whoever triggered the change';
COMMENT ON COLUMN audit_log.changed_at     IS 'Server-side timestamp recording when the change was persisted';
COMMENT ON COLUMN audit_log.old_value      IS 'JSON snapshot of the entity state before the change (NULL on CREATE)';
COMMENT ON COLUMN audit_log.new_value      IS 'JSON snapshot of the entity state after the change (NULL on DELETE)';
COMMENT ON COLUMN audit_log.ip_address     IS 'Client IP address captured from the request (IPv4 or IPv6)';
COMMENT ON COLUMN audit_log.user_agent     IS 'HTTP User-Agent header captured from the client request';

-- ============================================================================
-- SECTION 3: Indexes for Efficient Query Lookups
-- ============================================================================

-- Composite index on (entity_type, entity_id):
CREATE INDEX idx_audit_log_entity
    ON audit_log (entity_type, entity_id);

-- Index on changed_at:
CREATE INDEX idx_audit_log_changed_at
    ON audit_log (changed_at);

-- Index on changed_by:
CREATE INDEX idx_audit_log_changed_by
    ON audit_log (changed_by);

-- ============================================================================
-- END OF MIGRATION V2
-- ============================================================================
