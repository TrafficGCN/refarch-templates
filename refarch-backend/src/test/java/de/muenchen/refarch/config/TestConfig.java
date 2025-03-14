package de.muenchen.refarch.config;

import de.muenchen.refarch.user.User;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.UUID;

@TestConfiguration
@EnableWebMvc
public class TestConfig {

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @Bean
    @Primary
    @Profile("!no-security")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Primary
    public User testUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        return user;
    }
}
