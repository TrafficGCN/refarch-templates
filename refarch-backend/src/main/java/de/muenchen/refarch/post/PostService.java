package de.muenchen.refarch.post;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkService;
import de.muenchen.refarch.post.content.PostContent;
import de.muenchen.refarch.post.content.PostContentRepository;
import de.muenchen.refarch.post.dto.PostRequestDTO;
import de.muenchen.refarch.post.dto.PostResponseDTO;
import de.muenchen.refarch.post.content.dto.PostContentRequestDTO;
import de.muenchen.refarch.post.content.dto.PostContentResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostContentRepository postContentRepository;
    private final LinkService linkService;
    private final LanguageService languageService;

    @Transactional(readOnly = true)
    public List<PostResponseDTO> findAll() {
        return postRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostResponseDTO findById(UUID id) {
        return postRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
    }

    @Transactional
    public PostResponseDTO create(PostRequestDTO request) {
        Link link = linkService.getById(request.linkId());

        Post post = new Post();
        post.setLink(link);
        post.setThumbnail(request.thumbnail());
        post.setCommentsEnabled(request.commentsEnabled());
        post.setPublished(request.published());

        return mapToResponseDTO(postRepository.save(post));
    }

    @Transactional
    public PostResponseDTO update(UUID id, PostRequestDTO request) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
        Link link = linkService.getById(request.linkId());

        existingPost.setLink(link);
        existingPost.setThumbnail(request.thumbnail());
        existingPost.setCommentsEnabled(request.commentsEnabled());
        existingPost.setPublished(request.published());

        return mapToResponseDTO(postRepository.save(existingPost));
    }

    @Transactional
    public void delete(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
        postContentRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostContentResponseDTO> findAllContentByPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        return postContentRepository.findAllByPost(post).stream()
                .map(this::mapToContentResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostContentResponseDTO findContentByPostAndLanguage(UUID postId, UUID languageId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        Language language = languageService.getLanguageById(languageId);

        return postContentRepository.findByPostAndLanguage(post, language)
                .map(this::mapToContentResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for post %s and language %s", postId, languageId)));
    }

    @Transactional
    public PostContentResponseDTO createContent(UUID postId, PostContentRequestDTO request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        Language language = languageService.getLanguageById(request.languageId());

        Optional<PostContent> existingContent = postContentRepository.findByPostAndLanguage(post, language);
        if (existingContent.isPresent()) {
            throw new IllegalStateException(
                    String.format("Content already exists for post %s and language %s", postId, language.getAbbreviation()));
        }

        PostContent content = new PostContent();
        content.setPost(post);
        content.setLanguage(language);
        content.setTitle(request.title());
        content.setContent(request.content());
        content.setShortDescription(request.shortDescription());
        content.setKeywords(request.keywords());

        return mapToContentResponseDTO(postContentRepository.save(content));
    }

    @Transactional
    public PostContentResponseDTO updateContent(UUID postId, UUID languageId, PostContentRequestDTO request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        Language language = languageService.getLanguageById(languageId);

        PostContent existingContent = postContentRepository.findByPostAndLanguage(post, language)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for post %s and language %s", postId, languageId)));

        existingContent.setTitle(request.title());
        existingContent.setContent(request.content());
        existingContent.setShortDescription(request.shortDescription());
        existingContent.setKeywords(request.keywords());

        return mapToContentResponseDTO(postContentRepository.save(existingContent));
    }

    @Transactional
    public void deleteContent(UUID postId, UUID languageId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        Language language = languageService.getLanguageById(languageId);

        PostContent content = postContentRepository.findByPostAndLanguage(post, language)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for post %s and language %s", postId, languageId)));

        postContentRepository.delete(content);
    }

    @Transactional
    public PostResponseDTO setPublished(UUID id, boolean published) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
        post.setPublished(published);
        return mapToResponseDTO(postRepository.save(post));
    }

    private PostResponseDTO mapToResponseDTO(Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getLink(),
                post.getThumbnail(),
                post.isCommentsEnabled(),
                post.isPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    private PostContentResponseDTO mapToContentResponseDTO(PostContent content) {
        return new PostContentResponseDTO(
                content.getId(),
                content.getPost().getId(),
                content.getLanguage(),
                content.getTitle(),
                content.getContent(),
                content.getShortDescription(),
                content.getKeywords(),
                content.getCreatedAt(),
                content.getUpdatedAt());
    }
}
