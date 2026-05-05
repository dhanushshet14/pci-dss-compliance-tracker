package com.campuspe.pcidsscompliancetrackertool.security;

import com.campuspe.pcidsscompliancetrackertool.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT authentication filter that executes once per request.
 *
 * <p>Reads the {@code Authorization: Bearer <token>} header, validates the JWT
 * via {@link JwtUtil}, and — if valid — populates the
 * {@link SecurityContextHolder} with a fully authenticated principal so that
 * downstream {@code @PreAuthorize} checks and {@code SecurityConfig} rules
 * work correctly.</p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Intercepts every HTTP request to extract and validate a JWT from the
     * {@code Authorization} header.
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Only process requests that carry a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token) && !tokenBlacklistService.isBlacklisted(token)) {
                String username    = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);

                // Convert role strings to Spring Security GrantedAuthority objects
                // Prefix each role with "ROLE_" so that hasRole("ADMIN") matches
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
