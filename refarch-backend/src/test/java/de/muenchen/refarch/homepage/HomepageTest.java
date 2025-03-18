package de.muenchen.refarch.homepage;

import de.muenchen.refarch.homepage.content.HomepageContent;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.link.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HomepageTest {

    private Homepage homepage;
    private HomepageContent content;

    @BeforeEach
    void setUp() {
        final LocalDateTime now = LocalDateTime.now();

        final Link link = new Link();
        link.setId(UUID.randomUUID());
        link.setUrl("https://example.com");
        link.setName("Example Link");

        final Language language = new Language();
        language.setId(UUID.randomUUID());
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");

        homepage = new Homepage();
        homepage.setId(UUID.randomUUID());
        homepage.setLink(link);
        homepage.setThumbnail("thumbnail.jpg");
        homepage.setCreatedAt(now);
        homepage.setUpdatedAt(now);

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
    }

    @Test
    void addContent_ShouldAddContentAndSetHomepage() {
        homepage.addContent(content);

        assertThat(homepage.getContents()).contains(content);
        assertThat(content.getHomepage()).isEqualTo(homepage);
    }

    @Test
    void removeContent_ShouldRemoveContentAndUnsetHomepage() {
        homepage.addContent(content);
        homepage.removeContent(content);

        assertThat(homepage.getContents()).doesNotContain(content);
        assertThat(content.getHomepage()).isNull();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        final UUID id = UUID.randomUUID();
        final Link newLink = new Link();
        final String newThumbnail = "new-thumbnail.jpg";
        final LocalDateTime newTime = LocalDateTime.now();

        homepage.setId(id);
        homepage.setLink(newLink);
        homepage.setThumbnail(newThumbnail);
        homepage.setCreatedAt(newTime);
        homepage.setUpdatedAt(newTime);

        assertThat(homepage.getId()).isEqualTo(id);
        assertThat(homepage.getLink()).isEqualTo(newLink);
        assertThat(homepage.getThumbnail()).isEqualTo(newThumbnail);
        assertThat(homepage.getCreatedAt()).isEqualTo(newTime);
        assertThat(homepage.getUpdatedAt()).isEqualTo(newTime);
    }
}
