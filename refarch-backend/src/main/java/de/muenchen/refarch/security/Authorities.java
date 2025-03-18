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
    // Common security expressions
    private static final String PERMIT_ALL = "permitAll()";
    private static final String ADMIN_ROLE = "hasRole('ROLE_ADMIN')";
    private static final String ADMIN_EDITOR_ROLES = "hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')";
    private static final String ADMIN_MODERATOR_ROLES = "hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')";
    private static final String ADMIN_USER_MANAGER_ROLES = "hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGER')";
    private static final String AUTHENTICATED = "isAuthenticated()";
    private static final String CURRENT_USER = "@userSecurity.isCurrentUser(#userId)";
    private static final String ADMIN_OR_CURRENT_USER = "hasAnyRole('ROLE_ADMIN') or " + CURRENT_USER;

    // Global Settings
    public static final String SETTINGS_READ = PERMIT_ALL;
    public static final String SETTINGS_WRITE = ADMIN_ROLE;

    // Link management
    public static final String LINK_READ = PERMIT_ALL;
    public static final String LINK_WRITE = ADMIN_ROLE;

    // Language management
    public static final String LANGUAGE_READ = PERMIT_ALL;
    public static final String LANGUAGE_WRITE = ADMIN_ROLE;

    // Page management
    public static final String PAGE_READ = PERMIT_ALL;
    public static final String PAGE_WRITE = ADMIN_EDITOR_ROLES;
    public static final String PAGE_DELETE = ADMIN_ROLE;

    // Post management
    public static final String POST_READ = PERMIT_ALL;
    public static final String POST_WRITE = ADMIN_EDITOR_ROLES;
    public static final String POST_DELETE = ADMIN_ROLE;

    // Comment management
    public static final String COMMENT_READ = PERMIT_ALL;
    public static final String COMMENT_WRITE = AUTHENTICATED;
    public static final String COMMENT_MODERATE = ADMIN_MODERATOR_ROLES;

    // Homepage management
    public static final String HOMEPAGE_READ = PERMIT_ALL;
    public static final String HOMEPAGE_WRITE = ADMIN_ROLE;

    // User management
    public static final String USER_READ = ADMIN_USER_MANAGER_ROLES;
    public static final String USER_WRITE = ADMIN_USER_MANAGER_ROLES;
    public static final String USER_DELETE = ADMIN_ROLE;

    // User Bio management
    public static final String USER_BIO_READ = PERMIT_ALL;
    public static final String USER_BIO_WRITE = ADMIN_USER_MANAGER_ROLES + " and " + CURRENT_USER;
    public static final String USER_BIO_ADMIN = ADMIN_ROLE;

    // Role management
    public static final String ROLE_READ = ADMIN_ROLE;
    public static final String ROLE_WRITE = ADMIN_ROLE;

    // Pages Users management
    public static final String PAGES_USERS_READ = PERMIT_ALL;
    public static final String PAGES_USERS_WRITE = ADMIN_EDITOR_ROLES;

    // Posts Users management
    public static final String POSTS_USERS_READ = PERMIT_ALL;
    public static final String POSTS_USERS_WRITE = ADMIN_EDITOR_ROLES;

    // Session management
    public static final String SESSION_READ = ADMIN_OR_CURRENT_USER;
    public static final String SESSION_WRITE = ADMIN_OR_CURRENT_USER;
    public static final String SESSION_DELETE = ADMIN_OR_CURRENT_USER;
    public static final String SESSION_ADMIN = ADMIN_ROLE;

    private Authorities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
