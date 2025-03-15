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
    // Global Settings
    public static final String SETTINGS_READ = "permitAll()";
    public static final String SETTINGS_WRITE = "hasRole('ROLE_ADMIN')";

    // Link management
    public static final String LINK_READ = "permitAll()";
    public static final String LINK_WRITE = "hasRole('ROLE_ADMIN')";

    // Language management
    public static final String LANGUAGE_READ = "permitAll()";
    public static final String LANGUAGE_WRITE = "hasRole('ROLE_ADMIN')";

    // Page management
    public static final String PAGE_READ = "permitAll()";
    public static final String PAGE_WRITE = "hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')";
    public static final String PAGE_DELETE = "hasRole('ROLE_ADMIN')";

    // Post management
    public static final String POST_READ = "permitAll()";
    public static final String POST_WRITE = "hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')";
    public static final String POST_DELETE = "hasRole('ROLE_ADMIN')";

    // Comment management
    public static final String COMMENT_READ = "permitAll()";
    public static final String COMMENT_WRITE = "isAuthenticated()";
    public static final String COMMENT_MODERATE = "hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')";

    // Homepage management
    public static final String HOMEPAGE_READ = "permitAll()";
    public static final String HOMEPAGE_WRITE = "hasRole('ROLE_ADMIN')";

    // User management
    public static final String USER_READ = "hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')";
    public static final String USER_WRITE = "hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')";
    public static final String USER_DELETE = "hasRole('ROLE_ADMIN')";

    // User Bio management
    public static final String USER_BIO_READ = "permitAll()";
    public static final String USER_BIO_WRITE = "hasAnyRole('ROLE_ADMIN', 'ROLE_USER') and @userSecurity.isCurrentUser(#userId)";
    public static final String USER_BIO_ADMIN = "hasRole('ROLE_ADMIN')";

    // Role management
    public static final String ROLE_READ = "hasRole('ROLE_ADMIN')";
    public static final String ROLE_WRITE = "hasRole('ROLE_ADMIN')";

    // Pages Users management
    public static final String PAGES_USERS_READ = "permitAll()";
    public static final String PAGES_USERS_WRITE = "hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')";

    // Posts Users management
    public static final String POSTS_USERS_READ = "permitAll()";
    public static final String POSTS_USERS_WRITE = "hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')";

    // Session management
    public static final String SESSION_READ = "hasAnyRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#userId)";
    public static final String SESSION_WRITE = "hasAnyRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#userId)";
    public static final String SESSION_DELETE = "hasAnyRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#userId)";
    public static final String SESSION_ADMIN = "hasRole('ROLE_ADMIN')";

    private Authorities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
