package de.muenchen.refarch.configuration;

import de.muenchen.refarch.homepage.Homepage;
import de.muenchen.refarch.homepage.content.HomepageContent;
import de.muenchen.refarch.homepage.HomepageRepository;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageRepository;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkRepository;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.page.Page;
import de.muenchen.refarch.page.PageRepository;
import de.muenchen.refarch.page.content.PageContent;
import de.muenchen.refarch.post.Post;
import de.muenchen.refarch.post.PostRepository;
import de.muenchen.refarch.post.content.PostContent;
import de.muenchen.refarch.role.Role;
import de.muenchen.refarch.role.RoleRepository;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import de.muenchen.refarch.user.bio.UserBio;
import de.muenchen.refarch.user.bio.UserBioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final String[] DEFAULT_ROLES = {
            "ROLE_ADMIN",
            "ROLE_USER",
            "ROLE_EDITOR",
            "ROLE_MODERATOR"
    };

    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HomepageRepository homepageRepository;
    private final LinkRepository linkRepository;
    private final UserBioRepository userBioRepository;
    private final PageRepository pageRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void run(final String... args) {
        log.info("Checking database initialization status...");

        // Initialize languages if needed
        final Language english = getOrCreateEnglishLanguage();

        // Initialize roles if needed
        if (roleRepository.count() == 0) {
            createDefaultRoles();
        }

        // Get required roles for admin user
        final Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found after initialization"));
        final Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found after initialization"));

        // Initialize admin user if needed
        if (userRepository.count() == 0) {
            createUserBio(createAdminUser(adminRole, userRole), english);
        }

        // Initialize homepage if needed
        if (homepageRepository.count() == 0) {
            createHomepage(english);
        }

        // Initialize sample page if needed
        if (pageRepository.count() == 0) {
            createSamplePage(english);
        }

        // Initialize sample blog post if needed
        if (postRepository.count() == 0) {
            createSamplePost(english);
        }

        log.info("Database initialization check completed");
    }

    private void createDefaultRoles() {
        log.info("Creating default roles: {}", Arrays.toString(DEFAULT_ROLES));
        Arrays.stream(DEFAULT_ROLES).forEach(roleName -> {
            final Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        });
    }

    private Language getOrCreateEnglishLanguage() {
        // Find English language by abbreviation if it exists
        return languageRepository.findAll().stream()
                .filter(lang -> "en".equals(lang.getAbbreviation()))
                .findFirst()
                .orElseGet(() -> {
                    log.info("Creating default language: English");
                    return createEnglishLanguage();
                });
    }

    private Language createEnglishLanguage() {
        final Language english = new Language();
        english.setName("English");
        english.setAbbreviation("en");
        english.setFontAwesomeIcon("fa-flag-usa");
        english.setMdiIcon("mdi-flag-usa");
        return languageRepository.save(english);
    }

    private User createAdminUser(final Role adminRole, final Role userRole) {
        log.info("Creating admin user");
        final User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin")); // Change in production!
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setTitle("System Administrator");
        adminUser.setAffiliation("RefArch CMS");
        adminUser.setRoles(Set.of(adminRole, userRole));
        return userRepository.save(adminUser);
    }

    private void createUserBio(final User user, final Language language) {
        log.info("Creating bio for user: {}", user.getUsername());
        final UserBio bio = new UserBio();
        bio.setUser(user);
        bio.setLanguage(language);
        bio.setBio("""
                As a system administrator for RefArch CMS, I am responsible for maintaining and improving
                the content management system. With extensive experience in web development and system
                administration, I ensure that our platform remains secure, efficient, and user-friendly.

                My focus areas include:
                - System security and performance optimization
                - User management and access control
                - Content workflow improvements
                - Technical documentation
                """);
        userBioRepository.save(bio);
    }

    private void createSamplePage(final Language language) {
        log.info("Creating sample page");

        // Create page link
        final Link pageLink = new Link();
        pageLink.setName("About Us");
        pageLink.setUrl("/about");
        pageLink.setType("navigation");
        pageLink.setScope(LinkScope.INTERNAL);
        pageLink.setFontAwesomeIcon("fa-info-circle");
        pageLink.setMdiIcon("mdi-information");
        linkRepository.save(pageLink);

        // Create page
        final Page page = new Page();
        page.setLink(pageLink);
        page.setCommentsEnabled(true);
        page.setPublished(true);

        // Create page content
        final PageContent content = new PageContent();
        content.setLanguage(language);
        content.setTitle("About RefArch CMS");
        content.setContent("""
                # About RefArch CMS

                RefArch CMS is a modern, flexible content management system built with Spring Boot.
                Our platform provides a robust foundation for managing digital content with ease
                and efficiency.

                ## Our Mission

                We aim to simplify content management while maintaining high standards of security,
                performance, and user experience. Our system is designed to be both powerful and
                intuitive, serving the needs of content creators and technical administrators alike.

                ## Key Features

                - Multi-language support for global reach
                - Role-based access control for security
                - Modern API design for integration
                - Flexible content management
                - Real-time collaboration tools
                """);
        content.setShortDescription("Learn about RefArch CMS, our mission, and our commitment to excellence in content management.");
        content.setKeywords("cms, content management, spring boot, java, web application");

        page.addContent(content);
        pageRepository.save(page);
    }

    private void createSamplePost(final Language language) {
        log.info("Creating sample blog post");

        // Create post link
        final Link postLink = new Link();
        postLink.setName("Welcome to RefArch CMS");
        postLink.setUrl("/welcome-to-refarch-cms-blog");
        postLink.setType("post");
        postLink.setScope(LinkScope.INTERNAL);
        postLink.setFontAwesomeIcon("fa-newspaper");
        postLink.setMdiIcon("mdi-post");
        linkRepository.save(postLink);

        // Create post
        final Post post = new Post();
        post.setLink(postLink);
        post.setCommentsEnabled(true);
        post.setPublished(true);

        // Create post content
        final PostContent content = new PostContent();
        content.setLanguage(language);
        content.setTitle("Welcome to RefArch CMS Blog");
        content.setContent("""
                # Welcome to Our Blog

                We're excited to launch the RefArch CMS blog! This space will serve as a hub for
                announcements, tutorials, best practices, and insights into content management.

                ## What to Expect

                Our blog will cover various topics, including:
                - Product updates and new features
                - Technical tutorials and guides
                - Content management best practices
                - Case studies and success stories
                - Community highlights

                ## Getting Started
                """);
        content.setShortDescription("Welcome to the official RefArch CMS blog! Stay tuned for updates, tutorials, and best practices.");
        content.setKeywords("blog, welcome, cms, content management, announcements");

        post.addContent(content);
        postRepository.save(post);
    }

    private void createHomepage(final Language english) {
        log.info("Creating homepage");

        // Create homepage link
        final Link homepageLink = new Link();
        homepageLink.setName("Homepage");
        homepageLink.setUrl("/");
        homepageLink.setType("homepage");
        homepageLink.setScope(LinkScope.INTERNAL);
        homepageLink.setFontAwesomeIcon("fa-home");
        homepageLink.setMdiIcon("mdi-home");
        linkRepository.save(homepageLink);

        // Create homepage
        final Homepage homepage = new Homepage();
        homepage.setLink(homepageLink);
        homepage.setThumbnail("homepage-thumbnail.jpg");

        // Create homepage content
        final HomepageContent content = new HomepageContent();
        content.setLanguage(english);
        content.setWelcomeMessage("Welcome to RefArch CMS");
        content.setWelcomeMessageExtended("Your Modern Content Management Solution");
        content.setExploreOurWork("Explore Our Features");
        content.setGetInvolved("Get Started Today");
        content.setImportantLinks("Essential Resources");
        content.setEcosystemLinks("Our Ecosystem");
        content.setBlog("Latest Blog Posts");
        content.setPapers("Documentation");
        content.setReadMore("Read More");

        homepage.addContent(content);
        homepageRepository.save(homepage);
    }
}
