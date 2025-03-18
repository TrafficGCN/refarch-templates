package de.muenchen.refarch.homepage.content;

import de.muenchen.refarch.homepage.Homepage;
import de.muenchen.refarch.language.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HomepageContentTest {

    private HomepageContent content;
    private Homepage homepage;
    private Language language;

    @BeforeEach
    void setUp() {
        final LocalDateTime now = LocalDateTime.now();

        homepage = new Homepage();
        homepage.setId(UUID.randomUUID());
        homepage.setThumbnail("thumbnail.jpg");
        homepage.setCreatedAt(now);
        homepage.setUpdatedAt(now);

        language = new Language();
        language.setId(UUID.randomUUID());
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");

        content = new HomepageContent();
        content.setId(UUID.randomUUID());
        content.setLanguage(language);
        content.setWelcomeMessage("Welcome");
        content.setWelcomeMessageExtended("Extended Welcome");
        content.setExploreOurWork("Explore Our Work");
        content.setGetInvolved("Get Involved");
        content.setImportantLinks("Important Links");
        content.setEcosystemLinks("Ecosystem Links");
        content.setBlog("Blog");
        content.setPapers("Papers");
        content.setReadMore("Read More");
        content.setCreatedAt(now);
        content.setUpdatedAt(now);

        // Properly establish bidirectional relationship
        homepage.addContent(content);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        final UUID id = UUID.randomUUID();
        final Homepage newHomepage = new Homepage();
        final Language newLanguage = new Language();
        final String welcomeMessage = "New Welcome";
        final String welcomeMessageExtended = "New Extended Welcome";
        final String exploreOurWork = "New Explore Our Work";
        final String getInvolved = "New Get Involved";
        final String importantLinks = "New Important Links";
        final String ecosystemLinks = "New Ecosystem Links";
        final String blog = "New Blog";
        final String papers = "New Papers";
        final String readMore = "New Read More";
        final LocalDateTime newTime = LocalDateTime.now();

        content.setId(id);
        content.setHomepage(newHomepage);
        content.setLanguage(newLanguage);
        content.setWelcomeMessage(welcomeMessage);
        content.setWelcomeMessageExtended(welcomeMessageExtended);
        content.setExploreOurWork(exploreOurWork);
        content.setGetInvolved(getInvolved);
        content.setImportantLinks(importantLinks);
        content.setEcosystemLinks(ecosystemLinks);
        content.setBlog(blog);
        content.setPapers(papers);
        content.setReadMore(readMore);
        content.setCreatedAt(newTime);
        content.setUpdatedAt(newTime);

        assertThat(content.getId()).isEqualTo(id);
        assertThat(content.getHomepage()).isEqualTo(newHomepage);
        assertThat(content.getLanguage()).isEqualTo(newLanguage);
        assertThat(content.getWelcomeMessage()).isEqualTo(welcomeMessage);
        assertThat(content.getWelcomeMessageExtended()).isEqualTo(welcomeMessageExtended);
        assertThat(content.getExploreOurWork()).isEqualTo(exploreOurWork);
        assertThat(content.getGetInvolved()).isEqualTo(getInvolved);
        assertThat(content.getImportantLinks()).isEqualTo(importantLinks);
        assertThat(content.getEcosystemLinks()).isEqualTo(ecosystemLinks);
        assertThat(content.getBlog()).isEqualTo(blog);
        assertThat(content.getPapers()).isEqualTo(papers);
        assertThat(content.getReadMore()).isEqualTo(readMore);
        assertThat(content.getCreatedAt()).isEqualTo(newTime);
        assertThat(content.getUpdatedAt()).isEqualTo(newTime);
    }

    @Test
    void relationships_ShouldBeSetCorrectly() {
        assertThat(content.getHomepage()).isEqualTo(homepage);
        assertThat(content.getLanguage()).isEqualTo(language);
        assertThat(homepage.getContents()).contains(content);
    }
}
