package com.example.demo.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.enums.TaskType;
import com.example.demo.model.TimeEntry;
import com.example.demo.service.TimeEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Управление временными записями", description = "CRUD операции для работы с временными записями (тайм-трекинг)")
@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

        private final TimeEntryService timeEntryService;

        TimeEntryController(TimeEntryService timeEntryService) {
                this.timeEntryService = timeEntryService;
        }

        @Operation(summary = "Получить все временные записи", description = """
                        Возвращает полный список всех временных записей в системе.
                        \n
                        **Возвращает:** Массив объектов TimeEntry
                        \n
                        **Структура временной записи:**
                        \n- id - уникальный идентификатор
                        \n- student - студент, которому принадлежит запись
                        \n- taskType - тип задачи
                        \n- description - описание задачи
                        \n- start - время начала работы
                        \n- end - время окончания работы (null если задача еще выполняется)
                        \n- duration - продолжительность в минутах
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Успешное получение списка записей"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для просмотра")
        })
        @GetMapping
        public List<TimeEntry> getAllTimeEntries() {
                return timeEntryService.getAll();
        }

        @Operation(summary = "Получить временную запись по ID", description = """
                        Возвращает информацию о конкретной временной записи по её идентификатору.
                        \n
                        **Параметры пути:**
                        \n- id - Уникальный идентификатор временной записи (обязательное поле)
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Запись найдена", content = @Content(schema = @Schema(implementation = TimeEntry.class))),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для просмотра"),
                        @ApiResponse(responseCode = "404", description = "Запись с указанным ID не найдена")
        })
        @GetMapping("/{id}")
        public ResponseEntity<TimeEntry> getTimeEntryById(
                        @Parameter(description = "ID временной записи", required = true, example = "1") @PathVariable Long id) {
                return ResponseEntity.ok().body(timeEntryService.getById(id));
        }

        @Operation(summary = "Создать новую временную запись", description = """
                        Создает новую запись о потраченном времени.
                        \n
                        **Параметры запроса (TimeEntry):**
                        \n- student - Студент (обязательное поле)
                        \n- taskType - Тип задачи (обязательное поле)
                        \n- description - Описание работы (опционально)
                        \n- start - Время начала (обязательное поле)
                        \n- end - Время окончания (опционально, можно установить позже)
                        \n
                        **Пример тела запроса:**
                        ```json
                        {
                          "student": {
                            "id": 1
                          },
                          "taskType": "STUDY",
                          "description": "Выполнение домашнего задания по математике",
                          "start": "2025-01-23T10:00:00",
                          "end": "2025-01-23T12:30:00"
                        }
                        ```
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Запись успешно создана", content = @Content(schema = @Schema(implementation = TimeEntry.class))),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для создания"),
                        @ApiResponse(responseCode = "404", description = "Студент не найден")
        })
        @PostMapping
        public ResponseEntity<TimeEntry> createTimeEntry(
                        @Parameter(description = "Данные новой временной записи", required = true) @RequestBody @Valid TimeEntry timeEntry) {
                TimeEntry newTimeEntry = timeEntryService.create(timeEntry);
                return ResponseEntity.status(HttpStatus.CREATED).body(newTimeEntry);
        }

        @Operation(summary = "Обновить временную запись", description = """
                        Обновляет информацию о существующей временной записи.
                        \n
                        **Параметры пути:**
                        \n- id - Уникальный идентификатор записи для обновления
                        \n
                        **Пример тела запроса:**
                        ```json
                        {
                          "description": "Обновленное описание работы",
                          "end": "2025-01-23T13:00:00"
                        }
                        ```
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Запись успешно обновлена", content = @Content(schema = @Schema(implementation = TimeEntry.class))),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для обновления"),
                        @ApiResponse(responseCode = "404", description = "Запись с указанным ID не найдена")
        })
        @PutMapping("/{id}")
        public ResponseEntity<TimeEntry> updateTimeEntry(
                        @Parameter(description = "ID записи для обновления", required = true, example = "1") @PathVariable Long id,
                        @Parameter(description = "Обновленные данные записи", required = true) @RequestBody @Valid TimeEntry timeEntry) {
                TimeEntry updated = timeEntryService.update(id, timeEntry);
                if (updated != null) {
                        return ResponseEntity.ok(updated);
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        @Operation(summary = "Удалить временную запись", description = """
                        Удаляет временную запись из системы по её идентификатору.
                        \n
                        **Параметры пути:**
                        \n- id - Уникальный идентификатор записи для удаления
                        \n
                        **Внимание:** Операция необратима
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Запись успешно удалена"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для удаления"),
                        @ApiResponse(responseCode = "404", description = "Запись с указанным ID не найдена")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteTimeEntry(
                        @Parameter(description = "ID записи для удаления", required = true, example = "1") @PathVariable Long id) {
                if (timeEntryService.deleteById(id)) {
                        return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Фильтрация временных записей", description = """
                        Возвращает отфильтрованный и пагинированный список временных записей.
                        \n
                        **Параметры запроса:**
                        \n- studentId - ID студента для фильтрации
                        \n- type - Тип задачи для фильтрации
                        \n- page - Номер страницы (по умолчанию: 0)
                        \n- size - Количество элементов на странице (по умолчанию: 10)
                        \n- sort - Поле для сортировки (по умолчанию: start)
                        \n
                        **Примеры запросов:**
                        \n1. `/api/time-entries/filter?studentId=1` - записи конкретного студента
                        \n2. `/api/time-entries/filter?type=STUDY` - записи с типом "STUDY"
                        \n3. `/api/time-entries/filter?studentId=1&type=HOMEWORK` - комбинированный фильтр
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Успешная фильтрация записей"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для просмотра"),
                        @ApiResponse(responseCode = "404", description = "Студент не найден")
        })
        @GetMapping("/filter")
        public ResponseEntity<Object> getTimeEntriesByFilter(
                        @Parameter(description = "ID студента для фильтрации", required = false, example = "1") @RequestParam(required = false) Long studentId,
                        @Parameter(description = "Тип задачи для фильтрации", required = false, schema = @Schema(implementation = TaskType.class)) @RequestParam(required = false) TaskType type,
                        @Parameter(description = "Параметры пагинации и сортировки", required = false) @PageableDefault(page = 0, size = 10, sort = "start") Pageable pageable) {
                return ResponseEntity.ok(timeEntryService.getByFilter(studentId, type, pageable));
        }

        @Operation(summary = "Начать отслеживание времени", description = """
                        Начинает отслеживание времени для конкретной задачи.
                        \n
                        **Параметры запроса:**
                        \n- studentId - ID студента (обязательное поле)
                        \n- type - Тип задачи (обязательное поле)
                        \n- estimated_hours - Расчетные часы
                        \n
                        **Возвращает:** Созданную временную запись с установленным временем начала
                        \n
                        **Пример запроса:**
                        `/api/time-entries/start?studentId=1&type=STUDY&description=Чтение+лекции`
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Отслеживание начато", content = @Content(schema = @Schema(implementation = TimeEntry.class))),
                        @ApiResponse(responseCode = "400", description = "У студента уже есть активная задача"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для создания"),
                        @ApiResponse(responseCode = "404", description = "Студент не найден")
        })
        @PostMapping("/start")
        public ResponseEntity<TimeEntry> startTimeTracking(
                        @Parameter(description = "ID студента", required = true, example = "1") @RequestParam Long studentId,
                        @Parameter(description = "Тип задачи", required = true, schema = @Schema(implementation = TaskType.class)) @RequestParam TaskType type,
                        @Parameter(description = "Описание работы", required = false) @RequestParam(required = false) String description) {
                TimeEntry entry = timeEntryService.start(studentId, type, description);
                return ResponseEntity.ok(entry);
        }

        @Operation(summary = "Завершить отслеживание времени", description = """
                        Завершает текущее отслеживание времени для студента.
                        \n
                        **Параметры запроса:**
                        \n- studentId - ID студента (обязательное поле)
                        \n
                        **Возвращает:** Обновленную временную запись с установленным временем окончания
                        \n
                        **Пример запроса:**
                        `/api/time-entries/stop?studentId=1`
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Отслеживание завершено", content = @Content(schema = @Schema(implementation = TimeEntry.class))),
                        @ApiResponse(responseCode = "400", description = "У студента нет активных задач"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для обновления"),
                        @ApiResponse(responseCode = "404", description = "Студент или активная запись не найдена")
        })
        @PostMapping("/stop")
        public ResponseEntity<TimeEntry> stopTimeTracking(
                        @Parameter(description = "ID студента", required = true, example = "1") @RequestParam Long studentId) {
                TimeEntry entry = timeEntryService.stop(studentId);
                return ResponseEntity.ok(entry);
        }

        @Operation(summary = "Получить записи за неделю", description = """
                        Возвращает все временные записи студента за текущую неделю.
                        \n
                        **Параметры запроса:**
                        \n- studentId - ID студента (обязательное поле)
                        \n
                        **Неделя определяется:** с понедельника текущей недели до воскресенья
                        \n
                        **Пример запроса:**
                        `/api/time-entries/weekly?studentId=1`
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Записи за неделю получены"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для просмотра"),
                        @ApiResponse(responseCode = "404", description = "Студент не найден")
        })
        @GetMapping("/weekly")
        public ResponseEntity<List<TimeEntry>> getWeeklyTimeEntries(
                        @Parameter(description = "ID студента", required = true, example = "1") @RequestParam Long studentId) {
                List<TimeEntry> entries = timeEntryService.getWeeklyEntries(studentId);
                return ResponseEntity.ok(entries);
        }
}