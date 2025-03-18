package de.muenchen.refarch.common;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.page.Page;
import de.muenchen.refarch.post.Post;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.homepage.Homepage;

/**
 * Utility class for creating defensive copies of entities.
 */
public final class EntityCopyUtils {

    private EntityCopyUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a defensive copy of a User entity.
     *
     * @param user The user to copy
     * @return A defensive copy of the user, or null if the input is null
     */
    public static User copyUser(final User user) {
        if (user == null) {
            return null;
        }
        final User copy = new User();
        copy.setId(user.getId());
        copy.setUsername(user.getUsername());
        copy.setFirstName(user.getFirstName());
        copy.setLastName(user.getLastName());
        copy.setTitle(user.getTitle());
        copy.setAffiliation(user.getAffiliation());
        copy.setThumbnail(user.getThumbnail());
        copy.setCreatedAt(user.getCreatedAt());
        copy.setUpdatedAt(user.getUpdatedAt());
        return copy;
    }

    /**
     * Creates a defensive copy of a Language entity.
     *
     * @param language The language to copy
     * @return A defensive copy of the language, or null if the input is null
     */
    public static Language copyLanguage(final Language language) {
        if (language == null) {
            return null;
        }
        final Language copy = new Language();
        copy.setId(language.getId());
        copy.setName(language.getName());
        copy.setAbbreviation(language.getAbbreviation());
        copy.setFontAwesomeIcon(language.getFontAwesomeIcon());
        copy.setMdiIcon(language.getMdiIcon());
        return copy;
    }

    /**
     * Creates a defensive copy of a Post entity.
     *
     * @param post The post to copy
     * @return A defensive copy of the post, or null if the input is null
     */
    public static Post copyPost(final Post post) {
        if (post == null) {
            return null;
        }
        final Post copy = new Post();
        copy.setId(post.getId());
        copy.setLink(post.getLink()); // Link is already defensively copied in its getter
        copy.setThumbnail(post.getThumbnail());
        copy.setCommentsEnabled(post.isCommentsEnabled());
        copy.setPublished(post.isPublished());
        copy.setCreatedAt(post.getCreatedAt());
        copy.setUpdatedAt(post.getUpdatedAt());
        return copy;
    }

    /**
     * Creates a defensive copy of a Link entity.
     *
     * @param link The link to copy
     * @return A defensive copy of the link, or null if the input is null
     */
    public static Link copyLink(final Link link) {
        if (link == null) {
            return null;
        }
        final Link copy = new Link();
        copy.setId(link.getId());
        copy.setUrl(link.getUrl());
        copy.setName(link.getName());
        copy.setFontAwesomeIcon(link.getFontAwesomeIcon());
        copy.setMdiIcon(link.getMdiIcon());
        copy.setType(link.getType());
        copy.setScope(link.getScope());
        return copy;
    }

    /**
     * Creates a defensive copy of a Page entity.
     *
     * @param page The page to copy
     * @return A defensive copy of the page, or null if the input is null
     */
    public static Page copyPage(final Page page) {
        if (page == null) {
            return null;
        }
        final Page copy = new Page();
        copy.setId(page.getId());
        copy.setLink(copyLink(page.getLink()));
        copy.setThumbnail(page.getThumbnail());
        copy.setCommentsEnabled(page.isCommentsEnabled());
        copy.setPublished(page.isPublished());
        copy.setCreatedAt(page.getCreatedAt());
        copy.setUpdatedAt(page.getUpdatedAt());
        return copy;
    }

    /**
     * Creates a defensive copy of a Homepage entity.
     *
     * @param homepage The homepage to copy
     * @return A defensive copy of the homepage, or null if the input is null
     */
    public static Homepage copyHomepage(final Homepage homepage) {
        if (homepage == null) {
            return null;
        }
        final Homepage copy = new Homepage();
        copy.setId(homepage.getId());
        copy.setLink(copyLink(homepage.getLink()));
        copy.setThumbnail(homepage.getThumbnail());
        copy.setCreatedAt(homepage.getCreatedAt());
        copy.setUpdatedAt(homepage.getUpdatedAt());
        return copy;
    }
}
