-- Create links table
CREATE TABLE links (
    id SERIAL PRIMARY KEY,
    link VARCHAR(255) NOT NULL,
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
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    abbreviation VARCHAR(255) NOT NULL,
    font_awesome_icon VARCHAR(255) NOT NULL,
    mdi_icon VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create homepage table
CREATE TABLE homepage (
    id SERIAL PRIMARY KEY,
    language_id INT,
    link_id INT,
    FOREIGN KEY (language_id) REFERENCES languages_i18n(id) ON DELETE CASCADE,
    FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE,
    welcome_message TEXT,
    welcome_message_extended TEXT,
    explore_our_work TEXT,
    get_involved TEXT,
    important_links TEXT,
    ecosystem_links TEXT,
    blog TEXT,
    papers TEXT,
    read_more TEXT,
    thumbnail VARCHAR(510),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    title VARCHAR(255),
    affiliation VARCHAR(255),
    bio TEXT,
    thumbnail VARCHAR(510),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create pages table
CREATE TABLE pages (
    id SERIAL PRIMARY KEY,
    language_id INT,
    link_id INT,
    FOREIGN KEY (language_id) REFERENCES languages_i18n(id) ON DELETE CASCADE,
    FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE,
    title VARCHAR(255),
    content TEXT,
    short_description TEXT,
    keywords TEXT,
    thumbnail VARCHAR(510),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create posts table
CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    language_id INT,
    link_id INT,
    FOREIGN KEY (language_id) REFERENCES languages_i18n(id) ON DELETE CASCADE,
    title VARCHAR(255),
    content TEXT,
    short_description TEXT,
    keywords TEXT,
    thumbnail VARCHAR(510),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create posts_users junction table
CREATE TABLE posts_users (
    id SERIAL PRIMARY KEY,
    post_link_id INT,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (post_link_id, user_id)
); 