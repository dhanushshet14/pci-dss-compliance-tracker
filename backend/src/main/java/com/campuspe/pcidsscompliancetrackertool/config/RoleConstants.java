package com.campuspe.pcidsscompliancetrackertool.config;

/**
 * Centralised role constants for use in {@code @PreAuthorize} SpEL expressions.
 *
 * <p>Spring Security's {@code hasRole()} automatically prepends {@code ROLE_} to
 * the value it receives, so the constants used inside SpEL expressions must
 * <strong>not</strong> include the prefix. The {@code ROLE_}-prefixed variants
 * are provided for cases where the full authority string is needed
 * (e.g. {@code GrantedAuthority} construction).</p>
 */
public final class RoleConstants {

    private RoleConstants() {
        // Utility class — prevent instantiation
    }

    // ── Raw role names (used inside hasRole / hasAnyRole SpEL) ───────────────

    /** Admin role name (without ROLE_ prefix). */
    public static final String ADMIN   = "ADMIN";

    /** Manager role name (without ROLE_ prefix). */
    public static final String MANAGER = "MANAGER";

    /** Viewer role name (without ROLE_ prefix). */
    public static final String VIEWER  = "VIEWER";

    // ── Full authority strings (used when building GrantedAuthority lists) ────

    /** Spring Security authority for ADMIN. */
    public static final String ROLE_ADMIN   = "ROLE_ADMIN";

    /** Spring Security authority for MANAGER. */
    public static final String ROLE_MANAGER = "ROLE_MANAGER";

    /** Spring Security authority for VIEWER. */
    public static final String ROLE_VIEWER  = "ROLE_VIEWER";

    // ── Pre-built SpEL expressions for @PreAuthorize ─────────────────────────

    /** Only ADMIN can access. */
    public static final String HAS_ROLE_ADMIN =
            "hasRole(T(com.campuspe.pcidsscompliancetrackertool.config.RoleConstants).ADMIN)";

    /** ADMIN or MANAGER can access. */
    public static final String HAS_ROLE_ADMIN_OR_MANAGER =
            "hasAnyRole(T(com.campuspe.pcidsscompliancetrackertool.config.RoleConstants).ADMIN, " +
            "T(com.campuspe.pcidsscompliancetrackertool.config.RoleConstants).MANAGER)";

    /** Any authenticated user (ADMIN, MANAGER, or VIEWER). */
    public static final String IS_AUTHENTICATED = "isAuthenticated()";
}
