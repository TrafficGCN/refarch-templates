package de.muenchen.refarch.posts.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.user.User;
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

import java.util.List;
import java.util.UUID;

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
class PostsUsersControllerTest {

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
    private PostsUsersService postsUsersService;

    private UUID postLinkId;
    private UUID userId;
    private PostsUsers postsUsers;

    @BeforeEach
    void setUp() {
        postLinkId = UUID.randomUUID();
        userId = UUID.randomUUID();

        final User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        postsUsers = new PostsUsers();
        postsUsers.setId(UUID.randomUUID());
        postsUsers.setPostLinkId(postLinkId);
        postsUsers.setUser(user);
    }

    @Test
    void shouldReturnUsersAssociatedWithPost() throws Exception {
        when(postsUsersService.findByPostLinkId(postLinkId)).thenReturn(List.of(postsUsers));

        mockMvc.perform(get("/api/posts-users/post/{postLinkId}", postLinkId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(postsUsers.getId().toString()))
                .andExpect(jsonPath("$[0].postLinkId").value(postLinkId.toString()))
                .andExpect(jsonPath("$[0].user.id").value(userId.toString()));

        verify(postsUsersService).findByPostLinkId(postLinkId);
    }

    @Test
    void shouldReturnPostsAssociatedWithUser() throws Exception {
        when(postsUsersService.findByUserId(userId)).thenReturn(List.of(postsUsers));

        mockMvc.perform(get("/api/posts-users/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(postsUsers.getId().toString()))
                .andExpect(jsonPath("$[0].postLinkId").value(postLinkId.toString()))
                .andExpect(jsonPath("$[0].user.id").value(userId.toString()));

        verify(postsUsersService).findByUserId(userId);
    }

    @Test
    void shouldAssignUserToPostAndReturnAssociation() throws Exception {
        when(postsUsersService.assignUserToPost(postLinkId, userId)).thenReturn(postsUsers);

        mockMvc.perform(post("/api/posts-users/{postLinkId}/user/{userId}", postLinkId, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postsUsers.getId().toString()))
                .andExpect(jsonPath("$.postLinkId").value(postLinkId.toString()))
                .andExpect(jsonPath("$.user.id").value(userId.toString()));

        verify(postsUsersService).assignUserToPost(postLinkId, userId);
    }

    @Test
    void shouldRemoveUserFromPost() throws Exception {
        mockMvc.perform(delete("/api/posts-users/{postLinkId}/user/{userId}", postLinkId, userId))
                .andExpect(status().isNoContent());

        verify(postsUsersService).removeUserFromPost(postLinkId, userId);
    }
}
