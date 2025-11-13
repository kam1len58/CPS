package com.example.demo.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @CookieValue(name = "access-token", required = false) String accessToken,
            @CookieValue(name = "refresh-token", required = false) String refreshToken,
            @RequestBody LoginRequestDto request) {
        return authenticationService.login(request, accessToken, refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDTO> logout(
            @CookieValue(name = "access-token", required = false) String accessToken) {
        return authenticationService.logout(accessToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = "refresh-token", required = false) String refreshToken) {
        return authenticationService.refresh(refreshToken);
    }

    @GetMapping("/info")
    public ResponseEntity<UserLoggedDto> info() {
        return ResponseEntity.ok(authenticationService.info());
    }
}
