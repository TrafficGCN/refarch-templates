package de.muenchen.refarch.security;

import com.nimbusds.jwt.JWTClaimsSet;
import de.muenchen.refarch.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
@Profile("!no-security")
public class DynamicAuthenticationFilter extends OncePerRequestFilter {

    private final DynamicSecurityService dynamicSecurityService;
    private final JwtService jwtService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORITIES_CLAIM = "authorities";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator/info",
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/actuator/metrics",
            "/auth/login",
            "/auth/logout",
            "/settings",
            "/users");

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {

        final String path = request.getRequestURI();
        if (PUBLIC_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final boolean ssoEnabled = dynamicSecurityService.isSsoEnabled();
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Handle non-SSO case
        if (!ssoEnabled) {
            handleNonSsoRequest(authHeader, filterChain, request, response);
            return;
        }

        // Handle SSO case
        handleSsoRequest(authHeader, filterChain, request, response);
    }

    private void handleNonSsoRequest(final String authHeader, final FilterChain filterChain,
            final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (isValidBearerToken(authHeader)) {
            final String token = extractToken(authHeader);
            if (jwtService.validateToken(token)) {
                setPasswordBasedAuthentication(token);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleSsoRequest(final String authHeader, final FilterChain filterChain,
            final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (isValidBearerToken(authHeader)) {
            final String token = extractToken(authHeader);
            if (jwtService.validateToken(token)) {
                setPasswordBasedAuthentication(token);
                filterChain.doFilter(request, response);
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private boolean isValidBearerToken(final String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    private String extractToken(final String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    private void setPasswordBasedAuthentication(final String token) {
        try {
            final JWTClaimsSet claims = jwtService.parseToken(token);

            @SuppressWarnings("unchecked")
            final List<String> authorities = claims.getStringListClaim(AUTHORITIES_CLAIM);

            final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()));

            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (Exception e) {
            log.error("Error setting password-based authentication", e);
        }
    }
}
