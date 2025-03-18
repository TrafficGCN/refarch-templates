package de.muenchen.refarch.auth.api;

import de.muenchen.refarch.auth.service.JwtService;
import de.muenchen.refarch.auth.service.UserAuthenticationService;
import de.muenchen.refarch.auth.token.RefreshToken;
import de.muenchen.refarch.auth.token.RefreshTokenService;
import de.muenchen.refarch.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private static final String ERROR_KEY = "error";
    private final UserAuthenticationService authenticationService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public record LoginRequest(String email, String password) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody final LoginRequest request, final HttpServletRequest httpRequest) {
        try {
            log.debug("Attempting to authenticate user: {}", request.email());
            final User user = authenticationService.authenticateUser(request.email(), request.password());
            log.debug("User authenticated successfully: {}", user.getUsername());

            // Generate access token
            final String accessToken = jwtService.generateToken(user);
            log.debug("Generated access token for user: {}", user.getUsername());

            // Generate refresh token
            final RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user,
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent"));
            log.debug("Generated refresh token for user: {}", user.getUsername());

            final Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken.getToken());

            final Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("title", user.getTitle());
            userMap.put("affiliation", user.getAffiliation());
            userMap.put("thumbnail", user.getThumbnail());
            response.put("user", userMap);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.debug("Invalid credentials for user: {}", request.email());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR_KEY, "Invalid credentials"));
        } catch (Exception e) {
            log.error("Error during login for user: {}", request.email(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, "An error occurred during login"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody final RefreshRequest request) {
        return refreshTokenService.findValidToken(request.refreshToken())
                .map(refreshToken -> {
                    final User user = refreshToken.getUser();
                    final String newAccessToken = jwtService.generateToken(user);

                    // Update last used timestamp
                    refreshTokenService.updateLastUsed(refreshToken);

                    return ResponseEntity.ok(Map.of(
                            "accessToken", newAccessToken,
                            "refreshToken", refreshToken.getToken()));
                })
                .orElse(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(ERROR_KEY, "Invalid refresh token")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) final String authHeader,
            @RequestParam(required = false) final String refreshToken) {
        boolean validAccess = false;
        boolean validRefresh = false;

        // Validate access token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            validAccess = jwtService.validateToken(token);
        }

        // Invalidate refresh token if provided
        if (refreshToken != null) {
            refreshTokenService.findValidToken(refreshToken)
                    .ifPresent(token -> {
                        token.setValid(false);
                        refreshTokenService.revokeToken(token.getId());
                    });
            validRefresh = true;
        }

        if (validAccess || validRefresh) {
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(ERROR_KEY, "Invalid tokens"));
    }
}
