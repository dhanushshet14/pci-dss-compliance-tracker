-- ============================================================================
-- Flyway Migration: V1__init.sql
-- ============================================================================

-- ============================================================================
-- SECTION 1: Custom ENUM Type
-- ============================================================================
CREATE TYPE compliance_status AS ENUM (
    'NOT_STARTED',
    'IN_PROGRESS',
    'COMPLIANT',
    'NON_COMPLIANT',
    'PARTIALLY_COMPLIANT'
);

-- ============================================================================
-- SECTION 2: UUID Extension
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


-- ============================================================================
-- SECTION 3: Core Compliance Records Table
-- ============================================================================
CREATE TABLE compliance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    requirement_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status compliance_status NOT NULL DEFAULT 'NOT_STARTED',
    compliance_score  DECIMAL(5, 2) DEFAULT 0.00 CONSTRAINT chk_compliance_score
    CHECK (compliance_score >= 0.00 AND compliance_score <= 100.00),
    assigned_to  VARCHAR(150),
    due_date DATE,
    review_date DATE,
    evidence_notes TEXT,
    ai_description TEXT,
    ai_recommendations TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(150),
    updated_by VARCHAR(150)
);

-- ============================================================================
-- SECTION 4: Table and Column Comments
-- ============================================================================
COMMENT ON TABLE  compliance_records                    IS 'Core table storing PCI-DSS compliance requirement tracking records';
COMMENT ON COLUMN compliance_records.id                 IS 'Unique identifier (UUID v4) for the compliance record';
COMMENT ON COLUMN compliance_records.requirement_id     IS 'PCI-DSS requirement reference (e.g., Req-1.1, Req-3.4.1)';
COMMENT ON COLUMN compliance_records.title              IS 'Short human-readable title of the requirement';
COMMENT ON COLUMN compliance_records.description        IS 'Detailed description of the PCI-DSS requirement';
COMMENT ON COLUMN compliance_records.status             IS 'Current compliance status: NOT_STARTED, IN_PROGRESS, COMPLIANT, NON_COMPLIANT, PARTIALLY_COMPLIANT';
COMMENT ON COLUMN compliance_records.compliance_score   IS 'Numeric compliance score between 0.00 and 100.00';
COMMENT ON COLUMN compliance_records.assigned_to        IS 'Person or team responsible for this requirement';
COMMENT ON COLUMN compliance_records.due_date           IS 'Target date for achieving compliance';
COMMENT ON COLUMN compliance_records.review_date        IS 'Date of the most recent compliance review';
COMMENT ON COLUMN compliance_records.evidence_notes     IS 'Free-text evidence or audit notes';
COMMENT ON COLUMN compliance_records.ai_description     IS 'AI-generated description of the requirement';
COMMENT ON COLUMN compliance_records.ai_recommendations IS 'AI-generated recommendations for compliance';
COMMENT ON COLUMN compliance_records.created_at         IS 'Timestamp when the record was created';
COMMENT ON COLUMN compliance_records.updated_at         IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN compliance_records.is_deleted         IS 'Soft delete flag — true means logically deleted';
COMMENT ON COLUMN compliance_records.created_by         IS 'Username or identifier of the record creator';
COMMENT ON COLUMN compliance_records.updated_by         IS 'Username or identifier of the last modifier';

-- ============================================================================
-- SECTION 5: Indexes for Efficient Query Lookups
-- ============================================================================

-- Index on status:
CREATE INDEX idx_compliance_records_status
    ON compliance_records (status);

-- Index on due_date:
CREATE INDEX idx_compliance_records_due_date
    ON compliance_records (due_date);

-- Index on requirement_id:
CREATE INDEX idx_compliance_records_requirement_id
    ON compliance_records (requirement_id);

-- Index on assigned_to:

CREATE INDEX idx_compliance_records_assigned_to
    ON compliance_records (assigned_to);

-- ============================================================================
-- SECTION 6: Auto-Update Trigger for updated_at
-- ============================================================================

-- Trigger function: 
CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_compliance_records_updated_at
    BEFORE UPDATE ON compliance_records
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- ============================================================================
-- END OF MIGRATION V1
-- ============================================================================
