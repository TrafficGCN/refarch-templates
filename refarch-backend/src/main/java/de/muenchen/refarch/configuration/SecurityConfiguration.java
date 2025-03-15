package de.muenchen.refarch.configuration;

import de.muenchen.refarch.security.DynamicAuthenticationFilter;
import de.muenchen.refarch.security.DynamicSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * The central class for configuration of all security aspects.
 * Automatically used when not running with profile `no-security`.
 * Configures all endpoints to require authentication via access token.
 * (except the Spring Boot Actuator endpoints)
 * Additionally it configures the use of the {@link UserInfoAuthoritiesService}.
 */
@RequiredArgsConstructor
@Configuration
@Profile("!no-security")
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Import(RestTemplateAutoConfiguration.class)
@Slf4j
public class SecurityConfiguration {

    private final RestTemplateBuilder restTemplateBuilder;
    private final SecurityProperties securityProperties;
    private final DynamicAuthenticationFilter dynamicAuthenticationFilter;
    private final DynamicSecurityService dynamicSecurityService;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        // Add our dynamic filter first
        http.addFilterBefore(dynamicAuthenticationFilter, BasicAuthenticationFilter.class);

        // Configure basic security
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(
                        // allow access to /actuator/info
                        AntPathRequestMatcher.antMatcher("/actuator/info"),
                        // allow access to /actuator/health for OpenShift Health Check
                        AntPathRequestMatcher.antMatcher("/actuator/health"),
                        // allow access to /actuator/health/liveness for OpenShift Liveness Check
                        AntPathRequestMatcher.antMatcher("/actuator/health/liveness"),
                        // allow access to /actuator/health/readiness for OpenShift Readiness Check
                        AntPathRequestMatcher.antMatcher("/actuator/health/readiness"),
                        // allow access to /actuator/metrics for Prometheus monitoring in OpenShift
                        AntPathRequestMatcher.antMatcher("/actuator/metrics"))
                .permitAll()
                // Let our filter handle authentication dynamically
                .anyRequest()
                .authenticated());

        // Configure OAuth2 but make it optional
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                        new JwtUserInfoAuthenticationConverter(
                                new UserInfoAuthoritiesService(securityProperties.getUserInfoUri(), restTemplateBuilder))))
                .bearerTokenResolver(request -> {
                    // Only process bearer tokens if SSO is enabled
                    if (!dynamicSecurityService.isSsoEnabled()) {
                        return null;
                    }
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null && authorization.startsWith("Bearer ")) {
                        return authorization.substring(7);
                    }
                    return null;
                }));

        return http.build();
    }

}
