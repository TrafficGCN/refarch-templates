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
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        settingsId = UUID.randomUUID();
        now = LocalDateTime.now();

        responseDTO = new GlobalSettingsResponseDTO(
                settingsId,
                480,
                "https://example.com/logo.png",
                "Test Website",
                true,
                false,
                10,
                "en",
                "UA-12345",
                "contact@example.com",
                "Test website description",
                20,
                false,
                now,
                now);

        requestDTO = new GlobalSettingsRequestDTO(
                480,
                "https://example.com/logo.png",
                "Test Website",
                true,
                false,
                10,
                "en",
                "UA-12345",
                "contact@example.com",
                "Test website description",
                20,
                false);
    }

    @Test
    void getSettings_ShouldReturnSettings() throws Exception {
        when(globalSettingsService.getCurrentSettings()).thenReturn(responseDTO);

        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(settingsId.toString()))
                .andExpect(jsonPath("$.websiteName").value("Test Website"))
                .andExpect(jsonPath("$.sessionDurationMinutes").value(480))
                .andExpect(jsonPath("$.globalCommentsEnabled").value(true))
                .andExpect(jsonPath("$.ssoEnabled").value(false));
    }

    @Test
    void getSettings_ShouldReturn404_WhenSettingsNotFound() throws Exception {
        when(globalSettingsService.getCurrentSettings())
                .thenThrow(new EntityNotFoundException("Global settings not found"));

        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSettings_ShouldUpdateSettings() throws Exception {
        when(globalSettingsService.updateSettings(any(GlobalSettingsRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(settingsId.toString()))
                .andExpect(jsonPath("$.websiteName").value("Test Website"));
    }

    @Test
    void updateSettings_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Create invalid request with null required fields
        GlobalSettingsRequestDTO invalidRequest = new GlobalSettingsRequestDTO(
                null, // Invalid: sessionDurationMinutes is required
                "https://example.com/logo.png",
                "", // Invalid: websiteName cannot be blank
                null, // Invalid: globalCommentsEnabled is required
                null, // Invalid: maintenanceMode is required
                -1, // Invalid: maxUploadSizeMb must be positive
                "", // Invalid: defaultLanguage cannot be blank
                "UA-12345",
                "contact@example.com",
                "Test website description",
                0, // Invalid: maxItemsPerPage must be positive
                null // Invalid: ssoEnabled is required
        );

        mockMvc.perform(put("/api/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
