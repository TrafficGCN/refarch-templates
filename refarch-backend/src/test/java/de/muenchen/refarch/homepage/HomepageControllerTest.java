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

    private static final String THUMBNAIL_PATH = "thumbnail.jpg";
    private static final String WELCOME_MESSAGE = "Welcome";
    private static final String WELCOME_MESSAGE_EXTENDED = "Extended Welcome";
    private static final String EXPLORE_OUR_WORK = "Explore Our Work";
    private static final String GET_INVOLVED = "Get Involved";
    private static final String IMPORTANT_LINKS = "Important Links";
    private static final String ECOSYSTEM_LINKS = "Ecosystem Links";
    private static final String BLOG = "Blog";
    private static final String PAPERS = "Papers";
    private static final String READ_MORE = "Read More";
    private static final String EXAMPLE_URL = "https://example.com";
    private static final String EXAMPLE_LINK_NAME = "Example Link";
    private static final String ENGLISH_LANGUAGE_NAME = "English";
    private static final String ENGLISH_LANGUAGE_ABBREV = "en";
    private static final String FLAG_USA_ICON = "flag-usa";
    private static final String FLAG_MDI_ICON = "flag";

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
    private UUID languageId;
    private HomepageRequestDTO homepageRequestDTO;
    private HomepageResponseDTO homepageResponseDTO;
    private HomepageContentRequestDTO contentRequestDTO;
    private HomepageContentResponseDTO contentResponseDTO;

    @BeforeEach
    void setUp() {
        homepageId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        final UUID linkId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final Link link = new Link();
        link.setId(linkId);
        link.setUrl(EXAMPLE_URL);
        link.setName(EXAMPLE_LINK_NAME);
        link.setScope(LinkScope.EXTERNAL);

        final Language language = new Language();
        language.setId(languageId);
        language.setName(ENGLISH_LANGUAGE_NAME);
        language.setAbbreviation(ENGLISH_LANGUAGE_ABBREV);
        language.setFontAwesomeIcon(FLAG_USA_ICON);
        language.setMdiIcon(FLAG_MDI_ICON);

        homepageRequestDTO = new HomepageRequestDTO(
                linkId,
                THUMBNAIL_PATH);

        contentRequestDTO = new HomepageContentRequestDTO(
                languageId,
                WELCOME_MESSAGE,
                WELCOME_MESSAGE_EXTENDED,
                EXPLORE_OUR_WORK,
                GET_INVOLVED,
                IMPORTANT_LINKS,
                ECOSYSTEM_LINKS,
                BLOG,
                PAPERS,
                READ_MORE);

        contentResponseDTO = new HomepageContentResponseDTO(
                UUID.randomUUID(),
                homepageId,
                languageId,
                WELCOME_MESSAGE,
                WELCOME_MESSAGE_EXTENDED,
                EXPLORE_OUR_WORK,
                GET_INVOLVED,
                IMPORTANT_LINKS,
                ECOSYSTEM_LINKS,
                BLOG,
                PAPERS,
                READ_MORE,
                now,
                now);

        homepageResponseDTO = new HomepageResponseDTO(
                homepageId,
                linkId,
                THUMBNAIL_PATH,
                Set.of(contentResponseDTO),
                now,
                now);
    }

    @Test
    void shouldReturnAllHomepages() throws Exception {
        when(homepageService.findAll()).thenReturn(List.of(homepageResponseDTO));

        mockMvc.perform(get("/homepages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(homepageId.toString()))
                .andExpect(jsonPath("$[0].thumbnail").value(THUMBNAIL_PATH));

        verify(homepageService).findAll();
    }

    @Test
    void shouldReturnHomepageById() throws Exception {
        when(homepageService.findById(homepageId)).thenReturn(homepageResponseDTO);

        mockMvc.perform(get("/homepages/{id}", homepageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(homepageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(THUMBNAIL_PATH));

        verify(homepageService).findById(homepageId);
    }

    @Test
    void shouldCreateAndReturnHomepage() throws Exception {
        when(homepageService.create(any(HomepageRequestDTO.class))).thenReturn(homepageResponseDTO);

        mockMvc.perform(post("/homepages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(homepageRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(homepageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(THUMBNAIL_PATH));

        verify(homepageService).create(any(HomepageRequestDTO.class));
    }

    @Test
    void shouldUpdateAndReturnHomepage() throws Exception {
        when(homepageService.update(eq(homepageId), any(HomepageRequestDTO.class))).thenReturn(homepageResponseDTO);

        mockMvc.perform(put("/homepages/{id}", homepageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(homepageRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(homepageId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(THUMBNAIL_PATH));

        verify(homepageService).update(eq(homepageId), any(HomepageRequestDTO.class));
    }

    @Test
    void shouldDeleteHomepage() throws Exception {
        doNothing().when(homepageService).delete(homepageId);

        mockMvc.perform(delete("/homepages/{id}", homepageId))
                .andExpect(status().isNoContent());

        verify(homepageService).delete(homepageId);
    }

    @Test
    void shouldReturnAllHomepageContent() throws Exception {
        when(homepageService.findAllContentByHomepage(homepageId)).thenReturn(List.of(contentResponseDTO));

        mockMvc.perform(get("/homepages/{homepageId}/content", homepageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$[0].welcomeMessage").value(WELCOME_MESSAGE))
                .andExpect(jsonPath("$[0].welcomeMessageExtended").value(WELCOME_MESSAGE_EXTENDED));

        verify(homepageService).findAllContentByHomepage(homepageId);
    }

    @Test
    void shouldReturnHomepageContent() throws Exception {
        when(homepageService.findContentByHomepageAndLanguage(homepageId, languageId)).thenReturn(contentResponseDTO);

        mockMvc.perform(get("/homepages/{homepageId}/content/{languageId}", homepageId, languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$.welcomeMessage").value(WELCOME_MESSAGE))
                .andExpect(jsonPath("$.welcomeMessageExtended").value(WELCOME_MESSAGE_EXTENDED));

        verify(homepageService).findContentByHomepageAndLanguage(homepageId, languageId);
    }

    @Test
    void shouldCreateAndReturnHomepageContent() throws Exception {
        when(homepageService.createContent(eq(homepageId), any(HomepageContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        mockMvc.perform(post("/homepages/{homepageId}/content", homepageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$.welcomeMessage").value(WELCOME_MESSAGE))
                .andExpect(jsonPath("$.welcomeMessageExtended").value(WELCOME_MESSAGE_EXTENDED));

        verify(homepageService).createContent(eq(homepageId), any(HomepageContentRequestDTO.class));
    }

    @Test
    void shouldUpdateAndReturnHomepageContent() throws Exception {
        when(homepageService.updateContent(eq(homepageId), eq(languageId), any(HomepageContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        mockMvc.perform(put("/homepages/{homepageId}/content/{languageId}", homepageId, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.homepageId").value(homepageId.toString()))
                .andExpect(jsonPath("$.welcomeMessage").value(WELCOME_MESSAGE))
                .andExpect(jsonPath("$.welcomeMessageExtended").value(WELCOME_MESSAGE_EXTENDED));

        verify(homepageService).updateContent(eq(homepageId), eq(languageId), any(HomepageContentRequestDTO.class));
    }

    @Test
    void shouldDeleteHomepageContent() throws Exception {
        doNothing().when(homepageService).deleteContent(homepageId, languageId);

        mockMvc.perform(delete("/homepages/{homepageId}/content/{languageId}", homepageId, languageId))
                .andExpect(status().isNoContent());

        verify(homepageService).deleteContent(homepageId, languageId);
    }
}
