package de.muenchen.refarch.page;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.link.LinkService;
import de.muenchen.refarch.page.content.PageContent;
import de.muenchen.refarch.page.content.PageContentRepository;
import de.muenchen.refarch.page.dto.PageRequestDTO;
import de.muenchen.refarch.page.dto.PageResponseDTO;
import de.muenchen.refarch.page.content.dto.PageContentRequestDTO;
import de.muenchen.refarch.page.content.dto.PageContentResponseDTO;
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
class PageServiceTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private PageContentRepository pageContentRepository;

    @Mock
    private LinkService linkService;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private PageService pageService;

    private UUID pageId;
    private UUID linkId;
    private UUID languageId;
    private Page page;
    private Link link;
    private Language language;
    private PageContent pageContent;
    private PageRequestDTO pageRequestDTO;
    private PageContentRequestDTO contentRequestDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        pageId = UUID.randomUUID();
        linkId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        now = LocalDateTime.now();

        link = new Link();
        link.setId(linkId);
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.EXTERNAL);

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");

        page = new Page();
        page.setId(pageId);
        page.setLink(link);
        page.setThumbnail("thumbnail.jpg");
        page.setCommentsEnabled(true);
        page.setCreatedAt(now);
        page.setUpdatedAt(now);

        pageContent = new PageContent();
        pageContent.setId(UUID.randomUUID());
        pageContent.setPage(page);
        pageContent.setLanguage(language);
        pageContent.setTitle("Test Title");
        pageContent.setContent("Test Content");
        pageContent.setShortDescription("Test Description");
        pageContent.setKeywords("test,keywords");
        pageContent.setCreatedAt(now);
        pageContent.setUpdatedAt(now);

        pageRequestDTO = new PageRequestDTO(
                linkId,
                "thumbnail.jpg",
                true,
                true);

        contentRequestDTO = new PageContentRequestDTO(
                languageId,
                "Test Title",
                "Test Content",
                "Test Description",
                "test,keywords");
    }

    @Test
    void findAll_ShouldReturnAllPages() {
        when(pageRepository.findAll()).thenReturn(List.of(page));

        final List<PageResponseDTO> result = pageService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(pageId);
        verify(pageRepository).findAll();
    }

    @Test
    void findById_WhenPageExists_ShouldReturnPage() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));

        final PageResponseDTO result = pageService.findById(pageId);

        assertThat(result.id()).isEqualTo(pageId);
        verify(pageRepository).findById(pageId);
    }

    @Test
    void findById_WhenPageDoesNotExist_ShouldThrowException() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pageService.findById(pageId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Page not found with id: " + pageId);
        verify(pageRepository).findById(pageId);
    }

    @Test
    void create_ShouldCreatePage() {
        when(linkService.getById(linkId)).thenReturn(link);
        when(pageRepository.save(any(Page.class))).thenReturn(page);

        final PageResponseDTO result = pageService.create(pageRequestDTO);

        assertThat(result.id()).isEqualTo(pageId);
        assertThat(result.linkId()).isEqualTo(linkId);
        assertThat(result.thumbnail()).isEqualTo("thumbnail.jpg");
        assertThat(result.commentsEnabled()).isTrue();
        verify(linkService).getById(linkId);
        verify(pageRepository).save(any(Page.class));
    }

    @Test
    void update_WhenPageExists_ShouldUpdatePage() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(linkService.getById(linkId)).thenReturn(link);
        when(pageRepository.save(any(Page.class))).thenReturn(page);

        final PageResponseDTO result = pageService.update(pageId, pageRequestDTO);

        assertThat(result.id()).isEqualTo(pageId);
        verify(pageRepository).findById(pageId);
        verify(linkService).getById(linkId);
        verify(pageRepository).save(any(Page.class));
    }

    @Test
    void delete_WhenPageExists_ShouldDeletePage() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        doNothing().when(pageContentRepository).deleteAll(page.getContents());
        doNothing().when(pageRepository).delete(page);

        pageService.delete(pageId);

        verify(pageRepository).findById(pageId);
        verify(pageContentRepository).deleteAll(page.getContents());
        verify(pageRepository).delete(page);
    }

    @Test
    void findAllContentByPage_WhenPageExists_ShouldReturnAllContent() {
        // Create a second language
        final Language secondLanguage = new Language();
        secondLanguage.setId(UUID.randomUUID());
        secondLanguage.setName("German");
        secondLanguage.setAbbreviation("de");

        // Create a second content for testing
        final PageContent secondContent = new PageContent();
        secondContent.setId(UUID.randomUUID());
        secondContent.setPage(page);
        secondContent.setLanguage(secondLanguage);
        secondContent.setTitle("Second Title");
        secondContent.setContent("Second Content");
        secondContent.setShortDescription("Second Description");
        secondContent.setKeywords("second,keywords");
        secondContent.setCreatedAt(now);
        secondContent.setUpdatedAt(now);

        // Add both contents to the page using the helper method
        page.addContent(pageContent);
        page.addContent(secondContent);

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));

        final List<PageContentResponseDTO> result = pageService.findAllContentByPage(pageId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("title")
                .containsExactlyInAnyOrder("Test Title", "Second Title");
        verify(pageRepository).findById(pageId);
    }

    @Test
    void findContentByPageAndLanguage_WhenContentExists_ShouldReturnContent() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)).thenReturn(Optional.of(pageContent));

        final PageContentResponseDTO result = pageService.findContentByPageAndLanguage(pageId, languageId);

        assertThat(result.id()).isEqualTo(pageContent.getId());
        verify(pageRepository).findById(pageId);
        verify(languageService).getLanguageById(languageId);
        verify(pageContentRepository).findByPageIdAndLanguageId(pageId, languageId);
    }

    @Test
    void createContent_WhenContentDoesNotExist_ShouldCreateContent() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(pageContentRepository.existsByPageIdAndLanguageId(pageId, languageId)).thenReturn(false);
        when(pageContentRepository.save(any(PageContent.class))).thenReturn(pageContent);

        final PageContentResponseDTO result = pageService.createContent(pageId, contentRequestDTO);

        assertThat(result.id()).isEqualTo(pageContent.getId());
        verify(pageRepository).findById(pageId);
        verify(languageService).getLanguageById(languageId);
        verify(pageContentRepository).existsByPageIdAndLanguageId(pageId, languageId);
        verify(pageContentRepository).save(any(PageContent.class));
    }

    @Test
    void createContent_WhenContentExists_ShouldThrowException() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(pageContentRepository.existsByPageIdAndLanguageId(pageId, languageId)).thenReturn(true);

        assertThatThrownBy(() -> pageService.createContent(pageId, contentRequestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(String.format("Content already exists for page %s and language %s", pageId, language.getAbbreviation()));
        verify(pageRepository).findById(pageId);
        verify(languageService).getLanguageById(languageId);
        verify(pageContentRepository).existsByPageIdAndLanguageId(pageId, languageId);
        verify(pageContentRepository, never()).save(any(PageContent.class));
    }

    @Test
    void updateContent_WhenContentExists_ShouldUpdateContent() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)).thenReturn(Optional.of(pageContent));
        when(pageContentRepository.save(any(PageContent.class))).thenReturn(pageContent);

        final PageContentResponseDTO result = pageService.updateContent(pageId, languageId, contentRequestDTO);

        assertThat(result.id()).isEqualTo(pageContent.getId());
        verify(pageRepository).findById(pageId);
        verify(languageService).getLanguageById(languageId);
        verify(pageContentRepository).findByPageIdAndLanguageId(pageId, languageId);
        verify(pageContentRepository).save(any(PageContent.class));
    }

    @Test
    void deleteContent_WhenContentExists_ShouldDeleteContent() {
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(pageContentRepository.findByPageIdAndLanguageId(pageId, languageId))
                .thenReturn(Optional.of(pageContent));
        doNothing().when(pageContentRepository).delete(pageContent);

        pageService.deleteContent(pageId, languageId);

        verify(pageRepository).findById(pageId);
        verify(pageContentRepository).findByPageIdAndLanguageId(pageId, languageId);
        verify(pageContentRepository).delete(pageContent);
    }
}
