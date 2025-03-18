package de.muenchen.refarch.auth.token;

import de.muenchen.refarch.globalsettings.GlobalSettingsService;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import de.muenchen.refarch.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final GlobalSettingsService globalSettingsService;

    @Transactional
    public RefreshToken createRefreshToken(final User user, final String ipAddress, final String userAgent) {
        final GlobalSettingsResponseDTO settings = globalSettingsService.getCurrentSettings();
        final int expirationDays = settings.sessionDurationMinutes() / (24 * 60); // Convert minutes to days

        final RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(expirationDays))
                .lastUsed(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isValid(true)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidToken(final String token) {
        return refreshTokenRepository.findByTokenAndIsValidTrue(token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Transactional
    public void updateLastUsed(final RefreshToken token) {
        token.setLastUsed(LocalDateTime.now());
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeToken(final UUID tokenId) {
        refreshTokenRepository.findById(tokenId).ifPresent(token -> {
            token.setValid(false);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllUserTokens(final UUID userId) {
        refreshTokenRepository.deleteByUserIdAndIsValidTrue(userId);
    }

    @Transactional(readOnly = true)
    public List<RefreshToken> getActiveTokens(final UUID userId) {
        return refreshTokenRepository.findByUserIdAndIsValidTrueAndExpiresAtAfter(
                userId, LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
