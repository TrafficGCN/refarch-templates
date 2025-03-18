package de.muenchen.refarch.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.link.LinkService;
import de.muenchen.refarch.page.content.PageContentRepository;
import de.muenchen.refarch.page.content.dto.PageContentRequestDTO;
import de.muenchen.refarch.page.content.dto.PageContentResponseDTO;
import de.muenchen.refarch.page.dto.PageRequestDTO;
import de.muenchen.refarch.page.dto.PageResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(
        classes = { MicroServiceApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles(profiles = { TestConstants.SPRING_TEST_PROFILE, TestConstants.SPRING_NO_SECURITY_PROFILE })
@AutoConfigureMockMvc
@Import(TestConfig.class)
class PageControllerTest {
    // Constants
    private static final String API_PAGES = "/pages";
    private static final String API_PAGES_ID = "/pages/{id}";
    private static final String API_PAGES_CONTENT = "/pages/{pageId}/content";
    private static final String API_PAGES_CONTENT_LANGUAGE = "/pages/{pageId}/content/{languageId}";
    private static final String TEST_TITLE = "Test Page";
    private static final String TEST_SLUG = "test-page";
    private static final String TEST_DESCRIPTION = "Test page description";
    private static final String TEST_KEYWORDS = "test, page, keywords";
    private static final String TEST_CONTENT = "Test page content";
    private static final String TEST_LANGUAGE_NAME = "English";
    private static final String TEST_LANGUAGE_ABBREV = "en";
    private static final String TEST_LINK_URL = "/test-page";
    private static final String TEST_LINK_TEXT = "Test Page";
    private static final String PAGE_NOT_FOUND = "Page not found with id: ";
    private static final String CONTENT_NOT_FOUND = "Page content not found for page id: ";

    // Test container
    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    // Spring injected dependencies
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PageService pageService;

    @MockBean
    private PageRepository pageRepository;

    @MockBean
    private PageContentRepository pageContentRepository;

    @MockBean
    private LinkService linkService;

    @MockBean
    private LanguageService languageService;

    // Test data
    private UUID pageId;
    private UUID linkId;
    private UUID languageId;
    private PageRequestDTO pageRequestDTO;
    private PageResponseDTO pageResponseDTO;
    private PageContentRequestDTO contentRequestDTO;
    private PageContentResponseDTO contentResponseDTO;

    @BeforeEach
    void setUp() {
        pageId = UUID.randomUUID();
        linkId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final Link link = new Link();
        link.setId(linkId);
        link.setUrl(TEST_LINK_URL);
        link.setName(TEST_LINK_TEXT);
        link.setScope(LinkScope.EXTERNAL);

        final Language language = new Language();
        language.setId(languageId);
        language.setName(TEST_LANGUAGE_NAME);
        language.setAbbreviation(TEST_LANGUAGE_ABBREV);

        pageRequestDTO = new PageRequestDTO(
                linkId,
                TEST_LINK_URL,
                true,
                true);

        pageResponseDTO = new PageResponseDTO(
                pageId,
                linkId,
                TEST_LINK_URL,
                true,
                true,
                Set.of(),
                now,
                now);

        contentRequestDTO = new PageContentRequestDTO(
                languageId,
                TEST_TITLE,
                TEST_CONTENT,
                TEST_DESCRIPTION,
                TEST_KEYWORDS);

        contentResponseDTO = new PageContentResponseDTO(
                UUID.randomUUID(),
                pageId,
                languageId,
                TEST_TITLE,
                TEST_CONTENT,
                TEST_DESCRIPTION,
                TEST_KEYWORDS,
                now,
                now);
    }

    @Test
    void whenGettingAllPages_shouldReturnList() throws Exception {
        // Arrange
        final List<PageResponseDTO> pages = List.of(pageResponseDTO);
        when(pageService.findAll()).thenReturn(pages);

        // Act & Assert
        mockMvc.perform(get(API_PAGES))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(pageId.toString()))
                .andExpect(jsonPath("$[0].linkId").value(linkId.toString()))
                .andExpect(jsonPath("$[0].thumbnail").value(TEST_LINK_URL));

        verify(pageService).findAll();
    }

    @Test
    void whenGettingPageById_shouldReturnPage() throws Exception {
        // Arrange
        when(pageService.findById(pageId)).thenReturn(pageResponseDTO);

        // Act & Assert
        mockMvc.perform(get(API_PAGES_ID, pageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(pageId.toString()))
                .andExpect(jsonPath("$.linkId").value(linkId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(TEST_LINK_URL));

        verify(pageService).findById(pageId);
    }

    @Test
    void whenCreatingPage_shouldReturnCreated() throws Exception {
        // Arrange
        when(pageService.create(any(PageRequestDTO.class))).thenReturn(pageResponseDTO);

        // Act & Assert
        mockMvc.perform(post(API_PAGES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(pageId.toString()))
                .andExpect(jsonPath("$.linkId").value(linkId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(TEST_LINK_URL));

        verify(pageService).create(any(PageRequestDTO.class));
    }

    @Test
    void whenUpdatingPage_shouldReturnUpdated() throws Exception {
        // Arrange
        when(pageService.update(eq(pageId), any(PageRequestDTO.class))).thenReturn(pageResponseDTO);

        // Act & Assert
        mockMvc.perform(put(API_PAGES_ID, pageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(pageId.toString()))
                .andExpect(jsonPath("$.linkId").value(linkId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(TEST_LINK_URL));

        verify(pageService).update(eq(pageId), any(PageRequestDTO.class));
    }

    @Test
    void whenDeletingPage_shouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(API_PAGES_ID, pageId))
                .andExpect(status().isNoContent());

        verify(pageService).delete(pageId);
    }

    @Test
    void whenGettingAllPageContent_shouldReturnList() throws Exception {
        // Arrange
        final List<PageContentResponseDTO> contents = List.of(contentResponseDTO);
        when(pageService.findAllContentByPage(pageId)).thenReturn(contents);

        // Act & Assert
        mockMvc.perform(get(API_PAGES_CONTENT, pageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].pageId").value(pageId.toString()))
                .andExpect(jsonPath("$[0].title").value(TEST_TITLE))
                .andExpect(jsonPath("$[0].content").value(TEST_CONTENT));

        verify(pageService).findAllContentByPage(pageId);
    }

    @Test
    void whenGettingPageContent_shouldReturnContent() throws Exception {
        // Arrange
        when(pageService.findContentByPageAndLanguage(pageId, languageId)).thenReturn(contentResponseDTO);

        // Act & Assert
        mockMvc.perform(get(API_PAGES_CONTENT_LANGUAGE, pageId, languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageId").value(pageId.toString()))
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.content").value(TEST_CONTENT));

        verify(pageService).findContentByPageAndLanguage(pageId, languageId);
    }

    @Test
    void whenCreatingPageContent_shouldReturnCreated() throws Exception {
        // Arrange
        when(pageService.createContent(eq(pageId), any(PageContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        // Act & Assert
        mockMvc.perform(post(API_PAGES_CONTENT, pageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageId").value(pageId.toString()))
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.content").value(TEST_CONTENT));

        verify(pageService).createContent(eq(pageId), any(PageContentRequestDTO.class));
    }

    @Test
    void whenUpdatingPageContent_shouldReturnUpdated() throws Exception {
        // Arrange
        when(pageService.updateContent(eq(pageId), eq(languageId), any(PageContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        // Act & Assert
        mockMvc.perform(put(API_PAGES_CONTENT_LANGUAGE, pageId, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageId").value(pageId.toString()))
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.content").value(TEST_CONTENT));

        verify(pageService).updateContent(eq(pageId), eq(languageId), any(PageContentRequestDTO.class));
    }

    @Test
    void whenDeletingPageContent_shouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(API_PAGES_CONTENT_LANGUAGE, pageId, languageId))
                .andExpect(status().isNoContent());

        verify(pageService).deleteContent(pageId, languageId);
    }
}
