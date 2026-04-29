-- ============================================================================
-- Flyway Migration: V3__roles_seed.sql
-- Purpose: Create the users table and seed one demo user per role
-- ============================================================================

-- ============================================================================
-- SECTION 1: Users Table
-- ============================================================================
CREATE TABLE users (
    id         UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'VIEWER'
                            CONSTRAINT chk_user_role
                            CHECK (role IN ('ADMIN', 'MANAGER', 'VIEWER')),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  users              IS 'Application users with role-based access control';
COMMENT ON COLUMN users.id           IS 'UUID v4 primary key';
COMMENT ON COLUMN users.username     IS 'Unique login name';
COMMENT ON COLUMN users.email        IS 'Unique email address';
COMMENT ON COLUMN users.password     IS 'BCrypt-hashed password (never stored in plain text)';
COMMENT ON COLUMN users.role         IS 'Access level: ADMIN, MANAGER, or VIEWER';
COMMENT ON COLUMN users.created_at   IS 'Timestamp when the account was created';

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role     ON users (role);

-- ============================================================================
-- SECTION 2: Seed Demo Users (one per role)
-- ============================================================================
-- NOTE: The BCrypt hashes below were generated with cost factor 10.
--       They exist ONLY for development/demo purposes and must be replaced
--       or removed before any production deployment.
--
-- Plain-text passwords used to generate the hashes (demo only):
--   admin_user   → Admin@123
--   manager_user → Manager@123
--   viewer_user  → Viewer@123
-- ============================================================================

INSERT INTO users (username, email, password, role) VALUES
(
    'admin_user',
    'admin@pcidss-demo.com',
    -- BCrypt hash of "Admin@123"
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN'
),
(
    'manager_user',
    'manager@pcidss-demo.com',
    -- BCrypt hash of "Manager@123"
    '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm1z5j7HOr0GN3gSMIzu',
    'MANAGER'
),
(
    'viewer_user',
    'viewer@pcidss-demo.com',
    -- BCrypt hash of "Viewer@123"
    '$2a$10$LCPwRml.LicIzhkGYvGEleZoHkGVpa4V6MSnKoVL4WXLmE.75vMPK',
    'VIEWER'
);

-- ============================================================================
-- END OF MIGRATION V3
-- ============================================================================
