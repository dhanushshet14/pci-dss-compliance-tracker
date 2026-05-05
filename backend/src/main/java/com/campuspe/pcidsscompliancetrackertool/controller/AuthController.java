package com.campuspe.pcidsscompliancetrackertool.controller;

import com.campuspe.pcidsscompliancetrackertool.dto.*;
import com.campuspe.pcidsscompliancetrackertool.entity.User;
import com.campuspe.pcidsscompliancetrackertool.entity.Role;
import com.campuspe.pcidsscompliancetrackertool.repository.UserRepository;
import com.campuspe.pcidsscompliancetrackertool.security.JwtUtil;
import com.campuspe.pcidsscompliancetrackertool.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller that handles authentication operations:
 * user login, registration, and JWT token refresh.
 *
 * <p>All endpoints live under {@code /auth} and are publicly accessible
 * (configured in {@code SecurityConfig} to permit {@code /auth/**}).</p>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for login, registration and token refresh")
public class AuthController {

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final UserRepository       userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    // ── Configuration values (injected from application.yml) ─────────────────

    /**
     * Lifetime of an access token in seconds.
     * Mapped from {@code jwt.expiry-seconds} in {@code application.yml}.
     */
    @Value("${jwt.expiry-seconds}")
    private long expirySeconds;

    /**
     * Lifetime of a refresh token in seconds.
     * Mapped from {@code jwt.refresh-expiry-seconds} in {@code application.yml}.
     */
    @Value("${jwt.refresh-expiry-seconds}")
    private long refreshExpirySeconds;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Constructs the controller with required dependencies.
     *
     * @param userRepository  JPA repository for {@link User} entities
     * @param passwordEncoder BCrypt password encoder/matcher
     * @param jwtUtil         Utility for generating and validating JWT tokens
     */
    public AuthController(UserRepository userRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          TokenBlacklistService tokenBlacklistService) {
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.jwtUtil              = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    /**
     * Authenticates a user with the supplied credentials.
     *
     * <p>Looks up the user by username, verifies the password with BCrypt,
     * then generates a signed JWT access token and a refresh token.</p>
     *
     * @param request DTO containing {@code username} and {@code password}
     * @return {@code 200 OK} with {@link AuthResponseDto} on success,
     *         {@code 401 Unauthorized} with {@link ErrorResponse} if credentials are invalid
     */
    @Operation(
        summary     = "Login",
        description = "Authenticate with username and password; returns a JWT access token and refresh token."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        // Fetch user; return 401 immediately if not found (avoid username enumeration)
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Unauthorized", "Invalid username or password."));
        }

        String roleStr     = user.getRole().name();
        String accessToken = jwtUtil.generateToken(user.getUsername(), List.of(roleStr));
        String refreshTok  = jwtUtil.generateRefreshToken(user.getUsername(), List.of(roleStr));

        return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshTok, expirySeconds, roleStr));
    }

    // ── POST /auth/register ───────────────────────────────────────────────────

    /**
     * Registers a new user account.
     *
     * <p>Assigns the {@code VIEWER} role by default. Hashes the plain-text
     * password with BCrypt before persisting. The password is never returned
     * in the response.</p>
     *
     * @param request DTO containing {@code username}, {@code email}, and {@code password}
     * @return {@code 201 Created} with the new user's details (no password),
     *         {@code 409 Conflict} if the username is already taken,
     *         {@code 400 Bad Request} for validation errors
     */
    @Operation(
        summary     = "Register",
        description = "Create a new user account. Default role is VIEWER. Password is BCrypt-hashed before storage."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "409", description = "Username already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request) {
        // Conflict check
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Conflict",
                      "Username '" + request.getUsername() + "' is already taken."));
        }

        // Build and persist the new user
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.VIEWER);

        userRepository.save(newUser);

        // Return 201 with user details — password is intentionally excluded
        AuthResponseDto body = new AuthResponseDto(
            newUser.getUsername(),
            newUser.getEmail(),
            newUser.getRole().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // ── POST /auth/refresh ────────────────────────────────────────────────────

    /**
     * Issues a new JWT access token using a valid refresh token.
     *
     * <p>The refresh token is validated; if expired or tampered with, a
     * {@code 401 Unauthorized} response is returned. On success, only a
     * new access token is returned — the same refresh token stays in use
     * until it expires.</p>
     *
     * @param request DTO containing the {@code refreshToken}
     * @return {@code 200 OK} with a new {@link AuthResponseDto} (access token only),
     *         {@code 401 Unauthorized} if the refresh token is invalid or expired
     */
    @Operation(
        summary     = "Refresh token",
        description = "Exchange a valid refresh token for a new JWT access token."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token issued",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Refresh token invalid or expired",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();

        // Validate the refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Unauthorized",
                      "Refresh token is invalid or has expired."));
        }

        // Extract username and look up the user to get current role
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Unauthorized", "User no longer exists."));
        }

        String roleStr     = user.getRole().name();
        String newAccess   = jwtUtil.generateToken(username, List.of(roleStr));

        return ResponseEntity.ok(new AuthResponseDto(newAccess, null, expirySeconds, roleStr));
    }

    // ── POST /auth/logout ─────────────────────────────────────────────────────────────

    /**
     * Logs out the current user by invalidating the supplied JWT access token.
     *
     * <p>BUG-5 FIX: Without a server-side blacklist, a JWT remains valid until
     * it expires even after the client discards it.  This endpoint extracts the
     * token from the {@code Authorization} header, stores it in Redis with a TTL
     * equal to its remaining validity window, and returns {@code 204 No Content}.
     * {@link com.campuspe.pcidsscompliancetrackertool.security.JwtAuthFilter}
     * checks the blacklist on every subsequent request so the revoked token is
     * immediately rejected.</p>
     *
     * @param request the incoming HTTP request (used to read the Authorization header)
     * @return {@code 204 No Content} on success,
     *         {@code 400 Bad Request} if no valid Bearer token is present
     */
    @Operation(
        summary     = "Logout",
        description = "Invalidates the current JWT access token by adding it to a Redis blacklist. " +
                      "The token is rejected on all subsequent requests until its natural expiry."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logged out successfully"),
        @ApiResponse(responseCode = "400", description = "Missing or malformed Authorization header")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(400, "Bad Request", "Missing or malformed Authorization header."));
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            // Token is already expired — nothing to blacklist
            return ResponseEntity.noContent().build();
        }

        tokenBlacklistService.blacklist(token, jwtUtil.extractRemainingTtl(token));
        return ResponseEntity.noContent().build();
    }
}
