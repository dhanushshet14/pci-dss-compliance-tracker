package com.campuspe.pcidsscompliancetrackertool.entity;

/**
 * Enum representing the access-control roles available in the system.
 * Maps directly to the {@code role} column in the {@code users} table.
 */
public enum Role {

    /** Full access — can create, read, update, delete, and view stats. */
    ADMIN,

    /** Can create, read, and update — but cannot delete. */
    MANAGER,

    /** Read-only access to GET endpoints. */
    VIEWER
}
