package com.campuspe.pcidsscompliancetrackertool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.campuspe.pcidsscompliancetrackertool.security.JwtAuthFilter;

/**
 * Central Spring Security configuration for the PCI-DSS Compliance Tracker.
 *
 * <h3>Access rules (summary)</h3>
 * <ul>
 *   <li>{@code /auth/**} — public (login, register, refresh)</li>
 *   <li>{@code /swagger-ui/**}, {@code /v3/api-docs/**} — public (demo/docs)</li>
 *   <li>{@code DELETE} endpoints — {@code ADMIN} only</li>
 *   <li>{@code POST / PUT} endpoints — {@code ADMIN} or {@code MANAGER}</li>
 *   <li>{@code GET} endpoints — any authenticated user ({@code ADMIN}, {@code MANAGER}, {@code VIEWER})</li>
 * </ul>
 *
 * <p>Fine-grained method-level checks are handled via {@code @PreAuthorize}
 * annotations on controller methods, enabled by {@code @EnableMethodSecurity}.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity                           // enables @PreAuthorize / @Secured
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // ── BCryptPasswordEncoder bean ────────────────────────────────────────────

    /**
     * Provides a shared {@link BCryptPasswordEncoder} instance used for
     * hashing passwords on registration and verifying them on login.
     *
     * @return BCrypt encoder with default strength (10 rounds)
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Security filter chain ─────────────────────────────────────────────────

    /**
     * Defines the HTTP security filter chain.
     *
     * <ol>
     *   <li>Disables CSRF (stateless JWT API).</li>
     *   <li>Sets session management to {@code STATELESS}.</li>
     *   <li>Permits public access to auth and Swagger endpoints.</li>
     *   <li>Restricts DELETE to ADMIN, mutating methods to ADMIN/MANAGER.</li>
     *   <li>Requires authentication for everything else.</li>
     *   <li>Registers {@link JwtAuthFilter} before
     *       {@link UsernamePasswordAuthenticationFilter}.</li>
     * </ol>
     *
     * @param http the {@link HttpSecurity} builder
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — API is stateless (JWT-based)
            .csrf(csrf -> csrf.disable())

            // Stateless session — no server-side session storage
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL-level authorisation rules
            .authorizeHttpRequests(auth -> auth
                // ── Public endpoints ──────────────────────────────────────
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()

                // ── Role-based HTTP method restrictions ───────────────────
                // DELETE → ADMIN only
                .requestMatchers(HttpMethod.DELETE, "/api/**")
                    .hasRole(RoleConstants.ADMIN)

                // POST & PUT → ADMIN or MANAGER
                .requestMatchers(HttpMethod.POST, "/api/**")
                    .hasAnyRole(RoleConstants.ADMIN, RoleConstants.MANAGER)
                .requestMatchers(HttpMethod.PUT, "/api/**")
                    .hasAnyRole(RoleConstants.ADMIN, RoleConstants.MANAGER)

                // GET → any authenticated user (ADMIN, MANAGER, VIEWER)
                .requestMatchers(HttpMethod.GET, "/api/**")
                    .authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // Register JWT filter before the default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
