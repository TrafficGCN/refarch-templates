package de.muenchen.refarch.homepage;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
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
class HomepageControllerTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HomepageService homepageService;

    @MockBean
    private HomepageRepository homepageRepository;

    @MockBean
    private HomepageContentRepository homepageContentRepository;

    @MockBean
    private LinkService linkService;

    @MockBean
    private LanguageService languageService;

    private UUID homepageId;
    private UUID linkId;
    private UUID languageId;
    private Link link;
    private Language language;
    private HomepageRequestDTO homepageRequestDTO;
    private HomepageResponseDTO homepageResponseDTO;
    private HomepageContentRequestDTO contentRequestDTO;
    private HomepageContentResponseDTO contentResponseDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        homepageId = UUID.randomUUID();
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
        language.setFontAwesomeIcon("flag-usa");
        language.setMdiIcon("flag");

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

        contentResponseDTO = new HomepageContentResponseDTO(
                UUID.randomUUID(),
                homepageId,
                languageId,
                "Welcome",
                "Extended Welcome",
                "Explore Our Work",
                "Get Involved",
                "Important Links",
                "Ecosystem Links",
                "Blog",
                "Papers",
                "Read More",
                now,
                now);

        homepageResponseDTO = new HomepageResponseDTO(
                homepageId,
                linkId,
                "thumbnail.jpg",
                Set.of(contentResponseDTO),
                now,
                now);
    }

    @Test
    void getAllHomepages_ShouldReturnListOfHomepages() throws Exception {
        when(homepageService.findAll()).thenReturn(List.of(homepageResponseDTO));

        mockMvc.perform(get("/homepages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(homepageId.toString()))
                .andExpect(jsonPath("$[0].thumbnail").value("thumbnail.jpg"));

        verify(homepageService).findAll();
    }

    @Test
    void getHomepageById_ShouldReturnHomepage() throws Exception {
        when(homepageService.findById(homepageId)).thenReturn(homepageResponseDTO);

        mockMvc.perform(get("/homepages/{id}", homepageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(homepageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"));

        verify(homepageService).findById(homepageId);
    }

    @Test
    void createHomepage_ShouldCreateAndReturnHomepage() throws Exception {
        when(homepageService.create(any(HomepageRequestDTO.class))).thenReturn(homepageResponseDTO);

        mockMvc.perform(post("/homepages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(homepageRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(homepageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"));

        verify(homepageService).create(any(HomepageRequestDTO.class));
    }

    @Test
    void updateHomepage_ShouldUpdateAndReturnHomepage() throws Exception {
        when(homepageService.update(eq(homepageId), any(HomepageRequestDTO.class))).thenReturn(homepageResponseDTO);

        mockMvc.perform(put("/homepages/{id}", homepageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(homepageRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(homepageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"));

        verify(homepageService).update(eq(homepageId), any(HomepageRequestDTO.class));
    }

    @Test
    void deleteHomepage_ShouldDeleteHomepage() throws Exception {
        doNothing().when(homepageService).delete(homepageId);

        mockMvc.perform(delete("/homepages/{id}", homepageId))
                .andExpect(status().isNoContent());

        verify(homepageService).delete(homepageId);
    }

    @Test
    void getAllHomepageContent_ShouldReturnListOfContent() throws Exception {
        when(homepageService.findAllContentByHomepage(homepageId)).thenReturn(List.of(contentResponseDTO));

        mockMvc.perform(get("/homepages/{homepageId}/content", homepageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$[0].welcomeMessage").value("Welcome"))
                .andExpect(jsonPath("$[0].welcomeMessageExtended").value("Extended Welcome"));

        verify(homepageService).findAllContentByHomepage(homepageId);
    }

    @Test
    void getHomepageContent_ShouldReturnContent() throws Exception {
        when(homepageService.findContentByHomepageAndLanguage(homepageId, languageId)).thenReturn(contentResponseDTO);

        mockMvc.perform(get("/homepages/{homepageId}/content/{languageId}", homepageId, languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$.welcomeMessage").value("Welcome"))
                .andExpect(jsonPath("$.welcomeMessageExtended").value("Extended Welcome"));

        verify(homepageService).findContentByHomepageAndLanguage(homepageId, languageId);
    }

    @Test
    void createHomepageContent_ShouldCreateAndReturnContent() throws Exception {
        when(homepageService.createContent(eq(homepageId), any(HomepageContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        mockMvc.perform(post("/homepages/{homepageId}/content", homepageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$.welcomeMessage").value("Welcome"))
                .andExpect(jsonPath("$.welcomeMessageExtended").value("Extended Welcome"));

        verify(homepageService).createContent(eq(homepageId), any(HomepageContentRequestDTO.class));
    }

    @Test
    void updateHomepageContent_ShouldUpdateAndReturnContent() throws Exception {
        when(homepageService.updateContent(eq(homepageId), eq(languageId), any(HomepageContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        mockMvc.perform(put("/homepages/{homepageId}/content/{languageId}", homepageId, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$.welcomeMessage").value("Welcome"))
                .andExpect(jsonPath("$.welcomeMessageExtended").value("Extended Welcome"));

        verify(homepageService).updateContent(eq(homepageId), eq(languageId), any(HomepageContentRequestDTO.class));
    }

    @Test
    void deleteHomepageContent_ShouldDeleteContent() throws Exception {
        doNothing().when(homepageService).deleteContent(homepageId, languageId);

        mockMvc.perform(delete("/homepages/{homepageId}/content/{languageId}", homepageId, languageId))
                .andExpect(status().isNoContent());

        verify(homepageService).deleteContent(homepageId, languageId);
    }
}
