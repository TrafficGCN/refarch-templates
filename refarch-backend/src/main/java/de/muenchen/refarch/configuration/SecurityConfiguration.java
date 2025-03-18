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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * The central class for configuration of all security aspects.
 * Automatically used when not running with profile `no-security`.
 * When SSO is enabled: all endpoints require authentication
 * When SSO is disabled: all endpoints are public but password JWT is available
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        // Add our dynamic filter first
        http.addFilterBefore(dynamicAuthenticationFilter, BasicAuthenticationFilter.class);

        // Configure security based on SSO status
        if (dynamicSecurityService.isSsoEnabled()) {
            // SSO enabled - require authentication for all non-public endpoints
            http.authorizeHttpRequests(requests -> requests
                    .requestMatchers(
                            // Public endpoints
                            AntPathRequestMatcher.antMatcher("/actuator/info"),
                            AntPathRequestMatcher.antMatcher("/actuator/health"),
                            AntPathRequestMatcher.antMatcher("/actuator/health/liveness"),
                            AntPathRequestMatcher.antMatcher("/actuator/health/readiness"),
                            AntPathRequestMatcher.antMatcher("/actuator/metrics"),
                            AntPathRequestMatcher.antMatcher("/auth/login"),
                            AntPathRequestMatcher.antMatcher("/auth/logout"))
                    .permitAll()
                    .anyRequest()
                    .authenticated());
        } else {
            // SSO disabled - all endpoints are public
            http.authorizeHttpRequests(requests -> requests
                    .anyRequest()
                    .permitAll());
        }

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
                    final String authorization = request.getHeader("Authorization");
                    if (authorization != null && authorization.startsWith("Bearer ")) {
                        return authorization.substring(7);
                    }
                    return null;
                }));

        return http.build();
    }
}
