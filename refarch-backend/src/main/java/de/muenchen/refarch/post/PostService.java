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
import de.muenchen.refarch.security.Authorities;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final String POST_NOT_FOUND = "Post not found with id: ";
    private static final String CONTENT_NOT_FOUND = "Content not found for post %s and language %s";
    private static final String CONTENT_EXISTS = "Content already exists for post %s and language %s";

    private final PostRepository postRepository;
    private final PostContentRepository postContentRepository;
    private final LinkService linkService;
    private final LanguageService languageService;

    @PreAuthorize(Authorities.POST_READ)
    @Transactional(readOnly = true)
    public List<PostResponseDTO> findAll() {
        return postRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.POST_READ)
    @Transactional(readOnly = true)
    public PostResponseDTO findById(final UUID id) {
        return postRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + id));
    }

    @PreAuthorize(Authorities.POST_WRITE)
    @Transactional
    public PostResponseDTO create(final PostRequestDTO request) {
        final Link link = linkService.getById(request.linkId());

        final Post post = new Post();
        post.setLink(link);
        post.setThumbnail(request.thumbnail());
        post.setCommentsEnabled(request.commentsEnabled());
        post.setPublished(request.published());

        return mapToResponseDTO(postRepository.save(post));
    }

    @PreAuthorize(Authorities.POST_WRITE)
    @Transactional
    public PostResponseDTO update(final UUID id, final PostRequestDTO request) {
        final Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + id));
        final Link link = linkService.getById(request.linkId());

        existingPost.setLink(link);
        existingPost.setThumbnail(request.thumbnail());
        existingPost.setCommentsEnabled(request.commentsEnabled());
        existingPost.setPublished(request.published());

        return mapToResponseDTO(postRepository.save(existingPost));
    }

    @PreAuthorize(Authorities.POST_WRITE)
    @Transactional
    public void delete(final UUID id) {
        final Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + id));
        postContentRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }

    @PreAuthorize(Authorities.POST_READ)
    @Transactional(readOnly = true)
    public List<PostContentResponseDTO> findAllContentByPost(final UUID postId) {
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + postId));
        return postContentRepository.findAllByPost(post).stream()
                .map(this::mapToContentResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.POST_READ)
    @Transactional(readOnly = true)
    public PostContentResponseDTO findContentByPostAndLanguage(final UUID postId, final UUID languageId) {
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + postId));
        final Language language = languageService.getLanguageById(languageId);

        return postContentRepository.findByPostAndLanguage(post, language)
                .map(this::mapToContentResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, postId, languageId)));
    }

    @PreAuthorize(Authorities.POST_WRITE)
    @Transactional
    public PostContentResponseDTO createContent(final UUID postId, final PostContentRequestDTO request) {
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + postId));
        final Language language = languageService.getLanguageById(request.languageId());

        final Optional<PostContent> existingContent = postContentRepository.findByPostAndLanguage(post, language);
        if (existingContent.isPresent()) {
            throw new IllegalStateException(
                    String.format(CONTENT_EXISTS, postId, language.getAbbreviation()));
        }

        final PostContent content = new PostContent();
        content.setPost(post);
        content.setLanguage(language);
        content.setTitle(request.title());
        content.setContent(request.content());
        content.setShortDescription(request.shortDescription());
        content.setKeywords(request.keywords());

        return mapToContentResponseDTO(postContentRepository.save(content));
    }

    @PreAuthorize(Authorities.POST_WRITE)
    @Transactional
    public PostContentResponseDTO updateContent(final UUID postId, final UUID languageId, final PostContentRequestDTO request) {
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + postId));
        final Language language = languageService.getLanguageById(languageId);

        final PostContent existingContent = postContentRepository.findByPostAndLanguage(post, language)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, postId, languageId)));

        existingContent.setTitle(request.title());
        existingContent.setContent(request.content());
        existingContent.setShortDescription(request.shortDescription());
        existingContent.setKeywords(request.keywords());

        return mapToContentResponseDTO(postContentRepository.save(existingContent));
    }

    @PreAuthorize(Authorities.POST_WRITE)
    @Transactional
    public void deleteContent(final UUID postId, final UUID languageId) {
        final Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + postId));
        final Language language = languageService.getLanguageById(languageId);

        final PostContent content = postContentRepository.findByPostAndLanguage(post, language)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, postId, languageId)));

        postContentRepository.delete(content);
    }

    @PreAuthorize(Authorities.POST_WRITE)
    @Transactional
    public void updatePublished(final UUID id, final boolean published) {
        final Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND + id));
        post.setPublished(published);
        postRepository.save(post);
    }

    private PostResponseDTO mapToResponseDTO(final Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getLink(),
                post.getThumbnail(),
                post.isCommentsEnabled(),
                post.isPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    private PostContentResponseDTO mapToContentResponseDTO(final PostContent content) {
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
