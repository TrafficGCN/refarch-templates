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
import org.springframework.test.web.servlet.ResultMatcher;
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

    private static final String LANGUAGES_ENDPOINT = "/languages";
    private static final String ID_PATH_PARAM = "/{id}";
    private static final String ENGLISH = "English";
    private static final String EN = "en";
    private static final String FA_FLAG_USA = "fa-flag-usa";
    private static final String MDI_FLAG = "mdi-flag";
    private static final String INVALID_REQUEST = "{\"name\":\"\",\"abbreviation\":\"\"}";

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

        languageRequestDTO = new LanguageRequestDTO(ENGLISH, EN, FA_FLAG_USA, MDI_FLAG);

        language = new Language();
        language.setId(languageId);
        language.setName(ENGLISH);
        language.setAbbreviation(EN);
        language.setFontAwesomeIcon(FA_FLAG_USA);
        language.setMdiIcon(MDI_FLAG);
    }

    private ResultMatcher[] assertLanguageResponse() {
        return new ResultMatcher[] {
                jsonPath("$.id").value(languageId.toString()),
                jsonPath("$.name").value(ENGLISH),
                jsonPath("$.abbreviation").value(EN),
                jsonPath("$.fontAwesomeIcon").value(FA_FLAG_USA),
                jsonPath("$.mdiIcon").value(MDI_FLAG)
        };
    }

    private ResultMatcher[] assertLanguageListResponse() {
        return new ResultMatcher[] {
                jsonPath("$[0].id").value(languageId.toString()),
                jsonPath("$[0].name").value(ENGLISH),
                jsonPath("$[0].abbreviation").value(EN),
                jsonPath("$[0].fontAwesomeIcon").value(FA_FLAG_USA),
                jsonPath("$[0].mdiIcon").value(MDI_FLAG)
        };
    }

    @Test
    void whenGettingAllLanguages_ShouldReturnList() throws Exception {
        when(languageService.getAllLanguages()).thenReturn(List.of(language));

        mockMvc.perform(get(LANGUAGES_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(assertLanguageListResponse());

        verify(languageService).getAllLanguages();
    }

    @Test
    void whenGettingExistingLanguage_ShouldReturnLanguage() throws Exception {
        when(languageService.getLanguageById(languageId)).thenReturn(language);

        mockMvc.perform(get(LANGUAGES_ENDPOINT + ID_PATH_PARAM, languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(assertLanguageResponse());

        verify(languageService).getLanguageById(languageId);
    }

    @Test
    void whenCreatingLanguageWithValidRequest_ShouldReturnCreated() throws Exception {
        when(languageService.createLanguage(any(LanguageRequestDTO.class))).thenReturn(language);

        final String requestBody = objectMapper.writeValueAsString(languageRequestDTO);

        mockMvc.perform(post(LANGUAGES_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(assertLanguageResponse());

        verify(languageService).createLanguage(any(LanguageRequestDTO.class));
    }

    @Test
    void whenCreatingLanguageWithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(LANGUAGES_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INVALID_REQUEST))
                .andExpect(status().isBadRequest());

        verify(languageService, never()).createLanguage(any());
    }

    @Test
    void whenUpdatingLanguageWithValidRequest_ShouldReturnUpdated() throws Exception {
        when(languageService.updateLanguage(eq(languageId), any(LanguageRequestDTO.class))).thenReturn(language);

        final String requestBody = objectMapper.writeValueAsString(languageRequestDTO);

        mockMvc.perform(put(LANGUAGES_ENDPOINT + ID_PATH_PARAM, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(assertLanguageResponse());

        verify(languageService).updateLanguage(eq(languageId), any(LanguageRequestDTO.class));
    }

    @Test
    void whenUpdatingLanguageWithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put(LANGUAGES_ENDPOINT + ID_PATH_PARAM, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INVALID_REQUEST))
                .andExpect(status().isBadRequest());

        verify(languageService, never()).updateLanguage(any(), any());
    }

    @Test
    void whenDeletingLanguage_ShouldReturnNoContent() throws Exception {
        doNothing().when(languageService).deleteLanguage(languageId);

        mockMvc.perform(delete(LANGUAGES_ENDPOINT + ID_PATH_PARAM, languageId))
                .andExpect(status().isNoContent());

        verify(languageService).deleteLanguage(languageId);
    }
}
