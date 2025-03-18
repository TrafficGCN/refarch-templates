package de.muenchen.refarch.post;

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
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostContentRepository postContentRepository;

    @Mock
    private LinkService linkService;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private PostService postService;

    private UUID postId;
    private UUID linkId;
    private UUID languageId;
    private Post post;
    private Link link;
    private Language language;
    private PostContent postContent;
    private PostRequestDTO postRequestDTO;
    private PostContentRequestDTO contentRequestDTO;
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
        link.setScope(LinkScope.EXTERNAL);

        language = new Language();
        language.setId(languageId);
        language.setName("English");
        language.setAbbreviation("en");

        post = new Post();
        post.setId(postId);
        post.setLink(link);
        post.setThumbnail("thumbnail.jpg");
        post.setCommentsEnabled(true);

        postContent = new PostContent();
        postContent.setId(UUID.randomUUID());
        postContent.setPost(post);
        postContent.setLanguage(language);
        postContent.setTitle("Test Title");
        postContent.setContent("Test Content");
        postContent.setShortDescription("Test Description");
        postContent.setKeywords("test,keywords");

        postRequestDTO = new PostRequestDTO(
                linkId,
                "thumbnail.jpg",
                true,
                true);

        contentRequestDTO = new PostContentRequestDTO(
                languageId,
                "Test Title",
                "Test Content",
                "Test Description",
                "test,keywords");
    }

    @Test
    void findAll_ShouldReturnAllPosts() {
        when(postRepository.findAll()).thenReturn(List.of(post));

        final List<PostResponseDTO> result = postService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(postId);
        verify(postRepository).findAll();
    }

    @Test
    void findById_WhenPostExists_ShouldReturnPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        final PostResponseDTO result = postService.findById(postId);

        assertThat(result.id()).isEqualTo(postId);
        verify(postRepository).findById(postId);
    }

    @Test
    void findById_WhenPostDoesNotExist_ShouldThrowException() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(postId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Post not found with id: " + postId);
        verify(postRepository).findById(postId);
    }

    @Test
    void create_ShouldCreatePost() {
        when(linkService.getById(linkId)).thenReturn(link);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        final PostResponseDTO result = postService.create(postRequestDTO);

        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.link()).isEqualTo(link);
        assertThat(result.thumbnail()).isEqualTo("thumbnail.jpg");
        assertThat(result.commentsEnabled()).isTrue();
        verify(linkService).getById(linkId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void update_WhenPostExists_ShouldUpdatePost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(linkService.getById(linkId)).thenReturn(link);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        final PostResponseDTO result = postService.update(postId, postRequestDTO);

        assertThat(result.id()).isEqualTo(postId);
        verify(postRepository).findById(postId);
        verify(linkService).getById(linkId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void delete_WhenPostExists_ShouldDeletePost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(postContentRepository).deleteAllByPost(post);
        doNothing().when(postRepository).delete(post);

        postService.delete(postId);

        verify(postRepository).findById(postId);
        verify(postContentRepository).deleteAllByPost(post);
        verify(postRepository).delete(post);
    }

    @Test
    void findAllContentByPost_WhenPostExists_ShouldReturnAllContent() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postContentRepository.findAllByPost(post)).thenReturn(List.of(postContent));

        final List<PostContentResponseDTO> result = postService.findAllContentByPost(postId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(postContent.getId());
        verify(postRepository).findById(postId);
        verify(postContentRepository).findAllByPost(post);
    }

    @Test
    void findContentByPostAndLanguage_WhenContentExists_ShouldReturnContent() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(postContentRepository.findByPostAndLanguage(post, language)).thenReturn(Optional.of(postContent));

        final PostContentResponseDTO result = postService.findContentByPostAndLanguage(postId, languageId);

        assertThat(result.id()).isEqualTo(postContent.getId());
        verify(postRepository).findById(postId);
        verify(languageService).getLanguageById(languageId);
        verify(postContentRepository).findByPostAndLanguage(post, language);
    }

    @Test
    void createContent_WhenContentDoesNotExist_ShouldCreateContent() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(postContentRepository.findByPostAndLanguage(post, language)).thenReturn(Optional.empty());
        when(postContentRepository.save(any(PostContent.class))).thenReturn(postContent);

        final PostContentResponseDTO result = postService.createContent(postId, contentRequestDTO);

        assertThat(result.id()).isEqualTo(postContent.getId());
        verify(postRepository).findById(postId);
        verify(languageService).getLanguageById(languageId);
        verify(postContentRepository).findByPostAndLanguage(post, language);
        verify(postContentRepository).save(any(PostContent.class));
    }

    @Test
    void createContent_WhenContentExists_ShouldThrowException() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(postContentRepository.findByPostAndLanguage(post, language)).thenReturn(Optional.of(postContent));

        assertThatThrownBy(() -> postService.createContent(postId, contentRequestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(String.format("Content already exists for post %s and language %s", postId, language.getAbbreviation()));
        verify(postRepository).findById(postId);
        verify(languageService).getLanguageById(languageId);
        verify(postContentRepository).findByPostAndLanguage(post, language);
        verify(postContentRepository, never()).save(any(PostContent.class));
    }

    @Test
    void updateContent_WhenContentExists_ShouldUpdateContent() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(postContentRepository.findByPostAndLanguage(post, language)).thenReturn(Optional.of(postContent));
        when(postContentRepository.save(any(PostContent.class))).thenReturn(postContent);

        final PostContentResponseDTO result = postService.updateContent(postId, languageId, contentRequestDTO);

        assertThat(result.id()).isEqualTo(postContent.getId());
        verify(postRepository).findById(postId);
        verify(languageService).getLanguageById(languageId);
        verify(postContentRepository).findByPostAndLanguage(post, language);
        verify(postContentRepository).save(any(PostContent.class));
    }

    @Test
    void deleteContent_WhenContentExists_ShouldDeleteContent() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(languageService.getLanguageById(languageId)).thenReturn(language);
        when(postContentRepository.findByPostAndLanguage(post, language)).thenReturn(Optional.of(postContent));
        doNothing().when(postContentRepository).delete(postContent);

        postService.deleteContent(postId, languageId);

        verify(postRepository).findById(postId);
        verify(languageService).getLanguageById(languageId);
        verify(postContentRepository).findByPostAndLanguage(post, language);
        verify(postContentRepository).delete(postContent);
    }
}
