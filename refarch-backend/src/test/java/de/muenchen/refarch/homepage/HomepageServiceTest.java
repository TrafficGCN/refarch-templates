package de.muenchen.refarch.homepage;

import de.muenchen.refarch.homepage.content.HomepageContent;
import de.muenchen.refarch.homepage.content.HomepageContentRepository;
import de.muenchen.refarch.homepage.content.dto.HomepageContentRequestDTO;
import de.muenchen.refarch.homepage.content.dto.HomepageContentResponseDTO;
import de.muenchen.refarch.homepage.dto.HomepageRequestDTO;
import de.muenchen.refarch.homepage.dto.HomepageResponseDTO;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.link.LinkService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomepageServiceTest {

    @Mock
    private HomepageRepository homepageRepository;

    @Mock
    private HomepageContentRepository homepageContentRepository;

    @Mock
    private LinkService linkService;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private HomepageService homepageService;

    private UUID homepageId;
    private UUID linkId;
    private UUID languageId;
    private Homepage homepage;
    private Link link;
    private Language language;
    private HomepageContent content;
    private HomepageRequestDTO homepageRequestDTO;
    private HomepageContentRequestDTO contentRequestDTO;

    @BeforeEach
    void setUp() {
        homepageId = UUID.randomUUID();
        linkId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        link = new Link();
        link.setId(linkId);
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.EXTERNAL);

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");

        homepage = new Homepage();
        homepage.setId(homepageId);
        homepage.setLink(link);
        homepage.setThumbnail("thumbnail.jpg");
        homepage.setCreatedAt(now);
        homepage.setUpdatedAt(now);

        content = new HomepageContent();
        content.setId(UUID.randomUUID());
        content.setHomepage(homepage);
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

        homepage.addContent(content);

        homepageRequestDTO = new HomepageRequestDTO(
                linkId,
                "thumbnail.jpg");

        contentRequestDTO = new HomepageContentRequestDTO(
                languageId,
                "Welcome",
                "Extended Welcome",
                "Explore Our Work",
                "Get Involved",
                "Important Links",
                "Ecosystem Links",
                "Blog",
                "Papers",
                "Read More");
    }

    @Test
    void findAll_ShouldReturnAllHomepages() {
        when(homepageRepository.findAll()).thenReturn(List.of(homepage));

        final List<HomepageResponseDTO> result = homepageService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(homepageId);
        verify(homepageRepository).findAll();
    }

    @Test
    void findById_WhenHomepageExists_ShouldReturnHomepage() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.of(homepage));

        final HomepageResponseDTO result = homepageService.findById(homepageId);

        assertThat(result.id()).isEqualTo(homepageId);
        verify(homepageRepository).findById(homepageId);
    }

    @Test
    void findById_WhenHomepageDoesNotExist_ShouldThrowException() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homepageService.findById(homepageId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Homepage not found with id: " + homepageId);
        verify(homepageRepository).findById(homepageId);
    }

    @Test
    void create_ShouldCreateHomepage() {
        when(linkService.getById(linkId)).thenReturn(link);
        when(homepageRepository.save(any(Homepage.class))).thenReturn(homepage);

        final HomepageResponseDTO result = homepageService.create(homepageRequestDTO);

        assertThat(result.id()).isEqualTo(homepageId);
        assertThat(result.linkId()).isEqualTo(linkId);
        assertThat(result.thumbnail()).isEqualTo("thumbnail.jpg");
        verify(linkService).getById(linkId);
        verify(homepageRepository).save(any(Homepage.class));
    }

    @Test
    void update_WhenHomepageExists_ShouldUpdateHomepage() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.of(homepage));
        when(linkService.getById(linkId)).thenReturn(link);
        when(homepageRepository.save(any(Homepage.class))).thenReturn(homepage);

        final HomepageResponseDTO result = homepageService.update(homepageId, homepageRequestDTO);

        assertThat(result.id()).isEqualTo(homepageId);
        verify(homepageRepository).findById(homepageId);
        verify(linkService).getById(linkId);
        verify(homepageRepository).save(any(Homepage.class));
    }

    @Test
    void delete_WhenHomepageExists_ShouldDeleteHomepage() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.of(homepage));
        doNothing().when(homepageContentRepository).deleteAll(homepage.getContents());
        doNothing().when(homepageRepository).delete(homepage);

        homepageService.delete(homepageId);

        verify(homepageRepository).findById(homepageId);
        verify(homepageContentRepository).deleteAll(homepage.getContents());
        verify(homepageRepository).delete(homepage);
    }

    @Test
    void findAllContentByHomepage_WhenHomepageExists_ShouldReturnAllContent() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.of(homepage));

        final List<HomepageContentResponseDTO> result = homepageService.findAllContentByHomepage(homepageId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(content.getId());
        verify(homepageRepository).findById(homepageId);
    }

    @Test
    void findContentByHomepageAndLanguage_WhenContentExists_ShouldReturnContent() {
        when(homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId))
                .thenReturn(Optional.of(content));

        final HomepageContentResponseDTO result = homepageService.findContentByHomepageAndLanguage(homepageId, languageId);

        assertThat(result.id()).isEqualTo(content.getId());
        verify(homepageContentRepository).findByHomepageIdAndLanguageId(homepageId, languageId);
    }

    @Test
    void createContent_WhenContentDoesNotExist_ShouldCreateContent() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.of(homepage));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(homepageContentRepository.existsByHomepageIdAndLanguageId(homepageId, languageId)).thenReturn(false);
        when(homepageContentRepository.save(any(HomepageContent.class))).thenReturn(content);

        final HomepageContentResponseDTO result = homepageService.createContent(homepageId, contentRequestDTO);

        assertThat(result.id()).isEqualTo(content.getId());
        verify(homepageRepository).findById(homepageId);
        verify(languageService).getLanguageById(languageId);
        verify(homepageContentRepository).existsByHomepageIdAndLanguageId(homepageId, languageId);
        verify(homepageContentRepository).save(any(HomepageContent.class));
    }

    @Test
    void createContent_WhenContentExists_ShouldThrowException() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.of(homepage));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(homepageContentRepository.existsByHomepageIdAndLanguageId(homepageId, languageId)).thenReturn(true);

        assertThatThrownBy(() -> homepageService.createContent(homepageId, contentRequestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(String.format("Content already exists for homepage %s and language %s",
                        homepageId, language.getAbbreviation()));
        verify(homepageRepository).findById(homepageId);
        verify(languageService).getLanguageById(languageId);
        verify(homepageContentRepository).existsByHomepageIdAndLanguageId(homepageId, languageId);
        verify(homepageContentRepository, never()).save(any(HomepageContent.class));
    }

    @Test
    void updateContent_WhenContentExists_ShouldUpdateContent() {
        when(homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId))
                .thenReturn(Optional.of(content));
        when(homepageContentRepository.save(any(HomepageContent.class))).thenReturn(content);

        final HomepageContentResponseDTO result = homepageService.updateContent(homepageId, languageId, contentRequestDTO);

        assertThat(result.id()).isEqualTo(content.getId());
        verify(homepageContentRepository).findByHomepageIdAndLanguageId(homepageId, languageId);
        verify(homepageContentRepository).save(any(HomepageContent.class));
    }

    @Test
    void deleteContent_WhenContentExists_ShouldDeleteContent() {
        when(homepageRepository.findById(homepageId)).thenReturn(Optional.of(homepage));
        when(homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId))
                .thenReturn(Optional.of(content));
        doNothing().when(homepageContentRepository).delete(content);

        homepageService.deleteContent(homepageId, languageId);

        verify(homepageRepository).findById(homepageId);
        verify(homepageContentRepository).findByHomepageIdAndLanguageId(homepageId, languageId);
        verify(homepageContentRepository).delete(content);
    }
}
