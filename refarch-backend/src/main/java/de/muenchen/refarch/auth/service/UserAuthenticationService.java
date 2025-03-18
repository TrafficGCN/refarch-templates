package de.muenchen.refarch.auth.service;

import de.muenchen.refarch.globalsettings.GlobalSettingsService;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalSettingsService globalSettingsService;

    /**
     * Authenticate a user using email and password.
     *
     * @param email User's email.
     * @param password User's password.
     * @return Authenticated User object.
     * @throws BadCredentialsException if the email or password is incorrect.
     * @throws DisabledException if password authentication is disabled.
     */
    @Transactional(readOnly = true)
    public User authenticateUser(final String email, final String password) {
        // Check if password authentication is enabled
        final GlobalSettingsResponseDTO settings = globalSettingsService.getCurrentSettings();
        log.debug("Password authentication enabled: {}", settings.passwordAuthEnabled());
        if (!settings.passwordAuthEnabled()) {
            throw new DisabledException("Password authentication is disabled");
        }

        final Optional<User> userOptional = userRepository.findByEmailWithRoles(email);
        log.debug("User found for email {}: {}", email, userOptional.isPresent());
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            log.debug("Stored hashed password: {}", user.getPassword());
            log.debug("Attempting to match raw password with stored hash");
            final boolean matches = passwordEncoder.matches(password, user.getPassword());
            log.debug("Password matches for user {}: {}", email, matches);
            if (matches) {
                log.debug("User authenticated successfully: {}", user.getUsername());
                log.debug("User roles: {}", user.getRoles());
                return user;
            }
        }
        throw new BadCredentialsException("Invalid email or password");
    }
}
