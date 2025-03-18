package de.muenchen.refarch.pages.users;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagesUsersServiceTest {

    @Mock
    private PagesUsersRepository pagesUsersRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PagesUsersService pagesUsersService;

    private final UUID pageLinkId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final User user;
    private final PagesUsers pagesUsers;

    /* default */ PagesUsersServiceTest() {
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        pagesUsers = new PagesUsers();
        pagesUsers.setId(UUID.randomUUID());
        pagesUsers.setPageLinkId(pageLinkId);
        pagesUsers.setUser(user);
    }

    @Test
    void findByPageLinkId_ShouldReturnList() {
        when(pagesUsersRepository.findByPageLinkId(pageLinkId)).thenReturn(List.of(pagesUsers));

        final List<PagesUsers> result = pagesUsersService.findByPageLinkId(pageLinkId);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPageLinkId()).isEqualTo(pageLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(userId);
                });
        verify(pagesUsersRepository).findByPageLinkId(pageLinkId);
    }

    @Test
    void findByUserId_ShouldReturnList() {
        when(pagesUsersRepository.findByUserId(userId)).thenReturn(List.of(pagesUsers));

        final List<PagesUsers> result = pagesUsersService.findByUserId(userId);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(pu -> {
                    assertThat(pu.getPageLinkId()).isEqualTo(pageLinkId);
                    assertThat(pu.getUser().getId()).isEqualTo(userId);
                });
        verify(pagesUsersRepository).findByUserId(userId);
    }

    @Test
    void assignUserToPage_ShouldCreateAssociation() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pagesUsersRepository.save(any(PagesUsers.class))).thenReturn(pagesUsers);

        final PagesUsers result = pagesUsersService.assignUserToPage(pageLinkId, userId);

        assertThat(result.getPageLinkId()).isEqualTo(pageLinkId);
        assertThat(result.getUser().getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
        verify(pagesUsersRepository).save(any(PagesUsers.class));
    }

    @Test
    void removeUserFromPage_ShouldDeleteAssociation() {
        doNothing().when(pagesUsersRepository).deleteByPageLinkIdAndUserId(pageLinkId, userId);

        pagesUsersService.removeUserFromPage(pageLinkId, userId);

        verify(pagesUsersRepository).deleteByPageLinkIdAndUserId(pageLinkId, userId);
    }
}
