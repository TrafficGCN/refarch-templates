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

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Configuration
    @EnableWebMvc
    static class TestConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

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

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        pageId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        now = LocalDateTime.now();

        userResponseDTO = new UserResponseDTO(
                userId,
                "testUser",
                "test@example.com",
                "Test",
                "User",
                "Mr.",
                "Test Company",
                "thumbnail.jpg",
                now,
                now);

        commentResponseDTO = new CommentResponseDTO(
                commentId,
                "Test comment",
                postId,
                null,
                userResponseDTO,
                now,
                now);

        commentRequestDTO = new CommentRequestDTO("Test comment");
    }

    @Test
    void getCommentsByPost_ShouldReturnComments() throws Exception {
        when(commentService.findByPostId(eq(postId))).thenReturn(List.of(commentResponseDTO));

        mockMvc.perform(get("/api/comments/post/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].content").value("Test comment"));
    }

    @Test
    void getCommentsByPage_ShouldReturnComments() throws Exception {
        when(commentService.findByPageId(eq(pageId))).thenReturn(List.of(commentResponseDTO));

        mockMvc.perform(get("/api/comments/page/{pageId}", pageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].content").value("Test comment"));
    }

    @Test
    void getCommentsByUser_ShouldReturnComments() throws Exception {
        when(commentService.findByUserId(eq(userId))).thenReturn(List.of(commentResponseDTO));

        mockMvc.perform(get("/api/comments/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].content").value("Test comment"));
    }

    @Test
    void createPostComment_ShouldCreateComment() throws Exception {
        when(commentService.createPostComment(eq(userId), eq(postId), eq("Test comment"))).thenReturn(commentResponseDTO);

        mockMvc.perform(post("/api/comments/post/{postId}/user/{userId}", postId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    void createPageComment_ShouldCreateComment() throws Exception {
        when(commentService.createPageComment(eq(userId), eq(pageId), eq("Test comment"))).thenReturn(commentResponseDTO);

        mockMvc.perform(post("/api/comments/page/{pageId}/user/{userId}", pageId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    void updateComment_ShouldUpdateComment() throws Exception {
        when(commentService.updateComment(eq(commentId), eq(userId), eq("Test comment"))).thenReturn(commentResponseDTO);

        mockMvc.perform(put("/api/comments/{commentId}/user/{userId}", commentId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    void deleteComment_ShouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/comments/{commentId}/user/{userId}", commentId, userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createComment_ShouldReturnBadRequest_WhenContentIsBlank() throws Exception {
        commentRequestDTO = new CommentRequestDTO("");

        mockMvc.perform(post("/api/comments/post/{postId}/user/{userId}", postId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDTO)))
                .andExpect(status().isBadRequest());
    }
}
