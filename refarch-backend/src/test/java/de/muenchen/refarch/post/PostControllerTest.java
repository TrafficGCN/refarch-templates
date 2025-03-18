package de.muenchen.refarch.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.MicroServiceApplication;
import de.muenchen.refarch.TestConstants;
import de.muenchen.refarch.config.TestConfig;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkScope;
import de.muenchen.refarch.link.LinkService;
import de.muenchen.refarch.post.content.PostContentRepository;
import de.muenchen.refarch.post.content.dto.PostContentRequestDTO;
import de.muenchen.refarch.post.content.dto.PostContentResponseDTO;
import de.muenchen.refarch.post.dto.PostRequestDTO;
import de.muenchen.refarch.post.dto.PostResponseDTO;
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
@SuppressWarnings("PMD.TooManyFields")
class PostControllerTest {

    private static final String THUMBNAIL_PATH = "thumbnail.jpg";
    private static final String TEST_TITLE = "Test Title";
    private static final String TEST_CONTENT = "Test Content";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_KEYWORDS = "test,keywords";
    private static final String EXAMPLE_URL = "https://example.com";
    private static final String EXAMPLE_LINK_NAME = "Example Link";
    private static final String ENGLISH_LANGUAGE_NAME = "English";
    private static final String ENGLISH_LANGUAGE_ABBREV = "en";
    private static final String PMD_SUPPRESSION_SINGULAR_FIELD = "PMD.SingularField";

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
    @SuppressWarnings(PMD_SUPPRESSION_SINGULAR_FIELD)
    private UUID linkId;
    @SuppressWarnings(PMD_SUPPRESSION_SINGULAR_FIELD)
    private UUID languageId;
    @SuppressWarnings(PMD_SUPPRESSION_SINGULAR_FIELD)
    private Link link;
    @SuppressWarnings(PMD_SUPPRESSION_SINGULAR_FIELD)
    private Language language;
    private PostRequestDTO postRequestDTO;
    private PostResponseDTO postResponseDTO;
    private PostContentRequestDTO contentRequestDTO;
    private PostContentResponseDTO contentResponseDTO;
    @SuppressWarnings(PMD_SUPPRESSION_SINGULAR_FIELD)
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        linkId = UUID.randomUUID();
        languageId = UUID.randomUUID();
        now = LocalDateTime.now();

        link = new Link();
        link.setId(linkId);
        link.setUrl(EXAMPLE_URL);
        link.setName(EXAMPLE_LINK_NAME);
        link.setScope(LinkScope.EXTERNAL);

        language = new Language();
        language.setId(languageId);
        language.setName(ENGLISH_LANGUAGE_NAME);
        language.setAbbreviation(ENGLISH_LANGUAGE_ABBREV);

        postRequestDTO = new PostRequestDTO(
                linkId,
                THUMBNAIL_PATH,
                true,
                true);

        postResponseDTO = new PostResponseDTO(
                postId,
                link,
                THUMBNAIL_PATH,
                true,
                true,
                now,
                now);

        contentRequestDTO = new PostContentRequestDTO(
                languageId,
                TEST_TITLE,
                TEST_CONTENT,
                TEST_DESCRIPTION,
                TEST_KEYWORDS);

        contentResponseDTO = new PostContentResponseDTO(
                UUID.randomUUID(),
                postId,
                language,
                TEST_TITLE,
                TEST_CONTENT,
                TEST_DESCRIPTION,
                TEST_KEYWORDS,
                now,
                now);
    }

    @Test
    void shouldReturnAllPosts() throws Exception {
        when(postService.findAll()).thenReturn(List.of(postResponseDTO));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(postId.toString()))
                .andExpect(jsonPath("$[0].thumbnail").value(THUMBNAIL_PATH))
                .andExpect(jsonPath("$[0].commentsEnabled").value(true));

        verify(postService).findAll();
    }

    @Test
    void shouldReturnPostById() throws Exception {
        when(postService.findById(postId)).thenReturn(postResponseDTO);

        mockMvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(THUMBNAIL_PATH))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(postService).findById(postId);
    }

    @Test
    void shouldCreateAndReturnPost() throws Exception {
        when(postService.create(any(PostRequestDTO.class))).thenReturn(postResponseDTO);

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(THUMBNAIL_PATH))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(postService).create(any(PostRequestDTO.class));
    }

    @Test
    void shouldUpdateAndReturnPost() throws Exception {
        when(postService.update(eq(postId), any(PostRequestDTO.class))).thenReturn(postResponseDTO);

        mockMvc.perform(put("/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.thumbnail").value(THUMBNAIL_PATH))
                .andExpect(jsonPath("$.commentsEnabled").value(true));

        verify(postService).update(eq(postId), any(PostRequestDTO.class));
    }

    @Test
    void shouldDeletePost() throws Exception {
        doNothing().when(postService).delete(postId);

        mockMvc.perform(delete("/posts/{id}", postId))
                .andExpect(status().isNoContent());

        verify(postService).delete(postId);
    }

    @Test
    void shouldReturnAllPostContent() throws Exception {
        when(postService.findAllContentByPost(postId)).thenReturn(List.of(contentResponseDTO));

        mockMvc.perform(get("/posts/{postId}/content", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].postId").value(postId.toString()))
                .andExpect(jsonPath("$[0].title").value(TEST_TITLE))
                .andExpect(jsonPath("$[0].content").value(TEST_CONTENT));

        verify(postService).findAllContentByPost(postId);
    }

    @Test
    void shouldReturnPostContent() throws Exception {
        when(postService.findContentByPostAndLanguage(postId, languageId)).thenReturn(contentResponseDTO);

        mockMvc.perform(get("/posts/{postId}/content/{languageId}", postId, languageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(postId.toString()))
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.content").value(TEST_CONTENT));

        verify(postService).findContentByPostAndLanguage(postId, languageId);
    }

    @Test
    void shouldCreateAndReturnPostContent() throws Exception {
        when(postService.createContent(eq(postId), any(PostContentRequestDTO.class))).thenReturn(contentResponseDTO);

        mockMvc.perform(post("/posts/{postId}/content", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(postId.toString()))
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.content").value(TEST_CONTENT));

        verify(postService).createContent(eq(postId), any(PostContentRequestDTO.class));
    }

    @Test
    void shouldUpdateAndReturnPostContent() throws Exception {
        when(postService.updateContent(eq(postId), eq(languageId), any(PostContentRequestDTO.class))).thenReturn(contentResponseDTO);

        mockMvc.perform(put("/posts/{postId}/content/{languageId}", postId, languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId").value(postId.toString()))
                .andExpect(jsonPath("$.title").value(TEST_TITLE))
                .andExpect(jsonPath("$.content").value(TEST_CONTENT));

        verify(postService).updateContent(eq(postId), eq(languageId), any(PostContentRequestDTO.class));
    }

    @Test
    void shouldDeletePostContent() throws Exception {
        doNothing().when(postService).deleteContent(postId, languageId);

        mockMvc.perform(delete("/posts/{postId}/content/{languageId}", postId, languageId))
                .andExpect(status().isNoContent());

        verify(postService).deleteContent(postId, languageId);
    }
}
