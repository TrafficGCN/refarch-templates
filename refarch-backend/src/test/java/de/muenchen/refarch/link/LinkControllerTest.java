package de.muenchen.refarch.link;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.link.dto.LinkRequestDTO;
import de.muenchen.refarch.link.dto.LinkResponseDTO;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
class LinkControllerTest {

    private static final String EXAMPLE_URL = "https://example.com";
    private static final String EXAMPLE_LINK_NAME = "Example Link";
    private static final String FONT_AWESOME_ICON = "fa-link";
    private static final String MDI_ICON = "mdi-link";
    private static final String NAVIGATION_TYPE = "navigation";
    private static final String LINKS_ENDPOINT = "/links";
    private static final String MINIMAL_URL = "https://minimal.com";

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
    private LinkService linkService;

    @MockBean
    private LinkRepository linkRepository;

    private UUID linkId;
    private LinkRequestDTO linkRequestDTO;
    private LinkResponseDTO linkResponseDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        linkId = UUID.randomUUID();

        // Setup test request DTO
        linkRequestDTO = new LinkRequestDTO(
                EXAMPLE_URL,
                EXAMPLE_LINK_NAME,
                FONT_AWESOME_ICON,
                MDI_ICON,
                NAVIGATION_TYPE,
                LinkScope.EXTERNAL);

        // Setup test response DTO
        linkResponseDTO = new LinkResponseDTO(
                linkId,
                EXAMPLE_URL,
                EXAMPLE_LINK_NAME,
                FONT_AWESOME_ICON,
                MDI_ICON,
                NAVIGATION_TYPE,
                LinkScope.EXTERNAL);

        now = LocalDateTime.now();
    }

    @Test
    void shouldReturnAllLinks() throws Exception {
        // Arrange
        when(linkService.getAllLinks()).thenReturn(List.of(linkResponseDTO));

        // Act & Assert
        mockMvc.perform(get(LINKS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(linkId.toString()))
                .andExpect(jsonPath("$[0].link").value(EXAMPLE_URL))
                .andExpect(jsonPath("$[0].name").value(EXAMPLE_LINK_NAME))
                .andExpect(jsonPath("$[0].fontAwesomeIcon").value(FONT_AWESOME_ICON))
                .andExpect(jsonPath("$[0].mdiIcon").value(MDI_ICON))
                .andExpect(jsonPath("$[0].type").value(NAVIGATION_TYPE))
                .andExpect(jsonPath("$[0].scope").value("EXTERNAL"));

        verify(linkService).getAllLinks();
    }

    @Test
    void shouldCreateAndReturnLink() throws Exception {
        // Arrange
        when(linkService.createLink(any(LinkRequestDTO.class))).thenReturn(linkResponseDTO);

        // Act & Assert
        mockMvc.perform(post(LINKS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(linkId.toString()))
                .andExpect(jsonPath("$.link").value(EXAMPLE_URL))
                .andExpect(jsonPath("$.name").value(EXAMPLE_LINK_NAME))
                .andExpect(jsonPath("$.fontAwesomeIcon").value(FONT_AWESOME_ICON))
                .andExpect(jsonPath("$.mdiIcon").value(MDI_ICON))
                .andExpect(jsonPath("$.type").value(NAVIGATION_TYPE))
                .andExpect(jsonPath("$.scope").value("EXTERNAL"));

        verify(linkService).createLink(any(LinkRequestDTO.class));
    }

    @Test
    void shouldRejectInvalidLinkCreation() throws Exception {
        // Arrange
        final LinkRequestDTO invalidRequest = new LinkRequestDTO(
                "", // invalid: blank link
                "Example Link",
                "fa-link",
                "mdi-link",
                "navigation",
                null // invalid: null scope
        );

        // Act & Assert
        mockMvc.perform(post(LINKS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(linkService, never()).createLink(any());
    }

    @Test
    void shouldAcceptMinimalLinkCreation() throws Exception {
        // Arrange
        final LinkRequestDTO minimalRequest = new LinkRequestDTO(
                EXAMPLE_URL, // required
                null, // optional
                null, // optional
                null, // optional
                null, // optional
                LinkScope.EXTERNAL // required
        );

        final LinkResponseDTO minimalResponse = new LinkResponseDTO(
                linkId,
                EXAMPLE_URL,
                null,
                null,
                null,
                null,
                LinkScope.EXTERNAL);

        when(linkService.createLink(any(LinkRequestDTO.class))).thenReturn(minimalResponse);

        // Act & Assert
        mockMvc.perform(post(LINKS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(linkId.toString()))
                .andExpect(jsonPath("$.link").value(EXAMPLE_URL))
                .andExpect(jsonPath("$.scope").value("EXTERNAL"))
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.fontAwesomeIcon").doesNotExist())
                .andExpect(jsonPath("$.mdiIcon").doesNotExist())
                .andExpect(jsonPath("$.type").doesNotExist());

        verify(linkService).createLink(any(LinkRequestDTO.class));
    }

    @Test
    void create_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        final LinkRequestDTO invalidRequest = new LinkRequestDTO(
                "", // Invalid URL
                "Invalid Link",
                "fa-invalid",
                "mdi-invalid",
                "invalid",
                LinkScope.INTERNAL);

        // Act & Assert
        mockMvc.perform(post(LINKS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithMinimalRequest_ShouldCreateLink() throws Exception {
        // Arrange
        final LinkRequestDTO minimalRequest = new LinkRequestDTO(
                MINIMAL_URL,
                null,
                null,
                null,
                null,
                LinkScope.INTERNAL);

        final LinkResponseDTO minimalResponse = new LinkResponseDTO(
                linkId,
                MINIMAL_URL,
                null,
                null,
                null,
                null,
                LinkScope.INTERNAL);

        when(linkService.createLink(any(LinkRequestDTO.class))).thenReturn(minimalResponse);

        // Act & Assert
        mockMvc.perform(post(LINKS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(linkId.toString()))
                .andExpect(jsonPath("$.link").value(MINIMAL_URL))
                .andExpect(jsonPath("$.scope").value("INTERNAL"))
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.fontAwesomeIcon").doesNotExist())
                .andExpect(jsonPath("$.mdiIcon").doesNotExist())
                .andExpect(jsonPath("$.type").doesNotExist());

        verify(linkService).createLink(any(LinkRequestDTO.class));
    }
}
