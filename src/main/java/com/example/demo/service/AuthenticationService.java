package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ChangePasswordRequestDto;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Token;
import com.example.demo.model.User;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.CookieUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${jwt.access.duration.minute}")
    private long accessDurationMin;
    @Value("${jwt.access.duration.second}")
    private long accessDurationSec;
    @Value("${jwt.refresh.duration.days}")
    private long refreshDurationDate;
    @Value("${jwt.refresh.duration.second}")
    private long refreshDurationSec;

    private void addAccessTokenCookie(HttpHeaders headers, Token token) {
        logger.debug("Adding Access token cookie");
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createAccessCookie(token.getValue(), accessDurationSec).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
        logger.debug("Adding Refresh token cookie");
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createRefreshCookie(token.getValue(), refreshDurationSec).toString());
    }

    private void revokeAllTokens(User user) {
        logger.debug("Revoking all tokens for user: {}", user.getUsername());

        Set<Token> tokens = user.getTokens();

        tokens.forEach(token -> {
            if (token.getExpiringDate().isBefore(LocalDateTime.now())) {
                tokenRepository.delete(token);
                logger.debug("Expired token deleted for user: {}", user.getUsername());
            } else if (!token.isDisabled()) {
                token.setDisabled(true);
                tokenRepository.save(token);
                logger.debug("Token disabled for user: {}", user.getUsername());
            }
        });

        logger.info("All tokens revoked for user: {}", user.getUsername());
    }

    public ResponseEntity<LoginResponseDTO> login(LoginRequestDto request, String access, String refresh) {
        try {
            logger.info("Login operation started for user: {}", request.username());

            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.username(), request.password()));

            logger.debug("User authenticated: {}", request.username());

            User user = userService.getUser(request.username());
            logger.debug("User retrieved: {} (ID: {})", user.getUsername(), user.getId());

            boolean accessValid = jwtTokenProvider.isValid(access);
            boolean refreshValid = jwtTokenProvider.isValid(refresh);
            logger.debug("Token validation - Access: {}, Refresh: {}", accessValid, refreshValid);

            HttpHeaders headers = new HttpHeaders();

            revokeAllTokens(user);

            if (!accessValid) {
                logger.debug("Generating new Access token for user: {}", user.getUsername());

                Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
                        accessDurationMin, ChronoUnit.MINUTES, user);

                newAccess.setUser(user);
                addAccessTokenCookie(headers, newAccess);
                tokenRepository.save(newAccess);

                logger.info("New Access token created for user: {}", user.getUsername());
            }

            if (!refreshValid || accessValid) {
                logger.debug("Generating new Refresh token for user: {}", user.getUsername());

                Token newRefresh = jwtTokenProvider.generatedRefreshToken(refreshDurationDate, ChronoUnit.MINUTES,
                        user);

                newRefresh.setUser(user);
                addRefreshTokenCookie(headers, newRefresh);
                tokenRepository.save(newRefresh);

                logger.info("New Refresh token created for user: {}", user.getUsername());
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User login successful: {} (Role: {})", user.getUsername(), user.getRole().getName());

            return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(true, user.getRole().getName()));
        } catch (Exception e) {
            logger.error("Login failed for user: {} - {}", request.username(), e.getMessage(), e);
            throw e;
        }
    }

    public ResponseEntity<LoginResponseDTO> refresh(String refreshToken) {
        try {
            logger.info("Token refresh operation started");

            if (!jwtTokenProvider.isValid(refreshToken)) {
                logger.warn("Refresh token validation failed");
                throw new RuntimeException("token is invalid");
            }

            String username = jwtTokenProvider.getUsername(refreshToken);
            logger.debug("Retrieving user for refresh: {}", username);

            User user = userService.getUser(username);
            logger.debug("User found: {} (ID: {})", user.getUsername(), user.getId());

            logger.debug("Generating new Access token");
            Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
                    accessDurationMin, ChronoUnit.MINUTES, user);

            newAccess.setUser(user);
            HttpHeaders headers = new HttpHeaders();
            addAccessTokenCookie(headers, newAccess);
            tokenRepository.save(newAccess);

            logger.info("Token refreshed successfully for user: {}", user.getUsername());

            return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(true, user.getRole().getName()));
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ResponseEntity<LoginResponseDTO> logout(String accessToken) {
        try {
            logger.info("Logout operation started");

            SecurityContextHolder.clearContext();
            logger.debug("Security context cleared");

            String username = jwtTokenProvider.getUsername(accessToken);
            logger.debug("Retrieving user for logout: {}", username);

            User user = userService.getUser(username);
            logger.debug("User found: {} (ID: {})", user.getUsername(), user.getId());

            revokeAllTokens(user);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
            headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());

            logger.info("User logged out successfully: {}", user.getUsername());

            return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(false, null));
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserLoggedDto info() {
        try {
            logger.info("User info retrieval started");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof AnonymousAuthenticationToken) {
                logger.warn("Unauthenticated user attempted to access info");
                throw new RuntimeException("User is not authenticated");
            }

            String username = authentication.getName();
            logger.debug("Retrieving user info for: {}", username);

            User user = userService.getUser(username);
            logger.debug("User info retrieved: {} (ID: {})", user.getUsername(), user.getId());

            logger.info("User info sent successfully for: {}", user.getUsername());
            return UserMapper.userToUserLoggedDto(user);
        } catch (Exception e) {
            logger.error("User info retrieval failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ResponseEntity<LoginResponseDTO> changePassword(ChangePasswordRequestDto request) {
        try {
            logger.info("Password change operation started");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof AnonymousAuthenticationToken) {
                logger.warn("Unauthenticated user attempted to change password");
                throw new RuntimeException("User is not authenticated");
            }

            String username = authentication.getName();
            logger.debug("Retrieving user for password change: {}", username);

            User user = userService.getUser(username);
            logger.debug("User found: {} (ID: {})", user.getUsername(), user.getId());

            if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
                logger.warn("Password change failed - Invalid current password for user: {}", user.getUsername());
                throw new BadCredentialsException("Current password is invalid");
            }

            if (!request.newPassword().equals(request.newAgain())) {
                logger.warn("Password change failed - New passwords don't match for user: {}", user.getUsername());
                throw new BadCredentialsException("New passwords don't match each other");
            }

            logger.debug("Encoding new password for user: {}", user.getUsername());
            user.setPassword(passwordEncoder.encode(request.newPassword()));

            logger.debug("Saving updated user password");
            userRepository.save(user);
            logger.info("Password updated successfully for user: {}", user.getUsername());

            revokeAllTokens(user);
            SecurityContextHolder.clearContext();
            logger.debug("User tokens revoked and security context cleared");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
            headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());

            logger.info("Password change completed successfully for user: {}", user.getUsername());

            return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(false, null));
        } catch (Exception e) {
            logger.error("Password change failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}