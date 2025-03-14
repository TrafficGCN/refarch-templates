package de.muenchen.refarch.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.link.LinkService;
import de.muenchen.refarch.post.content.PostContent;
import de.muenchen.refarch.post.content.PostContentRepository;
import de.muenchen.refarch.post.dto.PostRequestDTO;
import de.muenchen.refarch.post.dto.PostResponseDTO;
import de.muenchen.refarch.post.content.dto.PostContentRequestDTO;
import de.muenchen.refarch.post.content.dto.PostContentResponseDTO;
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
class PostControllerTest {

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
    private PostService postService;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private PostContentRepository postContentRepository;

    @MockBean
    private LinkService linkService;

    @MockBean
    private LanguageService languageService;

    private UUID postId;
    private UUID linkId;
    private UUID languageId;
    private Link link;
    private Language language;
    private PostRequestDTO postRequestDTO;
    private PostResponseDTO postResponseDTO;
    private PostContentRequestDTO contentRequestDTO;
    private PostContentResponseDTO contentResponseDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        linkId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        now = LocalDateTime.now();

        link = new Link();
        link.setId(linkId);
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setScope(LinkScope.external);

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");

        postRequestDTO = new PostRequestDTO(
                linkId,
                "thumbnail.jpg",
                true);

        postResponseDTO = new PostResponseDTO(
                postId,
                link,
                "thumbnail.jpg",
                true,
                now,
                now);

        contentRequestDTO = new PostContentRequestDTO(
                languageId,
                "Test Title",
                "Test Content",
                "Test Description",
                "test,keywords");

        contentResponseDTO = new PostContentResponseDTO(
                UUID.randomUUID(),
                postId,
                language,
                "Test Title",
                "Test Content",
                "Test Description",
                "test,keywords",
                now,
                now);
    }

    @Test
    void getAllPosts_ShouldReturnListOfPosts() throws Exception {
        when(postService.findAll()).thenReturn(List.of(postResponseDTO));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(postId.toString()))
                .andExpect(jsonPath("$[0].thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$[0].commentsEnabled").value(true));

        verify(postService).findAll();
    }

    @Test
    void getPostById_ShouldReturnPost() throws Exception {
        when(postService.findById(postId)).thenReturn(postResponseDTO);

        mockMvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(postService).findById(postId);
    }

    @Test
    void createPost_ShouldCreateAndReturnPost() throws Exception {
        when(postService.create(any(PostRequestDTO.class))).thenReturn(postResponseDTO);

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(postService).create(any(PostRequestDTO.class));
    }

    @Test
    void updatePost_ShouldUpdateAndReturnPost() throws Exception {
        when(postService.update(eq(postId), any(PostRequestDTO.class))).thenReturn(postResponseDTO);

        mockMvc.perform(put("/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail.jpg"))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(postService).update(eq(postId), any(PostRequestDTO.class));
    }

    @Test
    void deletePost_ShouldDeletePost() throws Exception {
        doNothing().when(postService).delete(postId);

        mockMvc.perform(delete("/posts/{id}", postId))
                .andExpect(status().isNoContent());

        verify(postService).delete(postId);
    }

    @Test
    void getAllPostContent_ShouldReturnListOfContent() throws Exception {
        when(postService.findAllContentByPost(postId)).thenReturn(List.of(contentResponseDTO));

        mockMvc.perform(get("/posts/{postId}/content", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].postId").value(postId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].content").value("Test Content"));

        verify(postService).findAllContentByPost(postId);
    }

    @Test
    void getPostContent_ShouldReturnContent() throws Exception {
        when(postService.findContentByPostAndLanguage(postId, languageId)).thenReturn(contentResponseDTO);

        mockMvc.perform(get("/posts/{postId}/content/{languageId}", postId, languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(postId.toString()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(postService).findContentByPostAndLanguage(postId, languageId);
    }

    @Test
    void createPostContent_ShouldCreateAndReturnContent() throws Exception {
        when(postService.createContent(eq(postId), any(PostContentRequestDTO.class))).thenReturn(contentResponseDTO);

        mockMvc.perform(post("/posts/{postId}/content", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(postId.toString()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(postService).createContent(eq(postId), any(PostContentRequestDTO.class));
    }

    @Test
    void updatePostContent_ShouldUpdateAndReturnContent() throws Exception {
        when(postService.updateContent(eq(postId), eq(languageId), any(PostContentRequestDTO.class)))
                .thenReturn(contentResponseDTO);

        mockMvc.perform(put("/posts/{postId}/content/{languageId}", postId, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(postId.toString()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(postService).updateContent(eq(postId), eq(languageId), any(PostContentRequestDTO.class));
    }

    @Test
    void deletePostContent_ShouldDeleteContent() throws Exception {
        doNothing().when(postService).deleteContent(postId, languageId);

        mockMvc.perform(delete("/posts/{postId}/content/{languageId}", postId, languageId))
                .andExpect(status().isNoContent());

        verify(postService).deleteContent(postId, languageId);
    }
}
