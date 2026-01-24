package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DeadlinePredictionDTO;
import com.example.demo.model.RiskLevel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Управление дедлайнами", description = "CRUD операции для работы с дедлайнами и их прогнозированием")
@RestController
@RequestMapping("/api/deadlines")
public class DeadlinePredictionDtoController {
        private List<DeadlinePredictionDTO> deadlines = new ArrayList<>(Arrays.asList(
                        new DeadlinePredictionDTO(1L, "DB lab report", LocalDateTime.of(2025, 10, 15, 18, 0), 8.0,
                                        RiskLevel.HIGH),
                        new DeadlinePredictionDTO(2L, "Math homework", LocalDateTime.of(2025, 11, 23, 10, 0), 2.0,
                                        RiskLevel.LOW),
                        new DeadlinePredictionDTO(3L, "History Research Paper", LocalDateTime.of(2025, 9, 6, 13, 0),
                                        5.0,
                                        RiskLevel.MEDIUM),
                        new DeadlinePredictionDTO(4L, "Code Refactoring", LocalDateTime.of(2025, 12, 1, 9, 0), 12.0,
                                        RiskLevel.HIGH),
                        new DeadlinePredictionDTO(5L, "Economic exam prep", LocalDateTime.of(2025, 8, 4, 15, 0), 6.0,
                                        RiskLevel.MEDIUM)));

        @Operation(summary = "Получить все дедлайны", description = """
                        Возвращает список всех дедлайнов с прогнозами завершения.
                        \n
                        **Возвращает:** Массив объектов DeadlinePredictionDTO
                        \n
                        **Примеры дедлайнов в системе:**
                        \n- DB lab report (HIGH risk)
                        \n- Math homework (LOW risk)
                        \n- History Research Paper (MEDIUM risk)
                        \n- Code Refactoring (HIGH risk)
                        \n- Economic exam prep (MEDIUM risk)
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Успешное получение списка дедлайнов"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
        })
        @GetMapping
        public List<DeadlinePredictionDTO> getAllDeadlines() {
                return deadlines;
        }

        @Operation(summary = "Создать новый дедлайн", description = """
                        Добавляет новый дедлайн в систему.
                        \n
                        **Параметры запроса (DeadlinePredictionDTO):**
                        \n- title - Название задачи/проекта (обязательное поле)
                        \n- deadline - Дата и время дедлайна в формате ISO 8601 (обязательное поле)
                        \n- estimatedHours - Оценка времени на выполнение в часах (обязательное поле)
                        \n- riskLevel - Уровень риска: LOW, MEDIUM, HIGH (обязательное поле)
                        \n
                        **Примечание:** Поле id генерируется автоматически, не нужно передавать
                        \n
                        **Пример тела запроса:**
                        ```json
                        {
                          "title": "Spring Boot Project",
                          "deadline": "2025-12-31T23:59:00",
                          "estimatedHours": 40.5,
                          "riskLevel": "MEDIUM"
                        }
                        ```
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Дедлайн успешно создан"),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
        })
        @PostMapping
        public ResponseEntity<DeadlinePredictionDTO> createDeadline(
                        @Parameter(description = "Данные нового дедлайна", required = true) @RequestBody @Valid DeadlinePredictionDTO deadline) {
                deadline.setId((long) deadlines.size() + 1);
                deadlines.add(deadline);
                return ResponseEntity.status(HttpStatus.CREATED).body(deadline);
        }

        @Operation(summary = "Получить дедлайн по ID", description = """
                        Возвращает информацию о конкретном дедлайне по его идентификатору.
                        \n
                        **Параметры пути:**
                        \n- id - Уникальный идентификатор дедлайна (обязательное поле)
                        \n
                        **Возвращает:** Объект DeadlinePredictionDTO
                        \n
                        **Пример ответа:**
                        ```json
                        {
                          "id": 1,
                          "title": "DB lab report",
                          "deadline": "2025-10-15T18:00:00",
                          "estimatedHours": 8.0,
                          "riskLevel": "HIGH"
                        }
                        ```
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Дедлайн найден"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "404", description = "Дедлайн с указанным ID не найден")
        })
        @GetMapping("/{id}")
        public ResponseEntity<DeadlinePredictionDTO> getDeadlineById(
                        @Parameter(description = "ID дедлайна", required = true, example = "1") @PathVariable Long id) {
                for (DeadlinePredictionDTO deadline : deadlines) {
                        if (deadline.getId().equals(id)) {
                                return ResponseEntity.ok(deadline);
                        }
                }
                return ResponseEntity.notFound().build();
        }
}