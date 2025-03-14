package de.muenchen.refarch.pages.users;

import de.muenchen.refarch.user.User;
import de.muenchen.refarch.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagesUsersService {
    private final PagesUsersRepository pagesUsersRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PagesUsers> findByPageLinkId(UUID pageLinkId) {
        return pagesUsersRepository.findByPageLinkId(pageLinkId);
    }

    @Transactional(readOnly = true)
    public List<PagesUsers> findByUserId(UUID userId) {
        return pagesUsersRepository.findByUser_Id(userId);
    }

    @Transactional
    public PagesUsers assignUserToPage(UUID pageLinkId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        PagesUsers pagesUsers = new PagesUsers();
        pagesUsers.setPageLinkId(pageLinkId);
        pagesUsers.setUser(user);
        return pagesUsersRepository.save(pagesUsers);
    }

    @Transactional
    public void removeUserFromPage(UUID pageLinkId, UUID userId) {
        pagesUsersRepository.deleteByPageLinkIdAndUser_Id(pageLinkId, userId);
    }
}
