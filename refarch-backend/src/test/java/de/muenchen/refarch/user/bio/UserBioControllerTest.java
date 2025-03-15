package de.muenchen.refarch.user.bio;

import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.globalsettings.GlobalSettingsService;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserService;
import de.muenchen.refarch.user.bio.dto.UserBioRequestDTO;
import de.muenchen.refarch.user.bio.dto.UserBioResponseDTO;
import de.muenchen.refarch.user.dto.UserResponseDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class UserBioControllerTest {

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
    private UserBioService userBioService;

    @MockBean
    private UserBioRepository userBioRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private LanguageService languageService;

    @MockBean
    private GlobalSettingsService globalSettingsService;

    private User testUser;

    private UUID bioId;
    private UUID languageId;
    private Language language;
    private UserBioRequestDTO requestDTO;
    private UserBioResponseDTO responseDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        languageId = UUID.randomUUID();
        bioId = UUID.randomUUID();
        now = LocalDateTime.now();

        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setTitle("Dr.");
        testUser.setAffiliation("Test Organization");
        testUser.setThumbnail("test.jpg");
        testUser.setPassword("password");

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getTitle(),
                testUser.getAffiliation(),
                testUser.getThumbnail(),
                now,
                now);

        when(userService.getUserById(any(UUID.class))).thenReturn(userResponseDTO);
        when(languageService.getLanguageById(any(UUID.class))).thenReturn(language);

        responseDTO = new UserBioResponseDTO(
                bioId,
                testUser.getId(),
                languageId,
                "Test bio",
                now,
                now);

        requestDTO = new UserBioRequestDTO(testUser.getId(), languageId, "Test bio");

        GlobalSettingsResponseDTO defaultSettings = new GlobalSettingsResponseDTO(
                UUID.randomUUID(), // id
                480, // sessionDurationMinutes
                "http://example.com/logo.png", // logoUrl
                "Test Website", // websiteName
                true, // globalCommentsEnabled
                false, // maintenanceMode
                10, // maxUploadSizeMb
                "en", // defaultLanguage
                "UA-123456", // analyticsTrackingId
                "test@example.com", // contactEmail
                "Test description", // metaDescription
                50, // maxItemsPerPage
                false, // ssoAuthEnabled
                true, // passwordAuthEnabled
                LocalDateTime.now(), // createdAt
                LocalDateTime.now() // updatedAt
        );
        when(globalSettingsService.getCurrentSettings()).thenReturn(defaultSettings);
    }

    @Test
    void getAllUserBios_ShouldReturnAllBios() throws Exception {
        when(userBioService.getAllUserBios()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/user-bios"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(bioId.toString()))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$[0].languageId").value(languageId.toString()))
                .andExpect(jsonPath("$[0].bio").value("Test bio"));

        verify(userBioService).getAllUserBios();
    }

    @Test
    void getUserBioById_ShouldReturnBio() throws Exception {
        when(userBioService.getUserBioById(bioId)).thenReturn(responseDTO);

        mockMvc.perform(get("/user-bios/{id}", bioId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(bioId.toString()))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.languageId").value(languageId.toString()))
                .andExpect(jsonPath("$.bio").value("Test bio"));

        verify(userBioService).getUserBioById(bioId);
    }

    @Test
    void getUserBioByUserIdAndLanguageId_ShouldReturnBio() throws Exception {
        when(userBioService.getUserBioByUserIdAndLanguageId(testUser.getId(), languageId))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/user-bios/user/{userId}/language/{languageId}", testUser.getId(), languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(bioId.toString()))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.languageId").value(languageId.toString()))
                .andExpect(jsonPath("$.bio").value("Test bio"));

        verify(userBioService).getUserBioByUserIdAndLanguageId(testUser.getId(), languageId);
    }

    @Test
    void createUserBio_ShouldCreateBio() throws Exception {
        when(userBioService.createUserBio(any(UserBioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/user-bios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(bioId.toString()))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.languageId").value(languageId.toString()))
                .andExpect(jsonPath("$.bio").value("Test bio"));

        verify(userBioService).createUserBio(any(UserBioRequestDTO.class));
    }

    @Test
    void updateUserBio_ShouldUpdateBio() throws Exception {
        when(userBioService.updateUserBio(eq(bioId), any(UserBioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/user-bios/{id}", bioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(bioId.toString()))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.languageId").value(languageId.toString()))
                .andExpect(jsonPath("$.bio").value("Test bio"));

        verify(userBioService).updateUserBio(eq(bioId), any(UserBioRequestDTO.class));
    }

    @Test
    void deleteUserBio_ShouldDeleteBio() throws Exception {
        doNothing().when(userBioService).deleteUserBio(bioId);

        mockMvc.perform(delete("/user-bios/{id}", bioId))
                .andExpect(status().isNoContent());

        verify(userBioService).deleteUserBio(bioId);
    }
}
