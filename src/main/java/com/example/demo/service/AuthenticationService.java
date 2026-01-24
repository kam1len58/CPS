package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

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
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createAccessCookie(token.getValue(), accessDurationSec).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE,
                cookieUtil.createRefreshCookie(token.getValue(), refreshDurationSec).toString());
    }

    private void revokeAllTokens(User user) {
        Set<Token> tokens = user.getTokens();

        tokens.forEach(token -> {
            if (token.getExpiringDate().isBefore(LocalDateTime.now()))
                tokenRepository.delete(token);
            else if (!token.isDisabled()) {
                token.setDisabled(true);
                tokenRepository.save(token);
            }
        });
    }

    public ResponseEntity<LoginResponseDTO> login(LoginRequestDto request, String access, String refresh) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(), request.password()));
        User user = userService.getUser(request.username());
        boolean accessValid = jwtTokenProvider.isValid(access);
        boolean refreshValid = jwtTokenProvider.isValid(refresh);

        HttpHeaders headers = new HttpHeaders();

        revokeAllTokens(user);

        if (!accessValid) {
            Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
                    accessDurationMin, ChronoUnit.MINUTES, user);

            newAccess.setUser(user);
            addAccessTokenCookie(headers, newAccess);
            tokenRepository.save(newAccess);
        }

        if (!refreshValid || accessValid) {
            Token newRefresh = jwtTokenProvider.generatedRefreshToken(refreshDurationDate, ChronoUnit.MINUTES, user);

            newRefresh.setUser(user);
            addRefreshTokenCookie(headers, newRefresh);
            tokenRepository.save(newRefresh);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponseDTO> refresh(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new RuntimeException("token is invalid");
        }

        User user = userService.getUser(jwtTokenProvider.getUsername(refreshToken));

        Token newAccess = jwtTokenProvider.generatedAccessToken(Map.of("role", user.getRole().getAuthority()),
                accessDurationMin, ChronoUnit.MINUTES, user);

        newAccess.setUser(user);
        HttpHeaders headers = new HttpHeaders();
        addAccessTokenCookie(headers, newAccess);
        tokenRepository.save(newAccess);

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponseDTO> logout(String accessToken) {
        SecurityContextHolder.clearContext();

        User user = userService.getUser(jwtTokenProvider.getUsername(accessToken));
        revokeAllTokens(user);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(false, null));
    }

    public UserLoggedDto info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated");
        }

        User user = userService.getUser(authentication.getName());

        return UserMapper.userToUserLoggedDto(user);
    }

    public ResponseEntity<LoginResponseDTO> changePassword(ChangePasswordRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken)
            throw new RuntimeException("User is not authenticated");

        User user = userService.getUser(authentication.getName());

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword()))
            throw new BadCredentialsException("Current password is invalid");

        if (!request.newPassword().equals(request.newAgain()))
            throw new BadCredentialsException("New passwords don't match each other");

        // Обновляем пароль напрямую
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        // Используем метод save репозитория напрямую
        userRepository.save(user);

        revokeAllTokens(user);
        SecurityContextHolder.clearContext();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteRefreshCookie().toString());

        return ResponseEntity.ok().headers(headers).body(new LoginResponseDTO(false, null));
    }
}
