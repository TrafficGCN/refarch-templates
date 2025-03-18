package de.muenchen.refarch.config;

import de.muenchen.refarch.user.User;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.UUID;

/* default */ @TestConfiguration
@EnableWebMvc
@SuppressWarnings(
    {
            "PMD.UnitTestShouldUseTestAnnotation",
            "PMD.TestClassWithoutTestCases",
            "PMD.CommentDefaultAccessModifier"
    }
)
public class TestConfig {

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @Bean
    @Primary
    @Profile("!no-security")
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll());
        return http.build();
    }

    @Bean
    @Primary
    public User testUser() {
        final User user = new User();
        user.setId(TEST_USER_ID);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        return user;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        final UserDetails user = org.springframework.security.core.userdetails.User.builder()
                .username("test")
                .password("{noop}test")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
