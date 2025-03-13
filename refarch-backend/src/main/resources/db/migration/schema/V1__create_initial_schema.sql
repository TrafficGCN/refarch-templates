-- Create links table
CREATE TABLE links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    font_awesome_icon VARCHAR(255),
    mdi_icon VARCHAR(255),
    type VARCHAR(255),
    scope VARCHAR(255) NOT NULL CHECK (scope IN ('internal', 'external')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create languages table
CREATE TABLE languages_i18n (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    abbreviation VARCHAR(255) NOT NULL,
    font_awesome_icon VARCHAR(255) NOT NULL,
    mdi_icon VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    title VARCHAR(255),
    affiliation VARCHAR(255),
    thumbnail VARCHAR(510),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user bios table (i18n)
CREATE TABLE user_bios_i18n (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_id UUID NOT NULL,
    bio TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (language_id) REFERENCES languages_i18n(id) ON DELETE CASCADE,
    UNIQUE (user_id, language_id)
);

-- Create pages table
CREATE TABLE pages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id UUID,
    thumbnail VARCHAR(510),
    comments_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE
);

-- Create pages content table (i18n)
CREATE TABLE pages_content_i18n (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    page_id UUID NOT NULL,
    language_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    short_description TEXT,
    keywords TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    FOREIGN KEY (language_id) REFERENCES languages_i18n(id) ON DELETE CASCADE,
    UNIQUE (page_id, language_id)
);

-- Create posts table
CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id UUID,
    thumbnail VARCHAR(510),
    comments_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE
);

-- Create posts content table (i18n)
CREATE TABLE posts_content_i18n (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL,
    language_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    short_description TEXT,
    keywords TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (language_id) REFERENCES languages_i18n(id) ON DELETE CASCADE,
    UNIQUE (post_id, language_id)
);

-- Create homepage table
CREATE TABLE homepage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id UUID,
    thumbnail VARCHAR(510),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE
);

-- Create homepage content table (i18n)
CREATE TABLE homepage_content_i18n (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    homepage_id UUID NOT NULL,
    language_id UUID NOT NULL,
    welcome_message TEXT NOT NULL,
    welcome_message_extended TEXT,
    explore_our_work TEXT,
    get_involved TEXT,
    important_links TEXT,
    ecosystem_links TEXT,
    blog TEXT,
    papers TEXT,
    read_more TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (homepage_id) REFERENCES homepage(id) ON DELETE CASCADE,
    FOREIGN KEY (language_id) REFERENCES languages_i18n(id) ON DELETE CASCADE,
    UNIQUE (homepage_id, language_id)
);

-- Create posts_users junction table
CREATE TABLE posts_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_link_id UUID,
    user_id UUID,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (post_link_id, user_id)
);

-- Create comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID,
    page_id UUID,
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (
        (post_id IS NOT NULL AND page_id IS NULL) OR
        (post_id IS NULL AND page_id IS NOT NULL)
    )
); 