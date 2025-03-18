package de.muenchen.refarch.link;

import de.muenchen.refarch.link.dto.LinkRequestDTO;
import de.muenchen.refarch.link.dto.LinkResponseDTO;
import de.muenchen.refarch.security.Authorities;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    @PreAuthorize(Authorities.LINK_READ)
    @Transactional(readOnly = true)
    public List<LinkResponseDTO> getAllLinks() {
        return linkRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @PreAuthorize(Authorities.LINK_READ)
    @Transactional(readOnly = true)
    public Link getById(final UUID id) {
        return linkRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Link not found with id: " + id));
    }

    @PreAuthorize(Authorities.LINK_WRITE)
    @Transactional
    public LinkResponseDTO createLink(final LinkRequestDTO request) {
        final Link link = new Link();
        link.setName(request.name());
        link.setUrl(request.link());
        link.setScope(request.scope());
        link.setFontAwesomeIcon(request.fontAwesomeIcon());
        link.setMdiIcon(request.mdiIcon());
        link.setType(request.type());

        return mapToResponseDTO(linkRepository.save(link));
    }

    @PreAuthorize(Authorities.LINK_WRITE)
    @Transactional
    public LinkResponseDTO updateLink(final UUID id, final LinkRequestDTO request) {
        final Link link = getById(id);
        link.setName(request.name());
        link.setUrl(request.link());
        link.setScope(request.scope());
        link.setFontAwesomeIcon(request.fontAwesomeIcon());
        link.setMdiIcon(request.mdiIcon());
        link.setType(request.type());

        return mapToResponseDTO(linkRepository.save(link));
    }

    @PreAuthorize(Authorities.LINK_WRITE)
    @Transactional
    public void deleteLink(final UUID id) {
        if (!linkRepository.existsById(id)) {
            throw new EntityNotFoundException("Link not found with id: " + id);
        }
        linkRepository.deleteById(id);
    }

    private LinkResponseDTO mapToResponseDTO(final Link link) {
        return new LinkResponseDTO(
                link.getId(),
                link.getLink(),
                link.getName(),
                link.getFontAwesomeIcon(),
                link.getMdiIcon(),
                link.getType(),
                link.getScope());
    }
}
