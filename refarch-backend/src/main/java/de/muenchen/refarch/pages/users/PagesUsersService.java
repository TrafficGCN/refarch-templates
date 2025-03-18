package de.muenchen.refarch.pages.users;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import de.muenchen.refarch.security.Authorities;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagesUsersService {
    private final PagesUsersRepository pagesUsersRepository;
    private final UserRepository userRepository;

    @PreAuthorize(Authorities.PAGES_USERS_READ)
    @Transactional(readOnly = true)
    public List<PagesUsers> findByPageLinkId(final UUID pageLinkId) {
        return pagesUsersRepository.findByPageLinkId(pageLinkId);
    }

    @PreAuthorize(Authorities.PAGES_USERS_READ)
    @Transactional(readOnly = true)
    public List<PagesUsers> findByUserId(final UUID userId) {
        return pagesUsersRepository.findByUserId(userId);
    }

    @PreAuthorize(Authorities.PAGES_USERS_WRITE)
    @Transactional
    public PagesUsers assignUserToPage(final UUID pageLinkId, final UUID userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        final PagesUsers pagesUsers = new PagesUsers();
        pagesUsers.setPageLinkId(pageLinkId);
        pagesUsers.setUser(user);
        return pagesUsersRepository.save(pagesUsers);
    }

    @PreAuthorize(Authorities.PAGES_USERS_WRITE)
    @Transactional
    public void removeUserFromPage(final UUID pageLinkId, final UUID userId) {
        pagesUsersRepository.deleteByPageLinkIdAndUserId(pageLinkId, userId);
    }
}
