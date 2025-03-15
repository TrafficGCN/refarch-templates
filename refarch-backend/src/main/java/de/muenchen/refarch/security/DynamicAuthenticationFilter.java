package de.muenchen.refarch.security;

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
@Profile("!no-security")
public class DynamicAuthenticationFilter extends OncePerRequestFilter {

    private final DynamicSecurityService dynamicSecurityService;
    private final List<AntPathRequestMatcher> publicPaths = List.of(
            new AntPathRequestMatcher("/actuator/info"),
            new AntPathRequestMatcher("/actuator/health"),
            new AntPathRequestMatcher("/actuator/health/liveness"),
            new AntPathRequestMatcher("/actuator/health/readiness"),
            new AntPathRequestMatcher("/actuator/metrics"));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        boolean ssoEnabled = dynamicSecurityService.isSsoEnabled();
        log.debug("Processing request: {} with SSO enabled: {}", requestUri, ssoEnabled);

        // Check if it's a public path
        if (isPublicPath(request)) {
            log.debug("Public path detected, allowing access: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        // Get or create security context
        SecurityContext context = SecurityContextHolder.getContext();

        if (!ssoEnabled) {
            // SSO disabled - ensure admin authentication
            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER"));
            var authentication = new UsernamePasswordAuthenticationToken(
                    "admin",
                    null,
                    authorities);

            // Set authentication before processing
            context.setAuthentication(authentication);
            log.debug("SSO disabled - set admin authentication for: {}", requestUri);

            // Process request with admin authentication
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                log.error("Error processing request with admin auth", e);
                throw e;
            }

            // If we got a 401, try processing again with fresh authentication
            if (response.getStatus() == 401 && !response.isCommitted()) {
                log.debug("Got 401 with admin auth, retrying with fresh context");
                // Create new context and authentication
                SecurityContextHolder.clearContext();
                context = SecurityContextHolder.getContext();
                authentication = new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        authorities);
                context.setAuthentication(authentication);
                // Reset response
                response.reset();
                // Try again
                filterChain.doFilter(request, response);
            }
        } else {
            // SSO enabled - clear any admin authentication
            var currentAuth = context.getAuthentication();
            if (currentAuth != null && "admin".equals(currentAuth.getName())) {
                log.debug("SSO enabled - clearing admin authentication for: {}", requestUri);
                context.setAuthentication(null);
            }
            filterChain.doFilter(request, response);
        }

        log.debug("Request completed: {} with status: {}", requestUri, response.getStatus());
    }

    private boolean isPublicPath(HttpServletRequest request) {
        return publicPaths.stream().anyMatch(matcher -> matcher.matches(request));
    }
}
