package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Student;

import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TimeEntryRepository;
import com.example.demo.specifications.StudentSpecifications;

import jakarta.annotation.PostConstruct;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;

@Service

public class StudentService {
    private final StudentRepository studentRepository;
    private final TimeEntryRepository timeEntryRepository;

    public StudentService(StudentRepository studentRepository, TimeEntryRepository timeEntryRepository) {
        this.studentRepository = studentRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    @PostConstruct
    public void init() {

    }

    @Cacheable(value = "students", key = "#root.methodName")
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public List<Student> getByTitle(String name) {
        return studentRepository.findAllByName(name);
    }

    @Transactional
    @CacheEvict(value = { "students", "student" }, allEntries = true)
    public Student create(Student student) {
        return studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    // @Cacheable(value = "student", key = "#id")
    public Student getById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Caching(evict = {
            @CacheEvict(value = "students", allEntries = true),
            @CacheEvict(value = "student", key = "#id")
    })
    @Transactional
    public Student update(Long id, Student student) {
        Student existingStudent = studentRepository.findById(id).orElse(null);

        if (existingStudent == null) {
            return null;
        }

        if (student.getName() != null && !student.getName().isBlank()) {
            existingStudent.setName(student.getName().trim());
        }

        existingStudent.setGroup(student.getGroup() != null ? student.getGroup().trim() : null);
        existingStudent.setRecentEntries(student.getRecentEntries());
        return studentRepository.save(existingStudent);
    }

    @Caching(evict = {
            @CacheEvict(value = "students", allEntries = true),
            @CacheEvict(value = "student", key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (studentRepository.existsById(id)) {
            timeEntryRepository.deleteByStudentId(id);
            studentRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Page<Student> getByFilter(String name, Pageable pageable) {
        return studentRepository.findAll(StudentSpecifications.filter(name), pageable);
    }
}
