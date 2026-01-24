package com.example.demo.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                        \n- email - электронная почта
                        \n- group - учебная группа
                        \n- timeEntries - список временных записей (тайм-трекинг)
                        \n
                        **Пример ответа:**
                        ```json
                        [
                          {
                            "id": 1,
                            "name": "Иванов Иван",
                            "email": "ivanov@example.com",
                            "group": "ИТ-101",
                            "timeEntries": [...]
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
                        \n- timeEntries - Список временных записей (опционально, можно добавить позже)
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
                        @ApiResponse(responseCode = "403", description = "Недостаточно прав для создания"),
                        @ApiResponse(responseCode = "409", description = "Студент с таким email уже существует")
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
                        \n- timeEntries - Список временных записей (полная замена существующих)
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
                        \n3. `/api/students/filter?name=петр&page=0&size=20&sort=email,desc` - с сортировкой по email
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
}