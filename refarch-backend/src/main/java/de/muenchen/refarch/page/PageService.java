package de.muenchen.refarch.page;

import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkService;
import de.muenchen.refarch.page.content.PageContent;
import de.muenchen.refarch.page.content.PageContentRepository;
import de.muenchen.refarch.page.dto.PageRequestDTO;
import de.muenchen.refarch.page.dto.PageResponseDTO;
import de.muenchen.refarch.page.content.dto.PageContentRequestDTO;
import de.muenchen.refarch.page.content.dto.PageContentResponseDTO;
import de.muenchen.refarch.security.Authorities;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageService {
    private static final String PAGE_NOT_FOUND = "Page not found with id: ";
    private static final String CONTENT_NOT_FOUND = "Content not found for page %s and language %s";
    private static final String CONTENT_EXISTS = "Content already exists for page %s and language %s";

    private final PageRepository pageRepository;
    private final PageContentRepository pageContentRepository;
    private final LinkService linkService;
    private final LanguageService languageService;

    @PreAuthorize(Authorities.PAGE_READ)
    @Transactional(readOnly = true)
    public List<PageResponseDTO> findAll() {
        return pageRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.PAGE_READ)
    @Transactional(readOnly = true)
    public PageResponseDTO findById(final UUID id) {
        return pageRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + id));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageResponseDTO create(final PageRequestDTO request) {
        final Link link = linkService.getById(request.linkId());

        final Page page = new Page();
        page.setLink(link);
        page.setThumbnail(request.thumbnail());
        page.setCommentsEnabled(request.commentsEnabled());
        page.setPublished(request.published());

        return mapToResponseDTO(pageRepository.save(page));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageResponseDTO update(final UUID id, final PageRequestDTO request) {
        final Page existingPage = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + id));
        final Link link = linkService.getById(request.linkId());

        existingPage.setLink(link);
        existingPage.setThumbnail(request.thumbnail());
        existingPage.setCommentsEnabled(request.commentsEnabled());
        existingPage.setPublished(request.published());

        return mapToResponseDTO(pageRepository.save(existingPage));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public void delete(final UUID id) {
        final Page page = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + id));
        pageContentRepository.deleteAll(page.getContents());
        pageRepository.delete(page);
    }

    @PreAuthorize(Authorities.PAGE_READ)
    @Transactional(readOnly = true)
    public List<PageContentResponseDTO> findAllContentByPage(final UUID pageId) {
        final Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + pageId));
        return page.getContents().stream()
                .map(this::mapToContentResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.PAGE_READ)
    @Transactional(readOnly = true)
    public PageContentResponseDTO findContentByPageAndLanguage(final UUID pageId, final UUID languageId) {
        pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + pageId));
        languageService.getLanguageById(languageId); // This will throw if language doesn't exist
        return pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)
                .map(this::mapToContentResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, pageId, languageId)));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageContentResponseDTO createContent(final UUID pageId, final PageContentRequestDTO request) {
        final Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + pageId));
        final Language language = languageService.getLanguageById(request.languageId());

        if (pageContentRepository.existsByPageIdAndLanguageId(pageId, language.getId())) {
            throw new IllegalStateException(
                    String.format(CONTENT_EXISTS, pageId, language.getAbbreviation()));
        }

        final PageContent content = new PageContent();
        content.setPage(page);
        content.setLanguage(language);
        content.setTitle(request.title());
        content.setContent(request.content());
        content.setShortDescription(request.shortDescription());
        content.setKeywords(request.keywords());

        page.addContent(content);
        return mapToContentResponseDTO(pageContentRepository.save(content));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageContentResponseDTO updateContent(final UUID pageId, final UUID languageId, final PageContentRequestDTO request) {
        pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + pageId));
        languageService.getLanguageById(languageId); // This will throw if language doesn't exist
        final PageContent existingContent = pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, pageId, languageId)));

        existingContent.setTitle(request.title());
        existingContent.setContent(request.content());
        existingContent.setShortDescription(request.shortDescription());
        existingContent.setKeywords(request.keywords());

        return mapToContentResponseDTO(pageContentRepository.save(existingContent));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public void deleteContent(final UUID pageId, final UUID languageId) {
        final Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + pageId));
        final PageContent content = pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, pageId, languageId)));

        page.removeContent(content);
        pageContentRepository.delete(content);
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public void updatePublished(final UUID id, final boolean published) {
        final Page page = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PAGE_NOT_FOUND + id));
        page.setPublished(published);
        pageRepository.save(page);
    }

    private PageResponseDTO mapToResponseDTO(final Page page) {
        return new PageResponseDTO(
                page.getId(),
                page.getLink().getId(),
                page.getThumbnail(),
                page.isCommentsEnabled(),
                page.isPublished(),
                page.getContents().stream()
                        .map(this::mapToContentResponseDTO)
                        .collect(Collectors.toSet()),
                page.getCreatedAt(),
                page.getUpdatedAt());
    }

    private PageContentResponseDTO mapToContentResponseDTO(final PageContent content) {
        return new PageContentResponseDTO(
                content.getId(),
                content.getPage().getId(),
                content.getLanguage().getId(),
                content.getTitle(),
                content.getContent(),
                content.getShortDescription(),
                content.getKeywords(),
                content.getCreatedAt(),
                content.getUpdatedAt());
    }
}
