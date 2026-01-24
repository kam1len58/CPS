package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UploadResponseDto;
import com.example.demo.service.UploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Загрузка файлов", description = "Операции для загрузки и импорта данных из файлов")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
        private final UploadService uploadService;

        @Operation(summary = "Загрузить нормы времени", description = """
                        Загружает файл с нормами времени для различных типов задач.
                        \n
                        **Поддерживаемые форматы файлов:**
                        \n- CSV (Comma Separated Values)
                        \n- Excel (.xlsx, .xls)
                        \n
                        **Структура файла (CSV пример):**
                        ```csv
                        task_type,normal_hours,description
                        STUDY,2,Лекции и семинары
                        HOMEWORK,1.5,Домашнее задание
                        LAB,3,Лабораторная работа
                        ```
                        \n
                        **Поля в файле:**
                        \n- task_type - тип задачи (STUDY, HOMEWORK, LAB и т.д.)
                        \n- normal_hours - нормативное время в часах
                        \n- estimated_hours - расчётные часы
                        \n
                        **Возвращает:** Статистику импорта
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Файл успешно обработан, все записи импортированы", content = @Content(schema = @Schema(implementation = UploadResponseDto.class))),
                        @ApiResponse(responseCode = "206", description = "Файл частично обработан, некоторые записи не импортированы", content = @Content(schema = @Schema(implementation = UploadResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = """
                                        Некорректный запрос или формат файла. Возможные причины:
                                        \n- Неподдерживаемый формат файла
                                        \n- Неправильная структура файла
                                        \n- Отсутствуют обязательные поля
                                        \n- Некорректные данные в файле
                                        """, content = @Content(schema = @Schema(implementation = UploadResponseDto.class))),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для загрузки файлов"),
                        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера при обработке файла")
        })
        @PostMapping(value = "/time-norms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<UploadResponseDto> uploadTimeNorms(
                        @Parameter(description = """
                                        Файл с нормами времени.
                                        \n**Максимальный размер:** 10MB
                                        \n**Форматы:** CSV, Excel (.xlsx, .xls)
                                        """, required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam MultipartFile file) {
                try {
                        UploadResponseDto response = uploadService.importTypeAndTime(file);

                        if (response.failureCount() > 0) {
                                return ResponseEntity
                                                .status(HttpStatus.PARTIAL_CONTENT)
                                                .body(response);
                        }

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        return ResponseEntity
                                        .badRequest()
                                        .body(new UploadResponseDto(0, 0, 0));

                } catch (Exception e) {
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new UploadResponseDto(0, 0, 0));
                }
        }
}