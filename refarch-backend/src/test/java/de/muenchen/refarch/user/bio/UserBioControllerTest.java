package de.muenchen.refarch.user.bio;

import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.globalsettings.GlobalSettingsService;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserService;
import de.muenchen.refarch.user.bio.dto.UserBioRequestDTO;
import de.muenchen.refarch.user.bio.dto.UserBioResponseDTO;

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

    private static final String API_USER_BIOS = "/user-bios";
    private static final String API_USER_BIOS_ID = "/user-bios/{id}";
    private static final String API_USER_BIOS_USER_LANGUAGE = "/user-bios/user/{userId}/language/{languageId}";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "Test";
    private static final String TEST_LAST_NAME = "User";
    private static final String TEST_BIO = "Test bio";
    private static final String TEST_LANGUAGE_NAME = "English";
    private static final String TEST_LANGUAGE_ABBREV = "en";
    private static final String TEST_LANGUAGE_ICON = "fa-flag-usa";
    private static final String TEST_LANGUAGE_MDI = "mdi-flag";
    private static final String JSON_PATH_ID = "$.id";
    private static final String JSON_PATH_BIO = "$.bio";

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
    private UserBioRequestDTO requestDTO;
    private UserBioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        languageId = UUID.randomUUID();
        bioId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.setFirstName(TEST_FIRST_NAME);
        testUser.setLastName(TEST_LAST_NAME);

        // Create test language
        final Language language = new Language();
        language.setId(languageId);
        language.setName(TEST_LANGUAGE_NAME);
        language.setAbbreviation(TEST_LANGUAGE_ABBREV);
        language.setFontAwesomeIcon(TEST_LANGUAGE_ICON);
        language.setMdiIcon(TEST_LANGUAGE_MDI);

        // Create test request DTO
        requestDTO = new UserBioRequestDTO(testUser.getId(), languageId, TEST_BIO);

        // Create test response DTO
        responseDTO = new UserBioResponseDTO(
                bioId,
                testUser.getId(),
                languageId,
                TEST_BIO,
                now,
                now);
    }

    @Test
    void whenGettingAllUserBios_shouldReturnList() throws Exception {
        // Arrange
        final List<UserBioResponseDTO> bios = List.of(responseDTO);
        when(userBioService.getAllUserBios()).thenReturn(bios);

        // Act & Assert
        mockMvc.perform(get(API_USER_BIOS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(bioId.toString()))
                .andExpect(jsonPath("$[0].bio").value(TEST_BIO));

        verify(userBioService).getAllUserBios();
    }

    @Test
    void whenGettingUserBioById_shouldReturnBio() throws Exception {
        // Arrange
        when(userBioService.getUserBioById(bioId)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get(API_USER_BIOS_ID, bioId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_PATH_ID).value(bioId.toString()))
                .andExpect(jsonPath(JSON_PATH_BIO).value(TEST_BIO));

        verify(userBioService).getUserBioById(bioId);
    }

    @Test
    void whenGettingUserBioByUserIdAndLanguageId_shouldReturnBio() throws Exception {
        // Arrange
        when(userBioService.getUserBioByUserIdAndLanguageId(testUser.getId(), languageId))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get(API_USER_BIOS_USER_LANGUAGE, testUser.getId(), languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_PATH_ID).value(bioId.toString()))
                .andExpect(jsonPath(JSON_PATH_BIO).value(TEST_BIO));

        verify(userBioService).getUserBioByUserIdAndLanguageId(testUser.getId(), languageId);
    }

    @Test
    void whenCreatingUserBio_shouldReturnCreatedBio() throws Exception {
        // Arrange
        when(userBioService.createUserBio(any(UserBioRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post(API_USER_BIOS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(bioId.toString()))
                .andExpect(jsonPath("$.bio").value(TEST_BIO));

        verify(userBioService).createUserBio(any(UserBioRequestDTO.class));
    }

    @Test
    void whenUpdatingUserBio_shouldReturnUpdatedBio() throws Exception {
        // Arrange
        when(userBioService.updateUserBio(eq(bioId), any(UserBioRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put(API_USER_BIOS_ID, bioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_PATH_ID).value(bioId.toString()))
                .andExpect(jsonPath(JSON_PATH_BIO).value(TEST_BIO));

        verify(userBioService).updateUserBio(eq(bioId), any(UserBioRequestDTO.class));
    }

    @Test
    void whenDeletingUserBio_shouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(API_USER_BIOS_ID, bioId))
                .andExpect(status().isNoContent());

        verify(userBioService).deleteUserBio(bioId);
    }
}
