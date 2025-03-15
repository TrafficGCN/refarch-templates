package de.muenchen.refarch.security;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Each possible authority in this project is represented by a constant in this class.
 * The constants are used within the {@link org.springframework.stereotype.Controller} or
 * {@link org.springframework.stereotype.Service} classes in the method security annotations
 * (e.g. {@link PreAuthorize}).
 */
@SuppressWarnings("PMD.DataClass")
public final class Authorities {
    // Link management
    public static final String LINK_READ = "permitAll()"; // Everyone can read
    public static final String LINK_WRITE = "hasRole('ROLE_ADMIN')"; // Only admin can write/modify

    private Authorities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
