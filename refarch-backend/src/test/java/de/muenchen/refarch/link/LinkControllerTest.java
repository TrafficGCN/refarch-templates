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
                "https://example.com",
                "Example Link",
                "fa-link",
                "mdi-link",
                "navigation",
                LinkScope.external);

        // Setup test response DTO
        linkResponseDTO = new LinkResponseDTO(
                linkId,
                "https://example.com",
                "Example Link",
                "fa-link",
                "mdi-link",
                "navigation",
                LinkScope.external);

        now = LocalDateTime.now();
    }

    @Test
    void getAllLinks_ShouldReturnLinks() throws Exception {
        // Arrange
        when(linkService.getAllLinks()).thenReturn(List.of(linkResponseDTO));

        // Act & Assert
        mockMvc.perform(get("/links"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(linkId.toString()))
                .andExpect(jsonPath("$[0].link").value("https://example.com"))
                .andExpect(jsonPath("$[0].name").value("Example Link"))
                .andExpect(jsonPath("$[0].fontAwesomeIcon").value("fa-link"))
                .andExpect(jsonPath("$[0].mdiIcon").value("mdi-link"))
                .andExpect(jsonPath("$[0].type").value("navigation"))
                .andExpect(jsonPath("$[0].scope").value("external"));

        verify(linkService).getAllLinks();
    }

    @Test
    void createLink_WithValidRequest_ShouldReturnCreatedLink() throws Exception {
        // Arrange
        when(linkService.createLink(any(LinkRequestDTO.class))).thenReturn(linkResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(linkId.toString()))
                .andExpect(jsonPath("$.link").value("https://example.com"))
                .andExpect(jsonPath("$.name").value("Example Link"))
                .andExpect(jsonPath("$.fontAwesomeIcon").value("fa-link"))
                .andExpect(jsonPath("$.mdiIcon").value("mdi-link"))
                .andExpect(jsonPath("$.type").value("navigation"))
                .andExpect(jsonPath("$.scope").value("external"));

        verify(linkService).createLink(any(LinkRequestDTO.class));
    }

    @Test
    void createLink_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LinkRequestDTO invalidRequest = new LinkRequestDTO(
                "", // invalid: blank link
                "Example Link",
                "fa-link",
                "mdi-link",
                "navigation",
                null // invalid: null scope
        );

        // Act & Assert
        mockMvc.perform(post("/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(linkService, never()).createLink(any());
    }

    @Test
    void createLink_WithMissingOptionalFields_ShouldSucceed() throws Exception {
        // Arrange
        LinkRequestDTO minimalRequest = new LinkRequestDTO(
                "https://example.com", // required
                null, // optional
                null, // optional
                null, // optional
                null, // optional
                LinkScope.external // required
        );

        LinkResponseDTO minimalResponse = new LinkResponseDTO(
                linkId,
                "https://example.com",
                null,
                null,
                null,
                null,
                LinkScope.external);

        when(linkService.createLink(any(LinkRequestDTO.class))).thenReturn(minimalResponse);

        // Act & Assert
        mockMvc.perform(post("/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(linkId.toString()))
                .andExpect(jsonPath("$.link").value("https://example.com"))
                .andExpect(jsonPath("$.scope").value("external"))
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.fontAwesomeIcon").doesNotExist())
                .andExpect(jsonPath("$.mdiIcon").doesNotExist())
                .andExpect(jsonPath("$.type").doesNotExist());

        verify(linkService).createLink(any(LinkRequestDTO.class));
    }
}
