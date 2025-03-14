package de.muenchen.refarch.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.link.LinkService;
import de.muenchen.refarch.page.content.PageContentRepository;
import de.muenchen.refarch.page.dto.PageRequestDTO;
import de.muenchen.refarch.page.dto.PageResponseDTO;
import de.muenchen.refarch.page.content.dto.PageContentRequestDTO;
import de.muenchen.refarch.page.content.dto.PageContentResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
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
class PageControllerTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Configuration
    @EnableWebMvc
    static class TestConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

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

    private UUID pageId;
    private UUID linkId;
    private UUID languageId;
    private Link link;
    private Language language;
    private PageRequestDTO pageRequestDTO;
    private PageResponseDTO pageResponseDTO;
    private PageContentRequestDTO contentRequestDTO;
    private PageContentResponseDTO contentResponseDTO;
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
        link.setScope(LinkScope.external);

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");

        pageRequestDTO = new PageRequestDTO(
                linkId,
                "thumbnail.jpg",
                true);

        contentRequestDTO = new PageContentRequestDTO(
                languageId,
                "Test Title",
                "Test Content",
                "Test Description",
                "test,keywords");

        contentResponseDTO = new PageContentResponseDTO(
                UUID.randomUUID(),
                pageId,
                languageId,
                "Test Title",
                "Test Content",
                "Test Description",
                "test,keywords",
                now,
                now);

        pageResponseDTO = new PageResponseDTO(
                pageId,
                linkId,
                "thumbnail.jpg",
                true,
                Set.of(contentResponseDTO),
                now,
                now);
    }

    @Test
    void getAllPages_ShouldReturnListOfPages() throws Exception {
        when(pageService.findAll()).thenReturn(List.of(pageResponseDTO));

        mockMvc.perform(get("/pages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(pageId.toString()))
                .andExpect(jsonPath("$[0].thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$[0].commentsEnabled").value(true));

        verify(pageService).findAll();
    }

    @Test
    void getPageById_ShouldReturnPage() throws Exception {
        when(pageService.findById(pageId)).thenReturn(pageResponseDTO);

        mockMvc.perform(get("/pages/{id}", pageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(pageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(pageService).findById(pageId);
    }

    @Test
    void createPage_ShouldCreateAndReturnPage() throws Exception {
        when(pageService.create(any(PageRequestDTO.class))).thenReturn(pageResponseDTO);

        mockMvc.perform(post("/pages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(pageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(pageService).create(any(PageRequestDTO.class));
    }

    @Test
    void updatePage_ShouldUpdateAndReturnPage() throws Exception {
        when(pageService.update(eq(pageId), any(PageRequestDTO.class))).thenReturn(pageResponseDTO);

        mockMvc.perform(put("/pages/{id}", pageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(pageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(pageService).update(eq(pageId), any(PageRequestDTO.class));
    }

    @Test
    void deletePage_ShouldDeletePage() throws Exception {
        doNothing().when(pageService).delete(pageId);

        mockMvc.perform(delete("/pages/{id}", pageId))
                .andExpect(status().isNoContent());

        verify(pageService).delete(pageId);
    }

    @Test
    void getAllPageContent_ShouldReturnListOfContent() throws Exception {
        when(pageService.findAllContentByPage(pageId)).thenReturn(List.of(contentResponseDTO));

        mockMvc.perform(get("/pages/{pageId}/content", pageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].pageId").value(pageId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].content").value("Test Content"));

        verify(pageService).findAllContentByPage(pageId);
    }

    @Test
    void getPageContent_ShouldReturnContent() throws Exception {
        when(pageService.findContentByPageAndLanguage(pageId, languageId)).thenReturn(contentResponseDTO);

        mockMvc.perform(get("/pages/{pageId}/content/{languageId}", pageId, languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageId").value(pageId.toString()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(pageService).findContentByPageAndLanguage(pageId, languageId);
    }

    @Test
    void createPageContent_ShouldCreateAndReturnContent() throws Exception {
        when(pageService.createContent(eq(pageId), any(PageContentRequestDTO.class))).thenReturn(contentResponseDTO);

        mockMvc.perform(post("/pages/{pageId}/content", pageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageId").value(pageId.toString()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(pageService).createContent(eq(pageId), any(PageContentRequestDTO.class));
    }

    @Test
    void updatePageContent_ShouldUpdateAndReturnContent() throws Exception {
        when(pageService.updateContent(eq(pageId), eq(languageId), any(PageContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        mockMvc.perform(put("/pages/{pageId}/content/{languageId}", pageId, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageId").value(pageId.toString()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(pageService).updateContent(eq(pageId), eq(languageId), any(PageContentRequestDTO.class));
    }

    @Test
    void deletePageContent_ShouldDeleteContent() throws Exception {
        doNothing().when(pageService).deleteContent(pageId, languageId);

        mockMvc.perform(delete("/pages/{pageId}/content/{languageId}", pageId, languageId))
                .andExpect(status().isNoContent());

        verify(pageService).deleteContent(pageId, languageId);
    }
}
