package de.muenchen.refarch.configuration;

import de.muenchen.refarch.security.NoSecurityAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configures the security context to not require any authorization for incoming requests,
 * but still allows password-based JWT authentication for optional features.
 */
@Configuration
@Profile("no-security")
@EnableWebSecurity
@RequiredArgsConstructor
public class NoSecurityConfiguration {
    private final NoSecurityAuthenticationFilter authenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        // Add our filter first for optional JWT auth
        http.addFilterBefore(authenticationFilter, BasicAuthenticationFilter.class);

        // Configure all endpoints as public
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(AntPathRequestMatcher.antMatcher("/**"))
                .permitAll()
                .requestMatchers(PathRequest.toH2Console())
                .permitAll()
                .anyRequest()
                .permitAll())
                .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
