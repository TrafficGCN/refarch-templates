package de.muenchen.refarch.language;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.language.dto.LanguageRequestDTO;
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
class LanguageControllerTest {

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
    private LanguageService languageService;

    @MockBean
    private LanguageRepository languageRepository;

    private UUID languageId;
    private LanguageRequestDTO languageRequestDTO;
    private Language language;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        languageId = UUID.randomUUID();
        now = LocalDateTime.now();

        languageRequestDTO = new LanguageRequestDTO(
                "English",
                "en",
                "fa-flag-usa",
                "mdi-flag");

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");
        language.setFontAwesomeIcon("fa-flag-usa");
        language.setMdiIcon("mdi-flag");
    }

    @Test
    void getAllLanguages_ShouldReturnListOfLanguages() throws Exception {
        when(languageService.getAllLanguages()).thenReturn(List.of(language));

        mockMvc.perform(get("/languages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(languageId.toString()))
                .andExpect(jsonPath("$[0].name").value("English"))
                .andExpect(jsonPath("$[0].abbreviation").value("en"))
                .andExpect(jsonPath("$[0].fontAwesomeIcon").value("fa-flag-usa"))
                .andExpect(jsonPath("$[0].mdiIcon").value("mdi-flag"));

        verify(languageService).getAllLanguages();
    }

    @Test
    void getLanguageById_WhenExists_ShouldReturnLanguage() throws Exception {
        when(languageService.getLanguageById(languageId)).thenReturn(language);

        mockMvc.perform(get("/languages/{id}", languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(languageId.toString()))
                .andExpect(jsonPath("$.name").value("English"))
                .andExpect(jsonPath("$.abbreviation").value("en"))
                .andExpect(jsonPath("$.fontAwesomeIcon").value("fa-flag-usa"))
                .andExpect(jsonPath("$.mdiIcon").value("mdi-flag"));

        verify(languageService).getLanguageById(languageId);
    }

    @Test
    void createLanguage_WithValidRequest_ShouldReturnCreatedLanguage() throws Exception {
        when(languageService.createLanguage(any(LanguageRequestDTO.class))).thenReturn(language);

        mockMvc.perform(post("/languages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(languageRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(languageId.toString()))
                .andExpect(jsonPath("$.name").value("English"))
                .andExpect(jsonPath("$.abbreviation").value("en"))
                .andExpect(jsonPath("$.fontAwesomeIcon").value("fa-flag-usa"))
                .andExpect(jsonPath("$.mdiIcon").value("mdi-flag"));

        verify(languageService).createLanguage(any(LanguageRequestDTO.class));
    }

    @Test
    void createLanguage_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        LanguageRequestDTO invalidRequest = new LanguageRequestDTO(
                null,
                null,
                null,
                null);

        mockMvc.perform(post("/languages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(languageService, never()).createLanguage(any());
    }

    @Test
    void updateLanguage_WithValidRequest_ShouldReturnUpdatedLanguage() throws Exception {
        when(languageService.updateLanguage(eq(languageId), any(LanguageRequestDTO.class))).thenReturn(language);

        mockMvc.perform(put("/languages/{id}", languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(languageRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(languageId.toString()))
                .andExpect(jsonPath("$.name").value("English"))
                .andExpect(jsonPath("$.abbreviation").value("en"))
                .andExpect(jsonPath("$.fontAwesomeIcon").value("fa-flag-usa"))
                .andExpect(jsonPath("$.mdiIcon").value("mdi-flag"));

        verify(languageService).updateLanguage(eq(languageId), any(LanguageRequestDTO.class));
    }

    @Test
    void updateLanguage_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        LanguageRequestDTO invalidRequest = new LanguageRequestDTO(
                null,
                null,
                null,
                null);

        mockMvc.perform(put("/languages/{id}", languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(languageService, never()).updateLanguage(any(), any());
    }

    @Test
    void deleteLanguage_ShouldReturnNoContent() throws Exception {
        doNothing().when(languageService).deleteLanguage(languageId);

        mockMvc.perform(delete("/languages/{id}", languageId))
                .andExpect(status().isNoContent());

        verify(languageService).deleteLanguage(languageId);
    }
}
