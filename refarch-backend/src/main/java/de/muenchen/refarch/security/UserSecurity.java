package de.muenchen.refarch.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserSecurity {

    /**
     * Checks if the current authenticated user is accessing their own data
     *
     * @param userId The ID of the user being accessed
     * @return true if the current user is accessing their own data
     */
    public boolean isCurrentUser(final UUID userId) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // The principal name should match the user's ID in our system
        return authentication.getName().equals(userId.toString());
    }
}
