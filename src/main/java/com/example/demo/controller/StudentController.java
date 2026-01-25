package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.export.StudentReportsExportWrapper;
import com.example.demo.model.Student;
import com.example.demo.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Управление студентами", description = "CRUD операции для работы со студентами")
@RestController
@RequestMapping("/api/students")
public class StudentController {

        private final StudentService studentService;

        StudentController(StudentService studentService) {
                this.studentService = studentService;
        }

        @Operation(summary = "Получить всех студентов", description = """
                        Возвращает полный список всех студентов в системе.
                        \n
                        **Возвращает:** Массив объектов Student
                        \n
                        **Структура студента:**
                        \n- id - уникальный идентификатор
                        \n- name - имя студента
                        \n- group - учебная группа
                        \n- recentEntries - список последних временных записей (тайм-трекинг)
                        \n
                        **Пример ответа:**
                        ```json
                        [
                          {
                            "id": 1,
                            "name": "Иванов Иван",
                            "group": "ИТ-101",
                            "recentEntries": [...]
                          }
                        ]
                        ```
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Успешное получение списка студентов"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для просмотра")
        })
        @GetMapping
        public List<Student> getAllStudents() {
                return studentService.getAll();
        }

        @Operation(summary = "Получить студента по ID", description = """
                        Возвращает информацию о конкретном студенте по его идентификатору.
                        \n
                        **Параметры пути:**
                        \n- id - Уникальный идентификатор студента (обязательное поле)
                        \n
                        **Возвращает:** Объект Student
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Студент найден", content = @Content(schema = @Schema(implementation = Student.class))),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для просмотра"),
                        @ApiResponse(responseCode = "404", description = "Студент с указанным ID не найден")
        })
        @GetMapping("/{id}")
        public ResponseEntity<Student> getStudentById(
                        @Parameter(description = "ID студента", required = true, example = "1") @PathVariable Long id) {
                Student student = studentService.getById(id);
                if (student != null) {
                        return ResponseEntity.ok(student);
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        @Operation(summary = "Создать нового студента", description = """
                        Добавляет нового студента в систему.
                        \n
                        **Параметры запроса (Student):**
                        \n- name - Полное имя студента (обязательное поле)
                        \n- group - Учебная группа (опционально)
                        \n
                        **Примечание:** Поле id генерируется автоматически, не нужно передавать
                        \n
                        **Пример тела запроса:**
                        ```json
                        {
                          "name": "Петров Петр",
                          "group": "ИТ-201"
                        }
                        ```
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Студент успешно создан", content = @Content(schema = @Schema(implementation = Student.class))),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для создания")
        })
        @PostMapping
        public ResponseEntity<Student> createStudent(
                        @Parameter(description = "Данные нового студента", required = true) @RequestBody @Valid Student student) {
                Student newStudent = studentService.create(student);
                return ResponseEntity.status(HttpStatus.CREATED).body(newStudent);
        }

        @Operation(summary = "Обновить данные студента", description = """
                        Обновляет информацию о существующем студенте.
                        \n
                        **Параметры пути:**
                        \n- id - Уникальный идентификатор студента для обновления
                        \n
                        **Параметры запроса (Student):**
                        \n- name - Полное имя студента (обязательное поле)
                        \n- group - Учебная группа
                        \n
                        **Пример тела запроса:**
                        ```json
                        {
                          "name": "Петров Петр Сергеевич",
                          "group": "ИТ-202"
                        }
                        ```
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Данные студента успешно обновлены", content = @Content(schema = @Schema(implementation = Student.class))),
                        @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для обновления"),
                        @ApiResponse(responseCode = "404", description = "Студент с указанным ID не найден")
        })
        @PutMapping("/{id}")
        public ResponseEntity<Student> updateStudent(
                        @Parameter(description = "ID студента для обновления", required = true, example = "1") @PathVariable Long id,
                        @Parameter(description = "Обновленные данные студента", required = true) @RequestBody @Valid Student student) {
                Student updated = studentService.update(id, student);
                if (updated != null) {
                        return ResponseEntity.ok(updated);
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        @Operation(summary = "Удалить студента", description = """
                        Удаляет студента из системы по его идентификатору.
                        \n
                        **Параметры пути:**
                        \n- id - Уникальный идентификатор студента для удаления
                        \n
                        **Внимание:** Удаление студента также удаляет все связанные временные записи (timeEntries)
                        \n
                        **Примечание:** Операция необратима
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Студент успешно удален"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для удаления"),
                        @ApiResponse(responseCode = "404", description = "Студент с указанным ID не найден")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteStudent(
                        @Parameter(description = "ID студента для удаления", required = true, example = "1") @PathVariable Long id) {
                if (studentService.deleteById(id)) {
                        return ResponseEntity.noContent().build();
                }
                return ResponseEntity.notFound().build();
        }

        @Operation(summary = "Фильтрация студентов", description = """
                        Возвращает отфильтрованный и пагинированный список студентов.
                        \n
                        **Параметры запроса:**
                        \n- name - Фильтр по имени студента (частичное совпадение, нечувствительно к регистру)
                        \n- page - Номер страницы (по умолчанию: 0)
                        \n- size - Количество элементов на странице (по умолчанию: 10)
                        \n- sort - Поле для сортировки (по умолчанию: name)
                        \n
                        **Примеры запросов:**
                        \n1. `/api/students/filter?name=иван` - поиск студентов с именем содержащим "иван"
                        \n2. `/api/students/filter?page=1&size=5` - вторая страница по 5 студентов
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Успешная фильтрация студентов"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для просмотра")
        })
        @GetMapping("/filter")
        public ResponseEntity<Page<Student>> getStudentsByFilter(
                        @Parameter(description = "Фильтр по имени студента (частичное совпадение)", required = false, example = "Иван") @RequestParam(required = false) String name,
                        @Parameter(description = "Параметры пагинации и сортировки", required = false) @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {
                Pageable fixed = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                Sort.by("name"));
                return ResponseEntity.ok(studentService.getByFilter(name, fixed));
        }

        // === Импорт/Экспорт ===

        @Operation(summary = "Импорт студентов из XML", description = """
                        Импортировать данные о студентах из XML файла.
                        Требования к файлу: формат XML, размер не более 10MB
                        \n
                        **Структура XML файла:**
                        ```xml
                        <?xml version="1.0" encoding="UTF-8"?>
                        <studentsImport exportDate="2024-01-15" version="1.0" totalCount="2">
                          <student id="1">
                            <name>Иванов Иван</name>
                            <group>ИТ-101</group>
                          </student>
                          <student id="2">
                            <name>Петрова Мария</name>
                            <group>ИТ-102</group>
                          </student>
                        </studentsImport>
                        ```
                        \n
                        **Правила импорта:**
                        \n- Если студент с таким именем уже существует, обновляется его группа
                        \n- Если студента с таким именем нет, создается новый
                        \n- Поле id в XML используется только как справочная информация
                        """)
        @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<List<Student>> importStudentsFromXml(@RequestParam MultipartFile file) {
                try {
                        List<Student> importedStudents = studentService.importStudentsFromXmlFile(file);
                        return ResponseEntity.status(HttpStatus.CREATED).body(importedStudents);
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().build();
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
        }

        @Operation(summary = "Генерация PDF отчета по студентам", description = """
                        Сформировать PDF отчет о студентах и их временных записях.
                        \n
                        **Содержание отчета:**
                        \n- Список студентов с их временными записями
                        \n- Статистика по времени (общее, среднее, оплачиваемое)
                        \n- Типы задач с цветовым кодированием
                        \n- Информация об оплачиваемости задач
                        \n
                        **Формат:** PDF файл для скачивания
                        """)
        @GetMapping(value = "/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<ByteArrayResource> generateStudentsPdfReport() {
                try {
                        byte[] pdfContent = studentService.generateStudentsPdfReport();
                        String filename = "students_report_" +
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                                        + ".pdf";

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + filename + "\"")
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .contentLength(pdfContent.length)
                                        .body(new ByteArrayResource(pdfContent));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
        }

        @Operation(summary = "Генерация PDF отчета по временным записям студента", description = """
                        Сформировать детальный PDF отчет по временным записям конкретного студента.
                        \n
                        **Параметры пути:**
                        \n- studentId - ID студента
                        \n
                        **Содержание отчета:**
                        \n- Информация о студенте
                        \n- Детальный список всех временных записей
                        \n- Статистика по времени
                        \n- Распределение по типам задач
                        \n
                        **Формат:** PDF файл для скачивания
                        """)
        @GetMapping(value = "/{studentId}/time-entries/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<ByteArrayResource> generateStudentTimeEntriesPdfReport(
                        @Parameter(description = "ID студента", required = true, example = "1") @PathVariable Long studentId) {
                try {
                        byte[] pdfContent = studentService.generateStudentTimeEntriesPdfReport(studentId);
                        String filename = "student_" + studentId + "_time_entries_report_" +
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                                        + ".pdf";

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + filename + "\"")
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .contentLength(pdfContent.length)
                                        .body(new ByteArrayResource(pdfContent));
                } catch (RuntimeException e) {
                        return ResponseEntity.notFound().build();
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
        }

        @Operation(summary = "Экспорт студентов в JSON", description = """
                        Экспортировать данные о студентах в JSON формате.
                        \n
                        **Формат:** JSON файл для скачивания
                        """)
        @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<Student>> exportStudentsToJson() {
                try {
                        List<Student> students = studentService.getAll();
                        String filename = "students_export_" +
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
                                        + ".json";

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + filename + "\"")
                                        .body(students);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
        }
}