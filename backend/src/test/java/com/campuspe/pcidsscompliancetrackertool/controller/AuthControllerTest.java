package com.campuspe.pcidsscompliancetrackertool.controller;

import com.campuspe.pcidsscompliancetrackertool.BaseControllerTest;
import com.campuspe.pcidsscompliancetrackertool.dto.LoginRequestDto;
import com.campuspe.pcidsscompliancetrackertool.dto.RefreshRequestDto;
import com.campuspe.pcidsscompliancetrackertool.dto.RegisterRequestDto;
import com.campuspe.pcidsscompliancetrackertool.entity.Role;
import com.campuspe.pcidsscompliancetrackertool.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@code AuthController}.
 *
 * <p>Uses mocked {@code UserRepository} so that no real database is needed.
 * JWT tokens are generated dynamically via the real {@code JwtUtil} bean.</p>
 */
class AuthControllerTest extends BaseControllerTest {

    private static final String AUTH_URL = "/auth";

    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_EMAIL    = "testuser@company.com";
    private static final String VALID_PASSWORD = "Secret@123";

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUsername(VALID_USERNAME);
        existingUser.setEmail(VALID_EMAIL);
        existingUser.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        existingUser.setRole(Role.VIEWER);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  POST /auth/login
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("200 — valid credentials return tokens")
        void loginWithValidCredentials() throws Exception {
            when(userRepository.findByUsername(VALID_USERNAME))
                    .thenReturn(Optional.of(existingUser));

            LoginRequestDto request = new LoginRequestDto(VALID_USERNAME, VALID_PASSWORD);

            mockMvc.perform(post(AUTH_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.role").value("VIEWER"));
        }

        @Test
        @DisplayName("401 — wrong password is rejected")
        void loginWithWrongPassword() throws Exception {
            when(userRepository.findByUsername(VALID_USERNAME))
                    .thenReturn(Optional.of(existingUser));

            LoginRequestDto request = new LoginRequestDto(VALID_USERNAME, "WrongPass999");

            mockMvc.perform(post(AUTH_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid username or password."));
        }

        @Test
        @DisplayName("400 — blank username fails validation")
        void loginWithBlankUsername() throws Exception {
            LoginRequestDto request = new LoginRequestDto("", VALID_PASSWORD);

            mockMvc.perform(post(AUTH_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  POST /auth/register
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @DisplayName("201 — valid registration succeeds")
        void registerNewUser() throws Exception {
            when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            RegisterRequestDto request =
                    new RegisterRequestDto("newuser", "new@company.com", "Secret@123");

            mockMvc.perform(post(AUTH_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("newuser"))
                    .andExpect(jsonPath("$.role").value("VIEWER"));
        }

        @Test
        @DisplayName("409 — duplicate username returns conflict")
        void registerDuplicateUsername() throws Exception {
            when(userRepository.findByUsername(VALID_USERNAME))
                    .thenReturn(Optional.of(existingUser));

            RegisterRequestDto request =
                    new RegisterRequestDto(VALID_USERNAME, "dup@company.com", "Secret@123");

            mockMvc.perform(post(AUTH_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("400 — invalid email format fails validation")
        void registerInvalidEmail() throws Exception {
            RegisterRequestDto request =
                    new RegisterRequestDto("newuser", "not-an-email", "Secret@123");

            mockMvc.perform(post(AUTH_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  POST /auth/refresh
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /auth/refresh")
    class Refresh {

        @Test
        @DisplayName("200 — valid refresh token returns new access token")
        void refreshWithValidToken() throws Exception {
            when(userRepository.findByUsername(VALID_USERNAME))
                    .thenReturn(Optional.of(existingUser));

            String refreshToken = jwtUtil.generateRefreshToken(
                    VALID_USERNAME, List.of(Role.VIEWER.name()));

            RefreshRequestDto request = new RefreshRequestDto(refreshToken);

            mockMvc.perform(post(AUTH_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.role").value("VIEWER"));
        }

        @Test
        @DisplayName("401 — expired/invalid refresh token is rejected")
        void refreshWithInvalidToken() throws Exception {
            RefreshRequestDto request =
                    new RefreshRequestDto("invalid.token.value");

            mockMvc.perform(post(AUTH_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}
