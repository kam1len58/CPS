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

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class StudentController {

        private final StudentService studentService;

        StudentController(StudentService studentService) {
                this.studentService = studentService;
        }

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

        @GetMapping("/studentFilter")
        public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String name,
                        @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {
                return ResponseEntity.ok(studentService.getByFilter(name, pageable));
        }

}
