package com.campuspe.pcidsscompliancetrackertool.config;

import com.campuspe.pcidsscompliancetrackertool.entity.AuditLog;
import com.campuspe.pcidsscompliancetrackertool.repository.AuditLogRepository;
import com.campuspe.pcidsscompliancetrackertool.repository.ComplianceRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Cross-cutting aspect that automatically records audit-trail entries for
 * every create, update, or delete operation in the service layer.
 *
 * <h3>Pointcut</h3>
 * Matches all public methods in {@code com.campuspe.pcidsscompliancetrackertool.service}
 * whose name starts with {@code create}, {@code update}, or {@code softDelete}/{@code delete}.
 *
 * <h3>Safety</h3>
 * All audit logic is wrapped in a try-catch — an audit failure will <strong>never</strong>
 * prevent the main business operation from completing.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository          auditLogRepository;
    private final ComplianceRecordRepository  complianceRecordRepository;
    private final ObjectMapper                objectMapper;

    public AuditAspect(AuditLogRepository auditLogRepository,
                       ComplianceRecordRepository complianceRecordRepository,
                       ObjectMapper objectMapper) {
        this.auditLogRepository         = auditLogRepository;
        this.complianceRecordRepository = complianceRecordRepository;
        this.objectMapper               = objectMapper;
    }

    // ── Pointcut + Advice ─────────────────────────────────────────────────────

    /**
     * Intercepts create/update/delete methods in the service package.
     *
     * @param joinPoint the proceeding join point
     * @return the original method's return value (unmodified)
     * @throws Throwable if the target method throws
     */
    @Around("execution(public * com.campuspe.pcidsscompliancetrackertool.service.*.create*(..)) || " +
            "execution(public * com.campuspe.pcidsscompliancetrackertool.service.*.update*(..)) || " +
            "execution(public * com.campuspe.pcidsscompliancetrackertool.service.*.delete*(..)) || " +
            "execution(public * com.campuspe.pcidsscompliancetrackertool.service.*.softDelete*(..))")
    public Object auditServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String action     = deriveAction(methodName);
        String oldJson    = null;

        // ── Capture old state (for UPDATE / DELETE) ───────────────────────────
        UUID entityId = extractEntityId(joinPoint.getArgs());
        if (entityId != null && !"CREATE".equals(action)) {
            try {
                oldJson = complianceRecordRepository.findById(entityId)
                        .map(this::toJson)
                        .orElse(null);
            } catch (Exception e) {
                log.warn("[Audit] Could not capture old state for {}: {}", entityId, e.getMessage());
            }
        }

        // ── Execute the original method ───────────────────────────────────────
        Object result = joinPoint.proceed();

        // ── Save audit log (never let this fail the main operation) ───────────
        try {
            String newJson      = (result != null) ? toJson(result) : null;
            String entityIdStr  = resolveEntityIdString(entityId, result);
            String entityType   = deriveEntityType(joinPoint);

            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityIdStr != null ? entityIdStr : "unknown");
            auditLog.setAction(action);
            auditLog.setChangedBy(getCurrentUsername());
            auditLog.setOldValue(oldJson);
            auditLog.setNewValue("DELETE".equals(action) ? null : newJson);
            auditLog.setIpAddress(getClientIp());
            auditLog.setUserAgent(getUserAgent());

            auditLogRepository.save(auditLog);
            log.debug("[Audit] Saved {} audit for {}/{}", action, entityType, entityIdStr);
        } catch (Exception e) {
            log.warn("[Audit] Failed to save audit log for {}: {}", methodName, e.getMessage(), e);
        }

        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Derives the action string (CREATE / UPDATE / DELETE) from the method name.
     */
    private String deriveAction(String methodName) {
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete") || methodName.startsWith("softDelete")) return "DELETE";
        return "UNKNOWN";
    }

    /**
     * Derives the entity type dynamically from the service class name.
     * E.g. {@code ComplianceRecordService} → {@code ComplianceRecord}.
     */
    private String deriveEntityType(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        // Remove "Service" suffix if present
        if (className.endsWith("Service")) {
            return className.substring(0, className.length() - "Service".length());
        }
        return className;
    }

    /**
     * Extracts a {@link UUID} argument from the method parameters, if present.
     * Used to fetch the old state before update/delete.
     */
    private UUID extractEntityId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return (UUID) arg;
            }
        }
        return null;
    }

    /**
     * Resolves the entity ID as a string — either from the method argument
     * or from the returned object (for create operations).
     */
    private String resolveEntityIdString(UUID argId, Object result) {
        if (argId != null) return argId.toString();
        // Try to extract "id" from the result via reflection-free JSON approach
        if (result != null) {
            try {
                String json = objectMapper.writeValueAsString(result);
                var node = objectMapper.readTree(json);
                if (node.has("id")) {
                    return node.get("id").asText();
                }
            } catch (Exception ignored) {
                // Best-effort
            }
        }
        return null;
    }

    /**
     * Returns the currently authenticated username, or "system" if anonymous.
     */
    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {
            // Fall through
        }
        return "system";
    }

    /**
     * Extracts the client IP from the current HTTP request, if available.
     */
    private String getClientIp() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String forwarded = request.getHeader("X-Forwarded-For");
                if (forwarded != null && !forwarded.isBlank()) {
                    return forwarded.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
            // Not in a request context
        }
        return null;
    }

    /**
     * Extracts the User-Agent header from the current HTTP request, if available.
     */
    private String getUserAgent() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                return request.getHeader("User-Agent");
            }
        } catch (Exception ignored) {
            // Not in a request context
        }
        return null;
    }

    /**
     * Returns the current {@link HttpServletRequest}, or null if not
     * running inside a web request context (e.g. scheduler).
     */
    private HttpServletRequest getCurrentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    /**
     * Serialises an object to a JSON string using the injected {@link ObjectMapper}.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[Audit] JSON serialisation failed: {}", e.getMessage());
            return null;
        }
    }
}
