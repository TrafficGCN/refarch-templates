package de.muenchen.refarch.homepage;

import de.muenchen.refarch.homepage.content.HomepageContent;
import de.muenchen.refarch.homepage.content.HomepageContentRepository;
import de.muenchen.refarch.homepage.content.dto.HomepageContentRequestDTO;
import de.muenchen.refarch.homepage.content.dto.HomepageContentResponseDTO;
import de.muenchen.refarch.homepage.dto.HomepageRequestDTO;
import de.muenchen.refarch.homepage.dto.HomepageResponseDTO;
import de.muenchen.refarch.language.Language;
import de.muenchen.refarch.language.LanguageService;
import de.muenchen.refarch.link.Link;
import de.muenchen.refarch.link.LinkService;
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
@Transactional
@RequiredArgsConstructor
public class HomepageService {

    private static final String HOMEPAGE_NOT_FOUND = "Homepage not found with id: ";
    private static final String CONTENT_NOT_FOUND = "Content not found for homepage %s and language %s";
    private static final String CONTENT_EXISTS = "Content already exists for homepage %s and language %s";

    private final HomepageRepository homepageRepository;
    private final HomepageContentRepository homepageContentRepository;
    private final LinkService linkService;
    private final LanguageService languageService;

    @PreAuthorize(Authorities.HOMEPAGE_READ)
    @Transactional(readOnly = true)
    public List<HomepageResponseDTO> findAll() {
        return homepageRepository.findAll().stream()
                .map(this::toHomepageResponseDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize(Authorities.HOMEPAGE_READ)
    @Transactional(readOnly = true)
    public HomepageResponseDTO findById(final UUID id) {
        return homepageRepository.findById(id)
                .map(this::toHomepageResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(HOMEPAGE_NOT_FOUND + id));
    }

    @PreAuthorize(Authorities.HOMEPAGE_WRITE)
    public HomepageResponseDTO create(final HomepageRequestDTO requestDTO) {
        final Link link = linkService.getById(requestDTO.linkId());
        final Homepage homepage = new Homepage();
        homepage.setLink(link);
        homepage.setThumbnail(requestDTO.thumbnail());
        return toHomepageResponseDTO(homepageRepository.save(homepage));
    }

    @PreAuthorize(Authorities.HOMEPAGE_WRITE)
    public HomepageResponseDTO update(final UUID id, final HomepageRequestDTO requestDTO) {
        final Homepage homepage = homepageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HOMEPAGE_NOT_FOUND + id));
        final Link link = linkService.getById(requestDTO.linkId());
        homepage.setLink(link);
        homepage.setThumbnail(requestDTO.thumbnail());
        return toHomepageResponseDTO(homepageRepository.save(homepage));
    }

    @PreAuthorize(Authorities.HOMEPAGE_WRITE)
    public void delete(final UUID id) {
        final Homepage homepage = homepageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HOMEPAGE_NOT_FOUND + id));
        homepageContentRepository.deleteAll(homepage.getContents());
        homepageRepository.delete(homepage);
    }

    @PreAuthorize(Authorities.HOMEPAGE_READ)
    @Transactional(readOnly = true)
    public List<HomepageContentResponseDTO> findAllContentByHomepage(final UUID homepageId) {
        final Homepage homepage = homepageRepository.findById(homepageId)
                .orElseThrow(() -> new EntityNotFoundException(HOMEPAGE_NOT_FOUND + homepageId));
        return homepage.getContents().stream()
                .map(this::toHomepageContentResponseDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize(Authorities.HOMEPAGE_READ)
    @Transactional(readOnly = true)
    public HomepageContentResponseDTO findContentByHomepageAndLanguage(final UUID homepageId, final UUID languageId) {
        return homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId)
                .map(this::toHomepageContentResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, homepageId, languageId)));
    }

    @PreAuthorize(Authorities.HOMEPAGE_WRITE)
    public HomepageContentResponseDTO createContent(final UUID homepageId, final HomepageContentRequestDTO requestDTO) {
        final Homepage homepage = homepageRepository.findById(homepageId)
                .orElseThrow(() -> new EntityNotFoundException(HOMEPAGE_NOT_FOUND + homepageId));
        final Language language = languageService.getLanguageById(requestDTO.languageId());

        if (homepageContentRepository.existsByHomepageIdAndLanguageId(homepageId, language.getId())) {
            throw new IllegalStateException(
                    String.format(CONTENT_EXISTS, homepageId, language.getAbbreviation()));
        }

        final HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        updateContentFields(content, requestDTO);
        homepage.addContent(content);

        return toHomepageContentResponseDTO(homepageContentRepository.save(content));
    }

    @PreAuthorize(Authorities.HOMEPAGE_WRITE)
    public HomepageContentResponseDTO updateContent(final UUID homepageId, final UUID languageId,
            final HomepageContentRequestDTO requestDTO) {
        final HomepageContent content = homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, homepageId, languageId)));

        updateContentFields(content, requestDTO);
        return toHomepageContentResponseDTO(homepageContentRepository.save(content));
    }

    @PreAuthorize(Authorities.HOMEPAGE_WRITE)
    public void deleteContent(final UUID homepageId, final UUID languageId) {
        final Homepage homepage = homepageRepository.findById(homepageId)
                .orElseThrow(() -> new EntityNotFoundException(HOMEPAGE_NOT_FOUND + homepageId));
        final HomepageContent content = homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(CONTENT_NOT_FOUND, homepageId, languageId)));

        homepage.removeContent(content);
        homepageContentRepository.delete(content);
    }

    private HomepageResponseDTO toHomepageResponseDTO(final Homepage homepage) {
        return new HomepageResponseDTO(
                homepage.getId(),
                homepage.getLink().getId(),
                homepage.getThumbnail(),
                homepage.getContents().stream()
                        .map(this::toHomepageContentResponseDTO)
                        .collect(Collectors.toSet()),
                homepage.getCreatedAt(),
                homepage.getUpdatedAt());
    }

    private HomepageContentResponseDTO toHomepageContentResponseDTO(final HomepageContent content) {
        return new HomepageContentResponseDTO(
                content.getId(),
                content.getHomepage().getId(),
                content.getLanguage().getId(),
                content.getWelcomeMessage(),
                content.getWelcomeMessageExtended(),
                content.getExploreOurWork(),
                content.getGetInvolved(),
                content.getImportantLinks(),
                content.getEcosystemLinks(),
                content.getBlog(),
                content.getPapers(),
                content.getReadMore(),
                content.getCreatedAt(),
                content.getUpdatedAt());
    }

    private void updateContentFields(final HomepageContent content, final HomepageContentRequestDTO requestDTO) {
        content.setWelcomeMessage(requestDTO.welcomeMessage());
        content.setWelcomeMessageExtended(requestDTO.welcomeMessageExtended());
        content.setExploreOurWork(requestDTO.exploreOurWork());
        content.setGetInvolved(requestDTO.getInvolved());
        content.setImportantLinks(requestDTO.importantLinks());
        content.setEcosystemLinks(requestDTO.ecosystemLinks());
        content.setBlog(requestDTO.blog());
        content.setPapers(requestDTO.papers());
        content.setReadMore(requestDTO.readMore());
    }
}
