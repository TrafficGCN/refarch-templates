package de.muenchen.refarch.user.bio;

import de.muenchen.refarch.user.bio.dto.UserBioRequestDTO;
import de.muenchen.refarch.user.bio.dto.UserBioResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBioControllerTest {

    @Mock
    private UserBioService userBioService;

    @InjectMocks
    private UserBioController userBioController;

    private UUID userId;
    private UUID languageId;
    private UUID bioId;
    private UserBioResponseDTO responseDTO;
    private UserBioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        bioId = UUID.randomUUID();

        responseDTO = new UserBioResponseDTO(
                bioId,
                userId,
                languageId,
                "Test bio",
                LocalDateTime.now(),
                LocalDateTime.now());

        requestDTO = new UserBioRequestDTO(userId, languageId, "Test bio");
    }

    @Test
    void getAllUserBios_ShouldReturnAllBios() {
        when(userBioService.getAllUserBios()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<UserBioResponseDTO>> response = userBioController.getAllUserBios();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0)).isEqualTo(responseDTO);
    }

    @Test
    void getUserBioById_ShouldReturnBio() {
        when(userBioService.getUserBioById(bioId)).thenReturn(responseDTO);

        ResponseEntity<UserBioResponseDTO> response = userBioController.getUserBioById(bioId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseDTO);
    }

    @Test
    void getUserBioByUserIdAndLanguageId_ShouldReturnBio() {
        when(userBioService.getUserBioByUserIdAndLanguageId(userId, languageId))
                .thenReturn(responseDTO);

        ResponseEntity<UserBioResponseDTO> response = userBioController
                .getUserBioByUserIdAndLanguageId(userId, languageId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseDTO);
    }

    @Test
    void createUserBio_ShouldCreateBio() {
        when(userBioService.createUserBio(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<UserBioResponseDTO> response = userBioController.createUserBio(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseDTO);
    }

    @Test
    void updateUserBio_ShouldUpdateBio() {
        when(userBioService.updateUserBio(bioId, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<UserBioResponseDTO> response = userBioController
                .updateUserBio(bioId, requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseDTO);
    }

    @Test
    void deleteUserBio_ShouldDeleteBio() {
        ResponseEntity<Void> response = userBioController.deleteUserBio(bioId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userBioService).deleteUserBio(bioId);
    }
}
