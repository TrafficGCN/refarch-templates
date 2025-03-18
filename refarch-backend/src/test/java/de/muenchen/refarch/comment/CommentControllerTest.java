package de.muenchen.refarch.comment;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.comment.dto.CommentRequestDTO;
import de.muenchen.refarch.comment.dto.CommentResponseDTO;
import de.muenchen.refarch.user.dto.UserResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(
        classes = { MicroServiceApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles(profiles = { TestConstants.SPRING_TEST_PROFILE, TestConstants.SPRING_NO_SECURITY_PROFILE })
@AutoConfigureMockMvc
class CommentControllerTest {

    private static final String API_COMMENTS = "/api/comments";
    private static final String API_COMMENTS_ID = "/api/comments/{commentId}/user/{userId}";
    private static final String API_COMMENTS_POST = "/api/comments/post/{postId}";
    private static final String API_COMMENTS_PAGE = "/api/comments/page/{pageId}";
    private static final String API_COMMENTS_USER = "/api/comments/user/{userId}";
    private static final String API_COMMENTS_POST_CREATE = "/api/comments/post/{postId}/user/{userId}";
    private static final String API_COMMENTS_PAGE_CREATE = "/api/comments/page/{pageId}/user/{userId}";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_FIRST_NAME = "Test";
    private static final String TEST_LAST_NAME = "User";
    private static final String TEST_TITLE = "Mr.";
    private static final String TEST_COMPANY = "Test Company";
    private static final String TEST_THUMBNAIL = "thumbnail.jpg";
    private static final String TEST_COMMENT = "Test comment";
    private static final String TEST_UPDATED_COMMENT = "Updated comment";
    private static final String BLANK_COMMENT = "";

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
    private CommentService commentService;

    private UUID userId;
    private UUID postId;
    private UUID pageId;
    private UUID commentId;
    private UserResponseDTO userResponseDTO;
    private CommentResponseDTO commentResponseDTO;
    private CommentRequestDTO commentRequestDTO;
    private LocalDateTime now;

    @Configuration
    @EnableWebMvc
    /* default */ @SuppressWarnings({ "PMD.TestClassWithoutTestCases" })
    static class TestConfig {
        @Bean
        public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        pageId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        now = LocalDateTime.now();

        userResponseDTO = new UserResponseDTO(
                userId,
                TEST_USERNAME,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_TITLE,
                TEST_COMPANY,
                TEST_THUMBNAIL,
                now,
                now);

        commentResponseDTO = new CommentResponseDTO(
                commentId,
                TEST_COMMENT,
                postId,
                null,
                userResponseDTO,
                now,
                now);

        commentRequestDTO = new CommentRequestDTO(TEST_COMMENT);
    }

    @Test
    void whenGettingCommentsByPost_shouldReturnList() throws Exception {
        // Arrange
        final List<CommentResponseDTO> comments = List.of(commentResponseDTO);
        when(commentService.findByPostId(postId)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get(API_COMMENTS_POST, postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].content").value(TEST_COMMENT));
    }

    @Test
    void whenGettingCommentsByPage_shouldReturnList() throws Exception {
        // Arrange
        final List<CommentResponseDTO> comments = List.of(commentResponseDTO);
        when(commentService.findByPageId(pageId)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get(API_COMMENTS_PAGE, pageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].content").value(TEST_COMMENT));
    }

    @Test
    void whenGettingCommentsByUser_shouldReturnList() throws Exception {
        // Arrange
        final List<CommentResponseDTO> comments = List.of(commentResponseDTO);
        when(commentService.findByUserId(userId)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get(API_COMMENTS_USER, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].content").value(TEST_COMMENT));
    }

    @Test
    void whenCreatingPostComment_shouldReturnCreated() throws Exception {
        // Arrange
        when(commentService.createPostComment(eq(userId), eq(postId), eq(TEST_COMMENT))).thenReturn(commentResponseDTO);

        // Act & Assert
        mockMvc.perform(post(API_COMMENTS_POST_CREATE, postId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value(TEST_COMMENT));
    }

    @Test
    void whenCreatingPageComment_shouldReturnCreated() throws Exception {
        // Arrange
        final CommentRequestDTO pageCommentRequest = new CommentRequestDTO(TEST_COMMENT);
        final CommentResponseDTO pageCommentResponse = new CommentResponseDTO(
                commentId,
                TEST_COMMENT,
                null,
                pageId,
                userResponseDTO,
                now,
                now);
        when(commentService.createPageComment(eq(userId), eq(pageId), eq(TEST_COMMENT))).thenReturn(pageCommentResponse);

        // Act & Assert
        mockMvc.perform(post(API_COMMENTS_PAGE_CREATE, pageId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value(TEST_COMMENT));
    }

    @Test
    void whenUpdatingComment_shouldReturnUpdated() throws Exception {
        // Arrange
        final CommentRequestDTO updateRequest = new CommentRequestDTO(TEST_UPDATED_COMMENT);
        final CommentResponseDTO updatedResponse = new CommentResponseDTO(
                commentId,
                TEST_UPDATED_COMMENT,
                postId,
                pageId,
                userResponseDTO,
                now,
                now);
        when(commentService.updateComment(eq(commentId), eq(userId), eq(TEST_UPDATED_COMMENT))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put(API_COMMENTS_ID, commentId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value(TEST_UPDATED_COMMENT));
    }

    @Test
    void whenDeletingComment_shouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(API_COMMENTS_ID, commentId, userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenCreatingCommentWithBlankContent_shouldReturnBadRequest() throws Exception {
        // Arrange
        final CommentRequestDTO invalidRequest = new CommentRequestDTO(BLANK_COMMENT);

        // Act & Assert
        mockMvc.perform(post(API_COMMENTS_POST_CREATE, postId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
