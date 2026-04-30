package com.campuspe.pcidsscompliancetrackertool;

import com.campuspe.pcidsscompliancetrackertool.repository.AuditLogRepository;
import com.campuspe.pcidsscompliancetrackertool.repository.ComplianceRecordRepository;
import com.campuspe.pcidsscompliancetrackertool.repository.UserRepository;
import com.campuspe.pcidsscompliancetrackertool.security.JwtUtil;
import com.campuspe.pcidsscompliancetrackertool.service.ComplianceRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

/**
 * Base class for MockMvc integration tests.
 *
 * <p>Provides a fully wired Spring context with {@link MockMvc}, while
 * mocking the service and repository layers so that tests never touch the
 * real database or require a running PostgreSQL/Redis/Mail instance.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerTest {

    // ── Test constants ───────────────────────────────────────────────────────

    protected static final String TEST_ADMIN_USER   = "admin_test";
    protected static final String TEST_MANAGER_USER = "manager_test";
    protected static final String TEST_VIEWER_USER  = "viewer_test";

    protected static final String ROLE_ADMIN   = "ADMIN";
    protected static final String ROLE_MANAGER = "MANAGER";
    protected static final String ROLE_VIEWER  = "VIEWER";

    // ── Injected components ──────────────────────────────────────────────────

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    // ── Mocked beans (isolate from DB / external services) ───────────────────

    @MockBean
    protected ComplianceRecordService complianceRecordService;

    @MockBean
    protected ComplianceRecordRepository complianceRecordRepository;

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected AuditLogRepository auditLogRepository;

    @MockBean
    protected JavaMailSender javaMailSender;

    // ── Token helpers ────────────────────────────────────────────────────────

    protected String generateAdminToken() {
        return jwtUtil.generateToken(TEST_ADMIN_USER, List.of(ROLE_ADMIN));
    }

    protected String generateManagerToken() {
        return jwtUtil.generateToken(TEST_MANAGER_USER, List.of(ROLE_MANAGER));
    }

    protected String generateViewerToken() {
        return jwtUtil.generateToken(TEST_VIEWER_USER, List.of(ROLE_VIEWER));
    }

    protected String buildAuthHeader(String token) {
        return "Bearer " + token;
    }
}
