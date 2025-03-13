package de.muenchen.refarch.link;

import de.muenchen.refarch.link.dto.LinkRequestDTO;
import de.muenchen.refarch.link.dto.LinkResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private LinkService linkService;

    private Link link;
    private LinkRequestDTO requestDTO;
    private LinkResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        final UUID linkId = UUID.randomUUID();
        link = new Link();
        link.setId(linkId);
        link.setUrl("https://example.com");
        link.setName("Example Link");
        link.setFontAwesomeIcon("fa-link");
        link.setMdiIcon("mdi-link");
        link.setType("external");
        link.setScope(LinkScope.EXTERNAL);

        requestDTO = new LinkRequestDTO(
                "https://example.com",
                "Example Link",
                "fa-link",
                "mdi-link",
                "external",
                LinkScope.EXTERNAL);

        responseDTO = new LinkResponseDTO(
                linkId,
                "https://example.com",
                "Example Link",
                "fa-link",
                "mdi-link",
                "external",
                LinkScope.EXTERNAL);
    }

    @Test
    void shouldReturnAllLinks() {
        when(linkRepository.findAll()).thenReturn(List.of(link));

        final List<LinkResponseDTO> result = linkService.getAllLinks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);
        verify(linkRepository).findAll();
    }

    @Test
    void shouldCreateLink() {
        when(linkRepository.save(any(Link.class))).thenReturn(link);

        final LinkResponseDTO result = linkService.createLink(requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(linkRepository).save(any(Link.class));
    }
}
