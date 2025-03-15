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
    public PageResponseDTO findById(UUID id) {
        return pageRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Page not found with id: " + id));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageResponseDTO create(PageRequestDTO request) {
        Link link = linkService.getById(request.linkId());

        Page page = new Page();
        page.setLink(link);
        page.setThumbnail(request.thumbnail());
        page.setCommentsEnabled(request.commentsEnabled());
        page.setPublished(request.published());

        return mapToResponseDTO(pageRepository.save(page));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageResponseDTO update(UUID id, PageRequestDTO request) {
        Page existingPage = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Page not found with id: " + id));
        Link link = linkService.getById(request.linkId());

        existingPage.setLink(link);
        existingPage.setThumbnail(request.thumbnail());
        existingPage.setCommentsEnabled(request.commentsEnabled());
        existingPage.setPublished(request.published());

        return mapToResponseDTO(pageRepository.save(existingPage));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public void delete(UUID id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Page not found with id: " + id));
        pageContentRepository.deleteAll(page.getContents());
        pageRepository.delete(page);
    }

    @PreAuthorize(Authorities.PAGE_READ)
    @Transactional(readOnly = true)
    public List<PageContentResponseDTO> findAllContentByPage(UUID pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("Page not found with id: " + pageId));
        return page.getContents().stream()
                .map(this::mapToContentResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.PAGE_READ)
    @Transactional(readOnly = true)
    public PageContentResponseDTO findContentByPageAndLanguage(UUID pageId, UUID languageId) {
        return pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)
                .map(this::mapToContentResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for page %s and language %s", pageId, languageId)));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageContentResponseDTO createContent(UUID pageId, PageContentRequestDTO request) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("Page not found with id: " + pageId));
        Language language = languageService.getLanguageById(request.languageId());

        if (pageContentRepository.existsByPageIdAndLanguageId(pageId, language.getId())) {
            throw new IllegalStateException(
                    String.format("Content already exists for page %s and language %s", pageId, language.getAbbreviation()));
        }

        PageContent content = new PageContent();
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
    public PageContentResponseDTO updateContent(UUID pageId, UUID languageId, PageContentRequestDTO request) {
        PageContent existingContent = pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for page %s and language %s", pageId, languageId)));

        existingContent.setTitle(request.title());
        existingContent.setContent(request.content());
        existingContent.setShortDescription(request.shortDescription());
        existingContent.setKeywords(request.keywords());

        return mapToContentResponseDTO(pageContentRepository.save(existingContent));
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public void deleteContent(UUID pageId, UUID languageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("Page not found with id: " + pageId));
        PageContent content = pageContentRepository.findByPageIdAndLanguageId(pageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for page %s and language %s", pageId, languageId)));

        page.removeContent(content);
        pageContentRepository.delete(content);
    }

    @PreAuthorize(Authorities.PAGE_WRITE)
    @Transactional
    public PageResponseDTO setPublished(UUID id, boolean published) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Page not found with id: " + id));
        page.setPublished(published);
        return mapToResponseDTO(pageRepository.save(page));
    }

    private PageResponseDTO mapToResponseDTO(Page page) {
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

    private PageContentResponseDTO mapToContentResponseDTO(PageContent content) {
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
