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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HomepageService {

    private final HomepageRepository homepageRepository;
    private final HomepageContentRepository homepageContentRepository;
    private final LinkService linkService;
    private final LanguageService languageService;

    @Transactional(readOnly = true)
    public List<HomepageResponseDTO> findAll() {
        return homepageRepository.findAll().stream()
                .map(this::toHomepageResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HomepageResponseDTO findById(UUID id) {
        return homepageRepository.findById(id)
                .map(this::toHomepageResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Homepage not found with id: " + id));
    }

    public HomepageResponseDTO create(HomepageRequestDTO requestDTO) {
        Link link = linkService.getById(requestDTO.linkId());
        Homepage homepage = new Homepage();
        homepage.setLink(link);
        homepage.setThumbnail(requestDTO.thumbnail());
        return toHomepageResponseDTO(homepageRepository.save(homepage));
    }

    public HomepageResponseDTO update(UUID id, HomepageRequestDTO requestDTO) {
        Homepage homepage = homepageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Homepage not found with id: " + id));
        Link link = linkService.getById(requestDTO.linkId());
        homepage.setLink(link);
        homepage.setThumbnail(requestDTO.thumbnail());
        return toHomepageResponseDTO(homepageRepository.save(homepage));
    }

    public void delete(UUID id) {
        Homepage homepage = homepageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Homepage not found with id: " + id));
        homepageContentRepository.deleteAll(homepage.getContents());
        homepageRepository.delete(homepage);
    }

    @Transactional(readOnly = true)
    public List<HomepageContentResponseDTO> findAllContentByHomepage(UUID homepageId) {
        Homepage homepage = homepageRepository.findById(homepageId)
                .orElseThrow(() -> new EntityNotFoundException("Homepage not found with id: " + homepageId));
        return homepage.getContents().stream()
                .map(this::toHomepageContentResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HomepageContentResponseDTO findContentByHomepageAndLanguage(UUID homepageId, UUID languageId) {
        return homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId)
                .map(this::toHomepageContentResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for homepage %s and language %s", homepageId, languageId)));
    }

    public HomepageContentResponseDTO createContent(UUID homepageId, HomepageContentRequestDTO requestDTO) {
        Homepage homepage = homepageRepository.findById(homepageId)
                .orElseThrow(() -> new EntityNotFoundException("Homepage not found with id: " + homepageId));
        Language language = languageService.getLanguageById(requestDTO.languageId());

        if (homepageContentRepository.existsByHomepageIdAndLanguageId(homepageId, language.getId())) {
            throw new IllegalStateException(
                    String.format("Content already exists for homepage %s and language %s",
                            homepageId, language.getAbbreviation()));
        }

        HomepageContent content = new HomepageContent();
        content.setHomepage(homepage);
        content.setLanguage(language);
        updateContentFields(content, requestDTO);
        homepage.addContent(content);

        return toHomepageContentResponseDTO(homepageContentRepository.save(content));
    }

    public HomepageContentResponseDTO updateContent(UUID homepageId, UUID languageId,
            HomepageContentRequestDTO requestDTO) {
        HomepageContent content = homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for homepage %s and language %s", homepageId, languageId)));

        updateContentFields(content, requestDTO);
        return toHomepageContentResponseDTO(homepageContentRepository.save(content));
    }

    public void deleteContent(UUID homepageId, UUID languageId) {
        Homepage homepage = homepageRepository.findById(homepageId)
                .orElseThrow(() -> new EntityNotFoundException("Homepage not found with id: " + homepageId));
        HomepageContent content = homepageContentRepository.findByHomepageIdAndLanguageId(homepageId, languageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Content not found for homepage %s and language %s", homepageId, languageId)));

        homepage.removeContent(content);
        homepageContentRepository.delete(content);
    }

    private HomepageResponseDTO toHomepageResponseDTO(Homepage homepage) {
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

    private HomepageContentResponseDTO toHomepageContentResponseDTO(HomepageContent content) {
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

    private void updateContentFields(HomepageContent content, HomepageContentRequestDTO requestDTO) {
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
