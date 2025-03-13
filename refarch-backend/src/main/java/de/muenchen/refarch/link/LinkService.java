package de.muenchen.refarch.link;

import de.muenchen.refarch.link.dto.LinkRequestDTO;
import de.muenchen.refarch.link.dto.LinkResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    @Transactional(readOnly = true)
    public List<LinkResponseDTO> getAllLinks() {
        return linkRepository.findAll().stream()
                .map(link -> new LinkResponseDTO(
                        link.getId(),
                        link.getLink(),
                        link.getName(),
                        link.getFontAwesomeIcon(),
                        link.getMdiIcon(),
                        link.getType(),
                        link.getScope()))
                .toList();
    }

    @Transactional
    public LinkResponseDTO createLink(final LinkRequestDTO request) {
        final Link link = new Link();
        link.setUrl(request.link());
        link.setName(request.name());
        link.setFontAwesomeIcon(request.fontAwesomeIcon());
        link.setMdiIcon(request.mdiIcon());
        link.setType(request.type());
        link.setScope(request.scope());

        final Link savedLink = linkRepository.save(link);
        return new LinkResponseDTO(
                savedLink.getId(),
                savedLink.getLink(),
                savedLink.getName(),
                savedLink.getFontAwesomeIcon(),
                savedLink.getMdiIcon(),
                savedLink.getType(),
                savedLink.getScope());
    }
}
