package de.muenchen.refarch.globalsettings;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsRequestDTO;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import jakarta.persistence.EntityNotFoundException;
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
class GlobalSettingsControllerTest {

    private static final String API_SETTINGS = "/settings";
    private static final String TEST_LOGO_URL = "https://example.com/logo.png";
    private static final String TEST_WEBSITE_NAME = "Test Website";
    private static final String TEST_LANGUAGE = "en";
    private static final String TEST_ANALYTICS_ID = "UA-12345";
    private static final String TEST_CONTACT_EMAIL = "contact@example.com";
    private static final String TEST_DESCRIPTION = "Test website description";
    private static final int TEST_SESSION_DURATION = 480;
    private static final int TEST_MAX_UPLOAD_SIZE = 10;
    private static final int TEST_MAX_ITEMS_PER_PAGE = 20;
    private static final String SETTINGS_NOT_FOUND = "Settings not found";

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
    private GlobalSettingsService globalSettingsService;

    @MockBean
    private GlobalSettingsRepository globalSettingsRepository;

    private UUID settingsId;
    private GlobalSettingsRequestDTO requestDTO;
    private GlobalSettingsResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        settingsId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        responseDTO = new GlobalSettingsResponseDTO(
                settingsId,
                TEST_SESSION_DURATION,
                TEST_LOGO_URL,
                TEST_WEBSITE_NAME,
                true,
                false,
                TEST_MAX_UPLOAD_SIZE,
                TEST_LANGUAGE,
                TEST_ANALYTICS_ID,
                TEST_CONTACT_EMAIL,
                TEST_DESCRIPTION,
                TEST_MAX_ITEMS_PER_PAGE,
                false,
                true,
                now,
                now);

        requestDTO = new GlobalSettingsRequestDTO(
                TEST_SESSION_DURATION,
                TEST_LOGO_URL,
                TEST_WEBSITE_NAME,
                true,
                false,
                TEST_MAX_UPLOAD_SIZE,
                TEST_LANGUAGE,
                TEST_ANALYTICS_ID,
                TEST_CONTACT_EMAIL,
                TEST_DESCRIPTION,
                TEST_MAX_ITEMS_PER_PAGE,
                false,
                true);
    }

    @Test
    void whenGettingSettings_shouldReturnCurrentSettings() throws Exception {
        // Arrange
        when(globalSettingsService.getCurrentSettings()).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get(API_SETTINGS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(settingsId.toString()))
                .andExpect(jsonPath("$.websiteName").value(TEST_WEBSITE_NAME))
                .andExpect(jsonPath("$.defaultLanguage").value(TEST_LANGUAGE));

        verify(globalSettingsService).getCurrentSettings();
    }

    @Test
    void whenGettingSettingsAndNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(globalSettingsService.getCurrentSettings())
                .thenThrow(new EntityNotFoundException(SETTINGS_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get(API_SETTINGS))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUpdatingSettings_shouldReturnUpdatedSettings() throws Exception {
        // Arrange
        when(globalSettingsService.updateSettings(any(GlobalSettingsRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put(API_SETTINGS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(settingsId.toString()))
                .andExpect(jsonPath("$.websiteName").value(TEST_WEBSITE_NAME))
                .andExpect(jsonPath("$.defaultLanguage").value(TEST_LANGUAGE));

        verify(globalSettingsService).updateSettings(any(GlobalSettingsRequestDTO.class));
    }

    @Test
    void whenUpdatingSettingsWithInvalidRequest_shouldReturnBadRequest() throws Exception {
        // Arrange
        final GlobalSettingsRequestDTO invalidRequest = new GlobalSettingsRequestDTO(
                -1, // invalid session duration
                TEST_LOGO_URL,
                TEST_WEBSITE_NAME,
                true,
                false,
                TEST_MAX_UPLOAD_SIZE,
                TEST_LANGUAGE,
                TEST_ANALYTICS_ID,
                TEST_CONTACT_EMAIL,
                TEST_DESCRIPTION,
                TEST_MAX_ITEMS_PER_PAGE,
                false,
                true);

        // Act & Assert
        mockMvc.perform(put(API_SETTINGS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
