package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Student;
import com.example.demo.model.TaskType;
import com.example.demo.model.TimeEntry;
import com.example.demo.service.StudentService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api")
public class StudentController {

        private final StudentService studentService;

        StudentController(StudentService studentService) {
                this.studentService = studentService;
        }

        private List<Student> students = new ArrayList<>(Arrays.asList(
                        new Student(1l, "James", "2231121",
                                        Arrays.asList(new TimeEntry(1l, null, TaskType.CODING, "Programming",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(4), true),
                                                        new TimeEntry(2l, null, TaskType.REST, "Dinner",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusHours(1), false))),
                        new Student(2l, "Kim", "2231122",
                                        Arrays.asList(new TimeEntry(3l, null, TaskType.LECTURE, "Math",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(2), true),
                                                        new TimeEntry(4l, null, TaskType.EXAM_PREP, "Economic",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusHours(2), true))),
                        new Student(3l, "Jack", "2211121",
                                        Arrays.asList(new TimeEntry(5l, null, TaskType.PROJECT, "History",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(3), true),
                                                        new TimeEntry(6l, null, TaskType.REST, "Break",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusMinutes(15), false))),
                        new Student(4l, "Sam", "2231101",
                                        Arrays.asList(new TimeEntry(7l, null, TaskType.LAB, "Data base",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(5), true))),
                        new Student(5l, "Katty", "2251152",
                                        Arrays.asList(new TimeEntry(8l, null, TaskType.CODING,
                                                        "Programming the controller",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(6), true),
                                                        new TimeEntry(9l, null, TaskType.REST, "Break",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusMinutes(15), false)))));

        @GetMapping("/students")
        public List<Student> getStudents() {
                return studentService.getAll();
        }

        @GetMapping("/students/{id}")
        public ResponseEntity<Student> getStudent(@PathVariable Long id) {
                return ResponseEntity.ok().body(studentService.getById(id));
        }

        @PostMapping("/students")
        public ResponseEntity<Student> addStudent(@RequestBody @Valid Student student) {
                Student newStudent = studentService.create(student);
                return ResponseEntity.status(HttpStatus.CREATED).body(newStudent);
        }

        @PutMapping("/students/{id}")
        public ResponseEntity<Student> editStudent(@PathVariable Long id, @RequestBody @Valid Student student) {
                Student updated = studentService.update(id, student);
                if (updated != null) {
                        return ResponseEntity.ok(updated);
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

        @DeleteMapping("/students/{id}")
        public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
                if (studentService.deleteById(id)) {
                        ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok().build();
        }
}
