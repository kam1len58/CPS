package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repository.StudentRepository;

import jakarta.annotation.PostConstruct;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    private List<Student> students = new ArrayList<>();

    @PostConstruct
    public void init() {
        create(new Student(null, "unique_name_3", "group1", null));
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public List<Student> getByTitle(String name) {
        return studentRepository.findAllByName(name);
    }

    public Student create(Student student) {
        return studentRepository.save(student);
    }

    public Student getById(Long id) {
        for (Student student : students) {
            if (student.getId().equals(id)) {
                return studentRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public Student update(Long id, Student student) {
        return studentRepository.findById(id).map(existingStudent -> {
            existingStudent.setName(student.getName());
            existingStudent.setGroup(student.getGroup());
            existingStudent.setRecentEntries(student.getRecentEntries());
            return studentRepository.save(existingStudent);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
