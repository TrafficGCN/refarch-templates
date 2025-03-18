package de.muenchen.refarch.auth.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.muenchen.refarch.globalsettings.GlobalSettingsService;
import de.muenchen.refarch.globalsettings.dto.GlobalSettingsResponseDTO;
import de.muenchen.refarch.role.Role;
import de.muenchen.refarch.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final GlobalSettingsService globalSettingsService;

    private RSAKey rsaKey;
    private JWSSigner signer;
    private JWSVerifier verifier;

    @PostConstruct
    public void init() throws Exception {
        // Generate RSA key pair for signing/verifying JWTs
        // In production, this should be loaded from secure configuration
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("auth-key")
                .generate();
        signer = new RSASSASigner(rsaKey);
        verifier = new RSASSAVerifier(rsaKey.toPublicJWK());
    }

    public String generateToken(final User user) {
        try {
            final GlobalSettingsResponseDTO settings = globalSettingsService.getCurrentSettings();
            final int expirationMinutes = settings.sessionDurationMinutes(); // Use session duration for JWT expiration

            log.debug("Generating token for user: {}", user.getUsername());
            log.debug("User roles before mapping: {}", user.getRoles());

            final List<String> authorities = user.getRoles().stream()
                    .map(Role::getName)
                    .map(name -> name.startsWith("ROLE_") ? name : "ROLE_" + name)
                    .collect(Collectors.toList());

            log.debug("Mapped authorities for token: {}", authorities);

            // Build claims
            final JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .issuer("refarch-cms")
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .claim("authorities", authorities)
                    .claim("type", "password") // Distinguish from SSO tokens
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(
                            Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
                    .build();

            // Create and sign the JWT
            final SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaKey.getKeyID())
                            .build(),
                    claims);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Error generating JWT token", e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public boolean validateToken(final String token) {
        try {
            final SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify signature
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            // Verify expiration
            final JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            final Date expiration = claims.getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                return false;
            }

            // Verify it's a password-based token
            final String type = claims.getStringClaim("type");
            return "password".equals(type);

        } catch (Exception e) {
            return false;
        }
    }

    public JWTClaimsSet parseToken(final String token) {
        try {
            final SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }
}
