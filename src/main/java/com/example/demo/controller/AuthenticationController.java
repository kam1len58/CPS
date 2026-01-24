package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequestDto;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Аутентификация", description = "Модуль для аутентификации и управления сессиями пользователей")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Вход в систему", description = """
            Аутентифицирует пользователя по логину и паролю.
            \n
            **Параметры запроса:**
            \n- username - имя пользователя
            \n- password - пароль пользователя
            \n
            **Возвращает:**
            \n- access-token и refresh-token в cookies
            \n- Статус входа и роль пользователя в теле ответа
            \n
            **Примечание:** Если токены уже существуют и валидны, они будут обновлены
            """)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @CookieValue(name = "access-token", required = false) String accessToken,
            @CookieValue(name = "refresh-token", required = false) String refreshToken,
            @RequestBody LoginRequestDto request) {
        return authenticationService.login(request, accessToken, refreshToken);
    }

    @Operation(summary = "Выход из системы", description = """
            Завершает текущую сессию пользователя.
            \n
            **Действие:**
            \n- Отзывает все активные токены пользователя
            \n- Очищает cookies с токенами
            \n- Очищает контекст безопасности
            \n
            **Требуется:** access-token в cookies
            \n
            **Примечание:** Работает даже с невалидным токеном (просто очистит cookies)
            """)
    @PostMapping("/logout")
    public ResponseEntity<LoginResponseDTO> logout(
            @CookieValue(name = "access-token", required = false) String accessToken) {
        return authenticationService.logout(accessToken);
    }

    @Operation(summary = "Обновление Access токена", description = """
            Обновляет истекший access-token с помощью refresh-token.
            \n
            **Требуется:** валидный refresh-token в cookies
            \n
            **Возвращает:** новый access-token в cookies
            \n
            **Примечание:** Refresh-token должен быть активным и не отозванным
            """)
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = "refresh-token", required = false) String refreshToken) {
        return authenticationService.refresh(refreshToken);
    }

    @Operation(summary = "Информация о текущем пользователе", description = """
            Возвращает информацию об аутентифицированном пользователе.
            \n
            **Требуется:** валидный access-token в cookies
            \n
            **Возвращает:**
            \n- username - имя пользователя
            \n- role - роль пользователя
            \n- permissions - набор разрешений пользователя
            \n
            **Ошибка 401:** если пользователь не аутентифицирован
            """)
    @GetMapping("/info")
    public ResponseEntity<UserLoggedDto> info() {
        return ResponseEntity.ok(authenticationService.info());
    }

    @Operation(summary = "Смена пароля", description = """
            Позволяет текущему пользователю изменить свой пароль.
            \n
            **Параметры запроса:**
            \n- oldPassword - текущий пароль (для подтверждения)
            \n- newPassword - новый пароль
            \n- newAgain - повтор нового пароля (для проверки)
            \n
            **Требования:**
            \n1. Пользователь должен быть аутентифицирован
            \n2. oldPassword должен совпадать с текущим паролем
            \n3. newPassword и newAgain должны совпадать
            \n4. newPassword должен отличаться от oldPassword
            \n
            **Действие после успешной смены:**
            \n- Все активные токены пользователя отзываются
            \n- Пользователь автоматически выходит из системы
            \n- Cookies очищаются
            """)
    @PatchMapping("/changePassword")
    public ResponseEntity<LoginResponseDTO> changePassword(@RequestBody ChangePasswordRequestDto request) {
        return authenticationService.changePassword(request);
    }
}